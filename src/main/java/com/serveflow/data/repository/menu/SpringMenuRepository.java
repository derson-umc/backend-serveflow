package com.serveflow.data.repository.menu;

import com.serveflow.data.entity.menu.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringMenuRepository extends JpaRepository<MenuEntity, UUID> {
}
