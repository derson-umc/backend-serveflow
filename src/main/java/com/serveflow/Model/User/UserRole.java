package com.serveflow.Model.User;

import java.util.Set;

public enum UserRole {

    ROOT(Set.of("ADMIN", "FINANCEIRO", "PEDIDOS", "COZINHA")),
    ADMIN(Set.of("ADMIN", "FINANCEIRO", "PEDIDOS", "COZINHA")),
    CAIXA(Set.of("VENDAS", "PEDIDOS")),
    GARCON(Set.of("PEDIDOS")),
    COZINHEIRO(Set.of("COZINHA", "PEDIDOS")),
    USER(Set.of());

    private final Set<String> permissions;

    UserRole(Set<String> permissions) {
        this.permissions = permissions;
    }

    public boolean isAdmin() {
        return this == ROOT || this == ADMIN;
    }

    public boolean canAccessFinanceiro() {
        return permissions.contains("FINANCEIRO");
    }

    public boolean canAccessPedidos() {
        return permissions.contains("PEDIDOS");
    }

    public boolean canAlterarPedidos() {
        return this == ROOT || this == ADMIN || this == GARCON || this == COZINHEIRO;
    }

    public boolean canCozinha() {
        return permissions.contains("COZINHA");
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}
