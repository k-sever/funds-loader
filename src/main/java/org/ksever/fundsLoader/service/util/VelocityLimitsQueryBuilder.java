package org.ksever.fundsLoader.service.util;

import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.VelocityLimit;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class VelocityLimitsQueryBuilder {

    private static final int MAX_LIMITS_SIZE = 4;

    private static final String VELOCITY_LIMIT_SUM_TEMPLATE = """
            SELECT COALESCE(SUM("amount"), 0) + :request_amount <= :limit as over_limit
            FROM "funds"
            WHERE "customer_id"=:customerId AND "accepted"=true
            AND "time" >= DATE_TRUNC(':interval',TIMESTAMP ':request_timestamp') AND "time" <= TIMESTAMP ':request_timestamp'
            """;
    private static final String VELOCITY_LIMIT_COUNT_TEMPLATE = """
            SELECT COALESCE(COUNT(*), 0) < :limit as over_limit
            FROM "funds"
            WHERE "customer_id"=:customerId AND "accepted"=true
            AND "time" >= DATE_TRUNC(':interval',TIMESTAMP ':request_timestamp') AND "time" <= TIMESTAMP ':request_timestamp'
            """;

    public String buildSQLQuery(List<VelocityLimit> limits, LoadFundsRequest request) throws IllegalArgumentException {
        if (limits == null || limits.isEmpty()) {
            throw new IllegalArgumentException("limits must not be null or empty");
        }
        if (limits.size() > MAX_LIMITS_SIZE) {
            throw new IllegalArgumentException("limits size must not less or equal than " + MAX_LIMITS_SIZE);
        }
        return limits.stream()
                .map(l -> toQuery(l, request))
                .collect(Collectors.joining("UNION ALL\n"));
    }

    private String toQuery(VelocityLimit limit, LoadFundsRequest request) {
        return switch (limit.unit()) {
            case SUM -> toSumQuery(limit, request);
            case COUNT -> toCountQuery(limit, request);
        };
    }

    private String toSumQuery(VelocityLimit limit, LoadFundsRequest request) {
        return VELOCITY_LIMIT_SUM_TEMPLATE
                .replace(":customerId", String.valueOf(request.customerId()))
                .replace(":request_amount", String.valueOf(request.amount()))
                .replace(":request_timestamp", request.timestamp().format(DateTimeFormatter.ISO_DATE_TIME))
                .replace(":limit", String.valueOf(limit.maxAmount()))
                .replace(":interval", limit.period().name());
    }

    private String toCountQuery(VelocityLimit limit, LoadFundsRequest request) {
        return VELOCITY_LIMIT_COUNT_TEMPLATE
                .replace(":customerId", String.valueOf(request.customerId()))
                .replace(":request_timestamp", request.timestamp().format(DateTimeFormatter.ISO_DATE_TIME))
                .replace(":limit", String.valueOf(limit.maxAmount()))
                .replace(":interval", limit.period().name());
    }
}
