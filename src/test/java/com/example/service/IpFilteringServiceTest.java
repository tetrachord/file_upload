package com.example.service;

import com.example.domain.IpRequest;
import com.example.model.IpApiResponse;
import com.example.repository.IpRequestRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.example.service.IpFilteringService.JSON;
import static com.example.service.IpFilteringService.QUERY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = "ipapihost=http://localhost:8080")
class IpFilteringServiceTest {

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private IpRequestRepository ipRequestRepository;

    @Autowired
    private IpFilteringService ipFilteringService;

    @Value("${ipapihost}")
    private String ipApiHost;

    @Captor
    private ArgumentCaptor<IpRequest> ipRequestArgumentCaptor;

    @Test
    void shouldNotBlockAnIpFromUKAndNonCloudHost() {

        // given
        String ukIpAddress = "9.8.7.6";

        String requestUri = ipApiHost + JSON + ukIpAddress + QUERY_STRING;
        IpApiResponse ipApiResponse = new IpApiResponse("GB", "some ISP in the UK");
        when(restTemplate.getForObject(requestUri, IpApiResponse.class)).thenReturn(ipApiResponse);

        IpRequest expectedIpRequest = IpRequest.builder()
                        .requestId(UUID.randomUUID())
                .requestUri(requestUri)
                .requestTimestamp(LocalDateTime.now())
                .requestIpAddress(ukIpAddress)
                .requestCountryCode("GB")
                .requestIpProvider("some ISP in the UK")
                .build();

        // when
        Optional<String> optionalRequestIpBlockedReason = ipFilteringService.getRequestIpBlockedReason(ukIpAddress);

        // then
        assertTrue(optionalRequestIpBlockedReason.isEmpty());
        verify(ipRequestRepository).save(ipRequestArgumentCaptor.capture());

        IpRequest actualIpRequest = ipRequestArgumentCaptor.getValue();
        assertEquals(requestUri, actualIpRequest.getRequestUri());
        assertEquals(ukIpAddress, actualIpRequest.getRequestIpAddress());
        assertEquals("GB", actualIpRequest.getRequestCountryCode());
        assertEquals("some ISP in the UK", actualIpRequest.getRequestIpProvider());
    }

    @Test
    void shouldBlockAnIpFromSpain() {

        // given
        String spainIpAddress = "10.11.12.13";

        String requestUri = ipApiHost + JSON + spainIpAddress + QUERY_STRING;
        IpApiResponse ipApiResponse = new IpApiResponse("ES", "some ISP in Spain");
        when(restTemplate.getForObject(requestUri, IpApiResponse.class)).thenReturn(ipApiResponse);

        IpRequest expectedIpRequest = IpRequest.builder()
                .requestId(UUID.randomUUID())
                .requestUri(requestUri)
                .requestTimestamp(LocalDateTime.now())
                .requestIpAddress(spainIpAddress)
                .requestCountryCode("ES")
                .requestIpProvider("some ISP in Spain")
                .build();

        // when
        Optional<String> optionalRequestIpBlockedReason = ipFilteringService.getRequestIpBlockedReason(spainIpAddress);

        // then
        assertTrue(optionalRequestIpBlockedReason.isPresent());
        assertEquals("IP address 10.11.12.13 is blocked since it is from ES", optionalRequestIpBlockedReason.get());

        verify(ipRequestRepository).save(ipRequestArgumentCaptor.capture());

        IpRequest actualIpRequest = ipRequestArgumentCaptor.getValue();
        assertEquals(requestUri, actualIpRequest.getRequestUri());
        assertEquals(spainIpAddress, actualIpRequest.getRequestIpAddress());
        assertEquals("ES", actualIpRequest.getRequestCountryCode());
        assertEquals("some ISP in Spain", actualIpRequest.getRequestIpProvider());
    }

    @Test
    void shouldBlockAnIpFromAzureIsp() {

        // given
        String ukAzureIpAddress = "14.15.16.17";

        String requestUri = ipApiHost + JSON + ukAzureIpAddress + QUERY_STRING;
        IpApiResponse ipApiResponse = new IpApiResponse("GB", "Azure");
        when(restTemplate.getForObject(requestUri, IpApiResponse.class)).thenReturn(ipApiResponse);

        IpRequest expectedIpRequest = IpRequest.builder()
                .requestId(UUID.randomUUID())
                .requestUri(requestUri)
                .requestTimestamp(LocalDateTime.now())
                .requestIpAddress(ukAzureIpAddress)
                .requestCountryCode("GB")
                .requestIpProvider("Azure")
                .build();

        // when
        Optional<String> optionalRequestIpBlockedReason = ipFilteringService.getRequestIpBlockedReason(ukAzureIpAddress);

        // then
        assertTrue(optionalRequestIpBlockedReason.isPresent());
        assertEquals("IP address 14.15.16.17 is blocked since it is from Azure", optionalRequestIpBlockedReason.get());

        verify(ipRequestRepository).save(ipRequestArgumentCaptor.capture());

        IpRequest actualIpRequest = ipRequestArgumentCaptor.getValue();
        assertEquals(requestUri, actualIpRequest.getRequestUri());
        assertEquals(ukAzureIpAddress, actualIpRequest.getRequestIpAddress());
        assertEquals("GB", actualIpRequest.getRequestCountryCode());
        assertEquals("Azure", actualIpRequest.getRequestIpProvider());
    }
}