package br.com.challenge.challengeApi.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents_challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "document_challenge_sequence"
    )
    @SequenceGenerator(
            name = "document_challenge_sequence",
            sequenceName = "document_challenge_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status = DocumentStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "signer_id")
    private User signer;

    private LocalDateTime createdAt;

    private LocalDateTime signedAt;
}