package com.fifththird.edo.processingcore.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for S3FileSplitInput model.
 */
public class S3FileSplitInputTest {

    @Test
    public void testDefaultConstructor() {
        // Act
        S3FileSplitInput input = new S3FileSplitInput();
        
        // Assert
        assertNotNull(input);
        assertEquals(0, input.getRecordsPerSplit());
        assertNull(input.getTaskToken());
        assertNull(input.getTaskName());
        assertNull(input.getInputFile());
        assertNull(input.getOutputFile());
    }

    @Test
    public void testConstructorWithS3FilesAndRecordsPerSplit() {
        // Arrange
        S3File inputFile = new S3File("test-bucket", "input.json", false, false);
        S3File outputFile = new S3File("test-bucket", "output.json", false, false);
        long recordsPerSplit = 1000L;
        
        // Act
        S3FileSplitInput input = new S3FileSplitInput(inputFile, outputFile, recordsPerSplit);
        
        // Assert
        assertEquals(inputFile, input.getInputFile());
        assertEquals(outputFile, input.getOutputFile());
        assertEquals(recordsPerSplit, input.getRecordsPerSplit());
        assertNull(input.getTaskToken());
        assertNull(input.getTaskName());
    }

    @Test
    public void testConstructorWithAllFields() {
        // Arrange
        String taskToken = "test-token";
        String taskName = "test-task";
        S3File inputFile = new S3File("test-bucket", "input.json", false, false);
        S3File outputFile = new S3File("test-bucket", "output.json", false, false);
        long recordsPerSplit = 500L;
        
        // Act
        S3FileSplitInput input = new S3FileSplitInput(taskToken, taskName, inputFile, outputFile, recordsPerSplit);
        
        // Assert
        assertEquals(taskToken, input.getTaskToken());
        assertEquals(taskName, input.getTaskName());
        assertEquals(inputFile, input.getInputFile());
        assertEquals(outputFile, input.getOutputFile());
        assertEquals(recordsPerSplit, input.getRecordsPerSplit());
    }

    @Test
    public void testGetRecordsPerSplit() {
        // Arrange
        S3FileSplitInput input = new S3FileSplitInput();
        long expectedRecordsPerSplit = 2000L;
        
        // Act
        input.setRecordsPerSplit(expectedRecordsPerSplit);
        
        // Assert
        assertEquals(expectedRecordsPerSplit, input.getRecordsPerSplit());
    }

    @Test
    public void testSetRecordsPerSplit() {
        // Arrange
        S3FileSplitInput input = new S3FileSplitInput();
        long recordsPerSplit = 1500L;
        
        // Act
        input.setRecordsPerSplit(recordsPerSplit);
        
        // Assert
        assertEquals(recordsPerSplit, input.getRecordsPerSplit());
    }

    @Test
    public void testEquals_SameObject() {
        // Arrange
        S3FileSplitInput input = new S3FileSplitInput("token", "task", 
            new S3File("bucket", "input", false, false), 
            new S3File("bucket", "output", false, false), 1000L);
        
        // Act & Assert
        assertEquals(input, input);
    }

    @Test
    public void testEquals_NullObject() {
        // Arrange
        S3FileSplitInput input = new S3FileSplitInput("token", "task", 
            new S3File("bucket", "input", false, false), 
            new S3File("bucket", "output", false, false), 1000L);
        
        // Act & Assert
        assertNotEquals(null, input);
    }

    @Test
    public void testEquals_DifferentClass() {
        // Arrange
        S3FileSplitInput input = new S3FileSplitInput("token", "task", 
            new S3File("bucket", "input", false, false), 
            new S3File("bucket", "output", false, false), 1000L);
        Object other = new Object();
        
        // Act & Assert
        assertNotEquals(input, other);
    }

    @Test
    public void testEquals_SameValues() {
        // Arrange
        S3File inputFile = new S3File("bucket", "input", false, false);
        S3File outputFile = new S3File("bucket", "output", false, false);
        S3FileSplitInput input1 = new S3FileSplitInput("token", "task", inputFile, outputFile, 1000L);
        S3FileSplitInput input2 = new S3FileSplitInput("token", "task", inputFile, outputFile, 1000L);
        
        // Act & Assert
        assertEquals(input1, input2);
        assertEquals(input2, input1);
    }

    @Test
    public void testEquals_DifferentRecordsPerSplit() {
        // Arrange
        S3File inputFile = new S3File("bucket", "input", false, false);
        S3File outputFile = new S3File("bucket", "output", false, false);
        S3FileSplitInput input1 = new S3FileSplitInput("token", "task", inputFile, outputFile, 1000L);
        S3FileSplitInput input2 = new S3FileSplitInput("token", "task", inputFile, outputFile, 2000L);
        
        // Act & Assert
        assertNotEquals(input1, input2);
        assertNotEquals(input2, input1);
    }

    @Test
    public void testEquals_DifferentTaskToken() {
        // Arrange
        S3File inputFile = new S3File("bucket", "input", false, false);
        S3File outputFile = new S3File("bucket", "output", false, false);
        S3FileSplitInput input1 = new S3FileSplitInput("token1", "task", inputFile, outputFile, 1000L);
        S3FileSplitInput input2 = new S3FileSplitInput("token2", "task", inputFile, outputFile, 1000L);
        
        // Act & Assert
        assertNotEquals(input1, input2);
        assertNotEquals(input2, input1);
    }

    @Test
    public void testEquals_DifferentInputFile() {
        // Arrange
        S3File inputFile1 = new S3File("bucket", "input1", false, false);
        S3File inputFile2 = new S3File("bucket", "input2", false, false);
        S3File outputFile = new S3File("bucket", "output", false, false);
        S3FileSplitInput input1 = new S3FileSplitInput("token", "task", inputFile1, outputFile, 1000L);
        S3FileSplitInput input2 = new S3FileSplitInput("token", "task", inputFile2, outputFile, 1000L);
        
        // Act & Assert
        assertNotEquals(input1, input2);
        assertNotEquals(input2, input1);
    }

    @Test
    public void testHashCode_SameValues() {
        // Arrange
        S3File inputFile = new S3File("bucket", "input", false, false);
        S3File outputFile = new S3File("bucket", "output", false, false);
        S3FileSplitInput input1 = new S3FileSplitInput("token", "task", inputFile, outputFile, 1000L);
        S3FileSplitInput input2 = new S3FileSplitInput("token", "task", inputFile, outputFile, 1000L);
        
        // Act & Assert
        assertEquals(input1.hashCode(), input2.hashCode());
    }

    @Test
    public void testHashCode_DifferentValues() {
        // Arrange
        S3File inputFile = new S3File("bucket", "input", false, false);
        S3File outputFile = new S3File("bucket", "output", false, false);
        S3FileSplitInput input1 = new S3FileSplitInput("token", "task", inputFile, outputFile, 1000L);
        S3FileSplitInput input2 = new S3FileSplitInput("token", "task", inputFile, outputFile, 2000L);
        
        // Act & Assert
        assertNotEquals(input1.hashCode(), input2.hashCode());
    }

    @Test
    public void testToString() {
        // Arrange
        S3File inputFile = new S3File("test-bucket", "input.json", true, false);
        S3File outputFile = new S3File("test-bucket", "output.json", false, true);
        S3FileSplitInput input = new S3FileSplitInput("test-token", "test-task", inputFile, outputFile, 1500L);
        
        // Act
        String result = input.toString();
        
        // Assert
        assertTrue(result.contains("S3FileSplitInput"));
        assertTrue(result.contains("taskToken='test-token'"));
        assertTrue(result.contains("taskName='test-task'"));
        assertTrue(result.contains("inputFile=" + inputFile.toString()));
        assertTrue(result.contains("outputFile=" + outputFile.toString()));
        assertTrue(result.contains("recordsPerSplit=1500"));
    }

    @Test
    public void testToString_WithNullValues() {
        // Arrange
        S3FileSplitInput input = new S3FileSplitInput();
        
        // Act
        String result = input.toString();
        
        // Assert
        assertTrue(result.contains("S3FileSplitInput"));
        assertTrue(result.contains("taskToken='null'"));
        assertTrue(result.contains("taskName='null'"));
        assertTrue(result.contains("inputFile=null"));
        assertTrue(result.contains("outputFile=null"));
        assertTrue(result.contains("recordsPerSplit=0"));
    }
} 