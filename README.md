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

## Local infrastructure

```bash
docker compose up -d         # MySQL 8.4 on 3306, Redis 7 on 6379
```

Database schema is **never** created by Hibernate. It is applied from versioned SQL
scripts in `database/mysql/` (`ddl-auto` is `validate` in development, `none` in production).

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

## Current state (Phase 1, step 1a)

Structure, POMs and configuration are in place and the full reactor builds.
`common-core` contains the error contract, `PageResult` and the dynamic search model.
The other modules currently contain only their POM (dependency wiring) — their sources
arrive in the following steps.
