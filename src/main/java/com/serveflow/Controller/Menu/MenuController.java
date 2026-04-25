package com.serveflow.Controller.Menu;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Menu", description = "Catálogo de produtos para Garçom/Cozinha")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Operation(summary = "Lista itens do cardápio")
    @GetMapping
    public ResponseEntity<List<MenuItemOutput>> list() {
        // Stub: substituir por consulta real ao módulo Product/Menu.
        return ResponseEntity.ok(List.of(
                new MenuItemOutput(1L, "Pizza Margherita", "Massa fina, molho de tomate, mussarela e manjericão", new BigDecimal("49.90"), "Pizzas", "🍕"),
                new MenuItemOutput(2L, "Hambúrguer Artesanal", "180g de blend bovino, queijo cheddar e bacon", new BigDecimal("38.50"), "Lanches", "🍔"),
                new MenuItemOutput(3L, "Lasanha Bolonhesa", "Massa fresca, ragu de carne e bechamel", new BigDecimal("44.00"), "Massas", "🍝"),
                new MenuItemOutput(4L, "Salada Caesar", "Alface americana, frango grelhado, croutons e parmesão", new BigDecimal("32.00"), "Saladas", "🥗"),
                new MenuItemOutput(5L, "Sushi Combo (15 pçs)", "Variedade de niguiris e uramakis", new BigDecimal("69.90"), "Japonês", "🍣"),
                new MenuItemOutput(6L, "Suco Natural Laranja", "500ml, fruta espremida na hora", new BigDecimal("12.00"), "Bebidas", "🥤"),
                new MenuItemOutput(7L, "Cerveja Long Neck", "350ml, gelada", new BigDecimal("11.00"), "Bebidas", "🍺"),
                new MenuItemOutput(8L, "Petit Gateau", "Bolo morno de chocolate com sorvete de baunilha", new BigDecimal("24.00"), "Sobremesas", "🍰")
        ));
    }

    public record MenuItemOutput(Long id, String name, String description, BigDecimal price, String category, String emoji) {}
}
