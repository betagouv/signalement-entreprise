ALTER TABLE etablissements
    ADD codedepartement varchar;

CREATE INDEX etab_dpt_idx ON etablissements(codedepartement text_ops);
