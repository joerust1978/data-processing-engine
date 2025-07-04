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
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for SqsSnsEventLambdaHandler.
 */
@ExtendWith(MockitoExtension.class)
class SqsSnsEventLambdaHandlerTest {

    private TestSqsSnsEventLambdaHandler handler;

    @Mock
    private Context mockContext;

    @BeforeEach
    void setUp() {
        handler = new TestSqsSnsEventLambdaHandler();
    }

    @Test
    void testParseMessage_WithValidJson() throws Exception {
        // Given
        String jsonMessage = "{\"testField\":\"testValue\",\"numberField\":42}";

        // When
        TestMessage result = handler.parseMessage(jsonMessage);

        // Then
        assertNotNull(result);
        assertEquals("testValue", result.getTestField());
        assertEquals(42, result.getNumberField());
    }

    @Test
    void testParseMessage_WithInvalidJson() {
        // Given
        String invalidJson = "invalid json";

        // When & Then
        assertThrows(Exception.class, () -> {
            handler.parseMessage(invalidJson);
        });
    }

    @Test
    void testHandleRequest_WithValidSQSEvent() {
        // Given
        SQSEvent sqsEvent = createTestSQSEvent();

        // When
        String result = handler.handleRequest(sqsEvent, mockContext);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Successfully processed"));
        assertEquals("Successfully processed 1 SQS records", result);
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
        assertEquals("Successfully processed 0 SQS records", result);
    }

    @Test
    void testHandleRequest_WithNullSQSEvent() {
        // When
        String result = handler.handleRequest(null, mockContext);
        // Then
        assertNotNull(result);
        assertEquals("No SQS records to process", result);
    }

    @Test
    void testHandleRequest_WithProcessingException() {
        // Given
        SQSEvent sqsEvent = createTestSQSEvent();
        handler.setShouldThrowException(true);

        // When & Then
        assertThrows(DataProcessingException.class, () -> {
            handler.handleRequest(sqsEvent, mockContext);
        });
    }

    @Test
    void testHandleRequest_WithDataProcessingException() {
        // Given
        SQSEvent sqsEvent = createTestSQSEvent();
        handler.setShouldThrowDataProcessingException(true);

        // When & Then
        assertThrows(DataProcessingException.class, () -> {
            handler.handleRequest(sqsEvent, mockContext);
        });
    }

    @Test
    void testHandleResult_DefaultImplementation() {
        // Given
        TestMessage input = new TestMessage("test", 1);
        String result = "test-result";

        // When & Then - Should not throw exception
        assertDoesNotThrow(() -> {
            handler.handleResult(input, result);
        });
    }

    @Test
    void testHandleException_DefaultImplementation() {
        // Given
        TestMessage input = new TestMessage("test", 1);
        DataProcessingException exception = new DataProcessingException("Test error");

        // When & Then - Should re-throw the exception
        assertThrows(DataProcessingException.class, () -> {
            handler.handleException(input, exception);
        });
    }

    private SQSEvent createTestSQSEvent() {
        SQSEvent sqsEvent = new SQSEvent();
        SQSEvent.SQSMessage message = new SQSEvent.SQSMessage();
        message.setMessageId("msg-001");
        message.setBody("{\"testField\":\"testValue\",\"numberField\":42}");
        message.setReceiptHandle("receipt-handle-1");
        sqsEvent.setRecords(Arrays.asList(message));
        return sqsEvent;
    }

    /**
     * Test implementation of SqsSnsEventLambdaHandler.
     */
    private static class TestSqsSnsEventLambdaHandler extends SqsSnsEventLambdaHandler<TestMessage, String> {

        private boolean shouldThrowException = false;
        private boolean shouldThrowDataProcessingException = false;

        public void setShouldThrowException(boolean shouldThrowException) {
            this.shouldThrowException = shouldThrowException;
        }

        public void setShouldThrowDataProcessingException(boolean shouldThrowDataProcessingException) {
            this.shouldThrowDataProcessingException = shouldThrowDataProcessingException;
        }

        @Override
        protected Class<TestMessage> getTypeClass() {
            return TestMessage.class;
        }

        @Override
        protected String processSnsDataInternal(TestMessage parsedData) {
            if (shouldThrowException) {
                throw new RuntimeException("Test exception");
            }
            if (shouldThrowDataProcessingException) {
                throw new DataProcessingException("Test data processing exception");
            }
            return "processed-" + parsedData.getTestField();
        }
    }

    /**
     * Test message class for JSON deserialization.
     */
    public static class TestMessage {
        private String testField;
        private int numberField;

        // Default constructor for Jackson
        public TestMessage() {}

        public TestMessage(String testField, int numberField) {
            this.testField = testField;
            this.numberField = numberField;
        }

        public String getTestField() {
            return testField;
        }

        public void setTestField(String testField) {
            this.testField = testField;
        }

        public int getNumberField() {
            return numberField;
        }

        public void setNumberField(int numberField) {
            this.numberField = numberField;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestMessage that = (TestMessage) o;
            return numberField == that.numberField &&
                    (testField != null ? testField.equals(that.testField) : that.testField == null);
        }

        @Override
        public int hashCode() {
            int result = testField != null ? testField.hashCode() : 0;
            result = 31 * result + numberField;
            return result;
        }

        @Override
        public String toString() {
            return "TestMessage{testField='" + testField + "', numberField=" + numberField + "}";
        }
    }
} 