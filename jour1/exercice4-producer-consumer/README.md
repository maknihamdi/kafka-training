# Exercice 4 - Producer et Consumer Spring Boot

## 🎯 Objectifs

- Créer un Producer Kafka avec Spring Boot
- Créer un Consumer Kafka standalone (sans consumer group)
- Créer un Consumer Kafka scalable (avec consumer group)
- Produire des messages via une API REST
- Comprendre la différence entre consommation standalone et avec consumer group
- Observer le partitionnement avec des clés
- Tester la consommation parallèle avec plusieurs instances

## 📋 Prérequis

- Docker et Docker Compose installés
- Java 17+ installé
- Maven 3.6+ installé
- IntelliJ IDEA (recommandé) ou tout autre IDE Java
- Ports disponibles: 8081, 9092, 8080 (Kafka UI)

## 🏗️ Architecture

```
┌─────────────────┐         ┌─────────────┐         ┌─────────────────┐
│   REST Client   │────────>│   Producer  │────────>│  Kafka Cluster  │
│  (Postman/curl) │         │  (port 8081)│         │  (port 9092)    │
└─────────────────┘         └─────────────┘         └────────┬────────┘
                                                              │
                                                              │
                                    ┌─────────────────────────┴─────────────────────────┐
                                    │                                                   │
                                    v                                                   v
                            ┌─────────────────┐                            ┌──────────────────────┐
                            │    Consumer     │                            │  Scalable Consumer   │
                            │   (standalone)  │                            │  (consumer group)    │
                            │ Toutes partitions│                           │  Instance 1 & 2      │
                            └─────────────────┘                            └──────────────────────┘
```

**Modules:**
- `producer` - Produit des messages via API REST
- `consumer` - Consumer standalone sans consumer group (assign manuel)
- `scalable-consumer` - Consumer avec consumer group (@KafkaListener)

**Topics:**
- `messages` - 4 partitions

---

## 🚀 Partie 1 - Démarrage et Configuration

### 1.1 Démarrer l'infrastructure Kafka

```bash
cd jour1/exercice4-producer-consumer
docker-compose up -d
```

Vérifiez que Kafka est démarré:

```bash
docker ps
```

Vous devriez voir les conteneurs `kafka` et `kafka-ui`.

### 1.2 Créer le topic

```bash
docker exec kafka kafka-topics --create \
  --bootstrap-server localhost:9092 \
  --topic messages \
  --partitions 4 \
  --replication-factor 1
```

Vérifiez:

```bash
docker exec kafka kafka-topics --describe \
  --bootstrap-server localhost:9092 \
  --topic messages
```

### 1.3 Compiler les modules

Depuis la racine de l'exercice 4:

```bash
mvn clean compile
```

Cela compile les 3 modules: `producer`, `consumer`, et `scalable-consumer`.

---

## 📤 Partie 2 - Producer (API REST)

### 2.1 Structure du Producer

Le module `producer` contient:
- **ProducerApplication.java** - Point d'entrée Spring Boot
- **KafkaProducerConfig.java** - Configuration du producer (sérialisation JSON)
- **MessageProducerService.java** - Service pour envoyer des messages
- **MessageController.java** - API REST (port 8081)
- **Message.java** - Modèle de données

### 2.2 Lancer le Producer

**Via Maven:**
```bash
cd producer
mvn spring-boot:run
```

**Via IntelliJ:**
1. Ouvrez `producer/src/main/java/com/kafka/training/producer/ProducerApplication.java`
2. Clic droit → Run 'ProducerApplication'

Le producer démarre sur le port **8081**.

### 2.3 Tester l'envoi de messages

**Envoyer un message simple:**

```bash
curl -X POST http://localhost:8081/api/messages/send \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "messages",
    "key": "user-123",
    "content": "Hello Kafka!",
    "sender": "Alice"
  }'
```

**Envoyer un batch de messages:**

```bash
curl -X POST http://localhost:8081/api/messages/send-batch \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "messages",
    "count": 20,
    "sender": "BatchSender"
  }'
```

### 2.4 Observer le partitionnement

**Messages avec la même clé:**

```bash
for i in {1..5}; do
  curl -s -X POST http://localhost:8081/api/messages/send \
    -H "Content-Type: application/json" \
    -d "{
      \"topic\": \"messages\",
      \"key\": \"user-123\",
      \"content\": \"Message $i\",
      \"sender\": \"Alice\"
    }"
done
```

**Question:** Tous les messages vont-ils dans la même partition?
**Réponse:** Oui, car ils ont la même clé.

**Messages avec des clés différentes:**

```bash
for i in {1..10}; do
  curl -s -X POST http://localhost:8081/api/messages/send \
    -H "Content-Type: application/json" \
    -d "{
      \"topic\": \"messages\",
      \"key\": \"user-$i\",
      \"content\": \"Message from user-$i\",
      \"sender\": \"User$i\"
    }"
done
```

**Question:** Comment sont distribués les messages?
**Réponse:** Distribution basée sur le hash de la clé entre les 4 partitions.

---

## 📥 Partie 3 - Consumer Standalone (sans consumer group)

### 3.1 Structure du Consumer Standalone

Le module `consumer` contient:
- **ConsumerApplication.java** - Point d'entrée Spring Boot
- **KafkaConsumerConfig.java** - Configuration avec KafkaConsumer bean
- **MessageConsumerService.java** - Consommation via assign() (pas subscribe())
- **Message.java** - Modèle de données

### 3.2 Caractéristiques du Consumer Standalone

- ✅ Utilise `consumer.assign()` pour assigner manuellement les partitions
- ✅ **Pas de consumer group** (pas de group.id)
- ✅ Lit **toutes les partitions** du topic
- ✅ Loop infini avec `consumer.poll()`
- ✅ Pas de rebalancing
- ❌ Impossible de faire du load balancing avec d'autres instances

### 3.3 Lancer le Consumer Standalone

**Ouvrez un nouveau terminal** (le producer doit rester actif).

**Via Maven:**
```bash
cd consumer
mvn spring-boot:run
```

**Via IntelliJ:**
1. Ouvrez `consumer/src/main/java/com/kafka/training/consumer/ConsumerApplication.java`
2. Clic droit → Run 'ConsumerApplication'

### 3.4 Observer la consommation

Le consumer affiche dans les logs:

```
╔════════════════════════════════════════════════════════════════╗
║ 📩 Message reçu - Standalone Mode                             ║
╠════════════════════════════════════════════════════════════════╣
║ Topic        : messages
║ Partition    : 2
║ Offset       : 5
║ Key          : user-123
╠════════════════════════════════════════════════════════════════╣
║ Message      : Message{id='...', content='Hello Kafka!', ...}
╚════════════════════════════════════════════════════════════════╝
```

### 3.5 Configuration dynamique des partitions

Dans `application.yml`:

```yaml
kafka:
  consumer:
    topic: messages
    partitions: 4  # Nombre de partitions à consommer
```

Le consumer construit automatiquement la liste `[0, 1, 2, 3]` et les assigne toutes.

### 3.6 Cas d'usage du Consumer Standalone

- **Traitement batch** - Un seul processus qui lit tout le topic
- **Export de données** - Dumper tout le topic vers un fichier
- **Monitoring** - Observer tous les messages sans les marquer comme consommés
- **Développement/Debug** - Simplicité de configuration

---

## 🚀 Partie 4 - Scalable Consumer (avec consumer group)

### 4.1 Structure du Scalable Consumer

Le module `scalable-consumer` contient:
- **ScalableConsumerApplication.java** - Point d'entrée Spring Boot
- **KafkaConsumerConfig.java** - Configuration avec ConsumerFactory et consumer group
- **MessageConsumerService.java** - Consommation via @KafkaListener
- **Message.java** - Modèle de données
- **application.yml** - Configuration par défaut (instance 1)
- **application-instance2.yml** - Configuration pour instance 2

### 4.2 Caractéristiques du Scalable Consumer

- ✅ Utilise `@KafkaListener` avec un `groupId`
- ✅ **Consumer group** activé
- ✅ **Rebalancing automatique** des partitions
- ✅ Chaque message consommé par **une seule instance** du groupe
- ✅ **Scalabilité horizontale** - ajoutez plus d'instances pour augmenter le débit
- ✅ **Tolérance aux pannes** - si une instance tombe, les autres récupèrent ses partitions

### 4.3 Lancer la première instance

**Terminal 1:**

```bash
cd scalable-consumer
mvn spring-boot:run
```

**Via IntelliJ:**
1. Ouvrez `scalable-consumer/src/main/java/com/kafka/training/scalableconsumer/ScalableConsumerApplication.java`
2. Clic droit → Run 'ScalableConsumerApplication'

**Observer les logs:**

```
Consumer group: message-consumer-group
Assigned partitions: [messages-0, messages-1, messages-2, messages-3]
```

L'instance 1 consomme **les 4 partitions**.

### 4.4 Lancer la deuxième instance

**Terminal 2:**

```bash
cd scalable-consumer
mvn spring-boot:run -Dspring-boot.run.profiles=instance2
```

**Via IntelliJ:**
1. Clic droit sur `ScalableConsumerApplication`
2. Modify Run Configuration → Duplicate
3. Dans "Program arguments", ajoutez: `--spring.profiles.active=instance2`
4. Cochez "Allow multiple instances"
5. Run

**Observer les logs:**

**Instance 1:**
```
Consumer rebalancing...
New assigned partitions: [messages-0, messages-1]
```

**Instance 2:**
```
Consumer group: message-consumer-group
Assigned partitions: [messages-2, messages-3]
```

Les 4 partitions sont maintenant **réparties entre les 2 instances**!

### 4.5 Tester la consommation parallèle

**Envoyez un batch de 100 messages:**

```bash
curl -X POST http://localhost:8081/api/messages/send-batch \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "messages",
    "count": 100,
    "sender": "ParallelTest"
  }'
```

**Observer:**
- Instance 1 consomme les messages des partitions 0 et 1
- Instance 2 consomme les messages des partitions 2 et 3
- Les deux instances travaillent **en parallèle**
- Chaque message est consommé par **une seule instance**

### 4.6 Tester la résilience

**Arrêtez l'instance 2** (Ctrl+C dans le terminal 2).

**Observer dans les logs de l'instance 1:**

```
Consumer rebalancing...
New assigned partitions: [messages-0, messages-1, messages-2, messages-3]
```

L'instance 1 a **automatiquement récupéré** les partitions 2 et 3!

**Envoyez de nouveaux messages:**

```bash
curl -X POST http://localhost:8081/api/messages/send-batch \
  -H "Content-Type: application/json" \
  -d '{
    "topic": "messages",
    "count": 50,
    "sender": "ResilienceTest"
  }'
```

Tous les messages sont maintenant traités par l'instance 1.

### 4.7 Relancer l'instance 2

**Redémarrez l'instance 2:**

```bash
cd scalable-consumer
mvn spring-boot:run -Dspring-boot.run.profiles=instance2
```

**Observer:** Un nouveau rebalancing se produit, les partitions sont à nouveau réparties 2-2.

---

## 📊 Partie 5 - Analyse et Comparaison

### 5.1 Comparaison Consumer Standalone vs Scalable

| Aspect | Consumer Standalone | Scalable Consumer |
|--------|---------------------|-------------------|
| **API Kafka** | `consumer.assign()` | `consumer.subscribe()` |
| **Spring** | KafkaConsumer bean + Thread | @KafkaListener |
| **Consumer Group** | ❌ Pas de group.id | ✅ group.id requis |
| **Partitions** | Toutes assignées manuellement | Distribution automatique |
| **Rebalancing** | ❌ Aucun | ✅ Automatique |
| **Scalabilité** | ❌ Une seule instance | ✅ Multiples instances |
| **Tolérance pannes** | ❌ Aucune | ✅ Rebalancing automatique |
| **Cas d'usage** | Batch, export, monitoring | Production, haute disponibilité |

### 5.2 Vérifier le consumer group

```bash
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group message-consumer-group
```

**Sortie attendue (2 instances actives):**

```
GROUP                   TOPIC      PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG  CONSUMER-ID                                     HOST
message-consumer-group  messages   0          50              50              0    consumer-1-uuid                                  /172.18.0.1
message-consumer-group  messages   1          48              48              0    consumer-1-uuid                                  /172.18.0.1
message-consumer-group  messages   2          52              52              0    consumer-2-uuid                                  /172.18.0.1
message-consumer-group  messages   3          50              50              0    consumer-2-uuid                                  /172.18.0.1
```

**Observation:**
- LAG = 0 → tous les messages ont été consommés
- Chaque consumer-id est responsable de 2 partitions
- HOST montre l'adresse IP du consumer

### 5.3 Observer dans Kafka UI

Ouvrez http://localhost:8080:

1. **Topics** → `messages`:
   - Voir les messages produits
   - Observer la distribution par partition

2. **Consumer Groups** → `message-consumer-group`:
   - Voir les members du groupe
   - Voir les partitions assignées à chaque member
   - Observer les offsets et le lag

---

## 🎯 Livrables

À la fin de cet exercice, vous devez être capable de:

### Producer
- [ ] Créer un Producer Spring Boot avec KafkaTemplate
- [ ] Configurer la sérialisation JSON
- [ ] Produire des messages via une API REST
- [ ] Comprendre le rôle de la clé dans le partitionnement
- [ ] Envoyer des messages avec et sans clé

### Consumer Standalone
- [ ] Créer un consumer sans consumer group
- [ ] Utiliser `consumer.assign()` pour assigner manuellement les partitions
- [ ] Configurer la désérialisation JSON avec trusted packages
- [ ] Construire dynamiquement la liste des partitions
- [ ] Comprendre les cas d'usage du mode standalone

### Scalable Consumer
- [ ] Créer un consumer avec consumer group
- [ ] Utiliser @KafkaListener avec groupId
- [ ] Lancer plusieurs instances en parallèle
- [ ] Observer le rebalancing automatique des partitions
- [ ] Tester la tolérance aux pannes
- [ ] Analyser les consumer groups et les offsets

---

## 🧹 Nettoyage

### Arrêter les applications

1. Arrêtez le producer (Ctrl+C)
2. Arrêtez le consumer standalone (Ctrl+C)
3. Arrêtez les scalable consumers (Ctrl+C dans chaque terminal)

### Arrêter Kafka

```bash
docker-compose down
```

### Nettoyage complet (avec suppression des volumes)

```bash
docker-compose down -v
```

---

## 📚 Concepts Clés

### Producer

- **KafkaTemplate**: Abstraction Spring pour envoyer des messages
- **JsonSerializer**: Convertit les objets Java en JSON
- **Clé de partitionnement**: Hash(clé) % nombre_partitions
- **MAX_BLOCK_MS_CONFIG**: Timeout pour attendre les métadonnées (évite les boucles infinies)
- **Callback**: Détecte les succès/échecs d'envoi

### Consumer Standalone

- **KafkaConsumer**: API Kafka native
- **assign()**: Assignation manuelle des partitions (pas de consumer group)
- **poll()**: Récupère les messages par batch
- **seekToBeginning()**: Lit depuis le début du topic
- **Pas de rebalancing**: Toutes les partitions assignées à l'instance unique

### Scalable Consumer

- **@KafkaListener**: Annotation Spring pour écouter un topic
- **groupId**: Identifiant du consumer group
- **ConsumerFactory**: Factory Spring pour créer des consumers
- **Rebalancing**: Redistribution automatique des partitions entre les membres du groupe
- **ISR (In-Sync Replicas)**: Garantit qu'aucun message n'est perdu

### Configuration Commune

- **bootstrap-servers**: Adresse du cluster Kafka
- **JsonDeserializer.TRUSTED_PACKAGES**: Sécurité pour la désérialisation
- **JsonDeserializer.USE_TYPE_INFO_HEADERS**: Ignore les headers de type du producer
- **auto-offset-reset**: earliest (depuis le début) ou latest (nouveaux messages)
- **enable-auto-commit**: Commit automatique des offsets (true par défaut)

---

## 🔍 Pour Aller Plus Loin

### 1. Manual Offset Management

Désactivez `enable-auto-commit` et gérez manuellement les offsets:

```java
@KafkaListener(...)
public void consume(ConsumerRecord<String, Message> record, Acknowledgment ack) {
    try {
        processMessage(record.value());
        ack.acknowledge(); // Commit manuel
    } catch (Exception e) {
        // Gestion d'erreur - ne pas commit
    }
}
```

### 2. Error Handling

Ajoutez un error handler pour gérer les erreurs de désérialisation:

```java
@Bean
public DefaultErrorHandler errorHandler() {
    return new DefaultErrorHandler(new FixedBackOff(1000L, 3));
}
```

### 3. Concurrency

Augmentez le nombre de threads par consumer:

```java
factory.setConcurrency(3); // 3 threads par instance
```

Chaque thread peut consommer une partition en parallèle.

### 4. Custom Partitioner

Créez votre propre stratégie de partitionnement:

```java
public class CustomPartitioner implements Partitioner {
    @Override
    public int partition(String topic, Object key, byte[] keyBytes,
                        Object value, byte[] valueBytes, Cluster cluster) {
        // Logique personnalisée
        return Math.abs(key.hashCode()) % numPartitions;
    }
}
```

### 5. Consumer avec seek()

Lire depuis un offset spécifique:

```java
consumer.seek(new TopicPartition("messages", 0), 100);
```

---

## 🐛 Troubleshooting

### Erreur: "The class 'X' is not in the trusted packages"

**Solution:**
```java
deserializer.addTrustedPackages("*");
deserializer.setUseTypeHeaders(false);
```

### Erreur: "No group.id found in consumer config"

**Solution:** Ajoutez `groupId` dans @KafkaListener ou utilisez `assign()` au lieu de `subscribe()`.

### Les messages ne sont pas consommés

1. Vérifiez que le topic existe
2. Vérifiez que le producer a bien envoyé les messages
3. Vérifiez les logs du consumer pour les erreurs
4. Vérifiez le lag du consumer group

### Rebalancing infini

**Cause:** Le traitement des messages prend trop de temps.

**Solution:** Augmentez `max.poll.interval.ms`:

```yaml
spring:
  kafka:
    consumer:
      properties:
        max.poll.interval.ms: 600000  # 10 minutes
```

---

**Bravo! Vous maîtrisez maintenant les bases du Producer et Consumer Spring Boot avec Kafka! 🚀**
