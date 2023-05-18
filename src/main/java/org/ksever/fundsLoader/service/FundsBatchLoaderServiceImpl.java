package org.ksever.fundsLoader.service;

import lombok.extern.slf4j.Slf4j;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FundsBatchLoaderServiceImpl implements FundsBatchLoaderService {

    private final FundsLoaderService fundsLoaderService;

    @Autowired
    public FundsBatchLoaderServiceImpl(FundsLoaderService fundsLoaderService) {
        this.fundsLoaderService = fundsLoaderService;
    }

    @Override
    public List<LoadFundsResponse> loadFunds(List<LoadFundsRequest> requests) {
        if (requests == null) {
            throw new IllegalArgumentException("Requests can't be null");
        }
        var responses = new ArrayList<LoadFundsResponse>();
        for (var request : requests) {
            try {
                fundsLoaderService.loadFunds(request).ifPresent(responses::add);
            } catch (Exception e) {
                log.error("Failed to load funds for request: {}", request, e);
            }

        }
        return responses;
    }
}
