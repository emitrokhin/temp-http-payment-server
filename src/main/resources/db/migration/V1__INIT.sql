CREATE TABLE all_incoming_users
(
    id                 UUID                        NOT NULL,
    created_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at         TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id        BIGINT                      NOT NULL,
    first_name         VARCHAR(255)                NOT NULL,
    last_name          VARCHAR(100),
    username           VARCHAR(100),
    language_code      VARCHAR(10),
    allows_write_to_pm BOOLEAN                     NOT NULL,
    photo_url          VARCHAR(255),
    CONSTRAINT pk_all_incoming_users PRIMARY KEY (id)
);

CREATE TABLE first_run
(
    id          UUID                        NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id BIGINT                      NOT NULL,
    CONSTRAINT pk_firstrun PRIMARY KEY (id)
);

CREATE TABLE profiles
(
    id            UUID                        NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id   BIGINT                      NOT NULL,
    first_name    VARCHAR(100)                NOT NULL,
    last_name     VARCHAR(100)                NOT NULL,
    phone         VARCHAR(15)                 NOT NULL,
    email         VARCHAR(255)                NOT NULL,
    date_of_birth date,
    city          VARCHAR(255),
    profession    VARCHAR(255),
    CONSTRAINT pk_profiles PRIMARY KEY (id)
);

CREATE TABLE subscriptions
(
    id                  UUID                        NOT NULL,
    created_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id         BIGINT                      NOT NULL,
    subscription_date   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    subscription_status SMALLINT                    NOT NULL,
    CONSTRAINT pk_subscriptions PRIMARY KEY (id)
);

CREATE TABLE transactions
(
    id               UUID                        NOT NULL,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id      BIGINT                      NOT NULL,
    transaction_id   BIGINT                      NOT NULL,
    amount           DECIMAL(19, 2)              NOT NULL,
    currency         VARCHAR(255)                NOT NULL,
    payment_amount   DECIMAL                     NOT NULL,
    payment_currency VARCHAR(255)                NOT NULL,
    date_time        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    card_id          VARCHAR(255),
    card_first_six   INTEGER                     NOT NULL,
    card_last_four   INTEGER                     NOT NULL,
    card_type        VARCHAR(255)                NOT NULL,
    card_exp_date    VARCHAR(255)                NOT NULL,
    test_mode        SMALLINT                    NOT NULL,
    reason           VARCHAR(255),
    reason_code      INTEGER,
    status           VARCHAR(255)                NOT NULL,
    operation_type   VARCHAR(255)                NOT NULL,
    invoice_id       VARCHAR(255)                NOT NULL,
    subscription_id  VARCHAR(255),
    email            VARCHAR(255),
    token            VARCHAR(255),
    rrn              VARCHAR(255),
    CONSTRAINT pk_transactions PRIMARY KEY (id)
);

CREATE TABLE cards
(
    id             UUID                        NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id    BIGINT                      NOT NULL,
    card_id        VARCHAR(255)                NOT NULL,
    card_last_four INTEGER                     NOT NULL,
    card_type      VARCHAR(255)                NOT NULL,
    card_exp_date  VARCHAR(255)                NOT NULL,
    token          VARCHAR(255),
    CONSTRAINT pk_cards PRIMARY KEY (id)
);

ALTER TABLE first_run
    ADD CONSTRAINT uc_firstrun_telegram UNIQUE (telegram_id);

ALTER TABLE profiles
    ADD CONSTRAINT uc_profiles_telegramid UNIQUE (telegram_id);

ALTER TABLE transactions
    ADD CONSTRAINT uc_transactions_transactionid UNIQUE (transaction_id);