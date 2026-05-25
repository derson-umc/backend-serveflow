package com.serveflow.repository.order;

import com.serveflow.exception.order.OrderNotFoundException;
import com.serveflow.model.address.*;
import com.serveflow.model.address.Number;
import com.serveflow.model.order.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class OrderRepository {

    private final SpringOrderRepository springRepository;
    private final SpringAddressRepository addressRepository;

    public OrderRepository(SpringOrderRepository springRepository,
                           SpringAddressRepository addressRepository) {
        this.springRepository = springRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Order save(Order order) {
        boolean isNew = order.getVersion() == null;
        OrderEntity entity;

        if (isNew) {
            entity = toEntity(order);
        } else {
            entity = springRepository.findById(order.getId())
                    .orElseThrow(() -> new OrderNotFoundException(order.getId()));
            updateEntity(entity, order);
        }

        return toDomain(springRepository.save(entity));
    }

    public Order findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).toList();
    }

    public List<Order> findByStatus(OrderStatus status) {
        return springRepository.findByStatus(status).stream().map(this::toDomain).toList();
    }

    private Order toDomain(OrderEntity e) {
        return new Order(
                e.getIdOrder(),
                e.getCustomerName(),
                e.getAddress() != null ? toAddressDomain(e.getAddress()) : null,
                e.getType(),
                e.getStatus(),
                e.getCreatedAt(),
                e.getObservation(),
                e.getPaymentMethod(),
                e.getItems().stream().map(this::toItemDomain).toList(),
                e.getVersion()
        );
    }

    private OrderItem toItemDomain(OrderItemEntity e) {
        return new OrderItem(
                e.getIdOrderItem(),
                e.getProductId(),
                e.getProductName(),
                e.getQuantity(),
                e.getUnitPrice(),
                e.getObservation(),
                e.getAdditionals().stream().map(this::toAdditionalDomain).toList()
        );
    }

    private ItemAdditional toAdditionalDomain(ItemAdditionalEntity e) {
        return new ItemAdditional(e.getIdItemAdditional(), e.getName(), e.getQuantity(), e.getUnitPrice());
    }

    private Address toAddressDomain(AddressEntity e) {
        return Address.withId(
                e.getIdAddress(),
                e.getCep() != null ? new Cep(e.getCep()) : null,
                new Street(e.getStreet()),
                new City(e.getCity()),
                new State(e.getState()),
                new Number(e.getNumber()),
                e.getComplement() != null ? new Complement(e.getComplement()) : null
        );
    }

    private OrderEntity toEntity(Order order) {
        return updateEntity(new OrderEntity(), order);
    }

    private OrderEntity updateEntity(OrderEntity entity, Order order) {
        entity.setIdOrder(order.getId());
        entity.setVersion(order.getVersion());
        entity.setCustomerName(order.getCustomerName());
        entity.setType(order.getType());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setObservation(order.getObservation());
        entity.setPaymentMethod(order.getPaymentMethod());

        if (order.getAddress() != null) {
            entity.setAddress(toAddressEntity(order.getAddress()));
        }

        // Merge items without clear()+addAll() to avoid Hibernate orphanRemoval
        // delete-then-reinsert cycle that can cause FK constraint violations.
        Map<UUID, OrderItemEntity> existingById = entity.getItems().stream()
                .collect(Collectors.toMap(OrderItemEntity::getIdOrderItem, Function.identity()));

        List<UUID> domainIds = order.getItems().stream().map(OrderItem::getId).toList();

        // Remove items no longer in the domain model (handles item removal)
        entity.getItems().removeIf(e -> !domainIds.contains(e.getIdOrderItem()));

        // Update existing or add new items in-place
        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = existingById.getOrDefault(item.getId(), new OrderItemEntity());
            syncItemEntity(itemEntity, item, entity);
            if (!entity.getItems().contains(itemEntity)) {
                entity.getItems().add(itemEntity);
            }
        }

        return entity;
    }

    private OrderItemEntity syncItemEntity(OrderItemEntity entity, OrderItem item, OrderEntity order) {
        entity.setIdOrderItem(item.getId());
        entity.setOrder(order);
        entity.setProductId(item.getProductId());
        entity.setProductName(item.getProductName());
        entity.setQuantity(item.getQuantity());
        entity.setUnitPrice(item.getUnitPrice());
        entity.setObservation(item.getObservation());

        // Merge additionals without clear()+addAll() for the same reason as items
        Map<UUID, ItemAdditionalEntity> existingById = entity.getAdditionals().stream()
                .collect(Collectors.toMap(ItemAdditionalEntity::getIdItemAdditional, Function.identity()));

        List<UUID> domainAddIds = item.getAdditionals().stream()
                .map(ItemAdditional::getId).toList();

        entity.getAdditionals().removeIf(a -> !domainAddIds.contains(a.getIdItemAdditional()));

        for (ItemAdditional add : item.getAdditionals()) {
            ItemAdditionalEntity addEntity = existingById.getOrDefault(add.getId(), new ItemAdditionalEntity());
            syncAdditionalEntity(addEntity, add, entity);
            if (!entity.getAdditionals().contains(addEntity)) {
                entity.getAdditionals().add(addEntity);
            }
        }

        return entity;
    }

    private ItemAdditionalEntity syncAdditionalEntity(ItemAdditionalEntity entity,
                                                       ItemAdditional add,
                                                       OrderItemEntity item) {
        entity.setIdItemAdditional(add.getId());
        entity.setOrderItem(item);
        entity.setName(add.getName());
        entity.setQuantity(add.getQuantity());
        entity.setUnitPrice(add.getUnitPrice());
        return entity;
    }

    private AddressEntity toAddressEntity(Address a) {
        String cep        = normalize(a.getCep()        != null ? a.getCep().getValue()          : null);
        String street     = normalize(a.getStreet()     != null ? a.getStreet().getValue()       : null);
        String city       = normalize(a.getCity()       != null ? a.getCity().getValue()         : null);
        String state      = normalize(a.getState()      != null ? a.getState().getValue().name() : null);
        String number     = normalize(a.getNumber()     != null ? a.getNumber().getValue()       : null);
        String complement = normalize(a.getComplement() != null ? a.getComplement().getValue()   : null);

        return addressRepository.findExisting(cep, street, city, state, number, complement)
                .orElseGet(() -> {
                    AddressEntity entity = new AddressEntity();
                    entity.setCep(cep);
                    entity.setStreet(street);
                    entity.setCity(city);
                    entity.setState(state);
                    entity.setNumber(number);
                    entity.setComplement(complement);
                    return addressRepository.save(entity);
                });
    }

    private String normalize(String value) {
        return value != null ? value.trim().toLowerCase() : null;
    }

}
