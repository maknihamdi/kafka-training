#!/bin/bash

# ========================================
# Générateur d'événements pour ksqlDB
# ========================================
# Lit les données depuis events_data.txt et les produit dans Kafka

set -e

KAFKA_CONTAINER="kafka"
TOPIC="user_events"
BOOTSTRAP_SERVER="localhost:9092"
DATA_FILE="$(dirname "$0")/events_data.txt"

# Couleurs pour l'affichage
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 Générateur d'événements Kafka${NC}"
echo -e "${BLUE}📝 Topic: ${TOPIC}${NC}"
echo ""

# Vérifier que Kafka est accessible
if ! docker exec ${KAFKA_CONTAINER} kafka-topics --bootstrap-server ${BOOTSTRAP_SERVER} --list > /dev/null 2>&1; then
    echo -e "${YELLOW}❌ Kafka n'est pas accessible${NC}"
    echo "Assurez-vous que l'infrastructure est démarrée: make start"
    exit 1
fi

# Créer le topic s'il n'existe pas
if ! docker exec ${KAFKA_CONTAINER} kafka-topics --bootstrap-server ${BOOTSTRAP_SERVER} --list | grep -q "^${TOPIC}$"; then
    echo -e "${YELLOW}📂 Création du topic ${TOPIC}...${NC}"
    docker exec ${KAFKA_CONTAINER} kafka-topics \
        --bootstrap-server ${BOOTSTRAP_SERVER} \
        --create \
        --topic ${TOPIC} \
        --partitions 1 \
        --replication-factor 1 > /dev/null 2>&1
    echo -e "${GREEN}✅ Topic créé${NC}"
    echo ""
fi

# Fonction pour générer un timestamp en millisecondes
get_timestamp() {
    echo $(($(date +%s) * 1000))
}

# Fonction pour convertir une ligne en JSON
line_to_json() {
    local line="$1"

    # Ignorer les commentaires et lignes vides
    [[ "$line" =~ ^#.*$ ]] && return
    [[ -z "$line" ]] && return

    # Parser la ligne (format: user_id|event_type|amount|country)
    IFS='|' read -r user_id event_type amount country <<< "$line"

    # Timestamp actuel en millisecondes
    local timestamp=$(get_timestamp)

    # Construire le JSON
    if [[ -z "$amount" ]]; then
        # Pas de montant (LOGIN, LOGOUT, VIEW_PRODUCT)
        echo "{\"user_id\":${user_id},\"event_type\":\"${event_type}\",\"amount\":null,\"country\":\"${country}\",\"event_time\":${timestamp}}"
    else
        # Avec montant (PURCHASE, ADD_TO_CART, CANCEL)
        echo "{\"user_id\":${user_id},\"event_type\":\"${event_type}\",\"amount\":${amount},\"country\":\"${country}\",\"event_time\":${timestamp}}"
    fi
}

# Mode d'exécution
MODE="${1:-batch}"

if [[ "$MODE" == "continuous" ]] || [[ "$MODE" == "-c" ]] || [[ "$MODE" == "--continuous" ]]; then
    # Mode continu: génère des événements en boucle
    echo -e "${YELLOW}🔄 Mode continu activé (Ctrl+C pour arrêter)${NC}"
    echo -e "${YELLOW}⏱️  1 événement toutes les 2 secondes${NC}"
    echo ""

    count=0
    trap "echo ''; echo -e '${GREEN}✅ Arrêté après ${count} événements${NC}'; exit 0" INT

    while true; do
        # Lire aléatoirement une ligne du fichier (compatible Unix/macOS)
        line=$(grep -v '^#' "$DATA_FILE" | grep -v '^$' | sort -R | head -n 1)
        [[ -z "$line" ]] && continue

        json=$(line_to_json "$line")
        [[ -z "$json" ]] && continue

        # Envoyer à Kafka
        echo "$json" | docker exec -i ${KAFKA_CONTAINER} \
            kafka-console-producer \
            --bootstrap-server ${BOOTSTRAP_SERVER} \
            --topic ${TOPIC} 2>/dev/null

        count=$((count + 1))

        # Afficher l'événement
        IFS='|' read -r user_id event_type amount country <<< "$line"
        amount_str="${amount:-N/A}"
        printf "[%3d] %-15s | User %3d | %10s | %s\n" "$count" "$event_type" "$user_id" "$amount_str" "$country"

        sleep 2
    done
else
    # Mode batch: envoyer tous les événements du fichier
    total_lines=$(grep -v '^#' "$DATA_FILE" | grep -v '^$' | wc -l | tr -d ' ')
    echo -e "${BLUE}📊 Envoi de ${total_lines} événements...${NC}"
    echo ""

    count=0
    while IFS= read -r line; do
        json=$(line_to_json "$line")
        [[ -z "$json" ]] && continue

        # Envoyer à Kafka
        echo "$json" | docker exec -i ${KAFKA_CONTAINER} \
            kafka-console-producer \
            --bootstrap-server ${BOOTSTRAP_SERVER} \
            --topic ${TOPIC} 2>/dev/null

        count=$((count + 1))

        # Afficher l'événement
        IFS='|' read -r user_id event_type amount country <<< "$line"
        amount_str="${amount:-N/A}"
        printf "[%3d/%3d] %-15s | User %3d | %10s | %s\n" "$count" "$total_lines" "$event_type" "$user_id" "$amount_str" "$country"

        # Petite pause pour ne pas tout envoyer d'un coup
        sleep 0.1
    done < <(grep -v '^#' "$DATA_FILE" | grep -v '^$')

    echo ""
    echo -e "${GREEN}✅ ${count} événements envoyés avec succès!${NC}"
fi
