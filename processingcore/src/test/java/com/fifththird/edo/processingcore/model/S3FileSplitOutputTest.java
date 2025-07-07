package com.fifththird.edo.processingcore.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for S3FileSplitOutput.
 * Tests all constructors, getters, setters, equals, hashCode, and toString methods.
 */
public class S3FileSplitOutputTest {

    @Test
    public void testDefaultConstructor() {
        S3FileSplitOutput output = new S3FileSplitOutput();
        assertNotNull(output);
        assertNull(output.getOutputFile());
        assertEquals(0, output.getTotalRecordsProcessed());
        assertEquals(0, output.getTotalFileFragments());
        assertNull(output.getSplitMetadata());
    }

    @Test
    public void testConstructorWithOutputFile() {
        S3File outputFile = S3File.builder()
                .bucket("bucket")
                .fileKey("key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3FileSplitOutput output = new S3FileSplitOutput(outputFile);
        assertEquals(outputFile, output.getOutputFile());
    }

    @Test
    public void testConstructorWithAllFields() {
        S3File outputFile = S3File.builder().bucket("bucket").fileKey("key").gzipped(false).pgpEncrypted(false).build();
        S3File splitMetadata = S3File.builder().bucket("meta-bucket").fileKey("meta-key").gzipped(true).pgpEncrypted(true).build();
        long totalRecords = 1234L;
        long totalFragments = 12L;
        S3FileSplitOutput output = new S3FileSplitOutput(outputFile, totalRecords, totalFragments, splitMetadata);
        assertEquals(outputFile, output.getOutputFile());
        assertEquals(totalRecords, output.getTotalRecordsProcessed());
        assertEquals(totalFragments, output.getTotalFileFragments());
        assertEquals(splitMetadata, output.getSplitMetadata());
    }

    @Test
    public void testSettersAndGetters() {
        S3FileSplitOutput output = new S3FileSplitOutput();
        S3File outputFile = S3File.builder().bucket("bucket").fileKey("key").gzipped(false).pgpEncrypted(false).build();
        S3File splitMetadata = S3File.builder().bucket("meta-bucket").fileKey("meta-key").gzipped(true).pgpEncrypted(true).build();
        output.setOutputFile(outputFile);
        output.setTotalRecordsProcessed(100L);
        output.setTotalFileFragments(5L);
        output.setSplitMetadata(splitMetadata);
        assertEquals(outputFile, output.getOutputFile());
        assertEquals(100L, output.getTotalRecordsProcessed());
        assertEquals(5L, output.getTotalFileFragments());
        assertEquals(splitMetadata, output.getSplitMetadata());
    }

    @Test
    public void testEqualsAndHashCode() {
        S3File outputFile1 = S3File.builder().bucket("bucket").fileKey("key").gzipped(false).pgpEncrypted(false).build();
        S3File splitMetadata1 = S3File.builder().bucket("meta-bucket").fileKey("meta-key").gzipped(true).pgpEncrypted(true).build();
        S3FileSplitOutput output1 = new S3FileSplitOutput(outputFile1, 100L, 5L, splitMetadata1);
        S3FileSplitOutput output2 = new S3FileSplitOutput(outputFile1, 100L, 5L, splitMetadata1);
        S3FileSplitOutput output3 = new S3FileSplitOutput(outputFile1, 200L, 5L, splitMetadata1);
        assertEquals(output1, output2);
        assertEquals(output1.hashCode(), output2.hashCode());
        assertNotEquals(output1, output3);
        assertNotEquals(output1.hashCode(), output3.hashCode());
        assertNotEquals(output1, null);
        assertNotEquals(output1, "string");
    }

    @Test
    public void testToString() {
        S3File outputFile = S3File.builder().bucket("bucket").fileKey("key").gzipped(false).pgpEncrypted(false).build();
        S3File splitMetadata = S3File.builder().bucket("meta-bucket").fileKey("meta-key").gzipped(true).pgpEncrypted(true).build();
        S3FileSplitOutput output = new S3FileSplitOutput(outputFile, 100L, 5L, splitMetadata);
        String str = output.toString();
        assertNotNull(str);
        assertTrue(str.contains("S3FileSplitOutput"));
        assertTrue(str.contains("outputFile="));
        assertTrue(str.contains("totalRecordsProcessed=100"));
        assertTrue(str.contains("totalFileFragments=5"));
        assertTrue(str.contains("splitMetadata="));
    }
} 