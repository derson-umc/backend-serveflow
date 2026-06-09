package com.serveflow.controller.upload;

import com.serveflow.model.user.User;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.upload.ImageProcessingService;
import com.serveflow.service.upload.ImageProcessingService.ProcessedImage;
import com.serveflow.service.upload.StorageService;
import com.serveflow.util.IpResolverUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
public class ImageUploadController {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_BYTES = 8L * 1024 * 1024;

    private final AuditService auditService;
    private final StorageService storageService;
    private final ImageProcessingService imageProcessingService;

    @PostMapping("/image")
    public ResponseEntity<Map<String, String>> upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException {

        if (file.isEmpty())
            return badRequest("Arquivo vazio.");

        if (file.getSize() > MAX_BYTES)
            return badRequest("Arquivo muito grande. Limite: 8 MB.");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType))
            return badRequest("Tipo não permitido. Use JPEG, PNG, WebP ou GIF.");

        byte[] raw = file.getBytes();

        if (!isValidMagic(Arrays.copyOf(raw, Math.min(raw.length, 12)), contentType))
            return badRequest("Conteúdo não corresponde ao tipo declarado.");

        ProcessedImage processed = imageProcessingService.process(raw, contentType);

        String ext = toExtension(processed.contentType());
        String filename = UUID.randomUUID() + "." + ext;
        String url = storageService.store(processed.data(), filename, processed.contentType());

        auditService.logAction(user.getId(), "IMAGE_UPLOAD", "Image",
                null, IpResolverUtil.getClientIp(request));

        return ResponseEntity.ok(Map.of("url", url));
    }

    private static String toExtension(String contentType) {
        return contentType.substring(contentType.lastIndexOf('/') + 1).replace("jpeg", "jpg");
    }

    private static boolean isValidMagic(byte[] b, String mime) {
        return switch (mime) {
            case "image/jpeg" -> b.length >= 3
                    && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF;
            case "image/png"  -> b.length >= 8
                    && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
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

    private static ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }
}
