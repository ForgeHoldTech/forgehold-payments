# ForgeHold Digital Payments Platform

A Spring Boot backend for managing customers, digital wallets, deposits, withdrawals and wallet-to-wallet transfers, secured with JWT authentication.

## Features

- User registration and login with JWT-based authentication
- Role-based access (Spring Security)
- Customer management (create, read, update, delete)
- Digital wallets per customer, with support for multiple currencies
- Wallet deposits and withdrawals with running balance tracking
- Wallet-to-wallet money transfers
- Paginated transaction history per wallet
- Centralized exception handling with clean, consistent error responses

## Tech Stack

- Java 21
- Spring Boot
- Spring Security + JWT (jjwt)
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- JUnit (unit and integration tests)

## Getting Started

### Prerequisites

- Java 21
- Maven (or use the included `mvnw` wrapper)
- PostgreSQL running locally

### Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE forgehold_db;
```

### Configuration

Set the following environment variables before running the app:

| Variable | Description |
|---|---|
| `FORGEHOLD_DB_PASSWORD` | PostgreSQL password for the configured user |
| `FORGEHOLD_JWT_SECRET` | Secret key used to sign JWT tokens |

### Run the Application

```bash
./mvnw spring-boot:run
```

The API will start on `http://localhost:8080`.

### Run Tests

```bash
./mvnw test
```

## API Overview

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Log in and receive a JWT token |

### Customers

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/customers` | Create a customer |
| GET | `/api/customers` | List all customers |
| GET | `/api/customers/{id}` | Get a customer by ID |
| PUT | `/api/customers/{id}` | Update a customer |
| DELETE | `/api/customers/{id}` | Delete a customer |

### Wallets

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/wallets/customer/{customerId}` | Create a wallet for a customer |
| GET | `/api/wallets/customer/{customerId}` | List a customer's wallets |

### Wallet Transactions

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions/wallet/{walletId}/deposit` | Deposit into a wallet |
| POST | `/api/transactions/wallet/{walletId}/withdraw` | Withdraw from a wallet |
| GET | `/api/transactions/wallet/{walletId}` | Paginated transaction history |

### Transfers

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/transactions/transfer` | Transfer funds between two wallets |

All endpoints except `/api/auth/register` and `/api/auth/login` require a valid JWT, sent as:

```
Authorization: Bearer <token>
```

## Author

Usman Ahmad — [GitHub](https://github.com/ForgeHoldTech) · [Portfolio](https://forgeholdtech.github.io)
