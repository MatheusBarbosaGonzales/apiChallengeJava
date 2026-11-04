package br.com.challenge.challengeApi.controller;

import br.com.challenge.challengeApi.dto.DocumentExhibitionDto;
import br.com.challenge.challengeApi.model.Document;
import br.com.challenge.challengeApi.model.User;
import br.com.challenge.challengeApi.repository.UserRepository;
import br.com.challenge.challengeApi.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/documents")
public class DocumentController {
    @Autowired
    DocumentService documentService;

    @Autowired
    UserRepository userRepository;
    @GetMapping("/example")
    public ResponseEntity<Resource> getExampleDocument() {

        Resource resource = new ClassPathResource("documents/contrato_exemplo.pdf");

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(
                        "Content-Disposition",
                        "inline; filename=\"contrato_exemplo.pdf\""
                )
                .body(resource);
    }

    @PostMapping("/example")
    public ResponseEntity<DocumentExhibitionDto> createExampleDocument() {
        Document document = documentService.createExampleDocument();
        DocumentExhibitionDto dto = new DocumentExhibitionDto(document);
        return ResponseEntity.status(201).body(dto);
    }


    @PatchMapping("/{id}/sign")
    public ResponseEntity<DocumentExhibitionDto> signDocument(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String username = authentication.getName();

        User user = (User) userRepository.findByEmail(username);

        Document document = documentService.signDocument(id, user);

        return ResponseEntity.ok(
                new DocumentExhibitionDto(document)
        );
    }


    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentExhibitionDto> uploadDocument(
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        Document document = documentService.uploadDocument(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new DocumentExhibitionDto(document));
    }

    @GetMapping
    public ResponseEntity<List<DocumentExhibitionDto>> listDocuments() {

        return ResponseEntity.ok(
                documentService.listDocuments()
        );
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getDocumentFile(
            @PathVariable Long id
    ) throws IOException {

        Resource resource = documentService.getDocumentFile(id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"documento.pdf\""
                )
                .body(resource);
    }

}

