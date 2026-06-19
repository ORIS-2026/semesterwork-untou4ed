--liquibase formatted sql

--changeset id:001
--comment: Справочник категорий подборок подарков
CREATE TABLE IF NOT EXISTS categories
(
    id   BIGINT GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT categories_pk PRIMARY KEY (id)
);

--rollback DROP TABLE IF EXISTS categories;