--liquibase formatted sql

--changeset elyzian:004-modify-card-number-column.sql
CREATE SEQUENCE cards_number_seq
    START 1
    INCREMENT BY 1;