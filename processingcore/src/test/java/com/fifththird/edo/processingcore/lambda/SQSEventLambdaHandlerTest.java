package com.fifththird.edo.processingcore.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SQSEventLambdaHandler.
 */
@ExtendWith(MockitoExtension.class)
class SQSEventLambdaHandlerTest {

    private TestSQSEventLambdaHandler handler;

    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        handler = new TestSQSEventLambdaHandler();
    }

    @Test
    void testHandleRequest_WithValidSQSEvent() {
        // Given
        SQSEvent sqsEvent = createTestSQSEvent();
        
        // When
        String result = handler.handleRequest(sqsEvent, mockContext);
        
        // Then
        assertNotNull(result);
        // The result should be a JSON array containing both processed message results
        assertTrue(result.contains("message1"));
        assertTrue(result.contains("message2"));
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
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
        // Should return an empty array as JSON string since no messages were processed
        assertEquals("[]", result);
    }

    @Test
    void testHandleRequest_WithNullSQSEvent() {
        // Given
        SQSEvent sqsEvent = null;
        
        // When & Then
        assertThrows(NullPointerException.class, () -> {
            handler.handleRequest(sqsEvent, mockContext);
        });
    }

    @Test
    void testHandleRequest_WithNullContext() {
        // Given
        SQSEvent sqsEvent = createTestSQSEvent();
        
        // When
        String result = handler.handleRequest(sqsEvent, null);
        
        // Then
        assertNotNull(result);
        // The result should be a JSON array containing both processed message results
        assertTrue(result.contains("message1"));
        assertTrue(result.contains("message2"));
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    /**
     * Creates a test SQS event with sample messages.
     */
    private SQSEvent createTestSQSEvent() {
        SQSEvent sqsEvent = new SQSEvent();
        
        // Create first message
        SQSEvent.SQSMessage message1 = new SQSEvent.SQSMessage();
        message1.setMessageId("msg-001");
        message1.setBody("{\"test\": \"message1\"}");
        message1.setReceiptHandle("receipt-handle-1");
        
        // Create second message
        SQSEvent.SQSMessage message2 = new SQSEvent.SQSMessage();
        message2.setMessageId("msg-002");
        message2.setBody("{\"test\": \"message2\"}");
        message2.setReceiptHandle("receipt-handle-2");
        
        List<SQSEvent.SQSMessage> messages = Arrays.asList(message1, message2);
        sqsEvent.setRecords(messages);
        
        return sqsEvent;
    }

    /**
     * Simple test class for JSON deserialization.
     */
    public static class TestMessage {
        private String test;

        // Default constructor for Jackson
        public TestMessage() {}

        public TestMessage(String test) {
            this.test = test;
        }

        public String getTest() {
            return test;
        }

        public void setTest(String test) {
            this.test = test;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestMessage that = (TestMessage) o;
            return test != null ? test.equals(that.test) : that.test == null;
        }

        @Override
        public int hashCode() {
            return test != null ? test.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "TestMessage{test='" + test + "'}";
        }
    }

    /**
     * Concrete implementation of SQSEventLambdaHandler for testing purposes.
     */
    private static class TestSQSEventLambdaHandler extends SQSEventLambdaHandler<TestMessage, String> {
        
        @Override
        protected Class<TestMessage> getInputType() {
            return TestMessage.class;
        }
        
        @Override
        protected String processSqsTypeInternal(TestMessage input) {
            // Simple implementation that returns the test field
            return input.getTest();
        }
        
        @Override
        protected void handleResult(TestMessage input, String result) {
            // Simple implementation that does nothing
            // In a real implementation, this might save results to a database, send notifications, etc.
        }
        
        @Override
        protected void handleException(TestMessage input, DataProcessingException exception) {
            // Simple implementation that does nothing
            // In a real implementation, this might log the exception, send alerts, etc.
        }
    }
} 