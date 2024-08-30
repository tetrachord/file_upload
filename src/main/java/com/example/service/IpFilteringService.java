package com.example.service;

import com.example.model.IpApiResponse;
import com.example.domain.IpRequest;
import com.example.repository.IpRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class IpFilteringService {

    public static final String JSON = "/json/";
    public static final String QUERY_STRING = "?fields=country,isp";

    private static final String US = "US";
    private static final String SPAIN = "ES";
    private static final String CHINA = "CN";
    private static final String AWS = "AWS";
    private static final String GCP = "GCP";
    private static final String AZURE = "Azure";

    @Value("${ipapihost}")
    private String ipApiHost;

    private final RestTemplate restTemplate;

    private final IpRequestRepository ipRequestRepository;

    public IpFilteringService(RestTemplate restTemplate, IpRequestRepository ipRequestRepository) {
        this.restTemplate = restTemplate;
        this.ipRequestRepository = ipRequestRepository;
    }

    public Optional<String> getRequestIpBlockedReason(String clientIPAddress) {

        LocalDateTime requestTimestamp = LocalDateTime.now();

        String requestUri = ipApiHost + JSON + clientIPAddress + QUERY_STRING;

        IpApiResponse ipApiResponse =
                restTemplate.getForObject(requestUri, IpApiResponse.class);

        String country = ipApiResponse.getCountryCode();
        String isp = ipApiResponse.getIsp();

        IpRequest ipRequest = IpRequest.builder()
                .requestId(UUID.randomUUID())
                .requestUri(requestUri)
                .requestTimestamp(requestTimestamp)
                .requestIpAddress(clientIPAddress)
                .requestCountryCode(country)
                .requestIpProvider(isp)
                .build();

        ipRequestRepository.save(ipRequest);

        if ( country.equals(US) || country.equals(SPAIN) || country.equals(CHINA) ) {
            return Optional.of(String.format("IP address %s is blocked since it is from %s", clientIPAddress, country));
        }

        if ( isp.equals(AWS) || isp.equals(GCP) || isp.equals(AZURE) ) {
            return Optional.of(String.format("IP address %s is blocked since it is from %s", clientIPAddress, isp));
        }

        return Optional.empty();
    }
}
