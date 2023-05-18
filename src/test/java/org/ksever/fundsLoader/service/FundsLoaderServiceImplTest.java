package org.ksever.fundsLoader.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ksever.fundsLoader.model.LoadFundsRequest;
import org.ksever.fundsLoader.model.LoadFundsResponse;
import org.ksever.fundsLoader.repository.FundsRepository;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FundsLoaderServiceImplTest {

    @Resource
    private VelocityLimitService velocityLimitService;
    @Resource
    private FundsRepository fundsRepository;

    private FundsLoaderServiceImpl fundsLoaderService;

    @BeforeEach
    void setUp() {
        fundsLoaderService = new FundsLoaderServiceImpl(velocityLimitService, fundsRepository);
    }

    @Test
    void testLoadFunds() {
        var request = new LoadFundsRequest(1, 2, 100, LocalDateTime.parse("2023-05-18T14:25:00"));
        var result = fundsLoaderService.loadFunds(request);
        assertTrue(result.isPresent());
        assertEquals(new LoadFundsResponse(request.id(), request.customerId(), true), result.get());
        fundsRepository.findByIdAndCustomerId(request.id(), request.customerId()).ifPresentOrElse(
                r -> assertEquals(request, r),
                () -> fail("Request not found"));
    }

    @Test
    void testLoadDuplicateFunds() {
        var request = new LoadFundsRequest(1, 2, 100, LocalDateTime.parse("2023-05-18T14:25:00"));
        fundsRepository.save(request, true);
        var result = fundsLoaderService.loadFunds(new LoadFundsRequest(1, 2, 250,LocalDateTime.parse("2023-05-18T14:26:00")));
        assertTrue(result.isEmpty());
        fundsRepository.findByIdAndCustomerId(request.id(), request.customerId()).ifPresentOrElse(
                r -> assertEquals(request, r),
                () -> fail("Request not found"));
    }

    @Test
    void testLoadFundsLimited() {
        var request1 = new LoadFundsRequest(1, 2, 100, LocalDateTime.parse("2023-05-18T14:25:00"));
        var request2 = new LoadFundsRequest(2, 2, 300, LocalDateTime.parse("2023-05-18T14:26:00"));
        var request3 = new LoadFundsRequest(3, 2, 300, LocalDateTime.parse("2023-05-18T14:27:00"));
        fundsRepository.save(request1, true);
        fundsRepository.save(request2, true);
        fundsRepository.save(request3, true);

        var request4 = new LoadFundsRequest(4, 2, 500, LocalDateTime.parse("2023-05-18T14:28:00"));
        var result = fundsLoaderService.loadFunds(request4);
        assertTrue(result.isPresent());
        assertEquals(new LoadFundsResponse(request4.id(), request4.customerId(), false), result.get());
        fundsRepository.findByIdAndCustomerId(request4.id(), request4.customerId()).ifPresentOrElse(
                r -> assertEquals(request4, r),
                () -> fail("Request not found"));
    }

    @Test
    void testLoadFundsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> fundsLoaderService.loadFunds(null));
    }
}