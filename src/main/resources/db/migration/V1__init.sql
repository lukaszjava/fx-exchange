CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    pln NUMERIC(18,2) NOT NULL,
    usd NUMERIC(18,4) NOT NULL,
    version BIGINT NOT NULL
);
