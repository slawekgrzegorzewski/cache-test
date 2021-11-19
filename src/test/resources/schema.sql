DROP TABLE IF EXISTS cache_entry;

CREATE TABLE cache_entry
(
    key   VARCHAR(255) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);