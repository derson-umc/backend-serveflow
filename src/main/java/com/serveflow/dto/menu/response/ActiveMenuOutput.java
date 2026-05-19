package com.serveflow.dto.menu.response;

public record ActiveMenuOutput(
        boolean open,
        String dayOfWeek,
        String shift,
        MenuOutput menu
) {}
