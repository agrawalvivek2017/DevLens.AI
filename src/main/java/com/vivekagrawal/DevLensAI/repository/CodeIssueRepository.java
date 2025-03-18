package com.vivekagrawal.DevLensAI.repository;

import com.vivekagrawal.DevLensAI.model.CodeIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CodeIssueRepository extends JpaRepository<CodeIssue, UUID> {
    List<CodeIssue> findByAnalysisResultId(UUID analysisResultId);

    List<CodeIssue> findByAnalysisResultIdAndSeverity(UUID analysisResultId, CodeIssue.Severity severity);

    List<CodeIssue> findByAnalysisResultIdAndIssueType(UUID analysisResultId, CodeIssue.IssueType issueType);

    long countByAnalysisResultIdAndSeverity(UUID analysisResultId, CodeIssue.Severity severity);
}