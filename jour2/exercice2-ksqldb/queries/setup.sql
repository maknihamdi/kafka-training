-- ========================================
-- SETUP INITIAL KSQLDB
-- ========================================
-- Ce fichier est chargé automatiquement au démarrage de ksqlDB
-- Il crée le stream de base sur le topic "user_events"

-- Créer un stream sur le topic user_events
-- Ce topic sera alimenté par le script de génération de données
CREATE STREAM user_events_stream (
    user_id INT,
    event_type VARCHAR,
    amount DOUBLE,
    country VARCHAR,
    event_time BIGINT
) WITH (
    KAFKA_TOPIC='user_events',
    VALUE_FORMAT='JSON',
    PARTITIONS=1,
    TIMESTAMP='event_time'
);

-- Créer un stream dérivé avec uniquement les achats
CREATE STREAM purchases_stream AS
    SELECT
        user_id,
        amount,
        country,
        event_time
    FROM user_events_stream
    WHERE event_type = 'PURCHASE'
    EMIT CHANGES;
