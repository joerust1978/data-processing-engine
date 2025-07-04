package com.fifththird.edo.processingcore.exception;

/**
 * Exception thrown when data processing operations fail.
 * This is a runtime exception that can be thrown during data processing operations
 * such as file parsing, transformation, or any other processing step.
 */
public class DataProcessingException extends RuntimeException {

    /**
     * Constructs a new DataProcessingException with null as its detail message.
     */
    public DataProcessingException() {
        super();
    }

    /**
     * Constructs a new DataProcessingException with the specified detail message.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     */
    public DataProcessingException(String message) {
        super(message);
    }

    /**
     * Constructs a new DataProcessingException with the specified detail message and cause.
     *
     * @param message the detail message (which is saved for later retrieval by the getMessage() method)
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public DataProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DataProcessingException with the specified cause and a detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     *
     * @param cause the cause (which is saved for later retrieval by the getCause() method)
     */
    public DataProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new DataProcessingException with the specified detail message, cause,
     * suppression enabled or disabled, and writable stack trace enabled or disabled.
     *
     * @param message the detail message
     * @param cause the cause
     * @param enableSuppression whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    protected DataProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
} 