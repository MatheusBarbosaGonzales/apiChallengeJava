package br.com.challenge.challengeApi.service;

import br.com.challenge.challengeApi.dto.DocumentExhibitionDto;
import br.com.challenge.challengeApi.model.Document;
import br.com.challenge.challengeApi.model.DocumentStatus;
import br.com.challenge.challengeApi.model.User;
import br.com.challenge.challengeApi.repository.DocumentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    DocumentRepository documentRepository;

    public Document createExampleDocument() {
        String fileName = "contrato_exemplo.pdf";
        var resource = new ClassPathResource(
                "documents/" + fileName
        );
        if (!resource.exists()) {
            throw new IllegalStateException(
                    "Arquivo PDF não encontrado."
            );
        }
        Document document = new Document();
        document.setTitle("Contrato de exemplo LetsSign");
        document.setFileName(fileName);
        document.setStatus(DocumentStatus.PENDING);
        document.setCreatedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }

    public Document signDocument(Long documentId, User authenticatedUser) {

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new RuntimeException("Documento não encontrado")
                );

        if (document.getStatus() == DocumentStatus.SIGNED) {
            throw new IllegalStateException("Documento já foi assinado");
        }

        document.setSigner(authenticatedUser);
        document.setStatus(DocumentStatus.SIGNED);
        document.setSignedAt(LocalDateTime.now());

        return documentRepository.save(document);
    }


    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public Document uploadDocument(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo está vazio");
        }

        String originalName = file.getOriginalFilename();

        if (originalName == null ||
                !originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Apenas arquivos PDF são permitidos");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("O PDF deve ter no máximo 10 MB");
        }

        // Confere a assinatura básica do formato PDF
        byte[] header;
        try (InputStream input = file.getInputStream()) {
            header = input.readNBytes(5);
        }

        if (!Arrays.equals(header, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            throw new IllegalArgumentException("Arquivo PDF inválido");
        }

        Path directory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(directory);

        String storedName = UUID.randomUUID() + ".pdf";
        Path destination = directory.resolve(storedName);

        try {
            Files.copy(
                    file.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );

            Document document = new Document();
            document.setTitle(originalName);
            document.setFileName(storedName);
            document.setStatus(DocumentStatus.PENDING);
            document.setCreatedAt(LocalDateTime.now());

            return documentRepository.save(document);

        } catch (Exception exception) {
            Files.deleteIfExists(destination);
            throw exception;
        }
    }

    public List<DocumentExhibitionDto> listDocuments() {

        return documentRepository.findAll()
                .stream()
                .map(DocumentExhibitionDto::new)
                .toList();
    }


    public Resource getDocumentFile(Long id) throws IOException {

        Document document = documentRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Documento não encontrado"
                        )
                );

        String fileName = document.getFileName();

        Path uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        Path filePath = uploadDirectory.resolve(fileName)
                .normalize();

        if (!filePath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nome de arquivo inválido"
            );
        }

        if (Files.isRegularFile(filePath)) {
            return new UrlResource(filePath.toUri());
        }

        // Procura o PDF de exemplo nos resources
        if ("contrato_exemplo.pdf".equals(fileName)) {

            Resource exampleFile = new ClassPathResource(
                    "documents/contrato_exemplo.pdf"
            );

            if (exampleFile.exists()) {
                return exampleFile;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Arquivo PDF não encontrado"
        );
    }


}