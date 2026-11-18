-- ---------------------------------------------------------------------------
-- Unexpected system failures, searchable by correlation id.
-- Business exceptions are NOT stored here.
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS SYSTEM_ERROR_LOG (
    ID                 BIGINT        NOT NULL AUTO_INCREMENT,
    ERROR_CODE         VARCHAR(50)   NOT NULL,
    EXCEPTION_CLASS    VARCHAR(300)  NOT NULL,
    ERROR_MESSAGE      VARCHAR(2000) NULL,
    STACK_TRACE        VARCHAR(8000) NULL,
    REQUEST_URI        VARCHAR(500)  NULL,
    HTTP_METHOD        VARCHAR(10)   NULL,
    USER_ID            BIGINT        NULL,
    CORRELATION_ID     VARCHAR(100)  NULL,
    REQUEST_TIMESTAMP  DATETIME(6)   NOT NULL,
    CREATED_DATE       DATETIME(6)   NOT NULL,
    MODIFIED_DATE      DATETIME(6)   NOT NULL,
    CREATED_BY         BIGINT        NOT NULL,
    MODIFIED_BY        BIGINT        NOT NULL,
    VERSION            BIGINT        NOT NULL,
    PRIMARY KEY (ID),
    KEY IX_SYSTEM_ERROR_LOG_CORRELATION (CORRELATION_ID),
    KEY IX_SYSTEM_ERROR_LOG_CREATED (CREATED_DATE),
    KEY IX_SYSTEM_ERROR_LOG_CODE (ERROR_CODE)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO SCHEMA_CHANGE_LOG (SCRIPT_NAME, APPLIED_AT, APPLIED_BY, ENVIRONMENT, NOTES)
SELECT 'V0003__system_error_log.sql', UTC_TIMESTAMP(6), CURRENT_USER(), COALESCE(@app_env, 'unknown'),
       'system error log'
WHERE NOT EXISTS (SELECT 1 FROM SCHEMA_CHANGE_LOG WHERE SCRIPT_NAME = 'V0003__system_error_log.sql');
