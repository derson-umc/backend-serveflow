package com.serveflow.repository.audit;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(length = 100)
    private String entity;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "previous_data", columnDefinition = "TEXT")
    private String previousData;

    @Column(name = "new_data", columnDefinition = "TEXT")
    private String newData;

    @Column(length = 45)
    private String ip;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static AuditLogEntity of(Long userId, String action, String entity,
                                    Long entityId, String ip) {
        AuditLogEntity e = new AuditLogEntity();
        e.userId    = userId;
        e.action    = action;
        e.entity    = entity;
        e.entityId  = entityId;
        e.ip        = ip;
        e.createdAt = LocalDateTime.now();
        return e;
    }

    public AuditLogEntity withData(String previous, String next) {
        this.previousData = previous;
        this.newData      = next;
        return this;
    }
}
