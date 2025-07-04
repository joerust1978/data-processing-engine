package com.fifththird.edo.processingcore.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for StepFunctionInput.
 */
class StepFunctionInputTest {

    @Test
    void testDefaultConstructor() {
        StepFunctionInput input = new StepFunctionInput();
        assertNotNull(input);
        assertNull(input.getTaskToken());
        assertNull(input.getTaskName());
    }

    @Test
    void testParameterizedConstructor() {
        String taskToken = "test-token-123";
        String taskName = "test-task";
        
        StepFunctionInput input = new StepFunctionInput(taskToken, taskName);
        
        assertEquals(taskToken, input.getTaskToken());
        assertEquals(taskName, input.getTaskName());
    }

    @Test
    void testSettersAndGetters() {
        StepFunctionInput input = new StepFunctionInput();
        
        String taskToken = "new-token-456";
        String taskName = "new-task";
        
        input.setTaskToken(taskToken);
        input.setTaskName(taskName);
        
        assertEquals(taskToken, input.getTaskToken());
        assertEquals(taskName, input.getTaskName());
    }

    @Test
    void testEqualsAndHashCode() {
        StepFunctionInput input1 = new StepFunctionInput("token1", "task1");
        StepFunctionInput input2 = new StepFunctionInput("token1", "task1");
        StepFunctionInput input3 = new StepFunctionInput("token2", "task1");
        StepFunctionInput input4 = new StepFunctionInput("token1", "task2");
        
        // Test equality
        assertEquals(input1, input2);
        assertEquals(input1.hashCode(), input2.hashCode());
        
        // Test inequality
        assertNotEquals(input1, input3);
        assertNotEquals(input1, input4);
        assertNotEquals(input1.hashCode(), input3.hashCode());
        assertNotEquals(input1.hashCode(), input4.hashCode());
        
        // Test with null values
        StepFunctionInput input5 = new StepFunctionInput(null, null);
        StepFunctionInput input6 = new StepFunctionInput(null, null);
        assertEquals(input5, input6);
        assertEquals(input5.hashCode(), input6.hashCode());
        
        // Test with mixed null values
        StepFunctionInput input7 = new StepFunctionInput("token", null);
        StepFunctionInput input8 = new StepFunctionInput("token", "task");
        assertNotEquals(input7, input8);
    }

    @Test
    void testEqualsWithNullAndDifferentTypes() {
        StepFunctionInput input = new StepFunctionInput("token", "task");
        
        assertNotEquals(null, input);
        assertNotEquals("string", input);
        assertEquals(input, input); // Same object
    }

    @Test
    void testToString() {
        StepFunctionInput input = new StepFunctionInput("test-token", "test-task");
        String toString = input.toString();
        assertNotNull(toString);
    }

    @Test
    void testToStringWithNullValues() {
        StepFunctionInput input = new StepFunctionInput(null, null);
        String toString = input.toString();
        assertNotNull(toString);
    }
} 