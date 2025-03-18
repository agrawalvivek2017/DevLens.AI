package com.vivekagrawal.DevLensAI.service.ai.impl;

import com.vivekagrawal.DevLensAI.dto.AnalysisResultResponse;
import com.vivekagrawal.DevLensAI.exception.ResourceNotFoundException;
import com.vivekagrawal.DevLensAI.model.AnalysisResult;
import com.vivekagrawal.DevLensAI.model.CodeIssue;
import com.vivekagrawal.DevLensAI.model.CodeSnippet;
import com.vivekagrawal.DevLensAI.repository.AnalysisResultRepository;
import com.vivekagrawal.DevLensAI.repository.CodeIssueRepository;
import com.vivekagrawal.DevLensAI.repository.CodeSnippetRepository;
import com.vivekagrawal.DevLensAI.service.AnalysisService;
import com.vivekagrawal.DevLensAI.service.ai.AICodeAnalyzerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    private final CodeSnippetRepository codeSnippetRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final CodeIssueRepository codeIssueRepository;
    private final AICodeAnalyzerService aiCodeAnalyzerService;

    @Override
    @Async
    @Transactional
    public void analyzeCodeSnippet(UUID snippetId) {
        log.info("Starting analysis for snippet ID: {}", snippetId);

        // Find code snippet
        CodeSnippet codeSnippet = codeSnippetRepository.findById(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("CodeSnippet", "id", snippetId));

        // Create or get existing analysis result
        AnalysisResult analysisResult = analysisResultRepository.findByCodeSnippetId(snippetId)
                .orElse(AnalysisResult.builder()
                        .codeSnippet(codeSnippet)
                        .status(AnalysisResult.AnalysisStatus.PENDING)
                        .build());

        // Update status to IN_PROGRESS
        analysisResult.setStatus(AnalysisResult.AnalysisStatus.IN_PROGRESS);
        analysisResultRepository.save(analysisResult);

        try {
            // Start timing
            long startTime = System.currentTimeMillis();

            // Perform AI analysis
            AICodeAnalyzerService.AnalysisOutput analysisOutput =
                    aiCodeAnalyzerService.analyzeCode(codeSnippet.getContent(), codeSnippet.getLanguage());

            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Update analysis result
            analysisResult.setOverallScore(analysisOutput.overallScore());
            analysisResult.setSummary(analysisOutput.summary());
            analysisResult.setExecutionTimeMs(executionTime);
            analysisResult.setStatus(AnalysisResult.AnalysisStatus.COMPLETED);

            // Clear existing issues
            if (analysisResult.getIssues() != null) {
                analysisResult.getIssues().clear();
            } else {
                analysisResult.setIssues(new ArrayList<>());
            }

            // Add new issues
            List<CodeIssue> codeIssues = analysisOutput.issues().stream()
                    .map(issue -> CodeIssue.builder()
                            .analysisResult(analysisResult)
                            .issueType(issue.issueType())
                            .severity(issue.severity())
                            .lineNumber(issue.lineNumber())
                            .description(issue.description())
                            .suggestion(issue.suggestion())
                            .codeSnippet(issue.codeSnippet())
                            .fixedCodeSnippet(issue.fixedCodeSnippet())
                            .build())
                    .collect(Collectors.toList());

            analysisResult.getIssues().addAll(codeIssues);

            // Save the analysis result with issues
            analysisResultRepository.save(analysisResult);

            log.info("Completed analysis for snippet ID: {}", snippetId);
        } catch (Exception e) {
            log.error("Error analyzing code snippet: {}", e.getMessage(), e);

            // Update status to FAILED
            analysisResult.setStatus(AnalysisResult.AnalysisStatus.FAILED);
            analysisResult.setSummary("Analysis failed: " + e.getMessage());
            analysisResultRepository.save(analysisResult);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResultResponse getAnalysisResultBySnippetId(UUID snippetId) {
        AnalysisResult analysisResult = analysisResultRepository.findByCodeSnippetId(snippetId)
                .orElseThrow(() -> new ResourceNotFoundException("AnalysisResult", "snippetId", snippetId));

        return mapToAnalysisResultResponse(analysisResult);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResultResponse> getAllAnalysisResults() {
        return analysisResultRepository.findAll().stream()
                .map(this::mapToAnalysisResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResultResponse> getAnalysisResultsByLanguage(String language) {
        return analysisResultRepository.findByLanguage(language).stream()
                .map(this::mapToAnalysisResultResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResultResponse> getAnalysisResultsByUserId(String userId) {
        return analysisResultRepository.findByUserId(userId).stream()
                .map(this::mapToAnalysisResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * Maps an AnalysisResult entity to AnalysisResultResponse DTO
     */
    private AnalysisResultResponse mapToAnalysisResultResponse(AnalysisResult analysisResult) {
        // Count issues by severity
        Map<CodeIssue.Severity, Long> severityCounts = analysisResult.getIssues().stream()
                .collect(Collectors.groupingBy(CodeIssue::getSeverity, Collectors.counting()));

        // Map code issues to DTOs
        List<AnalysisResultResponse.CodeIssueDto> issuesDto = analysisResult.getIssues().stream()
                .map(issue -> AnalysisResultResponse.CodeIssueDto.builder()
                        .id(issue.getId())
                        .issueType(issue.getIssueType().name())
                        .severity(issue.getSeverity().name())
                        .lineNumber(issue.getLineNumber())
                        .description(issue.getDescription())
                        .suggestion(issue.getSuggestion())
                        .codeSnippet(issue.getCodeSnippet())
                        .fixedCodeSnippet(issue.getFixedCodeSnippet())
                        .build())
                .collect(Collectors.toList());

        return AnalysisResultResponse.builder()
                .id(analysisResult.getId())
                .codeSnippetId(analysisResult.getCodeSnippet().getId())
                .snippetTitle(analysisResult.getCodeSnippet().getTitle())
                .language(analysisResult.getCodeSnippet().getLanguage())
                .overallScore(analysisResult.getOverallScore())
                .summary(analysisResult.getSummary())
                .issues(issuesDto)
                .createdAt(analysisResult.getCreatedAt())
                .status(analysisResult.getStatus().name())
                .executionTimeMs(analysisResult.getExecutionTimeMs())
                .criticalIssuesCount(severityCounts.getOrDefault(CodeIssue.Severity.CRITICAL, 0L).intValue())
                .highIssuesCount(severityCounts.getOrDefault(CodeIssue.Severity.HIGH, 0L).intValue())
                .mediumIssuesCount(severityCounts.getOrDefault(CodeIssue.Severity.MEDIUM, 0L).intValue())
                .lowIssuesCount(severityCounts.getOrDefault(CodeIssue.Severity.LOW, 0L).intValue())
                .infoIssuesCount(severityCounts.getOrDefault(CodeIssue.Severity.INFO, 0L).intValue())
                .build();
    }
}
