DROP TABLE IF EXISTS cache_entry;

CREATE TABLE cache_entry
(
    entry_key   VARCHAR(255) PRIMARY KEY,
    entry_value VARCHAR(255) NOT NULL
);