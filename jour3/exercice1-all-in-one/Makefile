.PHONY: help start stop clean build run-producer run-streams logs status topics consume init-data topology topology-describe

help:
	@echo "Kafka Streams Exercice - Devis d'Assurance - Commandes disponibles:"
	@echo ""
	@echo "Infrastructure:"
	@echo "  make start           - Démarrer Kafka, Redis, Kafka Connect, Prometheus, Grafana"
	@echo "  make stop            - Arrêter tous les services"
	@echo "  make clean           - Supprimer tous les conteneurs et volumes"
	@echo "  make status          - Vérifier le statut des services"
	@echo "  make logs            - Afficher les logs"
	@echo ""
	@echo "Build & Run:"
	@echo "  make build           - Compiler les projets Maven"
	@echo "  make run-producer    - Lancer le producer (port 8081)"
	@echo "  make run-streams     - Lancer Kafka Streams (port 8082)"
	@echo "  make run-query-api   - Lancer le Query API (port 8084)"
	@echo ""
	@echo "Données Devis:"
	@echo "  make init-pricing    - Initialiser les prix des produits"
	@echo "  make generate-quotes - Générer 20 devis de test"
	@echo "  make generate-100    - Générer 100 devis"
	@echo ""
	@echo "Topics Kafka:"
	@echo "  make topics          - Lister tous les topics"
	@echo "  make describe-topics - Détails des topics (compaction, partitions)"
	@echo "  make consume-quotes  - Consommer devis-events"
	@echo "  make consume-validated - Consommer validated-quotes"
	@echo "  make consume-pricing - Consommer product-pricing"
	@echo "  make consume-all-quotes - Consommer all-quotes"
	@echo ""
	@echo "Query API:"
	@echo "  make query-all       - Récupérer tous les devis"
	@echo "  make query-stats     - Statistiques globales"
	@echo "  make query-customer  - Devis par client (ex: make query-customer C=C001)"
	@echo ""
	@echo "Redis:"
	@echo "  make redis-cli       - Accès Redis CLI"
	@echo "  make redis-ui        - Ouvrir Redis Commander (http://localhost:8085)"
	@echo ""
	@echo "Kafka Connect:"
	@echo "  make connect-status  - Vérifier le statut de Kafka Connect"
	@echo "  make connect-redis   - Configurer le connecteur Redis"
	@echo "  make connect-delete  - Supprimer le connecteur Redis"
	@echo ""
	@echo "Monitoring:"
	@echo "  make prometheus      - Ouvrir Prometheus (http://localhost:9090)"
	@echo "  make grafana         - Ouvrir Grafana (http://localhost:3000)"
	@echo ""
	@echo "Topologie Kafka Streams:"
	@echo "  make topology        - Afficher la topologie (JSON)"
	@echo "  make topology-describe - Description de la topologie (texte)"

start:
	@echo "🚀 Démarrage de l'infrastructure..."
	docker compose up -d
	@echo "⏳ Attente du démarrage (40s)..."
	@sleep 40
	@echo "✅ Infrastructure démarrée!"
	@echo ""
	@echo "💡 Prochaines étapes:"
	@echo "   1. Compiler: make build"
	@echo "   2. Lancer le producer: make run-producer"
	@echo "   3. Initialiser les prix: make init-pricing"
	@echo "   4. Générer des devis: make generate-quotes"
	@echo "   5. Lancer Kafka Streams: make run-streams"
	@echo "   6. Configurer le connecteur Redis: make connect-redis"
	@echo ""
	@echo "⚠️  Ports utilisés:"
	@echo "   - Kafka: localhost:9092"
	@echo "   - Kafka UI: http://localhost:8080"
	@echo "   - Producer API: http://localhost:8081"
	@echo "   - Streams API: http://localhost:8082"
	@echo "   - Kafka Connect: http://localhost:8083"
	@echo "   - Query API: http://localhost:8084"
	@echo "   - Redis Commander: http://localhost:8085"
	@echo "   - Redis: localhost:6379"
	@echo "   - Prometheus: http://localhost:9090"
	@echo "   - Grafana: http://localhost:3000"

stop:
	@echo "🛑 Arrêt des services..."
	docker compose down

clean:
	@echo "🧹 Nettoyage complet..."
	docker compose down -v
	@echo "✅ Nettoyage terminé!"

logs:
	docker compose logs -f

status:
	@echo "📊 Statut des services:"
	@docker compose ps

build:
	@echo "🔨 Compilation des projets Maven..."
	mvn clean install
	@echo "✅ Compilation terminée!"

run-producer:
	@echo "🚀 Lancement du Producer (port 8081)..."
	@echo "   API disponible sur: http://localhost:8081"
	cd producer && mvn spring-boot:run

run-streams:
	@echo "🌊 Lancement de Kafka Streams (port 8082)..."
	@echo "   Vérifiez les logs pour voir la topologie"
	cd streams && mvn spring-boot:run

run-query-api:
	@echo "🔍 Lancement du Query API (port 8084)..."
	@echo "   API disponible sur: http://localhost:8084"
	cd query-api && mvn spring-boot:run

init-pricing:
	@echo "💰 Initialisation des prix des produits..."
	@curl -X POST http://localhost:8081/api/pricing/init
	@echo ""
	@echo "✅ 5 produits créés dans le topic product-pricing (compacté)"

generate-quotes:
	@echo "📊 Génération de 20 devis..."
	@curl -X POST "http://localhost:8081/api/quotes/generate?count=20"
	@echo ""

generate-100:
	@echo "📊 Génération de 100 devis..."
	@curl -X POST "http://localhost:8081/api/quotes/generate?count=100"
	@echo ""

init-data: init-pricing

topics:
	@echo "📋 Liste des topics Kafka:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

describe-topics:
	@echo "📊 Détails des topics:"
	@echo ""
	@echo "=== devis-events ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic devis-events 2>/dev/null || echo "Topic pas encore créé"
	@echo ""
	@echo "=== product-pricing (COMPACTED) ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic product-pricing 2>/dev/null || echo "Topic pas encore créé"
	@echo ""
	@echo "=== validated-quotes ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic validated-quotes 2>/dev/null || echo "Topic pas encore créé"
	@echo ""
	@echo "=== all-quotes ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic all-quotes 2>/dev/null || echo "Topic pas encore créé"

consume-quotes:
	@echo "📩 Consommation de devis-events (10 derniers):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic devis-events \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000

consume-validated:
	@echo "📩 Consommation de validated-quotes (devis validés uniquement):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic validated-quotes \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000

consume-pricing:
	@echo "📩 Consommation de product-pricing (topic compacté):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic product-pricing \
		--from-beginning \
		--property print.key=true \
		--property key.separator=":" \
		--timeout-ms 5000

consume-all-quotes:
	@echo "📩 Consommation de all-quotes (devis enrichis):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic all-quotes \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000

consume-events: consume-quotes
consume-filtered: consume-validated
consume-profiles: consume-pricing

topology:
	@echo "📊 Topologie Kafka Streams (JSON):"
	@curl -s http://localhost:8082/api/topology | jq '.' 2>/dev/null || \
		curl -s http://localhost:8082/api/topology

topology-describe:
	@echo "📊 Description de la topologie Kafka Streams:"
	@echo ""
	@curl -s http://localhost:8082/api/topology/describe
	@echo ""
	@echo ""
	@echo "💡 Pour visualiser graphiquement:"
	@echo "   1. Copier la sortie ci-dessus"
	@echo "   2. Aller sur: https://zz85.github.io/kafka-streams-viz/"
	@echo "   3. Coller la description dans l'outil"

show-topology:
	curl -s http://localhost:8082/api/topology/describe
	open "https://zz85.github.io/kafka-streams-viz/#topology=$$topology"

# Redis commands
redis-cli:
	@echo "🔧 Connexion à Redis CLI..."
	@docker exec -it redis redis-cli

redis-ui:
	@echo "🌐 Ouverture de Redis Commander..."
	@open http://localhost:8085

# Kafka Connect commands
connect-status:
	@echo "📊 Statut de Kafka Connect:"
	@curl -s http://localhost:8083/connectors | jq '.' 2>/dev/null || curl -s http://localhost:8083/connectors

connect-redis:
	@echo "🔗 Configuration du connecteur Redis Sink..."
	@curl -X POST http://localhost:8083/connectors \
		-H "Content-Type: application/json" \
		-d '{ \
			"name": "redis-sink-quotes", \
			"config": { \
				"connector.class": "com.github.jcustenborder.kafka.connect.redis.RedisSinkConnector", \
				"tasks.max": "1", \
				"topics": "all-quotes", \
				"redis.hosts": "redis:6379", \
				"redis.database": "0", \
				"redis.client.mode": "Standalone", \
				"key.converter": "org.apache.kafka.connect.storage.StringConverter", \
				"value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter" \
			} \
		}' | jq '.' 2>/dev/null || echo "Erreur lors de la création du connecteur"
	@echo ""
	@echo "✅ Connecteur Redis configuré"

connect-delete:
	@echo "🗑️  Suppression du connecteur Redis..."
	@curl -X DELETE http://localhost:8083/connectors/redis-sink-quotes 2>/dev/null
	@echo ""
	@echo "✅ Connecteur supprimé"

# Monitoring commands
prometheus:
	@echo "📊 Ouverture de Prometheus..."
	@open http://localhost:9090

grafana:
	@echo "📈 Ouverture de Grafana..."
	@echo "   Credentials: admin/admin"
	@open http://localhost:3000

# Query API commands
query-all:
	@echo "📊 Récupération de tous les devis..."
	@curl -s http://localhost:8084/api/quotes | jq '.' 2>/dev/null || curl -s http://localhost:8084/api/quotes

query-stats:
	@echo "📈 Statistiques globales..."
	@curl -s http://localhost:8084/api/quotes/stats | jq '.' 2>/dev/null || curl -s http://localhost:8084/api/quotes/stats

query-customer:
	@echo "👤 Devis du client $(C)..."
	@curl -s http://localhost:8084/api/quotes/customer/$(C) | jq '.' 2>/dev/null || curl -s http://localhost:8084/api/quotes/customer/$(C)

query-product:
	@echo "📦 Devis du produit $(P)..."
	@curl -s http://localhost:8084/api/quotes/product/$(P) | jq '.' 2>/dev/null || curl -s http://localhost:8084/api/quotes/product/$(P)
