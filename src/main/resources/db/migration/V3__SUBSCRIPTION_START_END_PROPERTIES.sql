ALTER TABLE subscriptions
    ADD subscription_end_date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE subscriptions
    ADD subscription_start_date TIMESTAMP WITHOUT TIME ZONE;

ALTER TABLE subscriptions
    ALTER COLUMN subscription_end_date SET NOT NULL;

ALTER TABLE subscriptions
    ALTER COLUMN subscription_start_date SET NOT NULL;

ALTER TABLE subscriptions
DROP
COLUMN subscription_date;