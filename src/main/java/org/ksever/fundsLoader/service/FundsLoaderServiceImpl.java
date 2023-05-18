package org.ksever.fundsLoader.service;

import lombok.extern.slf4j.Slf4j;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;
import org.ksever.fundsLoader.repository.FundsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
public class FundsLoaderServiceImpl implements FundsLoaderService {

    private final VelocityLimitService velocityLimitService;
    private final FundsRepository fundsRepository;

    @Autowired
    public FundsLoaderServiceImpl(VelocityLimitService velocityLimitService, FundsRepository fundsRepository) {
        this.velocityLimitService = velocityLimitService;
        this.fundsRepository = fundsRepository;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Optional<LoadFundsResponse> loadFunds(LoadFundsRequest request) throws IllegalArgumentException {
        if (request == null) {
            throw new IllegalArgumentException("Request can't be null");
        }
        log.debug("Loading funds for request: {}", request);
        // TODO: consider using exists instead
        var existingRequest = fundsRepository.findByIdAndCustomerId(request.id(), request.customerId());
        if (existingRequest.isPresent()) {
            log.info("Duplicate request, skipping: {}", request);
            return Optional.empty();
        }
        var result = velocityLimitService.canLoadFunds(request);
        if (!result.canLoad()) {
            log.info("Can't load request: {}, velocity limit reached: {} ", request, result.errorMessage());
            fundsRepository.save(request, false);
            return Optional.of(new LoadFundsResponse(request.id(), request.customerId(), false));
        }
        fundsRepository.save(request, true);
        return Optional.of(new LoadFundsResponse(request.id(), request.customerId(), true));
    }
}
