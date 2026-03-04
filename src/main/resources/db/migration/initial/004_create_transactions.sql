--liquibase formatted sql

--changeset Adis:5
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.tables WHERE table_name='transactions'
CREATE TABLE transactions (
    id               BIGSERIAL PRIMARY KEY,
    from_card_id     BIGINT NOT NULL,
    to_card_id       BIGINT NOT NULL,
    amount           NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status           VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'
                     CHECK (status IN ('SUCCESS', 'FAILED', 'PENDING')),

    CONSTRAINT fk_transaction_from_card
        FOREIGN KEY (from_card_id) REFERENCES cards (id)
        ON DELETE RESTRICT ON UPDATE CASCADE,

    CONSTRAINT fk_transaction_to_card
        FOREIGN KEY (to_card_id) REFERENCES cards (id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

--changeset Adis:6
CREATE INDEX idx_transactions_from_card ON transactions (from_card_id);
CREATE INDEX idx_transactions_to_card ON transactions (to_card_id);