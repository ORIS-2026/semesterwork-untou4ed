--liquibase formatted sql

--changeset dev:007-create-table-gifts
CREATE TABLE IF NOT EXISTS gifts
(
    id          UUID           NOT NULL DEFAULT uuidv7(),
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    link        TEXT,
    price       DECIMAL(16, 2),
    owner_id    UUID           NOT NULL,
    owner_type  VARCHAR(50)    NOT NULL,
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL,
    CONSTRAINT gifts_pk PRIMARY KEY (id),
    CONSTRAINT gifts_owner_type_chk CHECK (owner_type IN ('wishlist', 'compilation'))
);

CREATE INDEX IF NOT EXISTS gifts_owner_idx ON gifts (owner_id, owner_type);
--rollback DROP INDEX IF EXISTS gifts_owner_idx;

--rollback DROP TABLE IF EXISTS gifts;
