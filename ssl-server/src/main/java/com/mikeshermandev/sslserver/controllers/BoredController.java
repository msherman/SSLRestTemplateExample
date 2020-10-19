package com.mikeshermandev.sslserver.controllers;

import com.mikeshermandev.sslserver.dto.BoredResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/server")
public class BoredController {
    Logger logger = LoggerFactory.getLogger(BoredController.class);

    @GetMapping("/activity")
    public ResponseEntity<BoredResponseDTO> getActivity() {
        RestTemplate template = new RestTemplate();
        logger.info("**** Response from boredapi.com");
        ResponseEntity<BoredResponseDTO> response = template.getForEntity("https://www.boredapi.com/api/activity", BoredResponseDTO.class);
        logger.info(String.format("Response code: %s", response.getStatusCode().toString()));
        logger.info(response.getBody().toString());
        return response;
    }

    @GetMapping("/static")
    public ResponseEntity<BoredResponseDTO> getStatic() {
        logger.info("**** Static response called");
        BoredResponseDTO boredResponseDTO = new BoredResponseDTO("Start a daily journal", "relaxation", 1, 0.0f, "", "8779876", 0f);
        return ResponseEntity.ok(boredResponseDTO);
    }
}
