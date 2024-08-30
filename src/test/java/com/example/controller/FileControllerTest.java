package com.example.controller;

import com.example.repository.IpRequestRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import com.maciejwalkowiak.wiremock.spring.InjectWireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;

import static com.example.service.IpFilteringService.JSON;
import static com.example.service.IpFilteringService.QUERY_STRING;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc
@AutoConfigureWebClient
@EnableWireMock({
        @ConfigureWireMock(port = 9090, name = "ip-filtering-service", property = "ipapihost")
})
@Transactional
@Sql({"/schema.sql"})
class FileControllerTest {

    private static final String USA_IP = "1.2.3.4";
    private static final String UK_IP  = "5.6.7.8";
    private static final String AWS_IP = "9.10.11.12";

    @InjectWireMock("ip-filtering-service")
    private WireMockServer wiremock;

    @Value("${ipapihost}")
    private String ipApiHost;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private IpRequestRepository ipRequestRepository;

    private File testDataFile;


    @Test
    void shouldGenerateOutcomeFile() throws Exception {

        // given
        stubUKIspResponse();

        String expectedJson = "[{\"Name\":\"John Smith\",\"Transport\":\"Bike\",\"Top Speed\":12.1},{\"Name\":\"Mike Smith\",\"Transport\":\"SUV\",\"Top Speed\":95.5},{\"Name\":\"Jenny Walters\",\"Transport\":\"Scooter\",\"Top Speed\":15.3}]";

        testDataFile = resourceLoader.getResource("classpath:EntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "EntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        // when
        mockMvc.perform(multipart("/person").file(multipartFile).header("X-FORWARDED-FOR", UK_IP))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldRejectEntryFileWithInvalidUUID() throws Exception {

        // given
        stubUKIspResponse();

        String expectedJson = "{\"violations\":[{\"fieldName\":\"uuid\",\"message\":\"Not a valid UUID\"}]}";

        testDataFile = resourceLoader.getResource("classpath:InvalidUUIDEntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "InvalidUUIDEntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        // when
        mockMvc.perform(multipart("/person")
                        .file(multipartFile)
                        .header("X-FORWARDED-FOR", UK_IP))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldRejectEntryFileWithInvalidTopSpeed() throws Exception {

        // given
        stubUKIspResponse();

        String expectedJson = """
                {
                  "violations": [
                    {
                      "fieldName": "topSpeed",
                      "message": "numeric value out of bounds (<2 digits>.<1 digits> expected)"
                    },
                    {
                      "fieldName": "topSpeed",
                      "message": "must be greater than or equal to 0.0"
                    },
                    {
                      "fieldName": "topSpeed",
                      "message": "must be less than or equal to 100.0"
                    }
                  ]
                }
                """;

        testDataFile = resourceLoader.getResource("classpath:InvalidTopSpeedEntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "InvalidTopSpeedEntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        // when
        mockMvc.perform(multipart("/person")
                        .file(multipartFile)
                        .header("X-FORWARDED-FOR", UK_IP))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldRejectEntryFileWithInvalidID() throws Exception {

        // given
        stubUKIspResponse();

        String expectedJson = "{\"violations\":[{\"fieldName\":\"id\",\"message\":\"must not be blank\"}]}";

        testDataFile = resourceLoader.getResource("classpath:InvalidIDEntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "InvalidIDEntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        // when
        mockMvc.perform(multipart("/person")
                        .file(multipartFile)
                        .header("X-FORWARDED-FOR", UK_IP))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldRejectHttpRequestWithUSAIpAddress() throws Exception {

        // given
        String expectedJson = "{\"reason\":\"IP address 1.2.3.4 is blocked since it is from US\"}";

        wiremock.stubFor(WireMock.get(JSON + USA_IP + QUERY_STRING).willReturn((aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {
                          "countryCode": "US",
                          "isp": "Some ISP in the US"
                        }
                        """))));

        testDataFile = resourceLoader.getResource("classpath:EntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "EntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        // when
        mockMvc.perform(multipart("/person")
                        .file(multipartFile)
                        .header("X-FORWARDED-FOR", USA_IP))
                .andExpect(status().isForbidden())
                .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldRejectHttpRequestWithAWSIpAddress() throws Exception {

        // given
        String expectedJson = "{\"reason\":\"IP address 9.10.11.12 is blocked since it is from AWS\"}";

        wiremock.stubFor(WireMock.get(JSON + AWS_IP + QUERY_STRING).willReturn((aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {
                          "countryCode": "GB",
                          "isp": "AWS"
                        }
                        """))));

        testDataFile = resourceLoader.getResource("classpath:EntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "EntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        // when
        mockMvc.perform(multipart("/person")
                        .file(multipartFile)
                        .header("X-FORWARDED-FOR", AWS_IP))
                .andExpect(status().isForbidden())
                .andExpect(content().json(expectedJson));
    }

    private void stubUKIspResponse() {
        wiremock.stubFor(WireMock.get(JSON + UK_IP + QUERY_STRING).willReturn((aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody("""
                        {
                          "countryCode": "GB",
                          "isp": "Some ISP in the UK"
                        }
                        """))));
    }
}