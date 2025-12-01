.PHONY: help start stop restart clean logs logs-all ui status shell-1 shell-2 shell-3 stop-broker-1 stop-broker-2 stop-broker-3 start-broker-1 start-broker-2 start-broker-3

help: ## Afficher l'aide
	@echo "╔════════════════════════════════════════════════════╗"
	@echo "║  Exercice 3 - Réplication et Résilience           ║"
	@echo "╚════════════════════════════════════════════════════╝"
	@echo ""
	@echo "Commandes disponibles:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'

start: ## Démarrer le cluster Kafka (3 brokers)
	@echo "🚀 Démarrage du cluster Kafka (3 brokers)..."
	docker-compose up -d
	@echo "✅ Cluster Kafka démarré!"
	@echo ""
	@echo "⏳ Attente du démarrage complet (30 secondes)..."
	@sleep 30
	@echo ""
	@echo "📊 Kafka UI: http://localhost:8080"
	@echo "🔧 Brokers:"
	@echo "   - Broker 1 (kafka-1): localhost:9092"
	@echo "   - Broker 2 (kafka-2): localhost:9093"
	@echo "   - Broker 3 (kafka-3): localhost:9094"

stop: ## Arrêter le cluster Kafka
	@echo "🛑 Arrêt du cluster Kafka..."
	docker-compose down
	@echo "✅ Cluster arrêté!"

restart: stop start ## Redémarrer le cluster Kafka

clean: ## Supprimer les volumes et tout nettoyer
	@echo "🧹 Nettoyage complet..."
	docker-compose down -v
	@echo "✅ Nettoyage terminé!"

logs: ## Afficher les logs de tous les brokers
	docker-compose logs -f kafka-1 kafka-2 kafka-3

logs-all: ## Afficher tous les logs (brokers + UI)
	docker-compose logs -f

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
	@docker exec kafka-1 kafka-topics --bootstrap-server localhost:9092 --list 2>/dev/null || echo "⚠️  Kafka n'est pas encore prêt ou aucun topic créé"

shell-1: ## Ouvrir un shell dans kafka-1
	@echo "🔧 Ouverture d'un shell dans kafka-1..."
	@docker exec -it kafka-1 /bin/bash

shell-2: ## Ouvrir un shell dans kafka-2
	@echo "🔧 Ouverture d'un shell dans kafka-2..."
	@docker exec -it kafka-2 /bin/bash

shell-3: ## Ouvrir un shell dans kafka-3
	@echo "🔧 Ouverture d'un shell dans kafka-3..."
	@docker exec -it kafka-3 /bin/bash

stop-broker-1: ## Arrêter kafka-1 (pour tester la résilience)
	@echo "🛑 Arrêt de kafka-1..."
	docker-compose stop kafka-1
	@echo "✅ kafka-1 arrêté!"

stop-broker-2: ## Arrêter kafka-2 (pour tester la résilience)
	@echo "🛑 Arrêt de kafka-2..."
	docker-compose stop kafka-2
	@echo "✅ kafka-2 arrêté!"

stop-broker-3: ## Arrêter kafka-3 (pour tester la résilience)
	@echo "🛑 Arrêt de kafka-3..."
	docker-compose stop kafka-3
	@echo "✅ kafka-3 arrêté!"

start-broker-1: ## Redémarrer kafka-1
	@echo "🚀 Redémarrage de kafka-1..."
	docker-compose start kafka-1
	@echo "✅ kafka-1 redémarré!"

start-broker-2: ## Redémarrer kafka-2
	@echo "🚀 Redémarrage de kafka-2..."
	docker-compose start kafka-2
	@echo "✅ kafka-2 redémarré!"

start-broker-3: ## Redémarrer kafka-3
	@echo "🚀 Redémarrage de kafka-3..."
	docker-compose start kafka-3
	@echo "✅ kafka-3 redémarré!"