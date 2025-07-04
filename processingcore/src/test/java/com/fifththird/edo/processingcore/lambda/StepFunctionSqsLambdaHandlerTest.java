package com.fifththird.edo.processingcore.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.model.StepFunctionInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest;
import software.amazon.awssdk.services.sfn.model.SfnException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for StepFunctionSqsLambdaHandler.
 */
@ExtendWith(MockitoExtension.class)
class StepFunctionSqsLambdaHandlerTest {

    private TestStepFunctionSqsLambdaHandler handler;

    @Mock
    private SfnClient mockSfnClient;

    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        handler = new TestStepFunctionSqsLambdaHandler(mockSfnClient);
    }

    @Test
    void testHandleResult_WithValidTaskToken() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput("test-token", "test-task");
        String result = "test-result";

        // When
        handler.handleResult(input, result);

        // Then
        verify(mockSfnClient, times(1)).sendTaskSuccess(any(SendTaskSuccessRequest.class));
    }

    @Test
    void testHandleResult_WithNullTaskToken() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput(null, "test-task");
        String result = "test-result";

        // When
        handler.handleResult(input, result);

        // Then
        verify(mockSfnClient, never()).sendTaskSuccess(any(SendTaskSuccessRequest.class));
    }

    @Test
    void testHandleResult_WithSfnClientException() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput("test-token", "test-task");
        String result = "test-result";
        when(mockSfnClient.sendTaskSuccess(any(SendTaskSuccessRequest.class)))
                .thenThrow(new RuntimeException("SfnClient error"));

        // When & Then
        assertThrows(DataProcessingException.class, () -> {
            handler.handleResult(input, result);
        });
    }

    @Test
    void testHandleException_WithValidTaskToken() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput("test-token", "test-task");
        DataProcessingException exception = new DataProcessingException("Test error");

        // When
        handler.handleException(input, exception);

        // Then
        verify(mockSfnClient, times(1)).sendTaskFailure(any(SendTaskFailureRequest.class));
    }

    @Test
    void testHandleException_WithNullTaskToken() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput(null, "test-task");
        DataProcessingException exception = new DataProcessingException("Test error");

        // When
        handler.handleException(input, exception);

        // Then
        verify(mockSfnClient, never()).sendTaskFailure(any(SendTaskFailureRequest.class));
    }

    @Test
    void testHandleException_WithSfnException() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput("test-token", "test-task");
        DataProcessingException exception = new DataProcessingException("Test error");
        when(mockSfnClient.sendTaskFailure(any(SendTaskFailureRequest.class)))
                .thenThrow(SfnException.builder().message("Sfn error").build());

        // When
        handler.handleException(input, exception);

        // Then
        // Should not throw exception, just log the error
        verify(mockSfnClient, times(1)).sendTaskFailure(any(SendTaskFailureRequest.class));
    }

    @Test
    void testHandleException_WithUnexpectedException() {
        // Given
        TestStepFunctionInput input = new TestStepFunctionInput("test-token", "test-task");
        DataProcessingException exception = new DataProcessingException("Test error");
        when(mockSfnClient.sendTaskFailure(any(SendTaskFailureRequest.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        handler.handleException(input, exception);

        // Then
        // Should not throw exception, just log the error
        verify(mockSfnClient, times(1)).sendTaskFailure(any(SendTaskFailureRequest.class));
    }

    @Test
    void testHandleRequest_WithValidSQSEvent() {
        // Given
        SQSEvent sqsEvent = createTestSQSEvent();

        // When
        String result = handler.handleRequest(sqsEvent, mockContext);

        // Then
        assertNotNull(result);
        assertEquals("[\"processed-result\"]", result);
    }

    @Test
    void testHandleRequest_WithEmptySQSEvent() {
        // Given
        SQSEvent sqsEvent = new SQSEvent();
        sqsEvent.setRecords(Arrays.asList());

        // When
        String result = handler.handleRequest(sqsEvent, mockContext);

        // Then
        assertNotNull(result);
        assertEquals("[]", result);
    }

    private SQSEvent createTestSQSEvent() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{\"taskToken\":\"test-token\",\"taskName\":\"test-task\"}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    /**
     * Test implementation of StepFunctionSqsLambdaHandler.
     */
    private static class TestStepFunctionSqsLambdaHandler extends StepFunctionSqsLambdaHandler<TestStepFunctionInput, String> {

        public TestStepFunctionSqsLambdaHandler(SfnClient sfnClient) {
            super(sfnClient);
        }

        @Override
        protected Class<TestStepFunctionInput> getInputType() {
            return TestStepFunctionInput.class;
        }

        @Override
        protected String processSqsTypeInternal(TestStepFunctionInput input) {
            return "processed-result";
        }
    }

    /**
     * Test implementation of StepFunctionInput.
     */
    private static class TestStepFunctionInput extends StepFunctionInput {
        
        // Default constructor for Jackson deserialization
        public TestStepFunctionInput() {
            super();
        }
        
        public TestStepFunctionInput(String taskToken, String taskName) {
            super(taskToken, taskName);
        }
    }
} 