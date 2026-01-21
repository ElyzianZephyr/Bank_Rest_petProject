--liquibase formatted sql

--changeset solodkov.s:001-create-cards-table.sql
CREATE SEQUENCE cards_seq INCREMENT BY 50;
CREATE TABLE cards
(

    id            BIGINT PRIMARY KEY      DEFAULT nextval('cards_seq'),
    card_number   VARCHAR(512   )   NOT NULL,
    balance       DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status        VARCHAR(20)    NOT NULL,
    validity_date DATE           NOT NULL,
    owner_id      BIGINT         NOT NULL,
    CONSTRAINT fk_cards_user FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_cards_owner ON cards (owner_id);

ALTER SEQUENCE cards_seq OWNED BY cards.id;

ALTER TABLE cards
    ADD CONSTRAINT cards_status_check CHECK (status IN ('ACTIVE', 'BLOCKED', 'EXPIRED'));

