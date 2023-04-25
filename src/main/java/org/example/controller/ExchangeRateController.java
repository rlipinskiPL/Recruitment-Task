package org.example.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.MaxAndMinDto;
import org.example.dto.TableDto;
import org.example.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/exchange")
public class ExchangeRateController {
    @Autowired
    private ExchangeRateService service;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper mapper;

    @GetMapping("/{currency}/{date}")
    public ResponseEntity<?> getExchangeRate(@PathVariable String currency, @PathVariable String date, @RequestParam(defaultValue = "false") Boolean detailed) {
        if (!isCurrencyCorrect(currency)) {
            throw new IllegalArgumentException("Currency must be in ISO-4217 standard");
        }
        if (!date.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            throw new IllegalArgumentException("Date must be in ISO-8601 standard");
        }

        String nbpUrl = "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/" + date + "/";
        ResponseEntity<String> response = restTemplate.getForEntity(nbpUrl, String.class);

        TableDto responseTable = null;
        try {
            responseTable = mapper.readValue(response.getBody(), TableDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException();
        }

        if (detailed) {
            return new ResponseEntity<>(responseTable, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(responseTable.getRates().get(0).getMid(), HttpStatus.OK);
        }
    }

    @GetMapping("/{currency}/max-min")
    public ResponseEntity<?> getMaxAndMinValue(@PathVariable String currency, @RequestParam String quotations, @RequestParam(defaultValue = "false") Boolean detailed) {
        if (!isCurrencyCorrect(currency)) {
            throw new IllegalArgumentException("Currency must be in ISO-4217 standard");
        }
        if (!quotations.matches("\\d+")) {
            throw new IllegalArgumentException("Quotations must be a positive integer");
        }

        String nbpUrl = "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/last/" + quotations + "/";
        ResponseEntity<String> response = restTemplate.getForEntity(nbpUrl, String.class);

        TableDto responseTable = null;
        try {
            responseTable = mapper.readValue(response.getBody(), TableDto.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException();
        }

        MaxAndMinDto result = service.computeMaxAndMinValue(responseTable);
        if (result == null) {
            throw new IllegalStateException();
        }

        if (detailed) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Max rate: " + result.getMaxRate().getMid() + ", Min rate: " + result.getMinRate().getMid(), HttpStatus.OK);
        }
    }

    private boolean isCurrencyCorrect(String currency) {
        return currency.length() == 3 && currency.matches("[A-Z]+");
    }
}
