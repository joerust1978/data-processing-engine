package com.fifththird.edo.processingcore.lambda;

import com.fifththird.edo.processingcore.model.S3FileSplitOutput;
import com.fifththird.edo.processingcore.model.S3File;
import com.fifththird.edo.processingcore.model.S3FileSplitInput;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sfn.SfnClient;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.output.QueueOutputStream;
import org.apache.commons.io.input.QueueInputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Callable;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.core.async.BlockingInputStreamAsyncRequestBody;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.Upload;

/**
 * Abstract Lambda handler for processing S3 file splitting operations.
 * This handler extends S3FileProcessorLambdaHandler to provide specialized functionality
 * for file splitting operations with S3FileSplitInput and S3FileProcessorOutput.
 * 
 * @param <T> The type of the input data, must extend S3FileSplitInput
 * @param <R> The type of the result data, must extend S3FileSplitOutput
 */
public abstract class S3FileSplittingProcessorLambdaHandler<T extends S3FileSplitInput, R extends S3FileSplitOutput> 
        extends S3FileProcessorLambdaHandler<T, R> {

    private static final Logger logger = LoggerFactory.getLogger(S3FileSplittingProcessorLambdaHandler.class);
    
    private final ExecutorService executorService;
    private final S3TransferManager transferManager;

    /**
     * Constructor with Step Function client, S3 client, PGP utilities, ExecutorService, and TransferManager.
     * 
     * @param sfnClient the Step Function client for workflow integration
     * @param s3Client the S3 client for file operations
     * @param pgpUtilities the PGP utilities for encryption/decryption
     * @param executorService the executor service for concurrent processing
     * @param transferManager the S3 transfer manager for efficient uploads
     */
    public S3FileSplittingProcessorLambdaHandler(SfnClient sfnClient, S3Client s3Client, PgpUtilities pgpUtilities, ExecutorService executorService, S3TransferManager transferManager) {
        super(sfnClient, s3Client, pgpUtilities);
        this.executorService = executorService;
        this.transferManager = transferManager;
    }

    /**
     * Process the S3 object for file splitting operations.
     * This implementation reads the input stream in chunks and processes each fragment
     * using the processFragment method.
     * 
     * @param input the S3 file split input containing splitting configuration
     * @param s3ObjectStream the InputStream to the S3 object
     * @return the processing result
     * @throws DataProcessingException if processing fails
     */
    @Override
    protected R processS3Object(T input, InputStream s3ObjectStream) throws DataProcessingException {
        logger.info("Processing S3 object for file splitting with {} records per split", input.getRecordsPerSplit());
        
        try {
            // Validate splitting parameters
            if (input.getRecordsPerSplit() <= 0) {
                throw new DataProcessingException("Records per split must be greater than 0");
            }
            
            // Create a LinkedBlockingQueue with Integer type and capacity of 1,000,000
            LinkedBlockingQueue<Integer> queue = new LinkedBlockingQueue<>(1000000);
            
            // Variable to track total records read across all fragments
            long totalRecordsRead = 0;
            
            // Variable to track total fragments created
            long totalFragmentsCreated = 0;
            
            // Process the stream in fragments
            while (s3ObjectStream.available() > 0) {
                // Create a QueueOutputStream for the current fragment with the bounded queue
                QueueOutputStream fragmentStream = new QueueOutputStream(queue);
                
                // Create a QueueInputStream using the same queue
                QueueInputStream fragmentInputStream = fragmentStream.newQueueInputStream();
                
                // Create an anonymous Callable<Integer> to process the fragment
                Callable<Integer> fragmentProcessor = new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        // Save the result of processFragment as an integer
                        Integer result = processFragment(input, s3ObjectStream, fragmentStream, input.getRecordsPerSplit());
                        
                        // Close the fragmentStream object
                        fragmentStream.close();
                        
                        // Return the result from processFragment that was saved as an integer
                        return result;
                    }
                };
                
                // Execute the fragment processing using the ExecutorService
                Integer result = executorService.submit(fragmentProcessor).get();
                
                // Construct a BlockingInputStreamAsyncRequestBody using AsyncRequestBody.forBlockingInputStream
                BlockingInputStreamAsyncRequestBody asyncRequestBody = AsyncRequestBody.forBlockingInputStream(null);
                
                // Construct an upload request using the transferManager
                Upload upload = transferManager.upload(b -> b
                    .putObjectRequest(por -> por
                        .bucket(input.getOutputFile().getBucket())
                        .key(input.getOutputFile().getFileKey())
                    )
                    .requestBody(asyncRequestBody)
                );
                
                // Write the fragmentInputStream to the asyncRequestBody
                asyncRequestBody.writeInputStream(fragmentInputStream);
                
                // Call the upload completionFuture method and join on it
                upload.completionFuture().join();
                
                // Increment counters for tracking
                totalFragmentsCreated++;
                totalRecordsRead += result;
                
                // Log the fragment processing
                logger.debug("Processed fragment with QueueOutputStream, records processed: {}", result);
            }
            
            // Create and return the result object
            R result = createResultInstance();
            result.setTotalRecordsProcessed(totalRecordsRead);
            result.setTotalFileFragments(totalFragmentsCreated);
            result.setSplitMetadata(S3File.builder().bucket(input.getOutputFile().getBucket()).fileKey(input.getOutputFile().getFileKey() + "metadata/").build());
            result.setOutputFile(input.getOutputFile());
            return result;
            
        } catch (Exception e) {
            logger.error("Error while processing S3 object for splitting", e);
            throw new DataProcessingException("Failed to process S3 object for splitting", e);
        }
    }

    /**
     * Process a fragment of the input stream and write it to the output stream.
     * This method must be implemented by subclasses to provide specific logic for
     * reading records from the source and writing them to the current fragment.
     * 
     * @param input the S3 file split input containing splitting configuration
     * @param sourceStream the InputStream containing the source data
     * @param fragmentStream the OutputStream to write the current fragment to
     * @param recordsToRead the number of records to read and write to the fragment
     * @return the number of records processed in this fragment
     * @throws DataProcessingException if processing fails
     */
    protected abstract Integer processFragment(T input, InputStream sourceStream, OutputStream fragmentStream, long recordsToRead) throws DataProcessingException;
    
    /**
     * Create a result object instance of type R with the processing statistics.
     * This method must be implemented by subclasses to provide specific result creation logic.
     * 
     * @param totalRecordsRead the total number of records processed
     * @param totalFragmentsCreated the total number of fragments created
     * @return the result object of type R
     * @throws DataProcessingException if result creation fails
     */
    protected abstract R createResultInstance() throws DataProcessingException;
} 