.PHONY: help start stop clean logs status connectors create-source create-sink delete-source delete-sink db-connect db-query topics

help:
	@echo "Kafka Connect - Commandes disponibles:"
	@echo "  make start           - Démarrer l'infrastructure complète"
	@echo "  make stop            - Arrêter tous les services"
	@echo "  make clean           - Supprimer tous les conteneurs"
	@echo "  make logs            - Afficher les logs de tous les services"
	@echo "  make status          - Vérifier le statut des services"
	@echo ""
	@echo "Connecteurs:"
	@echo "  make connectors      - Lister tous les connecteurs"
	@echo "  make create-source   - Créer le source connector (FileStream)"
	@echo "  make create-sink     - Créer le sink connector (JDBC)"
	@echo "  make delete-source   - Supprimer le source connector"
	@echo "  make delete-sink     - Supprimer le sink connector"
	@echo ""
	@echo "Base de données:"
	@echo "  make db-connect      - Se connecter à PostgreSQL"
	@echo "  make db-query        - Requêter la table messages"
	@echo ""
	@echo "Kafka:"
	@echo "  make topics          - Lister les topics Kafka"
	@echo "  make consume         - Consommer le topic db-source_data"
	@echo "  make add-data        - Ajouter une donnée dans source_data"

start:
	@echo "🚀 Démarrage de l'infrastructure Kafka Connect..."
	docker-compose up -d
	@echo "⏳ Attente du démarrage complet (90s)..."
	@sleep 90
	@echo "✅ Infrastructure démarrée!"
	@echo "   - Kafka UI: http://localhost:8080"
	@echo "   - Kafka Connect API: http://localhost:8083"
	@echo "   - PostgreSQL: localhost:5432 (user: kafka, password: kafka123)"

stop:
	@echo "🛑 Arrêt de tous les services..."
	docker-compose down

clean:
	@echo "🧹 Nettoyage complet..."
	docker-compose down -v
	@echo "✅ Nettoyage terminé!"

logs:
	docker-compose logs -f

status:
	@echo "📊 Statut des services:"
	@docker-compose ps
	@echo ""
	@echo "🔌 Kafka Connect:"
	@curl -s http://localhost:8083/ | jq . 2>/dev/null || echo "❌ Kafka Connect non disponible"

connectors:
	@echo "📋 Liste des connecteurs:"
	@curl -s http://localhost:8083/connectors | jq . || echo "❌ Impossible de lister les connecteurs"
	@echo ""
	@echo "Détails des connecteurs:"
	@for connector in $$(curl -s http://localhost:8083/connectors | jq -r '.[]' 2>/dev/null); do \
		echo ""; \
		echo "📌 $$connector:"; \
		curl -s http://localhost:8083/connectors/$$connector/status | jq .; \
	done

create-source:
	@echo "📥 Création du Source Connector (JDBC Source)..."
	@curl -X POST http://localhost:8083/connectors \
		-H "Content-Type: application/json" \
		-d @config/source-jdbc-connector.json \
		| jq . || echo "❌ Erreur lors de la création"
	@echo ""
	@echo "✅ Source connector créé! Vérifiez avec: make connectors"

create-sink:
	@echo "📤 Création du Sink Connector (JDBC)..."
	@curl -X POST http://localhost:8083/connectors \
		-H "Content-Type: application/json" \
		-d @config/sink-jdbc-connector.json \
		| jq . || echo "❌ Erreur lors de la création"
	@echo ""
	@echo "✅ Sink connector créé! Vérifiez avec: make connectors"

delete-source:
	@echo "🗑️  Suppression du Source Connector..."
	@curl -X DELETE http://localhost:8083/connectors/jdbc-source
	@echo ""
	@echo "✅ Source connector supprimé!"

delete-sink:
	@echo "🗑️  Suppression du Sink Connector..."
	@curl -X DELETE http://localhost:8083/connectors/jdbc-sink
	@echo ""
	@echo "✅ Sink connector supprimé!"

db-connect:
	@echo "🐘 Connexion à PostgreSQL..."
	@docker exec -it postgres psql -U kafka -d kafka_sink

db-query:
	@echo "🔍 Contenu de la table source_data:"
	@docker exec -it postgres psql -U kafka -d kafka_sink -c "SELECT * FROM source_data ORDER BY id;"
	@echo ""
	@echo "🔍 Contenu de la table sink_data:"
	@docker exec -it postgres psql -U kafka -d kafka_sink -c "SELECT * FROM sink_data ORDER BY id;"

topics:
	@echo "📋 Liste des topics Kafka:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
	@echo ""
	@echo "📊 Détails du topic db-source_data:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic db-source_data 2>/dev/null || echo "Topic pas encore créé"

consume:
	@echo "📩 Consommation du topic db-source_data:"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic db-source_data \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000

add-data:
	@echo "➕ Ajout de nouvelles données dans source_data..."
	@docker exec -it postgres psql -U kafka -d kafka_sink -c "INSERT INTO source_data (message) VALUES ('New message added at $(shell date)');"
	@echo "✅ Donnée ajoutée! Attendez 5s et vérifiez avec: make db-query"
