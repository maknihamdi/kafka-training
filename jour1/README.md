# Jour 1 - Fondamentaux de Kafka

## 🎯 Objectifs de la journée
- Découvrir l'architecture Kafka (cluster, brokers, topics, partitions)
- Comprendre les concepts fondamentaux par l'exploration
- Identifier les notions clés de Kafka
- Observer le partitionnement et les offsets

---

## Exercice 1 - Explorer le Cluster Kafka

**Durée:** 45 minutes

### Objectifs
- Démarrer un cluster Kafka en mode KRaft (sans Zookeeper)
- Explorer l'interface Kafka UI
- Découvrir par observation les concepts Kafka
- Identifier et lister les notions fondamentales

### Approche pédagogique
Cet exercice utilise une **approche par découverte**. Les participants explorent librement l'interface Kafka UI et identifient eux-mêmes les concepts clés. Cette méthode favorise l'apprentissage actif et la mémorisation.

### Notions découvertes
À travers cet exercice, les participants vont découvrir:
- **Architecture**: Cluster, Broker, KRaft
- **Organisation des données**: Topic, Partition, Replication Factor
- **Structure des messages**: Message, Key, Value, Header, Timestamp
- **Positionnement**: Offset
- **Schémas**: Schema Registry, JSON Schema
- **Types de données**: String, JSON, Binary

### Contenu fourni
- Docker Compose avec Kafka en mode KRaft
- Kafka UI pour l'exploration visuelle
- Schema Registry pour la gestion des schémas
- 7 topics pré-provisionnés avec différents types de données:
  - `events` - Messages texte simples (3 partitions)
  - `users` - JSON sans clé (2 partitions)
  - `orders` - JSON avec clés (4 partitions)
  - `logs` - Logs texte (1 partition)
  - `images` - Données binaires (2 partitions)
  - `transactions` - JSON avec clés et headers (2 partitions)
  - `products` - JSON avec JSON Schema enregistré (2 partitions)

### Livrable
Les participants doivent produire une **liste de 10-15 notions/concepts** identifiés avec:
- Le nom de la notion
- Où ils l'ont trouvée dans l'UI
- Leur hypothèse sur ce que c'est

### Déroulement
1. **30 min** - Exploration libre de Kafka UI
2. **15 min** - Mise en commun et explication des notions identifiées

### Accès à l'exercice
📁 Dossier: `jour1/exercice1-explorer-cluster/`

Voir le README de l'exercice pour les instructions détaillées.

---

## 📚 Ressources Complémentaires

### Documentation
- [Kafka Documentation Officielle](https://kafka.apache.org/documentation/)
- [KRaft Mode](https://kafka.apache.org/documentation/#kraft)

### Commandes Kafka Essentielles

#### Lister les topics
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

#### Décrire un topic
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic orders
```

#### Consommer des messages
```bash
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders \
  --from-beginning \
  --property print.key=true \
  --property print.partition=true \
  --property print.offset=true
```

#### Créer un topic
```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic mon-topic \
  --partitions 3 \
  --replication-factor 1
```

---

## 📝 Notes pour le Formateur

### Points clés à aborder après l'exercice 1

**Architecture Kafka**
- Le cluster et ses composants
- Le rôle du broker
- KRaft vs Zookeeper (évolution)

**Topics et Partitions**
- Le topic comme canal logique
- Le partitionnement pour la scalabilité
- L'ordre garanti par partition

**Messages**
- Structure: Key, Value, Headers, Timestamp
- Le rôle de la clé dans le partitionnement
- Les headers pour les métadonnées

**Offsets**
- Position unique par partition
- Utilisé par les consumers pour tracker leur progression
- Séquentiel et croissant

**Schema Registry**
- Gestion centralisée des schémas
- Évolution des schémas
- Compatibilité
