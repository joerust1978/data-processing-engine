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
        
        S3FileProcessorOutput output1 = new S3FileProcessorOutput(outputFile1);
        S3FileProcessorOutput output2 = new S3FileProcessorOutput(outputFile2);
        S3FileProcessorOutput output3 = new S3FileProcessorOutput(outputFile3);
        
        // Same object
        assertEquals(output1, output1);
        
        // Equal objects
        assertEquals(output1, output2);
        
        // Different objects
        assertNotEquals(output1, output3); // different outputFile
        
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
        
        S3FileProcessorOutput output1 = new S3FileProcessorOutput(outputFile1);
        S3FileProcessorOutput output2 = new S3FileProcessorOutput(outputFile2);
        
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
        S3FileProcessorOutput output = new S3FileProcessorOutput(outputFile);
        
        String result = output.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("S3FileProcessorOutput"));
        assertTrue(result.contains("outputFile="));
    }

    @Test
    public void testToStringWithNullValues() {
        S3FileProcessorOutput output = new S3FileProcessorOutput();
        
        String result = output.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("S3FileProcessorOutput"));
        assertTrue(result.contains("outputFile=null"));
    }
} 