package com.serveflow.domain.model.menu;

import java.time.LocalDateTime;
import java.util.*;

public class Menu {

    private final UUID id;
    private String name;
    private MenuStatus status;
    private UUID activeOrderId;
    private final List<MenuItem> items;
    private final LocalDateTime createdAt;
    private Long version;

    public Menu(UUID id, String name, MenuStatus status, UUID activeOrderId,
                List<MenuItem> items, LocalDateTime createdAt, Long version) {
        this.id = Objects.requireNonNull(id, "ID do menu e obrigatorio.");
        setName(name);
        this.status = Objects.requireNonNull(status, "Status do menu e obrigatorio.");
        this.activeOrderId = activeOrderId;
        this.items = new ArrayList<>(Optional.ofNullable(items).orElse(List.of()));
        this.createdAt = Objects.requireNonNull(createdAt, "Data de criacao e obrigatoria.");
        this.version = version;
    }

    public static Menu create(String name, List<MenuItem> items) {
        return new Menu(
                UUID.randomUUID(),
                name,
                MenuStatus.OPEN,
                null,
                items,
                LocalDateTime.now(),
                null
        );
    }

    public void lock(UUID orderId) {
        Objects.requireNonNull(orderId, "ID do pedido e obrigatorio para travar o menu.");
        if (status == MenuStatus.LOCKED) {
            throw new IllegalStateException(
                    "Menu ja esta travado pelo pedido " + activeOrderId + ".");
        }
        this.status = MenuStatus.LOCKED;
        this.activeOrderId = orderId;
    }

    public void unlock() {
        this.status = MenuStatus.OPEN;
        this.activeOrderId = null;
    }

    public boolean isLocked() {
        return status == MenuStatus.LOCKED;
    }

    public boolean isOpen() {
        return status == MenuStatus.OPEN;
    }

    public MenuItem findItem(UUID menuItemId) {
        return items.stream()
                .filter(item -> item.getId().equals(menuItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Item " + menuItemId + " nao encontrado no menu."));
    }

    public void addItem(MenuItem item) {
        Objects.requireNonNull(item, "Item nao pode ser nulo.");
        items.add(item);
    }

    public void removeItem(UUID menuItemId) {
        MenuItem item = findItem(menuItemId);
        items.remove(item);
    }

    public List<MenuItem> getAvailableItems() {
        return items.stream().filter(MenuItem::isAvailable).toList();
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public MenuStatus getStatus() { return status; }
    public UUID getActiveOrderId() { return activeOrderId; }
    public List<MenuItem> getItems() { return List.copyOf(items); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Menu other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    private void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nome do menu e obrigatorio.");
        this.name = name.strip();
    }
}
