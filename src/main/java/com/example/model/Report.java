package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class Report {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Transport")
    private String transport;

    @JsonProperty("Top Speed")
    private BigDecimal topSpeed;
}
