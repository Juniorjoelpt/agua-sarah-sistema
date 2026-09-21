# Água Sarah — Backend

Backend do sistema de gestão da Água Sarah: Spring Boot 3 (Java 17) + MySQL + Spring Security com JWT.

## Como rodar

1. **Banco de dados**: crie um banco MySQL local (ou deixe o `createDatabaseIfNotExist=true` do `application.yml` criar sozinho). Ajuste usuário/senha em `src/main/resources/application.yml` se não forem `root`/`root`.
2. **Build**: `mvn clean install` (ou abra o projeto no NetBeans/IntelliJ como projeto Maven existente).
3. **Rodar**: `mvn spring-boot:run`, ou execute `AguaSarahBackendApplication`.
4. Na primeira subida, o `DataSeeder` cria automaticamente o usuário administrador:
   - login: `admin`
   - senha: `admin123`
   (login/senha configuráveis em `application.yml`, chave `app.seed`)

## Autenticação

`POST /api/auth/login` com `{ "login": "admin", "senha": "admin123" }` devolve um JWT.
Envie esse token em todas as demais chamadas: `Authorization: Bearer <token>`.

## Módulos e principais endpoints

| Módulo | Endpoints |
|---|---|
| Autenticação | `POST /api/auth/login` |
| Usuários (ADMIN) | `/api/usuarios` |
| Clientes | `/api/clientes` |
| Produtos | `/api/produtos` |
| Caixa | `POST /api/caixa/abrir`, `POST /api/caixa/{id}/fechar`, `GET /api/caixa/atual`, `GET /api/caixa/{id}/resumo` |
| Vendas | `POST /api/vendas`, `GET /api/vendas/caixa/{caixaId}` |
| Estoque | `/api/estoque/insumos`, `POST /api/estoque/movimentacoes` |
| Despesas | `/api/despesas` |
| Frota | `/api/frota/caminhoes`, `POST /api/frota/recibos`, `POST /api/frota/prestacoes-contas` |
| Relatórios | `/api/relatorios/despesas`, `/api/relatorios/estoque`, `/api/relatorios/envase` |
| Compra de terceiros (ADMIN) | `/api/terceiros/**` |

## Decisões importantes já embutidas no código

- **Bonificação por avaria de produção** é sempre em **quantidade de galões** (digitada manualmente), nunca em valor — o valor é calculado (`galões × preço do produto de envase`).
- **Motorista ↔ Caminhão** é uma relação fixa (`@OneToOne`) — cada motorista sempre dirige o mesmo caminhão.
- **Rota fixa** usa `ClienteRotaFixa` (sem recibo). **Rota variável** usa `Recibo` → `PrestacaoContas`.
- **Módulo de terceiros** é isolado: caixa próprio (`CaixaTerceiro`), sem nenhuma referência às entidades `Caixa`/`Venda` do restante do sistema, e protegido em dois níveis — no `SecurityConfig` (`/api/terceiros/** → hasRole("ADMIN")`) e via `@PreAuthorize` no controller.
- **Nota fiscal**: propositalmente não existe no backend — é responsabilidade de um sistema fiscal separado, fora do escopo deste projeto.

## Próximos passos sugeridos

- Testar os fluxos completos (idealmente com o Postman/Insomnia) antes de plugar no frontend React já criado.
- Ajustar `app.jwt.secret` para uma chave forte antes de qualquer ambiente real.
- Adicionar testes automatizados dos services (regras de negócio de caixa, venda e frota são as mais sensíveis).
