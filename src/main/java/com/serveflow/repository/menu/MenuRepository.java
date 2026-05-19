package com.serveflow.repository.menu;

import com.serveflow.exception.menu.MenuNotFound;
import com.serveflow.model.menu.Menu;
import com.serveflow.model.menu.MenuItem;
import com.serveflow.model.menu.MenuShift;
import com.serveflow.model.menu.MenuStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Repository
@Transactional(readOnly = true)
public class MenuRepository {

    private final SpringMenuRepository springRepository;

    public MenuRepository(SpringMenuRepository springRepository) {
        this.springRepository = springRepository;
    }

    @Transactional
    public Menu save(Menu menu) {
        boolean isNew = menu.getVersion() == null;
        MenuEntity entity;

        if (isNew) {
            entity = toEntity(menu);
        } else {
            entity = springRepository.findById(menu.getId())
                    .orElseThrow(() -> new MenuNotFound(menu.getId()));
            updateEntity(entity, menu);
        }

        return toDomain(springRepository.save(entity));
    }

    public Menu findById(UUID id) {
        return springRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new MenuNotFound(id));
    }

    public List<Menu> findAll() {
        return springRepository.findAll().stream().map(this::toDomain).toList();
    }

    public Optional<Menu> findByActiveOrderId(UUID orderId) {
        return springRepository.findByActiveOrderId(orderId).map(this::toDomain);
    }

    public Optional<Menu> findByDayOfWeekAndShift(DayOfWeek dayOfWeek, MenuShift shift) {
        return springRepository.findByDayOfWeekAndShift(dayOfWeek, shift).map(this::toDomain);
    }

    @Transactional
    public void disableItemsByProductId(UUID productId) {
        springRepository.findAllByItemProductId(productId).forEach(menuEntity -> {
            menuEntity.getItems().stream()
                    .filter(i -> productId.equals(i.getProductId()) && !i.isRemoved())
                    .forEach(i -> i.setAvailable(false));
            springRepository.save(menuEntity);
        });
    }

    private Menu toDomain(MenuEntity e) {
        return new Menu(
                e.getIdMenu(),
                e.getName(),
                MenuStatus.valueOf(e.getStatus().name()),
                e.getActiveOrderId(),
                e.getItems().stream().map(this::toItemDomain).toList(),
                e.getCreatedAt(),
                e.getVersion(),
                e.getDayOfWeek(),
                e.getShift()
        );
    }

    private MenuItem toItemDomain(MenuItemEntity e) {
        return new MenuItem(
                e.getIdMenuItem(),
                e.getProductId(),
                e.getName(),
                e.getDescription(),
                e.getPrice(),
                e.isAvailable(),
                e.isRemoved(),
                e.getRemovedBy()
        );
    }

    private MenuEntity toEntity(Menu menu) {
        return updateEntity(new MenuEntity(), menu);
    }

    private MenuEntity updateEntity(MenuEntity entity, Menu menu) {
        entity.setIdMenu(menu.getId());
        entity.setVersion(menu.getVersion());
        entity.setName(menu.getName());
        entity.setStatus(MenuStatus.valueOf(menu.getStatus().name()));
        entity.setActiveOrderId(menu.getActiveOrderId());
        entity.setCreatedAt(menu.getCreatedAt());
        entity.setDayOfWeek(menu.getDayOfWeek());
        entity.setShift(menu.getShift());

        List<MenuItemEntity> updatedItems = menu.getItems().stream()
                .map(item -> syncItemEntity(
                        findOrNew(entity.getItems(), item.getId(),
                                MenuItemEntity::getIdMenuItem, MenuItemEntity::new),
                        item, entity))
                .toList();

        entity.getItems().clear();
        entity.getItems().addAll(updatedItems);

        return entity;
    }

    private MenuItemEntity syncItemEntity(MenuItemEntity entity, MenuItem item, MenuEntity menu) {
        entity.setIdMenuItem(item.getId());
        entity.setMenu(menu);
        entity.setProductId(item.getProductId());
        entity.setName(item.getName());
        entity.setDescription(item.getDescription());
        entity.setPrice(item.getPrice());
        entity.setAvailable(item.isAvailable());
        entity.setRemoved(item.isRemoved());
        entity.setRemovedBy(item.getRemovedBy());
        return entity;
    }

    private <E, ID> E findOrNew(List<E> list, ID id, Function<E, ID> idGetter, Supplier<E> factory) {
        return list.stream()
                .filter(e -> id != null && id.equals(idGetter.apply(e)))
                .findFirst()
                .orElseGet(factory);
    }
}
