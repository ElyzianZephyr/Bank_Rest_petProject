--liquibase formatted sql

--changeset elyzian:003-alter-clients-add-status.sql
ALTER TABLE clients
    ADD COLUMN is_locked BOOLEAN DEFAULT FALSE NOT NULL;