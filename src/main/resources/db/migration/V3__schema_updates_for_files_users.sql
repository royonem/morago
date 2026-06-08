ALTER TABLE files
    ADD file_purpose SMALLINT NULL;

ALTER TABLE files
    ADD file_status SMALLINT NULL;

ALTER TABLE files
    MODIFY file_purpose SMALLINT NOT NULL;

ALTER TABLE files
    DROP COLUMN file_size;

ALTER TABLE files
    ADD file_size BIGINT NOT NULL;

ALTER TABLE users
    MODIFY birthdate date NULL;

ALTER TABLE users
    MODIFY topik_level SMALLINT NULL;