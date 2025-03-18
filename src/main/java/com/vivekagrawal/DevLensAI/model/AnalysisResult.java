package com.vivekagrawal.DevLensAI.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analysis_results")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "code_snippet_id", nullable = false)
    private CodeSnippet codeSnippet;

    @Column(name = "overall_score")
    private Integer overallScore; // 0-100 score representing overall code quality

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @OneToMany(mappedBy = "analysisResult", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeIssue> issues = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AnalysisStatus status;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs; // Time taken to analyze the code

    public enum AnalysisStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED
    }
}
