# --- !Ups

CREATE TABLE IF NOT EXISTS etablissements_insee_import
(
    id          UUID PRIMARY KEY,
    started_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    ended_at    TIMESTAMP WITHOUT TIME ZONE,
    errors      VARCHAR
);

# --- !Downs
