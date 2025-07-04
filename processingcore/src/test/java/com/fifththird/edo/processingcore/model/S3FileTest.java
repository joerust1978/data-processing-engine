package com.fifththird.edo.processingcore.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for S3File model.
 */
class S3FileTest {

    @Test
    void testS3FileBuilder() {
        // Given
        String bucket = "test-bucket";
        String fileKey = "path/to/file.txt";
        
        // When
        S3File s3File = S3File.builder()
                .bucket(bucket)
                .fileKey(fileKey)
                .gzipped(true)
                .pgpEncrypted(false)
                .build();
        
        // Then
        assertNotNull(s3File);
        assertEquals(bucket, s3File.getBucket());
        assertEquals(fileKey, s3File.getFileKey());
        assertTrue(s3File.isGzipped());
        assertFalse(s3File.isPgpEncrypted());
    }

    @Test
    void testS3FileNoArgsConstructor() {
        // Given & When
        S3File s3File = new S3File();
        
        // Then
        assertNotNull(s3File);
        assertNull(s3File.getBucket());
        assertNull(s3File.getFileKey());
        assertFalse(s3File.isGzipped());
        assertFalse(s3File.isPgpEncrypted());
    }

    @Test
    void testS3FileAllArgsConstructor() {
        // Given
        String bucket = "test-bucket";
        String fileKey = "path/to/file.txt";
        boolean gzipped = true;
        boolean pgpEncrypted = true;
        
        // When
        S3File s3File = new S3File(bucket, fileKey, gzipped, pgpEncrypted);
        
        // Then
        assertNotNull(s3File);
        assertEquals(bucket, s3File.getBucket());
        assertEquals(fileKey, s3File.getFileKey());
        assertEquals(gzipped, s3File.isGzipped());
        assertEquals(pgpEncrypted, s3File.isPgpEncrypted());
    }

    @Test
    void testS3FileSettersAndGetters() {
        // Given
        S3File s3File = new S3File();
        String bucket = "test-bucket";
        String fileKey = "path/to/file.txt";
        
        // When
        s3File.setBucket(bucket);
        s3File.setFileKey(fileKey);
        s3File.setGzipped(true);
        s3File.setPgpEncrypted(true);
        
        // Then
        assertEquals(bucket, s3File.getBucket());
        assertEquals(fileKey, s3File.getFileKey());
        assertTrue(s3File.isGzipped());
        assertTrue(s3File.isPgpEncrypted());
    }

    @Test
    void testS3FileEqualsAndHashCode() {
        // Given
        S3File s3File1 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("path/to/file.txt")
                .gzipped(true)
                .pgpEncrypted(false)
                .build();
        
        S3File s3File2 = S3File.builder()
                .bucket("test-bucket")
                .fileKey("path/to/file.txt")
                .gzipped(true)
                .pgpEncrypted(false)
                .build();
        
        S3File s3File3 = S3File.builder()
                .bucket("different-bucket")
                .fileKey("path/to/file.txt")
                .gzipped(true)
                .pgpEncrypted(false)
                .build();
        
        // Then
        assertEquals(s3File1, s3File2);
        assertEquals(s3File1.hashCode(), s3File2.hashCode());
        assertNotEquals(s3File1, s3File3);
        assertNotEquals(s3File1.hashCode(), s3File3.hashCode());
    }

    @Test
    void testS3FileToString() {
        // Given
        S3File s3File = S3File.builder()
                .bucket("test-bucket")
                .fileKey("path/to/file.txt")
                .gzipped(true)
                .pgpEncrypted(false)
                .build();
        
        // When
        String toString = s3File.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test-bucket"));
        assertTrue(toString.contains("path/to/file.txt"));
        assertTrue(toString.contains("gzipped=true"));
        assertTrue(toString.contains("pgpEncrypted=false"));
    }
} 