package org.ksever.fundsLoader.service;

import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;

import java.util.List;

public interface FundsBatchLoaderService {

    List<LoadFundsResponse> loadFunds(List<LoadFundsRequest> requests) throws IllegalArgumentException;
}
