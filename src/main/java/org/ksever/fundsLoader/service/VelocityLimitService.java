package org.ksever.fundsLoader.service;

import org.ksever.fundsLoader.model.LoadFundsRequest;

public interface VelocityLimitService {
    record CanLoadResult(boolean canLoad, String errorMessage) {
    }

    CanLoadResult canLoadFunds(LoadFundsRequest request);

}
