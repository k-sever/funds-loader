package org.ksever.fundsLoader.repository;

import org.ksever.fundsLoader.model.LoadFundsRequest;

import java.util.Optional;

public interface FundsRepository {

    void save(LoadFundsRequest request, boolean accepted);

    Optional<LoadFundsRequest> findByIdAndCustomerId(int id, int customerId);
}
