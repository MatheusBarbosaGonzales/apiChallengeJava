package br.com.challenge.challengeApi.service;

import br.com.challenge.challengeApi.dto.DocumentExhibitionDto;
import br.com.challenge.challengeApi.model.Document;
import br.com.challenge.challengeApi.model.DocumentStatus;
import br.com.challenge.challengeApi.model.User;
import br.com.challenge.challengeApi.repository.DocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
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

    private static final long MAX_PDF_BYTES = 10L * 1024 * 1024;
    private static final long MAX_SIGNATURE_BYTES = 2L * 1024 * 1024;
    private static final String EXAMPLE_PDF = "contrato_exemplo.pdf";

    @Autowired
    private DocumentRepository documentRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public Document createExampleDocument() {
        Resource resource = new ClassPathResource("documents/" + EXAMPLE_PDF);
        if (!resource.exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo PDF de exemplo não encontrado");
        }

        Document document = new Document();
        document.setTitle("Contrato de exemplo LetsSign");
        document.setFileName(EXAMPLE_PDF);
        document.setStatus(DocumentStatus.PENDING);
        document.setCreatedAt(LocalDateTime.now());
        return documentRepository.save(document);
    }

    public Document signDocument(Long documentId, User authenticatedUser, MultipartFile signature)
            throws IOException {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento não encontrado"));

        if (document.getStatus() == DocumentStatus.SIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Documento já foi assinado");
        }

        byte[] signatureBytes = validateSignature(signature);
        byte[] originalPdf;
        try (InputStream input = getDocumentFile(documentId).getInputStream()) {
            originalPdf = input.readAllBytes();
        }

        byte[] signedPdf = applySignatureToPdf(originalPdf, signatureBytes);
        Path directory = uploadDirectory();
        Files.createDirectories(directory);
        String signedFileName = UUID.randomUUID() + ".pdf";
        Path signedFile = directory.resolve(signedFileName);
        Path temporaryFile = Files.createTempFile(directory, "signature-", ".tmp");

        try {
            Files.write(temporaryFile, signedPdf);
            try {
                Files.move(temporaryFile, signedFile, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporaryFile, signedFile);
            }

            document.setFileName(signedFileName);
            document.setSigner(authenticatedUser);
            document.setStatus(DocumentStatus.SIGNED);
            document.setSignedAt(LocalDateTime.now());
            return documentRepository.saveAndFlush(document);
        } catch (Exception exception) {
            Files.deleteIfExists(signedFile);
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private byte[] validateSignature(MultipartFile signature) throws IOException {
        if (signature == null || signature.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assinatura não enviada");
        }
        if (signature.getSize() > MAX_SIGNATURE_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A assinatura deve ter no máximo 2 MB");
        }

        byte[] bytes = signature.getBytes();
        if (bytes.length < 8 ||
                !Arrays.equals(Arrays.copyOf(bytes, 8),
                        new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A assinatura deve ser um PNG válido");
        }

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
        if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0
                || (long) image.getWidth() * image.getHeight() > 10_000_000L) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem de assinatura inválida");
        }
        return bytes;
    }

    private byte[] applySignatureToPdf(byte[] originalPdf, byte[] signatureBytes) throws IOException {
        try (PDDocument pdf = Loader.loadPDF(originalPdf);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (pdf.getNumberOfPages() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O PDF não possui páginas");
            }

            PDPage page = pdf.getPage(pdf.getNumberOfPages() - 1);
            if (page.getRotation() != 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "A assinatura automática ainda não suporta páginas rotacionadas");
            }

            PDRectangle box = page.getCropBox();
            float margin = 30f;
            float availableWidth = box.getWidth() - 2 * margin;
            float availableHeight = box.getHeight() - 2 * margin;
            if (availableWidth <= 0 || availableHeight <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Página muito pequena para assinatura");
            }

            PDImageXObject image = PDImageXObject.createFromByteArray(pdf, signatureBytes, "signature.png");
            float maxWidth = Math.min(150f, availableWidth);
            float maxHeight = Math.min(60f, availableHeight);
            float scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());
            float width = image.getWidth() * scale;
            float height = image.getHeight() * scale;
            float x = box.getUpperRightX() - margin - width;
            float y = box.getLowerLeftY() + margin;

            try (PDPageContentStream stream = new PDPageContentStream(
                    pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                stream.drawImage(image, x, y, width, height);
            }

            pdf.save(output);
            return output.toByteArray();
        }
    }

    public Document uploadDocument(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo está vazio");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Apenas arquivos PDF são permitidos");
        }
        if (file.getSize() > MAX_PDF_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O PDF deve ter no máximo 10 MB");
        }

        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(5);
            if (!Arrays.equals(header, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arquivo PDF inválido");
            }
        }

        Path directory = uploadDirectory();
        Files.createDirectories(directory);
        String storedName = UUID.randomUUID() + ".pdf";
        Path destination = directory.resolve(storedName);

        try {
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }
            Document document = new Document();
            document.setTitle(originalName);
            document.setFileName(storedName);
            document.setStatus(DocumentStatus.PENDING);
            document.setCreatedAt(LocalDateTime.now());
            return documentRepository.save(document);
        } catch (Exception exception) {
            Files.deleteIfExists(destination);
            if (exception instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }

    public List<DocumentExhibitionDto> listDocuments() {
        return documentRepository.findAll().stream()
                .map(DocumentExhibitionDto::new)
                .toList();
    }

    public Resource getDocumentFile(Long id) throws IOException {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento não encontrado"));

        String fileName = document.getFileName();
        if (fileName == null || fileName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo PDF não encontrado");
        }

        Path directory = uploadDirectory();
        Path filePath = directory.resolve(fileName).normalize();
        if (!filePath.startsWith(directory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome de arquivo inválido");
        }

        if (Files.isRegularFile(filePath)) {
            return new UrlResource(filePath.toUri());
        }

        if (EXAMPLE_PDF.equals(fileName)) {
            Resource example = new ClassPathResource("documents/" + EXAMPLE_PDF);
            if (example.exists()) {
                return example;
            }
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Arquivo PDF não encontrado");
    }

    private Path uploadDirectory() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }
}
