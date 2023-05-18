package org.ksever.fundsLoader.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.Period;
import org.ksever.fundsLoader.model.Unit;
import org.ksever.fundsLoader.model.VelocityLimit;
import org.ksever.fundsLoader.service.util.VelocityLimitsQueryBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VelocityLimitsQueryBuilderTest {

    static Stream<Arguments> successCases() {
        return Stream.of(

                Arguments.of(
                        List.of(
                                new VelocityLimit(1000, Unit.SUM, Period.DAY)
                        ),
                        new LoadFundsRequest(2, 34, 100.52, LocalDateTime.parse("2023-05-18T14:25:21")),
                        """
                                SELECT COALESCE(SUM("amount"), 0) + 100.52 <= 1000 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=34 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('DAY',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                """
                ),
                Arguments.of(
                        List.of(
                                new VelocityLimit(5000, Unit.SUM, Period.DAY),
                                new VelocityLimit(20000, Unit.SUM, Period.WEEK),
                                new VelocityLimit(3, Unit.COUNT, Period.DAY)
                        ),
                        new LoadFundsRequest(15, 123, 1000, LocalDateTime.parse("2023-05-18T14:25:21")),
                        """
                                SELECT COALESCE(SUM("amount"), 0) + 1000.0 <= 5000 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=123 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('DAY',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                UNION ALL
                                SELECT COALESCE(SUM("amount"), 0) + 1000.0 <= 20000 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=123 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('WEEK',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                UNION ALL
                                SELECT COALESCE(COUNT(*), 0) < 3 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=123 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('DAY',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                """
                ),
                Arguments.of(
                        List.of(
                                new VelocityLimit(5000, Unit.SUM, Period.DAY),
                                new VelocityLimit(20000, Unit.SUM, Period.WEEK),
                                new VelocityLimit(3, Unit.COUNT, Period.DAY),
                                new VelocityLimit(10, Unit.COUNT, Period.WEEK)
                        ),
                        new LoadFundsRequest(15, 5344, 764.23, LocalDateTime.parse("2023-05-18T14:25:21")),
                        """
                                SELECT COALESCE(SUM("amount"), 0) + 764.23 <= 5000 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=5344 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('DAY',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                UNION ALL
                                SELECT COALESCE(SUM("amount"), 0) + 764.23 <= 20000 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=5344 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('WEEK',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                UNION ALL
                                SELECT COALESCE(COUNT(*), 0) < 3 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=5344 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('DAY',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                UNION ALL
                                SELECT COALESCE(COUNT(*), 0) < 10 as over_limit
                                FROM "funds"
                                WHERE "customer_id"=5344 AND "accepted"=true
                                AND "time" >= DATE_TRUNC('WEEK',TIMESTAMP '2023-05-18T14:25:21') AND "time" <= TIMESTAMP '2023-05-18T14:25:21'
                                """
                )
        );
    }

    @ParameterizedTest
    @MethodSource("successCases")
    void testBuildSQLQuery(List<VelocityLimit> limits, LoadFundsRequest request, String expectedQuery) {
        var velocityLimitsQueryBuilder = new VelocityLimitsQueryBuilder();
        var query = velocityLimitsQueryBuilder.buildSQLQuery(limits, request);
        assertEquals(expectedQuery, query);
    }

    static Stream<Arguments> failCases() {
        return Stream.of(

                Arguments.of(
                        List.of(
                        ),
                        new LoadFundsRequest(2, 34, 100.52, LocalDateTime.now()),
                        IllegalArgumentException.class,
                        "limits must not be null or empty"
                ),
                Arguments.of(
                        null,
                        new LoadFundsRequest(2, 34, 100.52, LocalDateTime.now()),
                        IllegalArgumentException.class,
                        "limits must not be null or empty"
                ),
                Arguments.of(
                        List.of(
                                new VelocityLimit(5000, Unit.SUM, Period.DAY),
                                new VelocityLimit(20000, Unit.SUM, Period.WEEK),
                                new VelocityLimit(3, Unit.COUNT, Period.DAY),
                                new VelocityLimit(10, Unit.COUNT, Period.WEEK),
                                new VelocityLimit(20000, Unit.SUM, Period.WEEK)
                        ),
                        new LoadFundsRequest(2, 34, 100.52, LocalDateTime.now()),
                        IllegalArgumentException.class,
                        "limits size must not less or equal than 4"
                )

        );
    }

    @ParameterizedTest
    @MethodSource("failCases")
    void testBuildSQLQuery(List<VelocityLimit> limits, LoadFundsRequest request, Class<Exception> expectedException, String expectedMessage) {
        var velocityLimitsQueryBuilder = new VelocityLimitsQueryBuilder();
        Exception ex = assertThrows(
                expectedException,
                () -> velocityLimitsQueryBuilder.buildSQLQuery(limits, request)
        );
        assertEquals(expectedMessage, ex.getMessage());
    }
}