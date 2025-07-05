package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing the output of a processor operation.
 * Contains the output S3 file where processed results are stored.
 * Extends StepFunctionInput to include task token and task name for Step Function integration.
 * Note: Lombok annotations are included for documentation but manual methods are used
 * due to compilation issues with the annotation processor in this environment.
 */
@Getter
@Setter
public class S3FileProcessorOutput extends StepFunctionInput {
    
    /**
     * The output S3 file where processed results are stored.
     */
    private S3File outputFile;

    /**
     * Default constructor.
     */
    public S3FileProcessorOutput() {
        super();
    }

    /**
     * Constructor with output S3 file.
     */
    public S3FileProcessorOutput(S3File outputFile) {
        super();
        this.outputFile = outputFile;
    }

    /**
     * Constructor with all fields including Step Function parameters.
     */
    public S3FileProcessorOutput(String taskToken, String taskName, S3File outputFile) {
        super(taskToken, taskName);
        this.outputFile = outputFile;
    }

    // Manual getter and setter methods (Lombok annotations above for documentation)
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

        S3FileProcessorOutput that = (S3FileProcessorOutput) o;

        return outputFile != null ? outputFile.equals(that.outputFile) : that.outputFile == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (outputFile != null ? outputFile.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "S3FileProcessorOutput{" +
                "taskToken='" + super.getTaskToken() + '\'' +
                ", taskName='" + super.getTaskName() + '\'' +
                ", outputFile=" + outputFile +
                '}';
    }
} 