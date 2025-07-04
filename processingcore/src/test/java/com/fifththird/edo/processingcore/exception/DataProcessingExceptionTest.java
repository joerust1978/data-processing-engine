package com.fifththird.edo.processingcore.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DataProcessingException.
 */
class DataProcessingExceptionTest {

    @Test
    void testDefaultConstructor() {
        DataProcessingException exception = new DataProcessingException();
        
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageConstructor() {
        String message = "Test error message";
        DataProcessingException exception = new DataProcessingException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testMessageAndCauseConstructor() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Original cause");
        DataProcessingException exception = new DataProcessingException(message, cause);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testCauseConstructor() {
        Throwable cause = new RuntimeException("Original cause");
        DataProcessingException exception = new DataProcessingException(cause);
        
        assertEquals(cause.toString(), exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testCauseConstructorWithNullCause() {
        DataProcessingException exception = new DataProcessingException((Throwable) null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testFullConstructor() {
        String message = "Test error message";
        Throwable cause = new RuntimeException("Original cause");
        DataProcessingException exception = new DataProcessingException(message, cause, true, true);
        
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionInheritance() {
        DataProcessingException exception = new DataProcessingException("Test message");
        
        // Should be a RuntimeException
        assertTrue(exception instanceof RuntimeException);
        
        // Should be a DataProcessingException
        assertTrue(exception instanceof DataProcessingException);
    }

    @Test
    void testExceptionChaining() {
        RuntimeException originalException = new RuntimeException("Original error");
        DataProcessingException wrapperException = new DataProcessingException("Wrapper error", originalException);
        
        assertEquals("Wrapper error", wrapperException.getMessage());
        assertEquals(originalException, wrapperException.getCause());
        assertEquals("Original error", wrapperException.getCause().getMessage());
    }

    @Test
    void testNullMessage() {
        DataProcessingException exception = new DataProcessingException((String) null);
        
        assertNull(exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testEmptyMessage() {
        String emptyMessage = "";
        DataProcessingException exception = new DataProcessingException(emptyMessage);
        
        assertEquals(emptyMessage, exception.getMessage());
        assertNull(exception.getCause());
    }
} 