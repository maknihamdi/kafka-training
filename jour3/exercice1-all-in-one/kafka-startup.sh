#!/bin/bash

# Script de démarrage personnalisé pour Kafka avec JMX Exporter
# Ce script charge le JMX exporter uniquement pour le processus broker Kafka

export KAFKA_OPTS="-javaagent:/usr/share/jmx_exporter/jmx_prometheus_javaagent-0.20.0.jar=7071:/usr/share/jmx_exporter/kafka-jmx-config.yml"

# Démarrer Kafka normalement
exec /etc/confluent/docker/run
