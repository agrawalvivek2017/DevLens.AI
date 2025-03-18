package com.vivekagrawal.DevLensAI.repository;

import com.vivekagrawal.DevLensAI.model.CodeSnippet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, UUID> {
    List<CodeSnippet> findByUserId(String userId);
    List<CodeSnippet> findByLanguage(String language);
}
