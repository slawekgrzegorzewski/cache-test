DROP TABLE IF EXISTS cache_entry;

CREATE TABLE cache_entry
(
    key   VARCHAR(255) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);

INSERT INTO cache_entry
VALUES ('first', '1'),
       ('second', '2'),
       ('third', '3');