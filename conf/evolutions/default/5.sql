# --- !Ups

ALTER TABLE etablissements_import_info
    ALTER COLUMN file_url DROP NOT NULL;

ALTER TABLE etablissements_import_info
    ALTER COLUMN file_name DROP NOT NULL;

# --- !Downs
