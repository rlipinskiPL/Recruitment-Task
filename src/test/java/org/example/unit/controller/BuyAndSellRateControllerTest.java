package org.example.unit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controller.BuyAndSellRateController;
import org.example.dto.DifferenceDto;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.example.service.BuyAndSellRateService;
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

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(BuyAndSellRateController.class)
public class BuyAndSellRateControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private BuyAndSellRateService service;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ObjectMapper mapper;

    @Test
    public void getMajorDifference_whenValidData_ShouldReturnJson() throws Exception {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/C/NBP/2012",
                "2022-09-08",
                new BigDecimal("1.5"),
                new BigDecimal("1.7"),
                null
        );
        RateDto secondRate = new RateDto(
                "2/C/NBP/2012",
                "2022-09-07",
                new BigDecimal("1.6"),
                new BigDecimal("1.7"),
                null
        );
        TableDto tableDto = new TableDto(
                "C",
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
        given(service.computeMajorDifference(tableDto)).willReturn(new DifferenceDto(new BigDecimal("0.1"), firstRate));

        //Act and Assert
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations + "&detailed=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difference", is(0.1)))
                .andExpect(jsonPath("$.rate.no", is(firstRate.getNo())))
                .andExpect(jsonPath("$.rate.effectiveDate", is(firstRate.getEffectiveDate())))
                .andExpect(jsonPath("$.rate.bid", is(firstRate.getBid().doubleValue())))
                .andExpect(jsonPath("$.rate.ask", is(firstRate.getAsk().doubleValue())))
                .andExpect(jsonPath("$.rate.mid").doesNotExist());

        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(0.1)));
    }

    @Test
    public void getMajorDifference_whenInvalidCurrency_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "XXXX";
        Integer quotations = 2;

        //Act and Assert
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Currency must be in ISO-4217 standard")));
    }

    @Test
    public void getMajorDifference_whenInvalidQuotations_shouldReturn400code() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = -123;

        //Act and Assert
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
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
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
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
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
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
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
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
        given(service.computeMajorDifference(tableDto)).willThrow(IllegalStateException.class);

        //Act and Assert
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
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
        given(service.computeMajorDifference(tableDto)).willReturn(null);

        //Act and Assert
        mvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations))
                .andExpect(status().is(500));
    }

    private String getUrlWithQuotations(String currency, Integer quotations) {
        return "http://api.nbp.pl/api/exchangerates/rates/C/" + currency + "/last/" + quotations + "/";
    }
}
