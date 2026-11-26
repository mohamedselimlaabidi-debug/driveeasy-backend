# DriveEasy – Backend microservices

Plateforme de location de véhicules : parc automobile, clients et cycle complet d'une location (réservation, départ, retour, annulation), avec calcul du prix et des pénalités.

**Technologies :** Java 21, Spring Boot 3.5, Spring Cloud 2025.0 (Eureka, Config, Gateway, OpenFeign), Micrometer Tracing, Zipkin. Données **en mémoire uniquement** (aucune base de données).

## Prérequis

- JDK 21, Maven 3.9+
- Docker (pour Zipkin)
- Accès Internet au premier build (téléchargement des dépendances)

## Ports

| Composant | Port |
|---|---|
| api-gateway | 8080 |
| vehicule-service | 8081 |
| client-service | 8082 |
| location-service | 8083 |
| service-registry (Eureka) | 8761 |
| config-server | 8888 |
| Zipkin | 9411 |

## Installation et lancement

Toutes les commandes partent de la racine du projet.

```bash
# 1. Zipkin
docker compose up -d

# 2. Compilation et tests
mvn clean package
```

Lancer ensuite chaque composant **dans un terminal séparé, dans cet ordre**, en attendant la fin du démarrage de chacun :

```bash
java -jar service-registry/target/service-registry.jar
java -jar config-server/target/config-server.jar
java -jar vehicule-service/target/vehicule-service.jar
java -jar client-service/target/client-service.jar
java -jar location-service/target/location-service.jar
java -jar api-gateway/target/api-gateway.jar
```

Vérifications :

- Eureka : http://localhost:8761 (5 instances attendues : config-server, les 3 services, api-gateway)
- Configuration servie : http://localhost:8888/location-service/dev
- Santé : http://localhost:8081/actuator/health
- API : http://localhost:8080/api/vehicules
- Zipkin : http://localhost:9411

L'enregistrement dans Eureka peut prendre environ 30 secondes : une réponse 503 juste après le démarrage est normale.

## Configuration centralisée et profils

Les fichiers sont dans `config-repo/`.

- Par défaut le Config Server lit ce dossier local (profil `native`).
- Pour lire la configuration depuis GitHub (après publication du dépôt) :

```bash
export CONFIG_PROFILE=git
export CONFIG_GIT_URI=https://github.com/VOTRE_COMPTE/driveeasy-backend.git
java -jar config-server/target/config-server.jar
```

Sous PowerShell : `$env:CONFIG_PROFILE="git"`. Dépôt privé : ajouter `SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME` et `SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD` (jeton d'accès) ; ne jamais les committer.

- Profils des services : `dev` (par défaut) et `prod` (`APP_PROFILE=prod` : sans données de démonstration, échantillonnage 10 %).

## API (via la gateway, préfixe `/api`)

| Méthode | URI | Description |
|---|---|---|
| GET | `/api/vehicules?categorie=&carburant=&statut=&prixMax=` | Liste filtrée |
| GET | `/api/vehicules/{id}` | Détail |
| GET | `/api/vehicules/disponibles?dateDebut=&dateFin=` | Disponibles sur une période |
| POST / PUT / DELETE | `/api/vehicules[/{id}]` | Gestion du parc |
| PATCH | `/api/vehicules/{id}/statut` | Changement de statut |
| GET | `/api/clients?nom=&cin=&email=` | Recherche |
| GET / POST / PUT / DELETE | `/api/clients[/{id}]` | Gestion des clients |
| GET | `/api/clients/{id}/eligibilite` | Éligibilité |
| PATCH | `/api/clients/{id}/categorie` | Catégorie de fidélité |
| GET | `/api/locations?statut=&clientId=&vehiculeId=&dateDebut=&dateFin=&page=&size=` | Liste paginée |
| GET | `/api/locations/{id}` | Détail agrégé |
| POST | `/api/locations/simulation` | Simulation du prix |
| POST | `/api/locations` | Réservation |
| PATCH | `/api/locations/{id}/depart` \| `retour` \| `annulation` | Cycle de vie |
| GET | `/api/locations/statistiques` | CA, occupation, top véhicules |

Le fichier [`requetes.http`](requetes.http) contient un scénario prêt à exécuter (IntelliJ ou extension REST Client de VS Code ; les requêtes se recopient aussi dans Postman).

Codes renvoyés : 200, 201, 204, 400, 404, 409, 500, et **503** lorsqu'un service distant est injoignable. Les erreurs suivent le format `ProblemDetail` (RFC 7807).

## Jeux de données

Chargés au démarrage (profil `dev`). Aucun compte utilisateur : l'API n'est pas authentifiée.

| Client | Particularité |
|---|---|
| 1 – Amine Ben Salah | STANDARD, 4 locations terminées + 1 en cours en retard |
| 2 – Sarra Trabelsi | VIP, 1 réservation |
| 3 – Mehdi Gharbi | Permis expiré |
| 4 – Yasmine Jlassi | Moins de 21 ans |

Véhicules 1 à 8 : le 3 est LOUE, le 6 en MAINTENANCE, les autres DISPONIBLE.

Les données sont perdues à l'arrêt. Si vous redémarrez un service métier, **redémarrez les trois** pour garder des données cohérentes.

## Choix d'interprétation

- Attribut `categorie` ajouté au véhicule (CITADINE, BERLINE, SUV, UTILITAIRE, LUXE) : le filtre demandé l'exige.
- « Coefficient catégorie » = coefficient de la catégorie du **véhicule** ; la catégorie du client donne la remise.
- Jours facturés = écart entre les deux dates, minimum 1. Les périodes se chevauchent bornes incluses.
- Conformément à la règle de gestion, un véhicule LOUE ou MAINTENANCE ne peut pas être réservé, même pour une date future.
- La « datasource » externalisée est de type `memory`, les bases de données étant interdites.
- Une couche Service est placée entre Controller et Repository.
- Montants à 3 décimales (millimes).

## Limites connues

- Pas d'authentification ; les points d'accès internes sont aussi exposés par la gateway.
- Pas de circuit breaker ni de reprise automatique ; compensation « au mieux ».
- Verrouillage prévu pour une seule instance de chaque service.
- Frontend Angular (bonus) non réalisé.

## Captures d'écran

À ajouter dans `docs/captures/` après exécution : tableau de bord Eureka, requêtes Postman, trace Zipkin.
