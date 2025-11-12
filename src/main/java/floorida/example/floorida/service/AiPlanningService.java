package floorida.example.floorida.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class AiPlanningService {

    @Value("${OPENAI_API_KEY:}")
    private String openAiKey; // never log this

    private final ObjectMapper om = new ObjectMapper();

    public record AiFloor(String title, LocalDate date) {}

    public List<AiFloor> plan(String goal, LocalDate start, LocalDate end) {
        // If no key is present, fallback to deterministic local logic
        if (openAiKey == null || openAiKey.isBlank()) {
            return fallbackPlan(goal, start, end);
        }
        try {
            String prompt = "You're a planning assistant. Split the user's goal into a set of daily steps between the given dates. Return ONLY strict JSON with the following shape: {\"floors\":[{\"title\":string,\"date\":\"YYYY-MM-DD\"}...]}. Dates must be within the inclusive range and sorted.";

            ObjectNode body = om.createObjectNode();
            body.put("model", "gpt-4o-mini");
            var messages = om.createArrayNode();
            messages.add(om.createObjectNode().put("role", "system").put("content", prompt));
            messages.add(om.createObjectNode().put("role", "user").put("content",
                    String.format("goal: %s\nstart: %s\nend: %s", goal, start, end)));
            body.set("messages", messages);
            body.put("temperature", 0.2);

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
                    return fallbackPlan(goal, start, end);
                }
                // content should be JSON; if it contains backticks or code fences, strip quickly
                String cleaned = content.strip();
                if (cleaned.startsWith("```)")) {
                    // rare malformed fence; ignore
                }
                if (cleaned.startsWith("```")) {
                    int idx = cleaned.indexOf('\n');
                    int last = cleaned.lastIndexOf("```");
                    if (idx > 0 && last > idx) cleaned = cleaned.substring(idx + 1, last);
                }

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
                if (!result.isEmpty()) return result;
            }
        } catch (Exception ignore) {
            // fall through to fallback
        }
        return fallbackPlan(goal, start, end);
    }

    private List<AiFloor> fallbackPlan(String goal, LocalDate start, LocalDate end) {
        List<AiFloor> result = new ArrayList<>();
        LocalDate cursor = start;
        int idx = 1;
        while (!cursor.isAfter(end)) {
            result.add(new AiFloor(goal + " - 단계 " + idx, cursor));
            cursor = cursor.plusDays(1);
            idx++;
        }
        return result;
    }
}
