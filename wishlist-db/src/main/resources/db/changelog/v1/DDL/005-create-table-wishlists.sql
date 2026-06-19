--liquibase formatted sql

--changeset dev:005-create-table-wishlists labels:ddl,wishlists
--comment: Вишлисты пользователей; один аккаунт — один вишлист (UNIQUE на author_id)
CREATE TABLE IF NOT EXISTS wishlists
(
    id          UUID         NOT NULL DEFAULT uuidv7(),
    title       VARCHAR(255) NOT NULL DEFAULT 'Хочу эти подарочки',
    author_id   UUID         NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT wishlists_pk PRIMARY KEY (id),
    CONSTRAINT wishlists_author_id_uq UNIQUE (author_id),
    CONSTRAINT wishlists_author_id_fk FOREIGN KEY (author_id)
        REFERENCES users(id) ON DELETE CASCADE
);
--rollback DROP TABLE IF EXISTS wishlists;
