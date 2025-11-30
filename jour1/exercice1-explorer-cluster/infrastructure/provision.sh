#!/bin/bash

echo "🔧 Provisionnement du cluster Kafka..."

# Attendre que Kafka soit complètement prêt
echo "⏳ Vérification de la disponibilité de Kafka..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if kafka-broker-api-versions --bootstrap-server kafka:29092 &> /dev/null; then
        echo "✅ Kafka est prêt!"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "⏳ Tentative $RETRY_COUNT/$MAX_RETRIES - Kafka n'est pas encore prêt..."
    sleep 2
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ Erreur: Kafka n'est pas disponible après $MAX_RETRIES tentatives"
    exit 1
fi

# Attendre que Schema Registry soit prêt
echo "⏳ Vérification de la disponibilité du Schema Registry..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -s http://schema-registry:8081/subjects &> /dev/null; then
        echo "✅ Schema Registry est prêt!"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    echo "⏳ Tentative $RETRY_COUNT/$MAX_RETRIES - Schema Registry n'est pas encore prêt..."
    sleep 2
done

# Créer les topics
echo ""
echo "📝 Création des topics..."

echo "  → Topic: events (3 partitions)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic events \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists

echo "  → Topic: users (2 partitions)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic users \
    --partitions 2 \
    --replication-factor 1 \
    --if-not-exists

echo "  → Topic: orders (4 partitions)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic orders \
    --partitions 4 \
    --replication-factor 1 \
    --if-not-exists

echo "  → Topic: logs (1 partition)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic logs \
    --partitions 1 \
    --replication-factor 1 \
    --if-not-exists

echo "  → Topic: images (2 partitions - données binaires)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic images \
    --partitions 2 \
    --replication-factor 1 \
    --if-not-exists

echo "  → Topic: transactions (2 partitions - avec headers)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic transactions \
    --partitions 2 \
    --replication-factor 1 \
    --if-not-exists

echo "  → Topic: products (2 partitions - JSON Schema)"
kafka-topics --create \
    --bootstrap-server kafka:29092 \
    --topic products \
    --partitions 2 \
    --replication-factor 1 \
    --if-not-exists

# Insérer des données de démonstration
echo ""
echo "📊 Insertion de données de démonstration..."

echo "  → Insertion de messages dans 'events'..."
echo 'user_login:alice:2024-01-15T10:30:00
user_login:bob:2024-01-15T10:31:00
page_view:alice:/home:2024-01-15T10:32:00
page_view:bob:/products:2024-01-15T10:33:00
add_to_cart:alice:product_123:2024-01-15T10:34:00
checkout:alice:order_456:2024-01-15T10:35:00
user_logout:alice:2024-01-15T10:40:00
page_view:charlie:/home:2024-01-15T10:41:00
user_login:charlie:2024-01-15T10:42:00
add_to_cart:charlie:product_789:2024-01-15T10:43:00' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic events

echo "  → Insertion de messages dans 'users'..."
echo '{"id":"1","name":"Alice","email":"alice@example.com","country":"France"}
{"id":"2","name":"Bob","email":"bob@example.com","country":"USA"}
{"id":"3","name":"Charlie","email":"charlie@example.com","country":"UK"}
{"id":"4","name":"Diana","email":"diana@example.com","country":"France"}
{"id":"5","name":"Eve","email":"eve@example.com","country":"Germany"}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic users

echo "  → Insertion de messages dans 'orders' (avec clés)..."
echo '456:{"orderId":"456","userId":"1","amount":125.50,"status":"completed"}
457:{"orderId":"457","userId":"2","amount":89.99,"status":"pending"}
458:{"orderId":"458","userId":"3","amount":234.00,"status":"completed"}
459:{"orderId":"459","userId":"1","amount":45.50,"status":"processing"}
460:{"orderId":"460","userId":"4","amount":156.75,"status":"completed"}
461:{"orderId":"461","userId":"5","amount":99.99,"status":"pending"}
462:{"orderId":"462","userId":"2","amount":310.00,"status":"completed"}
463:{"orderId":"463","userId":"3","amount":67.25,"status":"processing"}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic orders \
  --property "parse.key=true" \
  --property "key.separator=:"

echo "  → Insertion de messages dans 'logs'..."
echo '[INFO] Application started
[INFO] User alice logged in
[WARN] High memory usage detected
[INFO] Order 456 processed successfully
[ERROR] Failed to connect to database
[INFO] Database connection restored
[INFO] User bob logged in
[INFO] Cache cleared
[WARN] Slow query detected: 2.5s
[INFO] Background job completed' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic logs

echo "  → Insertion de messages avec clé, valeur et headers dans 'transactions'..."
# Messages avec clés, valeurs et headers
echo 'source:api|country:FR|timestamp:1234567890|user1|{"amount":150.00,"currency":"EUR","type":"payment"}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic transactions \
  --property "parse.key=true" \
  --property "key.separator=|" \
  --property "parse.headers=true" \
  --property "headers.delimiter=|"

echo 'source:mobile|country:US|timestamp:1234567891|user2|{"amount":89.50,"currency":"USD","type":"refund"}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic transactions \
  --property "parse.key=true" \
  --property "key.separator=|" \
  --property "parse.headers=true" \
  --property "headers.delimiter=|"

echo 'source:web|country:FR|timestamp:1234567892|user1|{"amount":234.75,"currency":"EUR","type":"payment"}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic transactions \
  --property "parse.key=true" \
  --property "key.separator=|" \
  --property "parse.headers=true" \
  --property "headers.delimiter=|"

echo 'source:api|country:UK|timestamp:1234567893|user3|{"amount":450.00,"currency":"GBP","type":"payment"}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic transactions \
  --property "parse.key=true" \
  --property "key.separator=|" \
  --property "parse.headers=true" \
  --property "headers.delimiter=|"

echo "  → Insertion de données binaires dans 'images'..."
# Créer des données binaires simulées (en base64)
echo "image001" | base64 | kafka-console-producer --bootstrap-server kafka:29092 --topic images
echo "image002" | base64 | kafka-console-producer --bootstrap-server kafka:29092 --topic images
echo "image003" | base64 | kafka-console-producer --bootstrap-server kafka:29092 --topic images
echo "Binary data: PNG..." | base64 | kafka-console-producer --bootstrap-server kafka:29092 --topic images

echo ""
echo "  → Enregistrement du JSON Schema pour 'products'..."
# Enregistrer le JSON Schema dans le Schema Registry
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
  --data '{
    "schemaType": "JSON",
    "schema": "{\"$schema\":\"http://json-schema.org/draft-07/schema#\",\"title\":\"Product\",\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\"},\"name\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"},\"category\":{\"type\":\"string\"},\"inStock\":{\"type\":\"boolean\"}},\"required\":[\"id\",\"name\",\"price\",\"category\",\"inStock\"]}"
  }' \
  http://schema-registry:8081/subjects/products-value/versions

echo ""
echo "  → Insertion de messages dans 'products' (avec clés et JSON Schema)..."
echo 'P001:{"id":"P001","name":"Laptop Dell XPS 13","price":1299.99,"category":"Electronics","inStock":true}
P002:{"id":"P002","name":"iPhone 15 Pro","price":1099.00,"category":"Electronics","inStock":true}
P003:{"id":"P003","name":"Chaise de bureau","price":249.50,"category":"Furniture","inStock":false}
P004:{"id":"P004","name":"Clavier mécanique","price":159.99,"category":"Electronics","inStock":true}
P005:{"id":"P005","name":"Bureau en bois","price":499.00,"category":"Furniture","inStock":true}' | \
kafka-console-producer --bootstrap-server kafka:29092 --topic products \
  --property "parse.key=true" \
  --property "key.separator=:"

# Afficher un résumé
echo ""
echo "✅ Provisionnement terminé!"
echo ""
echo "📋 Résumé des topics créés:"
kafka-topics --bootstrap-server kafka:29092 --list

echo ""
echo "📊 Détails des topics:"
kafka-topics --bootstrap-server kafka:29092 --describe