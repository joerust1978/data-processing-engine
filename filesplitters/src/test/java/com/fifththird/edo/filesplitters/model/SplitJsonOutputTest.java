package com.fifththird.edo.filesplitters.model;

import com.fifththird.edo.processingcore.model.S3File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SplitJsonOutput model.
 */
class SplitJsonOutputTest {

    @Test
    void testDefaultConstructor() {
        SplitJsonOutput output = new SplitJsonOutput();
        
        assertNull(output.getOutputFileData());
        assertNull(output.getStateFileData());
        assertNull(output.getRecordCount());
    }

    @Test
    void testParameterizedConstructor() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 1000L;
        
        SplitJsonOutput output = new SplitJsonOutput(outputFileData, stateFileData, recordCount);
        
        assertEquals(outputFileData, output.getOutputFileData());
        assertEquals(stateFileData, output.getStateFileData());
        assertEquals(recordCount, output.getRecordCount());
    }

    @Test
    void testSettersAndGetters() {
        SplitJsonOutput output = new SplitJsonOutput();
        
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(true)
                .pgpEncrypted(true)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/files/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 500L;
        
        output.setOutputFileData(outputFileData);
        output.setStateFileData(stateFileData);
        output.setRecordCount(recordCount);
        
        assertEquals(outputFileData, output.getOutputFileData());
        assertEquals(stateFileData, output.getStateFileData());
        assertEquals(recordCount, output.getRecordCount());
    }

    @Test
    void testEquals_SameObject() {
        SplitJsonOutput output = new SplitJsonOutput();
        assertEquals(output, output);
    }

    @Test
    void testEquals_NullObject() {
        SplitJsonOutput output = new SplitJsonOutput();
        assertNotEquals(null, output);
    }

    @Test
    void testEquals_DifferentClass() {
        SplitJsonOutput output = new SplitJsonOutput();
        assertNotEquals("string", output);
    }

    @Test
    void testEquals_EqualObjects() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(outputFileData, stateFileData, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(outputFileData, stateFileData, recordCount);
        
        assertEquals(output1, output2);
    }

    @Test
    void testEquals_DifferentS3File() {
        S3File outputFileData1 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output1.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFileData2 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output2.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(outputFileData1, stateFileData, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(outputFileData2, stateFileData, recordCount);
        
        assertNotEquals(output1, output2);
    }

    @Test
    void testEquals_DifferentStateFileKeyPrefix() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData1 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix1/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData2 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix2/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(outputFileData, stateFileData1, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(outputFileData, stateFileData2, recordCount);
        
        assertNotEquals(output1, output2);
    }

    @Test
    void testEquals_DifferentRecordCount() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount1 = 1000L;
        Long recordCount2 = 2000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(outputFileData, stateFileData, recordCount1);
        SplitJsonOutput output2 = new SplitJsonOutput(outputFileData, stateFileData, recordCount2);
        
        assertNotEquals(output1, output2);
    }

    @Test
    void testEquals_NullFields() {
        SplitJsonOutput output1 = new SplitJsonOutput();
        SplitJsonOutput output2 = new SplitJsonOutput();
        
        assertEquals(output1, output2);
    }

    @Test
    void testEquals_MixedNullFields() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        SplitJsonOutput output1 = new SplitJsonOutput(outputFileData, null, null);
        SplitJsonOutput output2 = new SplitJsonOutput(outputFileData, null, null);
        
        assertEquals(output1, output2);
    }

    @Test
    void testHashCode_EqualObjects() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(outputFileData, stateFileData, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(outputFileData, stateFileData, recordCount);
        
        assertEquals(output1.hashCode(), output2.hashCode());
    }

    @Test
    void testHashCode_NullFields() {
        SplitJsonOutput output1 = new SplitJsonOutput();
        SplitJsonOutput output2 = new SplitJsonOutput();
        
        assertEquals(output1.hashCode(), output2.hashCode());
    }

    @Test
    void testToString() {
        S3File outputFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File stateFileData = S3File.builder()
                .bucket("test-bucket")
                .fileKey("state/prefix/")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        Long recordCount = 1000L;
        
        SplitJsonOutput output = new SplitJsonOutput(outputFileData, stateFileData, recordCount);
        String result = output.toString();
        
        assertTrue(result.contains("SplitJsonOutput"));
        assertTrue(result.contains("outputFileData=" + outputFileData.toString()));
        assertTrue(result.contains("stateFileData=" + stateFileData.toString()));
        assertTrue(result.contains("recordCount=1000"));
    }

    @Test
    void testToString_NullFields() {
        SplitJsonOutput output = new SplitJsonOutput();
        String result = output.toString();
        
        assertTrue(result.contains("SplitJsonOutput"));
        assertTrue(result.contains("outputFileData=null"));
        assertTrue(result.contains("stateFileData=null"));
        assertTrue(result.contains("recordCount=null"));
    }
} 