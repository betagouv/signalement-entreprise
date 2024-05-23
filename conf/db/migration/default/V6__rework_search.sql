DROP INDEX etab_denom_trgm_gin_idx;
DROP INDEX etab_enseigne1_trgm_gin_idx;
DROP INDEX etab_enseigne2_trgm_gin_idx;
DROP INDEX etab_enseigne3_trgm_gin_idx;
DROP INDEX etab_denominationusuelle1unitelegale_trgm_gin_idx;
DROP INDEX etab_denominationusuelle2unitelegale_trgm_gin_idx;
DROP INDEX etab_denominationusuelle3unitelegale_trgm_gin_idx;
DROP INDEX etab_nomcommercialetablissement_trgm_gin_idx;

ALTER TABLE etablissements ADD search_column_trgm TEXT GENERATED ALWAYS AS (
    denomination
        || ' '
        || coalesce(denominationusuelle1unitelegale, '')
        || ' '
        || coalesce(denominationusuelle2unitelegale, '')
        || ' '
        || coalesce(denominationusuelle3unitelegale, '')
        || ' '
        || coalesce(nomcommercialetablissement, '')
        || ' '
        || coalesce(enseigne1etablissement, '')
        || ' '
        || coalesce(enseigne2etablissement, '')
        || ' '
        || coalesce(enseigne3etablissement, '')
    ) STORED;
CREATE INDEX etab_search_column_trgm_gin_idx ON etablissements USING gin (search_column_trgm public.gin_trgm_ops);

ALTER TABLE activites DROP CONSTRAINT IF EXISTS activites_code_key;
ALTER TABLE activites ADD CONSTRAINT activites_code_pk PRIMARY KEY (code);