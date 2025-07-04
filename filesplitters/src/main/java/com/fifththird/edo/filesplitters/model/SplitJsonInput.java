package com.fifththird.edo.filesplitters.model;

import com.fifththird.edo.processingcore.model.S3File;
import com.fifththird.edo.processingcore.model.S3FileProcessorDefinition;

/**
 * Model class for JSON file splitting input parameters.
 * Extends S3FileProcessorDefinition to inherit S3 file processing capabilities
 * and adds specific parameters for JSON file splitting operations.
 */
public class SplitJsonInput extends S3FileProcessorDefinition {
    
    private Integer recordsPerSplit;
    
    /**
     * Default constructor.
     */
    public SplitJsonInput() {
        super();
    }
    
    /**
     * Gets the number of records per split file.
     * 
     * @return the number of records per split file
     */
    public Integer getRecordsPerSplit() {
        return recordsPerSplit;
    }
    
    /**
     * Sets the number of records per split file.
     * 
     * @param recordsPerSplit the number of records per split file
     */
    public void setRecordsPerSplit(Integer recordsPerSplit) {
        this.recordsPerSplit = recordsPerSplit;
    }
    
    @Override
    public String toString() {
        return "SplitJsonInput{" +
                "taskToken='" + getTaskToken() + '\'' +
                ", taskName='" + getTaskName() + '\'' +
                ", inputFile=" + getInputFile() +
                ", outputFile=" + getOutputFile() +
                ", recordsPerSplit=" + recordsPerSplit +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        
        SplitJsonInput that = (SplitJsonInput) o;
        
        return recordsPerSplit != null ? recordsPerSplit.equals(that.recordsPerSplit) : that.recordsPerSplit == null;
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (recordsPerSplit != null ? recordsPerSplit.hashCode() : 0);
        return result;
    }
} 