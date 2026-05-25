package com.serveflow.repository.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Column(columnDefinition = "TEXT")
    private String stacktrace;

    @Column(length = 100)
    private String service;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static ErrorLogEntity of(String message, String stacktrace, String service) {
        ErrorLogEntity e = new ErrorLogEntity();
        e.message    = message;
        e.stacktrace = stacktrace;
        e.service    = service;
        e.createdAt  = LocalDateTime.now();
        return e;
    }
}
