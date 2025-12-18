package com.common.jane_ai.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "com.jane.bloom")
@Component
@Data
public class BloomFilterProperties {

    private String bloomFilterName;

    private int expectedInsertions;

    private double falseProbability;
}
