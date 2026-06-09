package com.serveflow.model.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserRole enum")
class UserRoleTest {

    @Nested
    @DisplayName("isAdmin()")
    class IsAdmin {

        @Test
        @DisplayName("ADMIN.isAdmin() retorna true")
        void admin_isAdmin_true() {
            assertThat(UserRole.ADMIN.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("outros roles retornam false para isAdmin()")
        void otherRoles_isAdmin_false() {
            assertThat(UserRole.GERENTE.isAdmin()).isFalse();
            assertThat(UserRole.CAIXA.isAdmin()).isFalse();
            assertThat(UserRole.GARCON.isAdmin()).isFalse();
            assertThat(UserRole.COZINHEIRO.isAdmin()).isFalse();
            assertThat(UserRole.USER.isAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("isGerente()")
    class IsGerente {

        @Test
        @DisplayName("GERENTE.isGerente() retorna true")
        void gerente_isGerente_true() {
            assertThat(UserRole.GERENTE.isGerente()).isTrue();
        }

        @Test
        @DisplayName("outros roles retornam false para isGerente()")
        void otherRoles_isGerente_false() {
            assertThat(UserRole.ADMIN.isGerente()).isFalse();
            assertThat(UserRole.CAIXA.isGerente()).isFalse();
            assertThat(UserRole.GARCON.isGerente()).isFalse();
            assertThat(UserRole.USER.isGerente()).isFalse();
        }
    }

    @Nested
    @DisplayName("canAccessFinanceiro()")
    class CanAccessFinanceiro {

        @Test
        @DisplayName("ADMIN e GERENTE podem acessar financeiro")
        void adminGerente_canAccessFinanceiro() {
            assertThat(UserRole.ADMIN.canAccessFinanceiro()).isTrue();
            assertThat(UserRole.GERENTE.canAccessFinanceiro()).isTrue();
        }

        @Test
        @DisplayName("CAIXA, GARCON, COZINHEIRO, USER não acessam financeiro")
        void others_cannotAccessFinanceiro() {
            assertThat(UserRole.CAIXA.canAccessFinanceiro()).isFalse();
            assertThat(UserRole.GARCON.canAccessFinanceiro()).isFalse();
            assertThat(UserRole.COZINHEIRO.canAccessFinanceiro()).isFalse();
            assertThat(UserRole.USER.canAccessFinanceiro()).isFalse();
        }
    }

    @Nested
    @DisplayName("canAccessPedidos()")
    class CanAccessPedidos {

        @Test
        @DisplayName("ADMIN, GERENTE, CAIXA, GARCON, COZINHEIRO podem acessar pedidos")
        void mostRoles_canAccessPedidos() {
            assertThat(UserRole.ADMIN.canAccessPedidos()).isTrue();
            assertThat(UserRole.GERENTE.canAccessPedidos()).isTrue();
            assertThat(UserRole.CAIXA.canAccessPedidos()).isTrue();
            assertThat(UserRole.GARCON.canAccessPedidos()).isTrue();
            assertThat(UserRole.COZINHEIRO.canAccessPedidos()).isTrue();
        }

        @Test
        @DisplayName("USER não pode acessar pedidos")
        void user_cannotAccessPedidos() {
            assertThat(UserRole.USER.canAccessPedidos()).isFalse();
        }
    }

    @Nested
    @DisplayName("canAlterarPedidos()")
    class CanAlterarPedidos {

        @Test
        @DisplayName("ADMIN, GERENTE, GARCON, COZINHEIRO podem alterar pedidos")
        void rolesCanAlterarPedidos() {
            assertThat(UserRole.ADMIN.canAlterarPedidos()).isTrue();
            assertThat(UserRole.GERENTE.canAlterarPedidos()).isTrue();
            assertThat(UserRole.GARCON.canAlterarPedidos()).isTrue();
            assertThat(UserRole.COZINHEIRO.canAlterarPedidos()).isTrue();
        }

        @Test
        @DisplayName("CAIXA e USER não podem alterar pedidos")
        void caixaUser_cannotAlterarPedidos() {
            assertThat(UserRole.CAIXA.canAlterarPedidos()).isFalse();
            assertThat(UserRole.USER.canAlterarPedidos()).isFalse();
        }
    }

    @Nested
    @DisplayName("canCozinha()")
    class CanCozinha {

        @Test
        @DisplayName("ADMIN, GERENTE, COZINHEIRO podem acessar cozinha")
        void rolesCanCozinha() {
            assertThat(UserRole.ADMIN.canCozinha()).isTrue();
            assertThat(UserRole.GERENTE.canCozinha()).isTrue();
            assertThat(UserRole.COZINHEIRO.canCozinha()).isTrue();
        }

        @Test
        @DisplayName("CAIXA, GARCON, USER não acessam cozinha")
        void others_cannotCozinha() {
            assertThat(UserRole.CAIXA.canCozinha()).isFalse();
            assertThat(UserRole.GARCON.canCozinha()).isFalse();
            assertThat(UserRole.USER.canCozinha()).isFalse();
        }
    }

    @Nested
    @DisplayName("canVendas()")
    class CanVendas {

        @Test
        @DisplayName("CAIXA pode acessar vendas")
        void caixa_canVendas() {
            assertThat(UserRole.CAIXA.canVendas()).isTrue();
        }

        @Test
        @DisplayName("outros roles não acessam vendas diretamente (apenas via CAIXA)")
        void others_cannotVendas() {
            assertThat(UserRole.ADMIN.canVendas()).isFalse();
            assertThat(UserRole.GERENTE.canVendas()).isFalse();
            assertThat(UserRole.GARCON.canVendas()).isFalse();
            assertThat(UserRole.COZINHEIRO.canVendas()).isFalse();
            assertThat(UserRole.USER.canVendas()).isFalse();
        }
    }

    @Nested
    @DisplayName("getPermissions()")
    class GetPermissions {

        @Test
        @DisplayName("ADMIN possui todas as permissões")
        void admin_hasAllPermissions() {
            assertThat(UserRole.ADMIN.getPermissions())
                    .contains(Permission.ADMIN, Permission.FINANCEIRO,
                              Permission.PEDIDOS, Permission.COZINHA);
        }

        @Test
        @DisplayName("USER não possui permissões")
        void user_hasNoPermissions() {
            assertThat(UserRole.USER.getPermissions()).isEmpty();
        }

        @Test
        @DisplayName("GARCON possui apenas PEDIDOS")
        void garcon_hasPedidos() {
            assertThat(UserRole.GARCON.getPermissions())
                    .containsExactly(Permission.PEDIDOS);
        }
    }
}
