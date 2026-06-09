# ServeFlow — Backend

API REST do sistema de gestão de restaurantes ServeFlow, construída com Java 21 e Spring Boot 3.

---

## Tecnologias

| Tecnologia | Finalidade |
|---|---|
| Java 21 | Linguagem principal |
| Spring Boot 3 | Framework de aplicação |
| Spring Security + JWT | Autenticação e autorização |
| Spring Data JPA + Hibernate | Persistência |
| PostgreSQL | Banco de dados relacional |
| Flyway | Versionamento de migrações |
| WebSocket (STOMP) | Comunicação em tempo real (KDS) |
| Clean Architecture + DDD | Organização estrutural |
| SOLID | Princípios de design |

---

## Pré-requisitos

- **Java 21 SDK** instalado e configurado no `JAVA_HOME`
- **PostgreSQL** rodando localmente com banco e credenciais configurados

---

## Configuração

Crie o arquivo `.env` na raiz do projeto com base no `.env.example`:

| Variável | Descrição |
|---|---|
| `POSTGRES_HOST` | Host do banco (padrão: `localhost`) |
| `POSTGRES_PORT` | Porta do banco (padrão: `5432`) |
| `POSTGRES_DB` | Nome do banco |
| `POSTGRES_USER` | Usuário da aplicação |
| `POSTGRES_PASSWORD` | Senha do usuário |
| `JWT_SECRET` | Chave secreta para assinatura dos tokens (mínimo 256 bits) |
| `JWT_EXPIRATION` | Expiração do access token em ms (ex: `86400000`) |
| `ROOT_USERNAME` | Username do administrador inicial |
| `ROOT_PASSWORD` | Senha do administrador inicial |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (ex: `http://localhost:5173`) |

---

## Inicialização

```bash
# Compilar o projeto
./mvnw clean package -DskipTests

# Executar em desenvolvimento
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

A API ficará disponível em **http://localhost:8080**.  
Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## Estrutura de pacotes

```
src/main/java/com/serveflow/
├── config/          # Segurança, CORS, JWT, WebSocket
├── controller/      # Endpoints por módulo
├── service/         # Regras de negócio
├── repository/      # Acesso a dados (JPA + queries nativas)
├── model/           # Entidades de domínio
├── dto/             # Entrada e saída de dados
├── exception/       # Exceções e handlers globais
└── util/            # Utilitários compartilhados
```

---

## Licença

Projeto acadêmico — Universidade de Mogi das Cruzes (UMC).
