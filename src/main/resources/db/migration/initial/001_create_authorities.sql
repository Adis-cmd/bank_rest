--liquibase formatted sql

--changeset Adis:1
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name='authorities'
CREATE TABLE authorities (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(55) NOT NULL UNIQUE
);

--changeset Adis:2
INSERT INTO authorities (name) VALUES ('ROLE_ADMIN');
INSERT INTO authorities (name) VALUES ('ROLE_USER');