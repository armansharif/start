-- ---------------------------------------------------------------------------
-- Selective audit trail for operations that matter (not every write).
-- OLD_VALUE / NEW_VALUE hold masked JSON snapshots as text.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS AUDIT_LOG (
    ID              BIGINT        NOT NULL AUTO_INCREMENT,
    USER_ID         BIGINT        NULL,
    OPERATION       VARCHAR(100)  NOT NULL,
    ENTITY_NAME     VARCHAR(100)  NOT NULL,
    ENTITY_ID       VARCHAR(100)  NULL,
    OLD_VALUE       VARCHAR(4000) NULL,
    NEW_VALUE       VARCHAR(4000) NULL,
    EVENT_TIMESTAMP DATETIME(6)   NOT NULL,
    IP_ADDRESS      VARCHAR(45)   NULL,
    CORRELATION_ID  VARCHAR(100)  NULL,
    CREATED_DATE    DATETIME(6)   NOT NULL,
    MODIFIED_DATE   DATETIME(6)   NOT NULL,
    CREATED_BY      BIGINT        NOT NULL,
    MODIFIED_BY     BIGINT        NOT NULL,
    VERSION         BIGINT        NOT NULL,
    PRIMARY KEY (ID),
    KEY IX_AUDIT_LOG_ENTITY (ENTITY_NAME, ENTITY_ID),
    KEY IX_AUDIT_LOG_EVENT (EVENT_TIMESTAMP),
    KEY IX_AUDIT_LOG_USER (USER_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO SCHEMA_CHANGE_LOG (SCRIPT_NAME, APPLIED_AT, APPLIED_BY, ENVIRONMENT, NOTES)
SELECT 'V0004__audit_log.sql', UTC_TIMESTAMP(6), CURRENT_USER(), COALESCE(@app_env, 'unknown'),
       'audit trail'
WHERE NOT EXISTS (SELECT 1 FROM SCHEMA_CHANGE_LOG WHERE SCRIPT_NAME = 'V0004__audit_log.sql');
