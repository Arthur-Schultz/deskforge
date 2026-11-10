# DeskForge

E-commerce full stack de periféricos e acessórios de setup, com **Spring Boot 4 (Java 21)**, **PostgreSQL** e **React 19**. Resolve os problemas reais de uma loja virtual: estoque à prova de concorrência, máquina de estados de pedidos, autenticação com JWT, banco com privilégios mínimos e conformidade com a legislação brasileira (CDC, Decreto 7.962/2013, LGPD e Marco Civil da Internet).

> A empresa, os produtos e o CNPJ são fictícios. Nenhum pagamento é processado.

![Página inicial do DeskForge](docs/screenshots/home.png)

## Destaques

- **Estoque à prova de concorrência.** O checkout bloqueia as linhas dos produtos com `SELECT ... FOR UPDATE` em ordem de id, sem deadlocks. Um teste de integração dispara 8 compras simultâneas da última unidade e garante que só 1 é aprovada.
- **Máquina de estados de pedidos.** `AGUARDANDO_PAGAMENTO → PAGO → ENVIADO → ENTREGUE`, com cancelamento validado no domínio e devolução automática do estoque.
- **Reserva com prazo.** Um job agendado cancela os pedidos não pagos em 30 minutos e libera os produtos.
- **Direito de arrependimento (art. 49 do CDC).** O cliente cancela pela própria conta em até 7 dias após a entrega, com estorno solicitado ao gateway.
- **Pagamento com Stripe Checkout.** O cliente paga na página hospedada do Stripe, então dados de cartão nunca passam pela loja. A confirmação chega por webhook com assinatura verificada e é idempotente: eventos repetidos são ignorados. Se um pagamento chegar depois de o pedido expirar, ele é estornado automaticamente. A interface `PaymentGateway` também tem uma implementação simulada, usada por padrão.
- **Banco com privilégios mínimos.** As migrations rodam com o usuário dono do schema, e a aplicação conecta com um usuário que só tem `SELECT`, `INSERT`, `UPDATE` e `DELETE`. Regras de negócio críticas também são garantidas por `CHECK` no próprio PostgreSQL.
- **LGPD de verdade.** Coleta mínima, consentimento granular de cookies com registro de prova, exportação dos dados em JSON, exclusão de conta por anonimização (os pedidos ficam guardados por obrigação fiscal) e formulário de direitos do titular com protocolo e prazo.
- **Marco Civil da Internet.** Guarda de registros de acesso (IP, data e hora) por 6 meses e expurgo automático depois desse prazo.
- **Acessibilidade WCAG 2.2 AA.** Navegação completa por teclado, foco visível, link para pular ao conteúdo, contraste verificado, erros de formulário associados aos campos e suporte a `prefers-reduced-motion`.
- **Ilustrações originais em SVG.** Nenhuma imagem de terceiros, então não há risco de direitos autorais. A vitrine é uma mesa vista de cima, interativa e navegável por teclado.

## Telas

| Produto | Catálogo |
| --- | --- |
| ![Página de produto](docs/screenshots/product.png) | ![Catálogo filtrado](docs/screenshots/catalog.png) |

| Painel administrativo | Política de Privacidade |
| --- | --- |
| ![Painel admin](docs/screenshots/admin.png) | ![Política de privacidade](docs/screenshots/privacy.png) |

<p align="center"><img src="docs/screenshots/mobile.png" alt="Versão mobile" width="260"></p>

## Stack

| Camada | Tecnologias |
| --- | --- |
| API | Java 21, Spring Boot 4.1, Spring Security 7, Spring Data JPA (Hibernate 7), Bean Validation, Flyway, springdoc-openapi, JJWT, Stripe Java |
| Banco | PostgreSQL 17 |
| Web | React 19, TypeScript, Vite, React Router, TanStack Query, Tailwind CSS 4 |
| Testes | JUnit 5, AssertJ, Spring Security Test, Testcontainers |
| Infra | Docker Compose, Nginx, GitHub Actions |

## Arquitetura

```
deskforge/
├── backend/                     API REST (Spring Boot)
│   └── src/main/java/com/deskforge/
│       ├── auth/                JWT, filtro de autenticação, regras de acesso
│       ├── catalog/             Produtos, categorias, busca com Specifications
│       ├── order/               Checkout, máquina de estados, expiração de pedidos
│       ├── payment/             PaymentGateway, Stripe Checkout, webhooks e modo simulado
│       ├── privacy/             Consentimento, direitos do titular, exportação e anonimização
│       ├── support/             SAC com protocolo e prazo de resposta
│       ├── audit/               Registros de acesso (Marco Civil) e retenção
│       ├── admin/               Indicadores do painel
│       ├── store/               Dados do fornecedor, frete e tributos aproximados
│       └── shared/              Erros RFC 9457 (Problem Details), paginação, configuração
├── frontend/                    Loja e painel (React)
├── infra/
│   ├── postgres/                Script que cria o usuário restrito da aplicação
│   └── pgadmin/                 Conexão pré-configurada do pgAdmin
└── docker-compose.yml           PostgreSQL + API + Web (+ pgAdmin opcional)
```

Regras de negócio ficam nas entidades (`Order`, `Product`), e os serviços orquestram transações. Todos os erros seguem o formato **Problem Details (RFC 9457)**, com mensagens em português e erros por campo.

## Como rodar

Pré-requisito: Docker Desktop. Primeiro crie o arquivo de configuração a partir do modelo e troque as senhas:

```bash
cp .env.example .env
```

Para gerar o `JWT_SECRET`: `openssl rand -base64 48`. O `.env` está no `.gitignore` e nunca vai para o repositório.

### Tudo com Docker

```bash
docker compose up --build
```

- Loja: http://localhost:3000
- API: http://localhost:8080
- Documentação da API (Swagger): http://localhost:8080/swagger-ui.html

### Em modo de desenvolvimento

```bash
docker compose up -d db

cd backend
./mvnw spring-boot:run        # lê o ../.env automaticamente

cd ../frontend
npm install
npm run dev                   # http://localhost:5173
```

### Contas de demonstração

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Cliente | cliente@deskforge.dev | Cliente@2026 |
| Admin | admin@deskforge.dev | Admin@2026 |

## Pagamentos com Stripe (opcional)

Por padrão o pagamento é simulado. Para usar o Stripe em modo de teste:

1. Crie uma conta em stripe.com e copie a chave secreta de teste (`sk_test_...`) em *Desenvolvedores → Chaves de API*.
2. Instale o [Stripe CLI](https://docs.stripe.com/stripe-cli) e encaminhe os webhooks para a API:
   ```bash
   stripe login
   stripe listen --forward-to localhost:8080/api/payments/stripe/webhook \
     --events checkout.session.completed,checkout.session.expired,checkout.session.async_payment_succeeded,checkout.session.async_payment_failed
   ```
3. No `.env`, preencha:
   ```
   PAYMENT_PROVIDER=stripe
   STRIPE_SECRET_KEY=sk_test_...
   STRIPE_WEBHOOK_SECRET=whsec_...   # exibido pelo stripe listen
   ```
4. Pague com o cartão de teste `4242 4242 4242 4242`, qualquer data futura e qualquer CVC.

A API se recusa a iniciar com uma chave `sk_live_`, para que o projeto nunca cobre de verdade.

## Banco de dados

| Usuário | Usado por | Permissões |
| --- | --- | --- |
| `POSTGRES_USER` (deskforge) | Flyway, nas migrations | Dono do schema |
| `DATABASE_USERNAME` (deskforge_app) | API em tempo de execução | Apenas leitura e escrita de dados |

O schema é versionado pelo Flyway em `backend/src/main/resources/db/migration`, e o Hibernate apenas valida o mapeamento. Além de chaves estrangeiras e `UNIQUE`, o banco garante com `CHECK` os status válidos, os valores dos pedidos (`total = subtotal + frete`), e-mails em minúsculas e preços promocionais coerentes. As colunas `updated_at` são mantidas por trigger.

Para ver as tabelas no navegador, suba o pgAdmin (perfil opcional):

```bash
docker compose --profile tools up -d
```

Acesse http://localhost:5050. O servidor **DeskForge** já aparece configurado; ao abrir, informe a senha do `DATABASE_USERNAME`. Todas as portas ficam restritas a `127.0.0.1`.

> O script que cria o usuário restrito só roda na primeira inicialização do volume. Se você já tinha subido o banco antes, recrie com `docker compose down -v`.

## Testes

```bash
cd backend
mvn verify
```

São 15 testes unitários (domínio de pedidos, regras de frete, JWT) e 12 de integração com PostgreSQL real via Testcontainers, cobrindo segurança, compra concorrente, devolução de estoque e webhooks do Stripe (assinatura, idempotência, expiração e estorno automático). É preciso ter o Docker rodando. O GitHub Actions executa os testes da API e o lint e o build do front a cada push.

## Mapa de conformidade

| Requisito | Onde está |
| --- | --- |
| Identificação do fornecedor (Decreto 7.962, art. 2º) | Rodapé e `GET /api/store` |
| Resumo do contrato antes da compra (art. 4º) | Resumo do checkout com frete, total e tributos |
| Atendimento em até 5 dias (art. 4º) | `/contato`, com protocolo e data limite |
| Direito de arrependimento (CDC, art. 49) | Cancelamento em Meus pedidos e `POST /api/orders/{id}/cancellation` |
| Transparência de tributos (Lei 12.741/2012) | Tributos aproximados no carrinho, no checkout e no pedido |
| Base legal e minimização (LGPD, arts. 6º e 7º) | Cadastro com nome, e-mail e senha. Política de Privacidade com tabela de finalidades |
| Direitos do titular (LGPD, art. 18) | `/lgpd`, exportação JSON e exclusão de conta |
| Consentimento de cookies | Banner com opções equivalentes de aceitar e recusar e registro em `cookie_consents` |
| Guarda de registros (Marco Civil, art. 15) | `AccessLogFilter` + expurgo após 6 meses |
| Acessibilidade (LBI, art. 63) | `/acessibilidade` e implementação WCAG 2.2 AA |
| Sem avaliações ou promessas falsas | Não há avaliações de clientes. Toda condição exibida é aplicada pelo sistema |
| Direitos autorais | Ilustrações e marca originais, fontes self-hosted e `/creditos` |

## Roadmap

- [x] **v1:** catálogo, carrinho, checkout com controle de concorrência, JWT, painel admin, páginas legais e LGPD
- [x] **v2:** Stripe Checkout em modo de teste com webhooks idempotentes
- [ ] **v3:** e-mails transacionais (confirmação, recuperação de senha) assíncronos com templates Thymeleaf
- [ ] **v4:** refresh token em cookie HttpOnly e limitação de tentativas de login
- [ ] **v5:** deploy público e observabilidade

## Licença

[MIT](LICENSE)
