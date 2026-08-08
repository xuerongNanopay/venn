package com.veen.velocitylimits.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.veen.velocitylimits.entity.CustomerEntity;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindCustomerByCustomerId() {
        customerRepository.saveAndFlush(new CustomerEntity("customer-1"));

        var customer = customerRepository.findByCustomerId("customer-1");

        assertTrue(customer.isPresent());
        assertEquals("customer-1", customer.get().getCustomerId());
    }

    @Test
    void shouldFindCustomerByCustomerIdForUpdate() {
        customerRepository.saveAndFlush(new CustomerEntity("customer-1"));

        var customer = customerRepository.findByCustomerIdForUpdate("customer-1");

        assertTrue(customer.isPresent());
        assertEquals("customer-1", customer.get().getCustomerId());
    }

    @Test
    void shouldRejectDuplicateCustomerId() {
        customerRepository.saveAndFlush(new CustomerEntity("customer-1"));

        assertThrows(
            DataIntegrityViolationException.class,
            () -> customerRepository.saveAndFlush(new CustomerEntity("customer-1"))
        );
    }
}
