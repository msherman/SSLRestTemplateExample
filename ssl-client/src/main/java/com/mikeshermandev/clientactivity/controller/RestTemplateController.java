package com.mikeshermandev.clientactivity.controller;

import com.mikeshermandev.clientactivity.dto.BoredResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/bored")
public class RestTemplateController {
    Logger logger = LoggerFactory.getLogger(RestTemplateController.class);

    @Autowired
    private RestTemplate sslRestTemplate;

    @GetMapping("/static")
    public ResponseEntity<BoredResponseDTO> staticValue() {
        logger.info("**** SSL loaded Rest template used");
        return sslRestTemplate.getForEntity("https://localhost:8443/server/static", BoredResponseDTO.class);
    }

    @GetMapping("/failure")
    public ResponseEntity<BoredResponseDTO> unextendedRestTemplate() {
        logger.info("**** Standard Rest template used");
        RestTemplate nonSSLRestTemplate = new RestTemplate();
        return nonSSLRestTemplate.getForEntity("https://localhost:8443/server/static", BoredResponseDTO.class);
    }

    // There is a bug with this that is causing the server to not send all bytes before the connection is closed
    @GetMapping("/activity")
    public ResponseEntity<BoredResponseDTO> activity() {
        logger.info("************************");
        logger.info("**** there is some bug with this template where the server doesn't close out its connection");
        logger.info("**** future TODO to resolve this.");
        logger.info("************************");
        ResponseEntity<BoredResponseDTO> response = sslRestTemplate.getForEntity("https://localhost:8443/server/activity", BoredResponseDTO.class);
        logger.info(response.getBody().toString());
        return response;
    }
}
