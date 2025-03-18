package com.vivekagrawal.DevLensAI.service;

import com.vivekagrawal.DevLensAI.dto.AnalysisResultResponse;

import java.util.List;
import java.util.UUID;

public interface AnalysisService {

    /**
     * Initiates code analysis for a given snippet ID
     *
     * @param snippetId The ID of the code snippet to analyze
     */
    void analyzeCodeSnippet(UUID snippetId);

    /**
     * Retrieves analysis result by snippet ID
     *
     * @param snippetId The code snippet ID
     * @return The analysis result response
     */
    AnalysisResultResponse getAnalysisResultBySnippetId(UUID snippetId);

    /**
     * Retrieves all analysis results
     *
     * @return List of analysis result responses
     */
    List<AnalysisResultResponse> getAllAnalysisResults();

    /**
     * Retrieves analysis results by programming language
     *
     * @param language The programming language
     * @return List of analysis result responses
     */
    List<AnalysisResultResponse> getAnalysisResultsByLanguage(String language);

    /**
     * Retrieves analysis results by user ID
     *
     * @param userId The user ID
     * @return List of analysis result responses
     */
    List<AnalysisResultResponse> getAnalysisResultsByUserId(String userId);
}
