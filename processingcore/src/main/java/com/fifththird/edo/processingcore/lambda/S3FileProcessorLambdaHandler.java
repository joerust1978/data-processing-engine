package com.fifththird.edo.processingcore.lambda;

import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.model.S3FileProcessorDefinition;
import com.fifththird.edo.processingcore.model.S3FileProcessorOutput;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sfn.SfnClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Abstract Lambda handler for processing S3 file operations with Step Function integration.
 * This handler extends StepFunctionSqsLambdaHandler with specific generic constraints:
 * - T must extend S3FileProcessorDefinition (input type)
 * - R must extend S3FileProcessorOutput (output type)
 * 
 * This provides a type-safe foundation for S3 file processing operations
 * while maintaining Step Function integration capabilities.
 * 
 * @param <T> The type of the input data, must extend S3FileProcessorDefinition
 * @param <R> The type of the result data, must extend S3FileProcessorOutput
 */
public abstract class S3FileProcessorLambdaHandler<T extends S3FileProcessorDefinition, R extends S3FileProcessorOutput> 
        extends StepFunctionSqsLambdaHandler<T, R> {

    private static final Logger logger = LoggerFactory.getLogger(S3FileProcessorLambdaHandler.class);
    
    private final S3Client s3Client;
    private final PgpUtilities pgpUtilities;
    
    @Value("${pgp.private.key:}")
    private String pgpPrivateKey;
    
    @Value("${pgp.passphrase:}")
    private String pgpPassphrase;
    
    @Value("${pgp.public.key:}")
    private String pgpPublicKey;

    /**
     * Constructor that initializes the Step Functions client, S3 client, and PGP utilities.
     * 
     * @param sfnClient the AWS Step Functions client
     * @param s3Client the AWS S3 client
     * @param pgpUtilities the PGP utilities for encryption/decryption
     */
    public S3FileProcessorLambdaHandler(SfnClient sfnClient, S3Client s3Client, PgpUtilities pgpUtilities) {
        super(sfnClient);
        this.s3Client = s3Client;
        this.pgpUtilities = pgpUtilities;
    }

    /**
     * Process the S3 file processor definition and return the corresponding output.
     * This method provides a default implementation for S3 file processing operations.
     * Subclasses can override this method to provide custom processing logic.
     *
     * @param input the S3 file processor definition containing input and output file information
     * @return the S3 file processor output with processing results
     * @throws DataProcessingException if processing fails
     */
    @Override
    protected R processSqsTypeInternal(T input) throws DataProcessingException {
        logger.info("Processing S3 file operation for input: {}", input);
        try {
            // Validate input
            if (input == null) {
                throw new DataProcessingException("Input cannot be null");
            }
            if (input.getInputFile() == null) {
                throw new DataProcessingException("Input file is required");
            }
            if (input.getOutputFile() == null) {
                throw new DataProcessingException("Output file is required");
            }
            logger.debug("Processing input file: {}", input.getInputFile());
            logger.debug("Output will be written to: {}", input.getOutputFile());
            // Perform the actual S3 file processing
            R result = processS3File(input);
            logger.info("Successfully processed S3 file operation");
            return result;
        } catch (Exception e) {
            logger.error("Data processing error during S3 file operation", e);
            throw new DataProcessingException("Failed to process S3 file operation", e);
        }
    }
    
    /**
     * Process the S3 file according to the processor definition.
     * This method creates an InputStream to the S3 object and handles gzip decompression
     * and PGP decryption if needed. It should be overridden by subclasses to provide 
     * specific processing logic.
     * 
     * @param input the S3 file processor definition
     * @return the processing result
     * @throws DataProcessingException if processing fails
     */
    protected R processS3File(T input) throws DataProcessingException {
        // Null and config checks first
        if (input == null) {
            throw new DataProcessingException("Input cannot be null");
        }
        if (input.getInputFile() == null) {
            throw new DataProcessingException("Input file is required");
        }
        if (input.getOutputFile() == null) {
            throw new DataProcessingException("Output file is required");
        }
        if (input.getInputFile().isPgpEncrypted()) {
            if (pgpPrivateKey == null || pgpPrivateKey.trim().isEmpty()) {
                throw new DataProcessingException("pgp.private.key configuration is required for PGP decryption");
            }
            if (pgpPassphrase == null || pgpPassphrase.trim().isEmpty()) {
                throw new DataProcessingException("pgp.passphrase configuration is required for PGP decryption");
            }
        }
        logger.debug("Creating InputStream for S3 object: bucket={}, key={}, gzipped={}, encrypted={}", 
                    input.getInputFile().getBucket(), input.getInputFile().getFileKey(),
                    input.getInputFile().isGzipped(), input.getInputFile().isPgpEncrypted());
        
        InputStream processedStream = null;
        BufferedInputStream bufferedStream = null;
        
        try {
            // Create the GetObjectRequest using the input file information
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(input.getInputFile().getBucket())
                    .key(input.getInputFile().getFileKey())
                    .build();
            
            logger.debug("Retrieving S3 object with request: {}", getObjectRequest);
            
            // Get the S3 object and create an InputStream
            ResponseInputStream<GetObjectResponse> s3ObjectStream = s3Client.getObject(getObjectRequest);
            processedStream = s3ObjectStream;
            
            logger.info("Successfully created InputStream for S3 object: bucket={}, key={}", 
                       input.getInputFile().getBucket(), input.getInputFile().getFileKey());
            
            // Handle gzip decompression if needed
            if (input.getInputFile().isGzipped()) {
                logger.debug("Wrapping S3 object stream with GZIPInputStream for decompression");
                processedStream = new GZIPInputStream(processedStream);
                logger.debug("GZIPInputStream wrapper applied successfully");
            }
            
            // Handle PGP decryption if needed
            if (input.getInputFile().isPgpEncrypted()) {
                logger.debug("Wrapping stream with PGP decryption");
                
                try {
                    processedStream = pgpUtilities.wrapWithDecryption(processedStream, pgpPrivateKey, pgpPassphrase);
                    logger.debug("PGP decryption wrapper applied successfully");
                } catch (PGPException | IOException e) {
                    logger.error("Failed to apply PGP decryption to S3 object stream", e);
                    throw new DataProcessingException("Failed to decrypt PGP encrypted S3 object", e);
                }
            }
            
            // Wrap the processed stream in a BufferedInputStream for better performance
            bufferedStream = new BufferedInputStream(processedStream);
            
            // Process the S3 object using the buffered InputStream
            R result = processS3Object(input, bufferedStream);
            
            return result;
            
        } catch (S3Exception e) {
            logger.error("S3 error while retrieving object: bucket={}, key={}, error={}", 
                        input.getInputFile().getBucket(), input.getInputFile().getFileKey(), e.getMessage(), e);
            throw new DataProcessingException("Failed to retrieve S3 object: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error while processing S3 object: bucket={}, key={}", 
                        input.getInputFile().getBucket(), input.getInputFile().getFileKey(), e);
            throw new DataProcessingException("Failed to process S3 object", e);
        } finally {
            // Close the buffered stream if it exists
            if (bufferedStream != null) {
                try {
                    bufferedStream.close();
                } catch (IOException e) {
                    logger.warn("Failed to close buffered stream", e);
                }
            }
        }
    }
    
    /**
     * Process the S3 object using the provided InputStream.
     * This method must be implemented by subclasses to provide specific processing logic.
     * 
     * @param input the S3 file processor definition
     * @param s3ObjectStream the InputStream to the S3 object
     * @return the processing result
     * @throws DataProcessingException if processing fails
     */
    protected abstract R processS3Object(T input, InputStream s3ObjectStream) throws DataProcessingException;


} 