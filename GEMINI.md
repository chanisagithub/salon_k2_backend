# 2KCUT Saloon API - Project Documentation

## Project Overview
2KCUT is a barber shop management system built with Spring Boot. It provides a RESTful API for managing appointments, barbers, services, and user profiles.

### Core Technologies
- **Java 21**: The primary programming language.
- **Spring Boot 4.0.6**: Backend framework (Note: local versioning used).
- **Spring Data JPA**: For database ORM and repository management.
- **PostgreSQL**: The relational database.
- **Flyway**: Database migration tool.
- **Spring Security (OAuth2 Resource Server)**: JWT-based security integrated with **Keycloak**.
- **Lombok**: To reduce boilerplate code.

### Architecture
The project follows a standard layered architecture:
- **Web Layer (`com.unique.k2cut.web.rest`)**: REST controllers for handling HTTP requests.
- **Domain Layer (`com.unique.k2cut.domain.entity`)**: JPA entities representing the database schema.
- **Repository Layer (`com.unique.k2cut.repository`)**: Interfaces for database access (JPA Repositories).
- **Configuration (`com.unique.k2cut.config`)**: Security filters and data source settings.

---

## Building and Running

### Prerequisites
- Java 21 installed.
- Docker (for PostgreSQL and Keycloak).

### Key Commands
- **Run Application**: `./mvnw spring-boot:run`
- **Run Tests**: `./mvnw test`
- **Clean and Build**: `./mvnw clean install`
- **Database Migrations**: Automatically handled by Flyway on startup.

### Configuration
- **Application Port**: `7080` (Configured in `application.properties`).
- **PostgreSQL**: Expected on `localhost:5432` (or via Docker).
- **Keycloak**: Expected on `localhost:9090` with realm `k2cut`.

---

## Development Conventions

### API Versioning
All REST endpoints are prefixed with `/api/v1/`.
- Public endpoints: `/api/v1/public/**`
- Admin endpoints: `/api/v1/admin/**` (Requires `ROLE_ADMIN`)

### Security
The application is configured as an OAuth2 Resource Server. Requests must include a valid JWT Bearer token in the `Authorization` header.

### Database Migrations
New database changes must be added as SQL migration files in `src/main/resources/db/migration/` following the `V<Version>__<Description>.sql` naming convention.

### Coding Style
- Use **Lombok** annotations (`@Getter`, `@Setter`, `@NoArgsConstructor`, etc.) for entities and DTOs.
- Use **Jakarta Persistence** annotations for database mapping.
- Prefer `OffsetDateTime` for timestamp fields.
- Entities should use `UUID` for primary keys.
