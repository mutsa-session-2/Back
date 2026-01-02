package floorida.example.floorida.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AiPlanningService {

    private static final Logger log = LoggerFactory.getLogger(AiPlanningService.class);

    @Value("${OPENAI_API_KEY:}")
    private String openAiKey; // never log this

    private final ObjectMapper om = new ObjectMapper();

    public record AiFloor(String title, LocalDate date) {}

    public List<AiFloor> plan(String goal, LocalDate start, LocalDate end) {
        // If no key is present, fallback to deterministic local logic
        if (openAiKey == null || openAiKey.isBlank()) {
            log.warn("OPENAI_API_KEY is missing/blank; using fallback plan");
            return fallbackPlan(goal, start, end);
        }
        try {
            String prompt = """
                You are a planning assistant.
                Create a concrete day-by-day plan between the given dates (inclusive).

                Output rules:
                - Return ONLY strict JSON (no markdown, no explanations).
                - JSON shape: {"floors":[{"title":string,"date":"YYYY-MM-DD"}...]}
                - Include EXACTLY one item per date in the range.
                - Dates must be within the range and sorted ascending.
                - Titles must be specific and actionable in Korean.
                - Avoid generic titles like "단계 1" / "Step 1".
                """;

            ObjectNode body = om.createObjectNode();
            body.put("model", "gpt-4o-mini");
            var messages = om.createArrayNode();
            messages.add(om.createObjectNode().put("role", "system").put("content", prompt));
            messages.add(om.createObjectNode().put("role", "user").put("content",
                    String.format("goal: %s\nstart: %s\nend: %s", goal, start, end)));
            body.set("messages", messages);
            body.put("temperature", 0.2);
            body.put("max_tokens", 1200);
            // Ask for JSON-only output. If unsupported, API will ignore this field.
            body.set("response_format", om.createObjectNode().put("type", "json_object"));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openAiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                JsonNode root = om.readTree(resp.body());
                String content = root.path("choices").path(0).path("message").path("content").asText("");
                if (content == null || content.isBlank()) {
                    log.warn("OpenAI response content was blank; using fallback plan");
                    return fallbackPlan(goal, start, end);
                }

                String cleaned = extractJsonObject(content);
                JsonNode json = om.readTree(cleaned);
                List<AiFloor> result = new ArrayList<>();
                for (JsonNode f : json.path("floors")) {
                    String title = f.path("title").asText("");
                    String dateStr = f.path("date").asText("");
                    if (!title.isBlank() && !dateStr.isBlank()) {
                        LocalDate d = LocalDate.parse(dateStr);
                        if (!d.isBefore(start) && !d.isAfter(end)) {
                            result.add(new AiFloor(title, d));
                        }
                    }
                }
                if (!result.isEmpty()) {
                    // If model output is too generic (e.g., "단계 1"), replace with heuristic titles.
                    if (looksGenericSteps(result)) {
                        log.warn("OpenAI returned generic step titles; replacing with heuristic titles");
                        return heuristicPlan(goal, start, end);
                    }
                    return result;
                }
                log.warn("OpenAI JSON parsed but produced empty floors; using fallback plan");
                return fallbackPlan(goal, start, end);
            }
            log.warn("OpenAI API call failed (status={}); using fallback plan", resp.statusCode());
        } catch (Exception e) {
            // fall through to fallback
            log.warn("OpenAI planning failed; using fallback plan", e);
        }
        return fallbackPlan(goal, start, end);
    }

    /**
     * AI가 생성한 결과를 날짜 범위(포함) 기준으로 보정합니다.
     * - 날짜 누락/중복을 제거하고 범위 내 모든 날짜에 대해 1개씩 생성
     * - 제목이 '단계 n'처럼 너무 일반적이면 휴리스틱 제목으로 치환
     */
    public List<AiFloor> sanitizeAiFloors(String goal, LocalDate start, LocalDate end, List<AiFloor> floors) {
        int totalDays = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        List<String> fallbackTitles = buildHeuristicTitles(goal, totalDays);

        Map<LocalDate, String> byDate = new HashMap<>();
        if (floors != null) {
            for (AiFloor f : floors) {
                if (f == null || f.date() == null) continue;
                LocalDate d = f.date();
                if (d.isBefore(start) || d.isAfter(end)) continue;
                String t = f.title() == null ? "" : f.title().strip();
                if (t.isBlank()) continue;
                // keep first occurrence only
                byDate.putIfAbsent(d, t);
            }
        }

        List<AiFloor> normalized = new ArrayList<>(totalDays);
        for (int i = 0; i < totalDays; i++) {
            LocalDate date = start.plusDays(i);
            String title = byDate.getOrDefault(date, "");
            if (title.isBlank()) {
                title = fallbackTitles.get(i);
            } else {
                String tl = title.toLowerCase(Locale.ROOT);
                if (tl.matches(".*(단계\\s*\\d+|step\\s*\\d+).*")) {
                    title = fallbackTitles.get(i);
                }
            }
            normalized.add(new AiFloor(title, date));
        }
        return normalized;
    }

    private List<AiFloor> fallbackPlan(String goal, LocalDate start, LocalDate end) {
        // Fallback still should be usable (avoid "단계 n")
        return heuristicPlan(goal, start, end);
    }

    private List<AiFloor> heuristicPlan(String goal, LocalDate start, LocalDate end) {
        int totalDays = (int) (end.toEpochDay() - start.toEpochDay()) + 1;
        List<String> titles = buildHeuristicTitles(goal, totalDays);
        List<AiFloor> result = new ArrayList<>(totalDays);
        for (int i = 0; i < totalDays; i++) {
            result.add(new AiFloor(titles.get(i), start.plusDays(i)));
        }
        return result;
    }

    private boolean looksGenericSteps(List<AiFloor> floors) {
        if (floors == null || floors.isEmpty()) return false;
        int generic = 0;
        for (AiFloor f : floors) {
            String t = f.title() == null ? "" : f.title().toLowerCase(Locale.ROOT).strip();
            if (t.matches(".*(단계\\s*\\d+|step\\s*\\d+).*")) {
                generic++;
            }
        }
        // 대부분이 "단계 n"류면 품질이 낮다고 판단
        return generic >= Math.max(2, (int) Math.ceil(floors.size() * 0.6));
    }

    /**
     * 모델 응답이 설명/코드펜스/앞뒤 텍스트를 섞어 보내도 JSON 객체만 뽑아냅니다.
     */
    private String extractJsonObject(String raw) {
        if (raw == null) return "{}";
        String s = raw.strip();

        if (s.startsWith("```")) {
            int firstNewline = s.indexOf('\n');
            int lastFence = s.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                s = s.substring(firstNewline + 1, lastFence).strip();
            }
        }

        int firstBrace = s.indexOf('{');
        int lastBrace = s.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return s.substring(firstBrace, lastBrace + 1);
        }
        return s;
    }

    private List<String> buildHeuristicTitles(String goal, int totalDays) {
        List<String> titles = new ArrayList<>(totalDays);
        for (int i = 0; i < totalDays; i++) {
            titles.add((i + 1) + "일 차");
        }
        return titles;
    }
}
