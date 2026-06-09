package com.serveflow.service.upload;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnProperty(name = "cloudinary.cloud-name")
@RequiredArgsConstructor
public class CloudinaryStorageService implements StorageService {

    private static final String FOLDER = "serveflow/products";

    private final Cloudinary cloudinary;

    @Override
    public String store(byte[] data, String filename, String contentType) throws IOException {
        String publicId = FOLDER + "/" + filename.replaceAll("\\.[^.]+$", "");

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    data,
                    ObjectUtils.asMap(
                            "public_id",     publicId,
                            "resource_type", "image",
                            "overwrite",     false
                    )
            );
            String url = (String) result.get("secure_url");
            log.info("Imagem enviada ao Cloudinary: {}", url);
            return url;
        } catch (Exception e) {
            throw new IOException("Falha ao enviar imagem para o Cloudinary: " + e.getMessage(), e);
        }
    }
}
