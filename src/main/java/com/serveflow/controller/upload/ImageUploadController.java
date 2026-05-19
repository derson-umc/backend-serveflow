package com.serveflow.controller.upload;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
public class ImageUploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_SIZE = 8 * 1024 * 1024; // 8 MB
    private static final Path UPLOAD_DIR = Paths.get("uploads", "images");

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) throws IOException {

        if (file.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Arquivo vazio."));

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType))
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de arquivo não permitido. Use JPEG, PNG, WebP ou GIF."));

        if (file.getSize() > MAX_SIZE)
            return ResponseEntity.badRequest().body(Map.of("error", "Arquivo muito grande. Limite: 8 MB."));

        Files.createDirectories(UPLOAD_DIR);

        String ext = contentType.substring(contentType.lastIndexOf('/') + 1).replace("jpeg", "jpg");
        String filename = UUID.randomUUID() + "." + ext;
        Path dest = UPLOAD_DIR.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        String base = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort() + request.getContextPath();
        String url = base + "/uploads/images/" + filename;

        return ResponseEntity.ok(Map.of("url", url));
    }
}
