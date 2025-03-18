package com.vivekagrawal.DevLensAI.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "code_issues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false)
    private Severity severity;

    @Column(name = "line_number")
    private Integer lineNumber; // Can be null for whole-file issues

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "suggestion", columnDefinition = "TEXT")
    private String suggestion;

    @Column(name = "code_snippet", columnDefinition = "TEXT")
    private String codeSnippet; // The problematic code segment

    @Column(name = "fixed_code_snippet", columnDefinition = "TEXT")
    private String fixedCodeSnippet; // Suggested fixed code

    public enum IssueType {
        PERFORMANCE,
        SECURITY,
        CODE_SMELL,
        BEST_PRACTICE,
        POTENTIAL_BUG,
        COMPLEXITY,
        DOCUMENTATION
    }

    public enum Severity {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW,
        INFO
    }
}