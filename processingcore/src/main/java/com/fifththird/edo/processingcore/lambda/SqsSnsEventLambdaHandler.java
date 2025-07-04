package com.fifththird.edo.processingcore.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for SQS SNS Event Lambda handlers.
 * Extends RequestHandler to handle SQS events and return String results.
 * Accepts two generic type parameters for input and output processing.
 * 
 * @param <T> The type of the input data extracted from SQS messages
 * @param <R> The type of the result data to be returned
 */
public abstract class SqsSnsEventLambdaHandler<T, R> implements RequestHandler<SQSEvent, String> {
    
    private static final Logger logger = LoggerFactory.getLogger(SqsSnsEventLambdaHandler.class);
    
    /**
     * Parses a JSON string into the generic type T using Jackson ObjectMapper.
     * 
     * @param messageBody The JSON string to parse
     * @return The parsed object of type T
     * @throws Exception if parsing fails
     */
    protected T parseMessage(String messageBody) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JavaType javaType = objectMapper.getTypeFactory().constructType(getTypeClass());
        return objectMapper.readValue(messageBody, javaType);
    }
    
    /**
     * Abstract method to provide the Class for Jackson deserialization.
     * Subclasses must implement this to specify the exact class to deserialize to.
     * 
     * @return The Class for type T
     */
    protected abstract Class<T> getTypeClass();
    
    /**
     * Abstract method to process the parsed SNS data.
     * Subclasses must implement this to define the business logic for processing the data.
     * 
     * @param parsedData The parsed data of type T
     * @return The result of processing the data, of type R
     */
    protected abstract R processSnsDataInternal(T parsedData);
    
    /**
     * Handles the SQS event by processing each message and returning a JSON string result.
     * 
     * @param sqsEvent the SQS event containing messages to process
     * @param context the Lambda execution context
     * @return a JSON string representation of the processing results
     */
    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        try {
            if (sqsEvent != null && sqsEvent.getRecords() != null) {
                for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
                    T parsedData = null;
                    try {
                        String body = message.getBody();
                        parsedData = parseMessage(body);
                        // Process the parsed data
                        R result = processSnsDataInternal(parsedData);
                        // Handle the result
                        handleResult(parsedData, result);
                    } catch (DataProcessingException e) {
                        // Handle DataProcessingException specifically
                        handleException(parsedData, e);
                    } catch (Exception e) {
                        // Wrap other exceptions in DataProcessingException
                        handleException(parsedData, new DataProcessingException("Error processing SNS data", e));
                    }
                }
                
                return "Successfully processed " + sqsEvent.getRecords().size() + " SQS records";
            }
            return "No SQS records to process";
        } catch (Exception e) {
            logger.error("Error handling SQS event: {}", e.getMessage(), e);
            throw new DataProcessingException("Error handling SQS event", e);
        }
    }
    
    /**
     * Handles successful processing results.
     * Subclasses can override this method to implement custom result handling logic.
     * 
     * @param input the original input data
     * @param result the processing result
     */
    protected void handleResult(T input, R result) {
        // Empty implementation - subclasses can override to add custom logic
    }

    /**
     * Handles exceptions that occur during processing.
     * Subclasses can override this method to implement custom exception handling logic.
     * 
     * @param input The input data that was being processed when the exception occurred
     * @param exception The exception that occurred during processing
     */
    protected void handleException(T input, DataProcessingException exception) {
        // Log the exception
        logger.error("Error processing SNS data: {}", exception.getMessage(), exception);
        // Re-throw the exception
        throw exception;
    }
} 