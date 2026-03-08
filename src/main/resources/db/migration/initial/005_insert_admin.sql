--liquibase formatted sql

--changeset Adis:6
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM users WHERE email='admin@gmail.com'

-- Default admin credentials:
-- Email:    admin@gmail.com
-- Password: qwerty

INSERT INTO users (name, surname, email, password, enabled, authorities_id)
VALUES (
    'Admin',
    'Admin',
    'admin@gmail.com',
    '$2a$10$Kmi84XcLnM.mFy5TSGUOv.kGN.CFno7LANVc.vdaOrKxi/BexGbju',
    true,
    (SELECT id FROM authorities WHERE name = 'ROLE_ADMIN')
);

--rollback DELETE FROM users WHERE email='admin@gmail.com';