package com.veen.velocitylimits.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.domain.LoadFundResult;
import com.veen.velocitylimits.entity.CustomerEntity;
import com.veen.velocitylimits.entity.LoadFundEntity;
import com.veen.velocitylimits.exception.CustomerEntityLockException;
import com.veen.velocitylimits.repository.CustomerRepository;
import com.veen.velocitylimits.repository.LoadFundRepository;

@Service
public class VelocityLimitsService {
    
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("5000.00");
    private static final BigDecimal WEEKLY_LIMIT = new BigDecimal("20000.00");
    private static final int DAILY_LOAD_COUNT_LIMIT = 3;

    private final LoadFundRepository loadFundRepository;
    private final CustomerRepository customerRepository;

    public VelocityLimitsService(
        LoadFundRepository loadFundRepository,
        CustomerRepository customerRepository
    ) {
        this.loadFundRepository= loadFundRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public Optional<LoadFundResult> processLoadFund(LoadFund loadFund) {

        // 1. lock acount
        maybeCreateAndLockCustomerEntity(loadFund.customerId());

        // 2. Check duplicate load and custimer id.
        if (loadFundRepository
            .existsByLoadIdAndCustomerId(loadFund.loadId(), loadFund.customerId())
        ) {
            // if a load ID is observed more than once for a particular user, 
            // all but the first instance can be ignored
            //TODO: log
            return Optional.empty();
        }

        // 3. validate limit
        boolean accepted = validateLoadLimits(loadFund);

        loadFundRepository.save(
            new LoadFundEntity(
                loadFund.loadId(), 
                loadFund.customerId(),
                loadFund.loadAmount(), 
                loadFund.loadTime(),
                accepted
        ));


        return Optional.of(new LoadFundResult(
            loadFund.loadId(), 
            loadFund.customerId(), 
            accepted
        ));
    }

    /**
     * This is a hacker approach to create a customer.
     * The purpose of it is to make this application to work, as we don't have customer beforehand.
     * In the real production, we should have customer before allow them to load fund.
     */
    private CustomerEntity maybeCreateAndLockCustomerEntity(String customerId) {

        CustomerEntity customer = customerRepository
            .findByCustomerIdForUpdate(customerId)
            .orElse(null);

        if (customer != null) {
            return customer;
        }
        // Create a customer if not exist.
        try {
            customerRepository.save(new CustomerEntity(customerId));
        } catch (DataIntegrityViolationException ignored) {
            // another transaction created it
        }

        return customerRepository
            .findByCustomerIdForUpdate(customerId)
            .orElseThrow(() -> new CustomerEntityLockException(customerId));
    }

    /**
     * Ensure loadFund is compliance with limits
     *  1. A maximum of $5,000 can be loaded per day
     *  2. A maximum of 3 loads can be performed per day, regardless of amount
     *  3. A maximum of $20,000 can be loaded per week
     * 
     * @return true if loadFun passes all validations, elsewise false
     */
    private boolean validateLoadLimits(LoadFund loadFund) {

        Instant startOfDay = getStartOfUTCDay(loadFund.loadTime());
        Instant startOfNextDay = startOfDay.plus(1, ChronoUnit.DAYS);

        List<LoadFundEntity> dailyLoads = loadFundRepository
            .findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
                loadFund.customerId(),
                startOfDay,
                startOfNextDay
            );
        
        // A maximum of 3 loads can be performed per day, regardless of amount
        if (dailyLoads.size() >= DAILY_LOAD_COUNT_LIMIT) {
            //TODO: log
            return false;
        }

        BigDecimal dailyTotal = dailyLoads.stream()
            .map(LoadFundEntity::getLoadAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // A maximum of $5,000 can be loaded per day
        if (dailyTotal.add(loadFund.loadAmount()).compareTo(DAILY_LIMIT) > 0 ) {
            //TODO: log
            return false;
        }

        Instant startOfWeek = getStartOfUTCWeek(loadFund.loadTime());
        Instant startOfNextWeek = startOfWeek.plus(7, ChronoUnit.DAYS);

        List<LoadFundEntity> weekLoads = loadFundRepository
            .findByCustomerIdAndAcceptedTrueAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
                loadFund.customerId(), 
                startOfWeek, 
                startOfNextWeek
            );
        
        BigDecimal weeklyTotal = weekLoads.stream()
            .map(LoadFundEntity::getLoadAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // A maximum of $20,000 can be loaded per week
        if (weeklyTotal.add(loadFund.loadAmount()).compareTo(WEEKLY_LIMIT) > 0) {
            //TODO: log
            return false;
        }

        return true;
    }

    /**
     * Utility method to return an instant of the start of UTC day by given specific time.
     */
    private Instant getStartOfUTCDay(Instant time) {
        return time
                .atZone(ZoneOffset.UTC)
                .toLocalDate()
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant();
    }


    /**
     * Utility method to return an instant of the start of UTC week by given specific time.
     */
    private Instant getStartOfUTCWeek(Instant time) {
        LocalDate date = time
            .atZone(ZoneOffset.UTC)
            .toLocalDate();

        LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        return monday.atStartOfDay(ZoneOffset.UTC).toInstant();
    }


}
