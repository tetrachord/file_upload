package com.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IpApiResponse {

    private String countryCode;
    private String isp;
}
