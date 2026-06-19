--liquibase formatted sql

--changeset id:dml-001
--comment: Начальные категории подборок

INSERT INTO categories (name) VALUES
    ('Дни рождения'),
    ('Новый год'),
    ('Свадьба'),
    ('Детям'),
    ('Техника и гаджеты'),
    ('Книги'),
    ('Спорт и активный отдых'),
    ('Красота и уход'),
    ('Дом и интерьер'),
    ('Путешествия'),
    ('Игры и хобби'),
    ('Еда и напитки');

--rollback DELETE FROM categories;
