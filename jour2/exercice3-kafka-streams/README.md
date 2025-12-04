# Exercice 3 - Kafka Streams avec Spring Boot

## 🎯 Objectifs

- Comprendre les concepts de Kafka Streams
- Créer une pipeline de traitement de flux
- Explorer l'API Admin de Kafka
- Manipuler KStream et KTable
- Faire des jointures entre streams et tables
- Utiliser des topics compactés pour les données de référence
- Visualiser la topologie Kafka Streams
- Comprendre les transformations et agrégations

## 📋 Prérequis

- Docker et Docker Compose
- Java 17+
- Maven 3.8+
- Ports disponibles: 8080 (Kafka UI), 8081 (Producer), 8082 (Streams), 9092 (Kafka)

## 🏗️ Architecture

```
Producer (Spring Boot)
    ↓
    ├─> user-events (topic normal)
    └─> user-profiles (topic compacté)

Kafka Streams (Spring Boot)
    ↓
    Lit user-events
    ↓
    Filtre PURCHASE
    ↓
    Écrit dans filtered-events
```

## 📦 Structure du Projet

```
exercice3-kafka-streams/
├── pom.xml                 # Maven parent
├── docker-compose.yml      # Infrastructure Kafka
├── Makefile               # Commandes utiles
│
├── common/                # Module partagé
│   ├── pom.xml
│   └── src/main/java/com/kafka/training/common/
│       ├── model/
│       │   ├── Event.java           # Modèle partagé
│       │   ├── UserProfile.java     # Modèle partagé
│       │   └── EnrichedEvent.java   # Modèle partagé
│       └── serde/
│           └── JsonSerde.java       # Serde JSON personnalisé
│
├── producer/              # Module Producer
│   ├── pom.xml
│   └── src/main/java/com/kafka/training/producer/
│       ├── ProducerApplication.java
│       ├── config/
│       │   └── KafkaConfig.java        # API Admin + Producer
│       ├── service/
│       │   └── EventProducerService.java
│       └── controller/
│           └── ProducerController.java  # REST API
│
└── streams/               # Module Kafka Streams
    ├── pom.xml
    └── src/main/java/com/kafka/training/streams/
        ├── StreamsApplication.java
        ├── config/
        │   └── KafkaStreamsConfig.java
        └── topology/
            └── EventStreamTopology.java  # Pipeline à faire évoluer
```

**Note importante :** Le module `common` centralise les modèles de données et le JsonSerde, évitant ainsi les problèmes de désérialisation entre le producer et le streams.

---

## 🚀 Partie 1 - Démarrage

### 1.1 Lancer l'infrastructure

```bash
cd jour2/exercice3-kafka-streams
make start
```

Cela démarre :
- **Kafka** (port 9092)
- **Kafka UI** (port 8080)

⏳ **Attendre 30 secondes** pour le démarrage complet.

### 1.2 Compiler les projets

```bash
make build
```

---

## 🔧 Partie 2 - API Admin Kafka

### 2.1 Comment sont créés les topics ?

Les topics sont créés **de manière programmatique** via l'**API Kafka Admin** au démarrage du producer.

Ouvrez `producer/src/main/java/com/kafka/training/producer/config/KafkaConfig.java`

#### Configuration de l'Admin Client

```java
@Bean
public KafkaAdmin kafkaAdmin() {
    Map<String, Object> configs = new HashMap<>();
    configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    return new KafkaAdmin(configs);
}
```

Spring détecte automatiquement tous les beans `NewTopic` et les crée via le `KafkaAdmin` au démarrage.

#### Topic `user-events` (normal)

```java
@Bean
public NewTopic userEventsTopic() {
    return TopicBuilder.name("user-events")
            .partitions(3)          // 3 partitions pour parallélisme
            .replicas(1)            // 1 réplica (cluster à 1 broker)
            .build();
}
```

#### Topic `user-profiles` (compacté)

```java
@Bean
public NewTopic userProfilesTopic() {
    return TopicBuilder.name("user-profiles")
            .partitions(3)
            .replicas(1)
            .compact()              // cleanup.policy=compact
            .build();
}
```

☝️ **Important :** Le `.compact()` configure `cleanup.policy=compact`, ce qui signifie que Kafka ne conserve que **la dernière valeur par clé**. C'est essentiel pour les **KTable** car elles représentent un état actuel (et non un historique).

#### Topic `filtered-events` (sortie)

```java
@Bean
public NewTopic filteredEventsTopic() {
    return TopicBuilder.name("filtered-events")
            .partitions(3)
            .replicas(1)
            .build();
}
```

### 2.2 Pourquoi utiliser l'API Admin ?

| Avantage | Description |
|----------|-------------|
| **Contrôle total** | Nombre de partitions, replicas, compaction, rétention, etc. |
| **Reproductible** | Configuration dans le code, versionnée avec Git |
| **Pédagogique** | Les élèves voient explicitement la configuration des topics |
| **Production-ready** | Bonne pratique pour les environnements réels |

⚠️ **Alternative :** Kafka peut créer automatiquement les topics (`auto.create.topics.enable=true`), mais **sans contrôle** sur les paramètres (partitions par défaut, pas de compaction, etc.).

### 2.3 Lancer le Producer

Dans un terminal :

```bash
make run-producer
```

Le producer démarre sur **http://localhost:8081** et crée automatiquement les topics.

### 2.4 Vérifier les topics créés

Dans un autre terminal :

```bash
make topics
```

**Résultat attendu :**
```
user-events
user-profiles
```

### 2.5 Détails des topics

```bash
make describe-topics
```

Observez la différence entre :
- `user-events` : topic normal avec `cleanup.policy=delete`
- `user-profiles` : topic compacté avec `cleanup.policy=compact`

---

## 📊 Partie 3 - Générer des Données

### 3.1 Initialiser les profils utilisateurs

```bash
make init-data
```

Cela crée 5 profils dans le topic `user-profiles` (compacté) :
- user1: Alice (France, GOLD)
- user2: Bob (USA, SILVER)
- user3: Charlie (Germany, BRONZE)
- user4: Diana (UK, GOLD)
- user5: Eve (Spain, SILVER)

### 3.2 Générer des événements

```bash
make generate
```

Génère 20 événements aléatoires (PURCHASE, LOGIN, LOGOUT, VIEW, ADD_TO_CART).

### 3.3 Consommer les topics

**Événements :**
```bash
make consume-events
```

**Profils (avec clés) :**
```bash
make consume-profiles
```

Observez que les profils affichent `userId:{"userId":"user1",...}` car le topic est compacté avec userId comme clé.

---

## 🌊 Partie 4 - Pipeline Kafka Streams Simple

### 4.1 Comprendre la topologie initiale

Ouvrez `streams/src/main/java/com/kafka/training/streams/topology/EventStreamTopology.java`

La pipeline actuelle :
1. Lit `user-events`
2. Filtre uniquement les `PURCHASE`
3. Écrit dans `filtered-events`

```java
KStream<String, Event> eventsStream = streamsBuilder
    .stream("user-events", Consumed.with(Serdes.String(), eventSerde));

KStream<String, Event> purchaseStream = eventsStream
    .filter((key, event) -> "PURCHASE".equals(event.getEventType()));

purchaseStream.to("filtered-events", Produced.with(Serdes.String(), eventSerde));
```

### 4.2 Lancer Kafka Streams

Dans un nouveau terminal :

```bash
make run-streams
```

L'application démarre sur **port 8082**.

**Observez les logs :**
- La topologie s'affiche au démarrage
- Les événements sont traités en temps réel

### 4.3 Vérifier le résultat

```bash
make consume-filtered
```

Vous ne verrez que les événements `PURCHASE` !

### 4.4 Visualiser dans Kafka UI

Allez sur http://localhost:8080

1. Cliquez sur **Topics**
2. Observez les topics créés :
   - `user-events`
   - `user-profiles` (icône de compactage)
   - `filtered-events`
3. Consultez les messages dans chaque topic

### 4.5 Visualiser la topologie

La topologie Kafka Streams est exposée via une API REST sur le module streams.

**Option 1 : API JSON**
```bash
make show-topology
```

**Option 2 : Description texte**
```bash
curl http://localhost:8082/api/topology/describe
```

**Option 3 : Visualisation graphique**

1. Allez sur https://zz85.github.io/kafka-streams-viz/
2. Récupérez la topologie :
   ```bash
   curl http://localhost:8082/api/topology/describe
   ```
3. Collez le résultat dans le visualiseur
4. Admirez votre pipeline en graphe ! 🎨

---

## 🎓 Partie 5 - Exercices Kafka Streams

### Exercice 1 : Créer un KTable depuis user-profiles

**Objectif :** Charger les profils utilisateurs dans un KTable.

<details>
<summary>Indice</summary>

Un KTable est une table mise à jour en continu depuis un topic compacté.

```java
KTable<String, UserProfile> userProfileTable = streamsBuilder
    .table("user-profiles",
           Consumed.with(Serdes.String(), userProfileSerde));
```
</details>

<details>
<summary>Solution</summary>

Dans `EventStreamTopology.java`, ajoutez après la définition de `purchaseStream` :

```java
// KTable depuis le topic compacté
KTable<String, UserProfile> userProfileTable = streamsBuilder
    .table("user-profiles",
           Consumed.with(Serdes.String(), userProfileSerde),
           Materialized.as("user-profiles-store"));
```

Relancez Kafka Streams et observez les logs.
</details>

---

### Exercice 2 : Jointure Stream-Table

**Objectif :** Enrichir les achats avec les informations utilisateur (nom, tier).

<details>
<summary>Indice</summary>

Utilisez `join()` entre le KStream et le KTable :

```java
KStream<String, EnrichedEvent> enrichedStream = purchaseStream.join(
    userProfileTable,
    (event, profile) -> { /* créer EnrichedEvent */ }
);
```
</details>

<details>
<summary>Solution</summary>

```java
// Jointure : enrichir les achats avec les profils
KStream<String, EnrichedEvent> enrichedStream = purchaseStream.join(
    userProfileTable,
    (event, profile) -> {
        EnrichedEvent enriched = new EnrichedEvent();
        enriched.setUserId(event.getUserId());
        enriched.setEventType(event.getEventType());
        enriched.setAmount(event.getAmount());
        enriched.setCountry(event.getCountry());
        enriched.setTimestamp(event.getTimestamp());
        enriched.setUserName(profile.getName());
        enriched.setUserTier(profile.getTier());
        return enriched;
    }
);

// Écrire dans un nouveau topic
enrichedStream.to("enriched-events",
                  Produced.with(Serdes.String(), enrichedEventSerde));
```

Ajoutez dans `KafkaConfig.java` le topic de sortie :

```java
@Bean
public NewTopic enrichedEventsTopic() {
    return TopicBuilder.name("enriched-events")
            .partitions(3)
            .replicas(1)
            .build();
}
```
</details>

---

### Exercice 3 : Filtrer les achats GOLD

**Objectif :** Ne garder que les achats des utilisateurs GOLD.

<details>
<summary>Solution</summary>

```java
KStream<String, EnrichedEvent> goldPurchases = enrichedStream
    .filter((key, event) -> "GOLD".equals(event.getUserTier()))
    .peek((key, event) -> log.info("GOLD purchase: user={}, amount={}",
                                    event.getUserName(), event.getAmount()));

goldPurchases.to("gold-purchases",
                 Produced.with(Serdes.String(), enrichedEventSerde));
```
</details>

---

### Exercice 4 : Compter les achats par utilisateur

**Objectif :** Créer une table d'agrégation avec le nombre d'achats par user.

<details>
<summary>Indice</summary>

Utilisez `groupByKey()` puis `count()` :

```java
KTable<String, Long> purchaseCount = purchaseStream
    .groupByKey()
    .count();
```
</details>

<details>
<summary>Solution</summary>

```java
// Compter les achats par utilisateur
KTable<String, Long> purchaseCountTable = purchaseStream
    .groupByKey(Grouped.with(Serdes.String(), eventSerde))
    .count(Materialized.as("purchase-count-store"));

// Convertir en stream pour logger
purchaseCountTable.toStream()
    .peek((userId, count) -> log.info("User {} has {} purchases", userId, count));
```

Le state store `purchase-count-store` est créé automatiquement.
</details>

---

### Exercice 5 : Somme des montants par pays

**Objectif :** Calculer le total dépensé par pays.

<details>
<summary>Solution</summary>

```java
// Grouper par pays et sommer les montants
KTable<String, Double> totalByCountry = purchaseStream
    .groupBy(
        (key, event) -> event.getCountry(),
        Grouped.with(Serdes.String(), eventSerde)
    )
    .aggregate(
        () -> 0.0,  // Valeur initiale
        (country, event, total) -> total + event.getAmount(),
        Materialized.with(Serdes.String(), Serdes.Double())
    );

totalByCountry.toStream()
    .peek((country, total) -> log.info("Country {} total: {}", country, total));
```
</details>

---

### Exercice 6 : Fenêtres temporelles (AVANCÉ)

**Objectif :** Compter les achats par fenêtre de 1 minute.

<details>
<summary>Solution</summary>

```java
// Fenêtre tumbling de 1 minute
KTable<Windowed<String>, Long> windowedCount = purchaseStream
    .groupByKey(Grouped.with(Serdes.String(), eventSerde))
    .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1)))
    .count();

windowedCount.toStream()
    .peek((windowed, count) ->
        log.info("Window [{} - {}]: user={}, count={}",
                 windowed.window().startTime(),
                 windowed.window().endTime(),
                 windowed.key(),
                 count));
```
</details>

---

## 📊 Partie 6 - Visualisation Kafka UI

### 6.1 Explorer les topics

Sur http://localhost:8080 :

1. **Topics** → `user-profiles`
   - Observez `cleanup.policy=compact`
   - Les clés sont visibles (userId)

2. **Topics** → `filtered-events`
   - Uniquement des PURCHASE

3. **Topics** → `enriched-events` (si créé)
   - Événements avec nom et tier

### 6.2 Consumer Groups

1. **Consumers** → `event-processor`
   - C'est l'application Kafka Streams
   - Observez les partitions assignées
   - Lag = 0 si tout est traité

---

## 🎯 Partie 7 - Concepts Clés

### KStream vs KTable

| Aspect | KStream | KTable |
|--------|---------|--------|
| Nature | Flux d'événements | État actuel |
| Données | Tous les événements | Dernière valeur par clé |
| Topic | Normal (delete) | Compacté (compact) |
| Utilisation | Transformations, filtres | Jointures, lookups |

### GlobalKTable vs KTable

| KTable | GlobalKTable |
|--------|--------------|
| Partitionné | Répliqué entièrement |
| Rejoint sur la clé | Rejoint sur n'importe quoi |
| Moins de mémoire | Plus de mémoire |
| Plus rapide | Lookup flexible |

**Dans cet exercice, on utilise KTable sans RocksDB car les données sont petites.**

### Topic Compacté

- Conserve uniquement la **dernière valeur par clé**
- Utilisé pour les données de référence (users, config, etc.)
- Idéal pour les KTable

---

## 🧪 Partie 8 - Tests et Débug

### 8.1 Générer beaucoup d'événements

```bash
make generate-100
```

Observez dans Kafka Streams :
- Le traitement en temps réel
- Les agrégations qui se mettent à jour

### 8.2 Modifier un profil utilisateur

```bash
curl -X POST http://localhost:8081/api/profiles \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user1",
    "name": "Alice Updated",
    "country": "France",
    "tier": "PLATINUM"
  }'
```

Le topic compacté ne gardera que la dernière version !

### 8.3 Voir la topologie

Dans les logs de Kafka Streams au démarrage, vous verrez :

```
Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [user-events])
      --> KSTREAM-FILTER-0000000001
    Processor: KSTREAM-FILTER-0000000001 (stores: [])
      --> KSTREAM-SINK-0000000002
    Sink: KSTREAM-SINK-0000000002 (topic: filtered-events)
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

## 📚 Ressources

- [Kafka Streams Documentation](https://kafka.apache.org/documentation/streams/)
- [Spring Kafka Streams](https://docs.spring.io/spring-kafka/reference/html/#kafka-streams)
- [Kafka Admin API](https://kafka.apache.org/documentation/#adminapi)

---

**Bravo ! Vous maîtrisez maintenant Kafka Streams ! 🌊**
