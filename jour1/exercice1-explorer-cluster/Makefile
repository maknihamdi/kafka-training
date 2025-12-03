.PHONY: help start stop restart clean logs logs-init ui status

help: ## Afficher l'aide
	@echo "╔════════════════════════════════════════════════════╗"
	@echo "║  Exercice 1 - Explorer le cluster Kafka           ║"
	@echo "╚════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Commandes disponibles:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

start: ## Démarrer le cluster Kafka (avec provisioning automatique)
	@echo "🚀 Démarrage du cluster Kafka en mode KRaft..."
	docker compose up -d
	@echo "✅ Cluster Kafka démarré!"
	@echo "⏳ Le provisioning des données se fait automatiquement..."
	@echo ""
	@echo "📊 Kafka UI: http://localhost:8080"
	@echo "🔧 Schema Registry: http://localhost:8081"
	@echo ""
	@echo "💡 Utilisez 'make logs-init' pour suivre le provisioning"
	@echo "💡 Utilisez 'make ui' pour ouvrir Kafka UI dans le navigateur"

stop: ## Arrêter le cluster Kafka
	@echo "🛑 Arrêt du cluster Kafka..."
	docker compose down
	@echo "✅ Cluster arrêté!"

restart: stop start ## Redémarrer le cluster Kafka

clean: ## Supprimer les volumes et tout nettoyer
	@echo "🧹 Nettoyage complet..."
	docker compose down -v
	@echo "✅ Nettoyage terminé!"

logs: ## Afficher les logs de Kafka
	docker compose logs -f kafka

logs-init: ## Afficher les logs du provisioning
	docker compose logs -f kafka-init

ui: ## Ouvrir Kafka UI dans le navigateur
	@echo "🌐 Ouverture de Kafka UI..."
	@sleep 2
	@open http://localhost:8080 2>/dev/null || xdg-open http://localhost:8080 2>/dev/null || echo "Ouvrez http://localhost:8080 dans votre navigateur"

status: ## Afficher le statut du cluster
	@echo "📊 Statut du cluster Kafka:"
	@echo ""
	@docker compose ps
	@echo ""
	@echo "📋 Topics disponibles:"
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "⚠️  Kafka n'est pas encore prêt"
