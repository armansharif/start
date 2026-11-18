-- ---------------------------------------------------------------------------
-- Development only. Never applied to shared or production environments.
-- Creates the role/permission catalogue and one administrator account.
--
-- The administrator row below is a DEVELOPMENT credential only:
--   username: admin   password: Admin@12345
-- Change or delete this account before the database is used by anyone else; the hash was
-- generated locally with BCrypt cost 12 and the {"bcrypt"} prefix required by the
-- application's delegating password encoder. Never copy it into a shared environment.
-- ---------------------------------------------------------------------------

INSERT INTO ROLES (CODE, TITLE, CREATED_DATE, MODIFIED_DATE, CREATED_BY, MODIFIED_BY, VERSION)
SELECT 'ADMIN', 'Administrator', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM ROLES WHERE CODE = 'ADMIN');

INSERT INTO ROLES (CODE, TITLE, CREATED_DATE, MODIFIED_DATE, CREATED_BY, MODIFIED_BY, VERSION)
SELECT 'USER', 'Standard user', UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM ROLES WHERE CODE = 'USER');

INSERT INTO PERMISSIONS (CODE, TITLE, CREATED_DATE, MODIFIED_DATE, CREATED_BY, MODIFIED_BY, VERSION)
SELECT p.CODE, p.TITLE, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0, 0, 0
  FROM (SELECT 'user:read' AS CODE, 'View users' AS TITLE
        UNION ALL SELECT 'user:create', 'Create users'
        UNION ALL SELECT 'user:update', 'Update users'
        UNION ALL SELECT 'user:delete', 'Delete users'
        UNION ALL SELECT 'user:assign-role', 'Assign roles') p
 WHERE NOT EXISTS (SELECT 1 FROM PERMISSIONS existing WHERE existing.CODE = p.CODE);

INSERT INTO ROLE_PERMISSION (ROLE_ID, PERMISSION_ID, CREATED_DATE, MODIFIED_DATE, CREATED_BY, MODIFIED_BY, VERSION)
SELECT r.ID, p.ID, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0, 0, 0
  FROM ROLES r
  CROSS JOIN PERMISSIONS p
 WHERE r.CODE = 'ADMIN'
   AND NOT EXISTS (SELECT 1 FROM ROLE_PERMISSION rp WHERE rp.ROLE_ID = r.ID AND rp.PERMISSION_ID = p.ID);

INSERT INTO USERS (USERNAME, PASSWORD_HASH, FULL_NAME, STATUS,
                   CREATED_DATE, MODIFIED_DATE, CREATED_BY, MODIFIED_BY, VERSION)
SELECT 'admin', '{bcrypt}$2a$12$mBOXbCJ/fv4tlhN9lBMtpeLhhuDfjSEIolLZ8fi7tgloO3hGZnzl2', 'Administrator', 'ACTIVE',
       UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM USERS WHERE USERNAME = 'admin');

INSERT INTO USER_ROLE (USER_ID, ROLE_ID, CREATED_DATE, MODIFIED_DATE, CREATED_BY, MODIFIED_BY, VERSION)
SELECT u.ID, r.ID, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6), 0, 0, 0
  FROM USERS u, ROLES r
 WHERE u.USERNAME = 'admin' AND r.CODE = 'ADMIN'
   AND NOT EXISTS (SELECT 1 FROM USER_ROLE ur WHERE ur.USER_ID = u.ID AND ur.ROLE_ID = r.ID);
