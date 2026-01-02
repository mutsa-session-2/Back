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
        String g = goal == null ? "" : goal.strip();
        String gl = g.toLowerCase(Locale.ROOT);

        if (gl.contains("spring") || gl.contains("스프링") || gl.contains("spring boot") || gl.contains("스프링부트") || gl.contains("스프링 부트")) {
            return buildSpringBootTitles(totalDays);
        }

        List<String> base = List.of(
            "목표 분석 + 학습 계획 세우기",
            "핵심 개념 학습 + 요약 노트 작성",
            "예제 따라하기(기본 실습)",
            "작은 과제(미니 실습) 수행",
            "오답/막힌 부분 정리 + 보완 학습",
            "복습 + 체크리스트 점검",
            "주간 회고 + 다음 주 목표 조정"
        );

        List<String> titles = new ArrayList<>(totalDays);
        for (int i = 0; i < totalDays; i++) {
            String t = base.get(i % base.size());
            if (!g.isBlank()) {
                titles.add(t + " (" + abbreviate(g, 24) + ")");
            } else {
                titles.add(t);
            }
        }
        return titles;
    }

    private List<String> buildSpringBootTitles(int totalDays) {
        List<String> curriculum = List.of(
            "개발 환경 세팅(JDK/Gradle/IDE) + 프로젝트 생성",
            "Spring Boot 구조 이해 + 실행/설정(application.properties)",
            "MVC 기초: Controller/RequestMapping 이해",
            "DTO/Validation(@Valid) 적용해보기",
            "REST API 설계 원칙 정리 + 응답 포맷 합의",
            "JPA 기초: Entity/Repository 생성",
            "연관관계 기초 + N+1 개념 맛보기",
            "Service 계층 설계 + 트랜잭션(@Transactional)",
            "에러 처리: 예외/에러 응답 포맷 정리",
            "Swagger(OpenAPI) 문서화 정리",
            "인증 기초: JWT 개념 + 필터 흐름 이해",
            "Spring Security 설정 읽기 + 인증/인가 구분",
            "테스트 기초: 단위 테스트 vs 통합 테스트",
            "Repository 테스트 작성 + 테스트 데이터 전략",
            "API 테스트(Postman) 시나리오 작성",
            "페이징/정렬/검색 기본 구현",
            "파일 업로드/다운로드 기본 구현",
            "환경변수/프로파일(dev/prod) 분리",
            "로깅 전략: log level/민감정보 마스킹",
            "성능 기초: 쿼리 로그로 병목 찾기",
            "배포 준비: jar 빌드 + 실행 옵션 정리",
            "Docker 기초: Dockerfile 작성/빌드",
            "배포 체크리스트 작성 + 위험요소 점검",
            "CI 기초: Gradle test 자동화",
            "미니 프로젝트: CRUD API 스펙 정의",
            "미니 프로젝트: 구현 + 예외/검증",
            "미니 프로젝트: 테스트/문서화",
            "미니 프로젝트: 리팩터링(레이어/네이밍)",
            "최종 회고 + 다음 단계(고급 JPA/보안/배포) 계획"
        );

        List<String> titles = new ArrayList<>(totalDays);
        for (int i = 0; i < totalDays; i++) {
            if (i < curriculum.size()) {
                titles.add(curriculum.get(i));
                continue;
            }
            int cycle = (i - curriculum.size()) % 7;
            String t = switch (cycle) {
                case 0 -> "심화: JPA 연관관계/쿼리 튜닝 복습";
                case 1 -> "심화: Security 인가 정책/권한 설계";
                case 2 -> "심화: 테스트 커버리지 개선";
                case 3 -> "심화: 예외/에러 표준화";
                case 4 -> "심화: 배포 파이프라인 정리";
                case 5 -> "심화: 작은 기능 추가(개선사항 반영)";
                default -> "회고 + 다음 목표 업데이트";
            };
            titles.add(t);
        }
        return titles;
    }

    private String abbreviate(String s, int max) {
        if (s == null) return "";
        String trimmed = s.strip();
        if (trimmed.length() <= max) return trimmed;
        return trimmed.substring(0, Math.max(0, max - 1)) + "…";
    }
}
