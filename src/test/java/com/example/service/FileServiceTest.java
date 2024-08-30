package com.example.service;

import com.example.model.Person;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class FileServiceTest {

    @MockBean
    private ValidationService validationService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void shouldReturnAPathForAValidMultipartFile() throws IOException {

        // given
        File testDataFile = resourceLoader.getResource("classpath:EntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "EntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        Person person = Person.builder()
                .uuid("some UUID")
                .id("some ID")
                .name("Joe Bloggs")
                .likes("Likes Grape")
                .transport("Rides A Bike")
                .avgSpeed("6.5")
                .topSpeed("20.3")
                .build();
        when(validationService.validateLine(anyString())).thenReturn(person);

        // when
        Path generatedOutcomeFilePath = fileService.save(multipartFile);

        // then
        assertEquals("OutcomeFile", generatedOutcomeFilePath.getFileName().toString());
    }

    @Test
    void shouldThrowValidationExceptionForAnInvalidMultipartFile() throws IOException {

        // given
        File testDataFile = resourceLoader.getResource("classpath:InvalidIDEntryFile.txt").getFile();
        byte[] testDataFileContent = Files.readAllBytes(testDataFile.toPath());

        MockMultipartFile multipartFile
                = new MockMultipartFile(
                "personFile",
                "InvalidIDEntryFile.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                testDataFileContent
        );

        ConstraintViolationException mockConstraintViolationException = mock(ConstraintViolationException.class);
        when(validationService.validateLine(anyString())).thenThrow(mockConstraintViolationException);

        // when
        Exception thrown = assertThrows(
                ConstraintViolationException.class,
                () -> fileService.save(multipartFile),
                "save() should have thrown exception"
        );
    }
}