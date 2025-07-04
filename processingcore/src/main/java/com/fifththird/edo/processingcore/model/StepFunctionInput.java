package com.fifththird.edo.processingcore.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Model class representing input data for Step Function tasks.
 * Contains the task token and task name required for Step Function execution.
 */
@Getter
@Setter
public class StepFunctionInput {
    
    /**
     * The task token used to identify and track the Step Function task.
     */
    private String taskToken;
    
    /**
     * The name of the task being executed in the Step Function.
     */
    private String taskName;
    
    /**
     * Default constructor.
     */
    public StepFunctionInput() {
    }
    
    /**
     * Constructor with task token and task name.
     * 
     * @param taskToken the task token for the Step Function task
     * @param taskName the name of the task
     */
    public StepFunctionInput(String taskToken, String taskName) {
        this.taskToken = taskToken;
        this.taskName = taskName;
    }

    public String getTaskToken() {
        return taskToken;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskToken(String taskToken) {
        this.taskToken = taskToken;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StepFunctionInput that = (StepFunctionInput) o;
        if (taskToken != null ? !taskToken.equals(that.taskToken) : that.taskToken != null) return false;
        return taskName != null ? taskName.equals(that.taskName) : that.taskName == null;
    }

    @Override
    public int hashCode() {
        int result = taskToken != null ? taskToken.hashCode() : 0;
        result = 31 * result + (taskName != null ? taskName.hashCode() : 0);
        return result;
    }
} 