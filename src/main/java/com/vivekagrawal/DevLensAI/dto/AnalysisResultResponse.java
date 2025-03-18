package com.vivekagrawal.DevLensAI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResultResponse {
    private UUID id;
    private UUID codeSnippetId;
    private String snippetTitle;
    private String language;
    private Integer overallScore;
    private String summary;
    private List<CodeIssueDto> issues = new ArrayList<>();
    private LocalDateTime createdAt;
    private String status;
    private Long executionTimeMs;

    // Statistics
    private int criticalIssuesCount;
    private int highIssuesCount;
    private int mediumIssuesCount;
    private int lowIssuesCount;
    private int infoIssuesCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CodeIssueDto {
        private UUID id;
        private String issueType;
        private String severity;
        private Integer lineNumber;
        private String description;
        private String suggestion;
        private String codeSnippet;
        private String fixedCodeSnippet;
    }
}