package com.example.service;

import com.example.model.Person;
import com.example.model.Report;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class FileService {

    private static final String UPLOADS = "uploads";
    private static final String OUTCOME_FILENAME = "OutcomeFile";

    private final ValidationService validationService;

    public Path save(MultipartFile multipartFile) throws IOException {

        File outcomeFile = getEmptyOutcomeFile();

        List<Report> reportList = new ArrayList<>();

        InputStream inputStream = multipartFile.getInputStream();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {

            bufferedReader.lines()
                    .forEach(l -> {
                        Person person = validationService.validateLine(l);
                        Report report = Report.builder()
                                .name(person.getName())
                                .transport(extractModeOfTransportFrom(person.getTransport()))
                                .topSpeed(new BigDecimal(person.getTopSpeed()))
                                .build();

                        // TODO : remove debug code
                        System.out.println(report);

                        reportList.add(report);
                    });
        };

        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.writeValue(outcomeFile, reportList);

        return outcomeFile.toPath();
    }

    private static File getEmptyOutcomeFile() throws IOException {

        String rootPath = System.getProperty("user.dir");

        File uploadsDir = new File(rootPath + File.separator + UPLOADS);

        if ( !uploadsDir.exists() ) {
            uploadsDir.mkdir();
        }

        File outcomeFile = new File(uploadsDir.getAbsolutePath() + File.separator + OUTCOME_FILENAME);

        if ( !outcomeFile.exists() ) {
            outcomeFile.createNewFile();
        }
        return outcomeFile;
    }

    private String extractModeOfTransportFrom(String transport) {

        String result;

        if (transport.startsWith("Rides A ")) {
            result = transport.replace("Rides A ", "");
        } else if (transport.startsWith("Drives an ")) {
            result = transport.replace("Drives an ", "");
        } else {
            return transport;
        }

        return result;
    }
}
