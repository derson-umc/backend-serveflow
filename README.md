# ServeFlow — Backend

API REST do sistema de gestão de restaurantes ServeFlow, construída com Java 21 e Spring Boot 3.

---

## Tecnologias

| Tecnologia | Versão | Finalidade |
|---|---|---|
| Java | 21 | Linguagem principal |
| Spring Boot | 3.4.3 | Framework de aplicação |
| Spring Security + JWT (jjwt) | 0.12.3 | Autenticação e autorização |
| Spring Data JPA + Hibernate | — | Persistência |
| PostgreSQL | 42.7.3 (driver) | Banco de dados relacional |
| Flyway | — | Versionamento de migrações |
| WebSocket (STOMP) | — | Comunicação em tempo real (KDS) |
| Cloudinary | — | Upload de imagens em produção |
| Lombok | 1.18.38 | Redução de boilerplate |
| Clean Architecture + DDD | — | Organização estrutural |

---

## Pré-requisitos

- **Java 21 SDK** configurado no `JAVA_HOME`
- **PostgreSQL** rodando localmente **ou** Docker instalado

---

## Configuração

Crie o arquivo `.env` na raiz do projeto com base no `.env.example`:

### Banco de dados

| Variável | Descrição | Padrão |
|---|---|---|
| `POSTGRES_HOST` | Host do banco | `localhost` |
| `POSTGRES_PORT` | Porta do banco | `5432` |
| `POSTGRES_DB` | Nome do banco | `serveflow_db` |
| `POSTGRES_USER` | Usuário da aplicação | `postgres` |
| `POSTGRES_PASSWORD` | Senha do usuário | — |


### Aplicação

| Variável | Descrição | Padrão |
|---|---|---|
| `APP_BASE_URL` | URL base da API (usado para montar URLs de imagens) | `http://localhost:8080/api` |
| `JWT_SECRET` | Chave secreta para assinatura dos tokens (mínimo 256 bits) | — |
| `JWT_EXPIRATION` | Expiração do access token em ms | `28800000` (8h) |
| `ROOT_USERNAME` | Username do administrador inicial | `root` |
| `ROOT_PASSWORD` | Senha do administrador inicial | — |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas pelo CORS | `http://localhost:5173` |

### Integrações (opcionais)

| Variável | Descrição |
|---|---|
| `VIACEP_URL` | Endpoint ViaCEP para consulta de CEP |
| `MAIL_HOST` / `MAIL_PORT` | Servidor SMTP (reset de senha) |
| `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` | Credenciais SMTP — usar App Password do Google |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | Upload de imagens em produção |

---

## Inicialização

### Sem Docker (banco local)

```bash
# Compilar
./mvnw clean package -DskipTests

# Executar em desenvolvimento
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Com Docker (PostgreSQL + PgAdmin)

```bash
# Subir banco e PgAdmin
docker compose up -d

# Executar a aplicação
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

PgAdmin disponível em `http://localhost:5050` (credenciais definidas no `.env`).

---

A API ficará disponível em **http://localhost:8080**.  
Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## Estrutura de pacotes

```
src/main/java/com/serveflow/
├── config/                        # Segurança, CORS, JWT, WebSocket, Swagger
│
├── controller/                    # Endpoints REST por módulo
│   ├── cashier/
│   ├── dashboard/
│   ├── financial/
│   ├── kds/
│   ├── menu/
│   ├── order/
│   ├── product/
│   ├── stock/
│   ├── upload/
│   └── user/
│
├── service/                       # Regras de negócio por módulo
│   ├── audit/log/
│   ├── auth/
│   ├── cashier/
│   ├── dashboard/
│   ├── financial/
│   ├── kds/
│   ├── menu/
│   ├── order/
│   ├── product/
│   ├── stock/
│   ├── upload/
│   └── user/
│
├── repository/                    # Acesso a dados (JPA + queries nativas)
│   ├── audit/log/
│   ├── auth/
│   ├── cashier/
│   ├── dashboard/
│   ├── financial/
│   ├── menu/
│   ├── order/
│   ├── product/
│   ├── stock/
│   │   ├── productrecipe/
│   │   ├── recipeingredient/
│   │   ├── stockalert/
│   │   ├── stockitem/
│   │   └── stockmovement/
│   └── user/
│
├── model/                         # Entidades de domínio (JPA)
│   ├── address/
│   ├── auth/
│   ├── cashier/
│   ├── financial/
│   ├── menu/
│   ├── order/
│   ├── product/
│   ├── stock/
│   └── user/
│
├── dto/                           # Objetos de entrada e saída por módulo
│   ├── cashier/{request,response}/
│   ├── financial/{request,response}/
│   ├── kds/response/
│   ├── menu/{request,response}/
│   ├── order/{request,response}/
│   ├── product/{request,response}/
│   ├── stock/{request,response}/
│   └── user/{request,response}/
│
├── events/                        # Eventos de domínio (ex: OrderStatusEvent)
│
├── query/                         # Queries de leitura otimizadas
│   └── dashboard/
│
├── integration/                   # Integrações externas (ViaCEP, e-mail, Cloudinary)
│
├── exception/                     # Exceções de domínio e handler global
│   ├── auth/
│   ├── cashier/
│   ├── financial/
│   ├── handler/
│   ├── menu/
│   ├── order/
│   ├── product/
│   ├── stock/
│   └── user/
│
└── util/                          # Utilitários compartilhados
```

---

## Licença

Projeto acadêmico — Universidade de Mogi das Cruzes (UMC).
