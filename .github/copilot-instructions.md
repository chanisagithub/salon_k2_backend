# Copilot Instructions for `k2cut`

## Build, test, and lint commands

Use the Maven wrapper from the repository root.

- Build package: `./mvnw clean install`
- Run app locally: `./mvnw spring-boot:run`
- Run full test suite: `./mvnw test`
- Run a single test class: `./mvnw -Dtest=K2cutApplicationTests test`
- Run a single test method: `./mvnw -Dtest=K2cutApplicationTests#contextLoads test`

There is no dedicated lint task configured in `pom.xml` (no Checkstyle/PMD/SpotBugs/Spotless plugin configured).

## High-level architecture

This project is a Spring Boot 4 REST API for barber-shop booking with layered boundaries:

- **Web layer** (`com.unique.k2cut.web.rest`): versioned endpoints under `/api/v1/*`
  - `BookingController` serves booking APIs (`/services`, `/barbers`, `/appointments`, `/my-appointments`)
  - `PublicController` serves public health endpoint
  - `AdminTestController` is an admin-protected endpoint
- **Security layer** (`config/SecurityConfig`): stateless OAuth2 resource server with JWT
  - Path-level access rules are centralized in the security filter chain
  - Roles are extracted from Keycloak-style `realm_access.roles` and converted to `ROLE_*` authorities
- **Service layer** (`service/*`): business rules and transaction boundaries
  - `AppointmentService` enforces booking rules (future time, barber active, schedule/day validation, overlap conflict detection, 2-hour cancellation window)
  - `UserService` syncs local users from JWT claims on booking creation
- **Persistence layer** (`domain/entity` + `repository`):
  - JPA entities map to `users`, `services`, `barbers`, `barber_schedules`, `appointments`
  - Spring Data repositories expose derived-query methods used directly by services
- **Database lifecycle** (`src/main/resources/db/migration`):
  - Flyway migrations are authoritative for schema evolution (`V1__Initial_Schema.sql`)
  - App runs with `spring.jpa.hibernate.ddl-auto=validate`, so entity/schema mismatches fail at startup

## Key repository-specific conventions

- JWT subject (`jwt.getSubject()`) is treated as the local `users.id` UUID. Booking flow expects this and uses `UserService#getOrCreateUser(...)` to persist first-time users from token claims.
- API path taxonomy is strict:
  - Public: `/api/v1/public/**`
  - Booking: `/api/v1/booking/**` (with only `/services` and `/barbers` publicly accessible)
  - Admin: `/api/v1/admin/**` requiring `ROLE_ADMIN`
- Appointment overlap checks use repository method naming (`existsByBarberIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan`) with active statuses `PENDING` and `CONFIRMED`.
- Time handling is consistently `OffsetDateTime` for appointment timestamps and `LocalTime` for schedule windows.
- DTOs are Java records (`dto/*`), while entities use Lombok + JPA annotations.
- Business exceptions are surfaced via `@ResponseStatus` exception classes (`BookingException` => 400, `ResourceNotFoundException` => 404) rather than a global exception handler.
- For DB changes, add a new Flyway migration file under `db/migration` using `V<version>__<description>.sql`; do not rely on entity auto-DDL changes.
- The current integration-style test (`@SpringBootTest`) uses the default datasource configuration, so local Postgres settings in `application.properties` affect test execution unless a test profile is introduced.
