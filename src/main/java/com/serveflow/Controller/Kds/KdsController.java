package com.serveflow.Controller.Kds;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Tag(name = "KDS - Cozinha", description = "Visão de pedidos em tempo real para a cozinha")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/kds")
public class KdsController {

    @Operation(summary = "Lista pedidos abertos para a cozinha")
    @GetMapping("/orders")
    public ResponseEntity<List<KdsOrderOutput>> openOrders() {
        // Stub: integrar com o agregado Order.
        return ResponseEntity.ok(List.of(
                new KdsOrderOutput(101L, "Mesa 5", List.of("1x Pizza Margherita", "2x Suco de Laranja"), "EM_PREPARO", Instant.now().minusSeconds(300)),
                new KdsOrderOutput(102L, "Mesa 12", List.of("1x Hambúrguer Artesanal", "1x Cerveja"), "ABERTO", Instant.now().minusSeconds(60))
        ));
    }

    public record KdsOrderOutput(Long id, String table, List<String> items, String status, Instant openedAt) {}
}
