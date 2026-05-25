package com.serveflow.controller.stock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.stock.request.*;
import com.serveflow.dto.stock.response.*;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.stock.InsufficientStockException;
import com.serveflow.exception.stock.RecipeNotFoundException;
import com.serveflow.exception.stock.StockItemNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.stock.StockService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
class StockControllerTest {

    @Mock
    StockService stockService;
    @Mock
    AuditService auditService;

    @InjectMocks
    StockController controller;

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

    private StockItemOutput itemOutput(UUID id) {
        return new StockItemOutput(id, "Farinha", "kg",
                new BigDecimal("10.0"), new BigDecimal("2.0"), false,
                "Secos", "Fornecedor X", new BigDecimal("5.00"),
                "ACTIVE", LocalDateTime.of(2026, 1, 1, 12, 0));
    }

    private ProductRecipeOutput recipeOutput(UUID id, UUID productId) {
        return new ProductRecipeOutput(id, productId, "Hamburguer",
                "Modo de preparo", List.of(), "FABRICATED");
    }

    private StockAlertOutput alertOutput(UUID id) {
        return new StockAlertOutput(id, UUID.randomUUID(), "Farinha",
                new BigDecimal("0.5"), new BigDecimal("2.0"),
                false, LocalDateTime.of(2026, 1, 1, 12, 0), null);
    }

    private StockItemInput validItemInput() {
        return new StockItemInput("Farinha", "kg", new BigDecimal("10.0"),
                new BigDecimal("2.0"), "Secos", "Fornecedor X", new BigDecimal("5.00"));
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("GET /stock/report/consolidated: retorna 200 com relatório consolidado")
    void consolidatedReport_returns200() throws Exception {
        when(stockService.findConsolidatedReport()).thenReturn(List.of());

        mvc.perform(get("/stock/report/consolidated"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(stockService).findConsolidatedReport();
    }

    @Test
    @DisplayName("POST /stock/items: retorna 201 com item criado")
    void createItem_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.createItem(any(StockItemInput.class))).thenReturn(itemOutput(id));

        mvc.perform(post("/stock/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validItemInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Farinha"));

        verify(stockService).createItem(any(StockItemInput.class));
    }

    @Test
    @DisplayName("POST /stock/items: retorna 400 quando campos obrigatórios ausentes")
    void createItem_returns400_whenInvalid() throws Exception {
        StockItemInput invalid = new StockItemInput(null, null, null, null, null, null, null);

        mvc.perform(post("/stock/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /stock/items: retorna 200 com lista de todos os items")
    void findAllItems_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(stockService.findAllItems()).thenReturn(List.of(itemOutput(id1), itemOutput(id2)));

        mvc.perform(get("/stock/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(stockService).findAllItems();
    }

    @Test
    @DisplayName("GET /stock/items/active: retorna 200 com lista de items ativos")
    void findActiveItems_returns200WithList() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.findActiveItems()).thenReturn(List.of(itemOutput(id)));

        mvc.perform(get("/stock/items/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(stockService).findActiveItems();
    }

    @Test
    @DisplayName("GET /stock/items/{id}: retorna 200 quando item existe")
    void findItemById_returns200_whenFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.findItemById(id)).thenReturn(itemOutput(id));

        mvc.perform(get("/stock/items/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(stockService).findItemById(id);
    }

    @Test
    @DisplayName("GET /stock/items/{id}: retorna 404 quando item não existe")
    void findItemById_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.findItemById(id)).thenThrow(new StockItemNotFoundException(id));

        mvc.perform(get("/stock/items/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PUT /stock/items/{id}: retorna 200 com item atualizado")
    void updateItem_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.updateItem(eq(id), any(StockItemInput.class))).thenReturn(itemOutput(id));

        mvc.perform(put("/stock/items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validItemInput())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(stockService).updateItem(eq(id), any(StockItemInput.class));
    }

    @Test
    @DisplayName("PATCH /stock/items/{id}/toggle-status: retorna 200 com status alternado")
    void toggleStatus_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        StockItemOutput inactive = new StockItemOutput(id, "Farinha", "kg",
                new BigDecimal("10.0"), new BigDecimal("2.0"), false,
                "Secos", "Fornecedor X", new BigDecimal("5.00"),
                "INACTIVE", LocalDateTime.of(2026, 1, 1, 12, 0));
        when(stockService.toggleStatus(id)).thenReturn(inactive);

        mvc.perform(patch("/stock/items/{id}/toggle-status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(stockService).toggleStatus(id);
    }

    @Test
    @DisplayName("POST /stock/items/{id}/entry: retorna 200 com estoque atualizado")
    void addStock_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.addStock(eq(id), any(StockEntryInput.class))).thenReturn(itemOutput(id));

        StockEntryInput input = new StockEntryInput(new BigDecimal("5.0"), "Compra", null, null);

        mvc.perform(post("/stock/items/{id}/entry", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(stockService).addStock(eq(id), any(StockEntryInput.class));
    }

    @Test
    @DisplayName("POST /stock/items/{id}/loss: retorna 200 com perda registrada")
    void recordLoss_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.recordLoss(eq(id), any(StockLossInput.class))).thenReturn(itemOutput(id));

        StockLossInput input = new StockLossInput(new BigDecimal("2.0"), "Vencimento");

        mvc.perform(post("/stock/items/{id}/loss", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isOk());

        verify(stockService).recordLoss(eq(id), any(StockLossInput.class));
    }

    @Test
    @DisplayName("POST /stock/items/{id}/loss: retorna 422 quando estoque insuficiente")
    void recordLoss_returns422_whenInsufficientStock() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.recordLoss(eq(id), any(StockLossInput.class)))
                .thenThrow(new InsufficientStockException("Estoque insuficiente para Farinha."));

        StockLossInput input = new StockLossInput(new BigDecimal("99.0"), "Quebra");

        mvc.perform(post("/stock/items/{id}/loss", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("POST /stock/items/{id}/adjust: retorna 200 com ajuste aplicado")
    void recordAdjustment_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.recordAdjustment(eq(id), any(StockAdjustmentInput.class))).thenReturn(itemOutput(id));

        StockAdjustmentInput input = new StockAdjustmentInput(new BigDecimal("15.0"), "Inventário");

        mvc.perform(post("/stock/items/{id}/adjust", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isOk());

        verify(stockService).recordAdjustment(eq(id), any(StockAdjustmentInput.class));
    }

    @Test
    @DisplayName("GET /stock/movements: retorna 200 com lista de movimentações")
    void findAllMovements_returns200WithList() throws Exception {
        when(stockService.findAllMovements()).thenReturn(List.of());

        mvc.perform(get("/stock/movements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(stockService).findAllMovements();
    }

    @Test
    @DisplayName("GET /stock/items/{id}/movements: retorna 200 com movimentações do item")
    void movementsByItem_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(stockService.findMovementsByStockItem(id)).thenReturn(List.of());

        mvc.perform(get("/stock/items/{id}/movements", id))
                .andExpect(status().isOk());

        verify(stockService).findMovementsByStockItem(id);
    }

    @Test
    @DisplayName("POST /stock/recipes: retorna 201 com ficha técnica criada")
    void createRecipe_returns201() throws Exception {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(stockService.createRecipe(any(ProductRecipeInput.class))).thenReturn(recipeOutput(id, productId));

        ProductRecipeInput input = new ProductRecipeInput(productId, "Hamburguer",
                "Modo de preparo", "FABRICATED",
                List.of(new RecipeIngredientInput(UUID.randomUUID(), "Farinha",
                        new BigDecimal("0.5"), "kg", null)));

        mvc.perform(post("/stock/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(stockService).createRecipe(any(ProductRecipeInput.class));
    }

    @Test
    @DisplayName("GET /stock/recipes: retorna 200 com lista de fichas técnicas")
    void findAllRecipes_returns200WithList() throws Exception {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(stockService.findAllRecipes()).thenReturn(List.of(recipeOutput(id, productId)));

        mvc.perform(get("/stock/recipes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(stockService).findAllRecipes();
    }

    @Test
    @DisplayName("GET /stock/recipes/{id}: retorna 200 com ficha técnica")
    void findRecipeById_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        when(stockService.findRecipeById(id)).thenReturn(recipeOutput(id, productId));

        mvc.perform(get("/stock/recipes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(stockService).findRecipeById(id);
    }

    @Test
    @DisplayName("GET /stock/recipes/product/{productId}: retorna 404 quando produto não tem ficha")
    void findRecipeByProduct_returns404_whenNotFound() throws Exception {
        UUID productId = UUID.randomUUID();
        when(stockService.findRecipeByProductId(productId))
                .thenThrow(new RecipeNotFoundException(productId));

        mvc.perform(get("/stock/recipes/product/{productId}", productId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /stock/alerts: retorna 200 com alertas ativos")
    void findActiveAlerts_returns200() throws Exception {
        UUID alertId = UUID.randomUUID();
        when(stockService.findAllActiveAlerts()).thenReturn(List.of(alertOutput(alertId)));

        mvc.perform(get("/stock/alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(stockService).findAllActiveAlerts();
    }

    @Test
    @DisplayName("PATCH /stock/alerts/{id}/resolve: retorna 200 com alerta resolvido")
    void resolveAlert_returns200() throws Exception {
        UUID alertId = UUID.randomUUID();
        StockAlertOutput resolved = new StockAlertOutput(alertId, UUID.randomUUID(), "Farinha",
                new BigDecimal("0.5"), new BigDecimal("2.0"),
                true, LocalDateTime.of(2026, 1, 1, 12, 0), LocalDateTime.of(2026, 1, 2, 12, 0));
        when(stockService.resolveAlert(alertId)).thenReturn(resolved);

        mvc.perform(patch("/stock/alerts/{id}/resolve", alertId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resolved").value(true));

        verify(stockService).resolveAlert(alertId);
    }
}
