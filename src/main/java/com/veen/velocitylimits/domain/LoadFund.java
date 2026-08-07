package com.veen.velocitylimits.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LoadFund(
    String loadId,
    String customerId,
    BigDecimal loadAmount,
    Instant loadTime
) {
}
