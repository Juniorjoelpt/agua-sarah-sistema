# Água Sarah — Sistema de gestão

Sistema completo de PDV, estoque, frota e financeiro para a Água Sarah (envasadora de água mineral direto da fonte).

```
agua-sarah-sistema/
├── backend/    Spring Boot 3 (Java 17) + MySQL + JWT — API REST
└── frontend/   React + Vite + Tailwind — interface conectada à API
```

## Como rodar os dois juntos

**1. Backend**

```
cd backend
mvn clean install
mvn spring-boot:run
```

Sobe em `http://localhost:8080`. Na primeira execução, cria o usuário admin (`admin` / `admin123`, ver `backend/README.md`).

**2. Frontend**

```
cd frontend
npm install
cp .env.example .env
npm run dev
```

Sobe em `http://localhost:5173` e já aponta para `http://localhost:8080/api` por padrão (ajustável em `.env`).

## Documentação de cada parte

- `backend/README.md` — endpoints por módulo, regras de negócio já embutidas no código, variáveis de configuração
- `frontend/README.md` — estrutura das páginas, como a autenticação funciona, pontos de atenção da integração

## Decisões de escopo (para não esquecer)

- **Sem módulo de nota fiscal** — a emissão fiscal (NF-e/NFC-e) fica em sistema separado; aqui só existe o registro da venda.
- **Bonificação por avaria de produção** é sempre em **galões**, nunca em dinheiro — o valor é calculado, não digitado.
- **Motorista ↔ Caminhão** é uma relação fixa.
- **Módulo de compra de terceiros** é isolado (caixa próprio) e restrito a usuários `ADMIN`, com a proteção reforçada tanto no backend quanto refletida no frontend.
