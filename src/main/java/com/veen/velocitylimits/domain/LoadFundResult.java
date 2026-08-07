package com.veen.velocitylimits.domain;

public record LoadFundResult(
    String loadId,
    String customerId,
    boolean accepted
){}
