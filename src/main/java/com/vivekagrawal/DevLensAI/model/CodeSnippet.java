package com.vivekagrawal.DevLensAI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "code_snippets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "language", nullable = false)
    private String language; // java, python, javascript, etc.

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "user_id")
    private String userId; // Optional: for user tracking if I add authentication later

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToOne(mappedBy = "codeSnippet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AnalysisResult analysisResult;
}
