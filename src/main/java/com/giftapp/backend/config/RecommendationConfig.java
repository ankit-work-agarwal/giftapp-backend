package com.giftapp.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "recommendation")
@Data
public class RecommendationConfig {
    private int genderWeight;
    private int unisexWeight;
    private int occasionWeight;
    private int ageWeight;
    private int budgetWeight;
    private int colourWeight;
    private int minScore;
    private int tagWeight;
}