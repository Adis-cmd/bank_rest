--liquibase formatted sql

--changeset Adis:3
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name='users'
CREATE TABLE users (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(100),
    surname        VARCHAR(100),
    email          VARCHAR(255) UNIQUE NOT NULL,
    password       VARCHAR(255) NOT NULL,
    enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    authorities_id BIGINT,

    CONSTRAINT fk_user_authorities
        FOREIGN KEY (authorities_id)
        REFERENCES authorities (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);