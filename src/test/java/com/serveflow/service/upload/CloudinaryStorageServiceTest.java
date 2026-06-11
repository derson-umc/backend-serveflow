package com.serveflow.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CloudinaryStorageServiceTest {

    @Mock Cloudinary cloudinary;
    @Mock Uploader   uploader;

    @InjectMocks
    CloudinaryStorageService service;

    @BeforeEach
    void setUp() {
        when(cloudinary.uploader()).thenReturn(uploader);
    }

    @Nested
    @DisplayName("store() — sucesso")
    class StoreSuccess {

        @Test
        @DisplayName("retorna secure_url do Cloudinary")
        void store_returnsSecureUrl() throws Exception {
            Map<String, Object> result = Map.of("secure_url", "https://res.cloudinary.com/test/img.jpg");
            when(uploader.upload(any(), any())).thenReturn(result);

            String url = service.store(new byte[]{1, 2, 3}, "produto.jpg", "image/jpeg");

            assertThat(url).isEqualTo("https://res.cloudinary.com/test/img.jpg");
        }

        @Test
        @DisplayName("envia os bytes corretos ao uploader")
        void store_sendsCorrectBytes() throws Exception {
            byte[] data = {0x10, 0x20, 0x30};
            Map<String, Object> result = Map.of("secure_url", "https://cdn.com/img.jpg");
            when(uploader.upload(any(), any())).thenReturn(result);

            service.store(data, "img.jpg", "image/jpeg");

            verify(uploader).upload(eq(data), any());
        }

        @Test
        @DisplayName("public_id usa folder serveflow/products e nome sem extensão")
        void store_usesCorrectPublicId() throws Exception {
            Map<String, Object> result = Map.of("secure_url", "https://cdn.com/img.jpg");
            when(uploader.upload(any(), any())).thenReturn(result);

            service.store(new byte[]{1}, "minha-foto.jpg", "image/jpeg");

            verify(uploader).upload(any(), argThat(map -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> params = (Map<String, Object>) map;
                return "serveflow/products/minha-foto".equals(params.get("public_id"));
            }));
        }

        @Test
        @DisplayName("funciona com PNG — mesmo fluxo do JPEG")
        void store_worksWithPng() throws Exception {
            Map<String, Object> result = Map.of("secure_url", "https://cdn.com/img.png");
            when(uploader.upload(any(), any())).thenReturn(result);

            String url = service.store(new byte[]{1}, "banner.png", "image/png");

            assertThat(url).isEqualTo("https://cdn.com/img.png");
        }
    }

    @Nested
    @DisplayName("store() — falha")
    class StoreFailure {

        @Test
        @DisplayName("lança IOException quando upload falha com RuntimeException")
        void store_throwsIOException_onRuntimeException() throws Exception {
            when(uploader.upload(any(), any())).thenThrow(new RuntimeException("Network error"));

            assertThatThrownBy(() -> service.store(new byte[]{1}, "img.jpg", "image/jpeg"))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Falha ao enviar imagem para o Cloudinary")
                    .hasMessageContaining("Network error");
        }

        @Test
        @DisplayName("lança IOException quando upload lança IOException diretamente")
        void store_throwsIOException_onIOException() throws Exception {
            when(uploader.upload(any(), any())).thenThrow(new IOException("Timeout"));

            assertThatThrownBy(() -> service.store(new byte[]{1}, "img.jpg", "image/jpeg"))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Falha ao enviar imagem para o Cloudinary");
        }

        @Test
        @DisplayName("IOException causada encapsula a exceção original")
        void store_ioExceptionHasCause() throws Exception {
            RuntimeException cause = new RuntimeException("original error");
            when(uploader.upload(any(), any())).thenThrow(cause);

            assertThatThrownBy(() -> service.store(new byte[]{1}, "img.jpg", "image/jpeg"))
                    .isInstanceOf(IOException.class)
                    .hasCause(cause);
        }
    }
}
