# Formation Kafka - 3 Jours

## 📚 À propos

Ce repository contient l'ensemble des exercices et ressources pour une **formation Kafka complète sur 3 jours**. La formation couvre les fondamentaux de Kafka jusqu'aux notions avancées, avec une approche pratique basée sur des exercices concrets.

## 🎯 Objectifs de la formation

- Maîtriser les concepts fondamentaux de Kafka (brokers, topics, partitions, offsets)
- Développer des applications producers et consumers avec Spring Boot et Java
- Comprendre et appliquer les patterns Kafka
- Implémenter des solutions avancées (Kafka Streams, transactions, etc.)
- Déployer et opérer Kafka en production

## 🗓️ Programme

### [Jour 1 - Fondamentaux de Kafka](./jour1/README.md)

**Objectifs:**
- Découvrir l'architecture Kafka (cluster, brokers, topics, partitions)
- Comprendre les concepts fondamentaux par l'exploration
- Identifier les notions clés de Kafka
- Observer le partitionnement et les offsets

**Contenu:**
- Exercice 1: Explorer le Cluster Kafka (approche par découverte)
- Utilisation de Kafka UI pour la visualisation
- Introduction au Schema Registry
- Manipulation de différents types de données (String, JSON, Binary)

**Technologies:**
- Kafka en mode KRaft (sans Zookeeper)
- Docker Compose
- Kafka UI
- Schema Registry

### Jour 2 - Approfondissement et Patterns *(À venir)*

**Objectifs:**
- Créer des producers et consumers avec Spring Boot
- Maîtriser la configuration avancée
- Comprendre la sérialisation/désérialisation
- Implémenter des patterns Kafka courants

**Contenu:** *(En cours de préparation)*

### Jour 3 - Notions Avancées *(À venir)*

**Objectifs:**
- Utiliser Kafka Streams
- Implémenter les transactions
- Optimiser les performances
- Opérer Kafka en production

**Contenu:** *(En cours de préparation)*

## 🛠️ Prérequis

### Logiciels nécessaires

- **Docker** et **Docker Compose** (version récente)
- **Java 17** ou supérieur
- **Maven** 3.6+
- Un IDE Java (IntelliJ IDEA, VS Code, Eclipse)
- **Git**
- **Make** (optionnel mais recommandé)

### Connaissances requises

- Bases de Java
- Concepts de base des systèmes distribués
- Notions de Docker
- Familiarité avec Spring Boot

## 🚀 Démarrage Rapide

### 1. Cloner le repository

```bash
git clone <repository-url>
cd kafka-training
```

### 2. Jour 1 - Explorer le Cluster

```bash
cd jour1/exercice1-explorer-cluster
make start
make ui
```

Ouvrez http://localhost:8080 dans votre navigateur.

### 3. Compiler le projet (pour les exercices Java)

```bash
mvn clean install
```

## 📁 Structure du Repository

```
kafka-training/
├── README.md                          # Ce fichier
├── pom.xml                           # Configuration Maven parent
├── jour1/                            # Jour 1 - Fondamentaux
│   ├── README.md                     # Programme du jour 1
│   └── exercice1-explorer-cluster/   # Exercice d'exploration
│       ├── docker-compose.yml        # Infrastructure Kafka
│       ├── Makefile                  # Commandes utiles
│       ├── infrastructure/           # Scripts de provisioning
│       └── README.md                 # Instructions de l'exercice
├── jour2/                            # Jour 2 - Approfondissement (à venir)
└── jour3/                            # Jour 3 - Avancé (à venir)
```

## 🎓 Approche Pédagogique

Cette formation utilise une **approche pratique et progressive**:

1. **Découverte par l'exploration** (Jour 1)
   - Les participants explorent Kafka UI pour découvrir les concepts
   - Identification autonome des notions clés
   - Débriefing et explications théoriques

2. **Pratique guidée** (Jour 2)
   - Développement d'applications concrètes
   - Exercices progressifs avec Spring Boot
   - Mise en application des patterns

3. **Expertise avancée** (Jour 3)
   - Sujets avancés et cas d'usage réels
   - Optimisation et production
   - Troubleshooting et bonnes pratiques

## 📖 Ressources

### Documentation Officielle
- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Kafka KRaft Mode](https://kafka.apache.org/documentation/#kraft)
- [Spring for Apache Kafka](https://spring.io/projects/spring-kafka)
- [Confluent Platform](https://docs.confluent.io/)

### Outils
- [Kafka UI](https://github.com/provectus/kafka-ui) - Interface web pour Kafka
- [Schema Registry](https://docs.confluent.io/platform/current/schema-registry/index.html) - Gestion des schémas

## 🔧 Commandes Utiles

### Gestion du cluster

```bash
# Démarrer le cluster (depuis un dossier d'exercice)
make start

# Arrêter le cluster
make stop

# Nettoyer complètement
make clean

# Voir les logs
make logs

# Voir le statut
make status
```

### Commandes Kafka CLI

```bash
# Lister les topics
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

# Décrire un topic
docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic <topic-name>

# Consommer des messages
docker exec kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic <topic-name> \
  --from-beginning

# Produire des messages
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic <topic-name>
```

## 🐛 Troubleshooting

### Le cluster ne démarre pas

```bash
make clean
make start
```

### Problèmes de port

Vérifiez que les ports 9092, 8080, et 8081 ne sont pas déjà utilisés:

```bash
lsof -i :9092
lsof -i :8080
lsof -i :8081
```

### Kafka UI ne charge pas

- Attendez 30-60 secondes après le démarrage
- Vérifiez les logs: `make logs`
- Redémarrez: `make restart`

## 👥 Pour les Formateurs

### Préparation avant la formation

1. Tester tous les exercices
2. Vérifier que Docker et Docker Compose fonctionnent
3. Pré-télécharger les images Docker:
   ```bash
   docker-compose pull
   ```
4. Préparer des exemples supplémentaires si nécessaire

### Timing suggéré par jour

**Jour 1 (3-4h)**
- 09:00 - 09:15 : Introduction générale Kafka
- 09:15 - 10:00 : Exercice 1 - Exploration
- 10:00 - 10:30 : Débriefing et théorie
- 10:30 - 10:45 : Pause
- 10:45 - 12:30 : Exercices suivants

**Jours 2 et 3** *(À définir)*

## 📝 Licence

Ce matériel de formation est fourni à des fins éducatives.

## ✨ Contributions

Pour toute suggestion d'amélioration ou correction, n'hésitez pas à ouvrir une issue ou une pull request.

---

**Bonne formation! 🚀**