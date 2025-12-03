# Exercice 1 - Kafka Connect : Source et Sink Connectors

## 🎯 Objectifs

- Déployer un cluster Kafka Connect
- Comprendre l'architecture Kafka Connect (Workers, Connectors, Tasks)
- Créer un JDBC Source Connector pour lire depuis PostgreSQL
- Créer un JDBC Sink Connector pour écrire dans PostgreSQL
- Explorer les transformations (SMTs - Single Message Transforms)
- Monitorer les connecteurs via Kafka UI

## 📋 Prérequis

- Docker et Docker Compose installés
- curl et jq installés (pour les commandes API)
- Ports disponibles: 8080, 8083, 9092, 5432

## 🏗️ Architecture

```
┌──────────────────────┐
│   PostgreSQL         │
│  Table: source_data  │
│  (données source)    │
└──────────┬───────────┘
           │
           v
┌──────────────────────────────────┐
│    JDBC Source Connector         │
│  (lit les nouvelles lignes)      │
│  Mode: incrementing (id)         │
└──────────┬───────────────────────┘
           │
           v
┌──────────────────────────────────┐
│   Topic: db-source_data          │
│      (Kafka Broker)              │
└──────────┬───────────────────────┘
           │
           v
┌──────────────────────────────────┐
│     JDBC Sink Connector          │
│  (écrit dans PostgreSQL)         │
└──────────┬───────────────────────┘
           │
           v
┌──────────────────────────────────┐
│   PostgreSQL                     │
│  Table: sink_data                │
│  (données destination)           │
└──────────────────────────────────┘
```

**Pipeline complet:** `source_data` → `JDBC Source` → `Kafka` → `JDBC Sink` → `sink_data`

---

## 🚀 Partie 1 - Démarrage de l'Infrastructure

### 1.1 Démarrer les services

```bash
cd jour2/exercice1-kafka-connect
make start
```

Ou directement avec docker-compose:

```bash
docker compose up -d
```

**Services démarrés:**
- Kafka Broker (port 9092)
- Kafka Connect (port 8083) avec JDBC Connector installé
- PostgreSQL (port 5432)
- Kafka UI (port 8080)

**⏳ Temps d'attente:** ~90 secondes pour l'installation du JDBC Connector

### 1.2 Vérifier le statut

```bash
make status
```

Attendez que tous les services soient "healthy".

### 1.3 Accéder aux interfaces

**Kafka UI:** http://localhost:8080
- Topics
- Consumer Groups
- **Kafka Connect** (menu à gauche)

**Kafka Connect API:** http://localhost:8083

Tester l'API:
```bash
curl http://localhost:8083/
```

**PostgreSQL:**
```bash
make db-connect
```

Ou directement:
```bash
docker exec -it postgres psql -U kafka -d kafka_sink
```

---

## 📊 Partie 2 - Explorer les Données Initiales

### 2.1 Vérifier la table source

Les données sont initialisées automatiquement au démarrage de PostgreSQL via le script `init-db.sql`.

```bash
make db-query
```

Ou manuellement:
```bash
docker exec -it postgres psql -U kafka -d kafka_sink -c "SELECT * FROM source_data;"
```

**Résultat attendu:**
```
 id |           message            |         created_at
----+------------------------------+----------------------------
  1 | Hello Kafka Connect!         | 2025-12-01 14:30:00.123456
  2 | This is message number 2     | 2025-12-01 14:30:00.234567
  3 | Kafka Connect is awesome     | 2025-12-01 14:30:00.345678
  4 | Learning Kafka is fun        | 2025-12-01 14:30:00.456789
  5 | Data integration made easy   | 2025-12-01 14:30:00.567890
```

### 2.2 Vérifier la table destination (vide au départ)

```bash
docker exec -it postgres psql -U kafka -d kafka_sink -c "SELECT * FROM sink_data;"
```

**Résultat attendu:** Aucune ligne (la table est vide)

### 2.3 Structure des tables

**Table source_data:**
- `id` (SERIAL PRIMARY KEY) - Identifiant auto-incrémenté
- `message` (TEXT) - Contenu du message
- `created_at` (TIMESTAMP) - Date de création

**Table sink_data:**
- `id` (INT PRIMARY KEY) - Identifiant
- `message` (TEXT) - Contenu du message
- `created_at` (TIMESTAMP) - Date de création

---

## 📥 Partie 3 - Source Connector (Lire depuis PostgreSQL)

### 3.1 Comprendre le Source Connector

Le **JDBC Source Connector** lit les données depuis une table PostgreSQL et les envoie vers Kafka.

**Mode incrementing:**
- Lit uniquement les nouvelles lignes
- Utilise la colonne `id` pour tracker la progression
- Poll interval: 5 secondes

### 3.2 Créer le Source Connector

**Via Make:**
```bash
make create-source
```

**Via API REST:**
```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jdbc-source",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://postgres:5432/kafka_sink",
      "connection.user": "kafka",
      "connection.password": "kafka123",
      "table.whitelist": "source_data",
      "mode": "incrementing",
      "incrementing.column.name": "id",
      "topic.prefix": "db-",
      "poll.interval.ms": "5000"
    }
  }'
```

**Configuration expliquée:**
- `table.whitelist`: Table(s) à lire
- `mode: incrementing`: Lit les nouvelles lignes basées sur l'ID
- `incrementing.column.name: id`: Colonne utilisée pour tracker
- `topic.prefix: db-`: Préfixe du topic (→ `db-source_data`)
- `poll.interval.ms: 5000`: Vérifie les nouvelles données toutes les 5s

### 3.3 Vérifier le connector

```bash
make connectors
```

Ou via API:
```bash
curl http://localhost:8083/connectors/jdbc-source/status | jq
```

**Statut attendu:**
```json
{
  "name": "jdbc-source",
  "connector": {
    "state": "RUNNING",
    "worker_id": "kafka-connect:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "kafka-connect:8083"
    }
  ],
  "type": "source"
}
```

### 3.4 Observer le résultat

**Vérifier le topic Kafka:**
```bash
make topics
```

**Consommer le topic pour voir les messages:**
```bash
make consume
```

Ou manuellement:
```bash
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic db-source_data \
  --from-beginning \
  --max-messages 5
```

**Dans Kafka UI:**
1. Allez dans "Topics" → "db-source_data"
2. Cliquez sur "Messages"
3. Observez les 5 messages initiaux

---

## 📤 Partie 4 - Sink Connector (Écrire dans PostgreSQL)

### 4.1 Comprendre le Sink Connector

Le **JDBC Sink Connector** lit les messages depuis Kafka et les écrit dans PostgreSQL.

**Configuration:**
- `insert.mode: insert`: Insère les nouvelles lignes
- `pk.mode: record_value`: Utilise l'ID du message comme clé primaire
- `pk.fields: id`: Champ utilisé comme clé primaire

### 4.2 Créer le Sink Connector

**Via Make:**
```bash
make create-sink
```

**Via API REST:**
```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jdbc-sink",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSinkConnector",
      "tasks.max": "1",
      "topics": "db-source_data",
      "connection.url": "jdbc:postgresql://postgres:5432/kafka_sink",
      "connection.user": "kafka",
      "connection.password": "kafka123",
      "auto.create": "false",
      "auto.evolve": "false",
      "insert.mode": "insert",
      "table.name.format": "sink_data",
      "pk.mode": "record_value",
      "pk.fields": "id"
    }
  }'
```

### 4.3 Vérifier le connector

```bash
make connectors
```

### 4.4 Vérifier les données dans PostgreSQL

```bash
make db-query
```

Ou manuellement:
```bash
docker exec -it postgres psql -U kafka -d kafka_sink -c "SELECT * FROM sink_data ORDER BY id;"
```

**Résultat attendu:** Les 5 messages de `source_data` sont maintenant dans `sink_data`!

---

## 🔄 Partie 5 - Test du Pipeline Complet

### 5.1 Ajouter une nouvelle donnée

**Via Make:**
```bash
make add-data
```

**Via PostgreSQL directement:**
```bash
docker exec -it postgres psql -U kafka -d kafka_sink -c \
  "INSERT INTO source_data (message) VALUES ('New message from Kafka training');"
```

### 5.2 Observer le traitement

**Étape 1: Vérifier la table source**
```bash
make db-query
```

Vous devriez voir le nouveau message avec un ID supérieur (ex: id=6).

**Étape 2: Attendre 5 secondes** (poll interval du source connector)

**Étape 3: Vérifier le topic Kafka**
```bash
make consume
```

Vous devriez voir le nouveau message dans le topic.

**Étape 4: Vérifier la table destination**
```bash
make db-query
```

Vous devriez voir le nouveau message dans `sink_data`!

### 5.3 Ajouter plusieurs messages

```bash
docker exec -it postgres psql -U kafka -d kafka_sink << EOF
INSERT INTO source_data (message) VALUES
  ('Message 1'),
  ('Message 2'),
  ('Message 3');
EOF
```

Attendez 5 secondes et vérifiez:
```bash
make db-query
```

**Tous les messages** ont été propagés automatiquement!

---

## 🔧 Partie 6 - Transformations (SMTs)

### 6.1 Comprendre les transformations

Les **Single Message Transforms (SMTs)** permettent de modifier les messages à la volée dans Kafka Connect, sans avoir besoin d'écrire de code.

**Cas d'usage courants:**
- Ajouter des métadonnées (timestamp, source, environnement)
- Renommer ou supprimer des champs
- Masquer des données sensibles (RGPD, sécurité)
- Convertir les types de données
- Modifier le format des timestamps

**Types de transformations disponibles:**
- **InsertField** - Ajouter un champ (ex: timestamp, hostname)
- **ReplaceField** - Renommer ou supprimer des champs
- **MaskField** - Masquer des données sensibles
- **Cast** - Convertir les types de données
- **TimestampConverter** - Convertir les formats de temps
- **ValueToKey** - Copier un champ de la valeur vers la clé

---

### 6.2 Exemple Guidé : Ajouter des Métadonnées

Dans cet exemple, nous allons enrichir chaque message avec :
1. Un champ `source_system` pour identifier l'origine des données
2. Un timestamp `processed_at` pour tracer quand le message a été traité
3. Un champ `environment` pour identifier l'environnement (dev/prod)

#### Étape 1 : Supprimer le connector actuel

```bash
make delete-source
```

#### Étape 2 : Créer le connector avec transformations

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jdbc-source",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://postgres:5432/kafka_sink",
      "connection.user": "kafka",
      "connection.password": "kafka123",
      "table.whitelist": "source_data",
      "mode": "incrementing",
      "incrementing.column.name": "id",
      "topic.prefix": "db-",
      "poll.interval.ms": "5000",
      "transforms": "AddSource,AddTimestamp,AddEnv",
      "transforms.AddSource.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddSource.static.field": "source_system",
      "transforms.AddSource.static.value": "postgresql-prod",
      "transforms.AddTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddTimestamp.timestamp.field": "processed_at",
      "transforms.AddEnv.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddEnv.static.field": "environment",
      "transforms.AddEnv.static.value": "training"
    }
  }'
```

**Explication de la configuration :**
- `transforms`: Liste des transformations à appliquer (ordre d'exécution)
- `AddSource`: Ajoute un champ statique `source_system = "postgresql-prod"`
- `AddTimestamp`: Ajoute un timestamp automatique `processed_at`
- `AddEnv`: Ajoute un champ statique `environment = "training"`

#### Étape 3 : Vérifier le connector

```bash
curl http://localhost:8083/connectors/jdbc-source/status | jq
```

Attendez que le statut soit `RUNNING`.

#### Étape 4 : Ajouter une nouvelle donnée

```bash
make add-data
```

Ou manuellement :
```bash
docker exec -it postgres psql -U kafka -d kafka_sink -c \
  "INSERT INTO source_data (message) VALUES ('Test avec transformations SMT');"
```

#### Étape 5 : Observer le résultat transformé

**Dans le terminal :**
```bash
make consume
```

**Résultat attendu (format Struct) :**
```
Struct{
  id=10,
  message=Test avec transformations SMT,
  created_at=2025-12-01 16:30:00.123456,
  source_system=postgresql-prod,
  processed_at=1733071800000,
  environment=training
}
```

**Dans Kafka UI :**
1. Allez dans "Topics" → "db-source_data"
2. Cliquez sur "Messages"
3. Sélectionnez le dernier message
4. Observez les nouveaux champs ajoutés par les transformations !

#### Étape 6 : Comparer avec les données originales

```bash
make db-query
```

Vous constaterez que la table source PostgreSQL ne contient que les 3 champs originaux (`id`, `message`, `created_at`), tandis que le message Kafka contient 6 champs grâce aux transformations !

---

### 6.3 Exercice Pratique : Renommer des Champs

**Objectif :** Renommer `message` → `content` et `created_at` → `timestamp`

#### Configuration à ajouter

Supprimez le connector et recréez-le avec cette transformation supplémentaire :

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jdbc-source",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://postgres:5432/kafka_sink",
      "connection.user": "kafka",
      "connection.password": "kafka123",
      "table.whitelist": "source_data",
      "mode": "incrementing",
      "incrementing.column.name": "id",
      "topic.prefix": "db-",
      "poll.interval.ms": "5000",
      "transforms": "RenameFields",
      "transforms.RenameFields.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
      "transforms.RenameFields.renames": "message:content,created_at:timestamp"
    }
  }'
```

**Testez :**
1. Ajoutez une nouvelle donnée : `make add-data`
2. Consommez le topic : `make consume`
3. Vérifiez que les champs sont renommés !

**Résultat attendu :**
```
Struct{
  id=11,
  content=...,          ← Renommé de "message"
  timestamp=...         ← Renommé de "created_at"
}
```

---

### 6.4 Exercice Pratique : Chaîner Plusieurs Transformations

**Objectif :** Combiner renommage + ajout de métadonnées

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jdbc-source",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://postgres:5432/kafka_sink",
      "connection.user": "kafka",
      "connection.password": "kafka123",
      "table.whitelist": "source_data",
      "mode": "incrementing",
      "incrementing.column.name": "id",
      "topic.prefix": "db-",
      "poll.interval.ms": "5000",
      "transforms": "RenameFields,AddMetadata,AddTimestamp",
      "transforms.RenameFields.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
      "transforms.RenameFields.renames": "message:content",
      "transforms.AddMetadata.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddMetadata.static.field": "data_source",
      "transforms.AddMetadata.static.value": "postgres-training-db",
      "transforms.AddTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddTimestamp.timestamp.field": "ingestion_time"
    }
  }'
```

**⚠️ Important :** Les transformations sont appliquées dans l'ordre :
1. D'abord `RenameFields` (renomme `message` → `content`)
2. Ensuite `AddMetadata` (ajoute `data_source`)
3. Enfin `AddTimestamp` (ajoute `ingestion_time`)

**Testez et observez le résultat !**

---

### 6.5 Autres Transformations Utiles

#### Masquer des Données Sensibles (MaskField)

**Cas d'usage :** RGPD, sécurité, logs

```json
{
  "transforms": "MaskSensitive",
  "transforms.MaskSensitive.type": "org.apache.kafka.connect.transforms.MaskField$Value",
  "transforms.MaskSensitive.fields": "password,ssn,credit_card",
  "transforms.MaskSensitive.replacement": "****MASKED****"
}
```

Les champs `password`, `ssn`, et `credit_card` seront remplacés par `****MASKED****`.

#### Convertir les Types (Cast)

**Cas d'usage :** Forcer un type de données

```json
{
  "transforms": "CastTypes",
  "transforms.CastTypes.type": "org.apache.kafka.connect.transforms.Cast$Value",
  "transforms.CastTypes.spec": "age:int32,price:float64,active:boolean"
}
```

#### Supprimer des Champs (ReplaceField)

**Cas d'usage :** Réduire la taille des messages, supprimer des données inutiles

```json
{
  "transforms": "DropFields",
  "transforms.DropFields.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
  "transforms.DropFields.blacklist": "internal_id,debug_info,temp_field"
}
```

#### Convertir Format de Timestamp (TimestampConverter)

**Cas d'usage :** Convertir epoch → ISO 8601

```json
{
  "transforms": "ConvertTimestamp",
  "transforms.ConvertTimestamp.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
  "transforms.ConvertTimestamp.field": "created_at",
  "transforms.ConvertTimestamp.format": "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
  "transforms.ConvertTimestamp.target.type": "string"
}
```

---

### 6.6 🎓 Défi : Créer Votre Propre Pipeline de Transformations

**Objectif :** Créez un connector avec les transformations suivantes :

1. Renommer `message` → `event_description`
2. Ajouter un champ `pipeline_version` avec la valeur `"v1.0"`
3. Ajouter un timestamp `transformed_at`
4. Supprimer le champ `created_at` (si vous ne le voulez pas)

**Indice :** Utilisez `ReplaceField` avec `renames` et `blacklist`, ainsi que plusieurs `InsertField`.

**Solution :**
<details>
<summary>Cliquez pour voir la solution</summary>

```bash
curl -X POST http://localhost:8083/connectors \
  -H "Content-Type: application/json" \
  -d '{
    "name": "jdbc-source",
    "config": {
      "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
      "tasks.max": "1",
      "connection.url": "jdbc:postgresql://postgres:5432/kafka_sink",
      "connection.user": "kafka",
      "connection.password": "kafka123",
      "table.whitelist": "source_data",
      "mode": "incrementing",
      "incrementing.column.name": "id",
      "topic.prefix": "db-",
      "poll.interval.ms": "5000",
      "transforms": "RenameAndDrop,AddVersion,AddTimestamp",
      "transforms.RenameAndDrop.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
      "transforms.RenameAndDrop.renames": "message:event_description",
      "transforms.RenameAndDrop.blacklist": "created_at",
      "transforms.AddVersion.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddVersion.static.field": "pipeline_version",
      "transforms.AddVersion.static.value": "v1.0",
      "transforms.AddTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
      "transforms.AddTimestamp.timestamp.field": "transformed_at"
    }
  }'
```
</details>

**Testez votre solution et vérifiez le résultat avec `make consume` !**

---

## 📊 Partie 7 - Monitoring et Debugging

### 7.1 Vérifier l'état des connecteurs

**Via Kafka UI:**
1. Menu "Kafka Connect"
2. Voir la liste des connecteurs
3. Cliquer sur un connecteur pour voir les détails
4. Observer les métriques (messages traités, erreurs, etc.)

**Via API:**
```bash
# Statut d'un connector
curl http://localhost:8083/connectors/jdbc-source/status | jq

# Configuration d'un connector
curl http://localhost:8083/connectors/jdbc-source/config | jq

# Tasks d'un connector
curl http://localhost:8083/connectors/jdbc-source/tasks | jq
```

### 7.2 Logs des connecteurs

```bash
# Logs de Kafka Connect
docker logs kafka-connect -f

# Filtrer les erreurs
docker logs kafka-connect 2>&1 | grep ERROR

# Logs d'un connecteur spécifique
docker logs kafka-connect 2>&1 | grep jdbc-source
```

### 7.3 Redémarrer un connector en cas de problème

**Via Make:**
```bash
make delete-source
make create-source
```

**Via API:**
```bash
curl -X POST http://localhost:8083/connectors/jdbc-source/restart
```

### 7.4 Métriques importantes

**Offset tracking:**
```bash
curl http://localhost:8083/connectors/jdbc-source/status | jq '.tasks[0]'
```

**Topic lag:**
```bash
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group connect-jdbc-sink
```

### 7.5 Problèmes courants

**Le connector ne démarre pas:**
- Vérifiez les logs: `docker logs kafka-connect`
- Vérifiez la configuration: `curl http://localhost:8083/connectors/<name>/config`
- Vérifiez la connexion à PostgreSQL

**Les nouvelles données ne sont pas détectées:**
- Vérifiez le poll interval (5 secondes par défaut)
- Vérifiez que l'ID est bien incrémenté
- Vérifiez les logs du source connector

**Les données n'apparaissent pas dans sink_data:**
- Vérifiez que le topic contient des messages: `make consume`
- Vérifiez les logs du sink connector
- Vérifiez la connexion à PostgreSQL
- Vérifiez que les IDs ne sont pas dupliqués (erreur de PK)

---

## 🎯 Livrables

À la fin de cet exercice, vous devez être capable de:

### Infrastructure
- [ ] Déployer un cluster Kafka Connect avec Docker Compose
- [ ] Vérifier l'état des services (Kafka, Connect, PostgreSQL)
- [ ] Accéder à Kafka UI et à l'API Connect

### Source Connector
- [ ] Créer un JDBC Source Connector
- [ ] Comprendre le mode incrementing
- [ ] Vérifier que les données sont lues depuis PostgreSQL
- [ ] Observer les messages dans le topic Kafka

### Sink Connector
- [ ] Créer un JDBC Sink Connector
- [ ] Configurer la connexion à PostgreSQL
- [ ] Vérifier que les données sont écrites dans la base
- [ ] Requêter la table sink_data pour voir les résultats

### Pipeline Complet
- [ ] Ajouter des données dans source_data
- [ ] Observer la propagation automatique
- [ ] Vérifier que les données arrivent dans sink_data
- [ ] Comprendre le flux end-to-end

### Transformations
- [ ] Comprendre le concept de SMT
- [ ] Ajouter une transformation InsertField
- [ ] Chaîner plusieurs transformations
- [ ] Observer l'impact des transformations sur les données

### Monitoring
- [ ] Utiliser Kafka UI pour monitorer les connecteurs
- [ ] Utiliser l'API REST pour vérifier l'état
- [ ] Lire les logs pour débugger
- [ ] Redémarrer un connector en cas de problème

---

## 🧹 Nettoyage

### Arrêter les services

```bash
make stop
```

### Nettoyage complet

```bash
make clean
```

---

## 📚 Concepts Clés

### Kafka Connect Architecture

- **Worker** - Processus qui exécute les connecteurs
- **Connector** - Plugin qui définit la logique de transfert de données
- **Task** - Unité de travail parallélisable (configuré par `tasks.max`)
- **Converter** - Convertit les données entre Kafka et le format du connecteur

### Types de Connecteurs

**Source Connector:**
- Lit les données depuis un système externe
- Produit des messages vers Kafka
- Exemples: JDBC, Debezium (CDC), HTTP, S3

**Sink Connector:**
- Lit les messages depuis Kafka
- Écrit les données vers un système externe
- Exemples: JDBC, Elasticsearch, S3, HDFS

### JDBC Source Connector - Modes

**Incrementing Mode:**
- Utilise une colonne auto-incrémentée (ex: ID)
- Lit uniquement les nouvelles lignes
- Ne détecte PAS les mises à jour ou suppressions
- Simple et performant

**Timestamp Mode:**
- Utilise une colonne timestamp (ex: updated_at)
- Détecte les nouvelles lignes et les mises à jour
- Ne détecte PAS les suppressions

**Timestamp+Incrementing Mode:**
- Combine les deux approches
- Plus robuste mais plus complexe

**Bulk Mode:**
- Lit toutes les lignes à chaque poll
- Utilise beaucoup de ressources
- À éviter en production

### Single Message Transforms (SMT)

**InsertField** - Ajouter un champ statique ou dynamique
```json
"transforms": "InsertTimestamp",
"transforms.InsertTimestamp.type": "org.apache.kafka.connect.transforms.InsertField$Value",
"transforms.InsertTimestamp.timestamp.field": "processed_at"
```

**ReplaceField** - Renommer ou exclure des champs
```json
"transforms": "RenameField",
"transforms.RenameField.type": "org.apache.kafka.connect.transforms.ReplaceField$Value",
"transforms.RenameField.renames": "old_name:new_name"
```

**MaskField** - Masquer des données sensibles
```json
"transforms": "MaskPassword",
"transforms.MaskPassword.type": "org.apache.kafka.connect.transforms.MaskField$Value",
"transforms.MaskPassword.fields": "password,ssn"
```

**Cast** - Convertir les types
```json
"transforms": "Cast",
"transforms.Cast.type": "org.apache.kafka.connect.transforms.Cast$Value",
"transforms.Cast.spec": "age:int32,price:float64"
```

### API REST Kafka Connect

| Endpoint | Méthode | Description |
|----------|---------|-------------|
| `/connectors` | GET | Lister tous les connecteurs |
| `/connectors` | POST | Créer un nouveau connecteur |
| `/connectors/{name}` | GET | Obtenir les infos d'un connecteur |
| `/connectors/{name}/config` | PUT | Mettre à jour la config |
| `/connectors/{name}/status` | GET | Statut du connecteur |
| `/connectors/{name}/restart` | POST | Redémarrer le connecteur |
| `/connectors/{name}` | DELETE | Supprimer le connecteur |
| `/connector-plugins` | GET | Lister les plugins installés |

---

## 🔍 Pour Aller Plus Loin

### 1. Change Data Capture (CDC) avec Debezium

Capturez les changements de base de données en temps réel:
```json
{
  "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
  "database.hostname": "postgres",
  "database.port": "5432",
  "database.user": "kafka",
  "database.password": "kafka123",
  "database.dbname": "kafka_sink",
  "database.server.name": "postgres-cdc",
  "table.include.list": "public.source_data"
}
```

### 2. Schema Registry avec Avro

Utilisez Schema Registry pour gérer les schémas:
```json
{
  "key.converter": "io.confluent.connect.avro.AvroConverter",
  "value.converter": "io.confluent.connect.avro.AvroConverter",
  "key.converter.schema.registry.url": "http://schema-registry:8081",
  "value.converter.schema.registry.url": "http://schema-registry:8081"
}
```

### 3. Dead Letter Queue (DLQ)

Gérez les erreurs en redirigeant les messages problématiques:
```json
{
  "errors.tolerance": "all",
  "errors.deadletterqueue.topic.name": "dlq-topic",
  "errors.deadletterqueue.topic.replication.factor": "1",
  "errors.deadletterqueue.context.headers.enable": "true"
}
```

### 4. Distributed Mode en Production

En production, déployez Kafka Connect en mode distribué avec plusieurs workers pour la haute disponibilité et la scalabilité horizontale.

---

**Bravo! Vous maîtrisez maintenant Kafka Connect et les transformations! 🚀**
