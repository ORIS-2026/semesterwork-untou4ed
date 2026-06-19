--liquibase formatted sql

--changeset dev:003-create-table-groups
--comment: Таблица групп пользователей
CREATE TABLE IF NOT EXISTS groups
(
    id          UUID         NOT NULL DEFAULT uuidv7(),
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT groups_pk PRIMARY KEY (id),
    CONSTRAINT groups_name_uq UNIQUE (name)
);
--rollback DROP TABLE IF EXISTS groups;
