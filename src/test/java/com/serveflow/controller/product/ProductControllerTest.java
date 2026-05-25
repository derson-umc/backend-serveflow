package com.serveflow.controller.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.product.request.ProductInput;
import com.serveflow.dto.product.response.ProductOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.product.ProductNotFoundException;
import com.serveflow.model.user.User;
import com.serveflow.model.user.UserRole;
import com.serveflow.service.audit.AuditService;
import com.serveflow.service.product.ProductService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    ProductService productService;
    @Mock
    AuditService auditService;

    @InjectMocks
    ProductController controller;

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

    private ProductInput validInput() {
        return new ProductInput(
                "Hamburguer Artesanal",
                "Blend 180g, queijo, pão brioche",
                "Lanches",
                "Marca X",
                new BigDecimal("29.90"),
                "350g",
                null,
                null,
                null
        );
    }

    private ProductOutput output(UUID id) {
        return new ProductOutput(
                id,
                "Hamburguer Artesanal",
                "Blend 180g, queijo, pão brioche",
                "Lanches",
                "Marca X",
                new BigDecimal("29.90"),
                "350g",
                null,
                true,
                false,
                LocalDateTime.of(2026, 1, 1, 12, 0)
        );
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("POST /products: retorna 201 com body ao criar produto válido")
    void create_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.create(any(ProductInput.class))).thenReturn(output(id));

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Hamburguer Artesanal"))
                .andExpect(jsonPath("$.price").value(29.90));

        verify(productService).create(any(ProductInput.class));
    }

    @Test
    @DisplayName("POST /products: retorna 400 quando campos obrigatórios estão ausentes")
    void create_returns400WhenRequiredFieldsMissing() throws Exception {
        ProductInput invalid = new ProductInput(null, null, null, null, null, null, null, null, null);

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    @DisplayName("POST /products: retorna 400 com body malformado")
    void create_returns400WhenBodyMalformed() throws Exception {
        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /products/batch: retorna 201 com lista ao criar em lote")
    void createBatch_returns201WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(productService.createBatch(anyList())).thenReturn(List.of(output(id1), output(id2)));

        mvc.perform(post("/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(List.of(validInput(), validInput()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(id1.toString()))
                .andExpect(jsonPath("$[1].id").value(id2.toString()));

        verify(productService).createBatch(anyList());
    }

    @Test
    @DisplayName("POST /products/batch: retorna 400 com body malformado")
    void createBatch_returns400WhenBodyMalformed() throws Exception {
        mvc.perform(post("/products/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not json at all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /products/{id}: retorna 200 com produto quando encontrado")
    void findById_returns200WithProduct() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenReturn(output(id));

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.active").value(true));

        verify(productService).findById(id);
    }

    @Test
    @DisplayName("GET /products/{id}: retorna 404 quando produto não existe")
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.findById(id)).thenThrow(new ProductNotFoundException(id));

        mvc.perform(get("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /products/{id}: retorna 400 quando id não é UUID válido")
    void findById_returns400WhenIdIsNotUUID() throws Exception {
        mvc.perform(get("/products/{id}", "nao-e-um-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /products: retorna 200 com lista de produtos ativos")
    void findAllActive_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(productService.findAllActive()).thenReturn(List.of(output(id1), output(id2)));

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productService).findAllActive();
    }

    @Test
    @DisplayName("GET /products: retorna 200 com lista vazia quando não há ativos")
    void findAllActive_returns200WithEmptyList() throws Exception {
        when(productService.findAllActive()).thenReturn(List.of());

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("PUT /products/{id}: retorna 200 com produto atualizado")
    void update_returns200WithUpdatedProduct() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any(ProductInput.class))).thenReturn(output(id));

        mvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validInput())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(productService).update(eq(id), any(ProductInput.class));
    }

    @Test
    @DisplayName("PUT /products/{id}: retorna 404 quando produto não existe")
    void update_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(productService.update(eq(id), any(ProductInput.class))).thenThrow(new ProductNotFoundException(id));

        mvc.perform(put("/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validInput())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("DELETE /products/{id}: retorna 204 sem body ao desativar")
    void deactivate_returns204NoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(productService).deactivate(id);

        mvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNoContent());

        verify(productService).deactivate(id);
    }

    @Test
    @DisplayName("DELETE /products/{id}: retorna 404 quando produto não existe")
    void deactivate_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProductNotFoundException(id)).when(productService).deactivate(id);

        mvc.perform(delete("/products/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
