package com.serveflow.repository.menu;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_menu_item", updatable = false, nullable = false)
    private UUID idMenuItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_menu", nullable = false)
    private MenuEntity menu;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private boolean available;

    @Column(nullable = false)
    private boolean removed;

    @Column(name = "removed_by", length = 120)
    private String removedBy;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idMenuItem; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }
}
