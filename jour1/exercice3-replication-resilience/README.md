# Exercice 3 - Réplication et Résilience

## 🎯 Objectifs

- Comprendre le mécanisme de réplication dans Kafka
- Observer la distribution des replicas entre les brokers
- Identifier les rôles Leader et Follower
- Analyser les métadonnées d'un topic (ISR, Leader, Replicas)
- Tester la résilience du cluster en arrêtant des brokers
- Mettre une partition offline et comprendre les conditions

## 📋 Prérequis

- Docker et Docker Compose installés
- Ports disponibles: 8080, 9092, 9093, 9094
- Avoir complété les exercices 1 et 2

## 🏗️ Architecture du Cluster

Ce cluster Kafka est composé de **3 nœuds** en mode KRaft:

| Nœud | Rôle | Port | Container |
|------|------|------|-----------|
| kafka-1 | Broker + Controller | 9092 | kafka-1 |
| kafka-2 | Broker + Controller | 9093 | kafka-2 |
| kafka-3 | Broker seulement | 9094 | kafka-3 |

**Configuration importante:**
- **Replication Factor par défaut:** 3
- **Min In-Sync Replicas (min.insync.replicas):** 2
- **Controllers:** kafka-1 et kafka-2
- **Brokers:** kafka-1, kafka-2, kafka-3

---

## 🚀 Démarrage

### 1. Démarrer le cluster

```bash
make start
```

Attendez environ 30 secondes pour que tous les brokers soient prêts.

### 2. Vérifier le statut

```bash
make status
```

Vous devriez voir 3 containers en cours d'exécution.

### 3. Ouvrir Kafka UI

```bash
make ui
```

Ou ouvrez manuellement: http://localhost:8080

---

## 📝 Partie 1 - Création d'un Topic avec Réplication

### 1.1 Créer un topic avec RF=3

Ouvrez un shell dans kafka-1:

```bash
make shell-1
```

Créez un topic `replicated-topic` avec 4 partitions et un replication factor de 3:

```bash
kafka-topics --create \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic \
  --partitions 4 \
  --replication-factor 3
```

### 1.2 Examiner les métadonnées du topic

```bash
kafka-topics --describe \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic
```

**Question:** Pour chaque partition, identifiez:
- Le **Leader** (broker responsable des lectures/écritures)
- Les **Replicas** (tous les brokers qui ont une copie)
- Les **ISR** (In-Sync Replicas - replicas à jour)

**Exemple de sortie:**
```
Topic: replicated-topic	Partition: 0	Leader: 1	Replicas: 1,2,3	Isr: 1,2,3
Topic: replicated-topic	Partition: 1	Leader: 2	Replicas: 2,3,1	Isr: 2,3,1
Topic: replicated-topic	Partition: 2	Leader: 3	Replicas: 3,1,2	Isr: 3,1,2
Topic: replicated-topic	Partition: 3	Leader: 1	Replicas: 1,3,2	Isr: 1,3,2
```

### 1.3 Observer dans Kafka UI

Allez dans Kafka UI → Topics → replicated-topic

- Vérifiez les informations de réplication
- Observez la distribution des partitions
- Identifiez les leaders de chaque partition

**À noter:**
- Chaque partition a 3 replicas (sur les 3 brokers)
- Un seul broker est leader par partition
- Les ISR doivent contenir tous les replicas (1,2,3) si tout va bien

---

## 📬 Partie 2 - Production et Réplication

### 2.1 Produire des messages

Via Kafka UI:
1. Allez dans Topics → replicated-topic → Produce Message
2. Produisez 10 messages avec des clés différentes:
   - Key: `msg-1`, Value: `{"id": 1, "content": "Message 1"}`
   - Key: `msg-2`, Value: `{"id": 2, "content": "Message 2"}`
   - ... (jusqu'à msg-10)

**Ou via CLI:**

```bash
# Dans le shell de kafka-1
for i in {1..10}; do
  echo "msg-$i:{\"id\": $i, \"content\": \"Message $i\"}" | \
  kafka-console-producer \
    --bootstrap-server kafka-1:19092 \
    --topic replicated-topic \
    --property "parse.key=true" \
    --property "key.separator=:"
done
```

### 2.2 Observer la distribution des messages

```bash
kafka-console-consumer \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic \
  --from-beginning \
  --property print.key=true \
  --property print.partition=true \
  --property print.offset=true \
  --timeout-ms 5000
```

**Question:** Dans quelle(s) partition(s) vos messages sont-ils arrivés?

### 2.3 Vérifier la réplication sur les brokers

Pour chaque broker, vérifiez que les données sont bien répliquées:

**Sur kafka-1:**
```bash
ls -lah /var/lib/kafka/data/replicated-topic-*/
```

**Sur kafka-2 (ouvrez un nouveau terminal):**
```bash
make shell-2
ls -lah /var/lib/kafka/data/replicated-topic-*/
```

**Sur kafka-3:**
```bash
make shell-3
ls -lah /var/lib/kafka/data/replicated-topic-*/
```

**Observation:** Chaque broker devrait avoir des dossiers pour toutes les partitions (0, 1, 2, 3) car RF=3.

---

## 🔧 Partie 3 - Test de Résilience: Arrêter un Broker

### 3.1 État initial

Vérifiez l'état actuel du topic:

```bash
kafka-topics --describe \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic
```

Notez les ISR pour chaque partition (devrait être [1,2,3] ou une permutation).

### 3.2 Arrêter kafka-3 (broker-only)

Dans un nouveau terminal:

```bash
make stop-broker-3
```

### 3.3 Observer les changements

Attendez 5-10 secondes, puis vérifiez les métadonnées:

```bash
# Dans le shell de kafka-1
kafka-topics --describe \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic
```

**Question:** Que s'est-il passé?
- Les ISR contiennent-ils encore le broker 3?
- Les partitions dont le leader était 3 ont-elles changé de leader?
- Combien de replicas sont encore in-sync?

### 3.4 Vérifier dans Kafka UI

Rafraîchissez Kafka UI et observez:
- Les ISR ont été mis à jour (broker 3 retiré)
- Les leaders ont changé pour les partitions qui étaient sur broker 3
- Le topic est toujours disponible

### 3.5 Produire des messages avec un broker down

Produisez de nouveaux messages:

```bash
for i in {11..15}; do
  echo "msg-$i:{\"id\": $i, \"content\": \"Message $i with broker 3 down\"}" | \
  kafka-console-producer \
    --bootstrap-server kafka-1:19092 \
    --topic replicated-topic \
    --property "parse.key=true" \
    --property "key.separator=:"
done
```

**Observation:** Les messages sont toujours acceptés car il reste 2 brokers (ISR min = 2).

### 3.6 Redémarrer kafka-3

```bash
make start-broker-3
```

Attendez 10-15 secondes et vérifiez:

```bash
kafka-topics --describe \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic
```

**Question:** Le broker 3 est-il revenu dans les ISR?

---

## 💥 Partie 4 - Mettre une Partition Offline

L'objectif est de mettre une partition complètement offline (plus de leader disponible).

### 4.1 Comprendre les conditions

Pour qu'une partition devienne offline:
- Il faut perdre **tous les brokers dans les ISR**
- Ou perdre trop de brokers pour respecter `min.insync.replicas`

### 4.2 Scénario: Arrêter 2 brokers

**Étape 1:** Identifier le leader d'une partition (exemple: partition 0)

```bash
kafka-topics --describe \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic
```

Supposons que la partition 0 a:
- Leader: 1
- Replicas: 1,2,3
- ISR: 1,2,3

**Étape 2:** Arrêter les brokers 1 et 2 (ceux qui ont la partition 0)

```bash
# Dans un terminal
make stop-broker-1

# Dans un autre terminal
make stop-broker-2
```

**Étape 3:** Tenter de décrire le topic

```bash
# Depuis kafka-3 (le seul encore actif)
make shell-3

kafka-topics --describe \
  --bootstrap-server kafka-3:39092 \
  --topic replicated-topic
```

### 4.3 Observer les partitions offline

**Attendu:**
- La partition 0 (et potentiellement d'autres) n'a plus de leader
- Le champ `Leader` affichera `-1` ou `none`
- Les ISR seront vides ou incomplets

**Exemple:**
```
Topic: replicated-topic	Partition: 0	Leader: none	Replicas: 1,2,3	Isr: 3
```

### 4.4 Tenter de produire sur une partition offline

Essayez de produire un message:

```bash
echo "msg-fail:{\"id\": 99, \"content\": \"This should fail\"}" | \
kafka-console-producer \
  --bootstrap-server kafka-3:39092 \
  --topic replicated-topic \
  --property "parse.key=true" \
  --property "key.separator=:"
```

**Question:** Que se passe-t-il?
- Le producer peut-il écrire?
- Quel message d'erreur obtenez-vous?

**Note:** Selon la clé, le message peut aller sur une partition différente qui est encore online.

### 4.5 Récupération du cluster

Redémarrez les brokers:

```bash
make start-broker-1
make start-broker-2
```

Attendez 15-20 secondes et vérifiez:

```bash
make shell-1

kafka-topics --describe \
  --bootstrap-server kafka-1:19092 \
  --topic replicated-topic
```

**Observation:** Les partitions devraient retrouver leurs leaders et les ISR devraient se synchroniser.

---

## 🧪 Partie 5 - Expérimentations Avancées

### 5.1 Tester min.insync.replicas

Créez un topic avec `min.insync.replicas=3`:

```bash
kafka-topics --create \
  --bootstrap-server kafka-1:19092 \
  --topic strict-topic \
  --partitions 2 \
  --replication-factor 3 \
  --config min.insync.replicas=3
```

**Arrêtez 1 broker:**

```bash
make stop-broker-3
```

**Tentez de produire avec acks=all:**

```bash
echo "test:value" | \
kafka-console-producer \
  --bootstrap-server kafka-1:19092 \
  --topic strict-topic \
  --request-required-acks all \
  --property "parse.key=true" \
  --property "key.separator=:"
```

**Question:** Que se passe-t-il? Pourquoi?

**Réponse:** La production échoue car il ne reste que 2 ISR, mais `min.insync.replicas=3` exige 3 replicas synchronisés.

### 5.2 Observer le comportement d'un controller down

**Arrêtez kafka-1 (qui est un controller):**

```bash
make stop-broker-1
```

**Vérifiez:**
- Le cluster fonctionne-t-il toujours?
- Kafka-2 (l'autre controller) prend-il le relais?

---

## 📊 Partie 6 - Synthèse et Analyse

### 6.1 Tableau récapitulatif

Complétez ce tableau avec vos observations:

| Scénario | Brokers actifs | Partitions online | Production possible? | Observations |
|----------|----------------|-------------------|---------------------|--------------|
| Cluster normal | 3 | Toutes (4) | Oui | ISR complets |
| 1 broker down (kafka-3) | 2 | Toutes (4) | Oui | ISR réduits à [1,2] |
| 2 brokers down (kafka-1,2) | 1 | Certaines offline | Partiel | Partitions avec leader sur broker 3 OK |
| min.insync.replicas=3 + 1 broker down | 2 | Toutes (4) | Non | ISR < min.insync.replicas |

### 6.2 Questions de compréhension

1. **Qu'est-ce qu'un replica?**
2. **Quelle est la différence entre Replicas et ISR?**
3. **Quel est le rôle du Leader?**
4. **Que signifie min.insync.replicas=2?**
5. **Dans quelles conditions une partition devient-elle offline?**
6. **Quel est l'impact de la valeur de `acks` (0, 1, all) sur la durabilité?**

---

## 🎯 Livrables

À la fin de cet exercice, vous devez être capable de:

- [ ] Expliquer le mécanisme de réplication dans Kafka
- [ ] Identifier les Leader et Followers d'une partition
- [ ] Comprendre le rôle des ISR (In-Sync Replicas)
- [ ] Analyser les métadonnées d'un topic avec `kafka-topics --describe`
- [ ] Prédire l'impact de l'arrêt d'un ou plusieurs brokers
- [ ] Mettre une partition offline et comprendre comment la récupérer
- [ ] Configurer `min.insync.replicas` et comprendre son impact
- [ ] Compléter le tableau récapitulatif avec vos observations

---

## 🧹 Nettoyage

### Arrêter le cluster

```bash
make stop
```

### Nettoyer complètement (supprimer les volumes)

```bash
make clean
```

---

## 📚 Concepts Clés

### Replication Factor (RF)
- Nombre de copies d'une partition
- RF=3 signifie que chaque partition existe sur 3 brokers différents
- Assure la durabilité des données

### Leader
- Un seul broker par partition est le leader
- Toutes les lectures et écritures passent par le leader
- Les followers répliquent les données du leader

### ISR (In-Sync Replicas)
- Liste des replicas qui sont à jour avec le leader
- Un follower est in-sync s'il a répliqué tous les messages récents
- Si un follower prend du retard, il est retiré des ISR

### min.insync.replicas
- Nombre minimum de replicas qui doivent être in-sync pour accepter une écriture
- Utilisé avec `acks=all` pour garantir la durabilité
- Exemple: min.insync.replicas=2 avec RF=3 tolère 1 broker down

### Partition Offline
- Une partition est offline quand elle n'a plus de leader disponible
- Se produit quand tous les brokers des ISR sont down
- Les écritures et lectures sur cette partition échouent

---

## 🔍 Pour Aller Plus Loin

### Unclean Leader Election

Kafka peut être configuré pour permettre une "unclean leader election":

```bash
kafka-configs --alter \
  --bootstrap-server localhost:9092 \
  --entity-type topics \
  --entity-name replicated-topic \
  --add-config unclean.leader.election.enable=true
```

**Danger:** Permet à un replica hors-sync de devenir leader, risque de perte de données.

### Réassignation de Partitions

Pour rééquilibrer manuellement les partitions entre brokers, utilisez `kafka-reassign-partitions`.

### Monitoring

En production, surveillez:
- Le nombre d'ISR par partition (alerter si < RF)
- Les partitions sans leader
- Le lag de réplication
- La santé des controllers

---

**Bonne exploration de la réplication Kafka! 🚀**