# AI-Driven Smart Financial Wallet & Analytics Platform

A full-stack personal finance app: a **Spring Boot + Spring Security (JWT)** backend and a **React** frontend. Track income/expenses, set monthly budgets, view analytics dashboards, and get AI-style spending insights (anomaly detection, budget alerts, savings tips, next-month forecast) generated from a rule-based analytics engine.

## Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Security (JWT, stateless), Spring Data JPA, Bean Validation, H2 (dev) / PostgreSQL (prod), Lombok
- **Frontend:** React 18, React Router, Axios, Recharts, Vite

## Project Structure

```
financial-wallet-platform/
├── backend/     Spring Boot API (port 8080)
└── frontend/    React app (port 5173)
```

## Running the Backend

Requires Java 17+ and Maven.

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`, backed by an in-memory H2 database (data resets on restart). The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:walletdb`, user `sa`, no password).

Default profile is `dev`. To run against PostgreSQL, set `SPRING_PROFILES_ACTIVE=prod` and provide `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars.

Set a strong `JWT_SECRET` env var in any real deployment — the default in `application.yml` is for local development only.

## Running the Frontend

Requires Node.js 18+.

```bash
cd frontend
npm install
npm run dev
```

The app starts on `http://localhost:5173` and proxies `/api/*` calls to the backend at `localhost:8080` (see `vite.config.js`).

## Core Features

- **Auth:** Register / login with JWT, BCrypt password hashing, role-based access (`ROLE_USER`, `ROLE_ADMIN`)
- **Wallet:** Auto-created per user, balance updates automatically as transactions are added/updated/removed (optimistic locking)
- **Transactions:** Income/expense tracking with categories, merchants, dates — create, **edit**, and delete
- **Filtered transactions:** Server-side filtering by type, category, date range + pagination (`GET /api/transactions/filtered`)
- **Categories:** System-seeded defaults (Groceries, Rent, Salary, etc.) plus user-defined ones; protected deletes when in use
- **Budgets:** Monthly per-category limits with unique constraint per user/category/period, live spend tracking and progress bars
- **Analytics:** Category breakdown, 6-month income/expense trend, net savings trend (via Recharts)
- **AI Insights:** Rule-based engine that flags:
  - Category spending anomalies (vs. trailing 3-month average)
  - Budget overruns / near-limit warnings
  - Savings-rate tips
  - Next-month expense forecast (moving average)

## Robustness Improvements

- Optimistic locking (`@Version`) on Wallet to prevent concurrent balance corruption
- Unique constraint on budgets (`user + category + period`)
- Indexes on high-traffic transaction columns
- Safer JWT filter (invalid/expired tokens clear the security context instead of throwing)
- Stronger validation (amount > 0, period `YYYY-MM`, field size limits, future-date guard)
- Category ownership checks and “in-use” protection before delete
- Transaction update properly reverses old balance impact then applies the new one
- Global exception handler covers validation, conflict, forbidden, optimistic lock, and generic errors without leaking internals
- Frontend: consistent error extraction, loading states, edit flow, confirm-on-delete

## Extending the AI Insights with a Real LLM

`AIInsightService` is structured so the same aggregated data (category spend, budget status, trends) can be turned into a prompt and sent to an LLM (e.g. the Anthropic API) for richer natural-language narratives. Add a REST client bean, build the prompt from the same computations already in the service, and merge the model's output into the returned insight list — no other layer needs to change.

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Get a JWT |
| GET | `/api/wallet/me` | Current user's wallet |
| GET/POST | `/api/categories` | List / create categories |
| DELETE | `/api/categories/{id}` | Delete a user category |
| GET | `/api/transactions` | List all transactions |
| GET | `/api/transactions/filtered` | Filtered + paginated list (`type`, `categoryId`, `startDate`, `endDate`, `page`, `size`) |
| POST | `/api/transactions` | Create a transaction |
| PUT | `/api/transactions/{id}` | Update a transaction |
| DELETE | `/api/transactions/{id}` | Delete a transaction |
| GET/POST | `/api/budgets?period=YYYY-MM` | List / create-update budgets |
| DELETE | `/api/budgets/{id}` | Delete a budget |
| GET | `/api/analytics/summary` | Aggregated analytics |
| GET | `/api/insights` | AI-generated insights |

All endpoints except `/api/auth/**` require `Authorization: Bearer <token>`.

## Security Notes for Production

- Replace the default `jwt.secret` with a securely generated value passed via environment variable.
- Switch to the `prod` Spring profile with PostgreSQL.
- Tighten CORS (`app.cors.allowed-origins`) to your real frontend origin.
- Put the API behind HTTPS.
- Consider rate limiting and account lockout for brute-force protection.
