-- Script d'initialisation de la base de données PostgreSQL

-- Table source avec des données d'exemple
CREATE TABLE IF NOT EXISTS source_data (
    id SERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table de destination (pour le sink)
CREATE TABLE IF NOT EXISTS sink_data (
    id INT PRIMARY KEY,
    message TEXT NOT NULL,
    created_at TIMESTAMP
);

-- Insertion de données d'exemple dans la table source
INSERT INTO source_data (message) VALUES
    ('Hello Kafka Connect!'),
    ('This is message number 2'),
    ('Kafka Connect is awesome'),
    ('Learning Kafka is fun'),
    ('Data integration made easy');

-- Afficher les tables créées
\dt

-- Afficher les données de la table source
SELECT * FROM source_data;
