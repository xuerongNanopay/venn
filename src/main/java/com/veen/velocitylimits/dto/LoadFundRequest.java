package com.veen.velocitylimits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoadFundRequest(
    String id,

    @JsonProperty("customer_id")
    String customerId,

    @JsonProperty("load_amount")
    String loadAmount,

    String time
) {
}
