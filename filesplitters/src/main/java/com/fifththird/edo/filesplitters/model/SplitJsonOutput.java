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
    private S3File outputFileData;
    
    /**
     * S3 file data for state files related to this splitting operation.
     */
    private S3File stateFileData;
    
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
     * @param outputFileData the S3 file containing split JSON fragments
     * @param stateFileData S3 file data for state files
     * @param recordCount total record count across all files
     */
    public SplitJsonOutput(S3File outputFileData, S3File stateFileData, Long recordCount) {
        this.outputFileData = outputFileData;
        this.stateFileData = stateFileData;
        this.recordCount = recordCount;
    }
    
    /**
     * Gets the S3 file containing the split JSON fragments.
     * 
     * @return the S3 file
     */
    public S3File getOutputFileData() {
        return outputFileData;
    }
    
    /**
     * Sets the S3 file containing the split JSON fragments.
     * 
     * @param outputFileData the S3 file to set
     */
    public void setOutputFileData(S3File outputFileData) {
        this.outputFileData = outputFileData;
    }
    
    /**
     * Gets the S3 file data for state files.
     * 
     * @return the state file data
     */
    public S3File getStateFileData() {
        return stateFileData;
    }
    
    /**
     * Sets the S3 file data for state files.
     * 
     * @param stateFileData the state file data to set
     */
    public void setStateFileData(S3File stateFileData) {
        this.stateFileData = stateFileData;
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
                "outputFileData=" + outputFileData +
                ", stateFileData=" + stateFileData +
                ", recordCount=" + recordCount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        SplitJsonOutput that = (SplitJsonOutput) o;
        
        if (outputFileData != null ? !outputFileData.equals(that.outputFileData) : that.outputFileData != null) return false;
        if (stateFileData != null ? !stateFileData.equals(that.stateFileData) : that.stateFileData != null)
            return false;
        return recordCount != null ? recordCount.equals(that.recordCount) : that.recordCount == null;
    }
    
    @Override
    public int hashCode() {
        int result = outputFileData != null ? outputFileData.hashCode() : 0;
        result = 31 * result + (stateFileData != null ? stateFileData.hashCode() : 0);
        result = 31 * result + (recordCount != null ? recordCount.hashCode() : 0);
        return result;
    }
} 