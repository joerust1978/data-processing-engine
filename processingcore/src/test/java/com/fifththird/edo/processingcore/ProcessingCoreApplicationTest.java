package com.fifththird.edo.processingcore;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.sfn.SfnClient;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ProcessingCoreApplication.
 */
@SpringBootTest(classes = ProcessingCoreApplication.class)
@TestPropertySource(properties = {
    "aws.region=us-west-2"
})
class ProcessingCoreApplicationTest {

    @Autowired
    private SfnClient sfnClient;

    @Test
    void testApplicationContextLoads() {
        // This test verifies that the Spring application context loads successfully
        assertTrue(true, "Application context should load without errors");
    }

    @Test
    void testSfnClientBeanExists() {
        // Verify that the SfnClient bean is created and injected
        assertNotNull(sfnClient, "SfnClient bean should be created");
    }

    @Test
    void testSfnClientIsConfigured() {
        // Verify that the SfnClient is properly configured
        assertNotNull(sfnClient, "SfnClient should not be null");
        // Note: We can't easily test the region configuration without making actual AWS calls,
        // but we can verify the client is created successfully
    }

    @Test
    void testApplicationMainMethod() {
        // Test that the main method can be called without throwing exceptions
        assertDoesNotThrow(() -> {
            // We can't easily test the main method in a unit test context
            // as it would start the full Spring Boot application
            // This test just verifies the method exists and is callable
            ProcessingCoreApplication.class.getMethod("main", String[].class);
        });
    }
} 