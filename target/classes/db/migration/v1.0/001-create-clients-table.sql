--liquibase formatted sql

--changeset solodkov.s:001-create-clients-table.sql
CREATE SEQUENCE clients_seq INCREMENT BY 50;
CREATE TABLE clients
(
    id       BIGINT PRIMARY KEY   DEFAULT nextval('clients_seq'),
    username VARCHAR(50) NOT NULL,
    password TEXT        NOT NULL,
    role     VARCHAR(32) NOT NULL DEFAULT 'CLIENT',
    CONSTRAINT uk_clients_username UNIQUE (username)
);

ALTER SEQUENCE clients_seq OWNED BY clients.id;