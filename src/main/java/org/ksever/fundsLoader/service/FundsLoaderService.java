package org.ksever.fundsLoader.service;

import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;

import java.util.Optional;

public interface FundsLoaderService {

    Optional<LoadFundsResponse> loadFunds(LoadFundsRequest request) throws IllegalArgumentException;
}
