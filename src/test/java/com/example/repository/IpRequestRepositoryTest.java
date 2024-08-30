package com.example.repository;

import com.example.domain.IpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@Transactional
@SpringBootTest
@Sql({"/schema.sql"})
class IpRequestRepositoryTest {

    @Autowired
    private IpRequestRepository ipRequestRepository;

    @Test
    void shouldLogIpRequest() {

        // given
        UUID requestId = UUID.randomUUID();

        IpRequest ipRequest = IpRequest.builder()
                .requestId(requestId)
                .requestUri("http://ip-api.com/json/?fields=country,isp")
                .requestTimestamp(LocalDateTime.now())
                .requestIpAddress("1.2.3.4")
                .requestCountryCode("US")
                .requestIpProvider("some ISP in the US")
                .build();

        IpRequest savedEntity = ipRequestRepository.save(ipRequest);

        // when
        Optional<IpRequest> optionalRetrievedEntity = ipRequestRepository.getByRequestId(savedEntity.getRequestId());

        // then
        assertTrue(optionalRetrievedEntity.isPresent());
        assertEquals(requestId, optionalRetrievedEntity.get().getRequestId());
    }
}