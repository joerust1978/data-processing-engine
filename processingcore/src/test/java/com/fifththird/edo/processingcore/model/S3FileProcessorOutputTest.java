package com.fifththird.edo.processingcore.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for S3FileProcessorOutput.
 * Tests all constructors, getters, setters, equals, hashCode, and toString methods.
 */
public class S3FileProcessorOutputTest {

    @Test
    public void testDefaultConstructor() {
        S3FileProcessorOutput output = new S3FileProcessorOutput();
        
        assertNotNull(output);
        assertNull(output.getOutputFile());
        assertNull(output.getTaskToken());
        assertNull(output.getTaskName());
    }

    @Test
    public void testConstructorWithOutputFile() {
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3FileProcessorOutput output = new S3FileProcessorOutput(outputFile);
        
        assertNotNull(output);
        assertEquals(outputFile, output.getOutputFile());
        assertNull(output.getTaskToken());
        assertNull(output.getTaskName());
    }

    @Test
    public void testConstructorWithAllFields() {
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String taskToken = "test-task-token";
        String taskName = "test-task-name";
        
        S3FileProcessorOutput output = new S3FileProcessorOutput(taskToken, taskName, outputFile);
        
        assertNotNull(output);
        assertEquals(outputFile, output.getOutputFile());
        assertEquals(taskToken, output.getTaskToken());
        assertEquals(taskName, output.getTaskName());
    }

    @Test
    public void testSetOutputFile() {
        S3FileProcessorOutput output = new S3FileProcessorOutput();
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        output.setOutputFile(outputFile);
        assertEquals(outputFile, output.getOutputFile());
    }

    @Test
    public void testSetTaskToken() {
        S3FileProcessorOutput output = new S3FileProcessorOutput();
        String taskToken = "test-task-token";
        
        output.setTaskToken(taskToken);
        assertEquals(taskToken, output.getTaskToken());
    }

    @Test
    public void testSetTaskName() {
        S3FileProcessorOutput output = new S3FileProcessorOutput();
        String taskName = "test-task-name";
        
        output.setTaskName(taskName);
        assertEquals(taskName, output.getTaskName());
    }

    @Test
    public void testEquals() {
        S3File outputFile1 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile2 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile3 = S3File.builder()
                .bucket("different-bucket")
                .fileKey("different-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3FileProcessorOutput output1 = new S3FileProcessorOutput("token1", "name1", outputFile1);
        S3FileProcessorOutput output2 = new S3FileProcessorOutput("token1", "name1", outputFile2);
        S3FileProcessorOutput output3 = new S3FileProcessorOutput("token2", "name1", outputFile1);
        S3FileProcessorOutput output4 = new S3FileProcessorOutput("token1", "name2", outputFile1);
        S3FileProcessorOutput output5 = new S3FileProcessorOutput("token1", "name1", outputFile3);
        
        // Same object
        assertEquals(output1, output1);
        
        // Equal objects
        assertEquals(output1, output2);
        
        // Different objects
        assertNotEquals(output1, output3); // different taskToken
        assertNotEquals(output1, output4); // different taskName
        assertNotEquals(output1, output5); // different outputFile
        
        // Null comparison
        assertNotEquals(null, output1);
        
        // Different class
        assertNotEquals(output1, "string");
    }

    @Test
    public void testHashCode() {
        S3File outputFile1 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile2 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        S3FileProcessorOutput output1 = new S3FileProcessorOutput("token1", "name1", outputFile1);
        S3FileProcessorOutput output2 = new S3FileProcessorOutput("token1", "name1", outputFile2);
        
        // Equal objects should have equal hash codes
        assertEquals(output1.hashCode(), output2.hashCode());
        
        // Same object should have same hash code
        assertEquals(output1.hashCode(), output1.hashCode());
    }

    @Test
    public void testToString() {
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("test-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3FileProcessorOutput output = new S3FileProcessorOutput("test-token", "test-name", outputFile);
        
        String result = output.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("S3FileProcessorOutput"));
        assertTrue(result.contains("taskToken='test-token'"));
        assertTrue(result.contains("taskName='test-name'"));
        assertTrue(result.contains("outputFile="));
    }

    @Test
    public void testToStringWithNullValues() {
        S3FileProcessorOutput output = new S3FileProcessorOutput();
        
        String result = output.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("S3FileProcessorOutput"));
        assertTrue(result.contains("taskToken='null'"));
        assertTrue(result.contains("taskName='null'"));
        assertTrue(result.contains("outputFile=null"));
    }
} 