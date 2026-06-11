package com.serveflow.service.upload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class ImageProcessingServiceTest {

    ImageProcessingService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new ImageProcessingService();
        setField("maxDimension", 1200);
        setField("jpegQuality", 0.82);
    }

    private void setField(String name, Object value) throws Exception {
        Field f = ImageProcessingService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(service, value);
    }

    private static byte[] minimalJpeg() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "jpeg", out);
        return out.toByteArray();
    }

    private static byte[] minimalPng() throws IOException {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }

    @Nested
    @DisplayName("tipos não comprimíveis — retorna bytes originais")
    class NonCompressible {

        @Test
        @DisplayName("image/gif retorna dados e content-type originais")
        void gif_returnsOriginal() throws IOException {
            byte[] raw = new byte[]{1, 2, 3, 4, 5};
            ImageProcessingService.ProcessedImage result = service.process(raw, "image/gif");

            assertThat(result.data()).isSameAs(raw);
            assertThat(result.contentType()).isEqualTo("image/gif");
        }

        @Test
        @DisplayName("image/webp retorna dados e content-type originais")
        void webp_returnsOriginal() throws IOException {
            byte[] raw = new byte[]{10, 20, 30};
            ImageProcessingService.ProcessedImage result = service.process(raw, "image/webp");

            assertThat(result.data()).isSameAs(raw);
            assertThat(result.contentType()).isEqualTo("image/webp");
        }

        @Test
        @DisplayName("application/octet-stream retorna dados originais")
        void octet_returnsOriginal() throws IOException {
            byte[] raw = new byte[]{0, 1};
            ImageProcessingService.ProcessedImage result = service.process(raw, "application/octet-stream");

            assertThat(result.data()).isSameAs(raw);
        }
    }

    @Nested
    @DisplayName("tipos comprimíveis — JPEG e PNG")
    class Compressible {

        @Test
        @DisplayName("image/jpeg gera saída com content-type image/jpeg")
        void jpeg_outputIsJpeg() throws IOException {
            byte[] raw = minimalJpeg();
            ImageProcessingService.ProcessedImage result = service.process(raw, "image/jpeg");

            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.data()).isNotEmpty();
        }

        @Test
        @DisplayName("image/png é convertido para JPEG na saída")
        void png_isConvertedToJpeg() throws IOException {
            byte[] raw = minimalPng();
            ImageProcessingService.ProcessedImage result = service.process(raw, "image/png");

            assertThat(result.contentType()).isEqualTo("image/jpeg");
            assertThat(result.data()).isNotEmpty();
        }

        @Test
        @DisplayName("JPEG comprimido produz dados não nulos")
        void jpeg_producesNonNullData() throws IOException {
            byte[] raw = minimalJpeg();
            ImageProcessingService.ProcessedImage result = service.process(raw, "image/jpeg");

            assertThat(result.data()).isNotNull();
        }
    }

    @Nested
    @DisplayName("ProcessedImage record")
    class ProcessedImageRecord {

        @Test
        @DisplayName("data() retorna os bytes fornecidos")
        void data_returnsBytes() {
            byte[] bytes = {1, 2, 3};
            ImageProcessingService.ProcessedImage img =
                    new ImageProcessingService.ProcessedImage(bytes, "image/jpeg");

            assertThat(img.data()).isSameAs(bytes);
        }

        @Test
        @DisplayName("contentType() retorna o tipo fornecido")
        void contentType_returnsType() {
            ImageProcessingService.ProcessedImage img =
                    new ImageProcessingService.ProcessedImage(new byte[0], "image/png");

            assertThat(img.contentType()).isEqualTo("image/png");
        }
    }
}
