package com.serveflow.service.upload;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Service
public class ImageProcessingService {

    private static final Set<String> COMPRESSIBLE = Set.of("image/jpeg", "image/png");

    @Value("${app.upload.max-dimension:1200}")
    private int maxDimension;

    @Value("${app.upload.jpeg-quality:0.82}")
    private double jpegQuality;

    public ProcessedImage process(byte[] raw, String contentType) throws IOException {
        if (!COMPRESSIBLE.contains(contentType)) {
            return new ProcessedImage(raw, contentType);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 4);
        Thumbnails.of(new ByteArrayInputStream(raw))
                .size(maxDimension, maxDimension)
                .keepAspectRatio(true)
                .outputFormat("jpeg")
                .outputQuality(jpegQuality)
                .toOutputStream(out);
        return new ProcessedImage(out.toByteArray(), "image/jpeg");
    }

    public record ProcessedImage(byte[] data, String contentType) {}
}
