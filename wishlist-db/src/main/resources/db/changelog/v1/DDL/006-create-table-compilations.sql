--liquibase formatted sql

--changeset dev:006-create-table-compilations
--comment: Подборки подарков, привязанные к группе и категории
CREATE TABLE IF NOT EXISTS compilations
(
    id          UUID         NOT NULL DEFAULT uuidv7(),
    group_id    UUID         NOT NULL,
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    category_id INT          NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP    NOT NULL,
    CONSTRAINT compilations_pk PRIMARY KEY (id),
    CONSTRAINT compilations_category_id_fk FOREIGN KEY (category_id)
        REFERENCES categories(id),
    CONSTRAINT compilations_group_id_fk FOREIGN KEY (group_id)
        REFERENCES groups(id) ON DELETE CASCADE
);
--rollback DROP TABLE IF EXISTS compilations;
