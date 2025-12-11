package com.kafka.training.queryapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.kafka.training.queryapi",
        "com.kafka.training.common"
})
public class QueryApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(QueryApiApplication.class, args);
    }
}
