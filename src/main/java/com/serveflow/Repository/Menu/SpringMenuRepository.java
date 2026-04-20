package com.serveflow.Repository.Menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringMenuRepository extends JpaRepository<MenuEntity, UUID> {
    Optional<MenuEntity> findByActiveOrderId(UUID activeOrderId);
}
