package com.serveflow.dto.kds.response;

import java.util.List;
import java.util.UUID;

public record KdsItemOutput(
        UUID id,
        String productName,
        int quantity,
        String observation,
        List<String> additionals
) {}
