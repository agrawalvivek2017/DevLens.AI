package com.vivekagrawal.DevLensAI.controller;


import com.vivekagrawal.DevLensAI.dto.ApiResponse;
import com.vivekagrawal.DevLensAI.dto.CodeSnippetRequest;
import com.vivekagrawal.DevLensAI.dto.CodeSnippetResponse;
import com.vivekagrawal.DevLensAI.service.CodeSnippetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/snippets")
@RequiredArgsConstructor
public class CodeSnippetController {

    private final CodeSnippetService codeSnippetService;

    @PostMapping
    public ResponseEntity<ApiResponse<CodeSnippetResponse>> submitCodeSnippet(
            @Valid @RequestBody CodeSnippetRequest request) {
        CodeSnippetResponse response = codeSnippetService.saveCodeSnippet(request);
        return new ResponseEntity<>(
                ApiResponse.success("Code snippet submitted successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CodeSnippetResponse>> getCodeSnippet(
            @PathVariable UUID id) {
        CodeSnippetResponse response = codeSnippetService.getCodeSnippetById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CodeSnippetResponse>>> getAllCodeSnippets() {
        List<CodeSnippetResponse> responses = codeSnippetService.getAllCodeSnippets();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<List<CodeSnippetResponse>>> getCodeSnippetsByLanguage(
            @PathVariable String language) {
        List<CodeSnippetResponse> responses = codeSnippetService.getCodeSnippetsByLanguage(language);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCodeSnippet(
            @PathVariable UUID id) {
        codeSnippetService.deleteCodeSnippet(id);
        return ResponseEntity.ok(ApiResponse.success("Code snippet deleted successfully", null));
    }
}