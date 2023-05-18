package org.ksever.fundsLoader.service;

import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.repository.VelocityLimitsRepository;
import org.ksever.fundsLoader.service.util.VelocityLimitsQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VelocityLimitServiceImpl implements VelocityLimitService {

    private final VelocityLimitsRepository velocityLimitsRepository;
    private final VelocityLimitsQueryBuilder velocityLimitsQueryBuilder;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public VelocityLimitServiceImpl(VelocityLimitsRepository velocityLimitsRepository,
                                    VelocityLimitsQueryBuilder velocityLimitsQueryBuilder,
                                    JdbcTemplate jdbcTemplate) {
        this.velocityLimitsRepository = velocityLimitsRepository;
        this.velocityLimitsQueryBuilder = velocityLimitsQueryBuilder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CanLoadResult canLoadFunds(LoadFundsRequest request) {
        var limits = velocityLimitsRepository.getConfig(request.customerId());
        if (limits.isEmpty()) {
            return new CanLoadResult(true, null);
        }
        var sql = velocityLimitsQueryBuilder.buildSQLQuery(limits, request);
        // TODO: remove jdbcTemplate dependency
        List<Boolean> result = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getBoolean(1));
        for (int i = 0; i < result.size(); i++) {
            if (!result.get(i)) {
                return new CanLoadResult(false, limits.get(i).errorMessage());
            }
        }
        return new CanLoadResult(true, null);
    }
}
