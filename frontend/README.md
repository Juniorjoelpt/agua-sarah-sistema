# Água Sarah — Frontend

Frontend real do sistema, em React + Vite + Tailwind, conectado ao [backend Spring Boot](../agua-sarah-backend) já validado.

## Como rodar

1. `npm install`
2. Copie `.env.example` para `.env` e ajuste `VITE_API_URL` se o backend não estiver em `http://localhost:8080/api`
3. `npm run dev` — abre em `http://localhost:5173`
4. Faça login com o usuário criado pelo `DataSeeder` do backend (`admin` / `admin123`, ou o que você configurou)

`npm run build` gera a versão de produção em `dist/` (já validado que compila sem erros neste ambiente).

## Estrutura

- `src/api/` — um arquivo por módulo do backend (`clientes.js`, `vendas.js`, `frota.js`...), espelhando 1:1 os endpoints REST
- `src/context/AuthContext.jsx` — login, logout, token e usuário logado (guardados em `localStorage`)
- `src/components/` — Sidebar, Layout, rotas protegidas (`ProtectedRoute`, `AdminRoute`) e a biblioteca de UI (`ui.jsx`) usada em todas as telas
- `src/pages/` — uma página por módulo, todas puxando dados reais da API (sem mocks)
- `src/theme.js` — cores, fontes e identidade visual da marca (Água Sarah)

## Pontos de atenção já resolvidos no código

- **Perfis**: `Usuários` e `Compra de terceiros` só aparecem no menu e só são acessíveis (via `AdminRoute`) para quem loga como `ADMIN` — mas a proteção de verdade está no backend (`hasRole("ADMIN")` + `@PreAuthorize`); o frontend só evita mostrar uma tela que a API vai recusar.
- **Sessão expirada**: qualquer resposta 401 da API já limpa o token e redireciona para `/login` automaticamente (`src/api/client.js`).
- **Prestação de contas** busca sozinha o recibo pendente mais recente do caminhão ao abrir o formulário, para o operador não precisar saber o ID manualmente.
- **Nota fiscal**: não existe tela nem chamada de API para isso, propositalmente — fica no sistema fiscal separado, como decidido.

## Próximos passos sugeridos

- Rodar o backend localmente e testar o fluxo completo ponta a ponta (abrir caixa → vender → fechar caixa → relatório).
- Ajustar mensagens de erro específicas caso o backend retorne validações diferentes das esperadas.
- Adicionar paginação nas listagens (Clientes, Produtos, Despesas) quando o volume de dados crescer.
