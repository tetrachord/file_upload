package com.example.controller;

import com.example.exception.IpAddressBlockedException;
import com.example.service.FileService;
import com.example.service.IpFilteringService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class FileController {

    private FileService fileService;
    private IpFilteringService ipFilteringService;

    @PostMapping (path = "/person",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public @ResponseBody Resource generateOutComeFile(@RequestPart MultipartFile personFile, HttpServletRequest httpServletRequest) throws IOException {

        String clientIpAddress = httpServletRequest.getHeader("X-FORWARDED-FOR");

        Optional<String> requestIpBlockedReason = ipFilteringService.getRequestIpBlockedReason(clientIpAddress);
        if ( requestIpBlockedReason.isPresent() ) {
            throw new IpAddressBlockedException(requestIpBlockedReason.get());
        };

        Path outcomeFilePath = fileService.save(personFile);

        ByteArrayResource byteArrayResource =  new ByteArrayResource(Files.readAllBytes(outcomeFilePath));

        return byteArrayResource;
    }

}
