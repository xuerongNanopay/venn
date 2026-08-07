package com.veen.velocitylimits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoadFundRequest(
    @JsonProperty("id")
    String loadId,

    @JsonProperty("customer_id")
    String customerId,

    @JsonProperty("load_amount")
    String loadAmount,

    @JsonProperty("time")
    String loadTime
) {
}
