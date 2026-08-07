package com.veen.velocitylimits.repository;

import com.veen.velocitylimits.entity.LoadFundEntity;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoadRecordRepository extends JpaRepository<LoadFundEntity, Long> {
    
    boolean existsByLoadIdAndCustomerId(
        String loadId,
        String CustomerId
    );

    List<LoadFundEntity> findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
        String customerId,
        Instant start,
        Instant end
    );
}
