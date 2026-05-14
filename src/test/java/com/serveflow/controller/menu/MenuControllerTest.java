package com.serveflow.controller.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.serveflow.dto.menu.request.MenuInput;
import com.serveflow.dto.menu.request.MenuItemInput;
import com.serveflow.dto.menu.request.RemoveMenuItemInput;
import com.serveflow.dto.menu.request.UpdateAvailabilityInput;
import com.serveflow.dto.menu.response.MenuOutput;
import com.serveflow.exception.handler.GlobalExceptionHandler;
import com.serveflow.exception.menu.MenuNotFound;
import com.serveflow.service.menu.MenuService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MenuControllerTest {

    @Mock
    MenuService menuService;

    @InjectMocks
    MenuController controller;

    MockMvc mvc;
    ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private MenuInput validMenuInput() {
        MenuItemInput item = new MenuItemInput(UUID.randomUUID(), "Hamburguer", "Clássico", new BigDecimal("29.90"));
        return new MenuInput("Cardápio Almoço", List.of(item));
    }

    private MenuOutput menuOutput(UUID id) {
        return new MenuOutput(id, "Cardápio Almoço", "ACTIVE", null, List.of(), LocalDateTime.of(2026, 1, 1, 12, 0));
    }

    private String json(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    @Test
    @DisplayName("POST /menus: retorna 201 com body ao criar menu válido")
    void create_returns201WithBody() throws Exception {
        UUID id = UUID.randomUUID();
        when(menuService.create(any(MenuInput.class))).thenReturn(menuOutput(id));

        mvc.perform(post("/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(validMenuInput())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Cardápio Almoço"));

        verify(menuService).create(any(MenuInput.class));
    }

    @Test
    @DisplayName("POST /menus: retorna 400 quando name está ausente")
    void create_returns400WhenNameMissing() throws Exception {
        String body = "{\"name\":\"\",\"items\":[{\"productId\":\"" + UUID.randomUUID() + "\",\"name\":\"Item\",\"price\":10.0}]}";

        mvc.perform(post("/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields").exists());
    }

    @Test
    @DisplayName("POST /menus: retorna 400 com body malformado")
    void create_returns400WhenBodyMalformed() throws Exception {
        mvc.perform(post("/menus")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ bad json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /menus: retorna 200 com lista de menus")
    void findAll_returns200WithList() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(menuService.findAll()).thenReturn(List.of(menuOutput(id1), menuOutput(id2)));

        mvc.perform(get("/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(menuService).findAll();
    }

    @Test
    @DisplayName("GET /menus: retorna 200 com lista vazia quando não há menus")
    void findAll_returns200WithEmptyList() throws Exception {
        when(menuService.findAll()).thenReturn(List.of());

        mvc.perform(get("/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("GET /menus/{id}: retorna 200 com menu quando encontrado")
    void findById_returns200WithMenu() throws Exception {
        UUID id = UUID.randomUUID();
        when(menuService.findById(id)).thenReturn(menuOutput(id));

        mvc.perform(get("/menus/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(menuService).findById(id);
    }

    @Test
    @DisplayName("GET /menus/{id}: retorna 404 quando menu não existe")
    void findById_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(menuService.findById(id)).thenThrow(new MenuNotFound(id));

        mvc.perform(get("/menus/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /menus/{id}/unlock: retorna 200 com menu desbloqueado")
    void unlock_returns200() throws Exception {
        UUID id = UUID.randomUUID();
        when(menuService.unlock(id)).thenReturn(menuOutput(id));

        mvc.perform(patch("/menus/{id}/unlock", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));

        verify(menuService).unlock(id);
    }

    @Test
    @DisplayName("PATCH /menus/{id}/unlock: retorna 404 quando menu não existe")
    void unlock_returns404WhenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(menuService.unlock(id)).thenThrow(new MenuNotFound(id));

        mvc.perform(patch("/menus/{id}/unlock", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH /menus/{menuId}/items/{itemId}/availability: retorna 200")
    void updateAvailability_returns200() throws Exception {
        UUID menuId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        when(menuService.updateItemAvailability(eq(menuId), eq(itemId), eq(true))).thenReturn(menuOutput(menuId));

        mvc.perform(patch("/menus/{menuId}/items/{itemId}/availability", menuId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new UpdateAvailabilityInput(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(menuId.toString()));

        verify(menuService).updateItemAvailability(menuId, itemId, true);
    }

    @Test
    @DisplayName("DELETE /menus/{menuId}/items/{itemId}: retorna 200 com menu atualizado")
    void removeItem_returns200() throws Exception {
        UUID menuId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        RemoveMenuItemInput request = new RemoveMenuItemInput("Chef João");
        when(menuService.removeItem(eq(menuId), eq(itemId), any(RemoveMenuItemInput.class))).thenReturn(menuOutput(menuId));

        mvc.perform(delete("/menus/{menuId}/items/{itemId}", menuId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(menuId.toString()));

        verify(menuService).removeItem(eq(menuId), eq(itemId), any(RemoveMenuItemInput.class));
    }
}
