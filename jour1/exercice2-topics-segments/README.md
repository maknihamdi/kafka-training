# Exercice 2 - Topics et Segments Kafka

## 🎯 Objectifs
- Créer des topics avec différentes configurations
- Comprendre le concept de partitions
- Découvrir les segments et leur gestion par Kafka
- Explorer le système de fichiers du broker
- Observer comment Kafka stocke physiquement les données

## ⏱️ Durée
60 minutes

## 📚 Concepts Clés

### Topics et Partitions
- Un topic est divisé en partitions pour la scalabilité
- Chaque partition est un log ordonné et immuable
- Les partitions permettent le parallélisme

### Segments
- Chaque partition est divisée en segments (fichiers physiques)
- Un segment contient un ensemble de messages
- Kafka crée un nouveau segment quand le segment actif atteint sa taille limite
- Configuration: `segment.bytes` (par défaut: 1GB)

### Fichiers de segment
Pour chaque segment, Kafka crée 3 fichiers:
- `.log` - Les messages eux-mêmes
- `.index` - Index des offsets
- `.timeindex` - Index des timestamps

## 🚀 Instructions

### 1. Démarrer le Cluster

```bash
make start
```

Attendez 10-15 secondes que Kafka soit prêt.

### 2. Partie 1 - Topic Simple (Configuration par défaut)

#### Créer un topic avec la configuration par défaut

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-default 
```

#### Décrire le topic

```bash
docker exec kafka kafka-topics --describe \
  --bootstrap-server localhost:9092 \
  --topic topic-default
```

**Questions à se poser:**
- Quelle est la configuration du topic?
- Combien de partitions?

#### Explorer le système de fichiers

```bash
# Ouvrir un shell dans le container
make shell

# Dans le shell du container:
ls -lah /var/lib/kafka/data/

# Trouver le dossier du topic
ls -lah /var/lib/kafka/data/topic-default-0/

# Examiner les fichiers de segment
ls -lh /var/lib/kafka/data/topic-default-0/*.log
ls -lh /var/lib/kafka/data/topic-default-0/*.index
```

**Observations:**
- Voyez-vous des fichiers `.log`, `.index`, `.timeindex`?
- Quelle est leur taille initiale?

### 3. Partie 2 - Topic avec Plusieurs Partitions

#### Créer un topic avec 10 partitions

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-multipart \
  --partitions 10 
```

#### Explorer la structure

```bash
make shell

# Dans le shell:
ls -lah /var/lib/kafka/data/ | grep topic-multipart
```

**Questions:**
- Combien de dossiers voyez-vous pour ce topic?
- Comment sont-ils nommés?
- Que contient chaque dossier?

### 4. Partie 3 - Topic avec Segment Personnalisé (1MB)

Pour observer la création de segments, on va créer un topic avec des segments de petite taille.

#### Créer un topic avec segment.bytes = 500

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-small-segments \
  --partitions 2 \
  --config segment.bytes=500
```

#### Vérifier la configuration

```bash
docker exec kafka kafka-topics --describe \
  --bootstrap-server localhost:9092 \
  --topic topic-small-segments \
  --config segment.bytes
```

Ou plus complet:

```bash
docker exec kafka kafka-configs --describe \
  --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name topic-small-segments \
  --all
```

### 5. Partie 4 - Produire des Messages et Observer les Segments

#### Produire des messages via Kafka UI

1. Ouvrez Kafka UI: http://localhost:8080
2. Allez dans le topic `topic-small-segments`
3. Cliquez sur "Produce Message"
4. Produisez plusieurs messages (au moins 20-30)

**Exemple de message volumineux pour remplir rapidement:**

```json
{
  "id": 1,
  "data": "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
}
```

**Astuce:** Variez le `id` et dupliquez le texte pour avoir des messages de ~1KB chacun.

#### Observer la création de segments

Pendant et après la production de messages:

```bash
# Voir l'évolution des fichiers
docker exec kafka watch -n 1 "ls -lh /var/lib/kafka/data/topic-small-segments-0/*.log"
```

Ou plus simple:

```bash
docker exec kafka ls -lh /var/lib/kafka/data/topic-small-segments-0/
```

**Questions:**
- Après combien de messages voyez-vous un nouveau fichier `.log` apparaître?
- Quelle est la taille approximative des fichiers `.log`?
- Combien de fichiers `.log` avez-vous au total?

#### Examiner l'index

```bash
docker exec kafka kafka-dump-log \
  --files /var/lib/kafka/data/topic-small-segments-0/00000000000000000000.index \
  --print-data-log
```

**Questions:**
- À quoi sert cet index?
- Comment Kafka trouve-t-il rapidement un message par offset?

### 6. Partie 6 - Politiques de Rétention

Kafka offre plusieurs politiques pour gérer la rétention des données.

#### 6.1 Rétention par Temps (Time-based Retention)

Créer un topic avec rétention de 1 minute:

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-retention-time \
  --partitions 1 \
  --config segment.bytes=1000 \
  --config retention.ms=60000 \
  --config segment.ms=30000
```

**Tester la rétention:**

```bash
# Produire des messages
docker exec kafka bash -c "
for i in {1..50}; do
  echo 'Message '$i;
done | kafka-console-producer --bootstrap-server localhost:9092 --topic topic-retention-time
"

# Observer les segments
docker exec kafka ls -lh /var/lib/kafka/data/topic-retention-time-0/

# Attendre 2 minutes
sleep 120

# Observer à nouveau (les anciens segments devraient être supprimés)
docker exec kafka ls -lh /var/lib/kafka/data/topic-retention-time-0/
```

**Questions:**
- Les anciens segments ont-ils été supprimés après 1 minute?
- Combien de fichiers `.log` reste-t-il?
- Comment Kafka détermine-t-il quels segments supprimer?

#### 6.2 Rétention par Taille (Size-based Retention)

Créer un topic avec rétention limitée à 2KB par partition:

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-retention-size \
  --partitions 1 \
  --config segment.bytes=500 \
  --config retention.bytes=2000
```

**Tester la rétention:**

```bash
# Produire suffisamment de messages pour dépasser 2KB
docker exec kafka bash -c "
for i in {1..100}; do
  echo 'Message number '$i' with some padding to make it bigger';
done | kafka-console-producer --bootstrap-server localhost:9092 --topic topic-retention-size
"

# Observer les segments
docker exec kafka ls -lh /var/lib/kafka/data/topic-retention-size-0/

# Calculer la taille totale
docker exec kafka du -sh /var/lib/kafka/data/topic-retention-size-0/
```

**Questions:**
- Quelle est la taille totale des segments?
- Est-elle proche de 2KB (2000 bytes)?
- Que se passe-t-il quand on dépasse cette limite?

#### 6.3 Rétention Combinée (Temps ET Taille)

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-retention-combined \
  --partitions 1 \
  --config segment.bytes=500 \
  --config retention.ms=120000 \
  --config retention.bytes=3000
```

**La suppression se fait dès que l'une des conditions est atteinte!**

### 7. Partie 7 - Log Compaction

La compaction garde seulement le dernier message pour chaque clé.

#### 7.1 Créer un Topic Compacté

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --partitions 1 \
  --config cleanup.policy=compact \
  --config segment.bytes=500 \
  --config min.cleanable.dirty.ratio=0.01 \
  --config segment.ms=10000
```

**Paramètres de compaction:**
- `cleanup.policy=compact` - Active la compaction
- `min.cleanable.dirty.ratio=0.01` - Ratio minimal pour déclencher la compaction (1%)
- `segment.ms=10000` - Rotation de segment toutes les 10 secondes

#### 7.2 Tester la Compaction

```bash
# Produire des messages avec des clés (plusieurs fois la même clé)
docker exec kafka bash -c "
echo 'user1:Valeur initiale pour user1
user2:Valeur initiale pour user2
user3:Valeur initiale pour user3
user1:Mise à jour 1 pour user1
user2:Mise à jour 1 pour user2
user1:Mise à jour 2 pour user1
user1:Valeur FINALE pour user1
user2:Valeur FINALE pour user2
user3:Valeur FINALE pour user3' | kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --property 'parse.key=true' \
  --property 'key.separator=:'
"

# Consommer immédiatement - voir tous les messages
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --from-beginning \
  --property print.key=true \
  --property key.separator=: \
  --timeout-ms 5000
```

**Observer avant compaction:**

```bash
# Voir les segments
docker exec kafka ls -lh /var/lib/kafka/data/topic-compacted-0/

# Compter les messages dans le segment
docker exec kafka kafka-dump-log \
  --files /var/lib/kafka/data/topic-compacted-0/00000000000000000000.log \
  --print-data-log | grep -c "payload"
```

**Attendre la compaction (peut prendre quelques minutes):**

```bash
# Forcer la rotation des segments et attendre
sleep 30

# Déclencher manuellement la compaction (optionnel)
# Note: La compaction se fait automatiquement en arrière-plan

# Consommer à nouveau après compaction
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --from-beginning \
  --property print.key=true \
  --property key.separator=: \
  --timeout-ms 5000
```

**Questions:**
- Combien de messages voyez-vous pour chaque clé après compaction?
- Les anciennes valeurs sont-elles toujours présentes?
- Quelle est l'utilité de la compaction pour un système de cache distribué?

#### 7.3 Cas d'Usage de la Compaction

**Scénario: État des utilisateurs**

```bash
# Simuler des mises à jour d'état utilisateur
docker exec kafka bash -c "
echo 'user-123:{\"name\":\"Alice\",\"status\":\"active\",\"lastLogin\":\"2024-01-01\"}
user-456:{\"name\":\"Bob\",\"status\":\"active\",\"lastLogin\":\"2024-01-01\"}
user-123:{\"name\":\"Alice\",\"status\":\"active\",\"lastLogin\":\"2024-01-02\"}
user-789:{\"name\":\"Charlie\",\"status\":\"active\",\"lastLogin\":\"2024-01-02\"}
user-456:{\"name\":\"Bob\",\"status\":\"inactive\",\"lastLogin\":\"2024-01-03\"}
user-123:{\"name\":\"Alice\",\"status\":\"inactive\",\"lastLogin\":\"2024-01-04\"}' | \
kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --property 'parse.key=true' \
  --property 'key.separator=:'
"
```

**Après compaction, vous obtenez un "snapshot" de l'état actuel de chaque utilisateur!**

#### 7.4 Tombstone (Suppression de Clé)

Pour supprimer complètement une clé du topic compacté, envoyez une valeur `null`:

```bash
# Supprimer user-789
docker exec kafka bash -c "
echo 'user-789:' | kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --property 'parse.key=true' \
  --property 'key.separator=:' \
  --property 'null.marker='
"
```

Après compaction, `user-789` disparaîtra complètement du topic.

### 8. Exploration Bonus - Comparer les Topics

Créez un tableau comparatif de tous vos topics:

| Topic | Partitions | Segment Size | Retention Policy | Observations |
|-------|------------|--------------|------------------|--------------|
| topic-default | ... | ... | delete (default) | ... |
| topic-multipart | ... | ... | delete (default) | ... |
| topic-small-segments | ... | ... | delete (default) | ... |
| topic-retention-time | ... | ... | delete (1 min) | ... |
| topic-retention-size | ... | ... | delete (2KB) | ... |
| topic-compacted | ... | ... | compact | ... |

## 🔧 Commandes Utiles

### Gestion des Topics

```bash
# Lister tous les topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Supprimer un topic
docker exec kafka kafka-topics --delete \
  --bootstrap-server localhost:9092 \
  --topic <topic-name>

# Modifier la configuration d'un topic
docker exec kafka kafka-configs --alter \
  --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name <topic-name> \
  --add-config segment.bytes=2097152
```

### Exploration du Système de Fichiers

```bash
# Ouvrir un shell
make shell

# Navigation dans les dossiers
cd /var/lib/kafka/data/
ls -lah

# Voir la taille des dossiers
du -sh /var/lib/kafka/data/*

# Compter les fichiers dans une partition
ls /var/lib/kafka/data/topic-small-segments-0/*.log | wc -l

# Voir les dernières modifications
ls -lt /var/lib/kafka/data/topic-small-segments-0/
```

### Production de Messages en Masse

Pour générer rapidement beaucoup de messages:

```bash
# Produire 1000 messages
docker exec kafka bash -c "
for i in {1..1000}; do
  echo '{\"id\":'\$i',\"message\":\"Message number '\$i' with some padding text to increase size\"}';
done | kafka-console-producer --bootstrap-server localhost:9092 --topic topic-small-segments
"
```

### Paramètres Importants

- `segment.bytes` - Taille max d'un segment (défaut: 1GB)
- `segment.ms` - Temps max avant rotation (défaut: 7 jours)
- `retention.bytes` - Taille max de données à conserver par partition
- `retention.ms` - Durée de rétention des messages

### Pourquoi les Segments?

1. **Performance**: Lecture/écriture optimisée sur des fichiers de taille raisonnable
2. **Rétention**: Suppression facile des anciens segments
3. **Compaction**: Optimisation du stockage
4. **Réplication**: Transfert plus efficace

## 🐛 Troubleshooting

### Impossible de créer un topic

```bash
# Vérifier que Kafka est prêt
docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092
```

### Les segments ne se créent pas

- Vérifiez la taille des messages produits
- Assurez-vous que `segment.bytes` est assez petit
- Attendez quelques secondes après la production

### Cannot access /var/lib/kafka/data

```bash
# Vérifier les permissions
docker exec kafka ls -la /var/lib/kafka/
```

## 📝 Livrable

À la fin de cet exercice, vous devez être capable de:
- [ ] Créer des topics avec différentes configurations
- [ ] Expliquer ce qu'est un segment et son rôle
- [ ] Naviguer dans le système de fichiers du broker
- [ ] Identifier les fichiers .log, .index, .timeindex
- [ ] Comprendre quand et pourquoi Kafka crée de nouveaux segments
- [ ] Analyser le contenu d'un segment avec kafka-dump-log
- [ ] Configurer et comprendre les politiques de rétention (temps, taille, combinée)
- [ ] Expliquer le fonctionnement de la log compaction
- [ ] Identifier les cas d'usage appropriés pour la compaction vs suppression

## 📖 Pour Aller Plus Loin

### Compaction des Logs

Kafka peut aussi compacter les logs au lieu de les supprimer:

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic topic-compacted \
  --partitions 1 \
  --replication-factor 1 \
  --config cleanup.policy=compact \
  --config segment.bytes=1048576
```

### Monitoring des Segments

```bash
# Voir les métriques JMX
docker exec kafka kafka-run-class kafka.tools.JmxTool \
  --object-name kafka.log:type=Log,name=Size,topic=topic-small-segments,partition=0 \
  --attributes Value
```