package com.vivekagrawal.DevLensAI.service.impl;

import com.vivekagrawal.DevLensAI.dto.CodeSnippetRequest;
import com.vivekagrawal.DevLensAI.dto.CodeSnippetResponse;
import com.vivekagrawal.DevLensAI.exception.ResourceNotFoundException;
import com.vivekagrawal.DevLensAI.model.CodeSnippet;
import com.vivekagrawal.DevLensAI.repository.CodeSnippetRepository;
import com.vivekagrawal.DevLensAI.service.CodeSnippetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CodeSnippetServiceImpl implements CodeSnippetService {

    private final CodeSnippetRepository codeSnippetRepository;

    @Override
    @Transactional
    public CodeSnippetResponse saveCodeSnippet(CodeSnippetRequest request) {
        // Convert request to entity
        CodeSnippet codeSnippet = CodeSnippet.builder()
                .title(request.getTitle())
                .language(request.getLanguage())
                .content(request.getContent())
                .userId(request.getUserId())
                .build();

        // Save entity
        CodeSnippet savedCodeSnippet = codeSnippetRepository.save(codeSnippet);

        // Convert entity to response
        return mapToResponse(savedCodeSnippet);
    }

    @Override
    @Transactional(readOnly = true)
    public CodeSnippetResponse getCodeSnippetById(UUID id) {
        CodeSnippet codeSnippet = codeSnippetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CodeSnippet", "id", id));

        return mapToResponse(codeSnippet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeSnippetResponse> getAllCodeSnippets() {
        return codeSnippetRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeSnippetResponse> getCodeSnippetsByLanguage(String language) {
        return codeSnippetRepository.findByLanguage(language).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CodeSnippetResponse> getCodeSnippetsByUserId(String userId) {
        return codeSnippetRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCodeSnippet(UUID id) {
        if (!codeSnippetRepository.existsById(id)) {
            throw new ResourceNotFoundException("CodeSnippet", "id", id);
        }

        codeSnippetRepository.deleteById(id);
    }

    // Helper method to map entity to response
    private CodeSnippetResponse mapToResponse(CodeSnippet codeSnippet) {
        CodeSnippetResponse.AnalysisStatus analysisStatus = CodeSnippetResponse.AnalysisStatus.PENDING;

        if (codeSnippet.getAnalysisResult() != null) {
            switch (codeSnippet.getAnalysisResult().getStatus()) {
                case PENDING:
                    analysisStatus = CodeSnippetResponse.AnalysisStatus.PENDING;
                    break;
                case IN_PROGRESS:
                    analysisStatus = CodeSnippetResponse.AnalysisStatus.IN_PROGRESS;
                    break;
                case COMPLETED:
                    analysisStatus = CodeSnippetResponse.AnalysisStatus.COMPLETED;
                    break;
                case FAILED:
                    analysisStatus = CodeSnippetResponse.AnalysisStatus.FAILED;
                    break;
            }
        }

        return CodeSnippetResponse.builder()
                .id(codeSnippet.getId())
                .title(codeSnippet.getTitle())
                .language(codeSnippet.getLanguage())
                .content(codeSnippet.getContent())
                .userId(codeSnippet.getUserId())
                .createdAt(codeSnippet.getCreatedAt())
                .updatedAt(codeSnippet.getUpdatedAt())
                .analysisStatus(analysisStatus)
                .build();
    }
}
