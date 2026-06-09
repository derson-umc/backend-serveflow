package com.serveflow.repository.order;

import com.serveflow.model.order.OrderItemStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_order_item", updatable = false, nullable = false)
    private UUID idOrderItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order", nullable = false)
    private OrderEntity order;

    @Column(name = "product_id")
    private UUID productId;

    @Column(name = "product_name", nullable = false, length = 120)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(length = 300)
    private String observation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderItemStatus status;

    @Column(name = "cancel_reason", length = 300)
    private String cancelReason;

    @Column(name = "product_category", length = 30)
    private String productCategory;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 30)
    private List<ItemAdditionalEntity> additionals = new ArrayList<>();

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idOrderItem; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }

    @PrePersist
    protected void onCreate() {
        if (this.status == null) this.status = OrderItemStatus.PENDENTE;
    }
}
