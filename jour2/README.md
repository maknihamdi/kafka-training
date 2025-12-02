# Jour 2 - Kafka Connect et Stream Processing

## 🎯 Objectifs du Jour 2

Ce deuxième jour se concentre sur trois outils essentiels de l'écosystème Kafka:
- **Kafka Connect** - Pour l'intégration de données avec des systèmes externes
- **ksqlDB** - Pour le traitement en temps réel avec SQL
- **Kafka Streams** - Pour le stream processing avec Java/Spring Boot

À la fin de ce jour, vous serez capables de:
- Créer des pipelines d'intégration de données avec Kafka Connect
- Transformer les données à la volée avec les SMTs
- Utiliser ksqlDB pour filtrer, transformer et agréger des données
- Développer des applications Kafka Streams avec Spring Boot
- Comprendre et utiliser les topics compactés
- Implémenter des fenêtres temporelles et des agrégations
- Faire des jointures entre streams et tables

## 📚 Contenu

### [Exercice 1 - Kafka Connect : Source et Sink Connectors](./exercice1-kafka-connect/)

**Durée estimée:** 2-3 heures

#### Objectifs
- Comprendre l'architecture Kafka Connect (Workers, Connectors, Tasks)
- Déployer un cluster Kafka Connect
- Créer un Source Connector pour lire depuis PostgreSQL
- Créer un Sink Connector pour écrire dans PostgreSQL
- Appliquer des transformations (SMTs) aux messages

#### Ce que vous allez construire

```
PostgreSQL (source_data)
    → JDBC Source Connector
    → Kafka Topic
    → JDBC Sink Connector
    → PostgreSQL (sink_data)
```

#### Compétences acquises
- Configuration et déploiement de Kafka Connect
- Création de connecteurs JDBC
- Application de SMTs (Single Message Transforms):
  - InsertField - Ajouter des métadonnées
  - ReplaceField - Renommer/supprimer des champs
  - MaskField - Masquer des données sensibles
  - Cast - Convertir les types
- Monitoring via Kafka UI
- Debugging de connecteurs

#### Technologies
- Kafka Connect (Confluent Platform)
- JDBC Source/Sink Connectors
- PostgreSQL
- Kafka UI
- Docker Compose

---

### [Exercice 2 - ksqlDB : Stream Processing SQL](./exercice2-ksqldb/)

**Durée estimée:** 2-3 heures

#### Objectifs
- Comprendre la différence entre STREAMS et TABLES
- Utiliser SQL pour traiter des flux de données en temps réel
- Implémenter des agrégations et des fenêtres temporelles
- Joindre des streams et des tables
- Créer des topics compactés

#### Ce que vous allez construire

```
Topic source (user_events)
    → Streams (filtres, transformations)
    → Tables (agrégations, états)
    → Windowing (fenêtres temporelles)
    → Joins (enrichissement)
    → Topics dérivés
```

#### Compétences acquises
- **Streams:**
  - Créer des streams sur des topics existants
  - Filtrer les événements (WHERE)
  - Transformer les données (CASE, colonnes calculées)
  - Créer des streams dérivés

- **Tables:**
  - Créer des tables avec agrégations (COUNT, SUM, AVG, MAX, MIN)
  - Utiliser LATEST_BY_OFFSET pour l'état actuel
  - Comprendre les topics compactés
  - Voir les mises à jour en temps réel

- **Windowing:**
  - Fenêtres TUMBLING (fixes)
  - Fenêtres HOPPING (chevauchantes)
  - Fenêtres SESSION (basées sur l'activité)
  - Analyser les données par fenêtre temporelle

- **Joins:**
  - Stream-Table Join (enrichir avec l'état actuel)
  - Corrélation de données
  - Détection d'anomalies

#### Technologies
- ksqlDB Server et CLI
- Kafka UI avec support ksqlDB
- Shell script pour génération de données

---

### [Exercice 3 - Kafka Streams avec Spring Boot](./exercice3-kafka-streams/)

**Durée estimée:** 3-4 heures

#### Objectifs
- Comprendre les concepts de Kafka Streams
- Utiliser l'API Admin de Kafka pour créer des topics
- Créer une pipeline de stream processing avec Spring Boot
- Manipuler KStream et KTable
- Faire des jointures entre streams et tables
- Utiliser des topics compactés pour les données de référence
- Visualiser la topologie Kafka Streams

#### Ce que vous allez construire

```
Producer (Spring Boot + API Admin)
    ↓
    ├─> user-events (topic normal)
    └─> user-profiles (topic compacté)

Kafka Streams (Spring Boot)
    ↓
    Filtre, Transformations, Agrégations
    ↓
    Jointures avec KTable
    ↓
    Topics de sortie enrichis
```

#### Compétences acquises
- **API Admin Kafka:**
  - Créer des topics programmatiquement
  - Configurer le compactage
  - Gérer les partitions

- **Kafka Streams:**
  - KStream vs KTable vs GlobalKTable
  - Transformations (filter, map, flatMap)
  - Agrégations (count, sum, aggregate)
  - Fenêtres temporelles (tumbling, hopping)
  - Jointures Stream-Table
  - Visualiser la topologie

- **Spring Boot:**
  - Configuration Kafka Streams
  - Serdes JSON personnalisés
  - Intégration avec Spring Kafka
  - REST API pour injection de données

#### Technologies
- Spring Boot 3.2
- Spring Kafka & Kafka Streams
- Maven multi-modules
- Kafka UI
- Docker Compose

---

## 🚀 Démarrage Rapide

### Exercice 1 - Kafka Connect

```bash
cd jour2/exercice1-kafka-connect
make start
make create-source
make create-sink
make db-query
```

Ouvrez http://localhost:8080 pour Kafka UI.

### Exercice 2 - ksqlDB

```bash
cd jour2/exercice2-ksqldb
make start
make generate
make ksql-setup
make ksql
```

Dans le CLI ksqlDB, commencez à créer vos streams et tables!

### Exercice 3 - Kafka Streams

```bash
cd jour2/exercice3-kafka-streams
make start
make build
make run-producer   # Terminal 1
make run-streams    # Terminal 2
make init-data
make generate
```

Observez le traitement en temps réel dans les logs!

---

## 📖 Concepts Clés du Jour 2

### Kafka Connect

**Kafka Connect** est un framework pour connecter Kafka avec des systèmes externes (bases de données, systèmes de fichiers, APIs, etc.).

**Architecture:**
- **Worker** - Processus qui exécute les connecteurs
- **Connector** - Plugin qui définit la logique de transfert
- **Task** - Unité de travail parallélisable
- **Converter** - Convertit les données entre Kafka et le format du connecteur

**Types de connecteurs:**
- **Source Connector** - Lit depuis un système externe → Kafka
- **Sink Connector** - Lit depuis Kafka → système externe

**Modes:**
- **Standalone** - Un seul worker (développement, tests)
- **Distributed** - Plusieurs workers (production, haute disponibilité)

### SMTs (Single Message Transforms)

Les **SMTs** permettent de modifier les messages à la volée dans Kafka Connect, sans écrire de code.

**Cas d'usage:**
- Ajouter des métadonnées (timestamp, source, environnement)
- Renommer ou supprimer des champs
- Masquer des données sensibles (RGPD)
- Convertir les types de données
- Filtrer les messages

**SMTs courants:**
- `InsertField` - Ajouter un champ
- `ReplaceField` - Renommer/supprimer des champs
- `MaskField` - Masquer des données sensibles
- `Cast` - Convertir les types
- `TimestampConverter` - Convertir les formats de temps
- `ValueToKey` - Copier un champ de la valeur vers la clé

### ksqlDB

**ksqlDB** est un moteur de stream processing qui utilise SQL pour traiter les flux de données Kafka en temps réel.

**Concepts fondamentaux:**

**STREAM (Flux immuable):**
- Représente un flux de données en continu
- Append-only (ajout uniquement)
- Chaque événement est distinct
- Utilise un topic Kafka standard

**TABLE (État actuel):**
- Représente l'état actuel d'une entité
- Chaque clé a une seule valeur (dernière valeur)
- Topic compacté automatiquement
- Mises à jour par clé

**Comparaison:**

| Aspect | STREAM | TABLE |
|--------|--------|-------|
| Nature | Flux d'événements | État actuel |
| Données | Append-only | Mise à jour par clé |
| Topic | Retention standard | Compacté |
| Exemple | Transactions bancaires | Solde du compte |

### Topics Compactés

Les **topics compactés** (cleanup.policy=compact) conservent uniquement la dernière valeur pour chaque clé.

**Mode normal (retention):**
```
Message 1, Message 2, Message 3, Message 4, ...
Après X temps: tous supprimés
```

**Mode compacté:**
```
Key1: Val1, Key1: Val2, Key2: Val3, Key1: Val4
Après compaction: Key1: Val4, Key2: Val3
```

**Cas d'usage:**
- Tables ksqlDB (états, agrégations)
- Change Data Capture (CDC)
- Caches distribués
- Configuration partagée

### Windowing (Fenêtres Temporelles)

Les **fenêtres** permettent de grouper les événements par intervalle de temps.

**TUMBLING (Fenêtres fixes):**
```
[0-30s] [30-60s] [60-90s]
```
Fenêtres qui ne se chevauchent pas.

**HOPPING (Fenêtres chevauchantes):**
```
[0-60s]
    [30-90s]
        [60-120s]
```
Fenêtres qui avancent plus rapidement que leur taille.

**SESSION (Fenêtres d'activité):**
```
[Event1...gap...Event2] [Event3...gap...Event4]
```
Fenêtres basées sur l'inactivité (gap entre événements).

**Cas d'usage:**
- Analytics en temps réel (ventes par heure)
- Détection d'anomalies (seuils temporels)
- Agrégations glissantes (moyenne mobile)

---

## 🎓 Progression Pédagogique

### Matin - Kafka Connect (3h)

1. **Théorie** (30 min)
   - Architecture Kafka Connect
   - Types de connecteurs
   - SMTs

2. **Exercice 1** (2h30)
   - Déploiement du cluster
   - Source Connector
   - Sink Connector
   - Transformations SMTs
   - Debugging

### Après-midi - ksqlDB (3h)

1. **Théorie** (30 min)
   - STREAM vs TABLE
   - Topics compactés
   - Windowing

2. **Exercice 2** (2h30)
   - Création de streams
   - Filtres et transformations
   - Tables et agrégations
   - Fenêtres temporelles
   - Joins

---

## 🎯 Livrables du Jour 2

À la fin de ce jour, les participants doivent être capables de:

### Kafka Connect
- [ ] Déployer un cluster Kafka Connect
- [ ] Créer et configurer des connecteurs JDBC
- [ ] Appliquer des SMTs pour transformer les données
- [ ] Monitorer les connecteurs via Kafka UI
- [ ] Débugger les problèmes de connecteurs

### ksqlDB
- [ ] Différencier STREAM et TABLE
- [ ] Créer des streams avec filtres et transformations
- [ ] Créer des tables avec agrégations
- [ ] Implémenter des fenêtres temporelles
- [ ] Joindre streams et tables
- [ ] Comprendre les topics compactés

### Concepts Transverses
- [ ] Comprendre l'intégration de données avec Kafka
- [ ] Appliquer le stream processing en temps réel
- [ ] Utiliser SQL pour traiter des flux de données
- [ ] Monitorer et débugger les pipelines de données

---

## 📚 Ressources Complémentaires

### Documentation Officielle
- [Kafka Connect Documentation](https://kafka.apache.org/documentation/#connect)
- [ksqlDB Documentation](https://docs.ksqldb.io/)
- [Confluent Connectors](https://docs.confluent.io/kafka-connectors/self-managed/kafka_connectors.html)

### Guides et Tutoriels
- [Kafka Connect Deep Dive](https://www.confluent.io/blog/kafka-connect-deep-dive-converters-serialization-explained/)
- [ksqlDB Tutorials](https://kafka-tutorials.confluent.io/)
- [Single Message Transforms Guide](https://docs.confluent.io/platform/current/connect/transforms/overview.html)

### Outils
- [Kafka UI](https://github.com/provectus/kafka-ui) - Interface web
- [Confluent Hub](https://www.confluent.io/hub/) - Repository de connecteurs
- [ksqlDB CLI](https://docs.ksqldb.io/en/latest/operate-and-deploy/installation/installing/)

---

## 🔧 Troubleshooting Commun

### Kafka Connect

**Le connecteur ne démarre pas:**
```bash
# Voir les logs
docker logs kafka-connect

# Vérifier la configuration
curl http://localhost:8083/connectors/<name>/config | jq
```

**Erreurs de conversion:**
- Vérifiez les converters (JsonConverter vs StringConverter)
- Vérifiez `schemas.enable`

### ksqlDB

**Le stream ne reçoit pas de données:**
```sql
-- Vérifier le topic source
PRINT 'topic_name' FROM BEGINNING;

-- Vérifier les requêtes en cours
SHOW QUERIES;
```

**Erreur de format:**
- Vérifiez `VALUE_FORMAT` (JSON, AVRO, etc.)
- Vérifiez que le schéma correspond aux données

---

## 🚀 Pour Aller Plus Loin

### Kafka Connect
- Change Data Capture (CDC) avec Debezium
- Connecteurs pour S3, Elasticsearch, MongoDB
- Mode distribué en production
- Custom SMTs en Java

### ksqlDB
- User-Defined Functions (UDFs)
- Pull Queries (requêtes ponctuelles)
- Stream-Stream Joins
- Schema Registry avec Avro
- ksqlDB Connectors

---

**Prêt à démarrer? Commencez par l'[Exercice 1 - Kafka Connect](./exercice1-kafka-connect/)! 🚀**
