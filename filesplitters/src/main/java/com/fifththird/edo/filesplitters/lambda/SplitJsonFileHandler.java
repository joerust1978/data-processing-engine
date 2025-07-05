package com.fifththird.edo.filesplitters.lambda;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fifththird.edo.filesplitters.model.SplitJsonInput;
import com.fifththird.edo.filesplitters.model.SplitJsonOutput;
import com.fifththird.edo.processingcore.exception.DataProcessingException;
import com.fifththird.edo.processingcore.lambda.StepFunctionSqsLambdaHandler;
import com.fifththird.edo.processingcore.model.S3File;
import com.fifththird.edo.processingcore.util.PgpUtilities;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.sfn.SfnClient;
import org.bouncycastle.openpgp.PGPException;
import org.springframework.beans.factory.annotation.Value;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Lambda handler for splitting JSON files based on SQS events with Step Function integration.
 * Extends StepFunctionSqsLambdaHandler to process SQS messages containing SplitJsonInput data
 * and automatically handle Step Function success/failure notifications.
 * Input type is SplitJsonInput and output type is SplitJsonOutput for this implementation.
 */
public class SplitJsonFileHandler extends StepFunctionSqsLambdaHandler<SplitJsonInput, SplitJsonOutput> {

    private static final Logger logger = LoggerFactory.getLogger(SplitJsonFileHandler.class);
    
    private final S3Client s3Client;
    private final PgpUtilities pgpUtilities;
    private final ObjectMapper objectMapper;
    private final JsonFactory jsonFactory;
    
    @Value("${pgp.private.key:}")
    private String pgpPrivateKey;
    
    @Value("${pgp.passphrase:}")
    private String pgpPassphrase;
    
    @Value("${pgp.public.key:}")
    private String pgpPublicKey;

    /**
     * Constructor that initializes the handler with Step Functions and S3 clients.
     * 
     * @param sfnClient the AWS Step Functions client
     * @param s3Client the AWS S3 client
     * @param pgpUtilities the PGP utilities for decryption/encryption
     */
    public SplitJsonFileHandler(SfnClient sfnClient, S3Client s3Client, PgpUtilities pgpUtilities) {
        super(sfnClient);
        this.s3Client = s3Client;
        this.pgpUtilities = pgpUtilities;
        this.objectMapper = new ObjectMapper();
        this.jsonFactory = new JsonFactory();
    }

    /**
     * Provides the Class for Jackson deserialization.
     * 
     * @return the Class for SplitJsonInput
     */
    @Override
    protected Class<SplitJsonInput> getInputType() {
        return SplitJsonInput.class;
    }

    /**
     * Processes the parsed SplitJsonInput data.
     * This method reads a JSON file from S3, splits it into fragments based on recordsPerSplit,
     * and writes the fragments back to S3.
     * 
     * @param input the parsed SplitJsonInput data
     * @return the processing result (SplitJsonOutput with output information)
     */
    @Override
    protected SplitJsonOutput processSqsTypeInternal(SplitJsonInput input) {
        logger.info("Starting JSON file splitting process for input: {}", input);
        try {
            validateInput(input);
            int recordsPerSplit = input.getRecordsPerSplit();
            S3File inputFile = input.getInputFile();
            S3File outputFile = input.getOutputFile();

            // Open S3 input stream
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(inputFile.getBucket())
                    .key(inputFile.getFileKey())
                    .build();
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            InputStream inputStream = s3Object;
            
            // Handle GZIP compression
            if (inputFile.isGzipped()) {
                logger.debug("Decompressing GZIP content");
                inputStream = new GZIPInputStream(inputStream);
            }
            
            // Handle PGP encryption
            if (inputFile.isPgpEncrypted()) {
                logger.debug("Decrypting PGP content");
                
                if (pgpPrivateKey == null || pgpPrivateKey.trim().isEmpty()) {
                    throw new DataProcessingException("pgp.private.key configuration is required for PGP decryption");
                }
                if (pgpPassphrase == null || pgpPassphrase.trim().isEmpty()) {
                    throw new DataProcessingException("pgp.passphrase configuration is required for PGP decryption");
                }
                
                try {
                    inputStream = pgpUtilities.wrapWithDecryption(inputStream, pgpPrivateKey, pgpPassphrase);
                    logger.debug("Successfully wrapped input stream with PGP decryption");
                } catch (PGPException | IOException e) {
                    logger.error("Failed to decrypt PGP content", e);
                    throw new DataProcessingException("Failed to decrypt PGP content", e);
                }
            }
            
            // Wrap in BufferedInputStream for better performance
            inputStream = new BufferedInputStream(inputStream);
            
            // Use Jackson streaming API to read one object at a time
            JsonFactory factory = objectMapper.getFactory();
            JsonParser parser = factory.createParser(inputStream);
            List<JsonNode> buffer = new ArrayList<>(recordsPerSplit);
            int fragmentIndex = 0;
            int totalFragments = 0;
            int totalObjects = 0;
            while (parser.nextToken() != null) {
                if (parser.currentToken() == JsonToken.START_OBJECT) {
                    JsonNode node = objectMapper.readTree(parser);
                    buffer.add(node);
                    totalObjects++;
                    if (buffer.size() == recordsPerSplit) {
                        writeFragment(buffer, outputFile, fragmentIndex, -1); // -1 for totalFragments, will update later
                        fragmentIndex++;
                        buffer.clear();
                    }
                }
            }
            // Write any remaining objects
            if (!buffer.isEmpty()) {
                writeFragment(buffer, outputFile, fragmentIndex, -1);
                fragmentIndex++;
            }
            totalFragments = fragmentIndex;
            logger.info("Successfully split file into {} fragments ({} objects)", totalFragments, totalObjects);
            
            // Create SplitJsonOutput with the results
            SplitJsonOutput output = new SplitJsonOutput();
            output.setOutputFileData(outputFile);
            output.setRecordCount((long) totalObjects);
            
            // Create state file data (using the output file as base but with state prefix)
            S3File stateFileData = S3File.builder()
                    .bucket(outputFile.getBucket())
                    .fileKey(outputFile.getFileKey() + "state/")
                    .gzipped(outputFile.isGzipped())
                    .pgpEncrypted(outputFile.isPgpEncrypted())
                    .build();
            output.setStateFileData(stateFileData);
            
            return output;
        } catch (Exception e) {
            logger.error("Failed to process JSON file splitting", e);
            throw new DataProcessingException("Failed to process JSON file splitting", e);
        }
    }

    private void writeFragment(List<JsonNode> buffer, S3File s3File, int fragmentIndex, int totalFragments) {
        try {
            ArrayNode arrayNode = objectMapper.createArrayNode();
            for (JsonNode node : buffer) {
                arrayNode.add(node);
            }
            byte[] content = objectMapper.writeValueAsBytes(arrayNode);
            InputStream inputStream = new ByteArrayInputStream(content);
            if (s3File.isGzipped()) {
                ByteArrayOutputStream compressedOutput = new ByteArrayOutputStream();
                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedOutput)) {
                    IOUtils.copy(inputStream, gzipOutputStream);
                }
                content = compressedOutput.toByteArray();
                inputStream = new ByteArrayInputStream(content);
            }
            if (s3File.isPgpEncrypted()) {
                logger.debug("Encrypting fragment {} with PGP", fragmentIndex);
                
                if (pgpPublicKey == null || pgpPublicKey.trim().isEmpty()) {
                    throw new DataProcessingException("pgp.public.key configuration is required for PGP encryption");
                }
                
                try {
                    ByteArrayOutputStream fragmentOutput = new ByteArrayOutputStream();
                    OutputStream encryptedOutput = pgpUtilities.wrapWithEncryption(fragmentOutput, pgpPublicKey);
                    
                    // Write the fragment content to the encrypted stream
                    encryptedOutput.write(content);
                    encryptedOutput.close();
                    
                    content = fragmentOutput.toByteArray();
                    inputStream = new ByteArrayInputStream(content);
                    logger.debug("Successfully encrypted fragment {} with PGP", fragmentIndex);
                } catch (PGPException | IOException e) {
                    logger.error("Failed to encrypt fragment {} with PGP", fragmentIndex, e);
                    throw new DataProcessingException("Failed to encrypt fragment with PGP", e);
                }
            }
            String fragmentKey = generateFragmentKey(s3File.getFileKey(), fragmentIndex, totalFragments > 0 ? totalFragments : fragmentIndex + 1);
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3File.getBucket())
                    .key(fragmentKey)
                    .contentLength((long) content.length)
                    .build();
            RequestBody requestBody = RequestBody.fromInputStream(inputStream, content.length);
            s3Client.putObject(putObjectRequest, requestBody);
            logger.debug("Successfully uploaded fragment {} to S3: {}", fragmentIndex, fragmentKey);
            
            // Create S3File object for this fragment
            S3File fragmentS3File = S3File.builder()
                    .bucket(s3File.getBucket())
                    .fileKey(s3File.getFileKey() + "state/" + fragmentIndex)
                    .gzipped(false)
                    .pgpEncrypted(false)
                    .build();
            
            // Serialize the S3File object to JSON string
            String fragmentJson = objectMapper.writeValueAsString(fragmentS3File);
            
            // Write the JSON string to S3 with state/ prefix and fragment index
            String stateKey = s3File.getFileKey() + "state/" + fragmentIndex;
            PutObjectRequest statePutRequest = PutObjectRequest.builder()
                    .bucket(s3File.getBucket())
                    .key(stateKey)
                    .contentType("application/json")
                    .build();
            RequestBody stateRequestBody = RequestBody.fromString(fragmentJson);
            s3Client.putObject(statePutRequest, stateRequestBody);
            logger.debug("Successfully uploaded fragment state JSON to S3: {}", stateKey);
        } catch (IOException e) {
            logger.error("Failed to write fragment to S3", e);
            throw new DataProcessingException("Failed to write fragment to S3", e);
        }
    }
    
    /**
     * Validates the input parameters.
     * 
     * @param input the input to validate
     * @throws DataProcessingException if validation fails
     */
    private void validateInput(SplitJsonInput input) {
        if (input.getInputFile() == null) {
            throw new DataProcessingException("Input file is required");
        }
        if (input.getOutputFile() == null) {
            throw new DataProcessingException("Output file is required");
        }
        if (input.getRecordsPerSplit() == null || input.getRecordsPerSplit() <= 0) {
            throw new DataProcessingException("Records per split must be greater than 0");
        }
        if (input.getInputFile().getBucket() == null || input.getInputFile().getBucket().trim().isEmpty()) {
            throw new DataProcessingException("Input file bucket is required");
        }
        if (input.getInputFile().getFileKey() == null || input.getInputFile().getFileKey().trim().isEmpty()) {
            throw new DataProcessingException("Input file key is required");
        }
        if (input.getOutputFile().getBucket() == null || input.getOutputFile().getBucket().trim().isEmpty()) {
            throw new DataProcessingException("Output file bucket is required");
        }
        if (input.getOutputFile().getFileKey() == null || input.getOutputFile().getFileKey().trim().isEmpty()) {
            throw new DataProcessingException("Output file key is required");
        }
    }
    
    /**
     * Generates a fragment key based on the original key and fragment information.
     * 
     * @param originalKey the original S3 key
     * @param fragmentIndex the fragment index (0-based)
     * @param totalFragments the total number of fragments
     * @return the generated fragment key
     */
    private String generateFragmentKey(String originalKey, int fragmentIndex, int totalFragments) {
        // Remove file extension if present
        int lastDotIndex = originalKey.lastIndexOf('.');
        String baseKey = lastDotIndex > 0 ? originalKey.substring(0, lastDotIndex) : originalKey;
        String extension = lastDotIndex > 0 ? originalKey.substring(lastDotIndex) : "";
        
        // Generate fragment key with zero-padded index
        String paddedIndex = String.format("%04d", fragmentIndex + 1);
        String totalFragmentsStr = String.format("%04d", totalFragments);
        
        return String.format("%s_fragment_%s_of_%s%s", baseKey, paddedIndex, totalFragmentsStr, extension);
    }
} 