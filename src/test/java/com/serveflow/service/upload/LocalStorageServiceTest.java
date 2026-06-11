package com.serveflow.service.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    LocalStorageService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new LocalStorageService();
        setField("uploadDir", tempDir.toString());
        setField("uploadBaseUrl", "http://localhost:8080/api");
    }

    private void setField(String name, Object value) throws Exception {
        Field f = LocalStorageService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    @Nested
    @DisplayName("store()")
    class Store {

        @Test
        @DisplayName("salva o arquivo no diretório configurado")
        void store_savesFile() throws IOException {
            byte[] data = "imagem-teste".getBytes();

            service.store(data, "produto.jpg", "image/jpeg");

            Path saved = tempDir.resolve("produto.jpg");
            assertThat(Files.exists(saved)).isTrue();
        }

        @Test
        @DisplayName("o arquivo salvo contém os bytes corretos")
        void store_savesCorrectBytes() throws IOException {
            byte[] data = "conteudo-binario".getBytes();

            service.store(data, "foto.jpg", "image/jpeg");

            byte[] read = Files.readAllBytes(tempDir.resolve("foto.jpg"));
            assertThat(read).isEqualTo(data);
        }

        @Test
        @DisplayName("retorna URL com padrão {baseUrl}/images/{filename}")
        void store_returnsCorrectUrl() throws IOException {
            byte[] data = new byte[]{1, 2, 3};

            String url = service.store(data, "thumb.png", "image/png");

            assertThat(url).isEqualTo("http://localhost:8080/api/images/thumb.png");
        }

        @Test
        @DisplayName("cria o diretório automaticamente se não existir")
        void store_createsDirectoryIfAbsent() throws Exception {
            Path subDir = tempDir.resolve("subdir");
            setField("uploadDir", subDir.toString());

            service.store("img".getBytes(), "img.jpg", "image/jpeg");

            assertThat(Files.isDirectory(subDir)).isTrue();
        }

        @Test
        @DisplayName("lança IOException ao tentar salvar arquivo já existente")
        void store_throwsOnDuplicateFilename() throws IOException {
            byte[] data = "dados".getBytes();
            service.store(data, "existente.jpg", "image/jpeg");

            assertThatThrownBy(() -> service.store(data, "existente.jpg", "image/jpeg"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("funciona com content-type diferente de JPEG")
        void store_worksWithAnyContentType() throws IOException {
            byte[] data = new byte[]{0x47, 0x49, 0x46};

            String url = service.store(data, "animacao.gif", "image/gif");

            assertThat(url).endsWith("/images/animacao.gif");
            assertThat(Files.exists(tempDir.resolve("animacao.gif"))).isTrue();
        }
    }
}
