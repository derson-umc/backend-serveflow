# ServeFlow — Manual do Sistema e Arquitetura

> **Versão:** 2.0  |  **Data:** Maio 2026  |  **Stack:** Spring Boot 3.4.3 / Java 21 / PostgreSQL (Supabase) / React (Vercel) / Render

---

<!--
  [imagem.png]
  Insira aqui o screenshot do diagrama exportado do Excalidraw.
  Arquivo: serveflow-architecture.excalidraw (na raiz do projeto)
-->

---

## 1. Introdução

O ServeFlow é um sistema de gestão para pequenos restaurantes, desenvolvido com foco em praticidade, segurança e escalabilidade. Cobre o ciclo completo de operação: usuários, cardápios, pedidos, estoque, caixa e financeiro — com visualização em tempo real via WebSocket.

Este documento serve como **guia de estudo e referência técnica**: para cada tecnologia e padrão adotado, explica o **motivo da escolha**, o **propósito dentro do sistema** e um **exemplo prático concreto**.

---

## 2. Visão Geral da Arquitetura

```
┌─────────────────────────────────────────────────────────────────────┐
│                     ServeFlow — Fluxo Geral                         │
│  [Usuário / Navegador]                                              │
│         │  HTTPS + JWT Bearer Token                                 │
│  ┌──────────────┐   HTTPS REST    ┌──────────────────────────────┐ │
│  │   Frontend   │ ─────────────► │         Backend API           │ │
│  │   (Vercel)   │ ◄───────────── │         (Render)              │ │
│  │   React SPA  │   WSS (STOMP)  │   Spring Boot 3 / Java 21     │ │
│  └──────────────┘ ◄════════════► └──────────────┬────────────────┘ │
│                   Tempo real                     │ JDBC / JPA       │
│                   (KDS, Caixa)                   ▼                  │
│                                       ┌──────────────────────┐     │
│                                       │   Supabase           │     │
│                                       │   PostgreSQL 15      │     │
│                                       └──────────────────────┘     │
└─────────────────────────────────────────────────────────────────────┘
```

### Infraestrutura

| Camada    | Plataforma   | Tecnologia                        |
|-----------|-------------|-----------------------------------|
| Frontend  | **Vercel**  | React 18, TailwindCSS, Axios      |
| Backend   | **Render**  | Spring Boot 3.4.3, Java 21        |
| Banco     | **Supabase**| PostgreSQL 15, Connection Pooling |
| E-mail    | Gmail SMTP  | STARTTLS :587                     |
| CEP       | ViaCEP API  | REST pública                      |

---

## 3. Frontend — Por que cada tecnologia?

### 3.1 React 18

> **O que é?** Biblioteca JavaScript para construção de interfaces declarativas baseadas em componentes.

**Por que usamos?**
React resolve o problema de manter a interface sincronizada com os dados sem recarregar a página inteira. Em um sistema de restaurante, dados mudam constantemente (status de pedidos, estoque, caixa) — React re-renderiza apenas os componentes afetados.

**Para que serve no ServeFlow?**
Cada tela do sistema (login, pedidos, KDS, caixa, estoque, financeiro) é um conjunto de componentes React reutilizáveis. Quando o status de um pedido muda, apenas o card daquele pedido é atualizado na tela.

**Exemplo prático:**
O garçom seleciona um produto e a quantidade total do pedido atualiza instantaneamente. Sem React, seria necessário recarregar a página inteira ou escrever manipulação manual do DOM — propenso a bugs e lento.

**Onde encontrar:** Camada `src/` do projeto frontend.

---

### 3.2 React Router DOM

> **O que é?** Biblioteca de roteamento client-side para React — gerencia URLs sem recarregar o servidor.

**Por que usamos?**
Em uma SPA (Single Page Application), a navegação entre telas não pode usar reload de página completo — isso seria lento e perderia o estado da aplicação. O React Router DOM intercepta cliques em links e troca apenas o componente exibido.

**Para que serve no ServeFlow?**
Gerencia as rotas protegidas por perfil: `/orders` (garçom), `/kds` (cozinheiro), `/cashier` (caixa), `/stock` (gerente). Impede que um garçom acesse `/financial` diretamente pela URL.

**Exemplo prático:**
O gerente clica em "Estoque" no menu e a URL muda para `/stock/consolidated`. Nenhuma requisição é feita ao servidor para essa navegação — o browser apenas troca o componente React exibido. Rápido como trocar de aba.

---

### 3.3 TailwindCSS

> **O que é?** Framework CSS utility-first — estilização direto no HTML/JSX com classes padronizadas.

**Por que usamos?**
CSS tradicional exige criar arquivos `.css` separados para cada componente, gerando duplicação e inconsistências. TailwindCSS centraliza os estilos em um sistema de design tokens (cores, espaçamentos, tipografia) aplicados como classes.

**Para que serve no ServeFlow?**
Garante design responsivo consistente: o mesmo sistema funciona bem em tablets na cozinha (KDS em tela de 10"), celulares dos garçons e desktop do gerente. Classes como `md:grid-cols-2 lg:grid-cols-4` adaptam o layout automaticamente.

**Exemplo prático:**
```jsx
<button className="bg-green-500 hover:bg-green-600 text-white
                   px-6 py-3 rounded-lg font-semibold
                   transition-colors duration-200">
  Confirmar Pedido
</button>
```
Sem escrever um único arquivo `.css`, o botão tem cor, hover, padding, bordas arredondadas e animação.

---

### 3.4 Axios (com Interceptors)

> **O que é?** Cliente HTTP para JavaScript com suporte a promises e configuração global via interceptors.

**Por que usamos?**
O `fetch` nativo do browser não tem interceptors — código que roda automaticamente antes/depois de cada requisição. O Axios permite centralizar o header de autorização e o tratamento de token expirado em um único lugar.

**Para que serve no ServeFlow?**
O interceptor de response detecta erros 401 (token expirado), chama automaticamente `POST /api/auth/refresh`, obtém novo accessToken e **reenvia a requisição original** — completamente transparente para o usuário.

**Exemplo prático:**
```javascript
// Configuração uma única vez:
axios.interceptors.response.use(
  response => response,
  async error => {
    if (error.response.status === 401) {
      const newToken = await refreshToken(); // chama /auth/refresh
      error.config.headers.Authorization = `Bearer ${newToken}`;
      return axios(error.config); // reexecuta a requisição
    }
    return Promise.reject(error);
  }
);
```
O gerente está consultando o financeiro quando o token de 15min expira. O Axios renova automaticamente — sem pedir novo login.

---

### 3.5 Zustand / Context API

> **O que é?** Zustand é uma biblioteca leve de estado global para React; Context API é o mecanismo nativo do React para compartilhar dados.

**Por que usamos?**
Em React, dados passados de pai para filho via props se tornam inviáveis quando muitos níveis de componentes precisam do mesmo dado (prop drilling). Zustand resolve com uma store global simples.

**Para que serve no ServeFlow?**
Armazena o perfil do usuário logado (`name`, `role`, `token`) acessível por qualquer componente sem passar por props. Também centraliza o estado do cardápio ativo, evitando múltiplas requisições à API.

**Exemplo prático:**
```javascript
// Qualquer componente acessa diretamente:
const { user } = useAuthStore();
// Se user.role !== 'ADMIN', oculta o botão de configurações
```
O menu lateral mostra/oculta itens baseado no role sem receber essa informação via props de 4 componentes pai.

---

## 4. Backend — Por que cada tecnologia e padrão?

### 4.1 Spring Boot 3.4.3

> **O que é?** Framework Java que configura automaticamente todo o ecossistema Spring — servidor embutido, JPA, segurança, WebSocket — com mínimo de boilerplate.

**Por que usamos?**
Spring Boot elimina o problema de "configuration hell" do Spring tradicional. Com `@SpringBootApplication`, o framework detecta as dependências e configura tudo automaticamente (convention over configuration).

**Para que serve no ServeFlow?**
É a base de toda a API REST, gerenciando o ciclo de vida dos beans, injeção de dependências, conexão com banco, filtros de segurança e servidores de WebSocket — sem XML de configuração.

**Exemplo prático:**
```yaml
# application.yml — basta isso para conectar ao banco:
spring:
  datasource:
    url: jdbc:postgresql://host:5432/serveflow_db
    username: postgres
    password: ${DB_PASSWORD}
```
Sem Spring Boot, seriam necessárias configurações de `DataSource`, `EntityManagerFactory`, `TransactionManager` manualmente. Com Spring Boot: zero.

**Onde encontrar:** `ServeflowApplication.java` (classe principal com `@SpringBootApplication`).

---

### 4.2 Clean Architecture

> **O que é?** Padrão arquitetural que separa regras de negócio (interno) dos detalhes de implementação (externo: HTTP, banco, frameworks).

**Por que usamos?**
Sem separação de camadas, o código de negócio fica misturado com código de infraestrutura — impossível testar, difícil de manter. Clean Architecture garante que a lógica de negócio não depende de frameworks.

**Para que serve no ServeFlow?**
`OrderService` não sabe que foi chamado via HTTP. Não importa `HttpServletRequest`. Poderia ser chamado por um teste unitário, CLI ou mensageria — a lógica é idêntica.

**Exemplo prático:**
```
Camada externa (Infraestrutura):
  OrderController  →  recebe HTTP, chama OrderService
  OrderEntity      →  mapeamento JPA, conhece banco

Camada interna (Domínio):
  Order            →  regras de negócio puras
  OrderService     →  orquestra casos de uso
  OrderRepository  →  interface (não implementação!)
```
Se migrarmos de PostgreSQL para MongoDB, apenas `OrderEntity` e sua implementação mudam. `OrderService`, `Order` e `OrderController` **não são tocados**.

---

### 4.3 DDD — Domain-Driven Design

> **O que é?** Abordagem de desenvolvimento onde o código reflete o vocabulário e as regras do domínio de negócio (neste caso, um restaurante).

**Por que usamos?**
Sem DDD, o código fica orientado a tabelas de banco (`UserTable`, `OrderRow`) em vez de conceitos de negócio (`User`, `Order`). DDD melhora a comunicação entre devs e facilita entender regras de negócio no próprio código.

**Para que serve no ServeFlow?**
Classes como `Order`, `CashSession`, `StockMovement`, `MenuShift` refletem exatamente o vocabulário de um restaurante. As regras ficam encapsuladas nos próprios objetos de domínio.

**Exemplo prático:**
```java
// Com DDD — a regra "só confirma se CREATED" vive no domínio:
public class Order {
    public void confirm() {
        if (this.status != OrderStatus.CREATED) {
            throw new BusinessRuleException("Pedido já processado");
        }
        this.status = OrderStatus.CONFIRMED;
    }
}

// Sem DDD — a regra estaria espalhada em 3 lugares diferentes:
if (order.getStatus().equals("CREATED")) { ... } // no controller
if (status == 0) { ... }                          // no service
```

---

### 4.4 JWT — JSON Web Token

> **O que é?** Padrão de token compacto e autocontido que representa claims (afirmações) sobre um usuário, assinado criptograficamente.

**Por que usamos?**
Autenticação tradicional baseada em sessão exige que o servidor armazene e consulte sessões a cada requisição — não escala horizontalmente. JWT é **stateless**: o token carrega os dados do usuário e é validado por criptografia, sem consultar banco de dados.

**Para que serve no ServeFlow?**
Cada requisição carrega o token no header `Authorization: Bearer <token>`. O `JwtFilter` valida a assinatura, extrai `userId`, `username` e `role` — tudo sem tocar na tabela `users`.

**Exemplo prático:**
```
Header:  eyJhbGciOiJIUzI1NiJ9
Payload: eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiam9hbyIsInJvbGUiOiJHRVJFTlRFIn0
         → decodificado: {"userId":1,"username":"joao","role":"GERENTE","exp":1748295600}
Assinatura: validada com JWT_SECRET (HMAC-SHA256)
```
O backend sabe que é João, gerente, sem consultar o banco. A assinatura garante que o token não foi adulterado.

**Onde encontrar:** `config/JwtService.java`

---

### 4.5 BCrypt Password Encoder

> **O que é?** Algoritmo de hash adaptativo para senhas com salt automático, projetado especificamente para ser lento e resistente a ataques de força bruta.

**Por que usamos?**
MD5 e SHA-1 são rápidos — um atacante pode testar bilhões de senhas por segundo. BCrypt é intencionalmente lento (configurável) e adiciona um salt único por senha, tornando ataques de dicionário inviáveis.

**Para que serve no ServeFlow?**
A senha do usuário nunca é armazenada em texto plano. O `UserService` usa `passwordEncoder.encode(rawPassword)` e valida com `passwordEncoder.matches(rawPassword, storedHash)`.

**Exemplo prático:**
```
Senha original:  "admin123"
Hash BCrypt:     "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lih57"

Dois usuários com a mesma senha "admin123" terão hashes DIFERENTES
(por causa do salt aleatório embutido no hash).
Mesmo que o banco seja comprometido, as senhas são irrecuperáveis.
```
**Onde encontrar:** `config/SecurityConfig.java` → `passwordEncoder()` bean.

---

### 4.6 JwtFilter — OncePerRequestFilter

> **O que é?** Filtro Spring Security que executa exatamente uma vez por requisição HTTP, validando o token JWT antes de qualquer controller.

**Por que usamos?**
Precisamos que **toda** requisição autenticada seja validada antes de chegar ao controller. `OncePerRequestFilter` garante isso mesmo em chains de filtros complexas (sem dupla execução em forwards).

**Para que serve no ServeFlow?**
Intercepta toda requisição, extrai o Bearer token do header `Authorization`, valida com `JwtService`, popula o `SecurityContextHolder` com o usuário autenticado para o Spring Security usar no RBAC.

**Exemplo prático:**
```
GET /api/stock/items
  Authorization: Bearer eyJ...

JwtFilter executa:
  1. Extrai "eyJ..." do header
  2. JwtService.validateToken() → OK
  3. Extrai username="joao", role="GERENTE"
  4. SecurityContextHolder.setAuthentication(...)

Spring Security verifica:
  /stock/** requer GERENTE ou ADMIN → OK → chega no StockController
```
**Onde encontrar:** `config/JwtFilter.java`

---

### 4.7 RateLimitFilter

> **O que é?** Filtro que limita o número de requisições por IP em um intervalo de tempo.

**Por que usamos?**
Sem rate limiting, um atacante pode tentar milhares de combinações de senha por minuto (brute force) ou derrubar o servidor com excesso de requisições (DoS).

**Para que serve no ServeFlow?**
Protege especialmente o endpoint `/api/auth/login`. Após N tentativas do mesmo IP, retorna `429 Too Many Requests` e bloqueia temporariamente.

**Exemplo prático:**
```
IP 192.168.1.100 tenta login 50x em 1 minuto:
  Tentativas 1-10:  200 OK (senha errada → 401)
  Tentativa 11+:    429 Too Many Requests
  Após 5 minutos:   bloqueio removido
```
**Onde encontrar:** `config/RateLimitFilter.java`

---

### 4.8 CORS — Cross-Origin Resource Sharing

> **O que é?** Mecanismo de segurança dos browsers que bloqueia requisições HTTP entre domínios diferentes, a menos que o servidor explicitamente autorize.

**Por que usamos?**
Por padrão, o browser bloqueia chamadas de `serveflow.vercel.app` para `serveflow.onrender.com` porque são domínios diferentes (cross-origin). A `SecurityConfig` configura quais origens são permitidas.

**Para que serve no ServeFlow?**
Permite que o React (Vercel) chame a API (Render). Em desenvolvimento local, permite `http://localhost:3000` e `http://localhost:5173` chamar `http://localhost:8080`.

**Exemplo prático:**
```
SEM CORS configurado:
  Browser: "Access to XMLHttpRequest at 'https://serveflow.onrender.com/api/auth/login'
            from origin 'https://serveflow.vercel.app' has been blocked by CORS policy"

COM CORS configurado (CORS_ALLOWED_ORIGINS=https://serveflow.vercel.app):
  Servidor responde com header:
  Access-Control-Allow-Origin: https://serveflow.vercel.app
  → Browser permite a chamada
```
**Onde encontrar:** `config/SecurityConfig.java` → `corsConfigurationSource()`.

---

### 4.9 @Async — Execução Assíncrona

> **O que é?** Anotação Spring que executa um método em uma thread separada do pool de threads, sem bloquear a thread principal.

**Por que usamos?**
Operações lentas (gravar logs no banco, enviar e-mail) não devem atrasar a resposta ao usuário. Com `@Async`, essas operações rodam em paralelo.

**Para que serve no ServeFlow?**
O `AuditService` grava logs de acesso e auditoria sem atrasar o response. O usuário recebe `200 OK` imediatamente enquanto o log é persistido em outra thread.

**Exemplo prático:**
```
SEM @Async — tempo total = negócio + log:
  OrderService.complete()  →  50ms
  AuditService.logAction() →  +200ms (INSERT no banco)
  Response ao usuário      =  250ms

COM @Async — tempo total = apenas negócio:
  OrderService.complete()  →  50ms  → Response ao usuário: 50ms
  AuditService.logAction() →  200ms (em outra thread, em paralelo)
```
**Onde encontrar:** `config/AsyncConfig.java` + `service/audit/AuditService.java`

---

### 4.10 Spring Events — @EventListener

> **O que é?** Mecanismo de eventos do Spring que permite que um componente publique um evento e outros componentes reajam sem se conhecerem diretamente.

**Por que usamos?**
Sem eventos, `OrderService` teria que chamar diretamente `CashierService.registerMovement()` e `KdsEventPublisher.notify()` — acoplamento forte. Se amanhã quisermos enviar SMS ao cliente, teríamos que editar `OrderService`.

**Para que serve no ServeFlow?**
Quando um pedido é concluído, `OrderService` publica `OrderCompletedEvent`. `CashierEventListener` escuta e cria o movimento de caixa automaticamente — sem `OrderService` saber que existe um `CashierService`.

**Exemplo prático:**
```java
// OrderService apenas publica — não sabe quem vai escutar:
applicationEventPublisher.publishEvent(
    new OrderCompletedEvent(order.getId(), order.getTotal())
);

// CashierEventListener reage de forma independente:
@EventListener
@Async
public void onOrderCompleted(OrderCompletedEvent event) {
    cashierService.registerMovement(event.getOrderId(), event.getTotal());
}

// Para adicionar notificação SMS: cria novo listener, não toca em OrderService:
@EventListener
public void onOrderCompleted(OrderCompletedEvent event) {
    smsService.notifyCustomer(event);
}
```
**Onde encontrar:** `events/OrderCompletedEvent.java` + `service/cashier/CashierEventListener.java`

---

### 4.11 @Scheduled — Tarefas Agendadas

> **O que é?** Anotação Spring que agenda métodos para execução automática em intervalos definidos por expressão cron.

**Por que usamos?**
Manutenção periódica (limpeza de logs antigos, verificação de alertas de estoque) não deve ser disparada manualmente. `@Scheduled` garante execução automática sem intervenção humana.

**Para que serve no ServeFlow?**
`AuditLogCleanupJob` apaga registros de `access_log`, `audit_log` e `error_log` com mais de `AUDIT_RETENTION_DAYS` dias — evitando que as tabelas de log cresçam indefinidamente.

**Exemplo prático:**
```java
@Scheduled(cron = "0 0 2 * * ?")  // Todo dia às 02:00
public void cleanupOldLogs() {
    auditService.deleteLogsOlderThan(retentionDays);
    // Apaga: SELECT * FROM access_log WHERE timestamp < NOW() - INTERVAL '90 days'
}
```
Sem `@Scheduled`, seria necessário cron job externo ou trigger manual. Com Spring, tudo dentro do próprio aplicativo.

**Onde encontrar:** `service/audit/AuditLogCleanupJob.java`

---

### 4.12 Padrão Repository

> **O que é?** Padrão de design que abstrai o acesso a dados atrás de uma interface — o service usa a interface, não a implementação concreta.

**Por que usamos?**
Sem o padrão Repository, o service importaria `JpaRepository` ou `EntityManager` diretamente — acoplamento entre lógica de negócio e infraestrutura de banco. Impossível testar sem banco.

**Para que serve no ServeFlow?**
`UserService` chama `userRepository.findByEmail(email)` — uma interface de domínio. A implementação real (`SpringUserRepository extends JpaRepository`) fica isolada. O service nem sabe que é JPA.

**Exemplo prático:**
```java
// Interface de domínio (UserRepository.java) — sem dependência de framework:
public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
}

// Implementação com Spring Data JPA (SpringUserRepository.java):
@Repository
public interface SpringUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}

// Para testes unitários — implementação em memória:
public class InMemoryUserRepository implements UserRepository {
    private Map<String, User> store = new HashMap<>();
    // ... testa UserService sem banco de dados
}
```
**Onde encontrar:** `repository/user/UserRepository.java` + `repository/user/SpringUserRepository.java`

---

### 4.13 Custom Repository Impl

> **O que é?** Implementação manual de queries complexas quando o Spring Data JPA não consegue gerar automaticamente pelo nome do método.

**Por que usamos?**
Spring Data JPA gera queries simples pelo nome do método (`findByStatus`, `findByItemAndType`). Para filtros opcionais múltiplos + paginação + joins específicos, o nome do método se tornaria absurdo ou impossível.

**Para que serve no ServeFlow?**
`StockMovementRepositoryImpl` implementa `findMovements(itemId, type, startDate, endDate, page, size)` com todos os parâmetros opcionais — usando `CriteriaBuilder` ou JPQL dinâmico.

**Exemplo prático:**
```java
// Com Spring Data: impossível para parâmetros opcionais combinados
findByStockItemIdAndTypeAndTransactionDateBetween(...) // nome absurdo

// Com Custom Impl — JPQL dinâmico:
public Page<StockMovement> findMovements(UUID itemId, MovementType type,
                                          LocalDate start, LocalDate end,
                                          Pageable pageable) {
    // monta query dinamicamente com só os filtros não-nulos
}
```
**Onde encontrar:** `repository/stock/StockMovement/StockMovementRepositoryImpl.java`

---

### 4.14 GlobalExceptionHandler — @RestControllerAdvice

> **O que é?** Classe que captura exceções lançadas em qualquer controller e retorna respostas HTTP padronizadas.

**Por que usamos?**
Sem handler global, exceções não tratadas retornam stack traces em HTML para o cliente — péssima UX e exposição de informações internas. Com `@RestControllerAdvice`, todas as exceções viram respostas JSON padronizadas.

**Para que serve no ServeFlow?**
Centraliza o mapeamento de exceções de domínio para códigos HTTP. `UserNotFoundException` → 404, `BusinessRuleException` → 422, `MethodArgumentNotValidException` → 400 com lista de campos inválidos.

**Exemplo prático:**
```
POST /api/users  (com e-mail já cadastrado)
↓
ConflictException lançada no UserService
↓
GlobalExceptionHandler captura:
{
  "timestamp": "2026-05-26T14:32:10",
  "status": 409,
  "error": "E-mail já cadastrado no sistema",
  "fields": null
}
```
**Onde encontrar:** `exception/handler/GlobalExceptionHandler.java`

---

### 4.15 DTO — Data Transfer Objects

> **O que é?** Objetos simples usados para transferir dados entre camadas — separando o que entra (Input) do que sai (Output) e das entidades de banco.

**Por que usamos?**
Expor entidades JPA diretamente na API seria um problema: exporia campos internos (senha hash, versão do Hibernate), dificultaria validação e acoplaria API ao banco. DTOs definem contratos claros.

**Para que serve no ServeFlow?**
`UserInput` (POST /users) valida e-mail com `@Email`, senha com `@NotBlank`. `UserOutput` (resposta) omite `passwordHash` e inclui apenas dados seguros para retornar ao cliente.

**Exemplo prático:**
```java
// UserInput — o que o cliente ENVIA:
public record UserInput(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank @Size(min=8) String password,
    @NotNull UserRole role
) {}

// UserOutput — o que o sistema RETORNA (sem senha!):
public record UserOutput(
    Long id, String name, String email, UserRole role, boolean active
) {}

// UserEntity — o que o banco ARMAZENA (nunca exposto):
@Entity @Table(name="users")
public class UserEntity {
    String passwordHash; // nunca retornado na API
    Integer version;     // controle de concorrência do Hibernate
}
```
**Onde encontrar:** `dto/user/UserInput.java`, `dto/user/UserOutput.java`

---

### 4.16 WebSocket + STOMP

> **O que é?** WebSocket é um protocolo de comunicação bidirecional e persistente. STOMP (Simple Text Oriented Messaging Protocol) é um protocolo de mensagens sobre WebSocket com suporte a tópicos e subscriptions.

**Por que usamos?**
HTTP é request/response — o cliente sempre inicia. Para o KDS receber novos pedidos instantaneamente e o caixa ver movimentos em tempo real, precisamos de **server push**: o servidor envia dados sem o cliente perguntar.

**Para que serve no ServeFlow?**
- Cozinheiro no KDS vê novos pedidos em ~100ms sem recarregar a página
- Painel do caixa atualiza ao concluir pedidos automaticamente
- Múltiplos tablets na cozinha recebem o mesmo pedido simultaneamente

**Exemplo prático:**
```
SEM WebSocket (polling a cada 2s):
  Cozinheiro recarrega KDS manualmente OU
  Sistema faz GET /kds/orders a cada 2 segundos → 30 requisições/minuto

COM WebSocket STOMP:
  Garçom confirma pedido
  → OrderService publica OrderEvent
  → KdsEventPublisher envia para /topic/kds/orders
  → Todos os tablets conectados recebem em ~100ms automaticamente
  → Zero requisições de polling

Conexão: wss://serveflow.onrender.com/api/ws
Tópicos: /topic/kds/orders
         /topic/cashier/movements
         /topic/cashier/sessions
```
**Onde encontrar:** `config/WebSocketConfig.java`, `controller/kds/KdsEventPublisher.java`

---

## 5. Banco de Dados — Por que cada decisão?

### 5.1 PostgreSQL

> **O que é?** Sistema de gerenciamento de banco de dados relacional open source, robusto e rico em recursos.

**Por que usamos?**
O ServeFlow tem relacionamentos complexos: pedido → itens → adicionais → produtos → ingredientes → receitas → estoque. Um banco relacional com JOINs é a escolha natural. PostgreSQL adiciona tipos avançados (UUID nativo, ENUM, JSONB) e é melhor suportado pelo Supabase.

**Exemplo prático:**
```sql
-- Relatório financeiro que cruza 4 tabelas:
SELECT o.id, SUM(oi.total_price) as total,
       cs.opening_balance, cm.value
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN cash_movements cm ON cm.order_id = o.id
JOIN cash_sessions cs ON cs.id = cm.session_id
WHERE o.status = 'COMPLETED'
  AND DATE(o.created_at) = CURRENT_DATE
GROUP BY o.id, cs.opening_balance, cm.value;
```

---

### 5.2 Flyway — Controle de Versão do Banco

> **O que é?** Ferramenta de migração de banco de dados que aplica scripts SQL versionados em ordem, uma única vez, garantindo consistência entre ambientes.

**Por que usamos?**
Sem Flyway, cada dev teria um banco diferente e o deploy em produção exigiria executar SQL manualmente — arriscado e difícil de rastrear. Flyway é o "Git do banco de dados".

**Para que serve no ServeFlow?**
26 migrations (`V1__create_users.sql` → `V26__add_origem_to_cash_movements.sql`) são executadas automaticamente no startup. O Flyway verifica quais já foram aplicadas e executa apenas as novas.

**Exemplo prático:**
```
Desenvolvedor cria V27__add_table_number_to_orders.sql
└── ALTER TABLE orders ADD COLUMN table_number INTEGER;

Deploy em staging:
  Flyway: "V27 não aplicada → executando..."
  → ALTER TABLE aplicado automaticamente

Deploy em produção:
  Flyway: "V27 não aplicada → executando..."
  → Mesma alteração, sem intervenção manual
  → Ambientes sempre sincronizados
```
**Onde encontrar:** `src/main/resources/db/migration/`

---

### 5.3 UUID como Primary Key

> **O que é?** Identificador Universal Único — string de 36 caracteres gerada aleatoriamente, globalmente única.

**Por que usamos?**
IDs sequenciais (1, 2, 3...) expõem informações: um usuário pode tentar acessar `/orders/1`, `/orders/2`... e enumerar todos os pedidos. UUIDs são imprevisíveis. Também permitem gerar IDs na aplicação antes do INSERT.

**Exemplo prático:**
```
ID sequencial: GET /api/orders/1, /api/orders/2, /api/orders/3
  → Atacante consegue listar todos os pedidos facilmente

UUID: GET /api/orders/550e8400-e29b-41d4-a716-446655440000
  → Próximo UUID impossível de prever → 2^122 possibilidades
```

---

### 5.4 Supabase — PostgreSQL Gerenciado

> **O que é?** Plataforma open source que provê PostgreSQL como serviço com Connection Pooling, Auth, Storage e Real-time integrados.

**Por que usamos?**
Configurar PostgreSQL do zero exigiria: VPS, instalação, configuração de SSL, backup, monitoramento, pgBouncer... Supabase oferece tudo isso no free tier com uma linha de connection string.

**Recursos utilizados no ServeFlow:**

| Recurso | Por que usamos? | Benefício prático |
|---------|-----------------|-------------------|
| **Connection Pooling (pgBouncer)** | Cada conexão TCP ao PostgreSQL consome ~10MB RAM | 10 conexões no pool servem 100 usuários simultâneos |
| **SSL/TLS automático** | Dados em trânsito criptografados | Nenhuma configuração manual de certificado |
| **Backups automáticos** | Recuperação de desastres | Restaurar banco em caso de falha crítica |
| **PITR (Point-in-time Recovery)** | Backups são snapshots; PITR restaura para qualquer segundo | Dev apaga tabela → restaura estado de 1 minuto atrás |
| **Row Level Security** | Segurança a nível de linha no banco | Mesmo com bug no app, banco protege dados |
| **Dashboard SQL Editor** | Queries ad-hoc sem cliente externo | Gerente consulta dados direto pelo browser |

**Exemplo de PITR:**
```
15:34:00 - DBA executa: DELETE FROM orders; (sem WHERE!)
           → 0 pedidos no banco

COM PITR:
  Restaurar para 15:33:58
  → Todos os pedidos recuperados
  → Prejuízo: 2 segundos de dados, não o dia inteiro
```

---

### 5.5 Render — Backend Hosting

> **O que é?** Plataforma PaaS (Platform as a Service) para hospedar aplicações com deploy automático via Git.

**Por que usamos?**
Sem PaaS, seria necessário configurar VPS, instalar Java, configurar nginx como proxy reverso, configurar SSL, monitorar e fazer deploy manualmente. Render detecta `pom.xml` e faz tudo automaticamente.

**Exemplo prático:**
```
git push origin main
  → Render detecta mudança no GitHub
  → Executa: mvn clean package -DskipTests
  → Cria novo container com o JAR
  → Health check: GET /api/actuator/health → 200 OK
  → Troca para nova versão (zero downtime)
  → Deploy em ~3 minutos
```

---

### 5.6 Vercel — Frontend Hosting

> **O que é?** CDN e plataforma de hosting especializada em aplicações frontend JavaScript, com deploy automático e Preview Deployments.

**Por que usamos?**
Vercel é otimizado para React: compila com Vite/Webpack, distribui globalmente via CDN, gera HTTPS automático e cria ambientes de preview para cada Pull Request.

**Exemplo prático:**
```
Dev abre PR com nova feature "Filtro de Pedidos"
  → Vercel gera: https://serveflow-pr-42.vercel.app
  → Gerente testa a feature no próprio link de preview
  → PR aprovado → merge → deploy automático em produção
```

---

## 6. Guia do Usuário — Como Usar o Sistema

### 6.1 Primeiro Acesso — Login

1. Acesse o sistema pelo endereço fornecido pelo administrador
2. Informe seu **e-mail** e **senha** → clique em **Entrar**
3. O sistema identifica seu perfil automaticamente

> **Esqueceu a senha?** Clique em "Esqueci minha senha" → informe o e-mail → aguarde o link na caixa de entrada.

---

### 6.2 Perfil ADMIN — Administrador

**Cadastrar usuário:**
1. Menu **Usuários** → **Novo Usuário**
2. Nome, e-mail, senha e perfil (`ADMIN`, `GERENTE`, `GARCOM`, `COZINHEIRO`, `CAIXA`)
3. **Salvar** → comunique credenciais ao colaborador

**Alterar cargo:** Usuários → colaborador → **Alterar Cargo** → selecionar → confirmar

**Desativar usuário:** Usuários → colaborador → **Desativar** → colaborador perde acesso imediatamente

---

### 6.3 Perfil GERENTE — Gestão do Restaurante

**Cadastrar produto:**
1. **Produtos** → **Novo Produto** → nome, preço, tipo (`PRODUTO` ou `INGREDIENTE`)
2. Upload de imagem opcional (PNG/JPG, máx. 8MB)
3. Marcar **"Requer ficha técnica"** se o produto consome ingredientes do estoque

**Criar cardápio:**
1. **Cardápios** → **Novo Cardápio** → nome + turno (`ALMOÇO`, `JANTAR`, etc.)
2. Adicionar produtos → preço personalizado por item (opcional)
3. Configurar agendamento automático (opcional)

**Controlar estoque:**
- **Entrada:** Estoque → item → **Registrar Entrada** → quantidade + motivo
- **Perda:** item → **Registrar Perda** → quantidade + motivo
- **Ajuste:** item → **Ajuste** → nova quantidade real (após inventário físico)

**Ficha Técnica (Receita):**
1. **Receitas** → **Nova Receita** → selecionar produto
2. Adicionar ingredientes com quantidades por porção
3. A partir de então, cada venda debita automaticamente os ingredientes do estoque

**Módulo Financeiro:**
- **Contas a pagar/receber:** lançamento → descrição + valor + vencimento
- **Liquidar:** localizar conta → **Liquidar** → confirmar pagamento
- **Fluxo de caixa:** Financeiro → **Fluxo de Caixa** → selecionar período

---

### 6.4 Perfil GARÇOM — Registro de Pedidos

**Novo pedido:**
1. **Pedidos** → **Novo Pedido** → tipo: `MESA`, `BALCÃO` ou `DELIVERY`
2. CEP (delivery): sistema preenche endereço automaticamente via ViaCEP
3. Selecionar itens + quantidades + adicionais (ex.: "sem cebola")
4. Forma de pagamento → **Confirmar Pedido**

**Status dos pedidos:**

| Status | Ação do garçom |
|--------|---------------|
| `CRIADO` | Aguardar confirmação |
| `CONFIRMADO` | Pedido na fila da cozinha |
| `PREPARANDO` | Cozinha em preparo |
| `PRONTO` | Buscar na cozinha → clicar **Entregar** |
| `ENVIADO` | Aguardar liquidação no caixa |
| `CONCLUÍDO` | Finalizado e pago |

---

### 6.5 Perfil COZINHEIRO — KDS

A tela do KDS atualiza automaticamente via WebSocket — sem necessidade de recarregar.

**Gerenciar fila:**
1. Pedido `CONFIRMADO` aparece → clicar **Iniciar Preparo** → status: `PREPARANDO`
2. Preparo concluído → clicar **Pronto** → garçom é notificado

**Controlar disponibilidade:** Cardápio → item esgotado → desativar disponibilidade → garçom não consegue mais pedir.

> **Dica:** Mantenha o KDS em uma tela dedicada sempre visível na cozinha.

---

### 6.6 Perfil CAIXA — Controle de Caixa

**Abrir caixa** (início do turno):
1. **Caixa** → **Abrir Caixa** → informar saldo inicial → confirmar
> Só uma sessão aberta por vez. Feche o turno anterior antes de abrir novo.

**Liquidar pedido:**
1. **Caixa** → **Pedidos Pendentes** → localizar pedido → **Liquidar**
2. Confirmar valor recebido e forma de pagamento

**Movimentos manuais** (sangria, troco, retirada):
1. **Caixa** → **Novo Movimento** → Entrada ou Saída → valor + descrição

**Fechar caixa** (fim do turno):
1. **Caixa** → **Fechar Caixa** → revisar resumo → informar saldo contado → **Confirmar Fechamento**

---

### 6.7 Operações Comuns

**Alterar senha:** Avatar/nome → Minha Conta → Alterar Senha → senha atual + nova senha

**Recuperar senha:** Tela de login → "Esqueci minha senha" → e-mail → link chega em minutos

**Sair:** Avatar/nome → **Sair** (token invalidado com segurança)

---

### 6.8 Resolução de Problemas

| Situação | Solução |
|----------|---------|
| Item não aparece no cardápio | Produto deve estar ativo + item marcado disponível no cardápio |
| Pedido travado em CONFIRMADO | Cozinheiro deve clicar "Iniciar Preparo" no KDS |
| Estoque caindo sem vendas manuais | Verificar ficha técnica — pode haver desconto automático por receita |
| Não consigo abrir o caixa | Sessão anterior deve ser fechada primeiro |
| E-mail de reset não chegou | Verificar spam; se persistir, confirmar e-mail com administrador |
| KDS não atualiza | Verificar conexão com internet — WebSocket requer conexão ativa |

---

## 7. Infraestrutura e Deploy

### 7.1 Variáveis de Ambiente

| Variável | Descrição | Padrão (dev) |
|----------|-----------|--------------|
| `SPRING_PROFILES_ACTIVE` | Perfil ativo | `dev` |
| `DATABASE_URL` | URL JDBC do PostgreSQL | — |
| `JWT_SECRET` | Chave HMAC-SHA256 (mín. 256 bits) | obrigatório |
| `JWT_EXPIRATION` | Expiração do access token (ms) | `900000` (15min) |
| `CORS_ALLOWED_ORIGINS` | Origins permitidas | `http://localhost:3000` |
| `MAIL_HOST` | Servidor SMTP | `smtp.gmail.com` |
| `MAIL_PORT` | Porta SMTP | `587` |
| `MAIL_USERNAME` | Usuário SMTP | obrigatório |
| `MAIL_PASSWORD` | Senha SMTP | obrigatório |
| `MAIL_FROM` | Remetente dos e-mails | obrigatório |
| `APP_BASE_URL` | URL base para imagens | `http://localhost:8080/api` |
| `AUDIT_RETENTION_DAYS` | Retenção dos logs | `90` |
| `SWAGGER_ENABLED` | Ativa Swagger UI | `true` |

### 7.2 Profiles

| Profile | Arquivo | Uso |
|---------|---------|-----|
| `dev` | `application-dev.yml` | Desenvolvimento local |
| `staging` | `application-staging.yml` | Homologação |
| `prod` | `application-prod.yml` | Produção no Render |

### 7.3 Build e Deploy

```bash
mvn clean package -DskipTests        # Gera JAR
java -jar target/serveflow-*.jar      # Executa local
```

Render detecta `pom.xml`, executa `mvn clean package` e inicia o JAR automaticamente a cada push.

---

## 8. Boas Práticas e Manutenção

### 8.1 Adicionando Novo Módulo

```
V27__create_reservations.sql      → migration Flyway
ReservationEntity.java            → mapeamento JPA
SpringReservationRepository.java  → Spring Data JPA
Reservation.java                  → domínio puro
ReservationRepository.java        → interface de domínio
ReservationInput/Output.java      → DTOs
ReservationService.java           → lógica de negócio
ReservationController.java        → endpoint REST
ReservationNotFoundException.java → exceção de domínio
GlobalExceptionHandler.java       → adicionar @ExceptionHandler
```

### 8.2 Segurança

- Nunca commitar secrets — use variáveis de ambiente
- `JWT_SECRET` mínimo 256 bits de entropia
- `SWAGGER_ENABLED=false` em produção
- CORS: listar origens explicitamente, nunca `*`
- Refresh tokens são rotacionados a cada uso

### 8.3 Auditoria

| Tabela | Conteúdo | Retenção |
|--------|----------|----------|
| `access_log` | IP, endpoint, método, status | 90 dias |
| `audit_log` | Usuário, ação, entidade, detalhe | 90 dias |
| `error_log` | Endpoint, mensagem, stack trace | 90 dias |

### 8.4 Checklist de Deploy

- [ ] Variáveis de ambiente configuradas no Render
- [ ] `SPRING_PROFILES_ACTIVE=prod`
- [ ] `SWAGGER_ENABLED=false`
- [ ] `CORS_ALLOWED_ORIGINS` aponta para domínio Vercel
- [ ] Migrations Flyway executadas (logs de startup)
- [ ] Health check: `GET /api/actuator/health` → 200

---

## Referência Rápida — Endpoints

```
POST   /api/auth/login                 → Login
POST   /api/auth/refresh               → Renovar token
POST   /api/auth/forgot-password       → Solicitar reset
POST   /api/auth/reset-password        → Confirmar nova senha

POST   /api/orders                     → Novo pedido
PATCH  /api/orders/{id}/confirm        → Confirmar
PATCH  /api/orders/{id}/complete       → Concluir

GET    /api/kds/orders                 → Fila da cozinha
PATCH  /api/kds/orders/{id}/prepare    → Iniciar preparo
PATCH  /api/kds/orders/{id}/ready      → Marcar pronto

POST   /api/cashier/session/open       → Abrir caixa
POST   /api/cashier/session/{id}/close → Fechar caixa
POST   /api/cashier/orders/{id}/settle → Liquidar pedido

GET    /api/dashboard/metrics          → Métricas gerais
GET    /api/stock/report/consolidated  → Relatório de estoque
```

---

*ServeFlow — 207 classes Java · 26 migrations Flyway · 11 controllers · 14 services · 19 repositórios*
