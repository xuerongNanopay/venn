package com.veen.velocitylimits.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.domain.LoadFundResult;
import com.veen.velocitylimits.entity.CustomerEntity;
import com.veen.velocitylimits.entity.LoadFundEntity;
import com.veen.velocitylimits.repository.CustomerRepository;
import com.veen.velocitylimits.repository.LoadFundRepository;

@ExtendWith(MockitoExtension.class)
public class VelocityLimitsServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoadFundRepository loadFundRepository;

    private VelocityLimitsService service;

    @BeforeEach
    void setUp() {
        service = new VelocityLimitsService(
            loadFundRepository,
            customerRepository
        );
    }



    @Test
    void shouldIgnoreDuplicateLoadForSameCustomer() {
        LoadFund loadFund = new LoadFund(
            "load-1",
            "customer-1",
            new BigDecimal("1000.00"),
            Instant.parse("2018-01-03T10:15:30Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-1", "customer-1"))
            .thenReturn(true);

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isEmpty());
        verify(loadFundRepository, never()).save(any());
    }

    @Test
    void shouldAcceptFundWhenLessAndEqual5K() {
        LoadFund loadFund = new LoadFund(
            "load-2",
            "customer-1",
            new BigDecimal("3000.00"),
            Instant.parse("2018-01-03T10:15:30Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-2", "customer-1"))
            .thenReturn(false);
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            any(Instant.class),
            any(Instant.class)
        )).thenReturn(List.of(
            new LoadFundEntity(
                "load-1",
                "customer-1",
                new BigDecimal("2000.00"),
                Instant.parse("2018-01-03T09:15:30Z"),
                true
            )
        ));

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isPresent());
        assertEquals("load-2", result.get().loadId());
        assertEquals("customer-1", result.get().customerId());
        assertTrue(result.get().accepted());

        ArgumentCaptor<LoadFundEntity> captor = ArgumentCaptor.forClass(LoadFundEntity.class);
        verify(loadFundRepository).save(captor.capture());

        LoadFundEntity savedLoad = captor.getValue();
        assertEquals("load-2", savedLoad.getLoadId());
        assertEquals("customer-1", savedLoad.getCustomerId());
        assertEquals(new BigDecimal("3000.00"), savedLoad.getLoadAmount());
        assertEquals(Instant.parse("2018-01-03T10:15:30Z"), savedLoad.getLoadTime());
        assertTrue(savedLoad.isAccepted());
    }

    @Test
    void shouldRejectFundWhenGreater5K() {
        LoadFund loadFund = new LoadFund(
            "load-2",
            "customer-1",
            new BigDecimal("3000.01"),
            Instant.parse("2018-01-03T10:15:30Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-2", "customer-1"))
            .thenReturn(false);
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            any(Instant.class),
            any(Instant.class)
        )).thenReturn(List.of(
            new LoadFundEntity(
                "load-1",
                "customer-1",
                new BigDecimal("2000.00"),
                Instant.parse("2018-01-03T09:15:30Z"),
                true
            )
        ));

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isPresent());
        assertEquals("load-2", result.get().loadId());
        assertEquals("customer-1", result.get().customerId());
        assertFalse(result.get().accepted());

        ArgumentCaptor<LoadFundEntity> captor = ArgumentCaptor.forClass(LoadFundEntity.class);
        verify(loadFundRepository).save(captor.capture());

        LoadFundEntity savedLoad = captor.getValue();
        assertEquals("load-2", savedLoad.getLoadId());
        assertEquals("customer-1", savedLoad.getCustomerId());
        assertEquals(new BigDecimal("3000.01"), savedLoad.getLoadAmount());
        assertEquals(Instant.parse("2018-01-03T10:15:30Z"), savedLoad.getLoadTime());
        assertFalse(savedLoad.isAccepted());
    }

    @Test
    void shouldAcceptFundWhenLessAndEqual20KInAWeek() {
        LoadFund loadFund = new LoadFund(
            "load-5",
            "customer-1",
            new BigDecimal("3000.00"),
            Instant.parse("2018-01-07T23:59:59Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-5", "customer-1"))
            .thenReturn(false);
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            eq(Instant.parse("2018-01-07T00:00:00Z")),
            eq(Instant.parse("2018-01-08T00:00:00Z"))
        )).thenReturn(List.of());
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            eq(Instant.parse("2018-01-01T00:00:00Z")),
            eq(Instant.parse("2018-01-08T00:00:00Z"))
        )).thenReturn(List.of(
            new LoadFundEntity(
                "load-1",
                "customer-1",
                new BigDecimal("5000.00"),
                Instant.parse("2018-01-01T00:00:00Z"),
                true
            ),
            new LoadFundEntity(
                "load-2",
                "customer-1",
                new BigDecimal("5000.00"),
                Instant.parse("2018-01-02T09:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-3",
                "customer-1",
                new BigDecimal("4000.00"),
                Instant.parse("2018-01-02T10:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-4",
                "customer-1",
                new BigDecimal("3000.00"),
                Instant.parse("2018-01-02T11:15:30Z"),
                true
            )
        ));

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isPresent());
        assertEquals("load-5", result.get().loadId());
        assertEquals("customer-1", result.get().customerId());
        assertTrue(result.get().accepted());

        ArgumentCaptor<LoadFundEntity> captor = ArgumentCaptor.forClass(LoadFundEntity.class);
        verify(loadFundRepository).save(captor.capture());

        LoadFundEntity savedLoad = captor.getValue();
        assertEquals("load-5", savedLoad.getLoadId());
        assertEquals("customer-1", savedLoad.getCustomerId());
        assertEquals(new BigDecimal("3000.00"), savedLoad.getLoadAmount());
        assertEquals(Instant.parse("2018-01-07T23:59:59Z"), savedLoad.getLoadTime());
        assertTrue(savedLoad.isAccepted());
    }

    @Test
    void shouldRejectFundWhenGreater20KInAWeek() {
        LoadFund loadFund = new LoadFund(
            "load-5",
            "customer-1",
            new BigDecimal("3000.01"),
            Instant.parse("2018-01-07T23:59:59Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-5", "customer-1"))
            .thenReturn(false);
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            eq(Instant.parse("2018-01-07T00:00:00Z")),
            eq(Instant.parse("2018-01-08T00:00:00Z"))
        )).thenReturn(List.of());
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            eq(Instant.parse("2018-01-01T00:00:00Z")),
            eq(Instant.parse("2018-01-08T00:00:00Z"))
        )).thenReturn(List.of(
            new LoadFundEntity(
                "load-1",
                "customer-1",
                new BigDecimal("5000.00"),
                Instant.parse("2018-01-01T00:00:00Z"),
                true
            ),
            new LoadFundEntity(
                "load-2",
                "customer-1",
                new BigDecimal("5000.00"),
                Instant.parse("2018-01-02T09:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-3",
                "customer-1",
                new BigDecimal("4000.00"),
                Instant.parse("2018-01-02T10:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-4",
                "customer-1",
                new BigDecimal("3000.00"),
                Instant.parse("2018-01-02T11:15:30Z"),
                true
            )
        ));

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isPresent());
        assertEquals("load-5", result.get().loadId());
        assertEquals("customer-1", result.get().customerId());
        assertFalse(result.get().accepted());

        ArgumentCaptor<LoadFundEntity> captor = ArgumentCaptor.forClass(LoadFundEntity.class);
        verify(loadFundRepository).save(captor.capture());

        LoadFundEntity savedLoad = captor.getValue();
        assertEquals("load-5", savedLoad.getLoadId());
        assertEquals("customer-1", savedLoad.getCustomerId());
        assertEquals(new BigDecimal("3000.01"), savedLoad.getLoadAmount());
        assertEquals(Instant.parse("2018-01-07T23:59:59Z"), savedLoad.getLoadTime());
        assertFalse(savedLoad.isAccepted());
    }

    @Test
    void shouldAcceptFundWhenLessAndEqual3LoadsInADay() {
        LoadFund loadFund = new LoadFund(
            "load-3",
            "customer-1",
            new BigDecimal("1000.00"),
            Instant.parse("2018-01-03T10:15:30Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-3", "customer-1"))
            .thenReturn(false);
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            any(Instant.class),
            any(Instant.class)
        )).thenReturn(List.of(
            new LoadFundEntity(
                "load-1",
                "customer-1",
                new BigDecimal("1000.00"),
                Instant.parse("2018-01-03T08:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-2",
                "customer-1",
                new BigDecimal("1000.00"),
                Instant.parse("2018-01-03T09:15:30Z"),
                true
            )
        ));

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isPresent());
        assertEquals("load-3", result.get().loadId());
        assertEquals("customer-1", result.get().customerId());
        assertTrue(result.get().accepted());

        ArgumentCaptor<LoadFundEntity> captor = ArgumentCaptor.forClass(LoadFundEntity.class);
        verify(loadFundRepository).save(captor.capture());

        LoadFundEntity savedLoad = captor.getValue();
        assertEquals("load-3", savedLoad.getLoadId());
        assertEquals("customer-1", savedLoad.getCustomerId());
        assertEquals(new BigDecimal("1000.00"), savedLoad.getLoadAmount());
        assertEquals(Instant.parse("2018-01-03T10:15:30Z"), savedLoad.getLoadTime());
        assertTrue(savedLoad.isAccepted());
    }

    @Test
    void shouldRejectFundWhenGreater3LoadsInADay() {
        LoadFund loadFund = new LoadFund(
            "load-4",
            "customer-1",
            new BigDecimal("1000.00"),
            Instant.parse("2018-01-03T10:15:30Z")
        );

        when(customerRepository.findByCustomerIdForUpdate("customer-1"))
            .thenReturn(Optional.of(new CustomerEntity("customer-1")));
        when(loadFundRepository.existsByLoadIdAndCustomerId("load-4", "customer-1"))
            .thenReturn(false);
        when(loadFundRepository.findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
            eq("customer-1"),
            any(Instant.class),
            any(Instant.class)
        )).thenReturn(List.of(
            new LoadFundEntity(
                "load-1",
                "customer-1",
                new BigDecimal("100.00"),
                Instant.parse("2018-01-03T07:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-2",
                "customer-1",
                new BigDecimal("100.00"),
                Instant.parse("2018-01-03T08:15:30Z"),
                true
            ),
            new LoadFundEntity(
                "load-3",
                "customer-1",
                new BigDecimal("100.00"),
                Instant.parse("2018-01-03T09:15:30Z"),
                true
            )
        ));

        Optional<LoadFundResult> result = service.processLoadFund(loadFund);

        assertTrue(result.isPresent());
        assertEquals("load-4", result.get().loadId());
        assertEquals("customer-1", result.get().customerId());
        assertFalse(result.get().accepted());

        ArgumentCaptor<LoadFundEntity> captor = ArgumentCaptor.forClass(LoadFundEntity.class);
        verify(loadFundRepository).save(captor.capture());

        LoadFundEntity savedLoad = captor.getValue();
        assertEquals("load-4", savedLoad.getLoadId());
        assertEquals("customer-1", savedLoad.getCustomerId());
        assertEquals(new BigDecimal("1000.00"), savedLoad.getLoadAmount());
        assertEquals(Instant.parse("2018-01-03T10:15:30Z"), savedLoad.getLoadTime());
        assertFalse(savedLoad.isAccepted());
    }
}
