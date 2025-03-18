package com.vivekagrawal.DevLensAI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippetResponse {
    private UUID id;
    private String title;
    private String language;
    private String content;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private AnalysisStatus analysisStatus;

    public enum AnalysisStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
