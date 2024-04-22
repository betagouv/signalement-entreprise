ALTER TABLE etablissements
    RENAME COLUMN denominationusuelleetablissement TO denomination;

ALTER TABLE etablissements
    ADD denominationUsuelle1UniteLegale VARCHAR,
    ADD denominationUsuelle2UniteLegale VARCHAR,
    ADD denominationUsuelle3UniteLegale VARCHAR,
    DROP anciennedenominationusuelleetablissement;


DROP INDEX etab_denom_trgm_idx;
DROP INDEX etab_enseigne_trgm_idx;
DROP INDEX etab_cp_idx;
DROP INDEX etab_siret_idx;
DROP INDEX etab_siren_idx;

CREATE INDEX etab_denom_trgm_gin_idx ON etablissements USING gin (denomination public.gin_trgm_ops);
CREATE INDEX etab_enseigne1_trgm_gin_idx ON etablissements USING gin (enseigne1etablissement public.gin_trgm_ops);
CREATE INDEX etab_enseigne2_trgm_gin_idx ON etablissements USING gin (enseigne2etablissement public.gin_trgm_ops);
CREATE INDEX etab_enseigne3_trgm_gin_idx ON etablissements USING gin (enseigne3etablissement public.gin_trgm_ops);
CREATE INDEX etab_denominationusuelle1unitelegale_trgm_gin_idx ON etablissements USING gin (denominationusuelle1unitelegale public.gin_trgm_ops);
CREATE INDEX etab_denominationusuelle2unitelegale_trgm_gin_idx ON etablissements USING gin (denominationusuelle2unitelegale public.gin_trgm_ops);
CREATE INDEX etab_denominationusuelle3unitelegale_trgm_gin_idx ON etablissements USING gin (denominationusuelle3unitelegale public.gin_trgm_ops);
CREATE INDEX etab_nomcommercialetablissement_trgm_gin_idx ON etablissements USING gin (nomcommercialetablissement public.gin_trgm_ops);

CREATE INDEX etab_cp_idx ON etablissements (codepostaletablissement);
CREATE INDEX etab_siren_idx ON etablissements (siren);