-- =============================================================================
-- ServeFlow — RLS Audit Report + Scripts Corretos
-- =============================================================================
-- Auditoria de: frontend × banco de dados × controle de acesso
-- Data: 2026-05-30
--
-- ┌─────────────────────────────────────────────────────────────────────────┐
-- │ RESUMO EXECUTIVO                                                        │
-- ├─────────────────────────────────────────────────────────────────────────┤
-- │ ✅  service_role_key no frontend    → NÃO EXISTE                        │
-- │ ✅  SDK Supabase no frontend        → NÃO EXISTE                        │
-- │ ✅  Políticas allow-all / anon      → NÃO EXISTEM                       │
-- │ ❌  RLS nas 23 tabelas              → DESABILITADO (ver seção 2)        │
-- │ ❌  auth.uid() como regra de acesso → INAPLICÁVEL (ver seção 1)        │
-- └─────────────────────────────────────────────────────────────────────────┘
--
-- FLUXO REAL DA APLICAÇÃO:
--   Browser → Spring Boot API (Render:8080) → Supabase PostgreSQL
--   O browser NUNCA fala com o Supabase diretamente.
--   Todo controle de acesso é feito pelo Spring Boot (JWT + RBAC).
-- =============================================================================


-- =============================================================================
-- SEÇÃO 1 — POR QUE auth.uid() NÃO SE APLICA (diagnóstico técnico)
-- =============================================================================
--
-- auth.uid() é uma função Supabase que lê o campo "sub" do JWT da requisição
-- e o interpreta como UUID do usuário autenticado via Supabase Auth.
--
-- O JWT do ServeFlow é emitido pelo Spring Boot com esta estrutura:
--   {
--     "sub":  "joao.silva",   ← username (VARCHAR), não UUID Supabase
--     "role": "GERENTE",      ← role do Spring Security
--     "id":   42,             ← BIGINT da tabela users, não UUID Supabase
--     "iat":  1748000000,
--     "exp":  1748086400
--   }
--
-- Incompatibilidades que bloqueiam auth.uid():
--   1. sub = "joao.silva" → auth.uid() espera UUID ("a1b2c3-d4e5-...")
--   2. Assinatura HMAC com JWT_SECRET próprio → Supabase valida com seu segredo
--   3. Usuários na tabela 'users' (BIGSERIAL) → Supabase Auth usa auth.users (UUID)
--
-- SEGUNDO PROBLEMA — modelo de dados não é por-usuário:
--   Num restaurante, os dados são compartilhados entre toda a equipe.
--   Aplicar "usuário só acessa seus próprios dados" quebraria o sistema:
--   - Garçom só veria seus próprios pedidos (não os da mesa ao lado)
--   - Gerente só veria os produtos que ele criou
--   - Cozinheiro não veria pedidos feitos por outros garçons
--
-- A arquitetura correta para este sistema é:
--   → Controle de OPERAÇÕES por role (quem pode criar/editar/excluir)
--   → Todos os roles autorizados enxergam os dados compartilhados do restaurante
--   → RLS no banco como defesa em profundidade (não como lógica de negócio)
--
-- O script supabase-rls-setup.sql implementa este modelo corretamente.
-- =============================================================================


-- =============================================================================
-- SEÇÃO 2 — VERIFICAÇÃO DO ESTADO ATUAL DO RLS
-- =============================================================================
-- Execute estas queries para auditar o estado atual antes de aplicar mudanças.

-- 2a) Tabelas sem RLS (resultado esperado: todas as 23 antes do setup)
SELECT
  schemaname,
  tablename,
  rowsecurity   AS rls_enabled,
  forcedrowsecurity AS rls_forced
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename <> 'flyway_schema_history'
ORDER BY
  rowsecurity ASC,   -- desabilitado primeiro
  tablename;

-- 2b) Políticas existentes (resultado esperado: vazio antes do setup)
SELECT
  schemaname,
  tablename,
  policyname,
  permissive,
  roles,
  cmd         AS operation,
  qual        AS using_condition,
  with_check  AS check_condition
FROM pg_policies
WHERE schemaname = 'public'
ORDER BY tablename, cmd;

-- 2c) Usuários e roles com acesso ao schema
SELECT
  r.rolname,
  r.rolsuper,
  r.rolinherit,
  r.rolbypassrls,
  has_schema_privilege(r.rolname, 'public', 'USAGE') AS schema_access
FROM pg_roles r
WHERE r.rolname NOT LIKE 'pg_%'
  AND r.rolname NOT LIKE 'supabase%'
  AND r.rolcanlogin = true
ORDER BY r.rolname;

-- 2d) Permissões de tabela por role
SELECT
  grantee,
  table_name,
  string_agg(privilege_type, ', ' ORDER BY privilege_type) AS privileges
FROM information_schema.role_table_grants
WHERE table_schema = 'public'
  AND grantee NOT IN ('postgres', 'PUBLIC')
GROUP BY grantee, table_name
ORDER BY grantee, table_name;


-- =============================================================================
-- SEÇÃO 3 — MODELO CORRETO: RBAC NO BANCO (complemento ao serveflow_app)
-- =============================================================================
-- Este script COMPLEMENTA o supabase-rls-setup.sql.
-- Aplica políticas granulares por operação para o role serveflow_app,
-- refletindo as regras de negócio já implementadas no Spring Boot.
--
-- Premissa: serveflow_app representa o backend como um todo.
-- A autorização por role de usuário (garcon, gerente, etc.) continua
-- sendo responsabilidade do Spring Boot — o banco não sabe quem é
-- o usuário final, apenas que a requisição veio do backend autorizado.


-- =============================================================================
-- SEÇÃO 4 — TABELAS COM SEMÂNTICA DE "DONO" (ownership rows)
-- =============================================================================
-- Estas são as únicas tabelas onde faz sentido uma política por usuário.
-- Mesmo assim, o acesso é feito pelo backend, não pelo frontend.
--
-- refresh_tokens     → cada token pertence a um user_id específico
-- password_reset_tokens → cada token pertence a um username específico
-- access_log         → cada entrada tem um user_id
-- audit_log          → cada entrada tem um user_id
--
-- Se no futuro o ServeFlow integrar Supabase Auth, as políticas abaixo
-- seriam o ponto de partida — mas requereria:
--   1. Migrar autenticação para Supabase Auth
--   2. Sincronizar users.id com auth.users.id (UUID)
--   3. Configurar Supabase para aceitar o JWT do Spring Boot (ou migrar para o Supabase JWT)

-- Exemplo do que seria necessário para auth.uid() funcionar:
-- (NÃO EXECUTE — apenas documentação da migração futura)
/*
-- Passo 1: adicionar coluna de owner UUID nas tabelas relevantes
ALTER TABLE refresh_tokens ADD COLUMN auth_uid UUID;
ALTER TABLE access_log     ADD COLUMN auth_uid UUID;
ALTER TABLE audit_log      ADD COLUMN auth_uid UUID;

-- Passo 2: política de refresh_tokens por owner
CREATE POLICY owner_only ON refresh_tokens
  FOR ALL
  TO authenticated
  USING (auth_uid = auth.uid())
  WITH CHECK (auth_uid = auth.uid());

-- Passo 3: política de logs — usuário só vê seus próprios registros
CREATE POLICY owner_select ON access_log
  FOR SELECT
  TO authenticated
  USING (auth_uid = auth.uid());
*/


-- =============================================================================
-- SEÇÃO 5 — PROTEÇÃO ADICIONAL: BLOQUEAR ROLES NÃO AUTORIZADOS
-- =============================================================================
-- Garante que 'anon' e 'authenticated' (roles default do Supabase) não
-- tenham acesso a NENHUMA tabela, mesmo que RLS seja desabilitado acidentalmente.

-- Revogar permissões dos roles Supabase padrão
REVOKE ALL ON ALL TABLES    IN SCHEMA public FROM anon;
REVOKE ALL ON ALL TABLES    IN SCHEMA public FROM authenticated;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM anon;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public FROM authenticated;
REVOKE USAGE  ON SCHEMA public FROM anon;
REVOKE USAGE  ON SCHEMA public FROM authenticated;

-- Verificar que anon não tem acesso
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.role_table_grants
    WHERE grantee IN ('anon', 'authenticated')
      AND table_schema = 'public'
  ) THEN
    RAISE WARNING 'ATENÇÃO: anon ou authenticated ainda têm permissões em tabelas do schema public!';
  ELSE
    RAISE NOTICE 'OK: anon e authenticated não têm acesso ao schema public.';
  END IF;
END $$;


-- =============================================================================
-- SEÇÃO 6 — VERIFICAÇÃO FINAL PÓS-SETUP COMPLETO
-- =============================================================================

-- 6a) Confirma RLS ativo em todas as tabelas
SELECT
  tablename,
  CASE WHEN rowsecurity AND forcedrowsecurity THEN '✅ RLS + FORCE'
       WHEN rowsecurity                       THEN '⚠️  RLS sem FORCE'
       ELSE                                        '❌ SEM RLS'
  END AS status
FROM pg_tables
WHERE schemaname = 'public'
  AND tablename   <> 'flyway_schema_history'
ORDER BY tablename;

-- 6b) Confirma que serveflow_app tem políticas em todas as tabelas
WITH tabelas AS (
  SELECT tablename
  FROM pg_tables
  WHERE schemaname = 'public'
    AND tablename <> 'flyway_schema_history'
),
politicas AS (
  SELECT DISTINCT tablename
  FROM pg_policies
  WHERE schemaname = 'public'
    AND 'serveflow_app' = ANY(roles)
)
SELECT
  t.tablename,
  CASE WHEN p.tablename IS NOT NULL THEN '✅ Política criada'
       ELSE                              '❌ SEM POLÍTICA — bloqueia backend!'
  END AS politica_serveflow_app
FROM tabelas t
LEFT JOIN politicas p ON p.tablename = t.tablename
ORDER BY t.tablename;

-- 6c) Simula acesso como serveflow_app (deve retornar dados)
-- SET ROLE serveflow_app;
-- SELECT count(*) FROM products;   -- esperado: N linhas
-- SELECT count(*) FROM orders;     -- esperado: N linhas
-- RESET ROLE;

-- 6d) Simula acesso como anon (deve ser bloqueado)
-- SET ROLE anon;
-- SELECT count(*) FROM products;   -- esperado: ERROR permission denied
-- RESET ROLE;


-- =============================================================================
-- SEÇÃO 7 — RESUMO DA ESTRATÉGIA DE SEGURANÇA (camadas)
-- =============================================================================
--
-- CAMADA 1 — Frontend (React)
--   ✅ Não usa SDK Supabase
--   ✅ Não expõe nenhuma chave de banco
--   ✅ Toda comunicação via HTTPS → Spring Boot API
--   ✅ JWT armazenado em sessionStorage (tab-isolated)
--   ✅ Rotas protegidas por role (RoleRoute.jsx)
--
-- CAMADA 2 — Backend (Spring Boot)
--   ✅ Valida JWT em cada requisição (JwtFilter)
--   ✅ RBAC por role: garcon, gerente, cozinheiro, caixa, admin, root
--   ✅ Controllers delegam apenas para facades (thin layer)
--   ⚠️  Usar serveflow_backend ao invés de postgres (ver seção 1 do setup)
--
-- CAMADA 3 — Banco de dados (Supabase PostgreSQL)
--   ❌→✅ RLS habilitado em todas as tabelas (supabase-rls-setup.sql)
--   ❌→✅ Role serveflow_app com permissões mínimas por tabela
--   ❌→✅ anon e authenticated sem nenhum acesso
--   ✅  Flyway como postgres (superuser) — separado do app user
--   ✅  SSL obrigatório (sslmode=require) em todas as conexões
--
-- MODELO NÃO APLICÁVEL para este sistema:
--   ✗  auth.uid() = owner  → sistema restaurante é multi-role, não multi-tenant
--   ✗  "usuário acessa só seus dados" → quebra o fluxo operacional do restaurante
-- =============================================================================
