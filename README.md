# SignalConso API

API qui gère une base d'entreprise mise à jour depuis la base de l'insee.

L'api est disponible a cette adresse :
https://api.insee.fr/catalogue/site/themes/wso2/subthemes/insee/pages/item-info.jag?name=Sirene&version=V3&provider=insee

## Développement

### PostgreSQL

L'application requiert une connexion à un serveur PostgreSQL

Il est possible de lancer un PostgreSQL à partir d'une commande docker-compose (le fichier en question est disponible
sous scripts/local/)

à la racine du projet faire :

```
docker-compose -f scripts/local/docker-compose.yml up

```


#### Script de migration

Le projet utilise l'outil flyway (https://flywaydb.org/) pour la gestion des scripts de migration.

Les scripts de migration sont lancés au run de l'application, ils sont disponibles dans le repertoire conf/db/migration/default.

**Warning** Important

Un script doit impérativement être écrit de manière à ce que l'application fonctionne toujours en cas de rollback de l'application.

Ceci afin de ne pas avoir gérer de procédure de rollback complexe :
Avoir l'ancienne la structure de données et la nouvelle qui fonctionnent en parralèle puis un certain temps après supprimer l'ancienne structure.

Cette méthode est recommandée par flyway et est décrite sur le lien suivant : https://documentation.red-gate.com/fd/rollback-guidance-138347143.html



### Configuration locale

Lancer une base de donnes PosgreSQL provisionée avec les tables et données (voir plus haut)

L'application a besoin de variables d'environnements. Vous devez les configurer. Il y a plusieurs manières de faire,
nous recommandons la suivante :

```bash
# à ajouter dans votre .zprofile, .zshenv .bash_profile, ou équivalent
# pour toutes les valeurs avec XXX, vous devez renseigner des vraies valeurs.
# Vous pouvez par exemple reprendre les valeurs de l'environnement de démo dans Clever Cloud

function scsbt {
  # Set all environnements variables for the api then launch sbt
  # It forwards arguments, so you can do "scsbt", "scscbt compile", etc.
  echo "Launching sbt with extra environnement variables"
  INSEE_KEY="XXX" \
  INSEE_SECRET="XXX" \
  AUTHENTICATION_TOKEN="XXX" \
  APPLICATION_SECRET="XXX" \
  POSTGRESQL_ADDON_URI="XXX" \
  POSTGRESQL_ADDON_HOST=="XXX" \
  POSTGRESQL_ADDON_PORT=="XXX" \
  POSTGRESQL_ADDON_DB=="XXX" \
  POSTGRESQL_ADDON_USER=="XXX" \
  POSTGRESQL_ADDON_PASSWORD=="XXX" \
  sbt "$@"
}

```

Ceci définit une commande `scsbt`, à utiliser à la place de `sbt`

#### ❓ Pourquoi définir cette fonction, pourquoi ne pas juste exporter les variables en permanence ?

Pour éviter que ces variables ne soient lisibles dans l'environnement par n'importe quel process lancés sur votre
machine. Bien sûr c'est approximatif, on ne peut pas empêcher un process de parser le fichier de conf directement, mais
c'est déjà un petit niveau de protection.

#### ❓ Puis-je mettre ces variables dans un fichier local dans le projet, que j'ajouterai au .gitignore ?

C'est trop dangereux. Nos repos sont publics, la moindre erreur humaine au niveau du .gitignore pourrait diffuser toutes
les variables.

### Lancer l'appli

Lancer

```bash
scsbt run 
```

L'API est accessible à l'adresse `http://localhost:9000/api` avec rechargement à chaud des modifications.

## Variables d'environnement

| Nom                          | Description                                                                                                                                                                            | Valeur par défaut |
|:-----------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:------------------|
| INSEE_KEY                    | Identifiant pour communiquer avec l'API de l'insee                                                                                                                                     ||
| INSEE_SECRET                 | Secret pour communiquer avec l'API de l'insee                                                                                                                                          |                   |
| POSTGRESQL_ADDON_URI         | Full database url                                                                                                                                                                      |                   |
| POSTGRESQL_ADDON_HOST | Database host                                                                                                                                                                          |                   |
| POSTGRESQL_ADDON_PORT | Database port                                                                                                                                                                          |                   |
| POSTGRESQL_ADDON_DB          | Database name                                                                                                                                                                          |                   |
| POSTGRESQL_ADDON_USER        | Database user                                                                                                                                                                          |                   |
| POSTGRESQL_ADDON_PASSWORD    | Database password                                                                                                                                                                      |                   |
| APPLICATION_SECRET           | Clé secrète de l'application                                                                                                                                                           ||
| AUTHENTICATION_TOKEN         | Token hashé qui sert à communiquer avec l'API signal conso pour la Maj entreprise                                                                                                      ||
| PUBLIC_DATA_ONLY             | Récupération via l'INSEE des données entreprise publiques seulement  (utile pour la base de test pour ne pas avoir de données sensibles)                                               | true              |

---

## Connection avec Insee

La connection avec l'INSEE se fait grace à une paire clé/secret contenue dans les variables INSEE_KEY et INSEE_SECRET.

Il possible d'invalider / regénérer les clés en allant sur le site de
l'insee https://api.insee.fr/catalogue/site/themes/wso2/subthemes/insee/pages/applications.jag

## Création d'un token machine pour la synchronisation des entreprises avec signal conso

```scala

//Génération du token

import controllers.Token
import controllers.Token.ClearToken

val hashedToken = Token.hash(ClearToken("my_clear_token"))
println(hashedToken)

```

Côté Signal conso API, on ajoute la variable ETABLISSEMENT_API_KEY avec la valeur _"my_clear_token"_

Côté Etablissement API on ajoute la variable AUTHENTICATION_TOKEN avec la valeur de la variable **hashedToken**