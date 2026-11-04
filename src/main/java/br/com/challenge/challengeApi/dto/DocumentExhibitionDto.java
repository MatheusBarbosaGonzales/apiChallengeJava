
package br.com.challenge.challengeApi.dto;

import br.com.challenge.challengeApi.model.Document;
import br.com.challenge.challengeApi.model.DocumentStatus;

import java.time.LocalDateTime;

public record DocumentExhibitionDto(
        Long id,
        String title,
        String fileName,
        DocumentStatus status,
        LocalDateTime createdAt,
        LocalDateTime signedAt
) {
    public DocumentExhibitionDto(Document document) {
        this(
                document.getId(),
                document.getTitle(),
                document.getFileName(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getSignedAt()
        );
    }
}
