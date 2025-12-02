.PHONY: help start stop clean logs status ksql topics consume generate install-deps

help:
	@echo "ksqlDB Exercice - Commandes disponibles:"
	@echo "  make start           - Démarrer l'infrastructure (Kafka + ksqlDB + Kafka UI)"
	@echo "  make stop            - Arrêter tous les services"
	@echo "  make clean           - Supprimer tous les conteneurs et volumes"
	@echo "  make logs            - Afficher les logs de tous les services"
	@echo "  make status          - Vérifier le statut des services"
	@echo ""
	@echo "ksqlDB:"
	@echo "  make ksql            - Se connecter au CLI ksqlDB"
	@echo "  make ksql-setup      - Charger le fichier setup.sql dans ksqlDB"
	@echo "  make ksql-queries    - Afficher les requêtes en cours"
	@echo "  make ksql-streams    - Lister les streams créés"
	@echo "  make ksql-tables     - Lister les tables créées"
	@echo ""
	@echo "Données:"
	@echo "  make generate        - Générer tous les événements de test"
	@echo "  make generate-continuous - Générer des événements en continu"
	@echo "  make topics          - Lister les topics Kafka"
	@echo "  make consume         - Consommer le topic user_events"

start:
	@echo "🚀 Démarrage de l'infrastructure ksqlDB..."
	docker-compose up -d
	@echo "⏳ Attente du démarrage complet (60s)..."
	@sleep 60
	@echo "✅ Infrastructure démarrée!"
	@echo ""
	@echo "   - Kafka Broker: localhost:9092"
	@echo "   - ksqlDB Server: http://localhost:8088"
	@echo "   - Kafka UI: http://localhost:8080"
	@echo ""
	@echo "💡 Prochaines étapes:"
	@echo "   1. Générer des données: make generate"
	@echo "   2. Charger le setup ksqlDB: make ksql-setup"
	@echo "   3. Se connecter à ksqlDB: make ksql"

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
	@echo "🔷 ksqlDB Server:"
	@curl -s http://localhost:8088/info | jq . 2>/dev/null || echo "❌ ksqlDB non disponible"

ksql:
	@echo "🔷 Connexion au CLI ksqlDB..."
	@echo "💡 Tapez 'exit' ou Ctrl+D pour quitter"
	@echo ""
	docker exec -it ksqldb-cli ksql http://ksqldb-server:8088

ksql-setup:
	@echo "📂 Chargement de queries/setup.sql dans ksqlDB..."
	@docker exec ksqldb-cli ksql http://ksqldb-server:8088 --file /opt/queries/setup.sql
	@echo "✅ Setup terminé!"
	@echo "💡 Vérifiez avec: make ksql-streams"

ksql-queries:
	@echo "📋 Requêtes ksqlDB en cours:"
	@docker exec ksqldb-cli ksql http://ksqldb-server:8088 --execute "SHOW QUERIES;"

ksql-streams:
	@echo "📋 Streams ksqlDB:"
	@docker exec ksqldb-cli ksql http://ksqldb-server:8088 --execute "SHOW STREAMS;"

ksql-tables:
	@echo "📋 Tables ksqlDB:"
	@docker exec ksqldb-cli ksql http://ksqldb-server:8088 --execute "SHOW TABLES;"

generate:
	@echo "📊 Génération des événements de test..."
	./scripts/generate_events.sh
	@echo ""
	@echo "💡 Vérifiez avec: make consume"

generate-continuous:
	@echo "🔄 Génération continue d'événements (Ctrl+C pour arrêter)..."
	./scripts/generate_events.sh continuous

topics:
	@echo "📋 Liste des topics Kafka:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list
	@echo ""
	@echo "📊 Détail du topic user_events:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 \
		--describe --topic user_events 2>/dev/null || echo "Topic pas encore créé"

consume:
	@echo "📩 Consommation du topic user_events:"
	@docker exec kafka kafka-console-consumer \
		--bootstrap-server localhost:9092 \
		--topic user_events \
		--from-beginning \
		--max-messages 10 \
		--timeout-ms 5000
