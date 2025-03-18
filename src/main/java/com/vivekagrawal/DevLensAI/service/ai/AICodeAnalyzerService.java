package com.vivekagrawal.DevLensAI.service.ai;

import com.vivekagrawal.DevLensAI.model.CodeIssue;

import java.util.List;

/**
 * Service for analyzing code using AI models
 */
public interface AICodeAnalyzerService {

    /**
     * Analyzes code and returns a list of detected issues
     *
     * @param code The code to analyze
     * @param language The programming language
     * @return Analysis result containing detected issues and summary
     */
    AnalysisOutput analyzeCode(String code, String language);

    /**
     * Contains the results of AI code analysis
     */
    record AnalysisOutput(
            String summary,
            int overallScore,
            List<CodeIssue.IssueType> detectedIssueTypes,
            List<DetectedIssue> issues
    ) {}

    /**
     * Represents an issue detected by the AI analyzer
     */
    record DetectedIssue(
            CodeIssue.IssueType issueType,
            CodeIssue.Severity severity,
            Integer lineNumber,
            String description,
            String suggestion,
            String codeSnippet,
            String fixedCodeSnippet
    ) {}
}
