package com.serveflow.service.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
@ConditionalOnMissingBean(StorageService.class)
public class LocalStorageService implements StorageService {

    @Value("${app.upload.dir:images}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/api}")
    private String uploadBaseUrl;

    @Override
    public String store(byte[] data, String filename, String contentType) throws IOException {
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);
        Files.write(dir.resolve(filename), data, StandardOpenOption.CREATE_NEW);
        log.debug("Imagem salva: {}/{}", dir, filename);
        return uploadBaseUrl + "/images/" + filename;
    }
}
