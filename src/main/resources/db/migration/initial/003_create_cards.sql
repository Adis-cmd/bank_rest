--liquibase formatted sql

--changeset Adis:4
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name='cards'
CREATE TABLE cards (
    id              BIGSERIAL PRIMARY KEY,
    balance         NUMERIC(15,2) NOT NULL DEFAULT 0.00,
    owner_id        BIGINT NOT NULL,
    card_number     VARCHAR(255) UNIQUE NOT NULL,
    expiration_date DATE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
                    CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED', 'BLOCK_REQUESTED')),

    CONSTRAINT fk_card_owner
        FOREIGN KEY (owner_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
);