package org.ksever.fundsLoader.service;

import jakarta.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JsonFundsBatchLoaderServiceTest {

    @Resource
    private JsonFundsBatchLoaderServiceImpl jsonFundsBatchLoaderService;

    @Test
    void testProcessBatch() throws IOException {
        String input1 = loadResource("/input1.txt");
        String expected1 = loadResource("/output1.txt");
        String input2 = loadResource("/input2.txt");
        String expected2 = loadResource("/output2.txt");

        String output1 = jsonFundsBatchLoaderService.loadFunds(input1);
        assertEquals(expected1, output1);

        String output2 = jsonFundsBatchLoaderService.loadFunds(input2);
        assertEquals(expected2, output2);
    }

    private String loadResource(String resourceName) throws IOException {
        return IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream(resourceName)),
                StandardCharsets.UTF_8
        );
    }

    @Test
    void testProcessBatchWithMalformedEntries() throws IOException {
        String input = loadResource("/input3_malformed.txt");
        String expected = loadResource("/output3.txt");

        String output = jsonFundsBatchLoaderService.loadFunds(input);
        assertEquals(expected, output);
    }
}