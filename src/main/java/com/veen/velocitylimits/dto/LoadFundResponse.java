package com.veen.velocitylimits.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoadFundResponse(
    @JsonProperty("id")
    String loadId,

    @JsonProperty("customer_id")
    String customerId,

    boolean accepted
) {

}
