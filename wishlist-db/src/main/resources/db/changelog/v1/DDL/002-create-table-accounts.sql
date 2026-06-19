--liquibase formatted sql

--changeset dev:002-create-table-accounts labels:ddl,accounts
--comment: Таблица пользовательских аккаунтов
CREATE TABLE IF NOT EXISTS users
(
    id            UUID         NOT NULL DEFAULT uuidv7(),
    name           varchar(50) NOT NULL,
    surname        varchar(50),
    username       VARCHAR(50) NOT NULL,
    phone_number   VARCHAR(12) NOT NULL,
    email          VARCHAR(100),
    avatar_url     varchar(255),
    enabled        bool NOT NULL DEFAULT false,
    created_at     TIMESTAMP NOT NULL DEFAULT current_timestamp,
    updated_at     TIMESTAMP NOT NULL DEFAULT current_timestamp,
    deleted_at     TIMESTAMP,
----------------------------------------------------------------------------
    CONSTRAINT pk_users_id          PRIMARY KEY (id),
    CONSTRAINT uq_users_login       UNIQUE (username),
    CONSTRAINT uq_users_email       UNIQUE (email),
    CONSTRAINT uq_users_phone       UNIQUE (phone_number)
);

comment on table  users                        is 'Таблица пользователей';
comment on column users.id                     is 'Уникальный идентификатор пользователя (UUID v7)';
comment on column users.username               is 'Логин - используется для входа';
comment on column users.email                  is 'Почта - может быть пустой';
comment on column users.phone_number           is 'Уникальный 11 символьный российский номер телефона пользователя (учитывая первую 8)';
comment on column users.avatar_url             is 'URL к изображению аватарки';
comment on column users.enabled                is 'Аккаунт включается после подтверждения номера, и отключается с удалением';
comment on column users.created_at             is 'Дата создания аккаунта';
comment on column users.updated_at             is 'Дата последнего изменения аккаунта';
comment on column users.deleted_at             is 'Дата удаления аккаунта';
--rollback DROP TABLE IF EXISTS accounts;