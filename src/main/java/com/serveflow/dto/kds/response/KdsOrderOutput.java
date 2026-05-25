package com.serveflow.dto.kds.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record KdsOrderOutput(
        UUID id,
        String customerName,
        String type,
        String status,
        LocalDateTime createdAt,
        List<KdsItemOutput> items
) {}