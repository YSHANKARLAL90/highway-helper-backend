# Highway Helper Backend

Production-ready Spring Boot backend for **Highway Helper** — a platform connecting stranded drivers on highways with nearby mechanics, puncture shops, towing services, and emergency assistance providers.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3 |
| Build | Maven |
| Database | MySQL 8 |
| ORM | Spring Data JPA |
| Security | Spring Security + JWT |
| Migrations | Flyway |
| Mapping | MapStruct + Lombok |
| API Docs | OpenAPI 3 / Swagger UI |
| Container | Docker + Docker Compose |

## Architecture

Clean layered architecture under `com.highwayhelper`:

```
controller/   → REST endpoints, validation, Swagger annotations
service/      → Business logic and transaction boundaries
repository/   → Spring Data JPA repositories
entity/       → JPA entities and enums
dto/          → Request/response DTOs (never expose entities)
mapper/       → MapStruct entity ↔ DTO mappers
exception/    → Custom exceptions + @RestControllerAdvice
security/     → JWT filter, UserDetails, SecurityUtils
config/       → Security, JPA auditing, OpenAPI
```

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- MySQL 8 (or use Docker Compose)

### Run with Docker Compose

```bash
docker compose up --build
```

API: `http://localhost:8080/api`  
Swagger UI: `http://localhost:8080/api/swagger-ui.html`

### Run Locally

1. Start MySQL and create database `highway_helper`
2. Configure credentials in `application-dev.yml` or via env vars:

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=highway_helper
export DB_USERNAME=root
export DB_PASSWORD=root
export JWT_SECRET=your-256-bit-secret-key-here
```

3. Build and run:

```bash
mvn clean spring-boot:run
```

## API Overview

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | Public | Register user/mechanic account |
| POST | `/auth/login` | Public | Login, receive JWT |
| GET | `/auth/me` | JWT | Current user profile |
| POST | `/mechanics/register` | USER/MECHANIC | Create mechanic profile |
| GET | `/mechanics` | Public | List verified active mechanics |
| GET | `/mechanics/{id}` | Public | Mechanic details |
| POST | `/service-requests` | USER | Create assistance request |
| PATCH | `/service-requests/{id}/assign` | USER/ADMIN | Assign mechanic |
| PATCH | `/service-requests/{id}/status` | USER/MECHANIC/ADMIN | Update status |
| GET | `/admin/dashboard` | ADMIN | Platform statistics |
| PATCH | `/admin/mechanics/{id}/verify` | ADMIN | Verify mechanic |

### Service Request Status Flow

```
PENDING → ACCEPTED → ON_THE_WAY → COMPLETED
    ↓         ↓           ↓
 CANCELLED  CANCELLED   CANCELLED
```

## Roles

- **USER** — Vehicle owners who create service requests
- **MECHANIC** — Service providers (must be admin-verified to become ACTIVE)
- **ADMIN** — Platform operators (verify mechanics, view dashboard)

> Admin accounts cannot be self-registered via the public API. Create the first admin directly in the database or through a secure internal process.

## Testing

```bash
mvn test
```

Unit tests activate the `uat` profile; `src/test/resources/application-uat.yml` overrides the datasource with H2 in-memory for fast runs.

## Project Structure

```
src/main/java/com/highwayhelper/
├── HighwayHelperApplication.java
├── config/
├── controller/
├── dto/request/ & dto/response/
├── entity/ & entity/enums/
├── exception/
├── mapper/
├── repository/
├── security/
└── service/

src/main/resources/
├── application.yml
├── application-dev.yml
├── application-uat.yml
├── application-prod.yml
└── db/migration/V1__init_schema.sql
```

## License

Proprietary — Highway Helper © 2026
