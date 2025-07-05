package com.fifththird.edo.filesplitters.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fifththird.edo.filesplitters.model.SplitJsonInput;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.model.S3File;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.bouncycastle.openpgp.PGPException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SplitJsonFileHandlerTest {

    private SplitJsonFileHandler handler;
    @Mock
    private SfnClient mockSfnClient;
    @Mock
    private S3Client mockS3Client;
    @Mock
    private PgpUtilities mockPgpUtilities;
    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Mock S3Client.getObject to return a stream of individual JSON objects (not an array)
        String jsonObjects = "{" +
                "\"id\":1,\"name\":\"A\"}" + "\n" +
                "{" +
                "\"id\":2,\"name\":\"B\"}" + "\n" +
                "{" +
                "\"id\":3,\"name\":\"C\"}" + "\n" +
                "{" +
                "\"id\":4,\"name\":\"D\"}";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObjects.getBytes());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) jsonObjects.length()).build(), bais);
        Mockito.when(mockS3Client.getObject(Mockito.any(GetObjectRequest.class))).thenReturn(responseInputStream);
        handler = new SplitJsonFileHandler(mockSfnClient, mockS3Client, mockPgpUtilities);
        
        // Set the @Value fields using reflection since they're not injected in tests
        try {
            java.lang.reflect.Field privateKeyField = SplitJsonFileHandler.class.getDeclaredField("pgpPrivateKey");
            privateKeyField.setAccessible(true);
            privateKeyField.set(handler, "test-private-key");
            
            java.lang.reflect.Field passphraseField = SplitJsonFileHandler.class.getDeclaredField("pgpPassphrase");
            passphraseField.setAccessible(true);
            passphraseField.set(handler, "test-passphrase");
            
            java.lang.reflect.Field publicKeyField = SplitJsonFileHandler.class.getDeclaredField("pgpPublicKey");
            publicKeyField.setAccessible(true);
            publicKeyField.set(handler, "test-public-key");
        } catch (Exception e) {
            throw new RuntimeException("Failed to set PGP fields", e);
        }
    }

    @Test
    void testHandleRequest_WithValidSQSEvent() {
        SQSEvent sqsEvent = createTestSQSEvent(2); // split every 2 records
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        // Check that the result contains the expected SplitJsonOutput fields
        assertTrue(result.contains("outputFileData"));
        assertTrue(result.contains("stateFileData"));
        assertTrue(result.contains("recordCount"));
        // Verify that S3 putObject was called 4 times (2 fragments + 2 state files)
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        Mockito.verify(mockS3Client, Mockito.times(4)).putObject(putCaptor.capture(), bodyCaptor.capture());
        // Check that we have 4 putObject calls: 2 fragments + 2 state files
        assertEquals(4, putCaptor.getAllValues().size());
    }

    @Test
    void testHandleRequest_WithEmptySQSEvent() {
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(Arrays.asList());
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
    }

    @Test
    void testHandleRequest_WithNullSQSEvent() {
        assertThrows(NullPointerException.class, () -> handler.handleRequest(null, mockContext));
    }

    // GZIP Compression Tests
    @Test
    void testHandleRequest_WithGzippedInput() throws IOException {
        // Create GZIP compressed JSON content
        String jsonObjects = "{\"id\":1,\"name\":\"A\"}\n{\"id\":2,\"name\":\"B\"}";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(jsonObjects.getBytes());
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) baos.size()).build(), bais);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SQSEvent sqsEvent = createTestSQSEventWithGzip(true, false);
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockS3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testHandleRequest_WithGzippedOutput() throws IOException {
        SQSEvent sqsEvent = createTestSQSEventWithGzip(false, true);
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockS3Client, times(4)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    // PGP Encryption Tests
    @Test
    void testHandleRequest_WithPgpEncryptedInput() throws IOException, PGPException {
        // Mock PGP decryption
        String jsonObjects = "{\"id\":1,\"name\":\"A\"}\n{\"id\":2,\"name\":\"B\"}";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObjects.getBytes());
        when(mockPgpUtilities.wrapWithDecryption(any(InputStream.class), anyString(), anyString()))
                .thenReturn(bais);
        
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) jsonObjects.length()).build(), bais);
        reset(mockS3Client);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SQSEvent sqsEvent = createTestSQSEventWithPgp(true, false);
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockPgpUtilities).wrapWithDecryption(any(InputStream.class), anyString(), anyString());
        verify(mockS3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testHandleRequest_WithPgpEncryptedOutput() throws IOException, PGPException {
        // Mock PGP encryption
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        when(mockPgpUtilities.wrapWithEncryption(any(ByteArrayOutputStream.class), anyString()))
                .thenReturn(baos);

        SQSEvent sqsEvent = createTestSQSEventWithPgp(false, true);
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockPgpUtilities, times(2)).wrapWithEncryption(any(ByteArrayOutputStream.class), anyString());
        verify(mockS3Client, times(4)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testHandleRequest_WithPgpDecryptionFailure() throws IOException, PGPException {
        when(mockPgpUtilities.wrapWithDecryption(any(InputStream.class), anyString(), anyString()))
                .thenThrow(new PGPException("Test PGP error"));

        SplitJsonInput input = createTestInputWithPgp(true, false);
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    // Input Validation Tests
    @Test
    void testHandleRequest_WithNullInputFile() {
        SplitJsonInput input = createTestInputWithNullInputFile();
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithNullOutputFile() {
        SplitJsonInput input = createTestInputWithNullOutputFile();
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithInvalidRecordsPerSplit() {
        SplitJsonInput input = createTestInputWithInvalidRecordsPerSplit();
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithEmptyBucket() {
        SplitJsonInput input = createTestInputWithEmptyBucket();
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithEmptyFileKey() {
        SplitJsonInput input = createTestInputWithEmptyFileKey();
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    // Edge Cases Tests
    @Test
    void testHandleRequest_WithSingleObject() throws IOException {
        String jsonObjects = "{\"id\":1,\"name\":\"A\"}";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObjects.getBytes());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) jsonObjects.length()).build(), bais);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SQSEvent sqsEvent = createTestSQSEvent(1);
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockS3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testHandleRequest_WithEmptyFile() throws IOException {
        String jsonObjects = "";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObjects.getBytes());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength(0L).build(), bais);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SQSEvent sqsEvent = createTestSQSEvent(2);
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockS3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void testHandleRequest_WithLargeRecordsPerSplit() throws IOException {
        String jsonObjects = "{\"id\":1,\"name\":\"A\"}\n{\"id\":2,\"name\":\"B\"}";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObjects.getBytes());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) jsonObjects.length()).build(), bais);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SQSEvent sqsEvent = createTestSQSEvent(10); // More than available objects
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        verify(mockS3Client, times(2)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    // Error Handling Tests
    @Test
    void testHandleRequest_WithS3GetObjectFailure() {
        // Reset the mock to throw exception for this test
        reset(mockS3Client);
        when(mockS3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 error"));

        SplitJsonInput input = createTestInput(2);
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithS3PutObjectFailure() {
        doThrow(new RuntimeException("S3 put error"))
                .when(mockS3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));

        SplitJsonInput input = createTestInput(2);
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithInvalidJson() throws IOException {
        String invalidJson = "invalid json content";
        ByteArrayInputStream bais = new ByteArrayInputStream(invalidJson.getBytes());
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) invalidJson.length()).build(), bais);
        reset(mockS3Client);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SplitJsonInput input = createTestInput(2);
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    // Configuration Tests
    @Test
    void testHandleRequest_WithMissingPgpPrivateKey() throws Exception {
        // Clear the private key field for this test
        java.lang.reflect.Field privateKeyField = SplitJsonFileHandler.class.getDeclaredField("pgpPrivateKey");
        privateKeyField.setAccessible(true);
        privateKeyField.set(handler, "");
        
        SplitJsonInput input = createTestInputWithPgp(true, false);
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    @Test
    void testHandleRequest_WithMissingPgpPublicKey() throws IOException, PGPException, Exception {
        // Clear the public key field for this test
        java.lang.reflect.Field publicKeyField = SplitJsonFileHandler.class.getDeclaredField("pgpPublicKey");
        publicKeyField.setAccessible(true);
        publicKeyField.set(handler, "");
        
        // Mock PGP decryption for input
        String jsonObjects = "{\"id\":1,\"name\":\"A\"}";
        ByteArrayInputStream bais = new ByteArrayInputStream(jsonObjects.getBytes());
        when(mockPgpUtilities.wrapWithDecryption(any(InputStream.class), anyString(), anyString()))
                .thenReturn(bais);
        
        ResponseInputStream<GetObjectResponse> responseInputStream = new ResponseInputStream<>(
                GetObjectResponse.builder().contentLength((long) jsonObjects.length()).build(), bais);
        reset(mockS3Client);
        when(mockS3Client.getObject(any(GetObjectRequest.class))).thenReturn(responseInputStream);

        SplitJsonInput input = createTestInputWithPgp(true, true);
        assertThrows(DataProcessingException.class, () -> handler.processSqsTypeInternal(input));
    }

    // Fragment State File Tests
    @Test
    void testHandleRequest_CreatesFragmentStateFiles() {
        SQSEvent sqsEvent = createTestSQSEvent(2); // split every 2 records
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        
        // Verify that S3 putObject was called for both fragments and their state files
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        Mockito.verify(mockS3Client, Mockito.times(4)).putObject(putCaptor.capture(), bodyCaptor.capture());
        
        // Check that we have 4 putObject calls: 2 fragments + 2 state files
        assertEquals(4, putCaptor.getAllValues().size());
        
        // Verify the state file keys contain "state/" and fragment indices
        boolean foundStateFile0 = false;
        boolean foundStateFile1 = false;
        
        for (PutObjectRequest request : putCaptor.getAllValues()) {
            if (request.key().contains("state/0")) {
                foundStateFile0 = true;
                assertEquals("application/json", request.contentType());
            }
            if (request.key().contains("state/1")) {
                foundStateFile1 = true;
                assertEquals("application/json", request.contentType());
            }
        }
        
        assertTrue(foundStateFile0, "State file for fragment 0 should be created");
        assertTrue(foundStateFile1, "State file for fragment 1 should be created");
    }

    @Test
    void testHandleRequest_FragmentStateFileContent() {
        SQSEvent sqsEvent = createTestSQSEvent(2); // split every 2 records
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        
        // Capture all putObject calls
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> bodyCaptor = ArgumentCaptor.forClass(RequestBody.class);
        Mockito.verify(mockS3Client, Mockito.times(4)).putObject(putCaptor.capture(), bodyCaptor.capture());
        
        // Find the state file requests and verify their content
        for (int i = 0; i < putCaptor.getAllValues().size(); i++) {
            PutObjectRequest request = putCaptor.getAllValues().get(i);
            
            if (request.key().contains("state/") && "application/json".equals(request.contentType())) {
                // Verify the state file key contains the expected pattern
                assertTrue(request.key().contains("state/"), "State file key should contain state/");
                assertTrue(request.key().matches(".*state/\\d+"), "State file key should end with state/fragmentIndex");
                assertEquals("test-bucket", request.bucket(), "State file should use correct bucket");
            }
        }
    }

    @Test
    void testHandleRequest_FragmentStateFileKeys() {
        SQSEvent sqsEvent = createTestSQSEvent(1); // split every 1 record (4 fragments)
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        
        // Verify that S3 putObject was called for all fragments and their state files
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        Mockito.verify(mockS3Client, Mockito.times(8)).putObject(putCaptor.capture(), any(RequestBody.class));
        
        // Check that state file keys follow the expected pattern
        boolean foundState0 = false, foundState1 = false, foundState2 = false, foundState3 = false;
        
        for (PutObjectRequest request : putCaptor.getAllValues()) {
            String key = request.key();
            if (key.contains("state/0")) foundState0 = true;
            if (key.contains("state/1")) foundState1 = true;
            if (key.contains("state/2")) foundState2 = true;
            if (key.contains("state/3")) foundState3 = true;
        }
        
        assertTrue(foundState0, "Should create state file for fragment 0");
        assertTrue(foundState1, "Should create state file for fragment 1");
        assertTrue(foundState2, "Should create state file for fragment 2");
        assertTrue(foundState3, "Should create state file for fragment 3");
    }

    @Test
    void testHandleRequest_StateFileBucketMatchesInput() {
        SQSEvent sqsEvent = createTestSQSEvent(2); // split every 2 records
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        
        // Verify that all state files use the same bucket as the input
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        Mockito.verify(mockS3Client, Mockito.times(4)).putObject(putCaptor.capture(), any(RequestBody.class));
        
        for (PutObjectRequest request : putCaptor.getAllValues()) {
            if (request.key().contains("state/")) {
                assertEquals("test-bucket", request.bucket(), "State file should use same bucket as input");
            }
        }
    }

    @Test
    void testHandleRequest_StateFileContentType() {
        SQSEvent sqsEvent = createTestSQSEvent(2); // split every 2 records
        String result = handler.handleRequest(sqsEvent, mockContext);
        assertNotNull(result);
        
        // Verify that state files have correct content type
        ArgumentCaptor<PutObjectRequest> putCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        Mockito.verify(mockS3Client, Mockito.times(4)).putObject(putCaptor.capture(), any(RequestBody.class));
        
        for (PutObjectRequest request : putCaptor.getAllValues()) {
            if (request.key().contains("state/")) {
                assertEquals("application/json", request.contentType(), "State files should have JSON content type");
            }
        }
    }

    // Helper methods for creating test events
    private SQSEvent createTestSQSEvent(int recordsPerSplit) {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":" + recordsPerSplit + "," +
                "\"inputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"input.json\",\"gzipped\":false,\"pgpEncrypted\":false}," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":false,\"pgpEncrypted\":false}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithGzip(boolean inputGzipped, boolean outputGzipped) {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":2," +
                "\"inputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"input.json\",\"gzipped\":" + inputGzipped + ",\"pgpEncrypted\":false}," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":" + outputGzipped + ",\"pgpEncrypted\":false}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithPgp(boolean inputPgpEncrypted, boolean outputPgpEncrypted) {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":2," +
                "\"inputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"input.json\",\"gzipped\":false,\"pgpEncrypted\":" + inputPgpEncrypted + "}," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":false,\"pgpEncrypted\":" + outputPgpEncrypted + "}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithNullInputFile() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":2," +
                "\"inputFile\":null," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":false,\"pgpEncrypted\":false}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithNullOutputFile() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":2," +
                "\"inputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"input.json\",\"gzipped\":false,\"pgpEncrypted\":false}," +
                "\"outputFile\":null" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithInvalidRecordsPerSplit() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":0," +
                "\"inputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"input.json\",\"gzipped\":false,\"pgpEncrypted\":false}," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":false,\"pgpEncrypted\":false}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithEmptyBucket() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":2," +
                "\"inputFile\":{\"bucket\":\"\",\"fileKey\":\"input.json\",\"gzipped\":false,\"pgpEncrypted\":false}," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":false,\"pgpEncrypted\":false}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    private SQSEvent createTestSQSEventWithEmptyFileKey() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{" +
                "\"recordsPerSplit\":2," +
                "\"inputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"\",\"gzipped\":false,\"pgpEncrypted\":false}," +
                "\"outputFile\":{\"bucket\":\"test-bucket\",\"fileKey\":\"output.json\",\"gzipped\":false,\"pgpEncrypted\":false}" +
                "}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    // Helper methods for creating SplitJsonInput objects
    private SplitJsonInput createTestInput(int recordsPerSplit) {
        S3File inputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("input.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(recordsPerSplit);
        input.setInputFile(inputFile);
        input.setOutputFile(outputFile);
        return input;
    }

    private SplitJsonInput createTestInputWithNullInputFile() {
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(2);
        input.setInputFile(null);
        input.setOutputFile(outputFile);
        return input;
    }

    private SplitJsonInput createTestInputWithNullOutputFile() {
        S3File inputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("input.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(2);
        input.setInputFile(inputFile);
        input.setOutputFile(null);
        return input;
    }

    private SplitJsonInput createTestInputWithInvalidRecordsPerSplit() {
        S3File inputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("input.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(0);
        input.setInputFile(inputFile);
        input.setOutputFile(outputFile);
        return input;
    }

    private SplitJsonInput createTestInputWithEmptyBucket() {
        S3File inputFile = S3File.builder()
                .bucket("")
                .fileKey("input.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(2);
        input.setInputFile(inputFile);
        input.setOutputFile(outputFile);
        return input;
    }

    private SplitJsonInput createTestInputWithEmptyFileKey() {
        S3File inputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(false)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(2);
        input.setInputFile(inputFile);
        input.setOutputFile(outputFile);
        return input;
    }

    private SplitJsonInput createTestInputWithPgp(boolean inputPgpEncrypted, boolean outputPgpEncrypted) {
        S3File inputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("input.json")
                .gzipped(false)
                .pgpEncrypted(inputPgpEncrypted)
                .build();
        S3File outputFile = S3File.builder()
                .bucket("test-bucket")
                .fileKey("output.json")
                .gzipped(false)
                .pgpEncrypted(outputPgpEncrypted)
                .build();
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(2);
        input.setInputFile(inputFile);
        input.setOutputFile(outputFile);
        return input;
    }
} 