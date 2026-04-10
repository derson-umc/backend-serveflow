package com.serveflow.data.mapper;

import com.serveflow.data.entity.menu.MenuEntity;
import com.serveflow.data.entity.menu.MenuItemEntity;
import com.serveflow.domain.model.menu.Menu;
import com.serveflow.domain.model.menu.MenuItem;
import com.serveflow.domain.model.menu.MenuStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class MenuMapper {

    public Menu toDomain(MenuEntity e) {
        return new Menu(
                e.getIdMenu(),
                e.getName(),
                MenuStatus.valueOf(e.getStatus().name()),
                e.getActiveOrderId(),
                e.getItems().stream().map(this::toDomain).toList(),
                e.getCreatedAt(),
                e.getVersion()
        );
    }

    private MenuItem toDomain(MenuItemEntity e) {
        return new MenuItem(
                e.getIdMenuItem(),
                e.getProductId(),
                e.getName(),
                e.getDescription(),
                e.getPrice(),
                e.isAvailable()
        );
    }

    public MenuEntity toEntity(Menu menu) {
        return updateEntity(new MenuEntity(), menu);
    }

    public MenuEntity updateEntity(MenuEntity entity, Menu menu) {
        entity.setIdMenu(menu.getId());
        entity.setVersion(menu.getVersion());
        entity.setName(menu.getName());
        entity.setStatus(MenuEntity.MenuStatus.valueOf(menu.getStatus().name()));
        entity.setActiveOrderId(menu.getActiveOrderId());
        entity.setCreatedAt(menu.getCreatedAt());

        var updatedItems = menu.getItems().stream()
                .map(item -> syncItemEntity(
                        findOrNew(entity.getItems(), item.getId(), MenuItemEntity::getIdMenuItem, MenuItemEntity::new),
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
        return entity;
    }

    private <E, ID> E findOrNew(List<E> list, ID id, Function<E, ID> idGetter, Supplier<E> factory) {
        return list.stream()
                .filter(e -> id != null && id.equals(idGetter.apply(e)))
                .findFirst()
                .orElseGet(factory);
    }
}
