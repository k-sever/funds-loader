package org.ksever.fundsLoader;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.ksever.fundsLoader.service.JsonFundsBatchLoaderServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
@Slf4j
public class FundsLoaderApplication implements CommandLineRunner {

    @Autowired
    private JsonFundsBatchLoaderServiceImpl jsonFundsBatchLoaderService;

    public static void main(String[] args) {
        SpringApplication.run(FundsLoaderApplication.class, args);
    }

    @Override
    public void run(String... args) {
        if (args.length != 2) {
            log.info("Expecting exactly two arguments: <input file> <output file>");
            return;
        }

        var inputFile = new File(args[0]);
        var outputFile = new File(args[1]);

        if (!inputFile.exists()) {
            log.error("Input file does not exist: {}", inputFile.getAbsolutePath());
            return;
        }

        String input;

        try {
            input = FileUtils.readFileToString(inputFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error reading input file", e);
            return;
        }
        log.info("Loading funds");
        var output = jsonFundsBatchLoaderService.loadFunds(input);
        try {
            FileUtils.writeStringToFile(outputFile, output, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.info("Error writing output file: {}", e.getMessage());
            return;
        }

        log.info("Done!");
    }
}
