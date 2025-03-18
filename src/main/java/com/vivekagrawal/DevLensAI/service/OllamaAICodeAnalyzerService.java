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

    @Value("${app.ai.model.type:phi}")
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
            // Add system message to enforce JSON output
            requestBody.put("system", "You are a helpful code analysis assistant. Always respond with valid JSON in the requested format. Never include explanations outside the JSON.");

            // Prepare headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Create request entity
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            try {
                // Send request to Ollama API
                ResponseEntity<String> response = restTemplate.postForEntity(ollamaApiUrl, request, String.class);

                // Process response
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String responseBody = response.getBody();

                    // Parse response and extract the completion text
                    //JsonNode rootNode = objectMapper.readTree(responseBody);
                    String generatedText = parseFullResponse(responseBody);

                    return parseAnalysisResult(generatedText);
                } else {
                    log.warn("Received non-successful response from Ollama API: {}", response.getStatusCode());
                    throw new AIServiceException("Failed to get response from Ollama API: " + response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("Error communicating with Ollama API: {}", e.getMessage());
                // If API communication fails, create a fallback analysis result
                return createFallbackAnalysisResult(code, language, e.getMessage());
            }
        } catch (Exception e) {
            log.error("Error analyzing code with Ollama: {}", e.getMessage(), e);
            throw new AIServiceException("Failed to analyze code with AI model", e);
        }
    }

    String parseFullResponse(String responseBody) {
        String[] jsonLines = responseBody.trim().split("\n");
        StringBuilder fullResponse = new StringBuilder();

        for (String line : jsonLines) {
            try {
                JsonNode jsonNode = objectMapper.readTree(line);
                if (jsonNode.has("response")) {
                    fullResponse.append(jsonNode.get("response").asText());
                }
            } catch (Exception e) {
                // Handle parsing error
            }
        }

        return fullResponse.toString();
    }


    /**
     * Builds the prompt for the AI model
     */
    private String buildPrompt(String code, String language) {
        StringBuilder builder = new StringBuilder();

        builder.append("You are an expert code reviewer analyzing code. ");
        builder.append("Your response must be ONLY valid JSON with no explanations or text outside the JSON structure. ");
        builder.append("Analyze the following ").append(language).append(" code for issues:\n\n");

        builder.append("```").append(language).append("\n");
        builder.append(code).append("\n```\n\n");

        builder.append("Format your response as this exact JSON structure:\n");
        builder.append("{\n");
        builder.append("  \"summary\": \"Brief overall assessment of the code\",\n");
        builder.append("  \"overallScore\": 85,\n");
        builder.append("  \"issues\": [\n");
        builder.append("    {\n");
        builder.append("      \"issueType\": \"PERFORMANCE\",\n");
        builder.append("      \"severity\": \"MEDIUM\",\n");
        builder.append("      \"lineNumber\": 42,\n");
        builder.append("      \"description\": \"Description of issue\",\n");
        builder.append("      \"suggestion\": \"How to fix\",\n");
        builder.append("      \"codeSnippet\": \"Problem code\",\n");
        builder.append("      \"fixedCodeSnippet\": \"Fixed code\"\n");
        builder.append("    }\n");
        builder.append("  ]\n");
        builder.append("}\n\n");

        builder.append("Valid issueType values: PERFORMANCE, SECURITY, CODE_SMELL, BEST_PRACTICE, POTENTIAL_BUG, COMPLEXITY, DOCUMENTATION.\n");
        builder.append("Valid severity values: CRITICAL, HIGH, MEDIUM, LOW, INFO.\n");
        builder.append("Don't write anything except the JSON object.\n");

        return builder.toString();
    }

    /**
     * Parses the AI model response into an AnalysisOutput
     */
    private AnalysisOutput parseAnalysisResult(String aiResponse) {
        try {
            // Build a fallback result in case parsing fails
            AnalysisOutput fallbackOutput = buildFallbackOutput(aiResponse);

            // Attempt to extract JSON from the response
            String jsonStr = extractJsonFromResponse(aiResponse);
            if (jsonStr == null || jsonStr.trim().isEmpty()) {
                log.warn("No JSON found in AI response, using fallback output");
                return fallbackOutput;
            }

            try {
                // Try to parse the JSON
                Map<String, Object> resultMap = objectMapper.readValue(jsonStr, Map.class);

                String summary = (String) resultMap.getOrDefault("summary",
                        "Analysis completed, but result format was unexpected");

                int overallScore = 0;
                if (resultMap.get("overallScore") != null) {
                    overallScore = getIntValue(resultMap.get("overallScore"));
                }

                List<Map<String, Object>> issuesMap = (List<Map<String, Object>>) resultMap.get("issues");
                List<DetectedIssue> detectedIssues = new ArrayList<>();
                List<CodeIssue.IssueType> detectedIssueTypes = new ArrayList<>();

                if (issuesMap != null) {
                    for (Map<String, Object> issueMap : issuesMap) {
                        try {
                            String issueTypeStr = (String) issueMap.get("issueType");
                            String severityStr = (String) issueMap.get("severity");

                            if (issueTypeStr == null || severityStr == null) {
                                continue;
                            }

                            CodeIssue.IssueType issueType = safeValueOf(CodeIssue.IssueType.class,
                                    issueTypeStr, CodeIssue.IssueType.CODE_SMELL);

                            if (!detectedIssueTypes.contains(issueType)) {
                                detectedIssueTypes.add(issueType);
                            }

                            CodeIssue.Severity severity = safeValueOf(CodeIssue.Severity.class,
                                    severityStr, CodeIssue.Severity.MEDIUM);

                            DetectedIssue issue = new DetectedIssue(
                                    issueType,
                                    severity,
                                    issueMap.get("lineNumber") != null ? getIntValue(issueMap.get("lineNumber")) : null,
                                    (String) issueMap.getOrDefault("description", "No description provided"),
                                    (String) issueMap.getOrDefault("suggestion", "No suggestion provided"),
                                    (String) issueMap.getOrDefault("codeSnippet", ""),
                                    (String) issueMap.getOrDefault("fixedCodeSnippet", "")
                            );

                            detectedIssues.add(issue);
                        } catch (Exception e) {
                            log.warn("Error processing an issue, skipping: {}", e.getMessage());
                        }
                    }
                }

                if (detectedIssues.isEmpty()) {
                    // Add at least one issue if none were successfully parsed
                    detectedIssues.add(new DetectedIssue(
                            CodeIssue.IssueType.BEST_PRACTICE,
                            CodeIssue.Severity.INFO,
                            null,
                            "Code review completed",
                            "Consider reviewing code quality regularly",
                            "",
                            ""
                    ));

                    if (!detectedIssueTypes.contains(CodeIssue.IssueType.BEST_PRACTICE)) {
                        detectedIssueTypes.add(CodeIssue.IssueType.BEST_PRACTICE);
                    }
                }

                return new AnalysisOutput(summary, overallScore, detectedIssueTypes, detectedIssues);
            } catch (Exception e) {
                log.warn("Error parsing JSON structure, using fallback output: {}", e.getMessage());
                return fallbackOutput;
            }
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage(), e);
            throw new AIServiceException("Failed to parse AI analysis result", e);
        }
    }

    /**
     * Helper method to safely parse enums
     */
    private <T extends Enum<T>> T safeValueOf(Class<T> enumClass, String value, T defaultValue) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (Exception e) {
            log.warn("Invalid enum value '{}' for {}, using default: {}",
                    value, enumClass.getSimpleName(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * Build a fallback output with minimal useful information
     */
    private AnalysisOutput buildFallbackOutput(String aiResponse) {
        // Extract a summary from the text response
        String summary = aiResponse;
        if (summary.length() > 500) {
            summary = summary.substring(0, 500) + "...";
        }

        // Create a default issue
        DetectedIssue defaultIssue = new DetectedIssue(
                CodeIssue.IssueType.DOCUMENTATION,
                CodeIssue.Severity.INFO,
                null,
                "The code could benefit from a review",
                "Consider adding more documentation and following best practices",
                "",
                ""
        );

        List<DetectedIssue> issues = new ArrayList<>();
        issues.add(defaultIssue);

        List<CodeIssue.IssueType> issueTypes = new ArrayList<>();
        issueTypes.add(CodeIssue.IssueType.DOCUMENTATION);

        return new AnalysisOutput(
                "Code analysis completed with limited insights due to processing limitations",
                60, // Default middle score
                issueTypes,
                issues
        );
    }

    /**
     * Creates a fallback analysis result when the AI service fails
     */
    private AnalysisOutput createFallbackAnalysisResult(String code, String language, String errorMessage) {
        DetectedIssue issue = new DetectedIssue(
                CodeIssue.IssueType.DOCUMENTATION,
                CodeIssue.Severity.INFO,
                null,
                "AI service unavailable",
                "Try again later or check your code manually",
                "",
                ""
        );

        List<DetectedIssue> issues = new ArrayList<>();
        issues.add(issue);

        List<CodeIssue.IssueType> issueTypes = new ArrayList<>();
        issueTypes.add(CodeIssue.IssueType.DOCUMENTATION);

        return new AnalysisOutput(
                "Unable to perform AI analysis. Error: " + errorMessage,
                50,
                issueTypes,
                issues
        );
    }

    /**
     * Extracts the JSON portion from the AI response
     */
    private String extractJsonFromResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{}";
        }

        // Look for JSON between braces
        int startIdx = response.indexOf('{');
        int endIdx = response.lastIndexOf('}');

        if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            try {
                String jsonCandidate = response.substring(startIdx, endIdx + 1);
                // Validate that it's actually JSON
                objectMapper.readTree(jsonCandidate);
                return jsonCandidate;
            } catch (Exception e) {
                log.warn("Found text between braces but it's not valid JSON");
            }
        }

        // If no JSON was found, try to construct a simple one from the text
        String sanitizedText = response.replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", "");

        if (sanitizedText.length() > 200) {
            sanitizedText = sanitizedText.substring(0, 200) + "...";
        }

        return String.format(
                "{\"summary\": \"%s\", \"overallScore\": 50, \"issues\": []}",
                sanitizedText
        );
    }

    /**
     * Safely converts various number types to int
     */
    private int getIntValue(Object value) {
        if (value == null) {
            return 0;
        }

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof Double) {
                return ((Double) value).intValue();
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        } catch (Exception e) {
            log.warn("Failed to convert value to int: {}", value);
        }

        return 0;
    }
}