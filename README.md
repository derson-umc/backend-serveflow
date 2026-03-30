# ServeFlow – Gestão Inteligente para Pequenos Restaurantes

O **ServeFlow** é a solução para pequenos restaurantes que buscam **organização, eficiência e controle total** das operações do dia a dia. Desenvolvido com **Spring Boot** e **PostgreSQL**, ele transforma a gestão de produtos, pedidos e operações em um processo simples e ágil.

---

## Principais Benefícios

* **Controle de produtos:** Cadastro, atualização e monitoramento do estoque em tempo real.
* **Gestão de pedidos:** Acompanhe pedidos desde a criação até a finalização, reduzindo erros e atrasos.
* **Operações do dia a dia:** Relatórios rápidos e confiáveis de vendas e movimentações financeiras.
* **Escalabilidade e segurança:** Arquitetura modular que suporta crescimento do negócio com confiabilidade.

---

## Tecnologias Utilizadas

* **Back-end:** Spring Boot
* **Banco de Dados:** PostgreSQL
* **Arquitetura:** Camadas organizadas por **microsserviços**, garantindo modularidade, manutenção simplificada e escalabilidade.

---

## Arquitetura do Sistema

O ServeFlow utiliza **arquitetura em camadas**, organizada em **microsserviços**:

```
Controller    -> Recebe requisições e retorna respostas
     │
DTO           -> Objetos para transporte de dados entre camadas
     │
Exception     -> Trata erros e respostas de exceção de forma padronizada
     │
Service       -> Contém regras de negócio e processamento
     │
Repository    -> Interage com o banco PostgreSQL
     │
Model         -> Define entidades e estrutura de dados
```
