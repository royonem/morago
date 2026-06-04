CREATE TABLE bank_accounts
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime              NULL,
    updated_at     datetime              NULL,
    user_id        BIGINT                NOT NULL,
    bank_name      VARCHAR(255)          NOT NULL,
    account_number VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_bank_accounts PRIMARY KEY (id)
);

CREATE TABLE calls
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime              NULL,
    updated_at    datetime              NULL,
    client_id     BIGINT                NOT NULL,
    translator_id BIGINT                NOT NULL,
    topic_id      BIGINT                NOT NULL,
    cost          INT                   NOT NULL,
    status        SMALLINT              NOT NULL,
    rating        INT                   NULL,
    accepted_at   datetime              NULL,
    started_at    datetime              NULL,
    ended_at      datetime              NULL,
    cancelled_at  datetime              NULL,
    CONSTRAINT pk_calls PRIMARY KEY (id)
);

CREATE TABLE categories
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    name       VARCHAR(255)          NOT NULL,
    active     BIT(1)                NOT NULL,
    CONSTRAINT pk_categories PRIMARY KEY (id)
);

CREATE TABLE files
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    file_name  VARCHAR(255)          NOT NULL,
    file_path  VARCHAR(255)          NOT NULL,
    file_type  VARCHAR(255)          NOT NULL,
    file_size  INT                   NOT NULL,
    CONSTRAINT pk_files PRIMARY KEY (id)
);

CREATE TABLE languages
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    name       VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_languages PRIMARY KEY (id)
);

CREATE TABLE notifications
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    user_id    BIGINT                NOT NULL,
    content    VARCHAR(255)          NOT NULL,
    was_read   BIT(1)                NOT NULL,
    read_at    datetime              NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id)
);

CREATE TABLE refresh_tokens
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    token      VARCHAR(255)          NOT NULL,
    user_id    BIGINT                NOT NULL,
    expires_at datetime              NOT NULL,
    revoked    BIT(1)                NOT NULL,
    CONSTRAINT pk_refresh_tokens PRIMARY KEY (id)
);

CREATE TABLE roles
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime              NULL,
    updated_at datetime              NULL,
    name       VARCHAR(255)          NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

CREATE TABLE topics
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime              NULL,
    updated_at    datetime              NULL,
    category_id   BIGINT                NULL,
    topic_icon_id BIGINT                NULL,
    name          VARCHAR(255)          NOT NULL,
    active        BIT(1)                NOT NULL,
    CONSTRAINT pk_topics PRIMARY KEY (id)
);

CREATE TABLE transactions
(
    id                    BIGINT AUTO_INCREMENT NOT NULL,
    created_at            datetime              NULL,
    updated_at            datetime              NULL,
    wallet_id             BIGINT                NOT NULL,
    call_id               BIGINT                NULL,
    withdrawal_request_id BIGINT                NULL,
    type                  SMALLINT              NOT NULL,
    amount                BIGINT                NOT NULL,
    currency_code         SMALLINT              NOT NULL,
    status                SMALLINT              NOT NULL,
    balance_before        BIGINT                NOT NULL,
    balance_after         BIGINT                NOT NULL,
    `reference`           VARCHAR(255)          NOT NULL,
    `description`         VARCHAR(255)          NOT NULL,
    processed_at          datetime              NULL,
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

CREATE TABLE user_languages
(
    language_id BIGINT NOT NULL,
    user_id     BIGINT NOT NULL,
    CONSTRAINT pk_user_languages PRIMARY KEY (language_id, user_id)
);

CREATE TABLE user_roles
(
    role_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (role_id, user_id)
);

CREATE TABLE user_topics
(
    topic_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL,
    CONSTRAINT pk_user_topics PRIMARY KEY (topic_id, user_id)
);

CREATE TABLE users
(
    id                 BIGINT AUTO_INCREMENT NOT NULL,
    created_at         datetime              NULL,
    updated_at         datetime              NULL,
    profile_picture_id BIGINT                NULL,
    first_name         VARCHAR(255)          NOT NULL,
    last_name          VARCHAR(255)          NOT NULL,
    email              VARCHAR(255)          NOT NULL,
    password_hash      VARCHAR(255)          NOT NULL,
    phone              VARCHAR(255)          NOT NULL,
    availability       SMALLINT              NOT NULL,
    status             SMALLINT              NOT NULL,
    topik_level        SMALLINT              NOT NULL,
    birthdate          date                  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE wallets
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime              NULL,
    updated_at    datetime              NULL,
    user_id       BIGINT                NOT NULL,
    version       BIGINT                NULL,
    balance       BIGINT                NOT NULL CHECK (balance >= 0),
    currency_code SMALLINT              NOT NULL,
    status        SMALLINT              NOT NULL,
    CONSTRAINT pk_wallets PRIMARY KEY (id)
);

CREATE TABLE withdrawal_requests
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
    CONSTRAINT pk_withdrawal_requests PRIMARY KEY (id)
);

ALTER TABLE bank_accounts
    ADD CONSTRAINT uc_bank_accounts_accountnumber UNIQUE (account_number);

ALTER TABLE bank_accounts
    ADD CONSTRAINT uc_bank_accounts_user UNIQUE (user_id);

ALTER TABLE categories
    ADD CONSTRAINT uc_categories_name UNIQUE (name);

ALTER TABLE languages
    ADD CONSTRAINT uc_languages_name UNIQUE (name);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT uc_refresh_tokens_token UNIQUE (token);

ALTER TABLE roles
    ADD CONSTRAINT uc_roles_name UNIQUE (name);

ALTER TABLE topics
    ADD CONSTRAINT uc_topics_name UNIQUE (name);

ALTER TABLE topics
    ADD CONSTRAINT uc_topics_topic_icon UNIQUE (topic_icon_id);

ALTER TABLE transactions
    ADD CONSTRAINT uc_transactions_call UNIQUE (call_id);

ALTER TABLE transactions
    ADD CONSTRAINT uc_transactions_reference UNIQUE (`reference`);

ALTER TABLE transactions
    ADD CONSTRAINT uc_transactions_withdrawal_request UNIQUE (withdrawal_request_id);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_phone UNIQUE (phone);

ALTER TABLE users
    ADD CONSTRAINT uc_users_profile_picture UNIQUE (profile_picture_id);

ALTER TABLE wallets
    ADD CONSTRAINT uc_wallets_user UNIQUE (user_id);

ALTER TABLE bank_accounts
    ADD CONSTRAINT FK_BANK_ACCOUNTS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE calls
    ADD CONSTRAINT FK_CALLS_ON_CLIENT FOREIGN KEY (client_id) REFERENCES users (id);

ALTER TABLE calls
    ADD CONSTRAINT FK_CALLS_ON_TOPIC FOREIGN KEY (topic_id) REFERENCES topics (id);

ALTER TABLE calls
    ADD CONSTRAINT FK_CALLS_ON_TRANSLATOR FOREIGN KEY (translator_id) REFERENCES users (id);

ALTER TABLE notifications
    ADD CONSTRAINT FK_NOTIFICATIONS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE refresh_tokens
    ADD CONSTRAINT FK_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE topics
    ADD CONSTRAINT FK_TOPICS_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES categories (id);

ALTER TABLE topics
    ADD CONSTRAINT FK_TOPICS_ON_TOPIC_ICON FOREIGN KEY (topic_icon_id) REFERENCES files (id);

ALTER TABLE transactions
    ADD CONSTRAINT FK_TRANSACTIONS_ON_CALL FOREIGN KEY (call_id) REFERENCES calls (id);

ALTER TABLE transactions
    ADD CONSTRAINT FK_TRANSACTIONS_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallets (id);

ALTER TABLE transactions
    ADD CONSTRAINT FK_TRANSACTIONS_ON_WITHDRAWAL_REQUEST FOREIGN KEY (withdrawal_request_id) REFERENCES withdrawal_requests (id);

ALTER TABLE users
    ADD CONSTRAINT FK_USERS_ON_PROFILE_PICTURE FOREIGN KEY (profile_picture_id) REFERENCES files (id);

ALTER TABLE wallets
    ADD CONSTRAINT FK_WALLETS_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE withdrawal_requests
    ADD CONSTRAINT FK_WITHDRAWAL_REQUESTS_ON_BANK_ACCOUNT FOREIGN KEY (bank_account_id) REFERENCES bank_accounts (id);

ALTER TABLE withdrawal_requests
    ADD CONSTRAINT FK_WITHDRAWAL_REQUESTS_ON_REQUESTER FOREIGN KEY (requester_id) REFERENCES users (id);

ALTER TABLE withdrawal_requests
    ADD CONSTRAINT FK_WITHDRAWAL_REQUESTS_ON_REVIEWER FOREIGN KEY (reviewer_id) REFERENCES users (id);

ALTER TABLE withdrawal_requests
    ADD CONSTRAINT FK_WITHDRAWAL_REQUESTS_ON_WALLET FOREIGN KEY (wallet_id) REFERENCES wallets (id);

ALTER TABLE user_languages
    ADD CONSTRAINT fk_uselan_on_language FOREIGN KEY (language_id) REFERENCES languages (id);

ALTER TABLE user_languages
    ADD CONSTRAINT fk_uselan_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (role_id) REFERENCES roles (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE user_topics
    ADD CONSTRAINT fk_usetop_on_topic FOREIGN KEY (topic_id) REFERENCES topics (id);

ALTER TABLE user_topics
    ADD CONSTRAINT fk_usetop_on_user FOREIGN KEY (user_id) REFERENCES users (id);