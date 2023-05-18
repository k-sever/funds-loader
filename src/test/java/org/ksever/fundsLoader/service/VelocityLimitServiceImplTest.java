package org.ksever.fundsLoader.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.Period;
import org.ksever.fundsLoader.model.Unit;
import org.ksever.fundsLoader.model.VelocityLimit;
import org.ksever.fundsLoader.repository.FundsRepository;
import org.ksever.fundsLoader.repository.VelocityLimitsRepository;
import org.ksever.fundsLoader.service.util.VelocityLimitsQueryBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Slf4j
class VelocityLimitServiceImplTest {

    private final VelocityLimitsRepository velocityLimitsRepository = customerId -> List.of(
            new VelocityLimit(500, Unit.SUM, Period.DAY),
            new VelocityLimit(2000, Unit.SUM, Period.WEEK),
            new VelocityLimit(2, Unit.COUNT, Period.DAY),
            new VelocityLimit(7, Unit.COUNT, Period.WEEK)
    );
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private FundsRepository fundsRepository;
    @Resource
    private VelocityLimitsQueryBuilder velocityLimitsQueryBuilder;

    private VelocityLimitService velocityLimitService;

    @BeforeEach
    void setUp() {
        velocityLimitService = new VelocityLimitServiceImpl(velocityLimitsRepository, velocityLimitsQueryBuilder, jdbcTemplate);
    }

    static Stream<Arguments> successCases() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        new LoadFundsRequest(1, 2, 100, LocalDateTime.now())
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 100, LocalDateTime.now())

                        ),
                        new LoadFundsRequest(3, 2, 400, LocalDateTime.now())
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 300, LocalDateTime.parse("2023-05-15T12:00:00")),
                                new LoadFundsRequest(2, 2, 300, LocalDateTime.parse("2023-05-16T01:00:00")),
                                new LoadFundsRequest(3, 2, 100, LocalDateTime.parse("2023-05-17T14:25:00"))

                        ),
                        new LoadFundsRequest(4, 2, 100, LocalDateTime.parse("2023-05-17T14:25:00"))
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 300, LocalDateTime.parse("2023-05-14T12:00:00")),
                                new LoadFundsRequest(2, 5, 350, LocalDateTime.parse("2023-05-15T10:00:00")),
                                new LoadFundsRequest(3, 2, 150, LocalDateTime.parse("2023-05-15T01:00:00")),
                                new LoadFundsRequest(4, 2, 50, LocalDateTime.parse("2023-05-16T12:00:00")),
                                new LoadFundsRequest(5, 5, 100, LocalDateTime.parse("2023-05-16T12:00:00"))

                        ),
                        new LoadFundsRequest(6, 5, 450, LocalDateTime.parse("2023-05-17T00:00:00"))
                )
        );
    }

    @ParameterizedTest
    @MethodSource("successCases")
    void testCanLoadSucceeds(List<LoadFundsRequest> existingFunds, LoadFundsRequest newFunds) {
        existingFunds.forEach(funds -> fundsRepository.save(funds, true));


        var result = velocityLimitService.canLoadFunds(newFunds);
        log.info("Result: {}", result);

        assertTrue(result.canLoad());
    }

    static Stream<Arguments> failureCases() {
        return Stream.of(
                Arguments.of(
                        List.of(),
                        new LoadFundsRequest(1, 2, 501, LocalDateTime.now()),
                        "Exceeded the maximum limit of SUM per DAY (limit=500)"
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 200, LocalDateTime.now()),
                                new LoadFundsRequest(2, 2, 300, LocalDateTime.now())

                        ),
                        new LoadFundsRequest(3, 2, 1, LocalDateTime.now()),
                        "Exceeded the maximum limit of SUM per DAY (limit=500)"
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 100, LocalDateTime.now()),
                                new LoadFundsRequest(2, 2, 100, LocalDateTime.now())
                        ),
                        new LoadFundsRequest(4, 2, 100, LocalDateTime.now()),
                        "Exceeded the maximum limit of COUNT per DAY (limit=2)"
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 500, LocalDateTime.parse("2023-05-14T12:00:00")),
                                new LoadFundsRequest(2, 2, 500, LocalDateTime.parse("2023-05-15T12:00:00")),
                                new LoadFundsRequest(3, 2, 500, LocalDateTime.parse("2023-05-16T12:00:00")),
                                new LoadFundsRequest(4, 2, 500, LocalDateTime.parse("2023-05-17T12:00:00")),
                                new LoadFundsRequest(5, 2, 300, LocalDateTime.parse("2023-05-17T12:00:00"))

                        ),
                        new LoadFundsRequest(6, 2, 400, LocalDateTime.parse("2023-05-18T12:00:00")),
                        "Exceeded the maximum limit of SUM per WEEK (limit=2000)"
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 100, LocalDateTime.parse("2023-05-14T12:00:00")),
                                new LoadFundsRequest(2, 2, 200, LocalDateTime.parse("2023-05-15T11:00:00")),
                                new LoadFundsRequest(3, 2, 200, LocalDateTime.parse("2023-05-15T12:00:00")),
                                new LoadFundsRequest(4, 2, 200, LocalDateTime.parse("2023-05-16T12:00:00")),
                                new LoadFundsRequest(5, 2, 100, LocalDateTime.parse("2023-05-16T12:00:00")),
                                new LoadFundsRequest(6, 2, 100,LocalDateTime.parse("2023-05-17T12:00:00")),
                                new LoadFundsRequest(8, 2, 100, LocalDateTime.parse("2023-05-18T12:00:00")),
                                new LoadFundsRequest(9, 2, 100, LocalDateTime.parse("2023-05-19T12:00:00"))

                        ),
                        new LoadFundsRequest(10, 2, 100, LocalDateTime.parse("2023-05-19T13:00:00")),
                        "Exceeded the maximum limit of COUNT per WEEK (limit=7)"
                ),
                Arguments.of(
                        List.of(
                                new LoadFundsRequest(1, 2, 300, LocalDateTime.parse("2023-05-14T12:00:00")),
                                new LoadFundsRequest(2, 5, 350, LocalDateTime.parse("2023-05-15T12:00:00")),
                                new LoadFundsRequest(3, 2, 150, LocalDateTime.parse("2023-05-15T12:00:00")),
                                new LoadFundsRequest(4, 5, 500, LocalDateTime.parse("2023-05-16T12:00:00")),
                                new LoadFundsRequest(6, 2, 50, LocalDateTime.parse("2023-05-17T12:00:00")),
                                new LoadFundsRequest(10, 5, 500, LocalDateTime.parse("2023-05-17T12:00:00")),
                                new LoadFundsRequest(12, 5, 500, LocalDateTime.parse("2023-05-18T12:00:00")),
                                new LoadFundsRequest(15, 5, 100, LocalDateTime.parse("2023-05-19T12:00:00")),
                                new LoadFundsRequest(16, 5, 40, LocalDateTime.parse("2023-05-19T13:00:00"))
                        ),
                        new LoadFundsRequest(18, 5, 500, LocalDateTime.parse("2023-05-19T15:00:00")),
                        "Exceeded the maximum limit of SUM per DAY (limit=500)"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("failureCases")
    void testCanLoadFails(List<LoadFundsRequest> existingFunds, LoadFundsRequest newFunds, String expectedErrorMessage) {
        existingFunds.forEach(funds -> fundsRepository.save(funds, true));

        var result = velocityLimitService.canLoadFunds(newFunds);
        log.info("Result: {}", result);

        assertFalse(result.canLoad());
        assertNotNull(result.errorMessage());
        assertTrue(result.errorMessage().contains(expectedErrorMessage));

    }

    @Test
    void testCanLoadIgnoresInactiveFunds() {
        fundsRepository.save(new LoadFundsRequest(1, 2, 300, LocalDateTime.parse("2023-05-14T12:00:00")), false);
        fundsRepository.save(new LoadFundsRequest(2, 2, 1000, LocalDateTime.parse("2023-05-14T13:00:00")), false);

        var result = velocityLimitService.canLoadFunds(
                new LoadFundsRequest(3, 2, 300, LocalDateTime.parse("2023-05-14T14:00:00"))
        );
        log.info("Result: {}", result);

        assertTrue(result.canLoad());
    }
}