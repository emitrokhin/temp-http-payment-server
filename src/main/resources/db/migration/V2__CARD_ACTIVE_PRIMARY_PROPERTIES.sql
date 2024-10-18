ALTER TABLE cards
    ADD is_active BOOLEAN;

ALTER TABLE cards
    ADD is_primary BOOLEAN;

ALTER TABLE cards
    ALTER COLUMN is_active SET NOT NULL;

ALTER TABLE cards
    ALTER COLUMN is_primary SET NOT NULL;