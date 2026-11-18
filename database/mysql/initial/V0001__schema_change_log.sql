-- ---------------------------------------------------------------------------
-- Platform schema ledger.
--
-- Every script inserts its own row when it has been applied successfully, which is the
-- only source of truth about what has run in an environment (no Flyway in this project).
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SCHEMA_CHANGE_LOG (
    ID            BIGINT       NOT NULL AUTO_INCREMENT,
    SCRIPT_NAME   VARCHAR(200) NOT NULL,
    CHECKSUM      VARCHAR(64)  NULL,
    APPLIED_AT    DATETIME(6)  NOT NULL,
    APPLIED_BY    VARCHAR(100) NOT NULL,
    ENVIRONMENT   VARCHAR(50)  NOT NULL,
    NOTES         VARCHAR(500) NULL,
    PRIMARY KEY (ID),
    UNIQUE KEY UK_SCHEMA_CHANGE_LOG_SCRIPT (SCRIPT_NAME)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO SCHEMA_CHANGE_LOG (SCRIPT_NAME, APPLIED_AT, APPLIED_BY, ENVIRONMENT, NOTES)
SELECT 'V0001__schema_change_log.sql', UTC_TIMESTAMP(6), CURRENT_USER(), COALESCE(@app_env, 'unknown'), 'ledger table'
WHERE NOT EXISTS (SELECT 1 FROM SCHEMA_CHANGE_LOG WHERE SCRIPT_NAME = 'V0001__schema_change_log.sql');
