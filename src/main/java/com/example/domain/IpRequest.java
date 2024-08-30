package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "IPREQUEST")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class IpRequest {

    @Id
    private UUID requestId;

    private String requestUri;
    private LocalDateTime requestTimestamp;
    private String requestIpAddress;
    private String requestCountryCode;
    private String requestIpProvider;
}
