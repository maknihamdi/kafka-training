# Exercice 2 - ksqlDB : Stream Processing SQL

## 🎯 Objectifs

- Comprendre la différence entre STREAMS et TABLES
- Filtrer et transformer des flux de données avec SQL
- Créer des agrégations en temps réel
- Utiliser les fenêtres temporelles (windowing)
- Joindre des streams et des tables
- Comprendre les topics compactés

## 📋 Prérequis

- Docker et Docker Compose
- Bash (shell Unix/Linux standard)
- Ports disponibles: 8080, 8088, 9092

## 🏗️ Architecture Simplifiée

```
Script Shell (generate_events.sh)
    ↓
Topic Kafka: user_events
    ↓
ksqlDB Server (traitement SQL)
    ↓
Streams & Tables dérivés
```

---

## 🚀 Partie 1 - Démarrage

### 1.1 Lancer l'infrastructure

```bash
cd jour2/exercice2-ksqldb
make start
```

Cela démarre:
- **Kafka** (port 9092)
- **ksqlDB Server** (port 8088)
- **Kafka UI** (port 8080)

⏳ **Attendre 60 secondes** pour le démarrage complet.

### 1.2 Vérifier le statut

```bash
make status
```

Tous les services doivent être démarrés et prêts.

---

## 📊 Partie 2 - Générer des Données

### 2.1 Générer les événements de test

Le script `generate_events.sh` lit les données depuis `scripts/events_data.txt` et les envoie vers Kafka.

```bash
make generate
```

**Résultat attendu:**
```
📊 Envoi de 50 événements...

[  1/ 50] PURCHASE         | User 101 |      50.00 | France
[  2/ 50] LOGIN            | User 102 |        N/A | USA
[  3/ 50] PURCHASE         | User 103 |     120.50 | France
...
✅ 50 événements envoyés avec succès!
```

### 2.2 Vérifier les données dans Kafka

```bash
make consume
```

Vous devriez voir les événements JSON:
```json
{"user_id": 105, "event_type": "PURCHASE", "amount": 234.5, "country": "France", "event_time": 1733...}
```

### 2.3 Mode continu (optionnel)

Pour générer des événements en continu (1 événement toutes les 2 secondes):
```bash
make generate-continuous
```

Le script sélectionne aléatoirement des événements depuis `events_data.txt` et les envoie en boucle. Appuyez sur `Ctrl+C` pour arrêter.

---

## 🔧 Partie 3 - Charger le Setup ksqlDB

Maintenant que les données sont dans Kafka, chargez les streams de base :

```bash
make ksql-setup
```

Cela crée :
- `user_events_stream` - Tous les événements utilisateurs
- `purchases_stream` - Uniquement les achats (filtre PURCHASE)

**Vérifier :**
```bash
make ksql-streams
```

**Résultat attendu :**
```
 Stream Name         | Kafka Topic      | Key Format | Value Format
---------------------------------------------------------------------
 PURCHASES_STREAM    | PURCHASES_STREAM | KAFKA      | JSON
 USER_EVENTS_STREAM  | user_events      | KAFKA      | JSON
```

---

## 🔷 Partie 4 - Explorer avec ksqlDB CLI

### 4.1 Se connecter au CLI ksqlDB

```bash
make ksql
```

Vous êtes maintenant dans l'interface interactive ksqlDB!

### 4.2 Voir les streams existants

```sql
SHOW STREAMS;
```

### 4.3 Consulter les données en temps réel

```sql
-- Voir tous les événements
SELECT * FROM user_events_stream EMIT CHANGES LIMIT 10;
```

**Résultat attendu:**
```
+----------+------------+--------+---------+---------------+
| USER_ID  | EVENT_TYPE | AMOUNT | COUNTRY | EVENT_TIME    |
+----------+------------+--------+---------+---------------+
| 105      | PURCHASE   | 234.5  | France  | 1733071234567 |
| 112      | LOGIN      | null   | USA     | 1733071234568 |
...
```

**Note:** `EMIT CHANGES` signifie que la requête reste ouverte et affiche les nouveaux événements. Appuyez sur `Ctrl+C` pour arrêter.

```sql
-- Voir uniquement les achats
SELECT * FROM purchases_stream EMIT CHANGES LIMIT 5;
```

---

## 🔧 Partie 5 - Créer Vos Propres Streams

### 5.1 Filtrer les achats en France

```sql
CREATE STREAM french_purchases AS
    SELECT *
    FROM purchases_stream
    WHERE country = 'France'
    EMIT CHANGES;
```

**Vérifier:**
```sql
SELECT * FROM french_purchases EMIT CHANGES LIMIT 5;
```

### 5.2 Détecter les achats > 100€

```sql
CREATE STREAM high_value_purchases AS
    SELECT user_id, amount, country
    FROM purchases_stream
    WHERE amount > 100
    EMIT CHANGES;
```

### 5.3 Catégoriser les achats

```sql
CREATE STREAM categorized_purchases AS
    SELECT
        user_id,
        amount,
        country,
        CASE
            WHEN amount < 50 THEN 'SMALL'
            WHEN amount < 150 THEN 'MEDIUM'
            ELSE 'LARGE'
        END AS purchase_category,
        amount * 0.2 AS tax_amount
    FROM purchases_stream
    EMIT CHANGES;
```

**Tester:**
```sql
SELECT user_id, amount, purchase_category, tax_amount
FROM categorized_purchases
EMIT CHANGES
LIMIT 5;
```

---

## 📈 Partie 6 - Créer des Tables avec Agrégations

### 6.1 Total dépensé par utilisateur

```sql
CREATE TABLE user_total_spent AS
    SELECT
        user_id,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_spent,
        AVG(amount) AS avg_spent
    FROM purchases_stream
    GROUP BY user_id
    EMIT CHANGES;
```

**Important:** Cette table utilise un **topic compacté** automatiquement!

**Consulter la table (pull query):**
```sql
SELECT * FROM user_total_spent;
```

**Résultat attendu:**
```
+----------+----------------+-------------+-----------+
| USER_ID  | PURCHASE_COUNT | TOTAL_SPENT | AVG_SPENT |
+----------+----------------+-------------+-----------+
| 105      | 3              | 567.25      | 189.08    |
| 112      | 2              | 234.99      | 117.50    |
...
```

**Vérifier le topic compacté:**

Quittez ksqlDB (`exit`), puis:
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
  --describe --topic USER_TOTAL_SPENT
```

Vous devriez voir `cleanup.policy=compact` !

### 6.2 Statistiques par pays

```sql
CREATE TABLE purchases_by_country AS
    SELECT
        country,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_amount,
        AVG(amount) AS avg_amount
    FROM purchases_stream
    GROUP BY country
    EMIT CHANGES;
```

**Consulter:**
```sql
SELECT * FROM purchases_by_country;
```

---

## ⏱️ Partie 7 - Fenêtres Temporelles (Windowing)

### 7.1 Fenêtre TUMBLING (30 secondes)

```sql
CREATE TABLE purchases_by_country_windowed AS
    SELECT
        country,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_amount,
        WINDOWSTART AS window_start,
        WINDOWEND AS window_end
    FROM purchases_stream
    WINDOW TUMBLING (SIZE 30 SECONDS)
    GROUP BY country
    EMIT CHANGES;
```

**Observer en temps réel:**
```sql
SELECT
    country,
    purchase_count,
    total_amount,
    TIMESTAMPTOSTRING(window_start, 'HH:mm:ss') AS window_start_time
FROM purchases_by_country_windowed
EMIT CHANGES;
```

### 7.2 Tester le windowing

Dans un autre terminal, lancez le générateur en continu:
```bash
make generate-continuous
```

Retournez dans ksqlDB et observez les fenêtres se créer toutes les 30 secondes!

---

## 🔗 Partie 8 - Joindre Streams et Tables

### 8.1 Enrichir les achats avec les stats utilisateur

```sql
CREATE STREAM purchases_with_stats AS
    SELECT
        p.user_id,
        p.amount,
        u.total_spent AS user_total_spent,
        u.avg_spent AS user_avg_spent
    FROM purchases_stream p
    LEFT JOIN user_total_spent u ON p.user_id = u.user_id
    EMIT CHANGES;
```

**Observer:**
```sql
SELECT * FROM purchases_with_stats EMIT CHANGES LIMIT 5;
```

Chaque achat est enrichi avec les statistiques globales de l'utilisateur!

### 8.2 Détecter les achats au-dessus de la moyenne

```sql
CREATE STREAM above_average_purchases AS
    SELECT
        p.user_id,
        p.amount,
        u.avg_spent AS user_avg,
        p.amount - u.avg_spent AS amount_above_avg
    FROM purchases_stream p
    LEFT JOIN user_total_spent u ON p.user_id = u.user_id
    WHERE p.amount > u.avg_spent
    EMIT CHANGES;
```

---

## 📊 Partie 9 - Explorer avec Kafka UI

### 9.1 Ouvrir Kafka UI

Allez sur http://localhost:8080

### 9.2 Explorer les streams et tables

1. Menu **"ksqlDB"** (à gauche)
2. Voir la liste des streams et tables
3. Cliquer sur un stream pour voir les détails
4. Exécuter des requêtes directement dans l'interface

### 9.3 Voir les topics créés

1. Menu **"Topics"**
2. Observer les topics créés par ksqlDB:
   - `user_events` (source)
   - `PURCHASES_STREAM`
   - `FRENCH_PURCHASES`
   - `USER_TOTAL_SPENT` (compacté!)

---

## 🎓 Exercices Pratiques

### Exercice 1 : Événements LOGIN par pays
Créez une table qui compte les événements LOGIN par pays.

<details>
<summary>Solution</summary>

```sql
CREATE TABLE login_by_country AS
    SELECT
        country,
        COUNT(*) AS login_count
    FROM user_events_stream
    WHERE event_type = 'LOGIN'
    GROUP BY country
    EMIT CHANGES;
```
</details>

### Exercice 2 : Top utilisateurs dépensiers
Créez une requête pour voir les 5 utilisateurs qui ont le plus dépensé.

<details>
<summary>Solution</summary>

```sql
SELECT user_id, total_spent
FROM user_total_spent
ORDER BY total_spent DESC
LIMIT 5;
```
</details>

### Exercice 3 : Achats par fenêtre de 1 minute
Créez une table avec fenêtre HOPPING de 1 minute, avançant toutes les 30 secondes.

<details>
<summary>Solution</summary>

```sql
CREATE TABLE purchases_hopping AS
    SELECT
        country,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_amount
    FROM purchases_stream
    WINDOW HOPPING (SIZE 1 MINUTE, ADVANCE BY 30 SECONDS)
    GROUP BY country
    EMIT CHANGES;
```
</details>

---

## 📝 Résumé des Concepts

### STREAM vs TABLE

| Aspect | STREAM | TABLE |
|--------|--------|-------|
| Nature | Flux d'événements | État actuel |
| Données | Append-only | Mise à jour par clé |
| Topic | Retention normale | **Compacté** |
| Utilisation | Filtres, transformations | Agrégations |

### Types de Fenêtres

**TUMBLING (fixes):**
```
[0-30s] [30-60s] [60-90s]
```

**HOPPING (chevauchantes):**
```
[0-60s]
    [30-90s]
        [60-120s]
```

### Commandes Utiles

```sql
SHOW STREAMS;           -- Lister les streams
SHOW TABLES;            -- Lister les tables
SHOW TOPICS;            -- Lister les topics Kafka
SHOW QUERIES;           -- Voir les requêtes en cours
DESCRIBE stream_name;   -- Voir le schéma
TERMINATE query_id;     -- Arrêter une requête
```

---

## 🧹 Nettoyage

```bash
# Arrêter les services
make stop

# Nettoyage complet (supprime aussi les volumes)
make clean
```

---

## 📚 Fichiers de Référence

- **`queries/setup.sql`** - Streams de base créés au setup
- **`queries/examples.sql`** - Plus de 20 exemples de requêtes ksqlDB
- **`scripts/generate_events.sh`** - Script de génération de données
- **`scripts/events_data.txt`** - Fichier de données source (format: user_id|event_type|amount|country)

Consultez `examples.sql` pour plus d'exemples avancés!

---

**Bravo! Vous maîtrisez maintenant ksqlDB et le stream processing SQL! 🚀**
