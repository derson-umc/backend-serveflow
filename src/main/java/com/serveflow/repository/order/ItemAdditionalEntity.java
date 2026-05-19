package com.serveflow.repository.order;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "item_additionals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemAdditionalEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_item_additional", updatable = false, nullable = false)
    private UUID idItemAdditional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_order_item", nullable = false)
    private OrderItemEntity orderItem;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() { return idItemAdditional; }

    @Override
    public boolean isNew() { return isNew; }

    @PostPersist
    @PostLoad
    void markNotNew() { this.isNew = false; }
}
