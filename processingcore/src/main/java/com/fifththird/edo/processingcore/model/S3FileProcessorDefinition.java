package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing a processor definition for S3 file operations.
 * Defines input and output S3 files for processing operations.
 * Extends StepFunctionInput to include task token and task name for Step Function integration.
 * Note: Lombok annotations are included for documentation but manual methods are used
 * due to compilation issues with the annotation processor in this environment.
 */
@Getter
@Setter
public class S3FileProcessorDefinition extends StepFunctionInput {
    
    /**
     * The input S3 file to be processed.
     */
    private S3File inputFile;
    
    /**
     * The output S3 file where processed results will be stored.
     */
    private S3File outputFile;

    /**
     * Default constructor.
     */
    public S3FileProcessorDefinition() {
        super();
    }

    /**
     * Constructor with S3 files.
     */
    public S3FileProcessorDefinition(S3File inputFile, S3File outputFile) {
        super();
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    /**
     * Constructor with all fields including Step Function parameters.
     */
    public S3FileProcessorDefinition(String taskToken, String taskName, S3File inputFile, S3File outputFile) {
        super(taskToken, taskName);
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    // Manual getter and setter methods (Lombok annotations above for documentation)
    public S3File getInputFile() {
        return inputFile;
    }

    public void setInputFile(S3File inputFile) {
        this.inputFile = inputFile;
    }

    public S3File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(S3File outputFile) {
        this.outputFile = outputFile;
    }

    public void setTaskToken(String taskToken) {
        super.setTaskToken(taskToken);
    }

    public void setTaskName(String taskName) {
        super.setTaskName(taskName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        S3FileProcessorDefinition that = (S3FileProcessorDefinition) o;

        if (inputFile != null ? !inputFile.equals(that.inputFile) : that.inputFile != null) return false;
        return outputFile != null ? outputFile.equals(that.outputFile) : that.outputFile == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (inputFile != null ? inputFile.hashCode() : 0);
        result = 31 * result + (outputFile != null ? outputFile.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "S3FileProcessorDefinition{" +
                "taskToken='" + super.getTaskToken() + '\'' +
                ", taskName='" + super.getTaskName() + '\'' +
                ", inputFile=" + inputFile +
                ", outputFile=" + outputFile +
                '}';
    }
} 