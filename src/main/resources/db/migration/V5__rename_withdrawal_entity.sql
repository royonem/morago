ALTER TABLE transactions
    DROP FOREIGN KEY FK_TRANSACTIONS_ON_WITHDRAWAL_REQUEST;

ALTER TABLE withdrawal_requests
    DROP FOREIGN KEY FK_WITHDRAWAL_REQUESTS_ON_BANK_ACCOUNT;

ALTER TABLE withdrawal_requests
    DROP FOREIGN KEY FK_WITHDRAWAL_REQUESTS_ON_REQUESTER;

ALTER TABLE withdrawal_requests
    DROP FOREIGN KEY FK_WITHDRAWAL_REQUESTS_ON_REVIEWER;

ALTER TABLE withdrawal_requests
    DROP FOREIGN KEY FK_WITHDRAWAL_REQUESTS_ON_WALLET;

CREATE TABLE withdrawals
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime              NULL,
    updated_at       datetime              NULL,
    requester_id     BIGINT                NOT NULL,
    reviewer_id      BIGINT                NULL,
    wallet_id        BIGINT                NOT NULL,
    bank_account_id  BIGINT                NOT NULL,
    amount           BIGINT                NOT NULL,
    currency_code    SMALLINT              NOT NULL,
    status           SMALLINT              NOT NULL,
    rejection_reason VARCHAR(255)          NULL,
    reviewed_at      datetime              NULL,
    paid_at          datetime              NULL,
    CONSTRAINT pk_withdrawals PRIMARY KEY (id)
);

ALTER TABLE transactions
    ADD withdrawal_id BIGINT NULL;

ALTER TABLE transactions
    ADD CONSTRAINT uc_transactions_withdrawal UNIQUE (withdrawal_id);

ALTER TABLE transactions
    ADD CONSTRAINT FK_TRANSACTIONS_ON_WITHDRAWAL FOREIGN KEY (withdrawal_id) REFERENCES withdrawals (id);

ALTER TABLE withdrawals
    ADD CONSTRAINT FK_WITHDRAWALS_ON_BANK_ACCOUNT FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id);

ALTER TABLE withdrawals
    ADD CONSTRAINT FK_WITHDRAWALS_ON_REQUESTER FOREIGN KEY (requester_id) REFERENCES users (id);

ALTER TABLE withdrawals
    ADD CONSTRAINT FK_WITHDRAWALS_ON_REVIEWER FOREIGN KEY (reviewer_id) REFERENCES users (id);

ALTER TABLE withdrawals
    ADD CONSTRAINT FK_WITHDRAWALS_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallets (id);

DROP TABLE withdrawal_requests;

ALTER TABLE transactions
    DROP COLUMN withdrawal_request_id;