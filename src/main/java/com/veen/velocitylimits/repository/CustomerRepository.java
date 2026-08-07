package com.veen.velocitylimits.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.veen.velocitylimits.entity.CustomerEntity;

import jakarta.persistence.LockModeType;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByCustomerId(String customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from CustomerEntity c where c.customerId = :customer_id")
    Optional<CustomerEntity> findByCustomerIdForUpdate(
        @Param("customer_id") String CustomerId
    );

}
