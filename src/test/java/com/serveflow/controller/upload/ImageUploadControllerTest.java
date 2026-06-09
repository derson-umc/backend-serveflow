package com.serveflow.controller.upload;

import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.upload.ImageProcessingService;
import com.serveflow.service.upload.ImageProcessingService.ProcessedImage;
import com.serveflow.service.upload.StorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadControllerTest {

    @Mock
    AuditService auditService;
    @Mock
    StorageService storageService;
    @Mock
    ImageProcessingService imageProcessingService;

    @InjectMocks
    ImageUploadController controller;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1L, "admin", "admin@test.com", "pass", UserRole.ADMIN, "Admin");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // JPEG magic bytes: FF D8 FF
    private static byte[] jpegBytes(int size) {
        byte[] data = new byte[size];
        data[0] = (byte) 0xFF;
        data[1] = (byte) 0xD8;
        data[2] = (byte) 0xFF;
        return data;
    }

    // PNG magic bytes
    private static byte[] pngBytes(int size) {
        byte[] data = new byte[size];
        data[0] = (byte) 0x89;
        data[1] = (byte) 'P';
        data[2] = (byte) 'N';
        data[3] = (byte) 'G';
        data[4] = (byte) 0x0D;
        data[5] = (byte) 0x0A;
        data[6] = (byte) 0x1A;
        data[7] = (byte) 0x0A;
        return data;
    }

    // WebP magic bytes: RIFF....WEBP
    private static byte[] webpBytes() {
        byte[] data = new byte[12];
        data[0] = 'R'; data[1] = 'I'; data[2] = 'F'; data[3] = 'F';
        data[8] = 'W'; data[9] = 'E'; data[10] = 'B'; data[11] = 'P';
        return data;
    }

    // GIF magic bytes: GIF89a
    private static byte[] gifBytes() {
        return new byte[]{'G', 'I', 'F', '8', '9', 'a', 0, 0, 0, 0};
    }

    @Nested
    @DisplayName("POST /uploads/image — sucesso")
    class UploadSuccess {

        @Test
        @DisplayName("retorna 200 com url para JPEG válido")
        void upload_returns200_forJpeg() throws Exception {
            byte[] raw = jpegBytes(100);
            ProcessedImage processed = new ProcessedImage(raw, "image/jpeg");

            when(imageProcessingService.process(any(), eq("image/jpeg"))).thenReturn(processed);
            when(storageService.store(any(), anyString(), eq("image/jpeg"))).thenReturn("https://cdn.example.com/img.jpg");

            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", raw);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.url").value("https://cdn.example.com/img.jpg"));
        }

        @Test
        @DisplayName("retorna 200 com url para PNG válido")
        void upload_returns200_forPng() throws Exception {
            byte[] raw = pngBytes(20);
            ProcessedImage processed = new ProcessedImage(raw, "image/jpeg");

            when(imageProcessingService.process(any(), eq("image/png"))).thenReturn(processed);
            when(storageService.store(any(), anyString(), eq("image/jpeg"))).thenReturn("https://cdn.example.com/img.jpg");

            MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", raw);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("retorna 200 com url para WebP válido")
        void upload_returns200_forWebP() throws Exception {
            byte[] raw = webpBytes();
            ProcessedImage processed = new ProcessedImage(raw, "image/webp");

            when(imageProcessingService.process(any(), eq("image/webp"))).thenReturn(processed);
            when(storageService.store(any(), anyString(), eq("image/webp"))).thenReturn("https://cdn.example.com/img.webp");

            MockMultipartFile file = new MockMultipartFile("file", "test.webp", "image/webp", raw);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("retorna 200 com url para GIF válido")
        void upload_returns200_forGif() throws Exception {
            byte[] raw = gifBytes();
            ProcessedImage processed = new ProcessedImage(raw, "image/gif");

            when(imageProcessingService.process(any(), eq("image/gif"))).thenReturn(processed);
            when(storageService.store(any(), anyString(), eq("image/gif"))).thenReturn("https://cdn.example.com/img.gif");

            MockMultipartFile file = new MockMultipartFile("file", "test.gif", "image/gif", raw);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /uploads/image — erros de validação")
    class UploadErrors {

        @Test
        @DisplayName("retorna 400 quando arquivo está vazio")
        void upload_returns400_whenFileEmpty() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Arquivo vazio."));
        }

        @Test
        @DisplayName("retorna 400 quando tipo de conteúdo não é permitido")
        void upload_returns400_whenTypeNotAllowed() throws Exception {
            byte[] content = "fake pdf content".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", content);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Tipo não permitido. Use JPEG, PNG, WebP ou GIF."));
        }

        @Test
        @DisplayName("retorna 400 quando arquivo supera 8MB")
        void upload_returns400_whenFileTooLarge() throws Exception {
            byte[] raw = new byte[9 * 1024 * 1024]; // 9MB
            raw[0] = (byte) 0xFF;
            raw[1] = (byte) 0xD8;
            raw[2] = (byte) 0xFF;
            MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", raw);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Arquivo muito grande. Limite: 8 MB."));
        }

        @Test
        @DisplayName("retorna 400 quando magic bytes não correspondem ao tipo declarado")
        void upload_returns400_whenMagicBytesMismatch() throws Exception {
            // Content type says JPEG but bytes are not JPEG magic
            byte[] raw = "this is not jpeg content!!!!".getBytes();
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", raw);

            mvc.perform(multipart("/uploads/image").file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Conteúdo não corresponde ao tipo declarado."));
        }
    }
}
