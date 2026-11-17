# Database scripts

The schema is owned by versioned SQL scripts, not by the application. Hibernate never
creates or alters tables (`ddl-auto` is `validate` in development and `none` in
production).

## Layout

```
database/
├── mysql/
│   ├── initial/       baseline and ledger scripts (V0001, ...)
│   ├── changes/       every structural change, one script per change
│   ├── indexes/       index creation kept separate from table changes
│   ├── procedures/    stored procedures, when a report really needs one
│   ├── seed/          development-only data (never applied to production)
│   └── manifest.txt   the ordered execution list
└── oracle/            empty until an Oracle migration is actually requested
```

## Naming

`V####__short_description.sql`. The format intentionally matches Flyway so that adopting
it later is a configuration change, not a rename of dozens of files.

Rules:

1. Numbers are unique, ascending and never reused.
2. A script that has been applied anywhere other than a local machine is immutable.
3. Every script ends by inserting its own row into `SCHEMA_CHANGE_LOG`.
4. Changes to a column type or a constraint get their own script; never mix unrelated
   changes in one file.
5. Charset and collation are always explicit: `utf8mb4` / `utf8mb4_0900_ai_ci`.
6. Portable types only: `BIGINT`, `VARCHAR(n)`, `DECIMAL(p,s)`, `DATE`, `DATETIME(6)`,
   `TINYINT(1)`. No `ENUM`, `SET`, `JSON` columns, `TIMESTAMP` or unbounded `TEXT`.

## Applying scripts

Local development (MySQL from `docker-compose.yml`):

```bash
docker compose up -d mysql
mysql -h 127.0.0.1 -P 3306 -u app -p backend_platform < database/mysql/initial/V0001__schema_change_log.sql
```

Shared and production environments: a maintainer applies the scripts in manifest order,
records who applied them in `APPLIED_BY`/`NOTES`, and verifies `SELECT * FROM
SCHEMA_CHANGE_LOG ORDER BY ID` afterwards. The ledger is the audit trail of schema
changes.

## Oracle

`database/oracle/` stays empty until a migration is actually requested. Data type,
identity, pagination and constraint differences are documented in
`docs/persistence-architecture.md`; the application is not written for two databases at
the same time.
