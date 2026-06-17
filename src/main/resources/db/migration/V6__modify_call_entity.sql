ALTER TABLE calls
    ADD canceled_at datetime NULL;

ALTER TABLE calls
    ADD is_client_initiator BIT(1) NULL;

ALTER TABLE calls
    ADD max_call_time BIGINT NULL;

ALTER TABLE calls
    MODIFY is_client_initiator BIT(1) NOT NULL;

ALTER TABLE calls
    MODIFY max_call_time BIGINT NOT NULL;

ALTER TABLE calls
    DROP COLUMN cancelled_at;

ALTER TABLE calls
    DROP COLUMN cost;

ALTER TABLE calls
    ADD cost BIGINT NOT NULL;