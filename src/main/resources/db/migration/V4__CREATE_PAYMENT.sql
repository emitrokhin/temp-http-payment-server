CREATE TABLE payments
(
    id             UUID                        NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    telegram_id    BIGINT                      NOT NULL,
    payment_status SMALLINT                    NOT NULL,
    CONSTRAINT pk_payments PRIMARY KEY (id)
);