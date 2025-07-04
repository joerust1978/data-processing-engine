package com.fifththird.edo.processingcore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sfn.SfnClient;

/**
 * Main Spring Boot application class for the Processing Core module.
 * This application provides core processing utilities and services.
 */
@SpringBootApplication
public class ProcessingCoreApplication {

    @Value("${aws.region:us-east-2}")
    private String awsRegion;

    public static void main(String[] args) {
        SpringApplication.run(ProcessingCoreApplication.class, args);
    }

    /**
     * Creates and configures the AWS Step Functions client.
     * This client can be used to interact with AWS Step Functions service.
     *
     * @return configured SfnClient instance
     */
    @Bean
    public SfnClient sfnClient() {
        return SfnClient.builder()
                .region(Region.of(awsRegion))
                .build();
    }
} 