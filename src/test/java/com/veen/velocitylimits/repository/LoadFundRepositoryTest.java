package com.veen.velocitylimits.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.veen.velocitylimits.entity.LoadFundEntity;

@DataJpaTest
class LoadFundRepositoryTest {

    @Autowired
    private LoadFundRepository loadFundRepository;

    @Test
    void shouldFindExistingLoadByExactLoadIdAndCustomerId() {
        loadFundRepository.saveAllAndFlush(List.of(
            acceptedLoad("load-1", "customer-1", "100.00", "2018-01-01T00:00:00Z"),
            acceptedLoad("load-1", "customer-2", "100.00", "2018-01-01T00:00:00Z"),
            acceptedLoad("load-2", "customer-1", "100.00", "2018-01-01T00:00:00Z")
        ));

        assertTrue(loadFundRepository.existsByLoadIdAndCustomerId("load-1", "customer-1"));
        assertFalse(loadFundRepository.existsByLoadIdAndCustomerId("load-3", "customer-1"));
        assertFalse(loadFundRepository.existsByLoadIdAndCustomerId("load-2", "customer-2"));
    }

    @Test
    void shouldFindAcceptedLoadsForCustomerInInclusiveStartExclusiveEndWindow() {
        Instant start = Instant.parse("2018-01-01T00:00:00Z");
        Instant end = Instant.parse("2018-01-02T00:00:00Z");

        loadFundRepository.saveAllAndFlush(List.of(
            acceptedLoad("start", "customer-1", "100.00", "2018-01-01T00:00:00Z"),
            acceptedLoad("inside", "customer-1", "100.00", "2018-01-01T12:00:00Z"),
            acceptedLoad("end", "customer-1", "100.00", "2018-01-02T00:00:00Z"),
            declinedLoad("declined", "customer-1", "100.00", "2018-01-01T12:00:00Z"),
            acceptedLoad("other-customer", "customer-2", "100.00", "2018-01-01T12:00:00Z")
        ));

        List<LoadFundEntity> loads = loadFundRepository
            .findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
                "customer-1",
                start,
                end
            );

        Set<String> loadIds = loads.stream()
            .map(LoadFundEntity::getLoadId)
            .collect(Collectors.toSet());

        assertEquals(Set.of("start", "inside"), loadIds);
    }

    private LoadFundEntity acceptedLoad(
        String loadId,
        String customerId,
        String amount,
        String loadTime
    ) {
        return new LoadFundEntity(
            loadId,
            customerId,
            new BigDecimal(amount),
            Instant.parse(loadTime),
            true
        );
    }

    private LoadFundEntity declinedLoad(
        String loadId,
        String customerId,
        String amount,
        String loadTime
    ) {
        return new LoadFundEntity(
            loadId,
            customerId,
            new BigDecimal(amount),
            Instant.parse(loadTime),
            false
        );
    }
}
