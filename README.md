# ServeFlow — Backend

API REST para o sistema de gestão de restaurantes ServeFlow. Cobre pedidos, caixa, estoque, financeiro e autenticação com controle de acesso por perfil.

## Tecnologias

- Java 21
- Spring Boot 3.4.3 (Web, Security, Data JPA, WebSocket)
- PostgreSQL 16
- Flyway
- JWT (jjwt 0.12.3)
- Lombok
- Springdoc OpenAPI

## Pré-requisitos

- Java 21+
- Maven 3.9+
- PostgreSQL 16+ rodando localmente ou via Docker

## Configuração

Copie o arquivo de exemplo e preencha as variáveis:

```bash
cp .env.example .env
```

Variáveis obrigatórias:

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

Variáveis opcionais (reset de senha por e-mail):

| Variável | Descrição |
|---|---|
| `MAIL_HOST` / `MAIL_PORT` | Servidor SMTP |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Credenciais SMTP |

## Executando

```bash
# Compilar
mvn clean install -DskipTests

# Rodar em desenvolvimento
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

API disponível em `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Perfis de configuração

| Perfil | Uso |
|---|---|
| `dev` | Desenvolvimento local com SQL logging detalhado |
| `staging` | Ambiente de homologação |
| `prod` | Produção com logging reduzido |

## Módulos da API

| Módulo | Prefixo | Descrição |
|---|---|---|
| Autenticação | `/auth` | Login, refresh token, reset de senha |
| Usuários | `/users` | CRUD e gerenciamento de perfis |
| Produtos | `/products` | Cardápio e categorias |
| Pedidos | `/orders` | Comandas e delivery |
| Menu | `/menus` | Configuração do menu ativo |
| Caixa | `/cashier` | Sessões e movimentações de caixa |
| Estoque | `/stock` | Insumos, entradas, saídas e alertas |
| Financeiro | `/financial` | Contas a pagar e a receber |
| Dashboard | `/dashboard` | KPIs e relatórios gerenciais |
| KDS | `/kds` | Monitor de preparo em tempo real (WebSocket) |
| Upload | `/upload` | Imagens de produtos |

## Perfis de acesso (RBAC)

| Perfil | Permissões |
|---|---|
| `ADMIN` | Acesso total |
| `GERENTE` | Gestão operacional completa |
| `GARCOM` | Menu, pedidos e comandas |
| `COZINHEIRO` | KDS e fichas técnicas |
| `CAIXA` | Caixa e financeiro |

## Estrutura

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

src/main/resources/
├── db/migration/    # Scripts Flyway (V1–V26)
└── config/          # Arquivos de configuração por perfil
```

## Migrations

O Flyway executa as migrations automaticamente na inicialização. Todos os scripts são idempotentes. Para adicionar mudanças no banco, crie um novo arquivo `V27__descricao.sql` — nunca edite migrations já aplicadas.

## Licença

Projeto acadêmico — Universidade de Mogi das Cruzes (UMC).
