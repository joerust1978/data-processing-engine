package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing the output of a processor operation.
 * Contains the output S3 file where processed results are stored.
 * Note: Lombok annotations are included for documentation but manual methods are used
 * due to compilation issues with the annotation processor in this environment.
 */
@Getter
@Setter
public class S3FileProcessorOutput {
    
    /**
     * The output S3 file where processed results are stored.
     */
    private S3File outputFile;

    /**
     * Default constructor.
     */
    public S3FileProcessorOutput() {
    }

    /**
     * Constructor with output S3 file.
     */
    public S3FileProcessorOutput(S3File outputFile) {
        this.outputFile = outputFile;
    }

    // Manual getter and setter methods (Lombok annotations above for documentation)
    public S3File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(S3File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        S3FileProcessorOutput that = (S3FileProcessorOutput) o;

        return outputFile != null ? outputFile.equals(that.outputFile) : that.outputFile == null;
    }

    @Override
    public int hashCode() {
        return outputFile != null ? outputFile.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "S3FileProcessorOutput{" +
                "outputFile=" + outputFile +
                '}';
    }
} 