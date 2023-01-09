CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

create table if not exists etablissements
(
    id                                             uuid default uuid_generate_v4(),
    siren                                          varchar,
    nic                                            varchar,
    siret                                          varchar not null
        constraint etablissements_pk
            primary key,
    statutdiffusionetablissement                   varchar,
    datecreationetablissement                      varchar,
    trancheeffectifsetablissement                  varchar,
    anneeeffectifsetablissement                    varchar,
    activiteprincipaleregistremetiersetablissement varchar,
    datederniertraitementetablissement             varchar,
    etablissementsiege                             varchar,
    nombreperiodesetablissement                    varchar,
    complementadresseetablissement                 varchar,
    numerovoieetablissement                        varchar,
    indicerepetitionetablissement                  varchar,
    typevoieetablissement                          varchar,
    libellevoieetablissement                       varchar,
    codepostaletablissement                        varchar,
    libellecommuneetablissement                    varchar,
    libellecommuneetrangeretablissement            varchar,
    distributionspecialeetablissement              varchar,
    codecommuneetablissement                       varchar,
    codecedexetablissement                         varchar,
    libellecedexetablissement                      varchar,
    codepaysetrangeretablissement                  varchar,
    libellepaysetrangeretablissement               varchar,
    complementadresse2etablissement                varchar,
    numerovoie2etablissement                       varchar,
    indicerepetition2etablissement                 varchar,
    typevoie2etablissement                         varchar,
    libellevoie2etablissement                      varchar,
    codepostal2etablissement                       varchar,
    libellecommune2etablissement                   varchar,
    libellecommuneetranger2etablissement           varchar,
    distributionspeciale2etablissement             varchar,
    codecommune2etablissement                      varchar,
    codecedex2etablissement                        varchar,
    libellecedex2etablissement                     varchar,
    codepaysetranger2etablissement                 varchar,
    libellepaysetranger2etablissement              varchar,
    datedebut                                      varchar,
    etatadministratifetablissement                 varchar,
    enseigne1etablissement                         varchar,
    enseigne2etablissement                         varchar,
    enseigne3etablissement                         varchar,
    denominationusuelleetablissement               varchar,
    activiteprincipaleetablissement                varchar,
    nomenclatureactiviteprincipaleetablissement    varchar,
    caractereemployeuretablissement                varchar
);

create unique index siret_idx
    on etablissements (siret);

create index etab_denom_trgm_idx
    on etablissements using gist (denominationusuelleetablissement public.gist_trgm_ops);

create index etab_enseigne_trgm_idx
    on etablissements using gist (enseigne1etablissement public.gist_trgm_ops);

create index etab_cp_idx
    on etablissements using gin (codepostaletablissement);

create index etab_siret_idx
    on etablissements using gin (siret);

create index etab_siren_idx
    on etablissements using gin (siren);

create table if not exists activites
(
    code    varchar UNIQUE,
    libelle varchar
);

create table if not exists etablissements_import_info
(
    id           uuid                    not null
        primary key,
    file_name    varchar,
    file_url     varchar,
    lines_count  integer                 not null,
    lines_done   integer                 not null,
    started_at   timestamp default now() not null,
    ended_at     timestamp,
    errors       varchar,
    last_updated timestamp
);


INSERT INTO activites (code, libelle) VALUES ('SECTION A', 'AGRICULTURE, SYLVICULTURE ET PÊCHE') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01', 'Culture et production animale, chasse et services annexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.1', 'Cultures non permanentes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.11', 'Culture de céréales (à l''exception du riz), de légumineuses et de graines oléagineuses') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.11Z', 'Culture de céréales (à l''exception du riz), de légumineuses et de graines oléagineuses') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.12', 'Culture du riz') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.12Z', 'Culture du riz') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.13', 'Culture de légumes, de melons, de racines et de tubercules') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.13Z', 'Culture de légumes, de melons, de racines et de tubercules') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.14', 'Culture de la canne à sucre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.14Z', 'Culture de la canne à sucre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.15', 'Culture du tabac') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.15Z', 'Culture du tabac') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.16', 'Culture de plantes à fibres') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.16Z', 'Culture de plantes à fibres') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.19', 'Autres cultures non permanentes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.19Z', 'Autres cultures non permanentes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.2', 'Cultures permanentes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.21', 'Culture de la vigne') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.21Z', 'Culture de la vigne') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.22', 'Culture de fruits tropicaux et subtropicaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.22Z', 'Culture de fruits tropicaux et subtropicaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.23', 'Culture d''agrumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.23Z', 'Culture d''agrumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.24', 'Culture de fruits à pépins et à noyau') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.24Z', 'Culture de fruits à pépins et à noyau') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.25', 'Culture d''autres fruits d''arbres ou d''arbustes et de fruits à coque') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.25Z', 'Culture d''autres fruits d''arbres ou d''arbustes et de fruits à coque') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.26', 'Culture de fruits oléagineux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.26Z', 'Culture de fruits oléagineux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.27', 'Culture de plantes à boissons') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.27Z', 'Culture de plantes à boissons') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.28', 'Culture de plantes à épices, aromatiques, médicinales et pharmaceutiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.28Z', 'Culture de plantes à épices, aromatiques, médicinales et pharmaceutiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.29', 'Autres cultures permanentes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.29Z', 'Autres cultures permanentes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.3', 'Reproduction de plantes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.30', 'Reproduction de plantes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.30Z', 'Reproduction de plantes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.4', 'Production animale') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.41', 'Élevage de vaches laitières') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.41Z', 'Élevage de vaches laitières') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.42', 'Élevage d''autres bovins et de buffles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.42Z', 'Élevage d''autres bovins et de buffles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.43', 'Élevage de chevaux et d''autres équidés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.43Z', 'Élevage de chevaux et d''autres équidés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.44', 'Élevage de chameaux et d''autres camélidés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.44Z', 'Élevage de chameaux et d''autres camélidés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.45', 'Élevage d''ovins et de caprins') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.45Z', 'Élevage d''ovins et de caprins') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.46', 'Élevage de porcins') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.46Z', 'Élevage de porcins') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.47', 'Élevage de volailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.47Z', 'Élevage de volailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.49', 'Élevage d''autres animaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.49Z', 'Élevage d''autres animaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.5', 'Culture et élevage associés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.50', 'Culture et élevage associés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.50Z', 'Culture et élevage associés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.6', 'Activités de soutien à l''agriculture et traitement primaire des récoltes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.61', 'Activités de soutien aux cultures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.61Z', 'Activités de soutien aux cultures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.62', 'Activités de soutien à la production animale') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.62Z', 'Activités de soutien à la production animale') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.63', 'Traitement primaire des récoltes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.63Z', 'Traitement primaire des récoltes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.64', 'Traitement des semences') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.64Z', 'Traitement des semences') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.7', 'Chasse, piégeage et services annexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.70', 'Chasse, piégeage et services annexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('01.70Z', 'Chasse, piégeage et services annexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02', 'Sylviculture et exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.1', 'Sylviculture et autres activités forestières') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.10', 'Sylviculture et autres activités forestières') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.10Z', 'Sylviculture et autres activités forestières') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.2', 'Exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.20', 'Exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.20Z', 'Exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.3', 'Récolte de produits forestiers non ligneux poussant à l''état sauvage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.30', 'Récolte de produits forestiers non ligneux poussant à l''état sauvage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.30Z', 'Récolte de produits forestiers non ligneux poussant à l''état sauvage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.4', 'Services de soutien à l''exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.40', 'Services de soutien à l''exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('02.40Z', 'Services de soutien à l''exploitation forestière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03', 'Pêche et aquaculture') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.1', 'Pêche') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.11', 'Pêche en mer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.11Z', 'Pêche en mer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.12', 'Pêche en eau douce') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.12Z', 'Pêche en eau douce') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.2', 'Aquaculture') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.21', 'Aquaculture en mer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.21Z', 'Aquaculture en mer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.22', 'Aquaculture en eau douce') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('03.22Z', 'Aquaculture en eau douce') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('SECTION B', 'INDUSTRIES EXTRACTIVES') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05', 'Extraction de houille et de lignite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05.1', 'Extraction de houille') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05.10', 'Extraction de houille') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05.10Z', 'Extraction de houille') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05.2', 'Extraction de lignite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05.20', 'Extraction de lignite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('05.20Z', 'Extraction de lignite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06', 'Extraction d''hydrocarbures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06.1', 'Extraction de pétrole brut') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06.10', 'Extraction de pétrole brut') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06.10Z', 'Extraction de pétrole brut') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06.2', 'Extraction de gaz naturel') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06.20', 'Extraction de gaz naturel') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('06.20Z', 'Extraction de gaz naturel') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07', 'Extraction de minerais métalliques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.1', 'Extraction de minerais de fer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.10', 'Extraction de minerais de fer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.10Z', 'Extraction de minerais de fer') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.2', 'Extraction de minerais de métaux non ferreux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.21', 'Extraction de minerais d''uranium et de thorium') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.21Z', 'Extraction de minerais d''uranium et de thorium') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.29', 'Extraction d''autres minerais de métaux non ferreux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('07.29Z', 'Extraction d''autres minerais de métaux non ferreux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08', 'Autres industries extractives') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.1', 'Extraction de pierres, de sables et d''argiles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.11', 'Extraction de pierres ornementales et de construction, de calcaire industriel, de gypse, de craie et d''ardoise') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.11Z', 'Extraction de pierres ornementales et de construction, de calcaire industriel, de gypse, de craie et d''ardoise') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.12', 'Exploitation de gravières et sablières, extraction d’argiles et de kaolin') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.12Z', 'Exploitation de gravières et sablières, extraction d’argiles et de kaolin') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.9', 'Activités extractives n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.91', 'Extraction des minéraux chimiques et d''engrais minéraux ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.91Z', 'Extraction des minéraux chimiques et d''engrais minéraux ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.92', 'Extraction de tourbe') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.92Z', 'Extraction de tourbe') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.93', 'Production de sel ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.93Z', 'Production de sel ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.99', 'Autres activités extractives n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('08.99Z', 'Autres activités extractives n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09', 'Services de soutien aux industries extractives') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09.1', 'Activités de soutien à l''extraction d''hydrocarbures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09.10', 'Activités de soutien à l''extraction d''hydrocarbures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09.10Z', 'Activités de soutien à l''extraction d''hydrocarbures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09.9', 'Activités de soutien aux autres industries extractives') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09.90', 'Activités de soutien aux autres industries extractives ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('09.90Z', 'Activités de soutien aux autres industries extractives ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('SECTION C', 'INDUSTRIE MANUFACTURIÈRE') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10', 'Industries alimentaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.1', 'Transformation et conservation de la viande et préparation de produits à base de viande') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.11', 'Transformation et conservation de la viande de boucherie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.11Z', 'Transformation et conservation de la viande de boucherie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.12', 'Transformation et conservation de la viande de volaille') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.12Z', 'Transformation et conservation de la viande de volaille') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.13', 'Préparation de produits à base de viande') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.13A', 'Préparation industrielle de produits à base de viande') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.13B', 'Charcuterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.2', 'Transformation et conservation de poisson, de crustacés et de mollusques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.20', 'Transformation et conservation de poisson, de crustacés et de mollusques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.20Z', 'Transformation et conservation de poisson, de crustacés et de mollusques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.3', 'Transformation et conservation de fruits et légumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.31', 'Transformation et conservation de pommes de terre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.31Z', 'Transformation et conservation de pommes de terre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.32', 'Préparation de jus de fruits et légumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.32Z', 'Préparation de jus de fruits et légumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.39', 'Autre transformation et conservation de fruits et légumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.39A', 'Autre transformation et conservation de légumes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.39B', 'Transformation et conservation de fruits') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.4', 'Fabrication d’huiles et graisses végétales et animales') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.41', 'Fabrication d''huiles et graisses') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.41A', 'Fabrication d''huiles et graisses brutes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.41B', 'Fabrication d''huiles et graisses raffinées') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.42', 'Fabrication de margarine et graisses comestibles similaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.42Z', 'Fabrication de margarine et graisses comestibles similaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.5', 'Fabrication de produits laitiers') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.51', 'Exploitation de laiteries et fabrication de fromage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.51A', 'Fabrication de lait liquide et de produits frais') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.51B', 'Fabrication de beurre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.51C', 'Fabrication de fromage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.51D', 'Fabrication d''autres produits laitiers') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.52', 'Fabrication de glaces et sorbets') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.52Z', 'Fabrication de glaces et sorbets') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.6', 'Travail des grains ; fabrication de produits amylacés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.61', 'Travail des grains') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.61A', 'Meunerie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.61B', 'Autres activités du travail des grains') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.62', 'Fabrication de produits amylacés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.62Z', 'Fabrication de produits amylacés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.7', 'Fabrication de produits de boulangerie-pâtisserie et de pâtes alimentaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.71', 'Fabrication de pain et de pâtisserie fraîche') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.71A', 'Fabrication industrielle de pain et de pâtisserie fraîche') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.71B', 'Cuisson de produits de boulangerie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.71C', 'Boulangerie et boulangerie-pâtisserie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.71D', 'Pâtisserie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.72', 'Fabrication de biscuits, biscottes et pâtisseries de conservation') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.72Z', 'Fabrication de biscuits, biscottes et pâtisseries de conservation') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.73', 'Fabrication de pâtes alimentaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.73Z', 'Fabrication de pâtes alimentaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.8', 'Fabrication d''autres produits alimentaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.81', 'Fabrication de sucre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.81Z', 'Fabrication de sucre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.32Z', 'Laminage à froid de feuillards') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.82', 'Fabrication de cacao, chocolat et de produits de confiserie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.82Z', 'Fabrication de cacao, chocolat et de produits de confiserie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.83', 'Transformation du thé et du café') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.83Z', 'Transformation du thé et du café') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.84', 'Fabrication de condiments et assaisonnements') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.84Z', 'Fabrication de condiments et assaisonnements') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.85', 'Fabrication de plats préparés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.85Z', 'Fabrication de plats préparés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.86', 'Fabrication d''aliments homogénéisés et diététiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.86Z', 'Fabrication d''aliments homogénéisés et diététiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.89', 'Fabrication d''autres produits alimentaires n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.89Z', 'Fabrication d''autres produits alimentaires n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.9', 'Fabrication d''aliments pour animaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('31', 'Fabrication de meubles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.91', 'Fabrication d''aliments pour animaux de ferme') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.91Z', 'Fabrication d''aliments pour animaux de ferme') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.92', 'Fabrication d''aliments pour animaux de compagnie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('10.92Z', 'Fabrication d''aliments pour animaux de compagnie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11', 'Fabrication de boissons') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.0', 'Fabrication de boissons') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.01', 'Production de boissons alcooliques distillées') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.01Z', 'Production de boissons alcooliques distillées') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.02', 'Production de vin (de raisin)') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.02A', 'Fabrication de vins effervescents') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.02B', 'Vinification') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.03', 'Fabrication de cidre et de vins de fruits  ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.03Z', 'Fabrication de cidre et de vins de fruits ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.04', 'Production d''autres boissons fermentées non distillées') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.04Z', 'Production d''autres boissons fermentées non distillées') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.05', 'Fabrication de bière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.05Z', 'Fabrication de bière') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.06', 'Fabrication de malt') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.06Z', 'Fabrication de malt') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.07', 'Industrie des eaux minérales et autres eaux embouteillées et des boissons rafraîchissantes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.07A', 'Industrie des eaux de table') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('11.07B', 'Production de boissons rafraîchissantes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('12', 'Fabrication de produits à base de tabac') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('12.0', 'Fabrication de produits à base de tabac') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('12.00', 'Fabrication de produits à base de tabac') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('12.00Z', 'Fabrication de produits à base de tabac') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13', 'Fabrication de textiles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.1', 'Préparation de fibres textiles et filature') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.10', 'Préparation de fibres textiles et filature') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.10Z', 'Préparation de fibres textiles et filature') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.2', 'Tissage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.20', 'Tissage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.20Z', 'Tissage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.3', 'Ennoblissement textile') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.30', 'Ennoblissement textile') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.30Z', 'Ennoblissement textile') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.9', 'Fabrication d''autres textiles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.91', 'Fabrication d''étoffes à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.91Z', 'Fabrication d''étoffes à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.92', 'Fabrication d''articles textiles, sauf habillement') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.92Z', 'Fabrication d''articles textiles, sauf habillement') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.93', 'Fabrication de tapis et moquettes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.93Z', 'Fabrication de tapis et moquettes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.94', 'Fabrication de ficelles, cordes et filets') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.94Z', 'Fabrication de ficelles, cordes et filets') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.95', 'Fabrication de non-tissés, sauf habillement') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.95Z', 'Fabrication de non-tissés, sauf habillement') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.96', 'Fabrication d''autres textiles techniques et industriels') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.96Z', 'Fabrication d''autres textiles techniques et industriels') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.99', 'Fabrication d''autres textiles n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('13.99Z', 'Fabrication d''autres textiles n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14', 'Industrie de l''habillement') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.1', 'Fabrication de vêtements, autres qu''en fourrure') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.11', 'Fabrication de vêtements en cuir') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.11Z', 'Fabrication de vêtements en cuir') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.12', 'Fabrication de vêtements de travail') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.12Z', 'Fabrication de vêtements de travail') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.13', 'Fabrication de vêtements de dessus') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.13Z', 'Fabrication de vêtements de dessus') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.14', 'Fabrication de vêtements de dessous') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.14Z', 'Fabrication de vêtements de dessous') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.19', 'Fabrication d''autres vêtements et accessoires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.19Z', 'Fabrication d''autres vêtements et accessoires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.2', 'Fabrication d''articles en fourrure') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.20', 'Fabrication d''articles en fourrure') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.20Z', 'Fabrication d''articles en fourrure') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.3', 'Fabrication d''articles à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.31', 'Fabrication d''articles chaussants à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.31Z', 'Fabrication d''articles chaussants à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.39', 'Fabrication d''autres articles à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('14.39Z', 'Fabrication d''autres articles à mailles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15', 'Industrie du cuir et de la chaussure') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.1', 'Apprêt et tannage des cuirs ; préparation et teinture des fourrures ; fabrication d''articles de voyage, de maroquinerie et de sellerie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.11', 'Apprêt et tannage des cuirs ; préparation et teinture des fourrures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.11Z', 'Apprêt et tannage des cuirs ; préparation et teinture des fourrures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.12', 'Fabrication d''articles de voyage, de maroquinerie et de sellerie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.12Z', 'Fabrication d''articles de voyage, de maroquinerie et de sellerie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.2', 'Fabrication de chaussures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.20', 'Fabrication de chaussures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('15.20Z', 'Fabrication de chaussures') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16', 'Travail du bois et fabrication d''articles en bois et en liège, à l’exception des meubles ; fabrication d’articles en vannerie et sparterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.1', 'Sciage et rabotage du bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.10', 'Sciage et rabotage du bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.10A', 'Sciage et rabotage du bois, hors imprégnation') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.10B', 'Imprégnation du bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.2', 'Fabrication d''articles en bois, liège, vannerie et sparterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.21', 'Fabrication de placage et de panneaux de bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.21Z', 'Fabrication de placage et de panneaux de bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.22', 'Fabrication de parquets assemblés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.22Z', 'Fabrication de parquets assemblés') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.23', 'Fabrication de charpentes et d''autres menuiseries') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.23Z', 'Fabrication de charpentes et d''autres menuiseries') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.24', 'Fabrication d''emballages en bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.24Z', 'Fabrication d''emballages en bois') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.29', 'Fabrication d''objets divers en bois ; fabrication d''objets en liège, vannerie et sparterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('16.29Z', 'Fabrication d''objets divers en bois ; fabrication d''objets en liège, vannerie et sparterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17', 'Industrie du papier et du carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.1', 'Fabrication de pâte à papier, de papier et de carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.11', 'Fabrication de pâte à papier') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.11Z', 'Fabrication de pâte à papier') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.12', 'Fabrication de papier et de carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.12Z', 'Fabrication de papier et de carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.2', 'Fabrication d''articles en papier ou en carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.21', 'Fabrication de papier et carton ondulés et d''emballages en papier ou en carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.21A', 'Fabrication de carton ondulé') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.21B', 'Fabrication de cartonnages ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.21C', 'Fabrication d''emballages en papier') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.22', 'Fabrication d''articles en papier à usage sanitaire ou domestique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.22Z', 'Fabrication d''articles en papier à usage sanitaire ou domestique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.23', 'Fabrication d''articles de papeterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.23Z', 'Fabrication d''articles de papeterie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.24', 'Fabrication de papiers peints') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.24Z', 'Fabrication de papiers peints') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.29', 'Fabrication d''autres articles en papier ou en carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('17.29Z', 'Fabrication d''autres articles en papier ou en carton') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18', 'Imprimerie et reproduction d''enregistrements') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.1', 'Imprimerie et services annexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.11', 'Imprimerie de journaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.11Z', 'Imprimerie de journaux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.12', 'Autre imprimerie (labeur)') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.12Z', 'Autre imprimerie (labeur)') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.13', 'Activités de pré-presse ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.13Z', 'Activités de pré-presse ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.14', 'Reliure et activités connexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.14Z', 'Reliure et activités connexes') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.2', 'Reproduction d''enregistrements') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.20', 'Reproduction d''enregistrements') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('18.20Z', 'Reproduction d''enregistrements') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19', 'Cokéfaction et raffinage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19.1', 'Cokéfaction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19.10', 'Cokéfaction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19.10Z', 'Cokéfaction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19.2', 'Raffinage du pétrole') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19.20', 'Raffinage du pétrole') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('19.20Z', 'Raffinage du pétrole') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20', 'Industrie chimique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.1', 'Fabrication de produits chimiques de base, de produits azotés et d''engrais, de matières plastiques de base et de caoutchouc synthétique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.11', 'Fabrication de gaz industriels') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.11Z', 'Fabrication de gaz industriels') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.12', 'Fabrication de colorants et de pigments') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.12Z', 'Fabrication de colorants et de pigments') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.13', 'Fabrication d''autres produits chimiques inorganiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.13A', 'Enrichissement et  retraitement de matières nucléaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.13B', 'Fabrication d''autres produits chimiques inorganiques de base n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.14', 'Fabrication d''autres produits chimiques organiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.14Z', 'Fabrication d''autres produits chimiques organiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.15', 'Fabrication de produits azotés et d''engrais') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.15Z', 'Fabrication de produits azotés et d''engrais') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.16', 'Fabrication de matières plastiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.16Z', 'Fabrication de matières plastiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.17', 'Fabrication de caoutchouc synthétique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.17Z', 'Fabrication de caoutchouc synthétique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.2', 'Fabrication de pesticides et d’autres produits agrochimiques ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.20', 'Fabrication de pesticides et d’autres produits agrochimiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.20Z', 'Fabrication de pesticides et d’autres produits agrochimiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.3', 'Fabrication de peintures, vernis, encres et mastics') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.30', 'Fabrication de peintures, vernis, encres et mastics') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.30Z', 'Fabrication de peintures, vernis, encres et mastics') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.4', 'Fabrication de savons, de produits d''entretien et de parfums') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.41', 'Fabrication de savons, détergents et produits d''entretien') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.41Z', 'Fabrication de savons, détergents et produits d''entretien') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.42', 'Fabrication de parfums et de produits pour la toilette') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.42Z', 'Fabrication de parfums et de produits pour la toilette') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.5', 'Fabrication d''autres produits chimiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.51', 'Fabrication de produits explosifs') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.51Z', 'Fabrication de produits explosifs') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.52', 'Fabrication de colles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.52Z', 'Fabrication de colles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.53', 'Fabrication d''huiles essentielles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.53Z', 'Fabrication d''huiles essentielles') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.59', 'Fabrication d''autres produits chimiques n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.59Z', 'Fabrication d''autres produits chimiques n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.6', 'Fabrication de fibres artificielles ou synthétiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.60', 'Fabrication de fibres artificielles ou synthétiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('20.60Z', 'Fabrication de fibres artificielles ou synthétiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21', 'Industrie pharmaceutique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21.1', 'Fabrication de produits pharmaceutiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21.10', 'Fabrication de produits pharmaceutiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21.10Z', 'Fabrication de produits pharmaceutiques de base') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21.2', 'Fabrication de préparations pharmaceutiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21.20', 'Fabrication de préparations pharmaceutiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('21.20Z', 'Fabrication de préparations pharmaceutiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22', 'Fabrication de produits en caoutchouc et en plastique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.1', 'Fabrication de produits en caoutchouc') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.11', 'Fabrication et rechapage de pneumatiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.11Z', 'Fabrication et rechapage de pneumatiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.19', 'Fabrication d''autres articles en caoutchouc') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.19Z', 'Fabrication d''autres articles en caoutchouc') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.2', 'Fabrication  de produits en plastique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.21', 'Fabrication de plaques, feuilles, tubes et profilés en matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.21Z', 'Fabrication de plaques, feuilles, tubes et profilés en matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.22', 'Fabrication d''emballages en matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.22Z', 'Fabrication d''emballages en matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.23', 'Fabrication d''éléments en matières plastiques pour la construction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.23Z', 'Fabrication d''éléments en matières plastiques pour la construction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.29', 'Fabrication d''autres articles en matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.29A', 'Fabrication de pièces techniques à base de matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('22.29B', 'Fabrication de produits de consommation courante en matières plastiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23', 'Fabrication d''autres produits minéraux non métalliques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.1', 'Fabrication de verre et d''articles en verre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.11', 'Fabrication de verre plat') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.11Z', 'Fabrication de verre plat') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.12', 'Façonnage et transformation du verre plat') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.12Z', 'Façonnage et transformation du verre plat') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.13', 'Fabrication de verre creux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.13Z', 'Fabrication de verre creux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.14', 'Fabrication de fibres de verre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.14Z', 'Fabrication de fibres de verre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.19', 'Fabrication et façonnage d''autres articles en verre, y compris verre technique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.19Z', 'Fabrication et façonnage d''autres articles en verre, y compris verre technique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.2', 'Fabrication de produits réfractaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.20', 'Fabrication de produits réfractaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.20Z', 'Fabrication de produits réfractaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.3', 'Fabrication de matériaux de construction en terre cuite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.31', 'Fabrication de carreaux en céramique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.31Z', 'Fabrication de carreaux en céramique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.32', 'Fabrication de briques, tuiles et produits de construction, en terre cuite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.32Z', 'Fabrication de briques, tuiles et produits de construction, en terre cuite') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.4', 'Fabrication d''autres produits en céramique et en porcelaine ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.41', 'Fabrication d''articles céramiques à usage domestique ou ornemental') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.41Z', 'Fabrication d''articles céramiques à usage domestique ou ornemental') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.42', 'Fabrication d''appareils sanitaires en céramique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.42Z', 'Fabrication d''appareils sanitaires en céramique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.43', 'Fabrication d''isolateurs et pièces isolantes en céramique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.43Z', 'Fabrication d''isolateurs et pièces isolantes en céramique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.44', 'Fabrication d''autres produits céramiques à usage technique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.44Z', 'Fabrication d''autres produits céramiques à usage technique') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.49', 'Fabrication d''autres produits céramiques ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.49Z', 'Fabrication d''autres produits céramiques') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.5', 'Fabrication de ciment, chaux et plâtre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.51', 'Fabrication de ciment') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.51Z', 'Fabrication de ciment') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.52', 'Fabrication de chaux et plâtre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.52Z', 'Fabrication de chaux et plâtre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.6', 'Fabrication d''ouvrages en béton, en ciment ou en plâtre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.61', 'Fabrication d''éléments en béton pour la construction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.61Z', 'Fabrication d''éléments en béton pour la construction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.62', 'Fabrication d''éléments en plâtre pour la construction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.62Z', 'Fabrication d''éléments en plâtre pour la construction') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.63', 'Fabrication de béton prêt à l''emploi') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.63Z', 'Fabrication de béton prêt à l''emploi') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.64', 'Fabrication de mortiers et bétons secs') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.64Z', 'Fabrication de mortiers et bétons secs') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.65', 'Fabrication d''ouvrages en fibre-ciment') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.65Z', 'Fabrication d''ouvrages en fibre-ciment') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.69', 'Fabrication d''autres ouvrages en béton, en ciment ou en plâtre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.69Z', 'Fabrication d''autres ouvrages en béton, en ciment ou en plâtre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.7', 'Taille, façonnage et finissage de pierres ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.70', 'Taille, façonnage et finissage de pierres') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.70Z', 'Taille, façonnage et finissage de pierres') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.9', 'Fabrication de produits abrasifs et de produits minéraux non métalliques n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.91', 'Fabrication de produits abrasifs') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.91Z', 'Fabrication de produits abrasifs') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.99', 'Fabrication d''autres produits minéraux non métalliques n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('23.99Z', 'Fabrication d''autres produits minéraux non métalliques n.c.a.') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24', 'Métallurgie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.1', 'Sidérurgie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.10', 'Sidérurgie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.10Z', 'Sidérurgie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.2', 'Fabrication de tubes, tuyaux, profilés creux et accessoires correspondants en acier ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.20', 'Fabrication de tubes, tuyaux, profilés creux et accessoires correspondants en acier ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.20Z', 'Fabrication de tubes, tuyaux, profilés creux et accessoires correspondants en acier ') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.3', 'Fabrication d''autres produits de première transformation de l''acier') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.31', 'Étirage à froid de barres') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.31Z', 'Étirage à froid de barres') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.32', 'Laminage à froid de feuillards') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.33', 'Profilage à froid par formage ou pliage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.33Z', 'Profilage à froid par formage ou pliage') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.34', 'Tréfilage à froid') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.34Z', 'Tréfilage à froid') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.4', 'Production de métaux précieux et d''autres métaux non ferreux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.41', 'Production de métaux précieux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.41Z', 'Production de métaux précieux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.42', 'Métallurgie de l''aluminium') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.42Z', 'Métallurgie de l''aluminium') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.43', 'Métallurgie du plomb, du zinc ou de l''étain') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.43Z', 'Métallurgie du plomb, du zinc ou de l''étain') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.44', 'Métallurgie du cuivre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.44Z', 'Métallurgie du cuivre') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.45', 'Métallurgie des autres métaux non ferreux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.45Z', 'Métallurgie des autres métaux non ferreux') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.46', 'Élaboration et transformation de matières nucléaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.46Z', 'Élaboration et transformation de matières nucléaires') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.5', 'Fonderie') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.51', 'Fonderie de fonte') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.51Z', 'Fonderie de fonte') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.52', 'Fonderie d''acier') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.52Z', 'Fonderie d''acier') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.53', 'Fonderie de métaux légers') ON CONFLICT DO NOTHING;
INSERT INTO activites (code, libelle) VALUES ('24.53Z', 'Fonderie de métaux légers') ON CONFLICT DO NOTHING;

