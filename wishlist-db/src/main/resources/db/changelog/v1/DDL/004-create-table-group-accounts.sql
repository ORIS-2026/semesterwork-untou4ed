--liquibase formatted sql

--changeset dev:004-create-table-group-accounts labels:ddl,group-accounts
--comment: Связующая таблица групп и аккаунтов с ролями участников
CREATE TABLE IF NOT EXISTS group_accounts
(
    group_id   UUID         NOT NULL,
    account_id UUID         NOT NULL,
    status     VARCHAR(128) NOT NULL DEFAULT 'member',
    CONSTRAINT group_accounts_pk PRIMARY KEY (group_id, account_id),
    CONSTRAINT group_accounts_group_id_fk FOREIGN KEY (group_id)
        REFERENCES groups(id) ON DELETE CASCADE,
    CONSTRAINT group_accounts_account_id_fk FOREIGN KEY (account_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT group_accounts_status_chk CHECK (status IN ('member', 'admin', 'creator'))
);
--rollback DROP TABLE IF EXISTS group_accounts;
