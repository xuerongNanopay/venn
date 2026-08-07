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

import org.springframework.stereotype.Service;

import com.veen.velocitylimits.domain.LoadFund;
import com.veen.velocitylimits.domain.LoadFundResult;
import com.veen.velocitylimits.entity.LoadFundEntity;
import com.veen.velocitylimits.repository.LoadRecordRepository;

@Service
public class VelocityLimitsService {
    
    private static final BigDecimal DAILY_LIMIT = new BigDecimal("5000.00");
    private static final BigDecimal WEEKLY_LIMIT = new BigDecimal("20000.00");
    private static final int DAILY_LOAD_COUNT_LIMIT = 3;

    private final LoadRecordRepository loadRecordRepository;

    public VelocityLimitsService(LoadRecordRepository loadRecordRepository) {
        this.loadRecordRepository= loadRecordRepository;
    }

    public Optional<LoadFundResult> processLoadFund(LoadFund loadFund) {

        if (loadRecordRepository
            .existsByLoadIdAndCustomerId(loadFund.loadId(), loadFund.customerId())
        ) {
            // if a load ID is observed more than once for a particular user, 
            // all but the first instance can be ignored
            return Optional.empty();
        }

        boolean accepted = validateLoadLimits(loadFund);

        if ( accepted ) {
            loadRecordRepository.save(
                new LoadFundEntity(
                    loadFund.loadId(), 
                    loadFund.customerId(),
                    loadFund.loadAmount(), 
                    loadFund.loadTime(),
                    accepted
            ));
        }


        return Optional.of(new LoadFundResult(
            loadFund.loadId(), 
            loadFund.customerId(), 
            accepted
        ));
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

        List<LoadFundEntity> dailyLoads = loadRecordRepository
            .findByCustomerIdAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
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

        List<LoadFundEntity> weekLoads = loadRecordRepository
            .findByCustomerIdAndLoadTimeGreaterThanEqualAndLoadTimeLessThan(
                loadFund.customerId(), 
                startOfWeek, 
                startOfNextWeek
            );
        
        BigDecimal weeklyTotal = dailyLoads.stream()
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
