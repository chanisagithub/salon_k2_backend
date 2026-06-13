# 2KCUT Salon Backend API

Spring Boot REST API for barber-shop booking and appointment management, with JWT auth (OAuth2 resource server), PostgreSQL persistence, and Flyway migrations.

## Tech Stack and How Components Connect

| Layer | Tech | Purpose |
| --- | --- | --- |
| Runtime | Java 21 + Spring Boot 4.0.6 | Main application runtime and dependency wiring |
| API | Spring Web MVC | REST endpoints under `/api/v1/*` |
| Security | Spring Security + OAuth2 Resource Server | Validates JWTs from an external IdP (Keycloak/Auth0/etc.) |
| Identity Provider | Keycloak (recommended locally) | Issues JWT access tokens used by this API |
| Data Access | Spring Data JPA + Hibernate | ORM and repository access |
| Database | PostgreSQL | Persistent storage for users, barbers, services, schedules, appointments |
| Migrations | Flyway | Versioned SQL migrations in `src/main/resources/db/migration` |
| Build/Test | Maven Wrapper (`./mvnw`) | Build lifecycle and test execution |
| Utilities | Lombok | Reduces Java boilerplate in entities/services |

## Architecture Overview

- **Web layer**: `com.unique.k2cut.web.rest`
  - `BookingController` (`/api/v1/booking/*`)
  - `PublicController` (`/api/v1/public/*`)
  - `AdminController`, `AdminServiceController`, `AdminBarberController` (`/api/v1/admin/*`)
- **Service layer**: booking and domain rules (`service/*`)
- **Persistence layer**: entities (`domain/entity`) + repositories (`repository/*`)
- **Config layer**: security + JPA config (`config/*`)

## Security Model

- App runs as a **stateless** OAuth2 resource server.
- JWTs are validated using:
  - `JWT_ISSUER_URI`
  - `JWT_JWK_SET_URI`
- Role mapping expects Keycloak-style claims:
  - `realm_access.roles` -> converted to authorities like `ROLE_ADMIN`.
- Route access:
  - Public: `/api/v1/public/**`
  - Public booking read endpoints: `/api/v1/booking/services`, `/api/v1/booking/barbers`, `/api/v1/booking/slots`
  - Admin-only: `/api/v1/admin/**` (`ROLE_ADMIN`)
  - Other booking endpoints require authentication.
- Booking flow expects JWT `sub` to be a UUID that maps to local `users.id`.

## Prerequisites

1. Java 21
2. PostgreSQL 14+ (or compatible)
3. Keycloak (or another OpenID Connect provider issuing compatible JWT claims)
4. Docker + Docker Compose (optional, for containerized local/prod setup)

## Environment Variables

These are read by `application.properties`:

| Variable | Default | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/k2cut` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `12345` | DB password |
| `JWT_ISSUER_URI` | `http://localhost:9090/realms/master` | JWT issuer URI |
| `JWT_JWK_SET_URI` | `http://localhost:9090/realms/master/protocol/openid-connect/certs` | JWK URI |

> Keep issuer/JWK values aligned to the same Keycloak realm (for example, both under `/realms/k2cut` in production).

## Local Development Setup

### 1. Clone and enter project

```bash
git clone https://github.com/chanisagithub/salon_k2_backend.git
cd salon_k2_backend
```

### 2. Start PostgreSQL

You can use your own Postgres or Docker:

```bash
docker run --name k2cut-postgres \
  -e POSTGRES_DB=k2cut \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=12345 \
  -p 5432:5432 \
  -d postgres:16
```

### 3. Ensure UUID support function is available

Migration `V1__Initial_Schema.sql` uses `gen_random_uuid()`. Ensure extension exists:

```sql
CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

### 4. Start Keycloak (local example)

```bash
docker run --name k2cut-keycloak \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -p 9090:8080 \
  -d quay.io/keycloak/keycloak:26.0 \
  start-dev
```

Then create:
- Realm (for example `k2cut`)
- Client for this API
- Users
- Admin role assignment (`admin`) to users who need `/api/v1/admin/**`

### 5. Configure environment

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/k2cut
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=12345
export JWT_ISSUER_URI=http://localhost:9090/realms/k2cut
export JWT_JWK_SET_URI=http://localhost:9090/realms/k2cut/protocol/openid-connect/certs
```

### 6. Run the API

```bash
./mvnw spring-boot:run
```

API starts on `http://localhost:7080`.

## Dockerized Setup

This repository now includes:

- `Dockerfile` (multi-stage build, Java 21 runtime image)
- `docker-compose.yml` (app + PostgreSQL + Keycloak)
- `docker/postgres/init/01-pgcrypto.sql` (auto-enables `pgcrypto`)

### Run complete stack with Docker Compose

```bash
docker compose up --build -d
```

Services:
- API: `http://localhost:7080`
- PostgreSQL: `localhost:5432`
- Keycloak: `http://localhost:9090` (admin/admin)

Stop stack:

```bash
docker compose down
```

Stop and remove Postgres volume data:

```bash
docker compose down -v
```

> `docker compose` uses Keycloak realm `master` by default in this setup. Create your API client/users/roles there, or edit `JWT_ISSUER_URI` and `JWT_JWK_SET_URI` in `docker-compose.yml` to match your realm.

### Build and run only the API container

Use this when DB/IdP are managed externally.

```bash
docker build -t k2cut-api:latest .
docker run --name k2cut-api \
  -p 7080:7080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/k2cut \
  -e SPRING_DATASOURCE_USERNAME=<db-user> \
  -e SPRING_DATASOURCE_PASSWORD=<db-password> \
  -e JWT_ISSUER_URI=https://<idp-domain>/realms/<realm> \
  -e JWT_JWK_SET_URI=https://<idp-domain>/realms/<realm>/protocol/openid-connect/certs \
  -d k2cut-api:latest
```

## Build and Test

```bash
./mvnw clean install
./mvnw test
./mvnw -Dtest=K2cutApplicationTests test
./mvnw -Dtest=K2cutApplicationTests#contextLoads test
```

> Tests use the configured datasource; make sure PostgreSQL is reachable before running tests.

## API Endpoints

### Public

- `GET /api/v1/public/health`
- `GET /api/v1/booking/services`
- `GET /api/v1/booking/barbers`
- `GET /api/v1/booking/slots?barberId=<uuid>&serviceId=<uuid>&date=YYYY-MM-DD`

### Authenticated Booking

- `POST /api/v1/booking/appointments`
- `GET /api/v1/booking/my-appointments`
- `DELETE /api/v1/booking/appointments/{id}`

### Admin (`ROLE_ADMIN`)

Appointments:
- `GET /api/v1/admin/test`
- `GET /api/v1/admin/appointments`
- `PATCH /api/v1/admin/appointments/{id}/status?status=PENDING|CONFIRMED|CANCELLED|COMPLETED`

Service management:
- `GET /api/v1/admin/services` (includes inactive)
- `GET /api/v1/admin/services/{id}`
- `POST /api/v1/admin/services`
- `PUT /api/v1/admin/services/{id}`
- `DELETE /api/v1/admin/services/{id}` (soft-delete / deactivate)

Barber & schedule management:
- `GET /api/v1/admin/barbers` (includes inactive)
- `POST /api/v1/admin/barbers`
- `PUT /api/v1/admin/barbers/{id}`
- `DELETE /api/v1/admin/barbers/{id}` (soft-delete / deactivate)
- `GET /api/v1/admin/barbers/{id}/schedules`
- `PUT /api/v1/admin/barbers/{id}/schedules` (replaces the full weekly schedule)

> Migration `V2__Seed_Data.sql` seeds a starter catalogue (services, barbers, schedules) so the booking flow works immediately on a fresh database.

### Error responses

All errors return a consistent JSON body via the global exception handler:

```json
{ "timestamp": "...", "status": 400, "error": "Bad Request", "message": "Barber is already booked at this time" }
```

Validation failures additionally include a per-field `errors` map.

## Deployment Guide (Server)

## 1. Provision Infrastructure

At minimum:
1. Linux host (Ubuntu/Debian/CentOS)
2. Java 21 runtime
3. PostgreSQL instance (managed or self-hosted)
4. Identity provider (Keycloak/Auth provider)
5. Reverse proxy + TLS (Nginx recommended)

## 2. Prepare Production Database

1. Create DB/user with least privilege.
2. Enable required extension:
   ```sql
   CREATE EXTENSION IF NOT EXISTS pgcrypto;
   ```
3. Set strong credentials and restrict inbound network access.

## 3. Build Artifact

```bash
./mvnw clean install
```

Output jar will be under `target/` (for example `target/k2cut-0.0.1-SNAPSHOT.jar`).

## 4. Configure Environment on Server

Use environment variables (example):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/k2cut
export SPRING_DATASOURCE_USERNAME=<db-user>
export SPRING_DATASOURCE_PASSWORD=<strong-password>
export JWT_ISSUER_URI=https://<idp-domain>/realms/<realm>
export JWT_JWK_SET_URI=https://<idp-domain>/realms/<realm>/protocol/openid-connect/certs
```

## 5. Run with systemd (recommended for VM/bare-metal)

Create `/etc/systemd/system/k2cut.service`:

```ini
[Unit]
Description=2KCUT Salon Backend API
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/k2cut
Environment=SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/k2cut
Environment=SPRING_DATASOURCE_USERNAME=<db-user>
Environment=SPRING_DATASOURCE_PASSWORD=<strong-password>
Environment=JWT_ISSUER_URI=https://<idp-domain>/realms/<realm>
Environment=JWT_JWK_SET_URI=https://<idp-domain>/realms/<realm>/protocol/openid-connect/certs
ExecStart=/usr/bin/java -jar /opt/k2cut/k2cut-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

Enable/start:

```bash
sudo systemctl daemon-reload
sudo systemctl enable k2cut
sudo systemctl start k2cut
sudo systemctl status k2cut
```

## 6. Reverse Proxy (Nginx) + HTTPS

Basic Nginx upstream to Spring Boot (`127.0.0.1:7080`):

```nginx
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:7080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Then add TLS using Let’s Encrypt (`certbot`) or your certificate manager.

## 7. Container Deployment

For server/container platforms (VM with Docker, ECS, Kubernetes, etc.), use the included `Dockerfile`.

Build image:

```bash
docker build -t k2cut-api:latest .
```

Run image:

```bash
docker run --name k2cut-api \
  -p 7080:7080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/k2cut \
  -e SPRING_DATASOURCE_USERNAME=<db-user> \
  -e SPRING_DATASOURCE_PASSWORD=<strong-password> \
  -e JWT_ISSUER_URI=https://<idp-domain>/realms/<realm> \
  -e JWT_JWK_SET_URI=https://<idp-domain>/realms/<realm>/protocol/openid-connect/certs \
  -d k2cut-api:latest
```

## Production Readiness Checklist

- Use strong DB credentials and secret management (do not hardcode secrets).
- Restrict CORS origins in `SecurityConfig` (replace localhost origin).
- Disable debug security logging in production.
- Ensure only HTTPS is exposed publicly.
- Back up PostgreSQL and test restore flow.
- Monitor app logs, health endpoint, and DB metrics.
- Keep Java, Spring Boot dependencies, and Keycloak versions patched.

## Common Troubleshooting

- **`Connection to localhost:5432 refused`**
  - PostgreSQL is not running or connection details are wrong.
- **`JWT issuer mismatch` / token rejected**
  - `JWT_ISSUER_URI` does not match token `iss`.
- **Admin endpoints return 403**
  - JWT lacks `realm_access.roles` containing `admin`.
- **Flyway migration errors**
  - DB user lacks privileges, DB schema conflicts, or extension not enabled.

## Contributing

1. Create feature branch.
2. Add/modify code.
3. Add a new Flyway migration for schema changes (`V<version>__<description>.sql`).
4. Run tests.
5. Open pull request.
