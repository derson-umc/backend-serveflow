package com.serveflow.repository.product;

import com.serveflow.model.product.ProductCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_product", updatable = false, nullable = false)
    private UUID idProduct;

    @Version
    private Long version;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 80)
    private String brand;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 50)
    private String portion;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_category", length = 30)
    private ProductCategory productCategory;

    @Column(name = "requires_hot_prep", nullable = false)
    private boolean requiresHotPrep = false;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "requires_technical_sheet", nullable = false)
    private boolean requiresTechnicalSheet = false;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Override
    public UUID getId() { return idProduct; }

    @Override
    public boolean isNew() { return version == null; }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.version == null)   this.version   = 0L;
    }
}
