--liquibase formatted sql

--changeset dev:009-create-table-subscriptions
--comment: Подписки между пользователями; самоподписка запрещена CHECK-ограничением
CREATE TABLE IF NOT EXISTS subscriptions
(
    follower_id   UUID      NOT NULL,
    following_id  UUID      NOT NULL,
    subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT subscriptions_pk PRIMARY KEY (follower_id, following_id),
    CONSTRAINT subscriptions_follower_id_fk FOREIGN KEY (follower_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT subscriptions_following_id_fk FOREIGN KEY (following_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT subscriptions_no_self_follow_chk CHECK (follower_id <> following_id)
);

CREATE INDEX IF NOT EXISTS subscriptions_following_id_idx ON subscriptions (following_id);
--rollback DROP INDEX IF EXISTS subscriptions_following_id_idx;

--rollback DROP TABLE IF EXISTS subscriptions;
