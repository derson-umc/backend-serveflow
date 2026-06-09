package com.serveflow.service.upload;

import java.io.IOException;

public interface StorageService {

    String store(byte[] data, String filename, String contentType) throws IOException;
}
