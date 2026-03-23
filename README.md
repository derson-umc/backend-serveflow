# ServeFlow - Sistema para Pequenos Restaurantes

Sistema de gestao para pequenos restaurantes, feito com Spring Boot e PostgreSQL. Permite controlar produtos, pedidos e operacoes do dia a dia.

---

## Pre-requisitos

Antes de comecar, voce precisa ter instalado:

- Java 17 ou superior
- Maven 3.8 ou superior
- Docker e Docker Compose
- Git

---

## Estrutura do Projeto

```
project-serveflow/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/.../serveflow/
│       │       ├── controller/        # Endpoints REST
│       │       ├── service/           # Regras de negocio
│       │       ├── repository/        # Acesso a dados (JPA)
│       │       ├── model/             # Entidades do banco
│       │       ├── dto/               # Objetos de transferencia
│       │       └── exception/         # Tratamento de erros
│       └── resources/
│           └── application.properties # Configuracoes da aplicacao
├── docker-compose.yml                 # PostgreSQL + pgAdmin
├── .env                               # Credenciais (NAO vai pro Git)
├── .env.example                       # Template de configuracao
├── .gitignore
├── pom.xml
└── README.md
```

---

## Configuracao do Ambiente

### 1. Clone o repositorio

```bash
git clone https://github.com/derson-umc/project-serveflow.git
cd project-serveflow
```

### 2. Configure as variaveis de ambiente

Copie o arquivo de exemplo e preencha com seus dados:

```bash
cp .env.example .env
```

Abra o `.env` e preencha:

```env
# Banco de Dados
POSTGRES_USER=seu_usuario
POSTGRES_PASSWORD=sua_senha
POSTGRES_DB=serveflow_db
POSTGRES_PORT=5432

# pgAdmin
PGADMIN_DEFAULT_EMAIL=seu_email@exemplo.com
PGADMIN_DEFAULT_PASSWORD=sua_senha_pgadmin
PGADMIN_PORT=15432
```

O arquivo `.env` tem dados sensiveis e nao deve ser enviado para o Git. Ele ja esta no `.gitignore`.

---

## Docker

### Subir o banco e o pgAdmin

```bash
docker-compose up -d
```

Isso vai iniciar:

| Servico    | Porta | O que faz                     |
|------------|-------|-------------------------------|
| PostgreSQL | 5432  | Banco de dados da aplicacao   |
| pgAdmin    | 15432 | Interface web para o banco    |

### Acessar o pgAdmin

1. Abra no navegador: `http://localhost:15432`
2. Faca login com o email e senha que voce colocou no `.env`
3. Adicione o servidor com os dados:
   - Host: `postgress` (nome do container)
   - Porta: `5432`
   - Usuario e senha: os mesmos do `.env`

### Parar os containers

```bash
docker-compose down
```

### Parar e apagar os dados do banco

```bash
docker-compose down -v
```

---

## Como Rodar

### Pela IDE (IntelliJ ou VS Code)

1. Suba o banco: `docker-compose up -d`
2. Abra o projeto na IDE
3. Rode a classe `ServeFlowApplication.java`
4. Acesse: `http://localhost:8080`

### Pelo terminal

```bash
# Suba o banco
docker-compose up -d

# Compile o projeto
mvn clean install

# Rode a aplicacao
mvn spring-boot:run
```

A API vai estar disponivel em `http://localhost:8080`.

---

## Endpoints

| Metodo | Rota              | O que faz                  |
|--------|-------------------|----------------------------|
| GET    | /products         | Lista todos os produtos    |
| POST   | /products         | Cadastra um produto        |
| POST   | /products/batch   | Cadastra produtos em lote  |

Os endpoints serao atualizados conforme o projeto cresce.

---

## Como Contribuir

1. Crie uma branch a partir da develop:
   ```bash
   git checkout develop
   git checkout -b feature/sua-feature
   ```

2. Faca commits seguindo o padrao:
   ```
   feat(modulo): descricao da funcionalidade
   fix(modulo): descricao da correcao
   chore: tarefa de manutencao
   ```

3. Envie e abra um Pull Request para a develop:
   ```bash
   git push origin feature/sua-feature
   ```

---

## Licenca

Projeto de uso academico e pessoal.
