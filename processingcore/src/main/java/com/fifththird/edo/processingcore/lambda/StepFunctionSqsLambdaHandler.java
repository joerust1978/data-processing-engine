package com.fifththird.edo.processingcore.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.services.sfn.model.SendTaskSuccessRequest;
import software.amazon.awssdk.services.sfn.model.SendTaskFailureRequest;
import software.amazon.awssdk.services.sfn.model.SfnException;

/**
 * Abstract base class for Step Function SQS Lambda handlers.
 * Extends SQSEventLambdaHandler to provide Step Function specific functionality.
 * 
 * @param <T> The type of the input message body
 * @param <R> The type of the result returned from processing
 */
public abstract class StepFunctionSqsLambdaHandler<T extends com.fifththird.edo.processingcore.model.StepFunctionInput, R> extends SQSEventLambdaHandler<T, R> {

    private static final Logger logger = LoggerFactory.getLogger(StepFunctionSqsLambdaHandler.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SfnClient sfnClient;

    /**
     * Constructor that initializes the Step Functions client.
     * 
     * @param sfnClient the AWS Step Functions client
     */
    public StepFunctionSqsLambdaHandler(SfnClient sfnClient) {
        super();
        this.sfnClient = sfnClient;
    }

    /**
     * Handles successful processing results by sending a success message to Step Functions.
     * Serializes the result to JSON and sends it as the output of the Step Function task.
     *
     * @param input the input data
     * @param result the processing result to handle
     */
    @Override
    protected void handleResult(T input, R result) {
        try {
            // Serialize the result to JSON string
            String resultJson = objectMapper.writeValueAsString(result);
            logger.debug("Serialized result to JSON: {}", resultJson);
            // Get the task token from the input object
            String taskToken = input.getTaskToken();
            if (taskToken != null) {
                // Send success message to Step Functions
                SendTaskSuccessRequest successRequest = SendTaskSuccessRequest.builder()
                        .taskToken(taskToken)
                        .output(resultJson)
                        .build();
                sfnClient.sendTaskSuccess(successRequest);
                logger.info("Successfully sent task success to Step Functions with token: {}", taskToken);
            } else {
                logger.warn("No task token available, skipping Step Function success notification");
            }
        } catch (Exception e) {
            logger.error("Failed to handle result and send success to Step Functions", e);
            throw new DataProcessingException("Failed to handle result", e);
        }
    }

    /**
     * Handles processing exceptions by sending a failure message to Step Functions.
     * Sends the exception details as the error information for the Step Function task.
     *
     * @param input the input data
     * @param exception the data processing exception that occurred
     */
    @Override
    protected void handleException(T input, DataProcessingException exception) {
        try {
            logger.error("Handling DataProcessingException: {}", exception.getMessage(), exception);
            // Get the task token from the input object
            String taskToken = input.getTaskToken();
            if (taskToken != null) {
                // Send failure message to Step Functions
                SendTaskFailureRequest failureRequest = SendTaskFailureRequest.builder()
                        .taskToken(taskToken)
                        .error(exception.getClass().getSimpleName())
                        .cause(exception.getMessage())
                        .build();
                sfnClient.sendTaskFailure(failureRequest);
                logger.info("Successfully sent task failure to Step Functions with token: {}", taskToken);
            } else {
                logger.warn("No task token available, skipping Step Function failure notification");
            }
        } catch (SfnException e) {
            logger.error("Failed to send task failure to Step Functions", e);
        } catch (Exception e) {
            logger.error("Unexpected error while handling exception", e);
        }
    }
} 