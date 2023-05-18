package org.ksever.fundsLoader.repository;

import org.ksever.fundsLoader.model.Period;
import org.ksever.fundsLoader.model.Unit;
import org.ksever.fundsLoader.model.VelocityLimit;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StubVelocityLimitsRepository implements VelocityLimitsRepository {

    @Override
    public List<VelocityLimit> getConfig(int customerId) {
        return List.of(
                // TODO: define defaults in config
                new VelocityLimit(5_000, Unit.SUM, Period.DAY),
                new VelocityLimit(20_000, Unit.SUM, Period.WEEK),
                new VelocityLimit(3, Unit.COUNT, Period.DAY)
        );
    }
}
