package com.oratge.oratgeback.adapter.out.aemet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aemet.api")
public record AemetApiProperties(String baseUrl, String apiKey) {
}
