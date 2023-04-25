package org.example.unit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.ExchangeRateController;
import org.example.dto.MaxAndMinDto;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.example.service.ExchangeRateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExchangeRateController.class)
public class ExchangeRateControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private ExchangeRateService service;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ObjectMapper mapper;

    @Test
    public void getExchangeRate_whenValidData_ShouldReturnJson() throws Exception {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/A/NBP/2012",
                "2022-09-08",
                null,
                null,
                new BigDecimal("1.6")
        );
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                "GBP",
                List.of(firstRate)
        );

        String currency = "GBP";
        String date = "2022-09-08";
        String nbpUrl = getUrlWithDate(currency, date);
        String responseBody = "Value needed for mock";
        given(restTemplate.getForEntity(nbpUrl, String.class)).willReturn(new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(200)));
        given(mapper.readValue(responseBody, TableDto.class)).willReturn(tableDto);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/" + date))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(1.6)));

        mvc.perform(get("/api/exchange/" + currency + "/" + date + "?detailed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates[0].mid", is(1.6)));
    }

    @Test
    public void getExchangeRate_whenInvalidCurrency_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "XXXX";
        String date = "2022-09-08";

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/" + date))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Currency must be in ISO-4217 standard")));
    }

    @Test
    public void getExchangeRate_whenInvalidDate_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "09-08-2022";

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/" + date))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Date must be in ISO-8601 standard")));
    }

    @Test
    public void getExchangeRate_whenNbpApiReturn404_shouldReturn404code() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "2022-09-08";
        String nbpUrl = getUrlWithDate(currency, date);

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatusCode.valueOf(404));
        given(restTemplate.getForEntity(nbpUrl, String.class)).willThrow(exception);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/" + date))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$", is("Data not found")));
    }

    @Test
    public void getExchangeRate_whenMapperThrowsException_shouldReturn500code() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "2022-09-08";
        String nbpUrl = getUrlWithDate(currency, date);

        String responseBody = "Value needed for mock";
        given(restTemplate.getForEntity(nbpUrl, String.class)).willReturn(new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(200)));
        given(mapper.readValue(responseBody, TableDto.class)).willThrow(JsonProcessingException.class);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/" + date))
                .andExpect(status().is(500));
    }

    @Test
    public void getMaxAndMin_whenValidData_ShouldReturnJson() throws Exception {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/A/NBP/2012",
                "2022-09-08",
                null,
                null,
                new BigDecimal("1.6")
        );
        RateDto secondRate = new RateDto(
                "2/A/NBP/2012",
                "2022-09-07",
                null,
                null,
                new BigDecimal("1.8")
        );
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                "GBP",
                List.of(firstRate, secondRate)
        );

        String currency = "GBP";
        Integer quotations = 2;
        String nbpUrl = getUrlWithQuotations(currency, quotations);
        String responseBody = "Value needed for mock";
        given(restTemplate.getForEntity(nbpUrl, String.class)).willReturn(new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(200)));
        given(mapper.readValue(responseBody, TableDto.class)).willReturn(tableDto);
        given(service.computeMaxAndMinValue(tableDto)).willReturn(new MaxAndMinDto(secondRate, firstRate));

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations + "&detailed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxRate.no", equalTo(secondRate.getNo())))
                .andExpect(jsonPath("$.maxRate.effectiveDate", is(secondRate.getEffectiveDate())))
                .andExpect(jsonPath("$.maxRate.mid", is(secondRate.getMid().doubleValue())))
                .andExpect(jsonPath("$.maxRate.ask").doesNotExist())
                .andExpect(jsonPath("$.maxRate.bid").doesNotExist());

        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Max rate: " + secondRate.getMid() + ", Min rate: " + firstRate.getMid())));
    }

    @Test
    public void getMaxAndMin_whenInvalidCurrency_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "XXXX";
        Integer quotations = 2;

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Currency must be in ISO-4217 standard")));
    }

    @Test
    public void getMajorDifference_whenInvalidQuotations_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = -123;

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Quotations must be a positive integer")));
    }

    @Test
    public void getMajorDifference_whenQuotationsExceededSize_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 256;
        String nbpUrl = getUrlWithQuotations(currency, quotations);

        String errorText = "Przekroczony limit 255 wyników / Maximum size of 255 data series has been exceeded";
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatusCode.valueOf(400), errorText);
        given(restTemplate.getForEntity(nbpUrl, String.class)).willThrow(exception);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is(errorText)));
    }

    @Test
    public void getMajorDifference_whenNbpApiReturn404_shouldReturn404code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 2;
        String nbpUrl = getUrlWithQuotations(currency, quotations);

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatusCode.valueOf(404));
        given(restTemplate.getForEntity(nbpUrl, String.class)).willThrow(exception);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$", is("Data not found")));
    }

    @Test
    public void getMajorDifference_whenMapperThrowsException_shouldReturn500code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 2;
        String nbpUrl = getUrlWithQuotations(currency, quotations);

        String responseBody = "Value needed for mock";
        given(restTemplate.getForEntity(nbpUrl, String.class)).willReturn(new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(200)));
        given(mapper.readValue(responseBody, TableDto.class)).willThrow(JsonProcessingException.class);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(500));
    }

    @Test
    public void getMajorDifference_whenServiceThrowsException_shouldReturn500code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 2;
        String nbpUrl = getUrlWithQuotations(currency, quotations);
        TableDto tableDto = new TableDto();

        String responseBody = "Value needed for mock";
        given(restTemplate.getForEntity(nbpUrl, String.class)).willReturn(new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(200)));
        given(mapper.readValue(responseBody, TableDto.class)).willReturn(tableDto);
        given(service.computeMaxAndMinValue(tableDto)).willThrow(IllegalStateException.class);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(500));
    }

    @Test
    public void getMajorDifference_whenServiceReturnNull_shouldReturn500code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 2;
        String nbpUrl = getUrlWithQuotations(currency, quotations);
        TableDto tableDto = new TableDto();

        String responseBody = "Value needed for mock";
        given(restTemplate.getForEntity(nbpUrl, String.class)).willReturn(new ResponseEntity<>(responseBody, HttpStatusCode.valueOf(200)));
        given(mapper.readValue(responseBody, TableDto.class)).willReturn(tableDto);
        given(service.computeMaxAndMinValue(tableDto)).willReturn(null);

        //Act and Assert
        mvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations))
                .andExpect(status().is(500));
    }

    private String getUrlWithDate(String currency, String date) {
        return "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/" + date + "/";
    }

    private String getUrlWithQuotations(String currency, Integer quotations) {
        return "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/last/" + quotations + "/";
    }
}
