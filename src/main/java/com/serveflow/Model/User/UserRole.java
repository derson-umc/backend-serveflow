package com.serveflow.Model.User;

import com.serveflow.Model.Permission;

import java.util.Set;

public enum UserRole {

    ADMIN(Set.of(Permission.ADMIN, Permission.FINANCEIRO, Permission.PEDIDOS, Permission.COZINHA)),
    GERENTE(Set.of(Permission.FINANCEIRO, Permission.PEDIDOS, Permission.COZINHA)),
    CAIXA(Set.of(Permission.VENDAS, Permission.PEDIDOS)),
    GARCON(Set.of(Permission.PEDIDOS)),
    COZINHEIRO(Set.of(Permission.COZINHA, Permission.PEDIDOS)),
    USER(Set.of());

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isGerente() {
        return this == GERENTE;
    }

    public boolean canAccessFinanceiro() {
        return permissions.contains(Permission.FINANCEIRO);
    }

    public boolean canAccessPedidos() {
        return permissions.contains(Permission.PEDIDOS);
    }

    public boolean canAlterarPedidos() {
        return this == ADMIN || this == GERENTE || this == GARCON || this == COZINHEIRO;
    }

    public boolean canCozinha() {
        return permissions.contains(Permission.COZINHA);
    }

    public boolean canVendas() {
        return permissions.contains(Permission.VENDAS);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}