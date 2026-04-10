package com.serveflow.data.entity.menu;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "menus")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MenuEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_menu", updatable = false, nullable = false)
    private UUID idMenu;

    @Version
    private Long version;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MenuStatus status;

    @Column(name = "active_order_id")
    private UUID activeOrderId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItemEntity> items = new ArrayList<>();

    public enum MenuStatus { OPEN, LOCKED }

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idMenu; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist @PostLoad
    void markNotNew() { this.isNew = false; }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = MenuStatus.OPEN;
    }
}
