package com.fifththird.edo.processingcore.lambda;

import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.model.S3File;
import com.fifththird.edo.processingcore.model.S3FileProcessorOutput;
import com.fifththird.edo.processingcore.model.S3FileSplitInput;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for S3FileSplittingProcessorLambdaHandler.
 * Tests the functionality of the file splitting processor lambda handler including
 * constructors, processFragment method, and core logic.
 */
@ExtendWith(MockitoExtension.class)
class S3FileSplittingProcessorLambdaTests {

    @Mock
    private SfnClient sfnClient;
    
    @Mock
    private S3Client s3Client;
    
    @Mock
    private PgpUtilities pgpUtilities;
    
    @Mock
    private S3TransferManager transferManager;

    private TestS3FileSplittingProcessorLambdaHandler handler;
    private S3FileSplitInput testInput;
    private S3File testInputFile;
    private S3File testOutputFile;

    /**
     * Concrete implementation of S3FileSplittingProcessorLambdaHandler for testing.
     */
    private static class TestS3FileSplittingProcessorLambdaHandler 
            extends S3FileSplittingProcessorLambdaHandler<S3FileSplitInput, S3FileProcessorOutput> {

        public TestS3FileSplittingProcessorLambdaHandler(SfnClient sfnClient, S3Client s3Client, 
                                                        PgpUtilities pgpUtilities, ExecutorService executorService, 
                                                        S3TransferManager transferManager) {
            super(sfnClient, s3Client, pgpUtilities, executorService, transferManager);
        }

        @Override
        protected Integer processFragment(S3FileSplitInput input, InputStream sourceStream, 
                                        OutputStream fragmentStream, long recordsToRead) throws DataProcessingException {
            try {
                // Simulate processing by reading from source and writing to fragment
                byte[] buffer = new byte[1024];
                int bytesRead;
                int totalBytes = 0;
                int recordsProcessed = 0;
                
                while ((bytesRead = sourceStream.read(buffer)) != -1 && 
                       (recordsToRead <= 0 || recordsProcessed < recordsToRead)) {
                    fragmentStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                    recordsProcessed += bytesRead / 100; // Simulate record counting
                }
                
                return recordsProcessed;
            } catch (IOException e) {
                throw new DataProcessingException("Failed to process fragment", e);
            }
        }

        @Override
        protected Class<S3FileSplitInput> getInputType() {
            return S3FileSplitInput.class;
        }

        @Override
        protected S3FileProcessorOutput processSqsTypeInternal(S3FileSplitInput input) {
            // This method is not used in the splitting processor, but required by abstract class
            return new S3FileProcessorOutput();
        }

        @Override
        protected void handleResult(S3FileSplitInput input, S3FileProcessorOutput result) {
            // This method is not used in the splitting processor, but required by abstract class
        }

        @Override
        protected void handleException(S3FileSplitInput input, DataProcessingException exception) {
            // This method is not used in the splitting processor, but required by abstract class
        }
    }

    @BeforeEach
    void setUp() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        handler = new TestS3FileSplittingProcessorLambdaHandler(sfnClient, s3Client, pgpUtilities, executorService, transferManager);
        
        testInputFile = S3File.builder()
                .bucket("test-input-bucket")
                .fileKey("test-input-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
                
        testOutputFile = S3File.builder()
                .bucket("test-output-bucket")
                .fileKey("test-output-key")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        
        testInput = new S3FileSplitInput("test-token", "test-task", testInputFile, testOutputFile, 100L);
    }

    @Test
    void testConstructor() {
        assertNotNull(handler);
        // Verify that the handler was properly initialized
        assertDoesNotThrow(() -> {
            ExecutorService executorService = Executors.newFixedThreadPool(2);
            TestS3FileSplittingProcessorLambdaHandler newHandler = 
                new TestS3FileSplittingProcessorLambdaHandler(sfnClient, s3Client, pgpUtilities, executorService, transferManager);
            assertNotNull(newHandler);
        });
    }

    @Test
    void testProcessFragment_WithValidData() throws Exception {
        String testData = "test fragment data";
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, 50L);
        
        // Verify the result
        assertNotNull(result);
        assertTrue(result > 0);
        
        // Verify the fragment stream contains the data
        String fragmentData = fragmentStream.toString();
        assertEquals(testData, fragmentData);
    }

    @Test
    void testProcessFragment_WithEmptySource() throws Exception {
        InputStream sourceStream = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, 50L);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(0, result);
        
        // Verify the fragment stream is empty
        assertEquals(0, fragmentStream.size());
    }

    @Test
    void testProcessFragment_WithLargeData() throws Exception {
        // Create large test data
        StringBuilder largeData = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeData.append("test data line ").append(i).append("\n");
        }
        String testData = largeData.toString();
        
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, 1000L);
        
        // Verify the result
        assertNotNull(result);
        assertTrue(result > 0);
        
        // Verify the fragment stream contains the data
        String fragmentData = fragmentStream.toString();
        assertEquals(testData, fragmentData);
    }

    @Test
    void testProcessFragment_WithNullSourceStream() {
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute and verify exception
        assertThrows(DataProcessingException.class, () -> {
            handler.processFragment(testInput, null, fragmentStream, 50L);
        });
    }

    @Test
    void testProcessFragment_WithNullFragmentStream() {
        String testData = "test data";
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        
        // Execute and verify exception
        assertThrows(DataProcessingException.class, () -> {
            handler.processFragment(testInput, sourceStream, null, 50L);
        });
    }

    @Test
    void testProcessFragment_WithZeroRecordsToRead() throws Exception {
        String testData = "test data";
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, 0L);
        
        // Verify the result (should still process all available data)
        assertNotNull(result);
        assertTrue(result > 0);
    }

    @Test
    void testProcessFragment_WithNegativeRecordsToRead() throws Exception {
        String testData = "test data";
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, -1L);
        
        // Verify the result (should still process all available data)
        assertNotNull(result);
        assertTrue(result > 0);
    }

    @Test
    void testProcessFragment_WithIOException() {
        // Create a mock InputStream that throws IOException
        InputStream mockSourceStream = mock(InputStream.class);
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        try {
            when(mockSourceStream.read(any(byte[].class))).thenThrow(new IOException("Test IO error"));
        } catch (IOException e) {
            fail("Mock setup failed");
        }
        
        // Execute and verify exception
        DataProcessingException exception = assertThrows(DataProcessingException.class, () -> {
            handler.processFragment(testInput, mockSourceStream, fragmentStream, 50L);
        });
        
        assertEquals("Failed to process fragment", exception.getMessage());
        assertTrue(exception.getCause() instanceof IOException);
    }

    @Test
    void testGetInputType() {
        assertEquals(S3FileSplitInput.class, handler.getInputType());
    }

    @Test
    void testProcessSqsTypeInternal() {
        S3FileProcessorOutput result = handler.processSqsTypeInternal(testInput);
        assertNotNull(result);
    }

    @Test
    void testHandleResult() {
        S3FileProcessorOutput result = new S3FileProcessorOutput();
        assertDoesNotThrow(() -> {
            handler.handleResult(testInput, result);
        });
    }

    @Test
    void testHandleException() {
        DataProcessingException exception = new DataProcessingException("Test exception");
        assertDoesNotThrow(() -> {
            handler.handleException(testInput, exception);
        });
    }

    @Test
    void testProcessFragment_WithExactRecordCount() throws Exception {
        // Create data that should result in exactly 5 records (500 bytes / 100 bytes per record)
        String testData = "x".repeat(500);
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method with exactly 5 records to read
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, 5L);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(5, result);
        
        // Verify the fragment stream contains the data
        String fragmentData = fragmentStream.toString();
        assertEquals(testData, fragmentData);
    }

    @Test
    void testProcessFragment_WithPartialRecordCount() throws Exception {
        // Create data that should result in 10 records (1000 bytes / 100 bytes per record)
        String testData = "x".repeat(1000);
        InputStream sourceStream = new ByteArrayInputStream(testData.getBytes());
        ByteArrayOutputStream fragmentStream = new ByteArrayOutputStream();
        
        // Execute the method with only 3 records to read (should stop early)
        Integer result = handler.processFragment(testInput, sourceStream, fragmentStream, 3L);
        
        // Verify the result
        assertNotNull(result);
        assertEquals(3, result);
        
        // Verify the fragment stream contains partial data (300 bytes)
        String fragmentData = fragmentStream.toString();
        assertEquals("x".repeat(300), fragmentData);
    }
} 