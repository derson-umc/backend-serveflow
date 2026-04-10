package com.serveflow.domain.repository;

import com.serveflow.domain.model.menu.Menu;

import java.util.List;
import java.util.UUID;

public interface MenuRepository {
    Menu save(Menu menu);
    Menu findById(UUID id);
    List<Menu> findAll();
}
