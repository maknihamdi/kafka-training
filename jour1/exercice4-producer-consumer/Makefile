.PHONY: help start stop restart clean logs ui status compile run-producer run-consumer

help: ## Afficher l'aide
	@echo "╔════════════════════════════════════════════════════╗"
	@echo "║  Exercice 4 - Producer & Consumer Spring Boot     ║"
	@echo "╚════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Commandes disponibles:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

start: ## Démarrer le cluster Kafka
	@echo "🚀 Démarrage du cluster Kafka..."
	docker-compose up -d
	@echo "✅ Cluster Kafka démarré!"
	@echo ""
	@echo "⏳ Attente de l'initialisation des topics..."
	@sleep 10
	@echo ""
	@echo "📊 Kafka UI: http://localhost:8080"
	@echo "🔧 Bootstrap Server: localhost:9092"
	@echo ""
	@echo "📋 Topics créés:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list

stop: ## Arrêter le cluster Kafka
	@echo "🛑 Arrêt du cluster Kafka..."
	docker-compose down
	@echo "✅ Cluster arrêté!"

restart: stop start ## Redémarrer le cluster Kafka

clean: ## Supprimer les volumes et tout nettoyer
	@echo "🧹 Nettoyage complet..."
	docker-compose down -v
	@echo "✅ Nettoyage terminé!"

logs: ## Afficher les logs de Kafka
	docker-compose logs -f kafka

ui: ## Ouvrir Kafka UI dans le navigateur
	@echo "🌐 Ouverture de Kafka UI..."
	@sleep 2
	@open http://localhost:8080 2>/dev/null || xdg-open http://localhost:8080 2>/dev/null || echo "Ouvrez http://localhost:8080 dans votre navigateur"

status: ## Afficher le statut du cluster
	@echo "📊 Statut du cluster Kafka:"
	@echo ""
	@docker-compose ps
	@echo ""
	@echo "📋 Topics disponibles:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "⚠️  Kafka n'est pas encore prêt"

compile: ## Compiler les modules Maven
	@echo "🔨 Compilation des modules..."
	mvn clean install -DskipTests
	@echo "✅ Compilation terminée!"

run-producer: ## Lancer l'application Producer (port 8081)
	@echo "🚀 Lancement du Producer sur le port 8081..."
	@echo "📡 API disponible sur http://localhost:8081"
	@cd producer && mvn spring-boot:run

run-consumer: ## Lancer l'application Consumer
	@echo "🚀 Lancement du Consumer..."
	@cd consumer && mvn spring-boot:run
