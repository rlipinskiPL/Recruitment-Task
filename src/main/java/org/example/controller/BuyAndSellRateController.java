package org.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.DifferenceDto;
import org.example.dto.TableDto;
import org.example.service.BuyAndSellRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/buy-and-sell")
public class BuyAndSellRateController {
    @Autowired
    private BuyAndSellRateService service;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @GetMapping("/{currency}/difference")
    public ResponseEntity<?> getMajorDifference(@PathVariable String currency, @RequestParam String quotations, @RequestParam(defaultValue = "false") Boolean detailed) {
        if (currency.length() != 3 || !currency.matches("[A-Z]+")) {
            throw new IllegalArgumentException("Currency must be in ISO-4217 standard");
        }
        if (!quotations.matches("\\d+")) {
            throw new IllegalArgumentException("Quotations must be a positive integer");
        }

        String nbpUrl = "http://api.nbp.pl/api/exchangerates/rates/C/" + currency + "/last/" + quotations + "/";
        ResponseEntity<String> response = restTemplate.getForEntity(nbpUrl, String.class);

        TableDto responseTable = null;
        try {
            responseTable = mapper.readValue(response.getBody(), TableDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException();
        }

        DifferenceDto result = service.computeMajorDifference(responseTable);
        if (result == null) {
            throw new IllegalStateException();
        }

        if (detailed) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result.getDifference(), HttpStatus.OK);
        }
    }
}
