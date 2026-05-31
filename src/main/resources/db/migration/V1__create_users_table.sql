CREATE TABLE users (
    id         BIGSERIAL       PRIMARY KEY,
    email      VARCHAR(254)    NOT NULL,
    password   VARCHAR(255)    NOT NULL,
    username   VARCHAR(50)     NOT NULL,
    enabled    BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP       NOT NULL,
    updated_at TIMESTAMP       NOT NULL,

    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users (email);