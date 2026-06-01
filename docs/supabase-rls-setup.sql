-- =============================================================================
-- ServeFlow — Supabase RLS Security Setup
-- =============================================================================
-- Contexto da arquitetura:
--   Frontend (React) → Spring Boot API (Render) → Supabase PostgreSQL
--   O frontend NÃO usa o SDK do Supabase diretamente.
--   Todo acesso ao banco passa pelo backend Spring Boot.
--
-- Problema atual:
--   O backend conecta como 'postgres' (superuser do Supabase), que
--   ignora RLS incondicionalmente — mesmo com RLS ativo nas tabelas.
--
-- Solução implementada por este script:
--   1. Cria o role 'serveflow_app' para o backend (não superuser)
--   2. Cria o login user 'serveflow_backend' que herda de 'serveflow_app'
--   3. Revoga acesso público
--   4. Habilita RLS em todas as tabelas da aplicação
--   5. Cria políticas de acesso exclusivo para 'serveflow_app'
--
-- AÇÃO OBRIGATÓRIA APÓS RODAR:
--   Atualizar a variável de ambiente POSTGRES_USER=serveflow_backend
--   e POSTGRES_PASSWORD=<senha_definida_abaixo> no Render (backend).
--
-- ORDEM DE EXECUÇÃO: rodar como 'postgres' (superuser) no Supabase SQL Editor.
-- =============================================================================


-- =============================================================================
-- SEÇÃO 1 — Role dedicada para o backend
-- =============================================================================

-- Role de grupo (sem login) para agrupar permissões
CREATE ROLE serveflow_app NOLOGIN NOINHERIT NOSUPERUSER NOCREATEDB NOCREATEROLE;

-- Usuário de login que o backend Spring Boot irá usar
-- IMPORTANTE: troque 'SUBSTITUA_AQUI' por uma senha forte antes de executar
CREATE USER serveflow_backend
  WITH PASSWORD 'SUBSTITUA_AQUI'
  NOINHERIT NOSUPERUSER NOCREATEDB NOCREATEROLE NOBYPASSRLS CONNECTION LIMIT 10;

GRANT serveflow_app TO serveflow_backend;


-- =============================================================================
-- SEÇÃO 2 — Revogar acesso público ao schema
-- =============================================================================

-- Remove permissão padrão do PUBLIC (todo role) ao schema public
REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL TABLES IN SCHEMA public FROM PUBLIC;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM PUBLIC;

-- Garante acesso apenas ao role da aplicação
GRANT USAGE ON SCHEMA public TO serveflow_app;


-- =============================================================================
-- SEÇÃO 3 — Permissões de tabela por nível de operação
-- =============================================================================

-- Tabelas com CRUD completo (a aplicação lê, cria, atualiza e remove)
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE
  users,
  password_reset_tokens,
  refresh_tokens,
  products,
  stock_items,
  stock_alerts,
  product_recipes,
  recipe_ingredients,
  menus,
  menu_items,
  addresses,
  orders,
  order_items,
  item_additionals,
  accounts_receivable,
  accounts_payable,
  cash_sessions
TO serveflow_app;

-- Tabelas de movimentação/auditoria: INSERT + SELECT apenas
-- (movimentos não devem ser alterados ou excluídos)
GRANT SELECT, INSERT ON TABLE
  stock_movements,
  cash_movements,
  financial_audit,
  access_log,
  audit_log,
  error_log
TO serveflow_app;

-- Sequences (necessário para BIGSERIAL / SERIAL)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO serveflow_app;

-- Garante que permissões se apliquem a futuras tabelas criadas por 'postgres'
ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO serveflow_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
  GRANT USAGE, SELECT ON SEQUENCES TO serveflow_app;


-- =============================================================================
-- SEÇÃO 4 — Habilitar RLS em todas as tabelas da aplicação
-- =============================================================================
-- Nota: flyway_schema_history é gerenciado pelo Flyway como 'postgres'
--       e não precisa de RLS (o superuser bypassa de qualquer forma).

ALTER TABLE users                ENABLE ROW LEVEL SECURITY;
ALTER TABLE password_reset_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE refresh_tokens        ENABLE ROW LEVEL SECURITY;
ALTER TABLE products              ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_items           ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_movements       ENABLE ROW LEVEL SECURITY;
ALTER TABLE stock_alerts          ENABLE ROW LEVEL SECURITY;
ALTER TABLE product_recipes       ENABLE ROW LEVEL SECURITY;
ALTER TABLE recipe_ingredients    ENABLE ROW LEVEL SECURITY;
ALTER TABLE menus                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE menu_items            ENABLE ROW LEVEL SECURITY;
ALTER TABLE addresses             ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders                ENABLE ROW LEVEL SECURITY;
ALTER TABLE order_items           ENABLE ROW LEVEL SECURITY;
ALTER TABLE item_additionals      ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts_receivable   ENABLE ROW LEVEL SECURITY;
ALTER TABLE accounts_payable      ENABLE ROW LEVEL SECURITY;
ALTER TABLE financial_audit       ENABLE ROW LEVEL SECURITY;
ALTER TABLE cash_sessions         ENABLE ROW LEVEL SECURITY;
ALTER TABLE cash_movements        ENABLE ROW LEVEL SECURITY;
ALTER TABLE access_log            ENABLE ROW LEVEL SECURITY;
ALTER TABLE audit_log             ENABLE ROW LEVEL SECURITY;
ALTER TABLE error_log             ENABLE ROW LEVEL SECURITY;

-- FORCE RLS aplica restrições mesmo para o owner da tabela
-- (não afeta superusers, mas é boa prática defensiva)
ALTER TABLE users                 FORCE ROW LEVEL SECURITY;
ALTER TABLE password_reset_tokens FORCE ROW LEVEL SECURITY;
ALTER TABLE refresh_tokens        FORCE ROW LEVEL SECURITY;
ALTER TABLE products              FORCE ROW LEVEL SECURITY;
ALTER TABLE stock_items           FORCE ROW LEVEL SECURITY;
ALTER TABLE stock_movements       FORCE ROW LEVEL SECURITY;
ALTER TABLE stock_alerts          FORCE ROW LEVEL SECURITY;
ALTER TABLE product_recipes       FORCE ROW LEVEL SECURITY;
ALTER TABLE recipe_ingredients    FORCE ROW LEVEL SECURITY;
ALTER TABLE menus                 FORCE ROW LEVEL SECURITY;
ALTER TABLE menu_items            FORCE ROW LEVEL SECURITY;
ALTER TABLE addresses             FORCE ROW LEVEL SECURITY;
ALTER TABLE orders                FORCE ROW LEVEL SECURITY;
ALTER TABLE order_items           FORCE ROW LEVEL SECURITY;
ALTER TABLE item_additionals      FORCE ROW LEVEL SECURITY;
ALTER TABLE accounts_receivable   FORCE ROW LEVEL SECURITY;
ALTER TABLE accounts_payable      FORCE ROW LEVEL SECURITY;
ALTER TABLE financial_audit       FORCE ROW LEVEL SECURITY;
ALTER TABLE cash_sessions         FORCE ROW LEVEL SECURITY;
ALTER TABLE cash_movements        FORCE ROW LEVEL SECURITY;
ALTER TABLE access_log            FORCE ROW LEVEL SECURITY;
ALTER TABLE audit_log             FORCE ROW LEVEL SECURITY;
ALTER TABLE error_log             FORCE ROW LEVEL SECURITY;


-- =============================================================================
-- SEÇÃO 5 — Políticas RLS para 'serveflow_app'
-- =============================================================================
-- Estratégia: PERMISSIVE para serveflow_app, nenhuma política para anon/
-- authenticated (sem política + RLS ativo = acesso negado por padrão).
-- O controle de quem pode fazer o quê é responsabilidade do Spring Boot
-- (JWT + RBAC). O RLS é a última linha de defesa no banco.

-- ── users ─────────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON users;
DROP POLICY IF EXISTS app_insert ON users;
DROP POLICY IF EXISTS app_update ON users;
DROP POLICY IF EXISTS app_delete ON users;

CREATE POLICY app_select ON users FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON users FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON users FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON users FOR DELETE TO serveflow_app USING (true);

-- ── password_reset_tokens ──────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON password_reset_tokens;
DROP POLICY IF EXISTS app_insert ON password_reset_tokens;
DROP POLICY IF EXISTS app_update ON password_reset_tokens;
DROP POLICY IF EXISTS app_delete ON password_reset_tokens;

CREATE POLICY app_select ON password_reset_tokens FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON password_reset_tokens FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON password_reset_tokens FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON password_reset_tokens FOR DELETE TO serveflow_app USING (true);

-- ── refresh_tokens ─────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON refresh_tokens;
DROP POLICY IF EXISTS app_insert ON refresh_tokens;
DROP POLICY IF EXISTS app_update ON refresh_tokens;
DROP POLICY IF EXISTS app_delete ON refresh_tokens;

CREATE POLICY app_select ON refresh_tokens FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON refresh_tokens FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON refresh_tokens FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON refresh_tokens FOR DELETE TO serveflow_app USING (true);

-- ── products ───────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON products;
DROP POLICY IF EXISTS app_insert ON products;
DROP POLICY IF EXISTS app_update ON products;
DROP POLICY IF EXISTS app_delete ON products;

CREATE POLICY app_select ON products FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON products FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON products FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON products FOR DELETE TO serveflow_app USING (true);

-- ── stock_items ────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON stock_items;
DROP POLICY IF EXISTS app_insert ON stock_items;
DROP POLICY IF EXISTS app_update ON stock_items;
DROP POLICY IF EXISTS app_delete ON stock_items;

CREATE POLICY app_select ON stock_items FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON stock_items FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON stock_items FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON stock_items FOR DELETE TO serveflow_app USING (true);

-- ── stock_movements (append-only) ──────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON stock_movements;
DROP POLICY IF EXISTS app_insert ON stock_movements;

CREATE POLICY app_select ON stock_movements FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON stock_movements FOR INSERT TO serveflow_app WITH CHECK (true);

-- ── stock_alerts ───────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON stock_alerts;
DROP POLICY IF EXISTS app_insert ON stock_alerts;
DROP POLICY IF EXISTS app_update ON stock_alerts;
DROP POLICY IF EXISTS app_delete ON stock_alerts;

CREATE POLICY app_select ON stock_alerts FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON stock_alerts FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON stock_alerts FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON stock_alerts FOR DELETE TO serveflow_app USING (true);

-- ── product_recipes ────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON product_recipes;
DROP POLICY IF EXISTS app_insert ON product_recipes;
DROP POLICY IF EXISTS app_update ON product_recipes;
DROP POLICY IF EXISTS app_delete ON product_recipes;

CREATE POLICY app_select ON product_recipes FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON product_recipes FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON product_recipes FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON product_recipes FOR DELETE TO serveflow_app USING (true);

-- ── recipe_ingredients ─────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON recipe_ingredients;
DROP POLICY IF EXISTS app_insert ON recipe_ingredients;
DROP POLICY IF EXISTS app_update ON recipe_ingredients;
DROP POLICY IF EXISTS app_delete ON recipe_ingredients;

CREATE POLICY app_select ON recipe_ingredients FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON recipe_ingredients FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON recipe_ingredients FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON recipe_ingredients FOR DELETE TO serveflow_app USING (true);

-- ── menus ──────────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON menus;
DROP POLICY IF EXISTS app_insert ON menus;
DROP POLICY IF EXISTS app_update ON menus;
DROP POLICY IF EXISTS app_delete ON menus;

CREATE POLICY app_select ON menus FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON menus FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON menus FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON menus FOR DELETE TO serveflow_app USING (true);

-- ── menu_items ─────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON menu_items;
DROP POLICY IF EXISTS app_insert ON menu_items;
DROP POLICY IF EXISTS app_update ON menu_items;
DROP POLICY IF EXISTS app_delete ON menu_items;

CREATE POLICY app_select ON menu_items FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON menu_items FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON menu_items FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON menu_items FOR DELETE TO serveflow_app USING (true);

-- ── addresses ──────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON addresses;
DROP POLICY IF EXISTS app_insert ON addresses;
DROP POLICY IF EXISTS app_update ON addresses;
DROP POLICY IF EXISTS app_delete ON addresses;

CREATE POLICY app_select ON addresses FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON addresses FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON addresses FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON addresses FOR DELETE TO serveflow_app USING (true);

-- ── orders ─────────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON orders;
DROP POLICY IF EXISTS app_insert ON orders;
DROP POLICY IF EXISTS app_update ON orders;
DROP POLICY IF EXISTS app_delete ON orders;

CREATE POLICY app_select ON orders FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON orders FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON orders FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON orders FOR DELETE TO serveflow_app USING (true);

-- ── order_items ────────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON order_items;
DROP POLICY IF EXISTS app_insert ON order_items;
DROP POLICY IF EXISTS app_update ON order_items;
DROP POLICY IF EXISTS app_delete ON order_items;

CREATE POLICY app_select ON order_items FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON order_items FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON order_items FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON order_items FOR DELETE TO serveflow_app USING (true);

-- ── item_additionals ───────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON item_additionals;
DROP POLICY IF EXISTS app_insert ON item_additionals;
DROP POLICY IF EXISTS app_update ON item_additionals;
DROP POLICY IF EXISTS app_delete ON item_additionals;

CREATE POLICY app_select ON item_additionals FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON item_additionals FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON item_additionals FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON item_additionals FOR DELETE TO serveflow_app USING (true);

-- ── accounts_receivable ────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON accounts_receivable;
DROP POLICY IF EXISTS app_insert ON accounts_receivable;
DROP POLICY IF EXISTS app_update ON accounts_receivable;
DROP POLICY IF EXISTS app_delete ON accounts_receivable;

CREATE POLICY app_select ON accounts_receivable FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON accounts_receivable FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON accounts_receivable FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON accounts_receivable FOR DELETE TO serveflow_app USING (true);

-- ── accounts_payable ───────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON accounts_payable;
DROP POLICY IF EXISTS app_insert ON accounts_payable;
DROP POLICY IF EXISTS app_update ON accounts_payable;
DROP POLICY IF EXISTS app_delete ON accounts_payable;

CREATE POLICY app_select ON accounts_payable FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON accounts_payable FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON accounts_payable FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON accounts_payable FOR DELETE TO serveflow_app USING (true);

-- ── cash_sessions ──────────────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON cash_sessions;
DROP POLICY IF EXISTS app_insert ON cash_sessions;
DROP POLICY IF EXISTS app_update ON cash_sessions;
DROP POLICY IF EXISTS app_delete ON cash_sessions;

CREATE POLICY app_select ON cash_sessions FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON cash_sessions FOR INSERT TO serveflow_app WITH CHECK (true);
CREATE POLICY app_update ON cash_sessions FOR UPDATE TO serveflow_app USING (true) WITH CHECK (true);
CREATE POLICY app_delete ON cash_sessions FOR DELETE TO serveflow_app USING (true);

-- ── cash_movements (append-only) ───────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON cash_movements;
DROP POLICY IF EXISTS app_insert ON cash_movements;

CREATE POLICY app_select ON cash_movements FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON cash_movements FOR INSERT TO serveflow_app WITH CHECK (true);

-- ── financial_audit (append-only) ─────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON financial_audit;
DROP POLICY IF EXISTS app_insert ON financial_audit;

CREATE POLICY app_select ON financial_audit FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON financial_audit FOR INSERT TO serveflow_app WITH CHECK (true);

-- ── access_log (append-only) ───────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON access_log;
DROP POLICY IF EXISTS app_insert ON access_log;

CREATE POLICY app_select ON access_log FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON access_log FOR INSERT TO serveflow_app WITH CHECK (true);

-- ── audit_log (append-only) ────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON audit_log;
DROP POLICY IF EXISTS app_insert ON audit_log;

CREATE POLICY app_select ON audit_log FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON audit_log FOR INSERT TO serveflow_app WITH CHECK (true);

-- ── error_log (append-only) ────────────────────────────────────────────────
DROP POLICY IF EXISTS app_select ON error_log;
DROP POLICY IF EXISTS app_insert ON error_log;

CREATE POLICY app_select ON error_log FOR SELECT TO serveflow_app USING (true);
CREATE POLICY app_insert ON error_log FOR INSERT TO serveflow_app WITH CHECK (true);


-- =============================================================================
-- SEÇÃO 6 — Verificação pós-execução
-- =============================================================================
-- Rode estas queries para confirmar que tudo foi aplicado corretamente.

-- 6a) Tabelas com RLS ativo
SELECT
  tablename,
  rowsecurity   AS rls_enabled,
  forcedrowsecurity AS rls_forced
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename   <> 'flyway_schema_history'
ORDER BY tablename;

-- 6b) Políticas criadas
SELECT
  tablename,
  policyname,
  roles,
  cmd        AS operation,
  qual       AS using_expr,
  with_check AS check_expr
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, cmd;

-- 6c) Permissões do role serveflow_app
SELECT
  table_name,
  privilege_type
FROM information_schema.role_table_grants
WHERE grantee = 'serveflow_app'
ORDER BY table_name, privilege_type;

-- 6d) Testar acesso como anon (deve retornar 0 linhas e sem erro de permissão)
-- SET ROLE anon;
-- SELECT count(*) FROM users;   -- esperado: ERROR ou 0 linhas (RLS bloqueou)
-- RESET ROLE;


-- =============================================================================
-- SEÇÃO 7 — Variáveis de ambiente a atualizar no Render
-- =============================================================================
-- Após rodar este script, atualizar no painel do Render (backend):
--
--   POSTGRES_USER     = serveflow_backend
--   POSTGRES_PASSWORD = <senha definida acima>
--
-- A URL de conexão no application-prod.yml já usa ${POSTGRES_USER} e
-- ${POSTGRES_PASSWORD}, portanto nenhuma mudança de código é necessária.
--
-- ATENÇÃO: o Flyway continua conectando como 'postgres' (superuser)
-- para executar migrations — esse comportamento está correto, pois
-- CREATE TABLE, ALTER TABLE etc. requerem privilégios de superuser.
-- O application-prod.yml já separa o data source do Flyway (porta 5432)
-- do connection pool da aplicação (porta 6543), o que é o ideal.
-- =============================================================================
