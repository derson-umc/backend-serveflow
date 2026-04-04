package com.serveflow.data.entity.order;

import com.serveflow.data.entity.address.AddressEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity implements Persistable<UUID> {

    @Id
    @Column(name = "id_order", updatable = false, nullable = false)
    private UUID idOrder;

    @Version
    private Long version;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_address")
    private AddressEntity address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(length = 500)
    private String observation;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    public enum OrderStatus {
        CREATED, IN_PREPARATION, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }

    public enum OrderType {DELIVERY, LOCAL}

    @Transient
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return idOrder;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.CREATED;
    }
}


