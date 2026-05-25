package com.serveflow.repository.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccessLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(length = 45)
    private String ip;

    @Column(length = 255)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static AccessLogEntity of(Long userId, String ip, String endpoint,
                                     String httpMethod, int httpStatus) {
        AccessLogEntity e = new AccessLogEntity();
        e.userId     = userId;
        e.ip         = ip;
        e.endpoint   = endpoint;
        e.httpMethod = httpMethod;
        e.httpStatus = httpStatus;
        e.createdAt  = LocalDateTime.now();
        return e;
    }
}
