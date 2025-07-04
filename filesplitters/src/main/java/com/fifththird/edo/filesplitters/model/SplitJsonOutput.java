package com.fifththird.edo.filesplitters.model;

import com.fifththird.edo.processingcore.model.S3File;

/**
 * Model class representing the output of a JSON file splitting operation.
 * Contains information about the generated S3 file, key prefix for state files,
 * and the total record count across all files.
 */
public class SplitJsonOutput {
    
    /**
     * The S3 file containing the split JSON fragments.
     */
    private S3File s3File;
    
    /**
     * Key prefix for state files related to this splitting operation.
     */
    private String stateFileKeyPrefix;
    
    /**
     * Total record count across all files.
     */
    private Long recordCount;
    
    /**
     * Default constructor.
     */
    public SplitJsonOutput() {
    }
    
    /**
     * Constructor with all fields.
     * 
     * @param s3File the S3 file containing split JSON fragments
     * @param stateFileKeyPrefix key prefix for state files
     * @param recordCount total record count across all files
     */
    public SplitJsonOutput(S3File s3File, String stateFileKeyPrefix, Long recordCount) {
        this.s3File = s3File;
        this.stateFileKeyPrefix = stateFileKeyPrefix;
        this.recordCount = recordCount;
    }
    
    /**
     * Gets the S3 file containing the split JSON fragments.
     * 
     * @return the S3 file
     */
    public S3File getS3File() {
        return s3File;
    }
    
    /**
     * Sets the S3 file containing the split JSON fragments.
     * 
     * @param s3File the S3 file to set
     */
    public void setS3File(S3File s3File) {
        this.s3File = s3File;
    }
    
    /**
     * Gets the key prefix for state files.
     * 
     * @return the state file key prefix
     */
    public String getStateFileKeyPrefix() {
        return stateFileKeyPrefix;
    }
    
    /**
     * Sets the key prefix for state files.
     * 
     * @param stateFileKeyPrefix the state file key prefix to set
     */
    public void setStateFileKeyPrefix(String stateFileKeyPrefix) {
        this.stateFileKeyPrefix = stateFileKeyPrefix;
    }
    
    /**
     * Gets the total record count across all files.
     * 
     * @return the record count
     */
    public Long getRecordCount() {
        return recordCount;
    }
    
    /**
     * Sets the total record count across all files.
     * 
     * @param recordCount the record count to set
     */
    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }
    
    @Override
    public String toString() {
        return "SplitJsonOutput{" +
                "s3File=" + s3File +
                ", stateFileKeyPrefix='" + stateFileKeyPrefix + '\'' +
                ", recordCount=" + recordCount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SplitJsonOutput that = (SplitJsonOutput) o;
        
        if (s3File != null ? !s3File.equals(that.s3File) : that.s3File != null) return false;
        if (stateFileKeyPrefix != null ? !stateFileKeyPrefix.equals(that.stateFileKeyPrefix) : that.stateFileKeyPrefix != null)
            return false;
        return recordCount != null ? recordCount.equals(that.recordCount) : that.recordCount == null;
    }
    
    @Override
    public int hashCode() {
        int result = s3File != null ? s3File.hashCode() : 0;
        result = 31 * result + (stateFileKeyPrefix != null ? stateFileKeyPrefix.hashCode() : 0);
        result = 31 * result + (recordCount != null ? recordCount.hashCode() : 0);
        return result;
    }
} 