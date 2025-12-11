# Exercice All-in-One - Plateforme de Devis d'Assurance

## 🎯 Objectifs Pédagogiques

### Architecture Complète Kafka
- Pipeline complète de traitement temps réel avec Kafka Streams
- Pattern CQRS (Command Query Responsibility Segregation)
- Intégration Kafka Connect pour synchronisation vers Redis
- API REST de lecture et d'écriture
- Monitoring complet avec Prometheus et Grafana

### Kafka Streams
- Manipulation de KStream et KTable
- Filtrage et transformations de flux
- Jointures stream-table pour enrichissement
- Agrégations avec fenêtres temporelles
- Topics compactés pour données de référence

### Intégration & Architecture
- Kafka Connect avec Redis Sink Connector
- Redis comme cache de lecture (read model)
- Séparation write model (Producer) / read model (Query API)
- Module commun pour partage de modèles

### Monitoring
- 482 métriques Kafka exposées via Prometheus
- 3 dashboards Grafana pré-configurés (26 panels)
- Métriques Producer, Streams et Broker en temps réel

---

## 📊 Architecture Globale

```
┌─────────────────┐
│  Producer API   │ (Port 8081)
│  Write Model    │ POST /api/quotes
│                 │ POST /api/pricing/init
└────────┬────────┘
         │
         ↓
┌─────────────────────────────────┐
│      Kafka Topics               │
│  • devis-events                 │
│  • product-pricing (compacté)   │
└────────┬────────────────────────┘
         │
         ↓
┌─────────────────┐
│ Kafka Streams   │ (Port 8082)
│  Topologie:     │
│  - Filter       │ ← Devis validés uniquement
│  - Join         │ ← Enrichissement avec prix
│  - Transform    │ ← Calcul prime finale
│  - Aggregate    │ ← Stats par client
└────────┬────────┘
         │
         ↓
┌─────────────────────────────────┐
│     Output Topics               │
│  • validated-quotes             │
│  • all-quotes (→ Redis)         │
│  • quote-aggregates             │
└────────┬────────────────────────┘
         │
         ↓
┌─────────────────┐
│ Kafka Connect   │ (Port 8083)
│  Redis Sink     │
└────────┬────────┘
         │
         ↓
┌─────────────────┐        ┌──────────────┐
│     Redis       │ ←────  │  Prometheus  │
│   (Cache)       │        │   Grafana    │
└────────┬────────┘        └──────────────┘
         │
         ↓
┌─────────────────┐
│   Query API     │ (Port 8084)
│   Read Model    │ GET /api/quotes
│                 │ GET /api/quotes/stats
└─────────────────┘
```

---

## 📋 Prérequis

- Docker et Docker Compose
- Java 17+
- Maven 3.8+
- Ports disponibles : 8080-8085, 9090, 3000, 9092, 6379

---

## ⚠️ NOTE IMPORTANTE : Ordre de Démarrage

**L'ordre des étapes est CRUCIAL pour éviter les erreurs !**

Kafka Streams a besoin que les topics d'entrée existent **AVANT** de démarrer. Suivez cet ordre :

1. ✅ Démarrer l'infrastructure Docker (Kafka, Redis, etc.)
2. ✅ Compiler les projets Maven
3. ✅ Lancer le **Producer**
4. ✅ **Initialiser les données de référence** (create topics)
5. ✅ Lancer **Kafka Streams** (après création des topics)
6. ✅ Configurer Kafka Connect
7. ✅ Lancer le Query API

---

## 🚀 PARTIE 1 - Démarrage de l'Infrastructure

### Étape 1.1 : Lancer tous les services Docker

```bash
make start
```

Cette commande démarre :
- ✅ **Kafka** (port 9092) - Broker en mode KRaft
- ✅ **Kafka UI** (port 8080) - Interface web
- ✅ **Redis** (port 6379) - Cache pour read model
- ✅ **Redis Commander** (port 8085) - UI Redis
- ✅ **Kafka Connect** (port 8083) - Connecteur Redis
- ✅ **Prometheus** (port 9090) - Collecte de métriques
- ✅ **Grafana** (port 3000) - Visualisation métriques

⏳ **Attendre 40 secondes** pour le démarrage complet.

### Étape 1.2 : Vérifier les services

```bash
make status
```

Tous les services doivent être **UP** :
```
kafka            Up
kafka-ui         Up
redis            Up
redis-commander  Up
kafka-connect    Up
prometheus       Up
grafana          Up
```

### Étape 1.3 : Compiler les projets Maven

```bash
make build
```

Cela compile les 4 modules :
- ✅ **common** - Modèles partagés
- ✅ **producer** - API d'écriture
- ✅ **streams** - Traitement temps réel
- ✅ **query-api** - API de lecture

---

## 📦 PARTIE 2 - Structure du Projet

```
exercice1-all-in-one/
├── common/                    # Module partagé
│   └── src/main/java/.../model/
│       ├── Quote.java         # Devis d'assurance
│       ├── QuoteStatus.java   # DRAFT/VALIDATED/CANCELLED/EXPIRED
│       ├── ProductPricing.java    # Référentiel de prix
│       ├── EnrichedQuote.java     # Devis enrichi
│       └── QuoteAggregate.java    # Statistiques

├── producer/                  # API d'écriture (Port 8081)
│   └── controller/QuoteController.java
│       ├── POST /api/quotes           # Créer un devis
│       ├── POST /api/quotes/{id}/validate
│       ├── POST /api/quotes/{id}/cancel
│       ├── POST /api/quotes/generate  # Générer N devis
│       └── POST /api/pricing/init     # Init référentiel

├── streams/                   # Traitement (Port 8082)
│   └── topology/QuoteStreamTopology.java
│       ├── Filter: devis validés
│       ├── Join: enrichissement avec prix
│       ├── Transform: calcul prime finale
│       └── Aggregate: stats par client

├── query-api/                 # API de lecture (Port 8084)
│   └── controller/QuoteQueryController.java
│       ├── GET /api/quotes            # Tous les devis
│       ├── GET /api/quotes/{id}
│       ├── GET /api/quotes/customer/{id}
│       ├── GET /api/quotes/product/{code}
│       └── GET /api/quotes/stats

└── docker-compose.yml         # Infrastructure complète
```

---

## 🔧 PARTIE 3 - Démarrage des Applications

### Étape 3.1 : Lancer le Producer (Terminal 1)

```bash
make run-producer
```

Le Producer démarre sur **http://localhost:8081** et :
- ✅ Crée automatiquement les topics Kafka via l'API Admin
- ✅ Expose l'API REST pour créer des devis
- ✅ Expose les métriques Prometheus sur `/actuator/prometheus`

**Topics créés automatiquement :**
- `devis-events` - Tous les événements de devis
- `product-pricing` - Référentiel de prix (compacté)

**Vérifier que le Producer fonctionne :**
```bash
curl http://localhost:8081/actuator/health
# Réponse attendue: {"status":"UP"}
```

### Étape 3.2 : Initialiser les Données de Référence

⚠️ **IMPORTANT :** Il faut créer les topics d'entrée AVANT de lancer Kafka Streams !

```bash
make init-pricing
```

**Ce qui se passe :**
1. ✅ 5 produits sont créés dans `product-pricing` (topic compacté)
2. ✅ Le topic `product-pricing` est automatiquement créé
3. ✅ Kafka Streams pourra ensuite charger ces données dans un KTable

**Produits créés :**
| Code | Nom | Prix de base | Taxe |
|------|-----|--------------|------|
| AUTO | Auto Insurance | 500€ | 20% |
| HOME | Home Insurance | 800€ | 15% |
| HEALTH | Health Insurance | 1200€ | 10% |
| LIFE | Life Insurance | 2000€ | 5% |
| TRAVEL | Travel Insurance | 150€ | 25% |

**Vérifier dans Kafka UI (http://localhost:8080) :**
- Topic `product-pricing` → 5 messages
- Observer `cleanup.policy=compact`

### Étape 3.3 : Lancer Kafka Streams (Terminal 2)

```bash
make run-streams
```

Kafka Streams démarre sur **http://localhost:8082** et :
- ✅ Lit les topics d'entrée (`devis-events` et `product-pricing`)
- ✅ Crée les topics de sortie automatiquement
- ✅ Démarre la topologie de traitement
- ✅ Expose les métriques Prometheus

**Topics créés par Streams :**
- `validated-quotes` - Devis validés uniquement
- `all-quotes` - Devis enrichis (→ Redis)
- `quote-aggregates` - Statistiques par client

**Vérifier les logs :** La topologie s'affiche au démarrage sans erreur.

### Étape 3.4 : Configurer Kafka Connect vers Redis

```bash
make connect-redis
```

Cette commande configure le **Redis Sink Connector** qui :
- ✅ Lit le topic `all-quotes`
- ✅ Écrit chaque devis enrichi dans Redis
- ✅ Utilise quoteId comme clé Redis

**Vérifier le connecteur :**
```bash
make connect-status
# Doit afficher: ["redis-sink-quotes"]
```

### Étape 3.5 : Lancer le Query API (Terminal 3)

```bash
make run-query-api
```

Query API démarre sur **http://localhost:8084** et :
- ✅ Se connecte à Redis
- ✅ Expose l'API REST de lecture
- ✅ Expose les métriques Prometheus

**Vérifier le Query API :**
```bash
curl http://localhost:8084/api/quotes/health
# Réponse: {"status":"UP","service":"query-api"}
```

---

## 📊 PARTIE 4 - Alimentation de la Pipeline (2 Vagues Supplémentaires)

⚠️ **Note :** La VAGUE 1 (initialisation du référentiel) a déjà été effectuée à l'étape 3.2.

### 🌊 VAGUE 2 : Création de Devis (Mix d'États)

**Objectif :** Générer 20 devis avec différents statuts pour tester le filtrage.

```bash
make generate-quotes
```

**Ce qui se passe :**
1. ✅ 20 devis créés avec statuts aléatoires :
   - ~12 devis **VALIDATED** (60%)
   - ~4 devis **DRAFT** (20%)
   - ~4 devis **CANCELLED** (20%)

2. ✅ **Producer** → écrit dans `devis-events`

3. ✅ **Kafka Streams** :
   - Filtre → Garde uniquement les VALIDATED (~12)
   - Join → Enrichit avec `product-pricing` (KTable)
   - Transform → Calcule `finalPremium = basePremium * (1 + taxRate)`
   - Écrit dans `all-quotes`

4. ✅ **Kafka Connect** → Synchronise vers Redis

5. ✅ **Query API** → Peut maintenant lire depuis Redis

**Attendre 5 secondes** pour la propagation complète.

**Vérifier le filtrage :**
```bash
# Topic d'entrée : tous les devis (20)
make consume-quotes

# Topic filtré : devis validés uniquement (~12)
make consume-validated
```

**Vérifier l'enrichissement :**
```bash
# Devis enrichis avec productName, basePrice, taxRate, finalPremium
make consume-all-quotes
```

Exemple de devis enrichi :
```json
{
  "quoteId": "Q-a3f2b1c4",
  "customerId": "C002",
  "status": "VALIDATED",
  "productCode": "AUTO",
  "basePremium": 450.0,
  "finalPremium": 540.0,      ← Calculé: 450 * 1.20
  "productName": "Auto Insurance",
  "basePrice": 500.0,
  "taxRate": 0.20,
  "createdAt": 1702394821000,
  "updatedAt": 1702394821000
}
```

**Vérifier dans Redis :**
```bash
make redis-cli
> KEYS Q-*
# Devrait afficher ~12 clés (devis validés)
> GET Q-a3f2b1c4
# Affiche le JSON complet du devis enrichi
> exit
```

**Vérifier via Query API :**
```bash
# Tous les devis
make query-all

# Statistiques globales
make query-stats
```

Réponse attendue de `/stats` :
```json
{
  "totalQuotes": 12,
  "totalPremium": 8450.50,
  "averagePremium": 704.21,
  "byProduct": {
    "AUTO": 3,
    "HOME": 2,
    "HEALTH": 4,
    "LIFE": 1,
    "TRAVEL": 2
  }
}
```

---

### 🌊 VAGUE 3 : Évolution des Devis (Changements d'État)

**Objectif :** Simuler le cycle de vie des devis avec validations et annulations.

#### 3.1 Générer 50 nouveaux devis

```bash
curl -X POST "http://localhost:8081/api/quotes/generate?count=50"
```

**Résultat :** ~30 validés, ~10 draft, ~10 cancelled.

**Attendre 5 secondes** puis vérifier Redis :
```bash
make redis-cli
> DBSIZE
# Devrait afficher ~42 (12 + 30 nouveaux validés)
> exit
```

#### 3.2 Valider un devis spécifique

```bash
curl -X POST "http://localhost:8081/api/quotes/Q-12345678/validate"
```

**Ce qui se passe :**
1. ✅ Producer crée un événement avec `status=VALIDATED`
2. ✅ Streams filtre et enrichit
3. ✅ Kafka Connect écrit dans Redis
4. ✅ Query API peut le lire immédiatement

**Vérifier :**
```bash
curl http://localhost:8084/api/quotes/Q-12345678
```

#### 3.3 Annuler un devis

```bash
curl -X POST "http://localhost:8081/api/quotes/Q-12345678/cancel"
```

**Ce qui se passe :**
1. ✅ Producer crée un événement avec `status=CANCELLED`
2. ✅ Streams **ignore** car pas VALIDATED
3. ✅ Le devis **reste dans Redis** (ancienne version validée)

⚠️ **Note :** Dans cette version simplifiée, les annulations ne suppriment pas de Redis. Pour une vraie gestion, il faudrait :
- Soit écrire `null` dans `all-quotes` (Kafka tombstone)
- Soit un processus de nettoyage dans Query API

#### 3.4 Vérifier les agrégations par client

```bash
# Consommer le topic d'agrégations
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic quote-aggregates \
  --from-beginning \
  --max-messages 5
```

**Résultat attendu :** Statistiques par client avec fenêtre de 1h :
```json
{
  "customerId": "C001",
  "windowStart": 1702394400000,
  "windowEnd": 1702398000000,
  "count": 8,
  "totalPremium": 4850.75
}
```

#### 3.5 Requêtes Query API par client

```bash
# Tous les devis du client C001
make query-customer C=C001

# Tous les devis Auto
make query-product P=AUTO
```

---

## 📊 PARTIE 5 - Visualisation et Monitoring

### Étape 5.1 : Kafka UI

Ouvrir **http://localhost:8080**

**Exploration des topics :**
1. Topics → `devis-events` → Messages
   - Observer les différents statuts
2. Topics → `product-pricing` → Messages
   - Observer `cleanup.policy=compact`
   - Voir les clés (productCode)
3. Topics → `all-quotes` → Messages
   - Uniquement des devis enrichis

**Consumer Groups :**
- `event-processor` → Application Kafka Streams
- Observer le lag (devrait être proche de 0)

### Étape 5.2 : Redis Commander

Ouvrir **http://localhost:8085**

**Exploration :**
- Voir toutes les clés `Q-*`
- Cliquer sur une clé pour voir le JSON complet
- Observer la structure EnrichedQuote

### Étape 5.3 : Prometheus

Ouvrir **http://localhost:9090**

**Requêtes utiles :**

```promql
# Taux d'envoi Producer
rate(kafka_producer_record_send_total[1m])

# Taux de traitement Streams
rate(kafka_stream_task_process_total[1m])

# Consumer lag
sum(kafka_consumer_fetch_manager_records_lag) by (topic)

# Throughput Broker (bytes/sec)
rate(kafka_server_brokertopicmetrics_bytesin_total[1m])
```

### Étape 5.4 : Grafana

Ouvrir **http://localhost:3000** (admin/admin)

**Dashboards disponibles :**

1. **Kafka Producer Metrics** (6 panels)
   - Records Sent Rate
   - Request Latency
   - Buffer Memory
   - Batch Size
   - Error & Retry Rate
   - Waiting Threads

2. **Kafka Streams Metrics** (9 panels)
   - Process Rate
   - Commit Latency
   - Consumer Lag par partition
   - Poll Records Average
   - Alive Threads
   - Assigned Partitions
   - State Store Size
   - Poll Latency
   - Punctuate Latency

3. **Kafka Broker Metrics** (11 panels)
   - Network Throughput
   - Messages In Rate
   - Request Latency
   - Under Replicated Partitions
   - Log Size par partition
   - CPU Usage
   - JVM Heap Memory
   - GC Collection Time
   - Thread Count

---

## 🎯 PARTIE 6 - Tests de Bout en Bout

### Test 1 : Pipeline Complète

```bash
# 1. Créer un devis
QUOTE_ID=$(curl -s -X POST http://localhost:8081/api/quotes \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "C001",
    "status": "DRAFT",
    "productCode": "AUTO",
    "basePremium": 450.0
  }' | jq -r '.quoteId')

# 2. Le devis n'est pas encore dans Redis (DRAFT)
curl http://localhost:8084/api/quotes/$QUOTE_ID
# → 404 Not Found

# 3. Valider le devis
curl -X POST "http://localhost:8081/api/quotes/$QUOTE_ID/validate"

# 4. Attendre 2 secondes
sleep 2

# 5. Le devis est maintenant dans Redis (enrichi)
curl http://localhost:8084/api/quotes/$QUOTE_ID
# → 200 OK avec finalPremium calculée
```

### Test 2 : Génération Massive

```bash
# Générer 100 devis
make generate-100

# Attendre 10 secondes
sleep 10

# Vérifier les statistiques
make query-stats
```

### Test 3 : Visualiser la Topologie

```bash
make topology-describe
```

Copier la sortie et coller sur : https://zz85.github.io/kafka-streams-viz/

**Vous verrez graphiquement :**
- Source: devis-events
- Source: product-pricing (table)
- Filter: status == VALIDATED
- Join: quotes + pricing
- Sink: all-quotes
- Aggregate: fenêtre 1h

---

## 🎓 PARTIE 7 - Concepts Clés Démontrés

### 7.1 Pattern CQRS

| Write Model | Read Model |
|-------------|------------|
| Producer API (8081) | Query API (8084) |
| POST /api/quotes | GET /api/quotes |
| Écrit dans Kafka | Lit depuis Redis |
| Optimisé pour écriture | Optimisé pour lecture |

### 7.2 Kafka Streams Operations

```java
// 1. Filter
quotesStream.filter((k, v) -> v.getStatus() == VALIDATED)

// 2. Join (Stream + Table)
quotesStream.join(pricingTable, (quote, pricing) -> {...})

// 3. Transform
quote.setFinalPremium(basePremium * (1 + taxRate))

// 4. Aggregate (avec fenêtre)
.windowedBy(TimeWindows.of(Duration.ofHours(1)))
.aggregate(...)
```

### 7.3 Topic Compacté vs Normal

| Aspect | Normal (devis-events) | Compacté (product-pricing) |
|--------|----------------------|----------------------------|
| Cleanup | delete (par temps) | compact (par clé) |
| Historique | Tous les événements | Dernière valeur uniquement |
| Usage | KStream | KTable |
| Exemple | Flux de devis | Référentiel de prix |

### 7.4 Kafka Connect

- **Source Connector** : Externe → Kafka
- **Sink Connector** : Kafka → Externe (Redis dans notre cas)
- **Avantage** : Pas de code, juste configuration JSON

---

## 🧪 PARTIE 8 - Expérimentations Avancées

### Expérience 1 : Mettre à Jour un Prix

```bash
curl -X POST http://localhost:8081/api/pricing \
  -H "Content-Type: application/json" \
  -d '{
    "productCode": "AUTO",
    "productName": "Auto Insurance PROMO",
    "basePrice": 400.0,
    "taxRate": 0.18
  }'
```

**Résultat :**
- ✅ Le topic compacté garde la nouvelle valeur
- ✅ Le KTable se met à jour automatiquement
- ✅ Les prochains devis AUTO auront le nouveau prix

### Expérience 2 : Stress Test

```bash
# Générer 500 devis rapidement
for i in {1..5}; do
  curl -X POST "http://localhost:8081/api/quotes/generate?count=100" &
done
wait
```

**Observer dans Grafana :**
- Process Rate augmente
- Consumer Lag monte puis redescend
- JVM Heap Memory utilisé

### Expérience 3 : Redémarrer Streams

```bash
# 1. Arrêter Kafka Streams (Ctrl+C dans Terminal 2)
# 2. Générer des devis
make generate-quotes
# 3. Relancer Kafka Streams
make run-streams
```

**Résultat :**
- ✅ Streams rattrape le lag automatiquement
- ✅ Tous les devis sont traités
- ✅ Rien n'est perdu (durabilité Kafka)

---

## 📚 PARTIE 9 - Documentation Complète

### Commandes Make Disponibles

```bash
# Infrastructure
make start           # Démarrer tout
make stop            # Arrêter tout
make clean           # Supprimer volumes
make status          # Vérifier statut

# Build & Run
make build           # Compiler Maven
make run-producer    # Lancer Producer
make run-streams     # Lancer Streams
make run-query-api   # Lancer Query API

# Données
make init-pricing    # Init référentiel
make generate-quotes # 20 devis
make generate-100    # 100 devis

# Topics Kafka
make topics          # Lister topics
make describe-topics # Détails topics
make consume-quotes  # Consommer devis
make consume-validated # Consommer validés
make consume-all-quotes # Consommer enrichis

# Query API
make query-all       # GET /quotes
make query-stats     # GET /quotes/stats
make query-customer C=C001 # Par client
make query-product P=AUTO  # Par produit

# Redis
make redis-cli       # Redis CLI
make redis-ui        # Ouvrir Commander

# Kafka Connect
make connect-status  # Statut
make connect-redis   # Configurer

# Monitoring
make prometheus      # Ouvrir Prometheus
make grafana         # Ouvrir Grafana
make topology        # Topologie JSON
make topology-describe # Topologie texte
```

### URLs des Services

| Service | URL | Credentials |
|---------|-----|-------------|
| Producer API | http://localhost:8081 | - |
| Streams API | http://localhost:8082 | - |
| Kafka Connect | http://localhost:8083 | - |
| Query API | http://localhost:8084 | - |
| Kafka UI | http://localhost:8080 | - |
| Redis Commander | http://localhost:8085 | - |
| Prometheus | http://localhost:9090 | - |
| Grafana | http://localhost:3000 | admin/admin |


---

## 🧹 Nettoyage

```bash
# Arrêter proprement
Ctrl+C dans chaque terminal (Producer, Streams, Query API)

# Arrêter Docker
make stop

# Nettoyage complet (supprime volumes)
make clean
```

---

## 🎯 Points Clés de l'Exercice

✅ **Aucun développement requis** - Tout est pré-implémenté
✅ **3 vagues d'alimentation** - Pour voir l'évolution des données
✅ **Pipeline complète** - Du Producer au Query API via Redis
✅ **Monitoring temps réel** - Prometheus + Grafana
✅ **Pattern CQRS** - Séparation write/read
✅ **Kafka Streams** - Filter, Join, Transform, Aggregate
✅ **Architecture modulaire** - 4 modules Maven

---

## 🎉 Félicitations !

Vous avez maintenant une **plateforme complète de traitement de devis d'assurance** avec :
- Kafka Streams pour le traitement temps réel
- Redis pour le cache de lecture
- Kafka Connect pour la synchronisation
- Monitoring complet avec Prometheus et Grafana
- APIs REST séparées (write/read)

**Cette architecture est utilisée en production dans de nombreuses entreprises !** 🚀
