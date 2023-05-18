package org.ksever.fundsLoader.service;

import lombok.extern.slf4j.Slf4j;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JsonFundsBatchLoaderServiceImpl implements JsonFundsBatchLoaderService {

    private final FundsBatchLoaderService fundsBatchLoaderService;

    @Autowired
    public JsonFundsBatchLoaderServiceImpl(FundsBatchLoaderService fundsBatchLoaderService) {
        this.fundsBatchLoaderService = fundsBatchLoaderService;
    }

    @Override
    public String loadFunds(String requestsJsonLines) {
        var requestJsons = requestsJsonLines.split("\n");
        log.info("Processing {} requests", requestJsons.length);

        var requests = new ArrayList<LoadFundsRequest>(requestJsons.length);

        for (var requestJson : requestJsons) {
            log.debug("Processing request: {}", requestJson);

            try {
                var request = LoadFundsRequest.fromJson(requestJson);
                requests.add(request);
            } catch (Exception e) {
                log.error("Error processing request: {}", requestJson, e);
            }
        }

        log.debug("Parsed {} requests", requests.size());

        var responses = fundsBatchLoaderService.loadFunds(requests);

        var responseJsons = new ArrayList<String>(requestJsons.length);
        for (var response : responses) {
            try {
                var responseJson = LoadFundsResponse.toJson(response);
                responseJsons.add(responseJson);
            } catch (Exception e) {
                log.error("Error serializing response: {}", response, e);
            }
        }

        log.info("Successfully loaded {} requests", responseJsons.size());

        return String.join("\r\n", responseJsons) + "\r\n";
    }
}
