# Bank Transaction System

A full-stack personal banking web application with a Spring Boot UI and an optional Python analytics service. Users manage transactions, transfer funds, and review spending; administrators can credit salary and performance incentives to any account.

## Features

### Authentication & security
- Form-based login with BCrypt password hashing (Spring Security)
- Dedicated **login error page** (`/login-error`) when credentials are invalid, with a link back to sign in
- Successful logout shows a confirmation message on the login page
- Role-based access: **Administrator** and **Employee**
- 2-minute session timeout with a dedicated session-expired page
- Sensitive credentials blocked from GET query strings (username/password never accepted in URLs)

### Banking (all users)
- **Home dashboard** — quick access to history, security, spending, and administration *(admin only)*
- **Transaction History** — view debits and credits on your account
- **Manage Transactions** — create, edit, and delete your own debit records
- **Bank Account Summary** — opening balance, total spending, and amount left
- **Online Transfer** — send money to another user with balance validation *(via top navigation)*
- **Secure Access** — account and session details
- **Track Spending** — totals, averages, and top expenses

### Administration (admin user only)
- **Credit Salary** — post a salary credit to any user on a chosen date
- **Performance Incentive** — reward users by rating tier (Outstanding, Excellent, Good, Satisfactory) with suggested amounts
- Credits can be issued to employees or to the administrator’s own account
- Credits appear instantly on the recipient’s transaction history
- Administrative credits cannot be deleted by recipients

### Analytics (optional)
- Python FastAPI service for reports, charts, and CSV export against the same MySQL database

## Tech Stack

| Layer | Technologies |
|-------|--------------|
| Web App | Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, Thymeleaf, Bean Validation |
| Analytics | Python 3.12, FastAPI, Pandas, SQLAlchemy, Matplotlib |
| Database | MySQL 8.4 |
| Deployment | Docker, Docker Compose |

## Quick Start (Docker)

**Requirements:** Docker and Docker Compose

```bash
git clone https://github.com/sharanyashwant27-tech/BankTransactionSystem.git
cd BankTransactionSystem
docker compose up --build -d
```

| Service | URL |
|---------|-----|
| Login | http://localhost:8083/login |
| Login error | http://localhost:8083/login-error |
| Home | http://localhost:8083/home |
| Transactions | http://localhost:8083/transactions |
| Administration | http://localhost:8083/admin *(admin only)* |
| Analytics API docs | http://localhost:8090/docs |

**Stop services:**

```bash
docker compose down
```

**View logs:**

```bash
docker compose logs -f bank-app
```

**Rebuild after code changes:**

```bash
docker compose up --build -d bank-app
```

### First-time walkthrough

1. Open http://localhost:8083/login
2. Sign in as `admin` / `admin123` (administrator) or `admin1` / `admin123` (employee)
3. Use the top navigation to open **History**, **Transfer**, **Security**, or **Spending**
4. On the transactions page, switch tabs for **History**, **Manage Transactions**, **Bank Account Summary**, and **Online Transfer**
5. As `admin`, open **Administration** to credit salary or performance incentives

## Login Credentials

Docker sets seed passwords via environment variables (defaults shown below).

| Variable | Default | Purpose |
|----------|---------|---------|
| `APP_SEED_ADMIN_PASSWORD` | `admin123` | Password for the primary admin account |
| `APP_SEED_DEFAULT_PASSWORD` | `admin123` | Password for all employee demo accounts |
| `APP_SEED_ADMIN_USERNAME` | `admin` | Primary administrator username |

### Administrator

| Field | Value |
|-------|-------|
| **Username** | `admin` |
| **Password** | `admin123` |
| **Email** | `admin@bank.com` |
| **Role** | Administrator |

Use this account to access **Administration** and credit salary or performance incentives to any user (including yourself).

### Employee demo accounts

All employee accounts use password **`admin123`**.

| Username | Email | Role |
|----------|-------|------|
| admin1 | admin1@bank.com | Employee |
| admin2 | admin2@bank.com | Employee |
| admin3 | admin3@bank.com | Employee |
| admin4 | admin4@bank.com | Employee |
| admin5 | admin5@bank.com | Employee |

Users and sample transactions are seeded automatically on startup by `DataInitializer`.

### Authentication flow

| Event | Result |
|-------|--------|
| Valid login | Redirect to `/home` |
| Invalid login | Redirect to `/login-error` with “not a valid user” message and **Return to Login** link |
| Logout | Redirect to `/login` with a success message |
| Session timeout (2 min idle) | Redirect to `/session-expired`, then **Return to Login** |

## Web Application Pages

| Path | Description |
|------|-------------|
| `/login` | Sign in |
| `/login-error` | Shown when authentication fails |
| `/home` | Dashboard |
| `/transactions?tab=history` | Transaction history |
| `/transactions?tab=manage` | Create / edit / delete transactions |
| `/transactions?tab=summary` | Account summary |
| `/transactions?tab=transfer` | Online transfer |
| `/security` | Account protection details |
| `/spending` | Spending analysis |
| `/admin` | Salary and incentive credits *(admin only)* |
| `/session-expired` | Shown after session timeout |
| `/return-to-login` | Clears session and returns to login |

Top navigation provides **Home**, **History**, **Transfer**, **Security**, **Spending**, and **Administration** (admin only). Use these links to move between pages — there are no separate back-to-home buttons on child pages.

### Transaction types

| Type | Effect on balance |
|------|-------------------|
| **DEBIT** | Reduces available balance (user spending, outgoing transfers) |
| **CREDIT** | Increases available balance (incoming transfers, salary, incentives) |

Transactions with a missing type are treated as **DEBIT**. Account summary totals in `TransactionService` use null-safe stream filters when aggregating debits and credits.

**Balance formula:** `opening balance − debits + credits`

## Performance Incentive Tiers

| Rating | Suggested amount |
|--------|------------------|
| Outstanding | $1,500 |
| Excellent | $1,000 |
| Good | $500 |
| Satisfactory | $250 |

Amounts can be adjusted before submitting.

## Analytics API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Service health check |
| GET | `/report` | Service status message |
| GET | `/analytics/monthly-report` | Monthly transaction report |
| GET | `/analytics/spending-analysis` | Spending analysis |
| GET | `/analytics/charts/monthly-spending` | Monthly spending chart (PNG) |
| GET | `/analytics/charts/spending-by-user` | Spending by user chart (PNG) |
| GET | `/analytics/export/monthly-csv` | CSV export download |

## Local Development (without Docker)

### Prerequisites

- Java 21
- Maven 3.9+
- Python 3.12+ *(analytics service only)*
- MySQL 8.x on `localhost:3306`

### Database

```sql
CREATE DATABASE bankdb;
```

Update `src/main/resources/application.properties` if needed:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bankdb
spring.datasource.username=root
spring.datasource.password=password
server.port=8083
server.servlet.session.timeout=2m
```

Set seed passwords so demo users are created on startup:

```properties
app.seed.admin-password=admin123
app.seed.default-password=admin123
```

Or export environment variables:

```bash
set APP_SEED_ADMIN_PASSWORD=admin123
set APP_SEED_DEFAULT_PASSWORD=admin123
```

### Run Spring Boot

```bash
mvn clean package -DskipTests
java -jar target/bank-transaction-system-0.0.1-SNAPSHOT.jar
```

### Run Analytics Service

```bash
cd analytics-service
pip install -r requirements.txt
python run.py
```

## Project Structure

```
BankTransactionSystem/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── src/main/java/com/bank/
│   ├── BankApplication.java
│   ├── config/           # Data seeding, nav model advice, password config
│   ├── controller/       # Login, dashboard, transactions, admin
│   ├── dto/              # Forms and account summary DTOs
│   ├── entity/           # User, Transaction (roles, DEBIT/CREDIT types)
│   ├── repository/       # Spring Data JPA repositories
│   ├── security/         # Security config, login handlers, query param filter
│   └── service/          # User, transaction, and admin business logic
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/       # Application styles
│   └── templates/
│       ├── admin.html
│       ├── home.html
│       ├── login.html
│       ├── login-error.html
│       ├── session-expired.html
│       ├── security.html
│       ├── spending.html
│       ├── transactions.html
│       └── fragments/nav.html
└── analytics-service/
    ├── Dockerfile
    ├── main.py
    └── app/              # FastAPI routes and analytics logic
```

## Docker Services

| Container | Port | Description |
|-----------|------|-------------|
| bank-app | 8083 | Spring Boot web application |
| bank-analytics | 8090 | Python analytics API |
| bank-mysql | 3306 *(internal)* | MySQL database |

## Troubleshooting

| Issue | What to try |
|-------|-------------|
| Login fails | Confirm username/password (`admin` / `admin123`). You will land on `/login-error`. |
| Session expired quickly | Idle timeout is 2 minutes. Sign in again from `/login` or `/return-to-login`. |
| Changes not visible | Rebuild: `docker compose up --build -d bank-app`, then hard refresh the browser. |
| No demo users | Ensure `APP_SEED_ADMIN_PASSWORD` and `APP_SEED_DEFAULT_PASSWORD` are set (Docker sets both to `admin123` by default). |

## License

This project is for educational purposes.
