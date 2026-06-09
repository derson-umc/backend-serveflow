package com.serveflow.controller.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.stock.request.ProductRecipeInput;
import com.serveflow.dto.stock.response.ProductRecipeOutput;
import com.serveflow.dto.stock.response.StockMovementOutput;
import com.serveflow.dto.stock.response.StockMovementsPageOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.stock.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class StockControllerExtendedTest {

    @Mock StockService stockService;
    @Mock AuditService auditService;

    @InjectMocks StockController controller;

    MockMvc mvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        User mockUser = new User(1L, "admin", "admin@test.com", "pass", UserRole.ADMIN, "Administrador");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities()));
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler(auditService))
                .build();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private StockMovementOutput movementOutput(UUID id) {
        return new StockMovementOutput(id, UUID.randomUUID(), "Farinha", "ENTRY",
                "Entrada", new BigDecimal("10.0"), new BigDecimal("5.0"),
                new BigDecimal("15.0"), "Compra", null,
                LocalDateTime.of(2026, 1, 1, 10, 0));
    }

    private ProductRecipeOutput recipeOutput(UUID id, UUID productId) {
        return new ProductRecipeOutput(id, productId, "Pizza", "Assar", List.of(), "COMIDA");
    }

    @Nested
    @DisplayName("GET /stock/movements/filter")
    class FilterMovements {

        @Test
        @DisplayName("retorna 200 com movimentos filtrados")
        void filterMovements_returns200() throws Exception {
            StockMovementsPageOutput page = new StockMovementsPageOutput(
                    List.of(movementOutput(UUID.randomUUID())), 0, 50, 1L, 1);
            when(stockService.findMovementsFiltered(any(), any(), any(), any(), anyInt(), anyInt()))
                    .thenReturn(page);

            mvc.perform(get("/stock/movements/filter")
                            .param("page", "0")
                            .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.page").value(0))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(stockService).findMovementsFiltered(null, null, null, null, 0, 50);
        }

        @Test
        @DisplayName("retorna 200 com filtro por type")
        void filterMovements_withType_returns200() throws Exception {
            StockMovementsPageOutput page = new StockMovementsPageOutput(List.of(), 0, 50, 0L, 0);
            when(stockService.findMovementsFiltered(any(), eq("ENTRY"), any(), any(), anyInt(), anyInt()))
                    .thenReturn(page);

            mvc.perform(get("/stock/movements/filter")
                            .param("type", "ENTRY"))
                    .andExpect(status().isOk());

            verify(stockService).findMovementsFiltered(null, "ENTRY", null, null, 0, 50);
        }
    }

    @Nested
    @DisplayName("GET /stock/movements/order/{orderId}")
    class MovementsByOrder {

        @Test
        @DisplayName("retorna 200 com movimentos do pedido")
        void movementsByOrder_returns200() throws Exception {
            UUID orderId = UUID.randomUUID();
            UUID moveId = UUID.randomUUID();
            when(stockService.findMovementsByOrder(orderId))
                    .thenReturn(List.of(movementOutput(moveId)));

            mvc.perform(get("/stock/movements/order/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));

            verify(stockService).findMovementsByOrder(orderId);
        }

        @Test
        @DisplayName("retorna 200 com lista vazia quando pedido sem movimentos")
        void movementsByOrder_returnsEmpty() throws Exception {
            UUID orderId = UUID.randomUUID();
            when(stockService.findMovementsByOrder(orderId)).thenReturn(List.of());

            mvc.perform(get("/stock/movements/order/{orderId}", orderId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("PUT /stock/recipes/{id}")
    class UpdateRecipe {

        @Test
        @DisplayName("retorna 200 com receita atualizada")
        void updateRecipe_returns200() throws Exception {
            UUID recipeId = UUID.randomUUID();
            UUID productId = UUID.randomUUID();
            ProductRecipeOutput output = recipeOutput(recipeId, productId);
            when(stockService.updateRecipe(eq(recipeId), any(ProductRecipeInput.class)))
                    .thenReturn(output);

            // ProductRecipeInput(productId, productName, preparationMode, productType, ingredients)
            ProductRecipeInput input = new ProductRecipeInput(productId, "Pizza", "Assar", "COMIDA", List.of(
                    new com.serveflow.dto.stock.request.RecipeIngredientInput(
                            UUID.randomUUID(), "Farinha", new BigDecimal("0.5"), "KG", null)
            ));

            mvc.perform(put("/stock/recipes/{id}", recipeId)
                            .contentType("application/json")
                            .content(mapper.writeValueAsString(input)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(recipeId.toString()))
                    .andExpect(jsonPath("$.productName").value("Pizza"));

            verify(stockService).updateRecipe(eq(recipeId), any(ProductRecipeInput.class));
        }
    }

    @Nested
    @DisplayName("GET /stock/recipes/product/{productId}")
    class FindRecipeByProduct {

        @Test
        @DisplayName("retorna 200 com receita do produto")
        void findRecipeByProduct_returns200() throws Exception {
            UUID productId = UUID.randomUUID();
            UUID recipeId = UUID.randomUUID();
            when(stockService.findRecipeByProductId(productId)).thenReturn(recipeOutput(recipeId, productId));

            mvc.perform(get("/stock/recipes/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.productId").value(productId.toString()));

            verify(stockService).findRecipeByProductId(productId);
        }
    }
}
