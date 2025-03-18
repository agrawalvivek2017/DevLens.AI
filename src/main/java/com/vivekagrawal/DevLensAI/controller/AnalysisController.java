package com.vivekagrawal.DevLensAI.controller;

import com.vivekagrawal.DevLensAI.dto.AnalysisResultResponse;
import com.vivekagrawal.DevLensAI.dto.ApiResponse;
import com.vivekagrawal.DevLensAI.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/snippet/{snippetId}")
    public ResponseEntity<ApiResponse<String>> analyzeCodeSnippet(
            @PathVariable UUID snippetId) {
        analysisService.analyzeCodeSnippet(snippetId);
        return ResponseEntity.accepted()
                .body(ApiResponse.success("Code analysis initiated", null));
    }

    @GetMapping("/snippet/{snippetId}")
    public ResponseEntity<ApiResponse<AnalysisResultResponse>> getAnalysisResult(
            @PathVariable UUID snippetId) {
        AnalysisResultResponse response = analysisService.getAnalysisResultBySnippetId(snippetId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AnalysisResultResponse>>> getAllAnalysisResults() {
        List<AnalysisResultResponse> responses = analysisService.getAllAnalysisResults();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<List<AnalysisResultResponse>>> getAnalysisResultsByLanguage(
            @PathVariable String language) {
        List<AnalysisResultResponse> responses = analysisService.getAnalysisResultsByLanguage(language);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
