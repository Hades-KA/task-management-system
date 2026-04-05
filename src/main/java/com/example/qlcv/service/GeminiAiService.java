package com.example.qlcv.service;

import com.example.qlcv.dto.AiPlanResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;

/**
 * Service gọi Gemini API để sinh kế hoạch dự án.
 */
@Service
public class GeminiAiService {

    @Value("${ai.gemini.api-key:}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-2.5-flash}")
    private String model;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    /**
     * Gọi Gemini để sinh ra kế hoạch task từ mô tả dự án.
     */
    public AiPlanResponseDto generatePlan(String projectName,
                                          String projectDescription,
                                          LocalDate startDate,
                                          String requirement,
                                          String memberContext) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("Chưa cấu hình ai.gemini.api-key trong application.properties");
            }

            String prompt = buildPrompt(projectName, projectDescription, startDate, requirement, memberContext);
            String requestBody = buildRequestBody(prompt);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            int status = response.statusCode();
            String body = response.body();

            if (status == 404) {
                throw new IllegalStateException("Model Gemini không tồn tại hoặc không được hỗ trợ: " + model);
            }

            if (status == 429) {
                throw new IllegalStateException("Gemini đang bị giới hạn quota/rate limit free tier. Hãy đợi khoảng 1 phút rồi thử lại.");
            }

            if (status >= 400) {
                throw new IllegalStateException("Gemini API lỗi (HTTP " + status + "): " + body);
            }

            JsonNode root = objectMapper.readTree(body);

            JsonNode textNode = root.path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text");

            String text = textNode.asText();

            if (text == null || text.isBlank()) {
                throw new IllegalStateException("Gemini không trả về nội dung hợp lệ.");
            }

            text = cleanJson(text);

            AiPlanResponseDto result;
            try {
                result = objectMapper.readValue(text, AiPlanResponseDto.class);
            } catch (Exception e) {
                throw new IllegalStateException("AI đã phản hồi nhưng không đúng JSON mong đợi. Nội dung nhận được: " + text);
            }

            if (result.getTasks() == null) {
                result.setTasks(new java.util.ArrayList<>());
            }

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo kế hoạch AI: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo request body gửi lên Gemini.
     * Dùng responseMimeType để ép AI ưu tiên trả JSON.
     */
    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();

        ArrayNode contents = objectMapper.createArrayNode();
        ObjectNode content = objectMapper.createObjectNode();
        ArrayNode parts = objectMapper.createArrayNode();
        ObjectNode part = objectMapper.createObjectNode();

        part.put("text", prompt);
        parts.add(part);
        content.set("parts", parts);
        contents.add(content);

        root.set("contents", contents);

        ObjectNode generationConfig = objectMapper.createObjectNode();
        generationConfig.put("temperature", 0.2);
        generationConfig.put("responseMimeType", "application/json");
        root.set("generationConfig", generationConfig);

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Prompt yêu cầu AI trả về đúng JSON.
     */
    private String buildPrompt(String projectName,
                               String projectDescription,
                               LocalDate startDate,
                               String requirement,
                               String memberContext) {
        return """
        Bạn là trợ lý quản lý dự án kiểu Jira mini.

        Hãy phân tích yêu cầu dự án và sinh ra kế hoạch công việc bằng tiếng Việt.

        THÔNG TIN DỰ ÁN:
        - Tên dự án: %s
        - Mô tả dự án hiện có: %s
        - Ngày bắt đầu dự kiến: %s
        - Yêu cầu người dùng nhập:
        %s

        THÀNH VIÊN / BỐI CẢNH NHÂN SỰ:
        %s

        YÊU CẦU ĐẦU RA:
        Trả về DUY NHẤT một JSON hợp lệ.
        Không markdown.
        Không bọc ```json.
        Không giải thích thêm.
        Không thêm bất kỳ chữ nào ngoài JSON.

        JSON đúng theo schema:
        {
          "summary": "Tóm tắt tổng quan dự án",
          "timelineSummary": "Kế hoạch tổng thể theo giai đoạn hoặc tuần",
          "tasks": [
            {
              "title": "Tên công việc",
              "description": "Mô tả chi tiết việc cần làm",
              "taskType": "TASK",
              "priority": "MEDIUM",
              "estimatedDays": 2,
              "suggestedAssignee": "",
              "suggestedReason": ""
            }
          ]
        }

        RÀNG BUỘC:
        - Sinh từ 4 đến 8 task.
        - taskType chỉ dùng TASK, BUG, FEATURE.
        - priority chỉ dùng LOW, MEDIUM, HIGH.
        - estimatedDays là số nguyên từ 1 đến 5.
        - suggestedAssignee nếu có thì chỉ chọn trong danh sách thành viên được cung cấp.
        - Nếu không chắc thì để suggestedAssignee là chuỗi rỗng.
        - description phải cụ thể, thực tế, ngắn gọn.
        - summary và timelineSummary phải là chuỗi tiếng Việt rõ ràng.
        """.formatted(
                safe(projectName),
                safe(projectDescription),
                startDate != null ? startDate : "",
                safe(requirement),
                safe(memberContext)
        );
    }

    /**
     * Làm sạch JSON nếu AI lỡ bọc markdown.
     */
    private String cleanJson(String raw) {
        if (raw == null) return "";
        return raw.trim()
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}