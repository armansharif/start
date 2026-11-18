# Backend Platform

Reusable, modular backend infrastructure built with **Java 21** and **Spring Boot 4.1.x**.
Modular monolith (not microservices), designed so that modules stay separable later.

## Requirements

| Tool | Version | Notes |
|---|---|---|
| JDK | 21 | `JAVA_HOME` must point to a JDK 21 (Maven on this machine defaults to Java 8) |
| Maven | 3.9+ | `mvn` from PATH is fine |
| Docker | any recent | Only for local MySQL/Redis and Testcontainers; not needed to build |

Windows/Git Bash:

```bash
export JAVA_HOME="/c/Program Files/Java/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Build

```bash
mvn -B clean verify          # compile + unit tests
mvn -B -DskipTests package   # fast packaging only
```

## Database: one place to configure it

Every connection detail (host, port, database, user, password and the Redis endpoint)
lives in **`application/application-api/src/main/resources/application-db.yml`**. It is
imported by `application.yml`, so no other file repeats a URL or a credential.

Edit that file for a permanent change, or override it per run with environment variables
(values shown are the built-in local defaults):

| Variable | Default | Meaning |
|---|---|---|
| `APP_DB_URL` | `jdbc:mysql://localhost:3306/backend_platform?...` | full JDBC URL |
| `APP_DB_USERNAME` | `root` | database user |
| `APP_DB_PASSWORD` | *(empty)* | database password |
| `APP_DB_POOL_SIZE` / `APP_DB_MIN_IDLE` | `15` / `5` | Hikari pool sizing |
| `APP_REDIS_HOST` / `APP_REDIS_PORT` / `APP_REDIS_PASSWORD` | `localhost` / `6379` / *(empty)* | cache |
| `APP_PORT` / `APP_MANAGEMENT_PORT` | `8080` / `8081` | HTTP and actuator ports |

The defaults target a stock local install (root, no password). Shared environments must
set `APP_DB_USERNAME`/`APP_DB_PASSWORD` and must not use `root`.

## Creating the local database

The schema is **never** created by Hibernate: it is applied from the versioned scripts in
`database/mysql/` in manifest order (`ddl-auto` is `validate` in development, `none` in
production). With a local MySQL/MariaDB and a UTF-8 client:

```bash
mysql -u root -e "CREATE DATABASE IF NOT EXISTS backend_platform \
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci"

for f in initial/V0001__schema_change_log.sql changes/V0002__refresh_token.sql \
         changes/V0003__system_error_log.sql changes/V0004__audit_log.sql \
         changes/V0005__user_module_schema.sql indexes/V0006__user_module_indexes.sql; do
  mysql -u root --default-character-set=utf8mb4 \
        --init-command="SET @app_env='dev'" backend_platform < "database/mysql/$f"
done

# development data: roles, permissions, one admin account (admin / Admin@12345)
mysql -u root --default-character-set=utf8mb4 --init-command="SET @app_env='dev'" \
      backend_platform < database/mysql/seed/dev/V9001__dev_roles_permissions.sql
```

Notes for this machine and for scripts in general:

- XAMPP ships **MariaDB 10.4**, which has no `utf8mb4_0900_ai_ci` collation; the scripts use
  `utf8mb4_unicode_ci`, accepted by both MariaDB and MySQL 8.
- Always pass `--default-character-set=utf8mb4` to the client and apply a script from a
  **file** (not from a shell command line): the Windows console codepage converts Persian
  text to `?` before MySQL ever sees it. The application itself stores UTF-8 correctly
  (`FULL_NAME` of a Persian user is stored as `D8B3D8A7...`, not `3F3F...`).
- The seed account exists only so a fresh database is usable; change its password.
- Without Docker the alternative is `docker compose up -d` (MySQL 8.4 + Redis 7).
- Redis is optional for starting the application: the cache is only touched by cached
  endpoints. `/actuator/health` reports `DOWN` while Redis is unreachable.

## Module layout

```
common/common-core         technology-free: error contract, paging, dynamic search model, ports
common/common-datetime     Persian calendar (Time4J behind a port), parse/format/validate, timezone
common/common-validation   reusable Bean Validation constraints
common/common-cache        Spring Cache + Redis: cache names, TTL policy, eviction helpers
common/common-persistence  BaseEntity, BaseDao, JPA + JDBC, transactions, auditing, paging
common/common-search       dynamic search engine: metadata registry, JPA strategy, JDBC builder
common/common-web          response envelope, page response, global exception handling
common/common-logging      correlation id, system error log, audit trail
common/common-security     Spring Security, JWT access/refresh, RBAC/PBAC
common/common-test         test support: Testcontainers base, ArchUnit rules, fixtures
modules/user-module        reference business module (the template for future modules)
application/application-api  the only executable module
database/                  MySQL (and later Oracle) SQL scripts
docs/                      architecture and usage documentation
```

Dependency direction is strictly downward:
`core → datetime → persistence → {search, web, logging} → security → modules → application-api`.
Business modules never depend on each other's internal packages (only on `*.api`).

## Rules enforced by the build

- No Flyway/Liquibase (`maven-enforcer-plugin`); schema changes are SQL scripts.
- Java 21 and Maven 3.9 minimum.
- No Hibernate relations in entities, no Persian calendar logic outside `common-datetime`
  (enforced by ArchUnit rules in `common-test`, added in a later step).

## Current state

Completed and verified by the build:

| Area | State |
|---|---|
| Maven structure, POMs, guardrails | done |
| `common-core`: error contract, `PageResult`, dynamic search model | done |
| `common-datetime`: Time4J converter, strict parser, formatter, half-open ranges, validation annotations, clock | done (42 tests) |
| `common-persistence`: `BaseEntity`, `BaseDao`, `AbstractJpaDao`, `AbstractJdbcDao`, dialect paging, auditing, `BaseService` | done (13 tests) |
| `common-web`: `ApiResponse` envelope, `PageResponse`, `GlobalExceptionHandler`, message resolution, Persian date converter registration | done (4 tests) |
| `common-test`: ArchUnit rule set, now enforced without `allowEmptyShould` | done (rules verified in `ArchitectureTest`) |
| `common-security`: JWT access tokens, opaque rotating refresh tokens, brute force guard | done (20 tests) |
| `common-search`: annotation driven metadata, JPA specifications, SQL builder with bound parameters | done (22 tests) |
| `common-logging`: correlation id filter + MDC propagation, system error recorder, audit recorder | done (3 tests) |
| `common-cache`: Redis cache manager, per cache TTL, namespaced keys | done (no dedicated tests yet) |
| `user-module`: entities, JPA + JDBC DAOs, MapStruct mapping, service, controller, authentication adapter | done (19 tests) |
| `database/`: ledger, manifest, change/index/seed scripts up to `V0006` | done (scripts not yet executed against a live MySQL: Docker is not installed here) |

Total: 129 tests, all green.

## Running the application

The JWT secret is never committed; the application refuses to start without it:

```bash
export APP_JWT_SECRET="$(openssl rand -base64 48)"
# only needed when 8081 is already taken on the machine
export APP_MANAGEMENT_PORT=8082
mvn -B -pl application/application-api spring-boot:run
```

On this machine the JWT secret was supplied this way, the database defaults in
`application-db.yml` were used unchanged, and login, user creation with a Persian birth
date, dynamic search, the SQL report, refresh tokens, `401`/`403`/`409` and localized
error messages were exercised against the local MariaDB.

Endpoints (all under `/api/v1`): `POST /auth/login`, `POST /auth/refresh`, and the user
module: `GET|PUT|DELETE /users/{id}`, `POST /users`, `POST /users/search`, `POST
/users/report`, `PATCH /users/{id}/status`, `PUT /users/{id}/roles`. User endpoints require
the `user:*` authorities seeded with the development scripts.

Search requests accept `page`, `size`, `sortBy`, `sortDirection`, `keyword`, `fromDate` and
`toDate`. Field level filters (`filter`) are currently built server side and are not bound
from JSON; sending `filter` has no effect. Unknown or non sortable `sortBy` values are
rejected with `SEARCH_INVALID_FIELD` (`COMMON-1501`).

## Reference module: `user-module`

It is the template for every future module, so it shows each decision once:

- **Entities** carry identifiers only (`UserRole.userId` / `roleId`); the database enforces
  the foreign keys and unique pairs.
- **Two read paths**: `UserDao` (JPA specifications, entity results) and `UserReportDao`
  (hand written SQL with an explicit `RowMapper`). The service depends on the interfaces;
  `UserModuleConfig` binds the implementations.
- **Search** is whitelist based: `UserSearchModel` annotates the fields a client may filter
  or sort by, so `passwordHash` is unreachable from the request body.
- **Dates** arrive as Persian strings (`@ValidPersianDate`) and are converted once in the
  service; responses contain both the Gregorian value and the Persian display value.
- **Security**: `UserAuthenticationAdapter` is the only place translating `UserStatus` into
  `enabled`/`locked`, and it resolves roles and permissions with a single SQL join.
- **Authorisation** is enforced with `@PreAuthorize` on the controller, and the service owns
  the rules that depend on data (duplicate username/mobile, unknown role).

## Environment notes

- This machine's `JAVA_HOME` points to JDK 8; builds must set it to the JDK 21 path.
- Docker is not installed, so Testcontainers integration tests cannot run yet. They are
  intentionally not added as failing tests; database-backed tests are written once Docker
  (or a reachable MySQL) is available.
