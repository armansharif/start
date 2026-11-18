-- ---------------------------------------------------------------------------
-- Refresh tokens (security infrastructure).
--
-- Only the SHA-256 hash of the token is stored. Rotation keeps FAMILY_ID stable so that
-- reuse of a consumed token can revoke the whole family.
-- USER_ID gets its foreign key once the USER table exists (user module script).
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS REFRESH_TOKEN (
    ID             BIGINT       NOT NULL AUTO_INCREMENT,
    USER_ID        BIGINT       NOT NULL,
    FAMILY_ID      VARCHAR(36)  NOT NULL,
    TOKEN_HASH     VARCHAR(64)  NOT NULL,
    EXPIRES_AT     DATETIME(6)  NOT NULL,
    REVOKED_AT     DATETIME(6)  NULL,
    IP_ADDRESS     VARCHAR(45)  NULL,
    USER_AGENT     VARCHAR(300) NULL,
    CREATED_DATE   DATETIME(6)  NOT NULL,
    MODIFIED_DATE  DATETIME(6)  NOT NULL,
    CREATED_BY     BIGINT       NOT NULL,
    MODIFIED_BY    BIGINT       NOT NULL,
    VERSION        BIGINT       NOT NULL,
    PRIMARY KEY (ID),
    UNIQUE KEY UK_REFRESH_TOKEN_HASH (TOKEN_HASH),
    KEY IX_REFRESH_TOKEN_USER (USER_ID),
    KEY IX_REFRESH_TOKEN_FAMILY (FAMILY_ID)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

INSERT INTO SCHEMA_CHANGE_LOG (SCRIPT_NAME, APPLIED_AT, APPLIED_BY, ENVIRONMENT, NOTES)
SELECT 'V0002__refresh_token.sql', UTC_TIMESTAMP(6), CURRENT_USER(), COALESCE(@app_env, 'unknown'),
       'refresh token storage'
WHERE NOT EXISTS (SELECT 1 FROM SCHEMA_CHANGE_LOG WHERE SCRIPT_NAME = 'V0002__refresh_token.sql');
