package com.serveflow.Controller.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.serveflow.Dto.User.Request.UserInput;
import com.serveflow.Dto.User.Response.UserOutput;
import com.serveflow.Service.User.UserService;

import java.util.List;

@Tag(name = "Usuários", description = "CRUD de usuários (ADMIN/ROOT)")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @Operation(summary = "Criar usuário")
    @PostMapping
    public ResponseEntity<UserOutput> create(@Valid @RequestBody UserInput request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Listar usuários")
    @GetMapping
    public ResponseEntity<List<UserOutput>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @Operation(summary = "Buscar usuário por id")
    @GetMapping("/{id}")
    public ResponseEntity<UserOutput> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Atualizar usuário")
    @PutMapping("/{id}")
    public ResponseEntity<UserOutput> update(@PathVariable Long id,
                                             @Valid @RequestBody UserInput request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Excluir usuário (apenas ROOT)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
