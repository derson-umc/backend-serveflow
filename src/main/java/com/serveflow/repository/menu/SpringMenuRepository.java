package com.serveflow.repository.menu;

import com.serveflow.model.menu.MenuShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringMenuRepository extends JpaRepository<MenuEntity, UUID> {
    Optional<MenuEntity> findByActiveOrderId(UUID activeOrderId);

    Optional<MenuEntity> findByDayOfWeekAndShift(DayOfWeek dayOfWeek, MenuShift shift);

    @Query("SELECT DISTINCT m FROM MenuEntity m JOIN m.items i WHERE i.productId = :productId AND i.removed = false")
    List<MenuEntity> findAllByItemProductId(@Param("productId") UUID productId);
}
