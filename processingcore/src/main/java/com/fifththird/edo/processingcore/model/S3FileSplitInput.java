package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing input for S3 file splitting operations.
 * Extends S3FileProcessorDefinition to include task token and task name for Step Function integration,
 * and adds a recordsPerSplit field to specify how many records should be in each split.
 * Note: Lombok annotations are included for documentation but manual methods are used
 * due to compilation issues with the annotation processor in this environment.
 */
@Getter
@Setter
public class S3FileSplitInput extends S3FileProcessorDefinition {
    
    /**
     * The number of records to include in each split file.
     */
    private long recordsPerSplit;

    /**
     * Default constructor.
     */
    public S3FileSplitInput() {
        super();
    }

    /**
     * Constructor with S3 files and records per split.
     */
    public S3FileSplitInput(S3File inputFile, S3File outputFile, long recordsPerSplit) {
        super(inputFile, outputFile);
        this.recordsPerSplit = recordsPerSplit;
    }

    /**
     * Constructor with all fields including Step Function parameters.
     */
    public S3FileSplitInput(String taskToken, String taskName, S3File inputFile, S3File outputFile, long recordsPerSplit) {
        super(taskToken, taskName, inputFile, outputFile);
        this.recordsPerSplit = recordsPerSplit;
    }

    // Manual getter and setter methods (Lombok annotations above for documentation)
    public long getRecordsPerSplit() {
        return recordsPerSplit;
    }

    public void setRecordsPerSplit(long recordsPerSplit) {
        this.recordsPerSplit = recordsPerSplit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        S3FileSplitInput that = (S3FileSplitInput) o;

        return recordsPerSplit == that.recordsPerSplit;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (int) (recordsPerSplit ^ (recordsPerSplit >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "S3FileSplitInput{" +
                "taskToken='" + super.getTaskToken() + '\'' +
                ", taskName='" + super.getTaskName() + '\'' +
                ", inputFile=" + super.getInputFile() +
                ", outputFile=" + super.getOutputFile() +
                ", recordsPerSplit=" + recordsPerSplit +
                '}';
    }
} 