package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing the output of a file splitting operation.
 * Extends S3FileProcessorOutput to include the output S3 file where split results are stored,
 * and adds specific fields for tracking split operation metadata.
 * Note: Lombok annotations are included for documentation but manual methods are used
 * due to compilation issues with the annotation processor in this environment.
 */
@Getter
@Setter
public class S3FileSplitOutput extends S3FileProcessorOutput {
    
    /**
     * The total number of records processed during the split operation.
     */
    private long totalRecordsProcessed;
    
    /**
     * The total number of file fragments created during the split operation.
     */
    private long totalFileFragments;
    
    /**
     * The S3 file containing metadata about the split operation.
     */
    private S3File splitMetadata;

    /**
     * Default constructor.
     */
    public S3FileSplitOutput() {
        super();
    }

    /**
     * Constructor with output S3 file.
     */
    public S3FileSplitOutput(S3File outputFile) {
        super(outputFile);
    }

    /**
     * Constructor with all fields.
     */
    public S3FileSplitOutput(S3File outputFile, long totalRecordsProcessed, long totalFileFragments, S3File splitMetadata) {
        super(outputFile);
        this.totalRecordsProcessed = totalRecordsProcessed;
        this.totalFileFragments = totalFileFragments;
        this.splitMetadata = splitMetadata;
    }

    // Manual getter and setter methods (Lombok annotations above for documentation)
    public long getTotalRecordsProcessed() {
        return totalRecordsProcessed;
    }

    public void setTotalRecordsProcessed(long totalRecordsProcessed) {
        this.totalRecordsProcessed = totalRecordsProcessed;
    }

    public long getTotalFileFragments() {
        return totalFileFragments;
    }

    public void setTotalFileFragments(long totalFileFragments) {
        this.totalFileFragments = totalFileFragments;
    }

    public S3File getSplitMetadata() {
        return splitMetadata;
    }

    public void setSplitMetadata(S3File splitMetadata) {
        this.splitMetadata = splitMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        S3FileSplitOutput that = (S3FileSplitOutput) o;

        if (totalRecordsProcessed != that.totalRecordsProcessed) return false;
        if (totalFileFragments != that.totalFileFragments) return false;
        return splitMetadata != null ? splitMetadata.equals(that.splitMetadata) : that.splitMetadata == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (totalRecordsProcessed ^ (totalRecordsProcessed >>> 32));
        result = 31 * result + (int) (totalFileFragments ^ (totalFileFragments >>> 32));
        result = 31 * result + (splitMetadata != null ? splitMetadata.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "S3FileSplitOutput{" +
                "outputFile=" + getOutputFile() +
                ", totalRecordsProcessed=" + totalRecordsProcessed +
                ", totalFileFragments=" + totalFileFragments +
                ", splitMetadata=" + splitMetadata +
                '}';
    }
} 