package com.serveflow.controller.upload;

import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_SIZE   = 8 * 1024 * 1024;
    private static final Path UPLOAD_DIR = Paths.get("uploads", "images");

    private final AuditService auditService;

    @Value("${app.upload.base-url:http://localhost:8080/api}")
    private String uploadBaseUrl;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user,
            HttpServletRequest httpReq) throws IOException {

        if (file.isEmpty())
            return badRequest("Arquivo vazio.");

        if (file.getSize() > MAX_SIZE)
            return badRequest("Arquivo muito grande. Limite: 8 MB.");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType))
            return badRequest("Tipo de arquivo não permitido. Use JPEG, PNG, WebP ou GIF.");

        byte[] magic = readMagicBytes(file.getInputStream(), 12);
        if (!isValidImageMagic(magic, contentType))
            return badRequest("Conteúdo do arquivo não corresponde ao tipo declarado.");

        Files.createDirectories(UPLOAD_DIR);

        String ext      = contentType.substring(contentType.lastIndexOf('/') + 1).replace("jpeg", "jpg");
        String filename = UUID.randomUUID() + "." + ext;
        Path   dest     = UPLOAD_DIR.resolve(filename);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        auditService.logAction(user.getId(), "IMAGE_UPLOAD", "Image",
                null, IpResolverUtil.getClientIp(httpReq));

        String url = uploadBaseUrl + "/uploads/images/" + filename;
        return ResponseEntity.ok(Map.of("url", url));
    }

    private static byte[] readMagicBytes(InputStream is, int len) throws IOException {
        byte[] buf  = new byte[len];
        int    read = is.read(buf, 0, len);
        return read < len ? java.util.Arrays.copyOf(buf, read) : buf;
    }

    private static boolean isValidImageMagic(byte[] b, String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> b.length >= 3
                    && (b[0] & 0xFF) == 0xFF
                    && (b[1] & 0xFF) == 0xD8
                    && (b[2] & 0xFF) == 0xFF;
            case "image/png"  -> b.length >= 8
                    && (b[0] & 0xFF) == 0x89
                    && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                    && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A
                    && (b[6] & 0xFF) == 0x1A && (b[7] & 0xFF) == 0x0A;
            case "image/webp" -> b.length >= 12
                    && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                    && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
            case "image/gif"  -> b.length >= 6
                    && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8'
                    && (b[4] == '7' || b[4] == '9') && b[5] == 'a';
            default           -> false;
        };
    }

    private static ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }
}
