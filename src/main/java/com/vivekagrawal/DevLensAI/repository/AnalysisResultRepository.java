package com.vivekagrawal.DevLensAI.repository;

import com.vivekagrawal.DevLensAI.model.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, UUID> {
    Optional<AnalysisResult> findByCodeSnippetId(UUID codeSnippetId);

    @Query("SELECT ar FROM AnalysisResult ar JOIN ar.codeSnippet cs WHERE cs.userId = :userId")
    List<AnalysisResult> findByUserId(@Param("userId") String userId);

    @Query("SELECT ar FROM AnalysisResult ar JOIN ar.codeSnippet cs WHERE cs.language = :language")
    List<AnalysisResult> findByLanguage(@Param("language") String language);

    @Query("SELECT ar FROM AnalysisResult ar WHERE ar.status = :status")
    List<AnalysisResult> findByStatus(@Param("status") AnalysisResult.AnalysisStatus status);
}
