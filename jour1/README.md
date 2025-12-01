# Jour 1 - Fondamentaux de Kafka

## 🎯 Objectifs de la journée
- Découvrir l'architecture Kafka (cluster, brokers, topics, partitions)
- Comprendre les concepts fondamentaux par l'exploration
- Identifier les notions clés de Kafka
- Observer le partitionnement et les offsets
- Créer et configurer des topics avec différents paramètres
- Comprendre le mécanisme des segments et leur impact sur le stockage
- Maîtriser les politiques de rétention et la log compaction
- Comprendre la réplication et la haute disponibilité
- Tester la résilience du cluster face aux pannes
- Identifier les conditions d'une partition offline

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

## Exercice 2 - Topics et Segments

**Durée:** 60 minutes

### Objectifs
- Créer des topics avec différentes configurations
- Comprendre le mécanisme de segmentation des logs
- Explorer le système de fichiers Kafka
- Analyser les fichiers de segments (.log, .index, .timeindex)
- Configurer les politiques de rétention (temps, taille, combinée)
- Comprendre et utiliser la log compaction

### Approche pédagogique
Cet exercice est **pratique et orienté ligne de commande**. Les participants créent des topics avec différents paramètres de segmentation, produisent des messages, et explorent directement le système de fichiers Kafka pour observer comment les segments sont créés et gérés.

### Notions approfondies
À travers cet exercice, les participants vont approfondir:
- **Configuration de topics**: partitions, segment.bytes, segment.ms
- **Segments**: .log (données), .index (index d'offset), .timeindex (index de temps)
- **Stockage**: organisation physique dans /var/lib/kafka/data/
- **Analyse**: kafka-dump-log pour inspecter les segments
- **Rétention**:
  - Basée sur le temps (retention.ms)
  - Basée sur la taille (retention.bytes)
  - Politique combinée (temps ET taille)
- **Log Compaction**:
  - cleanup.policy=compact
  - Rétention par clé (dernière valeur uniquement)
  - Tombstone records (suppression logique)
  - Cas d'usage: état utilisateur, cache, CDC

### Contenu de l'exercice
L'exercice est divisé en 8 parties:
1. **Topic avec configuration par défaut** - Observer la configuration standard
2. **Topic avec 10 partitions** - Impact du partitionnement sur le stockage
3. **Topic avec segments de petite taille** - Forcer la création de multiples segments (segment.bytes=500)
4. **Production de messages** - Via Kafka UI pour remplir les topics
5. **Exploration du système de fichiers** - Accès shell pour observer les segments
6. **Politiques de rétention**:
   - Rétention basée sur le temps (1 minute)
   - Rétention basée sur la taille (2KB)
   - Rétention combinée
7. **Log Compaction**:
   - Configuration d'un topic compacté
   - Test avec des clés dupliquées
   - Utilisation des tombstones pour supprimer des clés
8. **Analyse et comparaison** - Tableau récapitulatif de toutes les configurations

### Livrables
Les participants doivent:
- Créer 8 topics avec des configurations différentes
- Explorer le système de fichiers et identifier les segments
- Analyser le contenu des segments avec kafka-dump-log
- Comprendre les différentes politiques de rétention et leurs cas d'usage
- Expliquer la différence entre suppression et compaction
- Produire un tableau comparatif des configurations testées

### Déroulement
1. **10 min** - Création des topics et configuration
2. **15 min** - Production de messages et exploration filesystem
3. **15 min** - Analyse des segments avec kafka-dump-log
4. **15 min** - Test des politiques de rétention
5. **10 min** - Compaction et tombstones
6. **5 min** - Synthèse et comparaison

### Accès à l'exercice
📁 Dossier: `jour1/exercice2-topics-segments/`

Voir le README de l'exercice pour les instructions détaillées.

---

## Exercice 3 - Réplication et Résilience

**Durée:** 75 minutes

### Objectifs
- Comprendre le mécanisme de réplication dans Kafka
- Observer la distribution des replicas entre les brokers
- Identifier les rôles Leader et Follower
- Analyser les métadonnées d'un topic (ISR, Leader, Replicas)
- Tester la résilience du cluster en arrêtant des brokers
- Mettre une partition offline et comprendre les conditions

### Approche pédagogique
Cet exercice est **expérimental et interactif**. Les participants travaillent avec un cluster multi-brokers (3 nœuds) et simulent des pannes pour observer le comportement de Kafka en conditions dégradées. L'approche "chaos engineering" permet de comprendre les garanties et limites de la réplication.

### Notions approfondies
À travers cet exercice, les participants vont approfondir:
- **Architecture multi-brokers**: Cluster de 3 nœuds (2 controllers + 3 brokers)
- **Réplication**:
  - Replication Factor (RF)
  - Leader et Followers
  - Synchronisation des replicas
- **Haute disponibilité**:
  - ISR (In-Sync Replicas)
  - min.insync.replicas
  - Leader election
- **Résilience**:
  - Comportement avec 1 broker down
  - Comportement avec 2 brokers down
  - Conditions d'une partition offline
- **Durabilité**:
  - Impact de `acks=all`
  - Trade-off disponibilité vs durabilité

### Contenu de l'exercice
L'exercice est divisé en 6 parties:
1. **Création d'un topic avec réplication** - RF=3, 4 partitions, observer Leader/Replicas/ISR
2. **Production et réplication** - Produire des messages et vérifier la réplication physique
3. **Test de résilience: 1 broker down** - Arrêter kafka-3, observer le rebalancing
4. **Mettre une partition offline** - Arrêter 2 brokers simultanément pour perdre le quorum
5. **Expérimentations avancées** - Tester min.insync.replicas=3, controller down
6. **Synthèse et analyse** - Tableau récapitulatif des scénarios testés

### Livrables
Les participants doivent:
- Créer un topic avec RF=3 et analyser sa distribution
- Expliquer la différence entre Replicas et ISR
- Identifier le Leader de chaque partition
- Simuler des pannes et observer les élections de leader
- Mettre une partition complètement offline (Leader: none)
- Comprendre l'impact de min.insync.replicas sur la disponibilité
- Produire un tableau comparatif des scénarios de panne

### Déroulement
1. **15 min** - Création du topic et analyse des métadonnées
2. **15 min** - Production et vérification de la réplication
3. **15 min** - Test avec 1 broker down
4. **15 min** - Scénario partition offline (2 brokers down)
5. **10 min** - Expérimentations avancées (min.insync.replicas)
6. **5 min** - Synthèse et questions de compréhension

### Accès à l'exercice
📁 Dossier: `jour1/exercice3-replication-resilience/`

Voir le README de l'exercice pour les instructions détaillées.

---

## 📝 QCM - Validation des Connaissances

**Durée:** 10 minutes + 15 minutes de correction

Après avoir complété les 3 exercices, un QCM de 5 questions permet de valider la compréhension des concepts fondamentaux:
- Partitions
- Offsets
- Segments
- Politiques de rétention
- Log compaction

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

### Points clés à aborder après l'exercice 2

**Segments et Stockage**
- Les segments comme unités de stockage physique
- Fichiers .log, .index, .timeindex
- Segment actif vs segments fermés
- Impact du paramètre segment.bytes

**Politiques de Rétention**
- Rétention basée sur le temps (retention.ms)
  - Cas d'usage: logs d'application, événements temporaires
- Rétention basée sur la taille (retention.bytes)
  - Cas d'usage: limitation de l'espace disque
- Politique combinée (temps ET taille)
  - La première condition atteinte déclenche la suppression

**Log Compaction**
- Différence avec la suppression (delete)
- Rétention de la dernière valeur par clé
- Tombstone records (clé avec valeur null) pour supprimer
- Cas d'usage:
  - État utilisateur (user profiles)
  - Cache distribué
  - Change Data Capture (CDC)
  - Configuration management

**Comparaison Suppression vs Compaction**
| Aspect | Suppression (delete) | Compaction (compact) |
|--------|---------------------|----------------------|
| Politique | retention.ms / retention.bytes | cleanup.policy=compact |
| Conservation | Tout pendant la période | Dernière valeur par clé |
| Cas d'usage | Logs, événements temporaires | États, cache, CDC |
| Garanties | Fenêtre temporelle fixe | Toujours la dernière valeur |

### Points clés à aborder après l'exercice 3

**Réplication**
- Replication Factor (RF) - nombre de copies d'une partition
- Leader - broker responsable des lectures/écritures
- Followers - répliquent les données du leader
- Garantie de durabilité avec RF > 1

**ISR (In-Sync Replicas)**
- Liste des replicas synchronisés avec le leader
- Un follower sort des ISR s'il prend trop de retard
- Critique pour la haute disponibilité
- Nombre d'ISR >= min.insync.replicas pour accepter les écritures

**Leader Election**
- Automatique en cas de panne du leader actuel
- Nouveau leader choisi parmi les ISR
- Transparent pour les clients (reconnexion automatique)
- Impact sur la latence pendant l'élection (~quelques secondes)

**min.insync.replicas**
- Nombre minimum de replicas in-sync requis pour une écriture
- Utilisé avec `acks=all` pour garantir la durabilité
- Exemple: RF=3, min.insync.replicas=2 → tolère 1 broker down
- Trade-off: disponibilité vs durabilité

**Partition Offline**
- Se produit quand tous les ISR sont down
- Aucun leader disponible
- Les écritures et lectures échouent sur cette partition
- Récupération: redémarrer au moins un broker des ISR

**Controller**
- Broker spécial qui gère les métadonnées du cluster
- En mode KRaft: quorum de controllers (haute disponibilité)
- Responsable des élections de leader
- Gère l'ajout/suppression de brokers

**Cas d'usage réels**
| Scénario | RF | min.insync.replicas | Tolérance panne | Cas d'usage |
|----------|----|--------------------|-----------------|-------------|
| Dev/Test | 1 | 1 | Aucune | Environnement non-critique |
| Production Standard | 3 | 2 | 1 broker | Équilibre disponibilité/durabilité |
| Production Critique | 3 | 3 | Aucune (écriture) | Données critiques, aucune perte acceptable |
| Multi-DC | 5+ | 3 | 2 brokers | Distribution géographique |
