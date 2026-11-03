# Catálogo de Vinhos — API RESTful e Controle de Estoque

API back-end desenvolvida em **Java 25**, com **Spring Boot**, **Spring Data JPA**, **Hibernate** e **SQLite**, para gerenciar um catálogo de vinhos e suas quantidades em estoque.

O projeto começou como um CRUD e foi ampliado com operações de **entrada e saída de garrafas**, **alertas de estoque baixo** e **sugestões automáticas de reposição**. Foi desenvolvido como projeto acadêmico para aplicar conceitos de persistência com JPA, Web Services RESTful, arquitetura em camadas e testes automatizados.

## Sumário

- [Funcionalidades](#funcionalidades)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como executar](#como-executar)
- [Banco de dados e configuração](#banco-de-dados-e-configuração)
- [Modelo de dados](#modelo-de-dados)
- [Endpoints da API](#endpoints-da-api)
- [Exemplos de requisições](#exemplos-de-requisições)
- [Regras de negócio](#regras-de-negócio)
- [Testes automatizados](#testes-automatizados)
- [Limitações e melhorias futuras](#limitações-e-melhorias-futuras)

## Funcionalidades

- CRUD completo de vinhos: cadastro, listagem, consulta por ID, atualização e exclusão.
- Persistência dos dados em um arquivo SQLite usando JPA e Hibernate.
- Controle de quantidade disponível e estoque mínimo por vinho.
- Registro de entradas e saídas de garrafas.
- Bloqueio de saída superior à quantidade disponível.
- Validação de preços e quantidades negativas.
- Proteção contra estouro de `Integer` nas entradas de estoque.
- Listagem de vinhos cujo estoque está abaixo do mínimo.
- Sugestão de reposição calculada com base no saldo atual.
- Testes unitários das regras de negócio com JUnit 5 e Mockito.

> **Escopo atual:** o sistema mantém o **saldo atual** de cada vinho. Ele ainda não registra um histórico individual de movimentações, não possui autenticação nem realiza compras automaticamente.

## Tecnologias

| Tecnologia | Utilização |
|---|---|
| Java 25 | Linguagem e versão configurada para compilação |
| Spring Boot 4.1.2 | Inicialização e configuração da aplicação |
| Spring Web MVC | Endpoints HTTP RESTful e comunicação JSON |
| Spring Data JPA | Repositórios e operações de persistência |
| Hibernate | Implementação de JPA e tradução das consultas para SQL |
| Hibernate Community Dialects | Dialeto SQLite |
| SQLite / SQLite JDBC | Banco relacional armazenado em arquivo local |
| Maven / Maven Wrapper | Dependências, compilação e testes |
| JUnit 5 | Testes automatizados |
| Mockito | Simulação de dependências nos testes unitários |
| Eclipse IDE | Ambiente de desenvolvimento utilizado no projeto |

## Arquitetura

A aplicação está organizada em camadas:

```text
Cliente HTTP (curl, Postman, navegador ou outra aplicação)
                         |
                         v
                 VinhoController
                         |
                         v
                   VinhoService
                         |
                         v
                 VinhoRepository
                         |
                         v
                JPA / Hibernate
                         |
                         v
                 SQLite (catalogo.db)
```

Estrutura principal do código:

```text
catalogo/
├── src/
│   ├── main/
│   │   ├── java/br/com/projeto/catalogo/
│   │   │   ├── CatalogoApplication.java
│   │   │   ├── controller/
│   │   │   │   └── VinhoController.java
│   │   │   ├── model/
│   │   │   │   └── Vinho.java
│   │   │   ├── repository/
│   │   │   │   └── VinhoRepository.java
│   │   │   └── service/
│   │   │       └── VinhoService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/java/br/com/projeto/catalogo/
│       ├── CatalogoApplicationTests.java
│       └── service/
│           └── VinhoServiceTest.java
├── .mvn/wrapper/
├── mvnw
├── mvnw.cmd
└── pom.xml
```

**Responsabilidades:**

- **Controller:** mapeia as rotas REST, recebe o JSON, define os códigos HTTP e envia as respostas.
- **Service:** centraliza regras de negócio, validações, cálculos de estoque e coordena a persistência.
- **Repository:** estende `JpaRepository<Vinho, Long>` e declara a consulta personalizada de estoque baixo.
- **Model:** define a entidade `Vinho`, mapeada pelo JPA para a tabela `vinhos`.

A consulta de estoque baixo é escrita em **JPQL**, utilizando o nome da entidade e seus atributos Java:

```java
@Query("SELECT v FROM Vinho v WHERE v.quantidade < v.estoqueMinimo")
List<Vinho> buscarComEstoqueBaixo();
```

## Pré-requisitos

- **JDK 25** instalado e acessível pelo terminal (`java` e `javac`).
- **Git** para clonar o repositório.
- Conexão com a internet no primeiro uso para baixar as dependências e, se necessário, a distribuição Maven do Wrapper.
- **SQLite CLI (`sqlite3`)** é opcional, útil apenas para inspecionar diretamente o banco.

Confira a instalação do Java:

```bash
java -version
javac -version
```

O Maven pode ser executado pelo **Maven Wrapper** presente no repositório, sem precisar instalar o Maven globalmente.

## Como executar

### 1. Clonar o projeto

```bash
git clone https://github.com/chavesfop/catalogo-vinhos-java.git
cd catalogo-vinhos-java
```

### 2. Iniciar o servidor

No Linux ou macOS:

```bash
./mvnw spring-boot:run
```

Se necessário, torne o script executável antes:

```bash
chmod +x mvnw
```

No Windows:

```bat
mvnw.cmd spring-boot:run
```

Também é possível importar o projeto no Eclipse em **File → Import → Maven → Existing Maven Projects** e executar `CatalogoApplication.java` em **Run As → Java Application**.

Quando o Spring Boot terminar de iniciar, a API estará disponível em:

```text
http://localhost:8080
```

Não há uma página inicial configurada na rota `/`. Um **404** nessa URL é esperado; consulte a API em `/api/vinhos`.

### 3. Verificar se está respondendo

```bash
curl -i http://localhost:8080/api/vinhos
```

Se não houver registros, o corpo será:

```json
[]
```

Para encerrar a aplicação iniciada no terminal, utilize **Ctrl+C**. No Eclipse, encerre a execução na visualização **Console** ou **Debug**.

## Banco de dados e configuração

Arquivo: `src/main/resources/application.properties`

```properties
# Application
spring.application.name=catalogo

# SQLite
spring.datasource.url=jdbc:sqlite:catalogo.db
spring.datasource.driver-class-name=org.sqlite.JDBC

# JPA / Hibernate
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# HTTP Server
server.port=8080
```

- **`catalogo.db`** é criado/acessado no **diretório de trabalho** do processo Java. Ao executar os comandos a partir da raiz do projeto, o arquivo normalmente ficará ali.
- **`ddl-auto=update`** permite ao Hibernate criar/atualizar estruturas durante o desenvolvimento. Não deve ser tratado como estratégia de migração para produção.
- **`show-sql=true`** exibe os comandos SQL gerados no console.
- O SQLite não requer um servidor de banco separado.
- O arquivo `*.db` está ignorado pelo Git, para não versionar os dados locais.

Consulta opcional no terminal (execute a partir da pasta que contém o banco):

```bash
sqlite3 catalogo.db ".tables"
sqlite3 catalogo.db "PRAGMA table_info(vinhos);"
sqlite3 catalogo.db "SELECT id, nome, quantidade, estoque_minimo FROM vinhos;"
```

> **Atenção:** um SQLite de desenvolvimento pode conter dados locais. Faça backup antes de alterações manuais e não publique arquivos de banco contendo informações reais.

## Modelo de dados

Entidade JPA: `Vinho` — tabela: `vinhos`.

| Campo Java | Tipo | Descrição |
|---|---|---|
| `id` | `Long` | Identificador gerado automaticamente pelo JPA/banco |
| `nome` | `String` | Nome do vinho |
| `uva` | `String` | Uva ou composição do vinho |
| `preco` | `BigDecimal` | Preço do vinho |
| `quantidade` | `Integer` | Quantidade atual de garrafas; padrão Java: `0` |
| `estoqueMinimo` | `Integer` | Limite mínimo de estoque; padrão Java: `0` |

O atributo Java `estoqueMinimo` é mapeado para a coluna SQLite `estoque_minimo`.

Exemplo de representação JSON:

```json
{
  "id": 1,
  "nome": "Alma Negra Misterio",
  "uva": "Blend",
  "preco": 159.90,
  "quantidade": 10,
  "estoqueMinimo": 5
}
```

## Endpoints da API

**URL base:** `http://localhost:8080/api/vinhos`

| Método | Rota | Descrição | Sucesso |
|---|---|---|---|
| `GET` | `/api/vinhos` | Lista todos os vinhos | `200 OK` |
| `GET` | `/api/vinhos/{id}` | Busca um vinho por ID | `200 OK` |
| `POST` | `/api/vinhos` | Cadastra um vinho | `201 Created` |
| `PUT` | `/api/vinhos/{id}` | Atualiza nome, uva e preço | `200 OK` |
| `DELETE` | `/api/vinhos/{id}` | Exclui um vinho | `204 No Content` |
| `POST` | `/api/vinhos/{id}/entrada` | Soma garrafas ao estoque | `200 OK` |
| `POST` | `/api/vinhos/{id}/saida` | Subtrai garrafas do estoque | `200 OK` |
| `GET` | `/api/vinhos/estoque-baixo` | Lista vinhos abaixo do mínimo | `200 OK` |
| `GET` | `/api/vinhos/sugestoes-reposicao` | Calcula quantidades para reposição | `200 OK` |

**Erros tratados na API:** `400 Bad Request` para validações explícitas de negócio em POST/PUT/entrada/saída e `404 Not Found` quando o ID não existe nas operações individuais. Os exemplos abaixo usam IDs ilustrativos: ajuste-os conforme os IDs retornados na sua instalação.

## Exemplos de requisições

Os exemplos usam **curl** em um terminal Linux/macOS. Execute-os com a aplicação iniciada. Com a opção `-i`, o `curl` também mostra os cabeçalhos e o status HTTP.

### 1. Cadastrar um vinho — `POST /api/vinhos`

```bash
curl -i -X POST http://localhost:8080/api/vinhos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Alma Negra Misterio",
    "uva": "Blend",
    "preco": 159.90,
    "quantidade": 10,
    "estoqueMinimo": 5
  }'
```

Resposta esperada: **`201 Created`**.

```json
{
  "id": 1,
  "nome": "Alma Negra Misterio",
  "uva": "Blend",
  "preco": 159.90,
  "quantidade": 10,
  "estoqueMinimo": 5
}
```

O `id` é gerado automaticamente. Não envie `id` no corpo do cadastro.

### 2. Listar todos os vinhos — `GET /api/vinhos`

```bash
curl -i http://localhost:8080/api/vinhos
```

Resposta esperada: **`200 OK`**, com um array JSON (ou `[]` se não houver vinhos).

```json
[
  {
    "id": 1,
    "nome": "Alma Negra Misterio",
    "uva": "Blend",
    "preco": 159.90,
    "quantidade": 10,
    "estoqueMinimo": 5
  }
]
```

### 3. Consultar um vinho pelo ID — `GET /api/vinhos/{id}`

```bash
curl -i http://localhost:8080/api/vinhos/1
```

Se o ID existir: **`200 OK`** com o JSON do vinho. Se não existir: **`404 Not Found`** com corpo vazio.

### 4. Atualizar um vinho — `PUT /api/vinhos/{id}`

```bash
curl -i -X PUT http://localhost:8080/api/vinhos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Alma Negra Misterio 2022",
    "uva": "Blend",
    "preco": 179.90
  }'
```

Resposta esperada: **`200 OK`** com o vinho atualizado.

> Na implementação atual, o `PUT` altera **somente** `nome`, `uva` e `preco`. Os campos `quantidade` e `estoqueMinimo` do registro existente são preservados mesmo que sejam enviados no JSON. Para alterar a quantidade, use os endpoints de entrada e saída. Ainda não existe uma rota específica para alterar o estoque mínimo de um vinho já cadastrado.

### 5. Registrar entrada de estoque — `POST /api/vinhos/{id}/entrada`

Supondo 10 garrafas disponíveis, uma entrada de 5 unidades atualiza o saldo para 15.

```bash
curl -i -X POST http://localhost:8080/api/vinhos/1/entrada \
  -H "Content-Type: application/json" \
  -d '{"quantidade": 5}'
```

Resposta esperada: **`200 OK`** com o objeto `Vinho` atualizado. Exemplo do campo alterado:

```json
{
  "id": 1,
  "nome": "Alma Negra Misterio 2022",
  "uva": "Blend",
  "preco": 179.90,
  "quantidade": 15,
  "estoqueMinimo": 5
}
```

### 6. Registrar saída de estoque — `POST /api/vinhos/{id}/saida`

Supondo 15 garrafas disponíveis, uma saída de 3 atualiza o saldo para 12.

```bash
curl -i -X POST http://localhost:8080/api/vinhos/1/saida \
  -H "Content-Type: application/json" \
  -d '{"quantidade": 3}'
```

Resposta esperada: **`200 OK`** com o vinho atualizado (`"quantidade": 12` neste exemplo).

Se tentar retirar mais garrafas que as disponíveis, a API retorna **`400 Bad Request`**:

```bash
curl -i -X POST http://localhost:8080/api/vinhos/1/saida \
  -H "Content-Type: application/json" \
  -d '{"quantidade": 999}'
```

Exemplo da resposta, se o saldo for 12:

```json
{
  "erro": "Estoque insuficiente. Disponivel: 12"
}
```

### 7. Listar vinhos com estoque baixo — `GET /api/vinhos/estoque-baixo`

Para demonstrar a consulta, cadastre um segundo vinho abaixo do estoque mínimo:

```bash
curl -i -X POST http://localhost:8080/api/vinhos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Angelica Zapata Malbec",
    "uva": "Malbec",
    "preco": 189.90,
    "quantidade": 2,
    "estoqueMinimo": 6
  }'
```

Consulte:

```bash
curl -i http://localhost:8080/api/vinhos/estoque-baixo
```

Resposta esperada: **`200 OK`** com os vinhos para os quais `quantidade < estoqueMinimo`.

```json
[
  {
    "id": 2,
    "nome": "Angelica Zapata Malbec",
    "uva": "Malbec",
    "preco": 189.90,
    "quantidade": 2,
    "estoqueMinimo": 6
  }
]
```

Se nenhum vinho estiver abaixo do mínimo, o resultado será `[]`. Vinhos com `quantidade == estoqueMinimo` **não** entram nessa lista, pois a regra utiliza `<`, e não `<=`.

### 8. Consultar sugestões de reposição — `GET /api/vinhos/sugestoes-reposicao`

```bash
curl -i http://localhost:8080/api/vinhos/sugestoes-reposicao
```

Resposta esperada: **`200 OK`** com as quantidades sugeridas:

```json
[
  {
    "id": 2,
    "nome": "Angelica Zapata Malbec",
    "quantidadeAtual": 2,
    "estoqueMinimo": 6,
    "quantidadeSugerida": 4
  }
]
```

**Fórmula:**

```text
quantidadeSugerida = estoqueMinimo - quantidadeAtual
```

Somente vinhos abaixo do mínimo entram no cálculo. A consulta **não** altera o banco nem emite pedidos de compra. A entrada de novas garrafas altera o resultado de uma consulta posterior.

### 9. Excluir um vinho — `DELETE /api/vinhos/{id}`

> Esta operação remove o registro. Substitua o ID do exemplo por um vinho de teste que possa ser excluído.

```bash
curl -i -X DELETE http://localhost:8080/api/vinhos/2
```

Se existir: **`204 No Content`**, sem corpo na resposta. Se não existir: **`404 Not Found`**.

### 10. Exemplos de validações — `400 Bad Request`

**Preço negativo no cadastro:**

```bash
curl -i -X POST http://localhost:8080/api/vinhos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Vinho Invalido",
    "uva": "Malbec",
    "preco": -50.00,
    "quantidade": 5,
    "estoqueMinimo": 2
  }'
```

Resposta:

```json
{"erro": "O preco nao pode ser negativo."}
```

**Quantidade negativa no cadastro:**

```bash
curl -i -X POST http://localhost:8080/api/vinhos \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Vinho Invalido",
    "uva": "Malbec",
    "preco": 80.00,
    "quantidade": -1,
    "estoqueMinimo": 2
  }'
```

Resposta:

```json
{"erro": "A quantidade nao pode ser negativa."}
```

**Entrada com zero unidades:**

```bash
curl -i -X POST http://localhost:8080/api/vinhos/1/entrada \
  -H "Content-Type: application/json" \
  -d '{"quantidade": 0}'
```

Resposta:

```json
{"erro": "A quantidade de entrada deve ser maior que zero."}
```

**Consulta de vinho inexistente:**

```bash
curl -i http://localhost:8080/api/vinhos/9999
```

Resposta: **`404 Not Found`** (corpo vazio).

## Regras de negócio

| Operação | Regra aplicada |
|---|---|
| Cadastro | `id` não pode ser informado; deve ser gerado automaticamente |
| Cadastro | `preco`, `quantidade` e `estoqueMinimo` não podem ser nulos nem negativos |
| Atualização (`PUT`) | Preço não pode ser nulo nem negativo |
| Atualização (`PUT`) | Atualiza nome, uva e preço; preserva quantidade e estoque mínimo |
| Entrada | Quantidade de entrada deve ser maior que zero |
| Entrada | Estoque atual não pode ser nulo nem negativo |
| Entrada | A soma é protegida contra overflow de `int` por `Math.addExact()` |
| Saída | Quantidade de saída deve ser maior que zero |
| Saída | Estoque atual não pode ser nulo nem negativo |
| Saída | É proibido retirar mais unidades que o saldo disponível |
| Estoque baixo | Um vinho está abaixo do mínimo quando `quantidade < estoqueMinimo` |
| Reposição | Sugere `estoqueMinimo - quantidade`, apenas para vinhos abaixo do mínimo |

A implementação atual faz validações de negócio por meio de `IllegalArgumentException`. Nos endpoints correspondentes, o Controller converte essas exceções em `400 Bad Request`, com resposta no formato `{"erro": "mensagem"}`.

O projeto **ainda não** implementa validação obrigatória de `nome` e `uva`, autenticação, controle de concorrência ou um tratamento global de exceções.

## Testes automatizados

O projeto utiliza **JUnit 5** e **Mockito** para testar a camada de serviços sem fazer alterações reais no SQLite.

Classe principal de testes:

```text
src/test/java/br/com/projeto/catalogo/service/VinhoServiceTest.java
```

Exemplos de cenários cobertos:

- Cadastro válido e rejeição de preço, quantidade ou estoque mínimo negativos.
- Consulta de vinho existente e inexistente.
- Atualização válida e rejeição de preço negativo.
- Exclusão de vinho existente e inexistente.
- Listagem com resultados e lista vazia.
- Entrada e saída válidas de estoque.
- Bloqueio de saída com saldo insuficiente.
- Bloqueio de entradas e saídas com quantidades negativas ou iguais a zero.
- Proteção contra overflow de `Integer`.
- Consulta de produtos abaixo do estoque mínimo.
- Cálculo de sugestão de reposição e ausência de sugestões.

A classe `CatalogoApplicationTests` também contém um teste de inicialização do contexto Spring (`contextLoads`).

### Executar pelo terminal

```bash
./mvnw test
```

Ou, para executar apenas a classe de testes de serviço:

```bash
./mvnw -Dtest=VinhoServiceTest test
```

### Executar pelo Eclipse

Abra `VinhoServiceTest.java` e selecione **Run As → JUnit Test**. Uma execução bem-sucedida mostra a barra verde na visualização JUnit.

> **Diferença entre os testes:** os testes unitários com Mockito verificam as regras do `VinhoService` usando um repositório simulado; **não** exercitam a consulta JPQL nem o SQLite real. Testes de integração JPA e testes HTTP automatizados são evoluções futuras. O teste `contextLoads` pode utilizar a configuração real de datasource, por isso é recomendável separar o banco de testes do banco de desenvolvimento antes de ampliar os testes de integração.

Em Java 25, podem aparecer avisos relacionados ao carregamento do agente Mockito ou ao acesso nativo do driver SQLite. Esses avisos, por si só, não significam falha; o resultado deve ser confirmado pelos testes e códigos de saída do Maven.

## Limitações e melhorias futuras

O objetivo atual é demonstrar JPA, RESTful e regras de negócio com uma implementação simples. Melhorias possíveis:

- Criar testes de integração para JPA/SQLite e testes automatizados dos endpoints HTTP.
- Adicionar validações formais para nome, uva e formato das requisições.
- Centralizar o tratamento de erros com `@RestControllerAdvice`.
- Adotar DTOs para separar contratos HTTP das entidades persistidas.
- Criar um endpoint específico para alterar o estoque mínimo.
- Registrar histórico de movimentações com data, tipo, quantidade e motivo.
- Garantir consistência em movimentações simultâneas com transações e estratégia de concorrência.
- Adicionar paginação, filtros de pesquisa, documentação OpenAPI/Swagger e autenticação.
- Separar configurações por ambiente e utilizar migrações de banco para produção.

## Contexto acadêmico

O projeto foi desenvolvido para exercitar e demonstrar:

1. **Funcionalidade:** CRUD completo, persistência e operações de estoque.
2. **Inovação:** alertas e sugestões automáticas de reposição.
3. **Testes:** verificação automatizada das regras de negócio com JUnit e Mockito.
4. **Qualidade de código:** separação em camadas e responsabilidades bem definidas.
5. **Apresentação:** demonstração dos endpoints, persistência e execução dos testes.

Outras entregas da disciplina, como atas de reuniões e relatórios de impeditivos, devem ser organizadas conforme as orientações do professor.

---

**Repositório:** https://github.com/chavesfop/catalogo-vinhos-java
