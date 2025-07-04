package com.fifththird.edo.processingcore.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for S3FileProcessorDefinition.
 */
class S3FileProcessorDefinitionTest {

    @Test
    void testS3FileProcessorDefinitionNoArgsConstructor() {
        // Given & When
        S3FileProcessorDefinition definition = new S3FileProcessorDefinition();
        
        // Then
        assertNotNull(definition);
        assertNull(definition.getInputFile());
        assertNull(definition.getOutputFile());
    }

    @Test
    void testS3FileProcessorDefinitionAllArgsConstructor() {
        // Given
        S3File inputFile = S3File.builder()
                .bucket("input-bucket")
                .fileKey("input/file.txt")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3File outputFile = S3File.builder()
                .bucket("output-bucket")
                .fileKey("output/processed.txt")
                .gzipped(true)
                .pgpEncrypted(true)
                .build();
        
        // When
        S3FileProcessorDefinition definition = new S3FileProcessorDefinition(inputFile, outputFile);
        
        // Then
        assertNotNull(definition);
        assertEquals(inputFile, definition.getInputFile());
        assertEquals(outputFile, definition.getOutputFile());
    }

    @Test
    void testS3FileProcessorDefinitionSettersAndGetters() {
        // Given
        S3FileProcessorDefinition definition = new S3FileProcessorDefinition();
        S3File inputFile = S3File.builder()
                .bucket("input-bucket")
                .fileKey("input/file.txt")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3File outputFile = S3File.builder()
                .bucket("output-bucket")
                .fileKey("output/processed.txt")
                .gzipped(true)
                .pgpEncrypted(true)
                .build();
        
        // When
        definition.setInputFile(inputFile);
        definition.setOutputFile(outputFile);
        
        // Then
        assertEquals(inputFile, definition.getInputFile());
        assertEquals(outputFile, definition.getOutputFile());
    }

    @Test
    void testS3FileProcessorDefinitionEqualsAndHashCode() {
        // Given
        S3File inputFile1 = S3File.builder()
                .bucket("input-bucket")
                .fileKey("input/file.txt")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3File outputFile1 = S3File.builder()
                .bucket("output-bucket")
                .fileKey("output/processed.txt")
                .gzipped(true)
                .pgpEncrypted(true)
                .build();
        
        S3FileProcessorDefinition definition1 = new S3FileProcessorDefinition(inputFile1, outputFile1);
        S3FileProcessorDefinition definition2 = new S3FileProcessorDefinition(inputFile1, outputFile1);
        // Set taskToken and taskName to the same values
        definition1.setTaskToken("token");
        definition2.setTaskToken("token");
        definition1.setTaskName("name");
        definition2.setTaskName("name");
        
        S3File inputFile2 = S3File.builder()
                .bucket("different-bucket")
                .fileKey("input/file.txt")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3FileProcessorDefinition definition3 = new S3FileProcessorDefinition(inputFile2, outputFile1);
        
        // Then
        assertEquals(definition1.toString(), definition2.toString());
        assertEquals(definition1.hashCode(), definition2.hashCode());
        assertNotEquals(definition1, definition3);
        assertNotEquals(definition1.hashCode(), definition3.hashCode());
    }

    @Test
    void testS3FileProcessorDefinitionToString() {
        // Given
        S3File inputFile = S3File.builder()
                .bucket("input-bucket")
                .fileKey("input/file.txt")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3File outputFile = S3File.builder()
                .bucket("output-bucket")
                .fileKey("output/processed.txt")
                .gzipped(true)
                .pgpEncrypted(true)
                .build();
        
        S3FileProcessorDefinition definition = new S3FileProcessorDefinition(inputFile, outputFile);
        
        // When
        String toString = definition.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("S3FileProcessorDefinition"));
        assertTrue(toString.contains("inputFile="));
        assertTrue(toString.contains("outputFile="));
        assertTrue(toString.contains("input-bucket"));
        assertTrue(toString.contains("output-bucket"));
    }

    @Test
    void testS3FileProcessorDefinitionWithNullValues() {
        // Given
        S3FileProcessorDefinition definition = new S3FileProcessorDefinition(null, null);
        
        // Then
        assertNotNull(definition);
        assertNull(definition.getInputFile());
        assertNull(definition.getOutputFile());
        
        // When
        definition.setInputFile(null);
        definition.setOutputFile(null);
        
        // Then
        assertNull(definition.getInputFile());
        assertNull(definition.getOutputFile());
    }
} 