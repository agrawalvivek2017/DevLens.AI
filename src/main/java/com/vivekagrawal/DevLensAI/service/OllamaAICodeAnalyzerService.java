package com.vivekagrawal.DevLensAI.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vivekagrawal.DevLensAI.exception.AIServiceException;
import com.vivekagrawal.DevLensAI.model.CodeIssue;
import com.vivekagrawal.DevLensAI.service.ai.AICodeAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class OllamaAICodeAnalyzerService implements AICodeAnalyzerService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ollama.api.url:http://localhost:11434/api/generate}")
    private String ollamaApiUrl;

    @Value("${app.ai.model.type:llama3}")
    private String modelName;

    @Value("${app.ai.model.temperature:0.3}")
    private double temperature;

    @Value("${app.ai.model.max-tokens:2048}")
    private int maxTokens;

    @Override
    public AnalysisOutput analyzeCode(String code, String language) {
        try {
            String prompt = buildPrompt(code, language);
            log.debug("Sending code for analysis with model: {}", modelName);

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("prompt", prompt);
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            // Send request to Ollama API
            ResponseEntity<String> response = restTemplate.postForEntity(ollamaApiUrl, request, String.class);

            // Process response
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();

                // Parse response and extract the completion text
                JsonNode rootNode = objectMapper.readTree(responseBody);
                String generatedText = rootNode.path("response").asText();

                return parseAnalysisResult(generatedText);
            } else {
                throw new AIServiceException("Failed to get response from Ollama API: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error analyzing code with Ollama: {}", e.getMessage(), e);
            throw new AIServiceException("Failed to analyze code with AI model", e);
        }
    }

    /**
     * Builds the prompt for the AI model
     */
    private String buildPrompt(String code, String language) {
        StringBuilder builder = new StringBuilder();

        builder.append("You are an expert code reviewer with deep knowledge of software engineering principles, ");
        builder.append("best practices, security, and performance optimization. ");
        builder.append("Analyze the following ").append(language).append(" code for issues and provide structured feedback.\n\n");

        builder.append("CODE TO ANALYZE:\n```").append(language).append("\n");
        builder.append(code).append("\n```\n\n");

        builder.append("Analyze the code for:\n");
        builder.append("1. Performance issues\n");
        builder.append("2. Security vulnerabilities\n");
        builder.append("3. Code smells and anti-patterns\n");
        builder.append("4. Best practice violations\n");
        builder.append("5. Potential bugs\n");
        builder.append("6. Complexity issues\n");
        builder.append("7. Documentation and readability issues\n\n");

        builder.append("Provide your analysis in the following JSON format:\n");
        builder.append("{\n");
        builder.append("    \"summary\": \"Brief overall assessment of the code\",\n");
        builder.append("    \"overallScore\": 85, // 0-100 score\n");
        builder.append("    \"issues\": [\n");
        builder.append("        {\n");
        builder.append("            \"issueType\": \"PERFORMANCE|SECURITY|CODE_SMELL|BEST_PRACTICE|POTENTIAL_BUG|COMPLEXITY|DOCUMENTATION\",\n");
        builder.append("            \"severity\": \"CRITICAL|HIGH|MEDIUM|LOW|INFO\",\n");
        builder.append("            \"lineNumber\": 42, // optional, can be null for whole-file issues\n");
        builder.append("            \"description\": \"Detailed description of the issue\",\n");
        builder.append("            \"suggestion\": \"Recommendation to fix the issue\",\n");
        builder.append("            \"codeSnippet\": \"Problematic code segment\",\n");
        builder.append("            \"fixedCodeSnippet\": \"Suggested fixed code\"\n");
        builder.append("        }\n");
        builder.append("        // More issues...\n");
        builder.append("    ]\n");
        builder.append("}\n\n");

        builder.append("Include only real issues. If there are no serious issues, provide minor improvement suggestions.\n");
        builder.append("Ensure the JSON is valid and follows the schema exactly.");

        return builder.toString();
    }

    /**
     * Parses the AI model response into an AnalysisOutput
     */
    private AnalysisOutput parseAnalysisResult(String aiResponse) {
        try {
            // Attempt to extract JSON from the response (in case model includes explanatory text)
            String jsonStr = extractJsonFromResponse(aiResponse);

            // Parse the JSON
            Map<String, Object> resultMap = objectMapper.readValue(jsonStr, Map.class);

            String summary = (String) resultMap.get("summary");
            int overallScore = getIntValue(resultMap.get("overallScore"));

            List<Map<String, Object>> issuesMap = (List<Map<String, Object>>) resultMap.get("issues");
            List<DetectedIssue> detectedIssues = new ArrayList<>();
            List<CodeIssue.IssueType> detectedIssueTypes = new ArrayList<>();

            for (Map<String, Object> issueMap : issuesMap) {
                CodeIssue.IssueType issueType = CodeIssue.IssueType.valueOf((String) issueMap.get("issueType"));
                if (!detectedIssueTypes.contains(issueType)) {
                    detectedIssueTypes.add(issueType);
                }

                DetectedIssue issue = new DetectedIssue(
                        issueType,
                        CodeIssue.Severity.valueOf((String) issueMap.get("severity")),
                        issueMap.get("lineNumber") != null ? getIntValue(issueMap.get("lineNumber")) : null,
                        (String) issueMap.get("description"),
                        (String) issueMap.get("suggestion"),
                        (String) issueMap.get("codeSnippet"),
                        (String) issueMap.get("fixedCodeSnippet")
                );

                detectedIssues.add(issue);
            }

            return new AnalysisOutput(summary, overallScore, detectedIssueTypes, detectedIssues);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response: {}", e.getMessage(), e);
            throw new AIServiceException("Failed to parse AI analysis result", e);
        }
    }

    /**
     * Extracts the JSON portion from the AI response
     */
    private String extractJsonFromResponse(String response) {
        // Look for JSON between braces
        int startIdx = response.indexOf('{');
        int endIdx = response.lastIndexOf('}');

        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            return response.substring(startIdx, endIdx + 1);
        }

        // If no JSON found, return the original response and let the JSON parser handle it
        return response;
    }

    /**
     * Safely converts various number types to int
     */
    private int getIntValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        return 0;
    }
}