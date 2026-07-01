# Bank Transaction System

A full-stack bank transaction management application with a Spring Boot web UI and a Python analytics enhancement layer.

## Features

- User authentication with Spring Security
- Personal transaction history per logged-in user
- MySQL database with JPA/Hibernate
- Thymeleaf UI (login, home, transactions)
- Python FastAPI analytics service (reports, charts, CSV export)
- Docker Compose deployment for any machine

## Tech Stack

| Layer | Technologies |
|-------|--------------|
| Backend | Java 21, Spring Boot 3.x, Spring Security, Spring Data JPA, Thymeleaf |
| Analytics | Python 3.12, FastAPI, Pandas, SQLAlchemy, Matplotlib |
| Database | MySQL 8.4 |
| Build | Maven, Docker, Docker Compose |

## Quick Start (Docker)

**Requirements:** Docker and Docker Compose

```bash
git clone https://github.com/sharanyashwant27-tech/BankTransactionSystem.git
cd BankTransactionSystem
docker compose up --build -d
```

| Service | URL |
|---------|-----|
| Bank App (Login) | http://localhost:8083/login |
| Home Dashboard | http://localhost:8083/home |
| Transactions | http://localhost:8083/transactions |
| Analytics API | http://localhost:8090/docs |
| Analytics Report | http://localhost:8090/report |

**Stop services:**

```bash
docker compose down
```

**View logs:**

```bash
docker compose logs -f
```

## Login Credentials

All users share the password **`admin123`**.

| Username | Email |
|----------|-------|
| admin | admin@bank.com |
| admin1 | admin1@bank.com |
| admin2 | admin2@bank.com |
| admin3 | admin3@bank.com |
| admin4 | admin4@bank.com |
| admin5 | admin5@bank.com |

Users and sample transactions are seeded automatically on first startup via `DataInitializer`.

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
- Python 3.12+
- MySQL 8.x running on `localhost:3306`

### Database

```sql
CREATE DATABASE bankdb;
```

Update credentials in `src/main/resources/application.properties` if needed:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bankdb
spring.datasource.username=root
spring.datasource.password=password
server.port=8083
```

### Run Spring Boot App

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

Or:

```bash
uvicorn main:app --reload --port 8090
```

## Project Structure

```
BankTransactionSystem/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── src/main/java/com/bank/
│   ├── BankApplication.java
│   ├── config/          # Password & data seeding
│   ├── controller/      # Login & transaction controllers
│   ├── entity/          # User, Transaction JPA entities
│   ├── repository/      # Spring Data JPA repositories
│   ├── security/        # Spring Security config
│   └── service/         # Business logic
├── src/main/resources/
│   ├── application.properties
│   ├── static/css/      # Stylesheets
│   └── templates/       # Thymeleaf HTML pages
└── analytics-service/
    ├── Dockerfile
    ├── main.py          # Uvicorn entry point
    ├── run.py
    └── app/
        ├── main.py      # FastAPI routes
        ├── analytics_service.py
        ├── models.py
        └── database.py
```

## Docker Services

| Container | Port | Description |
|-----------|------|-------------|
| bank-app | 8083 | Spring Boot web application |
| bank-analytics | 8090 | Python analytics API |
| bank-mysql | 3306 (internal) | MySQL database |

## License

This project is for educational purposes.
