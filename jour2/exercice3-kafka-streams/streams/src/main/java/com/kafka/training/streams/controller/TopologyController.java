package com.kafka.training.streams.controller;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.TopologyDescription;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/topology")
public class TopologyController {

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    public TopologyController(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    @GetMapping
    public Map<String, Object> getTopology() {
        Map<String, Object> response = new HashMap<>();

        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

            if (kafkaStreams != null) {
                // Topologie en format texte
                TopologyDescription topology = kafkaStreams.localThreadsMetadata()
                        .stream()
                        .findFirst()
                        .map(metadata -> streamsBuilderFactoryBean.getTopology().describe())
                        .orElse(null);

                if (topology != null) {
                    response.put("topology", topology.toString());
                    response.put("subtopologies", topology.subtopologies().size());
                } else {
                    response.put("topology", "Topology not available yet");
                }

                // État du stream
                response.put("state", kafkaStreams.state().toString());

                // Métadonnées des threads
                response.put("threads", kafkaStreams.localThreadsMetadata().size());

            } else {
                response.put("error", "KafkaStreams not initialized");
            }

        } catch (Exception e) {
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/describe")
    public String describeTopology() {
        try {
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();

            if (kafkaStreams != null) {
                return streamsBuilderFactoryBean.getTopology().describe().toString();
            } else {
                return "KafkaStreams not initialized";
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
