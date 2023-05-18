package org.ksever.fundsLoader.repository;

import org.ksever.fundsLoader.model.VelocityLimit;

import java.util.List;
import java.util.Set;

public interface VelocityLimitsRepository {

    List<VelocityLimit> getConfig(int customerId);
}
