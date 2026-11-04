package br.com.challenge.challengeApi.repository;

import br.com.challenge.challengeApi.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
