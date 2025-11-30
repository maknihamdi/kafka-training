# Exercice 1 - Explorer le Cluster Kafka

## 🎯 Objectifs
- Démarrer un cluster Kafka en mode KRaft
- Explorer l'interface Kafka UI
- Découvrir par vous-même les concepts fondamentaux de Kafka
- Identifier les notions clés à travers l'observation

## ⏱️ Durée
45 minutes

## 🚀 Instructions

### 1. Démarrer le Cluster

Depuis le dossier de l'exercice, exécutez:

```bash
make start
```

Cette commande va:
1. Démarrer Kafka en mode KRaft
2. Démarrer Kafka UI et Schema Registry
3. Créer automatiquement des topics de démonstration
4. Insérer automatiquement des données d'exemple

Pour ouvrir Kafka UI directement dans le navigateur:
```bash
make ui
```

### 2. Explorer Kafka UI

Ouvrez votre navigateur sur: **http://localhost:8080**

Vous allez découvrir plusieurs sections dans l'interface. Explorez librement et observez!

#### 🔍 Zones à explorer:

**Dashboard / Vue d'ensemble**
- Que voyez-vous sur la page d'accueil?
- Combien de composants différents sont affichés?

**Brokers**
- Naviguez dans la section "Brokers"
- Qu'est-ce qu'un broker?
- Quelles informations sont affichées?

**Topics**
- Naviguez dans la section "Topics"
- Combien de topics sont créés?
- Cliquez sur différents topics et observez les différences

**Messages dans un Topic**
- Choisissez un topic (par exemple `orders`)
- Allez dans l'onglet "Messages"
- Cliquez sur "Consume from beginning"
- Examinez attentivement la structure des messages affichés

**Schema Registry**
- Naviguez dans la section "Schema Registry"
- Que contient-il?
- Quel topic a un schéma enregistré?

### 3. Mission d'Exploration

**Votre mission:** Identifier et lister les **notions/concepts Kafka** que vous découvrez en explorant l'interface.

Pour chaque notion identifiée, notez:
- **Le nom** de la notion (ex: "Partition", "Offset", etc.)
- **Où vous l'avez vue** (dans quelle section de l'UI)
- **Ce que vous pensez que c'est** (votre hypothèse)

### 📝 Guide d'exploration

Voici des questions pour guider votre exploration (ne cherchez pas les réponses, explorez!):

#### Sur les Topics

Comparez les différents topics créés:
- `events` (3 partitions)
- `users` (2 partitions)
- `orders` (4 partitions)
- `logs` (1 partition)
- `images` (2 partitions)
- `transactions` (2 partitions)
- `products` (2 partitions)

Questions à vous poser:
- Qu'est-ce qui différencie ces topics?
- Que signifient les chiffres entre parenthèses?
- Y a-t-il d'autres informations affichées pour chaque topic?

#### Sur les Messages

Explorez les messages dans différents topics:

**Topic `orders`:**
- Comment les messages sont-ils structurés?
- Voyez-vous des colonnes "Key", "Value", "Partition", "Offset"?
- Que représente chacune de ces colonnes selon vous?

**Topic `transactions`:**
- Qu'est-ce qui est différent par rapport à `orders`?
- Y a-t-il des informations supplémentaires?

**Topic `images`:**
- Comment les données sont-elles affichées?
- Sont-elles lisibles? Pourquoi?

**Topic `products`:**
- Y a-t-il une indication de schéma?
- Regardez dans Schema Registry, que voyez-vous?

#### Sur le Partitionnement

Dans un topic avec plusieurs partitions (ex: `orders`):
- Les messages sont-ils répartis de manière uniforme?
- Certains messages ont-ils la même partition? Pourquoi selon vous?
- Quel est le lien entre la "clé" (Key) et la partition?

#### Sur les Offsets

Regardez la colonne "Offset":
- Comment évolue cet offset?
- Est-il unique par topic ou par partition?
- Que se passe-t-il quand vous consommez les messages "from beginning"?

### 📋 Livrable

À la fin de cet exercice, préparez une **liste de 10-15 notions/concepts** que vous avez identifiés.

**Format suggéré:**

| Notion | Où trouvée | Mon hypothèse |
|--------|------------|---------------|
| Broker | Section Brokers | Serveur qui stocke les données? |
| Partition | ... | ... |
| ... | ... | ... |

**Exemples de notions à chercher:**
- Cluster
- Broker
- Topic
- Partition
- Message
- Key (Clé)
- Value (Valeur)
- Header
- Offset
- Replication Factor
- Schema Registry
- Consumer Group
- Timestamp

## 🔧 Commandes Utiles (Optionnel)

Si vous voulez aller plus loin avec la ligne de commande:

### Voir le statut du cluster
```bash
make status
```

### Lister tous les topics
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
```

### Décrire un topic
```bash
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic orders
```

### Consommer des messages avec détails
```bash
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic orders \
  --from-beginning \
  --property print.key=true \
  --property print.partition=true \
  --property print.offset=true \
  --property print.timestamp=true
```

### Arrêter le cluster
```bash
make stop
```

## 🎓 Expérimentation Libre (Bonus)

Si vous avez le temps, essayez de:

1. **Créer votre propre topic**
```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic mon-topic \
  --partitions 2 \
  --replication-factor 1
```

2. **Envoyer des messages**
```bash
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic mon-topic
```

3. **Les consommer**
```bash
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic mon-topic \
  --from-beginning
```

## 🐛 Troubleshooting

### Le cluster ne démarre pas
```bash
make clean
make start
```

### Kafka UI ne charge pas
- Attendez 30 secondes après le démarrage
- Vérifiez les logs: `make logs`
- Vérifiez le statut: `make status`

### Pas de topics visibles
- Vérifiez les logs du provisioning: `make logs-init`
- Redémarrez le cluster: `make restart`

## 📝 Notes pour le Formateur

**Déroulement suggéré:**
1. Laisser 30 minutes d'exploration libre
2. Recueillir les notions identifiées par chaque participant
3. Créer une liste consolidée au tableau
4. Expliquer chaque notion identifiée
5. Compléter avec les notions manquantes

**Notions clés à couvrir:**
- Architecture: Cluster, Broker, KRaft
- Organisation: Topic, Partition, Replication Factor
- Données: Message, Key, Value, Header, Timestamp
- Position: Offset
- Schéma: Schema Registry, JSON Schema
- Consommation: Consumer Group

**Points pédagogiques:**
- L'apprentissage par découverte favorise la mémorisation
- Les participants posent de meilleures questions quand ils explorent d'abord
- Utiliser leurs observations comme base pour les explications théoriques