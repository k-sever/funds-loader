package org.ksever.fundsLoader.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FundsBatchLoaderServiceImplTest {

    @Resource
    private FundsLoaderService fundsLoaderService;
    private FundsBatchLoaderService fundsBatchLoaderService;

    @BeforeEach
    void setUp() {
        fundsBatchLoaderService = new FundsBatchLoaderServiceImpl(fundsLoaderService);
    }

    @Test
    void testLoadFunds() {
        var results = fundsBatchLoaderService.loadFunds(List.of(
                new LoadFundsRequest(123, 1, 564.56, LocalDateTime.parse("2023-05-18T10:10:00")),
                new LoadFundsRequest(124, 2, 235.78, LocalDateTime.parse("2023-05-18T10:12:31")),
                new LoadFundsRequest(124, 2, 235.78, LocalDateTime.parse("2023-05-18T10:12:31")),
                new LoadFundsRequest(127, 3, 290.12, LocalDateTime.parse("2023-05-18T10:12:31")),
                new LoadFundsRequest(134, 1, 4013.67, LocalDateTime.parse("2023-05-18T10:15:20")),
                new LoadFundsRequest(156, 1, 700.35, LocalDateTime.parse("2023-05-18T10:15:21")),
                new LoadFundsRequest(178, 3, 1893.13, LocalDateTime.parse("2023-05-18T10:15:30")),
                new LoadFundsRequest(198, 2, 50000.00, LocalDateTime.parse("2023-05-18T10:15:30")),
                new LoadFundsRequest(213, 2, 4820.02, LocalDateTime.parse("2023-05-18T10:15:44")),
                new LoadFundsRequest(222, 3, 4500.01, LocalDateTime.parse("2023-05-18T10:16:32")),
                new LoadFundsRequest(222, 3, 4500.01, LocalDateTime.parse("2023-05-18T10:16:32")),
                new LoadFundsRequest(223, 2, 100.00, LocalDateTime.parse("2023-05-18T10:17:10")),
                new LoadFundsRequest(224, 2, 100.00, LocalDateTime.parse("2023-05-18T10:17:11")),
                new LoadFundsRequest(226, 2, 100.00, LocalDateTime.parse("2023-05-18T10:17:11"))
        ));

        assertEquals(List.of(
                new LoadFundsResponse(123, 1, true),
                new LoadFundsResponse(124, 2, true),
                new LoadFundsResponse(127, 3, true),
                new LoadFundsResponse(134, 1, true),
                new LoadFundsResponse(156, 1, false),
                new LoadFundsResponse(178, 3, true),
                new LoadFundsResponse(198, 2, false),
                new LoadFundsResponse(213, 2, false),
                new LoadFundsResponse(222, 3, false),
                new LoadFundsResponse(223, 2, true),
                new LoadFundsResponse(224, 2, true),
                new LoadFundsResponse(226, 2, false)
                ),
                results);

    }
}