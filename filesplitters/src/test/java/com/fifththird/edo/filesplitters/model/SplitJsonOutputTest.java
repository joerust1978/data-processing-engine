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
        
        assertNull(output.getS3File());
        assertNull(output.getStateFileKeyPrefix());
        assertNull(output.getRecordCount());
    }

    @Test
    void testParameterizedConstructor() {
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix = "state/prefix/";
        Long recordCount = 1000L;
        
        SplitJsonOutput output = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount);
        
        assertEquals(s3File, output.getS3File());
        assertEquals(stateFileKeyPrefix, output.getStateFileKeyPrefix());
        assertEquals(recordCount, output.getRecordCount());
    }

    @Test
    void testSettersAndGetters() {
        SplitJsonOutput output = new SplitJsonOutput();
        
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(true)
                .pgpEncrypted(true)
                .build();
        String stateFileKeyPrefix = "state/files/";
        Long recordCount = 500L;
        
        output.setS3File(s3File);
        output.setStateFileKeyPrefix(stateFileKeyPrefix);
        output.setRecordCount(recordCount);
        
        assertEquals(s3File, output.getS3File());
        assertEquals(stateFileKeyPrefix, output.getStateFileKeyPrefix());
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
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix = "state/prefix/";
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount);
        
        assertEquals(output1, output2);
    }

    @Test
    void testEquals_DifferentS3File() {
        S3File s3File1 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output1.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File s3File2 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output2.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix = "state/prefix/";
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(s3File1, stateFileKeyPrefix, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(s3File2, stateFileKeyPrefix, recordCount);
        
        assertNotEquals(output1, output2);
    }

    @Test
    void testEquals_DifferentStateFileKeyPrefix() {
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix1 = "state/prefix1/";
        String stateFileKeyPrefix2 = "state/prefix2/";
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(s3File, stateFileKeyPrefix1, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(s3File, stateFileKeyPrefix2, recordCount);
        
        assertNotEquals(output1, output2);
    }

    @Test
    void testEquals_DifferentRecordCount() {
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix = "state/prefix/";
        Long recordCount1 = 1000L;
        Long recordCount2 = 2000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount1);
        SplitJsonOutput output2 = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount2);
        
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
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        SplitJsonOutput output1 = new SplitJsonOutput(s3File, null, null);
        SplitJsonOutput output2 = new SplitJsonOutput(s3File, null, null);
        
        assertEquals(output1, output2);
    }

    @Test
    void testHashCode_EqualObjects() {
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix = "state/prefix/";
        Long recordCount = 1000L;
        
        SplitJsonOutput output1 = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount);
        SplitJsonOutput output2 = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount);
        
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
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        String stateFileKeyPrefix = "state/prefix/";
        Long recordCount = 1000L;
        
        SplitJsonOutput output = new SplitJsonOutput(s3File, stateFileKeyPrefix, recordCount);
        String result = output.toString();
        
        assertTrue(result.contains("SplitJsonOutput"));
        assertTrue(result.contains("s3File=" + s3File.toString()));
        assertTrue(result.contains("stateFileKeyPrefix='state/prefix/'"));
        assertTrue(result.contains("recordCount=1000"));
    }

    @Test
    void testToString_NullFields() {
        SplitJsonOutput output = new SplitJsonOutput();
        String result = output.toString();
        
        assertTrue(result.contains("SplitJsonOutput"));
        assertTrue(result.contains("s3File=null"));
        assertTrue(result.contains("stateFileKeyPrefix='null'"));
        assertTrue(result.contains("recordCount=null"));
    }
} 