package com.fifththird.edo.processingcore.lambda;

import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.model.S3FileProcessorDefinition;
import com.fifththird.edo.processingcore.model.S3FileProcessorOutput;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.QueueOutputStream;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sfn.SfnClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.Upload;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Abstract Lambda handler for processing S3 file operations that output results.
 * This handler extends S3FileProcessorLambdaHandler with specific generic constraints:
 * - T must extend S3FileProcessorDefinition (input type)
 * - R must extend S3FileProcessorOutput (output type)
 * 
 * This provides a type-safe foundation for S3 file processing operations
 * that produce output while maintaining Step Function integration capabilities.
 * 
 * @param <T> The type of the input data, must extend S3FileProcessorDefinition
 * @param <R> The type of the result data, must extend S3FileProcessorOutput
 */
public abstract class S3FileProcessorOutputtingLambdaHandler<T extends S3FileProcessorDefinition, R extends S3FileProcessorOutput> 
        extends S3FileProcessorLambdaHandler<T, R> {

    private final ExecutorService executorService;
    private final S3TransferManager transferManager;

    /**
     * Constructor that initializes the Step Functions client, S3 client, PGP utilities, executor service, and transfer manager.
     * 
     * @param sfnClient the AWS Step Functions client
     * @param s3Client the AWS S3 client
     * @param pgpUtilities the PGP utilities for encryption/decryption
     * @param executorService the executor service for concurrent processing
     * @param transferManager the S3 transfer manager for file operations
     */
    public S3FileProcessorOutputtingLambdaHandler(SfnClient sfnClient, S3Client s3Client, PgpUtilities pgpUtilities, ExecutorService executorService, S3TransferManager transferManager) {
        super(sfnClient, s3Client, pgpUtilities);
        this.executorService = executorService;
        this.transferManager = transferManager;
    }

    /**
     * Process the S3 object and return the corresponding output.
     * This method creates a queue-based stream processing pipeline.
     *
     * @param input the S3 file processor definition containing input and output file information
     * @param s3ObjectStream the InputStream to the S3 object
     * @return the processing result
     * @throws DataProcessingException if processing fails
     */
    @Override
    protected R processS3Object(T input, InputStream s3ObjectStream) throws DataProcessingException {
        // Create a LinkedBlockingQueue with Integer generic type and capacity of 1000000
        LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>(1000000);
        
        // Create a QueueOutputStream and pass the LinkedBlockingQueue to the constructor
        QueueOutputStream queueOutputStream = new QueueOutputStream(queue);
        
        // Use the QueueOutputStream object to create a QueueInputStream by calling newQueueInputStream
        QueueInputStream queueInputStream = queueOutputStream.newQueueInputStream();
        
        // Create an anonymous Callable object that calls processS3ObjectStream
        Callable<R> processingTask = new Callable<R>() {
            @Override
            public R call() throws Exception {
                return processS3ObjectStream(input, s3ObjectStream, queueOutputStream);
            }
        };
        
        // Use the executorService to process the processingTask
        try {
            Future<R> future = executorService.submit(processingTask);
            BlockingInputStreamAsyncRequestBody blockingInputStreamAsyncRequestBody = AsyncRequestBody.forBlockingInputStream(null);
            
            // Use the transferManager to execute an upload request
            Upload upload = transferManager.upload(b -> b
                .putObjectRequest(por -> por
                    .bucket(input.getOutputFile().getBucket())
                    .key(input.getOutputFile().getFileKey())
                )
                .requestBody(blockingInputStreamAsyncRequestBody)
            );
            
            // Write the queueInputStream contents to the blockingInputStreamAsyncRequestBody object
            blockingInputStreamAsyncRequestBody.writeInputStream(queueInputStream);
            
            // Call the upload completionFuture method and join on it
            upload.completionFuture().join();
            
            return future.get();
        } catch (Exception e) {
            throw new DataProcessingException("Failed to process S3 object using executor service", e);
        }
    }

    /**
     * Process the S3 object stream and write output data to the provided output stream.
     * This method should be implemented by subclasses to provide specific processing logic
     * that reads from the input stream and writes processed data to the output stream.
     *
     * @param input the S3 file processor definition containing input and output file information
     * @param s3ObjectStream the InputStream to the S3 object
     * @param s3OutputData the OutputStream where processed data should be written
     * @return the processing result
     * @throws DataProcessingException if processing fails
     */
    protected abstract R processS3ObjectStream(T input, InputStream s3ObjectStream, OutputStream s3OutputData) throws DataProcessingException;
} 