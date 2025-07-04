package com.fifththird.edo.processingcore.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract Lambda handler for processing SQS events.
 * This handler processes SQS messages and can be deployed as an AWS Lambda function.
 * 
 * @param <T> The type of the input data extracted from SQS messages
 * @param <R> The type of the result data to be returned
 */
public abstract class SQSEventLambdaHandler<T, R> implements RequestHandler<SQSEvent, String> {

    private static final Logger logger = LoggerFactory.getLogger(SQSEventLambdaHandler.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Handles SQS events by processing each message in the event.
     * 
     * @param sqsEvent the SQS event containing messages to process
     * @param context the Lambda execution context
     * @return JSON string representation of the processing results as an array
     */
    @Override
    public String handleRequest(SQSEvent sqsEvent, Context context) {
        logger.info("Processing SQS event with {} messages", sqsEvent.getRecords().size());
        
        try {
            // Create an ArrayNode to collect all results
            ArrayNode resultsArray = objectMapper.createArrayNode();
            
            // Process each SQS message
            for (SQSEvent.SQSMessage message : sqsEvent.getRecords()) {
                try {
                    T inputData = parseMessage(message.getBody());
                    R result = processSqsTypeInternal(inputData);
                    
                    // Convert the result to a JsonNode and add it to the results array
                    JsonNode resultNode = objectMapper.valueToTree(result);
                    resultsArray.add(resultNode);
                    handleResult(inputData, result);
                } catch (DataProcessingException e) {
                    // Handle the DataProcessingException by calling the abstract method
                    handleException(parseMessage(message.getBody()), e);
                }
            }
            
            logger.info("Successfully processed all {} SQS messages", sqsEvent.getRecords().size());
            
            // Serialize the results array to JSON and return
            try {
                String jsonResult = objectMapper.writeValueAsString(resultsArray);
                logger.debug("Serialized results array to JSON: {}", jsonResult);
                return jsonResult;
            } catch (Exception e) {
                logger.error("Failed to serialize results array to JSON", e);
                throw new DataProcessingException("Failed to serialize results array to JSON", e);
            }
            
        } catch (Exception e) {
            logger.error("Error processing SQS event", e);
            throw new DataProcessingException("Failed to process SQS event", e);
        }
    }

    /**
     * Processes an individual SQS message.
     * 
     * @param message the SQS message to process
     * @param context the Lambda execution context
     * @return the processing result
     */
    private R processMessage(SQSEvent.SQSMessage message, Context context) {
        try {
            logger.debug("Processing message: {}", message.getMessageId());
            
            // Extract message body
            String messageBody = message.getBody();
            logger.debug("Message body: {}", messageBody);
            
            // Parse the message body to extract input data
            T inputData = parseMessage(messageBody);
            
            // Process the input data using the abstract method
            R result = processSqsTypeInternal(inputData);
            
            // Handle the result after processing
            handleResult(inputData, result);
            
            logger.debug("Successfully processed message: {}", message.getMessageId());
            return result;
            
        } catch (DataProcessingException e) {
            // Re-throw DataProcessingException directly
            throw e;
        } catch (Exception e) {
            logger.error("Error processing message: {}", message.getMessageId(), e);
            // Create a DataProcessingException and throw it
            DataProcessingException dataProcessingException = new DataProcessingException("Failed to process message: " + message.getMessageId(), e);
            
            // Depending on your error handling strategy, you might want to:
            // - Return null or a default result to continue processing other messages
            // - Re-throw the exception to trigger Lambda retry
            // - Send the message to a DLQ (Dead Letter Queue)
            throw dataProcessingException;
        }
    }

    /**
     * Parse the SQS message body to extract input data of type T using Jackson.
     * 
     * @param messageBody the raw message body from SQS
     * @return the parsed input data
     * @throws DataProcessingException if parsing fails
     */
    protected T parseMessage(String messageBody) {
        try {
            // Get the Class object for the generic type T
            JavaType type = objectMapper.getTypeFactory().constructType(getInputType());
            return objectMapper.readValue(messageBody, type);
        } catch (Exception e) {
            logger.error("Failed to parse message body: {}", messageBody, e);
            throw new DataProcessingException("Failed to parse message body", e);
        }
    }

    /**
     * Get the Class object for the input type T.
     * This method must be implemented by subclasses to provide the actual type.
     * 
     * @return the Class object for type T
     */
    protected abstract Class<T> getInputType();

    /**
     * Process the parsed input data and return a result of type R.
     * This method must be implemented by subclasses.
     *
     * @param input the parsed input data
     * @return the processing result
     */
    protected abstract R processSqsTypeInternal(T input);

    /**
     * Handle the processing result.
     * This method must be implemented by subclasses.
     *
     * @param input the parsed input data
     * @param result the processing result
     */
    protected abstract void handleResult(T input, R result);

    /**
     * Handle data processing exceptions.
     * This method must be implemented by subclasses to provide custom exception handling.
     *
     * @param input the parsed input data
     * @param exception the data processing exception that occurred
     */
    protected abstract void handleException(T input, DataProcessingException exception);
} 