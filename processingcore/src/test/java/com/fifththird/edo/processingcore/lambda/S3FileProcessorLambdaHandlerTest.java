package com.fifththird.edo.processingcore.lambda;

import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.model.S3File;
import com.fifththird.edo.processingcore.model.S3FileProcessorDefinition;
import com.fifththird.edo.processingcore.model.S3FileProcessorOutput;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for S3FileProcessorLambdaHandler.
 * Tests S3 object retrieval, gzip decompression, PGP decryption, and error handling.
 */
@ExtendWith(MockitoExtension.class)
public class S3FileProcessorLambdaHandlerTest {

    @Mock
    private SfnClient sfnClient;
    
    @Mock
    private S3Client s3Client;
    
    @Mock
    private PgpUtilities pgpUtilities;
    
    private TestS3FileProcessorLambdaHandler handler;
    
    private S3FileProcessorDefinition testInput;
    private S3File inputFile;
    private S3File outputFile;

    /**
     * Concrete implementation of S3FileProcessorLambdaHandler for testing.
     */
    private static class TestS3FileProcessorLambdaHandler 
            extends S3FileProcessorLambdaHandler<S3FileProcessorDefinition, S3FileProcessorOutput> {
        
        public TestS3FileProcessorLambdaHandler(SfnClient sfnClient, S3Client s3Client, PgpUtilities pgpUtilities) {
            super(sfnClient, s3Client, pgpUtilities);
        }
        
        @Override
        protected Class<S3FileProcessorDefinition> getInputType() {
            return S3FileProcessorDefinition.class;
        }
        
        @Override
        protected S3FileProcessorOutput processS3Object(S3FileProcessorDefinition input, InputStream s3ObjectStream) 
                throws DataProcessingException {
            // Simple implementation for testing
            try {
                // Read a small amount to verify stream works
                byte[] buffer = new byte[1024];
                int bytesRead = s3ObjectStream.read(buffer);
                s3ObjectStream.close();
                
                return new S3FileProcessorOutput(input.getOutputFile());
            } catch (IOException e) {
                throw new DataProcessingException("Failed to process S3 object", e);
            }
        }
    }

    @BeforeEach
    public void setUp() {
        handler = new TestS3FileProcessorLambdaHandler(sfnClient, s3Client, pgpUtilities);
        
        // Set up configuration values
        ReflectionTestUtils.setField(handler, "pgpPrivateKey", "test-private-key");
        ReflectionTestUtils.setField(handler, "pgpPassphrase", "test-passphrase");
        
        // Set up test data
        inputFile = new S3File("test-bucket", "test-input-key", false, false);
        outputFile = new S3File("test-bucket", "test-output-key", false, false);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
    }

    @Test
    public void testProcessS3File_Success() throws Exception {
        // Arrange
        String testData = "test data content";
        ByteArrayInputStream testStream = new ByteArrayInputStream(testData.getBytes());
        
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(), testStream));
        
        // Act
        S3FileProcessorOutput result = handler.processS3File(testInput);
        
        // Assert
        assertNotNull(result);
        assertEquals(outputFile, result.getOutputFile());
        
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void testProcessS3File_WithGzippedInput() throws Exception {
        // Arrange
        inputFile = new S3File("test-bucket", "test-input-key", true, false);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
        
        // Use real gzipped data
        String testData = "test gzipped data content";
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(byteStream);
        gzipOut.write(testData.getBytes());
        gzipOut.close();
        ByteArrayInputStream testStream = new ByteArrayInputStream(byteStream.toByteArray());
        
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(), testStream));
        
        // Act
        S3FileProcessorOutput result = handler.processS3File(testInput);
        
        // Assert
        assertNotNull(result);
        assertEquals(outputFile, result.getOutputFile());
        
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void testProcessS3File_WithPgpEncryptedInput() throws Exception {
        // Arrange
        inputFile = new S3File("test-bucket", "test-input-key", false, true);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
        
        String testData = "test encrypted data content";
        ByteArrayInputStream testStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayInputStream decryptedStream = new ByteArrayInputStream("decrypted data".getBytes());
        
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(), testStream));
        when(pgpUtilities.wrapWithDecryption(any(InputStream.class), anyString(), anyString()))
                .thenReturn(decryptedStream);
        
        // Act
        S3FileProcessorOutput result = handler.processS3File(testInput);
        
        // Assert
        assertNotNull(result);
        assertEquals(outputFile, result.getOutputFile());
        
        verify(s3Client).getObject(any(GetObjectRequest.class));
        verify(pgpUtilities).wrapWithDecryption(any(InputStream.class), eq("test-private-key"), eq("test-passphrase"));
    }

    @Test
    public void testProcessS3File_WithGzippedAndPgpEncryptedInput() throws Exception {
        // Arrange
        inputFile = new S3File("test-bucket", "test-input-key", true, true);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
        
        // Use real gzipped data for the encrypted input
        String testData = "test gzipped and encrypted data content";
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        GZIPOutputStream gzipOut = new GZIPOutputStream(byteStream);
        gzipOut.write(testData.getBytes());
        gzipOut.close();
        ByteArrayInputStream gzippedStream = new ByteArrayInputStream(byteStream.toByteArray());
        ByteArrayInputStream decryptedStream = new ByteArrayInputStream("decrypted data".getBytes());
        
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(), gzippedStream));
        when(pgpUtilities.wrapWithDecryption(any(InputStream.class), anyString(), anyString()))
                .thenReturn(decryptedStream);
        
        // Act
        S3FileProcessorOutput result = handler.processS3File(testInput);
        
        // Assert
        assertNotNull(result);
        assertEquals(outputFile, result.getOutputFile());
        
        verify(s3Client).getObject(any(GetObjectRequest.class));
        verify(pgpUtilities).wrapWithDecryption(any(InputStream.class), eq("test-private-key"), eq("test-passphrase"));
    }

    @Test
    public void testProcessS3File_WithNullInput() {
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(null);
        });
        assertEquals("Input cannot be null", exception.getMessage());
    }

    @Test
    public void testProcessS3File_WithNullInputFile() {
        // Arrange
        testInput = new S3FileProcessorDefinition("test-token", "test-task", null, outputFile);
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(testInput);
        });
        assertEquals("Input file is required", exception.getMessage());
    }

    @Test
    public void testProcessS3File_WithNullOutputFile() {
        // Arrange
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, null);
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(testInput);
        });
        assertEquals("Output file is required", exception.getMessage());
    }

    @Test
    public void testProcessS3File_WithS3Exception() {
        // Arrange
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(testInput);
        });
        
        assertTrue(exception.getMessage().contains("Failed to retrieve S3 object"));
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void testProcessS3File_WithPgpDecryptionFailure() throws Exception {
        // Arrange
        inputFile = new S3File("test-bucket", "test-input-key", false, true);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
        
        String testData = "test encrypted data content";
        ByteArrayInputStream testStream = new ByteArrayInputStream(testData.getBytes());
        
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(), testStream));
        when(pgpUtilities.wrapWithDecryption(any(InputStream.class), anyString(), anyString()))
                .thenThrow(new IOException("PGP decryption failed"));
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(testInput);
        });
        
        assertEquals("Failed to process S3 object", exception.getMessage());
        verify(s3Client).getObject(any(GetObjectRequest.class));
        verify(pgpUtilities).wrapWithDecryption(any(InputStream.class), eq("test-private-key"), eq("test-passphrase"));
    }

    @Test
    public void testProcessS3File_WithMissingPgpPrivateKey() throws Exception {
        // Arrange
        inputFile = new S3File("test-bucket", "test-input-key", false, true);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
        
        ReflectionTestUtils.setField(handler, "pgpPrivateKey", "");
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(testInput);
        });
        assertEquals("pgp.private.key configuration is required for PGP decryption", exception.getMessage());
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
        verify(pgpUtilities, never()).wrapWithDecryption(any(), anyString(), anyString());
    }

    @Test
    public void testProcessS3File_WithMissingPgpPassphrase() throws Exception {
        // Arrange
        inputFile = new S3File("test-bucket", "test-input-key", false, true);
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, outputFile);
        
        ReflectionTestUtils.setField(handler, "pgpPassphrase", "");
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processS3File(testInput);
        });
        assertEquals("pgp.passphrase configuration is required for PGP decryption", exception.getMessage());
        verify(s3Client, never()).getObject(any(GetObjectRequest.class));
        verify(pgpUtilities, never()).wrapWithDecryption(any(), anyString(), anyString());
    }

    @Test
    public void testProcessSqsTypeInternal_Success() throws Exception {
        // Arrange
        String testData = "test data content";
        ByteArrayInputStream testStream = new ByteArrayInputStream(testData.getBytes());
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(), testStream));
        
        // Act
        S3FileProcessorOutput result = handler.processSqsTypeInternal(testInput);
        
        // Assert
        assertNotNull(result);
        assertEquals(outputFile, result.getOutputFile());
        
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void testProcessSqsTypeInternal_WithNullInput() {
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processSqsTypeInternal(null);
        });
        assertEquals("Failed to process S3 file operation", exception.getMessage());
    }

    @Test
    public void testProcessSqsTypeInternal_WithNullInputFile() {
        // Arrange
        testInput = new S3FileProcessorDefinition("test-token", "test-task", null, outputFile);
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processSqsTypeInternal(testInput);
        });
        assertEquals("Failed to process S3 file operation", exception.getMessage());
    }

    @Test
    public void testProcessSqsTypeInternal_WithNullOutputFile() {
        // Arrange
        testInput = new S3FileProcessorDefinition("test-token", "test-task", inputFile, null);
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processSqsTypeInternal(testInput);
        });
        assertEquals("Failed to process S3 file operation", exception.getMessage());
    }

    @Test
    public void testProcessSqsTypeInternal_WithProcessingException() throws Exception {
        // Arrange
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());
        
        // Act & Assert
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processSqsTypeInternal(testInput);
        });
        
        assertEquals("Failed to process S3 file operation", exception.getMessage());
        verify(s3Client).getObject(any(GetObjectRequest.class));
    }

    @Test
    public void testConstructor() {
        // Act
        TestS3FileProcessorLambdaHandler newHandler = new TestS3FileProcessorLambdaHandler(sfnClient, s3Client, pgpUtilities);
        
        // Assert
        assertNotNull(newHandler);
    }
} 