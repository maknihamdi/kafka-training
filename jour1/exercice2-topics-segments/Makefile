.PHONY: help start stop restart clean logs ui status shell

help: ## Afficher l'aide
	@echo "╔════════════════════════════════════════════════════╗"
	@echo "║  Exercice 2 - Topics et Segments                  ║"
	@echo "╚════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Commandes disponibles:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-15s\033[0m %s\n", $$1, $$2}'

start: ## Démarrer le cluster Kafka
	@echo "🚀 Démarrage du cluster Kafka..."
	docker-compose up -d
	@echo "✅ Cluster Kafka démarré!"
	@echo ""
	@echo "📊 Kafka UI: http://localhost:8080"
	@echo "🔧 Pour exécuter des commandes: make shell"

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
	@docker exec kafka kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "⚠️  Kafka n'est pas encore prêt ou aucun topic créé"

shell: ## Ouvrir un shell dans le container Kafka
	@echo "🔧 Ouverture d'un shell dans le container Kafka..."
	@echo "💡 Utilisez 'exit' pour quitter"
	@docker exec -it kafka /bin/bash

explore: ## Explorer le système de fichiers Kafka
	@echo "📁 Exploration du système de fichiers Kafka..."
	@echo ""
	@echo "📍 Répertoire des données Kafka:"
	@docker exec kafka ls -lah /var/lib/kafka/data
	@echo ""
	@echo "💡 Pour explorer en détail, utilisez: make shell"