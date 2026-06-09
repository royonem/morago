ALTER TABLE notifications
    ADD action_url VARCHAR(255) NULL;

ALTER TABLE notifications
    ADD is_read BIT(1) NULL;

ALTER TABLE notifications
    ADD sent_at datetime NULL;

ALTER TABLE notifications
    ADD title VARCHAR(255) NULL;

ALTER TABLE notifications
    MODIFY is_read BIT(1) NOT NULL;

ALTER TABLE notifications
    MODIFY title VARCHAR(255) NOT NULL;

ALTER TABLE notifications
    DROP COLUMN was_read;