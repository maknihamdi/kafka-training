-- ========================================
-- EXEMPLES DE REQUÊTES KSQLDB
-- ========================================
-- Ces requêtes sont des exemples pour guider les participants
-- À exécuter dans le CLI ksqlDB: make ksql

-- ========================================
-- STREAMS DÉJÀ CRÉÉS AU DÉMARRAGE
-- ========================================
-- user_events_stream : tous les événements
-- purchases_stream : uniquement les achats (filtre PURCHASE)

-- Voir le schéma d'un stream
DESCRIBE user_events_stream;
DESCRIBE purchases_stream;

-- ========================================
-- 1. CONSULTER LES STREAMS EN TEMPS RÉEL
-- ========================================

-- Voir tous les événements en temps réel
SELECT * FROM user_events_stream EMIT CHANGES LIMIT 10;

-- Voir uniquement les achats
SELECT * FROM purchases_stream EMIT CHANGES LIMIT 5;

-- ========================================
-- 2. FILTRER LES ÉVÉNEMENTS
-- ========================================

-- Achats en France uniquement
CREATE STREAM french_purchases AS
    SELECT *
    FROM purchases_stream
    WHERE country = 'France'
    EMIT CHANGES;

-- Achats supérieurs à 100€
CREATE STREAM high_value_purchases AS
    SELECT
        user_id,
        amount,
        country,
        event_time
    FROM purchases_stream
    WHERE amount > 100
    EMIT CHANGES;

-- Événements LOGIN uniquement
CREATE STREAM login_events AS
    SELECT *
    FROM user_events_stream
    WHERE event_type = 'LOGIN'
    EMIT CHANGES;

-- ========================================
-- 3. TRANSFORMER LES DONNÉES
-- ========================================

-- Catégoriser les achats par montant
CREATE STREAM categorized_purchases AS
    SELECT
        user_id,
        amount,
        country,
        CASE
            WHEN amount < 50 THEN 'SMALL'
            WHEN amount < 150 THEN 'MEDIUM'
            ELSE 'LARGE'
        END AS purchase_category,
        amount * 0.2 AS tax_amount,
        amount * 1.2 AS total_with_tax
    FROM purchases_stream
    EMIT CHANGES;

-- ========================================
-- 4. AGRÉGATIONS AVEC TABLES
-- ========================================

-- Total dépensé par utilisateur
CREATE TABLE user_total_spent AS
    SELECT
        user_id,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_spent,
        AVG(amount) AS avg_spent,
        MAX(amount) AS max_purchase,
        MIN(amount) AS min_purchase
    FROM purchases_stream
    GROUP BY user_id
    EMIT CHANGES;

-- Statistiques par pays
CREATE TABLE purchases_by_country AS
    SELECT
        country,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_amount,
        AVG(amount) AS avg_amount
    FROM purchases_stream
    GROUP BY country
    EMIT CHANGES;

-- Compter les événements par type
CREATE TABLE events_by_type AS
    SELECT
        event_type,
        COUNT(*) AS event_count
    FROM user_events_stream
    GROUP BY event_type
    EMIT CHANGES;

-- ========================================
-- 5. FENÊTRES TEMPORELLES (WINDOWING)
-- ========================================

-- Achats par pays sur fenêtres de 30 secondes
CREATE TABLE purchases_by_country_windowed AS
    SELECT
        country,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_amount,
        WINDOWSTART AS window_start,
        WINDOWEND AS window_end
    FROM purchases_stream
    WINDOW TUMBLING (SIZE 30 SECONDS)
    GROUP BY country
    EMIT CHANGES;

-- Fenêtre glissante de 1 minute, avançant toutes les 30 secondes
CREATE TABLE purchases_hopping_window AS
    SELECT
        country,
        COUNT(*) AS purchase_count,
        SUM(amount) AS total_amount
    FROM purchases_stream
    WINDOW HOPPING (SIZE 1 MINUTE, ADVANCE BY 30 SECONDS)
    GROUP BY country
    EMIT CHANGES;

-- ========================================
-- 6. JOINTURES
-- ========================================

-- Enrichir les achats avec les stats de l'utilisateur
CREATE STREAM purchases_with_stats AS
    SELECT
        p.user_id,
        p.amount,
        p.country,
        u.total_spent AS user_total_spent,
        u.avg_spent AS user_avg_spent,
        u.purchase_count AS user_purchase_count
    FROM purchases_stream p
    LEFT JOIN user_total_spent u ON p.user_id = u.user_id
    EMIT CHANGES;

-- Détecter les achats au-dessus de la moyenne de l'utilisateur
CREATE STREAM above_average_purchases AS
    SELECT
        p.user_id,
        p.amount,
        u.avg_spent AS user_avg,
        p.amount - u.avg_spent AS amount_above_avg
    FROM purchases_stream p
    LEFT JOIN user_total_spent u ON p.user_id = u.user_id
    WHERE p.amount > u.avg_spent
    EMIT CHANGES;

-- ========================================
-- 7. STATISTIQUES GLOBALES
-- ========================================

CREATE TABLE global_stats AS
    SELECT
        'GLOBAL' AS key,
        COUNT(*) AS total_purchases,
        SUM(amount) AS total_revenue,
        AVG(amount) AS avg_amount
    FROM purchases_stream
    GROUP BY 'GLOBAL'
    EMIT CHANGES;

-- ========================================
-- COMMANDES UTILES
-- ========================================

-- Lister tous les topics
SHOW TOPICS;

-- Lister tous les streams
SHOW STREAMS;

-- Lister toutes les tables
SHOW TABLES;

-- Voir les requêtes en cours
SHOW QUERIES;

-- Décrire un stream ou une table
DESCRIBE EXTENDED user_events_stream;

-- Voir les données d'une table (pull query)
SELECT * FROM user_total_spent;
SELECT * FROM purchases_by_country;

-- Terminer une requête
TERMINATE <query_id>;

-- Supprimer un stream ou une table
DROP STREAM IF EXISTS stream_name DELETE TOPIC;
DROP TABLE IF EXISTS table_name DELETE TOPIC;

-- Afficher les messages d'un topic directement
PRINT 'user_events' FROM BEGINNING LIMIT 5;
