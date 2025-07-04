package com.fifththird.edo.filesplitters.model;

import com.fifththird.edo.processingcore.model.S3File;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SplitJsonInputTest {

    @Test
    void testDefaultConstructorAndSetters() {
        SplitJsonInput input = new SplitJsonInput();
        assertNull(input.getInputFile());
        assertNull(input.getOutputFile());
        assertNull(input.getTaskToken());
        assertNull(input.getTaskName());
        assertNull(input.getRecordsPerSplit());

        S3File inputFile = S3File.builder().bucket("in-bucket").fileKey("in.json").build();
        S3File outputFile = S3File.builder().bucket("out-bucket").fileKey("out.json").build();
        input.setInputFile(inputFile);
        input.setOutputFile(outputFile);
        input.setTaskToken("token");
        input.setTaskName("name");
        input.setRecordsPerSplit(100);

        assertEquals(inputFile, input.getInputFile());
        assertEquals(outputFile, input.getOutputFile());
        assertEquals("token", input.getTaskToken());
        assertEquals("name", input.getTaskName());
        assertEquals(100, input.getRecordsPerSplit());
    }

    @Test
    void testEqualsAndHashCode() {
        SplitJsonInput input1 = new SplitJsonInput();
        SplitJsonInput input2 = new SplitJsonInput();
        input1.setRecordsPerSplit(10);
        input2.setRecordsPerSplit(10);
        input1.setTaskToken("token");
        input2.setTaskToken("token");
        input1.setTaskName("name");
        input2.setTaskName("name");
        assertEquals(input1, input2);
        assertEquals(input1.hashCode(), input2.hashCode());
        input2.setRecordsPerSplit(20);
        assertNotEquals(input1, input2);
    }

    @Test
    void testToString() {
        SplitJsonInput input = new SplitJsonInput();
        input.setRecordsPerSplit(5);
        String str = input.toString();
        assertNotNull(str);
        assertTrue(str.contains("recordsPerSplit=5"));
    }
} 