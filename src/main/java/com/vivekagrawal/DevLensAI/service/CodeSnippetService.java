package com.vivekagrawal.DevLensAI.service;

import com.vivekagrawal.DevLensAI.dto.CodeSnippetRequest;
import com.vivekagrawal.DevLensAI.dto.CodeSnippetResponse;

import java.util.List;
import java.util.UUID;

public interface CodeSnippetService {

    /**
     * Saves a new code snippet
     *
     * @param request The code snippet request
     * @return The code snippet response
     */
    CodeSnippetResponse saveCodeSnippet(CodeSnippetRequest request);

    /**
     * Retrieves a code snippet by ID
     *
     * @param id The code snippet ID
     * @return The code snippet response
     */
    CodeSnippetResponse getCodeSnippetById(UUID id);

    /**
     * Retrieves all code snippets
     *
     * @return List of code snippet responses
     */
    List<CodeSnippetResponse> getAllCodeSnippets();

    /**
     * Retrieves code snippets by language
     *
     * @param language The programming language
     * @return List of code snippet responses
     */
    List<CodeSnippetResponse> getCodeSnippetsByLanguage(String language);

    /**
     * Retrieves code snippets by user ID
     *
     * @param userId The user ID
     * @return List of code snippet responses
     */
    List<CodeSnippetResponse> getCodeSnippetsByUserId(String userId);

    /**
     * Deletes a code snippet
     *
     * @param id The code snippet ID
     */
    void deleteCodeSnippet(UUID id);
}
