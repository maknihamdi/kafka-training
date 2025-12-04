.PHONY: help start stop clean build run-producer run-streams logs status topics consume init-data topology topology-describe

help:
	@echo "Kafka Streams Exercice - Commandes disponibles:"
	@echo ""
	@echo "Infrastructure:"
	@echo "  make start           - Démarrer Kafka et Kafka UI"
	@echo "  make stop            - Arrêter tous les services"
	@echo "  make clean           - Supprimer tous les conteneurs et volumes"
	@echo "  make status          - Vérifier le statut des services"
	@echo "  make logs            - Afficher les logs"
	@echo ""
	@echo "Build & Run:"
	@echo "  make build           - Compiler les projets Maven"
	@echo "  make run-producer    - Lancer le producer (port 8081)"
	@echo "  make run-streams     - Lancer Kafka Streams (port 8082)"
	@echo ""
	@echo "Données:"
	@echo "  make init-data       - Initialiser les profils utilisateurs"
	@echo "  make generate        - Générer 20 événements de test"
	@echo "  make generate-100    - Générer 100 événements"
	@echo ""
	@echo "Topics Kafka:"
	@echo "  make topics          - Lister tous les topics"
	@echo "  make describe-topics - Détails des topics (compaction, partitions)"
	@echo "  make consume-events  - Consommer user-events"
	@echo "  make consume-filtered - Consommer filtered-events"
	@echo "  make consume-profiles - Consommer user-profiles"
	@echo ""
	@echo "Topologie Kafka Streams:"
	@echo "  make topology        - Afficher la topologie (JSON)"
	@echo "  make topology-describe - Description de la topologie (texte)"

start:
	@echo "🚀 Démarrage de Kafka..."
	docker compose up -d
	@echo "⏳ Attente du démarrage (30s)..."
	@sleep 30
	@echo "✅ Infrastructure démarrée!"
	@echo ""
	@echo "💡 Prochaines étapes:"
	@echo "   1. Compiler: make build"
	@echo "   2. Lancer le producer: make run-producer"
	@echo "   3. Initialiser les profils: make init-data"
	@echo "   4. Générer des événements: make generate"
	@echo "   5. Lancer Kafka Streams: make run-streams"
	@echo ""
	@echo "⚠️  Ports utilisés:"
	@echo "   - Kafka: localhost:9092"
	@echo "   - Kafka UI: http://localhost:8080"
	@echo "   - Producer API: http://localhost:8081"
	@echo "   - Streams API: http://localhost:8082"

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

init-data:
	@echo "👥 Initialisation des profils utilisateurs..."
	@curl -X POST http://localhost:8081/api/profiles/init
	@echo ""
	@echo "✅ 5 profils créés dans le topic user-profiles (compacté)"

generate:
	@echo "📊 Génération de 20 événements..."
	@curl -X POST "http://localhost:8081/api/events/generate?count=20"
	@echo ""

generate-100:
	@echo "📊 Génération de 100 événements..."
	@curl -X POST "http://localhost:8081/api/events/generate?count=100"
	@echo ""

topics:
	@echo "📋 Liste des topics Kafka:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

describe-topics:
	@echo "📊 Détails des topics:"
	@echo ""
	@echo "=== user-events ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic user-events
	@echo ""
	@echo "=== user-profiles (COMPACTED) ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic user-profiles
	@echo ""
	@echo "=== filtered-events ==="
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic filtered-events 2>/dev/null || echo "Topic pas encore créé"

consume-events:
	@echo "📩 Consommation de user-events (10 derniers):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic user-events \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000

consume-filtered:
	@echo "📩 Consommation de filtered-events (PURCHASE uniquement):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic filtered-events \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000

consume-profiles:
	@echo "📩 Consommation de user-profiles (topic compacté):"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic user-profiles \
		--from-beginning \
		--property print.key=true \
		--property key.separator=":" \
		--timeout-ms 5000

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
