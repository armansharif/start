-- ---------------------------------------------------------------------------
-- Indexes supporting the dynamic search and the report queries of the user module.
-- Kept separate from table creation so index changes are reviewable on their own.
-- ---------------------------------------------------------------------------

CREATE INDEX IX_USERS_FULL_NAME ON USERS (FULL_NAME);
CREATE INDEX IX_USERS_STATUS ON USERS (STATUS);
CREATE INDEX IX_USERS_CREATED_DATE ON USERS (CREATED_DATE);
CREATE INDEX IX_USERS_EMAIL ON USERS (EMAIL);

INSERT INTO SCHEMA_CHANGE_LOG (SCRIPT_NAME, APPLIED_AT, APPLIED_BY, ENVIRONMENT, NOTES)
SELECT 'V0006__user_module_indexes.sql', UTC_TIMESTAMP(6), CURRENT_USER(), COALESCE(@app_env, 'unknown'),
       'search/report indexes'
WHERE NOT EXISTS (SELECT 1 FROM SCHEMA_CHANGE_LOG WHERE SCRIPT_NAME = 'V0006__user_module_indexes.sql');
