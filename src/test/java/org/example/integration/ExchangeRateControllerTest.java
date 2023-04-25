package org.example.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExchangeRateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockRestServiceServer mockServer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void getExchangeRate_whenResponseIsOk_shouldReturnMid() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "2022-09-08";
        String url = getUrlWithDate(currency, date);

        RateDto rateDto = new RateDto(
                "174/A/NBP/2022",
                date,
                null,
                null,
                new BigDecimal("5.4322"));
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                currency,
                List.of(rateDto));

        mockServer.expect(ExpectedCount.twice(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(tableDto)));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/" + date + "?detailed=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rates[0].mid", is(rateDto.getMid().doubleValue())));

        mockMvc.perform(get("/api/exchange/" + currency + "/" + date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(rateDto.getMid().doubleValue())));
    }

    @Test
    public void getExchangeRate_whenResponseIs404_shouldReturn404() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "2022-09-08";
        String url = getUrlWithDate(currency, date);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/" + date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    public void getExchangeRate_whenResponseIs400_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "2022-09-08";
        String url = getUrlWithDate(currency, date);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("Bad Request"));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/" + date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Bad Request")));
    }

    @Test
    public void getExchangeRate_whenDateIsInvalid_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";
        String date = "08-09-2022";

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/" + date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getExchangeRate_whenCurrencyIsInvalid_shouldReturn400() throws Exception {
        //Arrange
        String currency = "XXXX";
        String date = "2022-08-09";

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/" + date)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getMaxAndMin_whenResponseIsOk_shouldReturnJson() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 3;
        String url = getUrlWithQuotations(currency, quotations);

        RateDto rateDto1 = new RateDto(
                "174/A/NBP/2022",
                "2022-09-08",
                null,
                null,
                new BigDecimal("5.4322"));
        RateDto rateDto2 = new RateDto(
                "174/A/NBP/2022",
                "2022-09-09",
                null,
                null,
                new BigDecimal("5.3902"));
        RateDto rateDto3 = new RateDto(
                "174/A/NBP/2022",
                "2022-09-08",
                null,
                null,
                new BigDecimal("5.4409"));
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                currency,
                List.of(rateDto1, rateDto2, rateDto3));

        mockServer.expect(ExpectedCount.twice(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(objectMapper.writeValueAsString(tableDto)));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations + "&detailed=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxRate.mid", is(rateDto3.getMid().doubleValue())))
                .andExpect(jsonPath("$.minRate.mid", is(rateDto2.getMid().doubleValue())));

        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is("Max rate: " + rateDto3.getMid() + ", Min rate: " + rateDto2.getMid())));
    }

    @Test
    public void getMaxAndMin_whenResponseIs404_shouldReturn404() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 3;
        String url = getUrlWithQuotations(currency, quotations);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404));
    }

    @Test
    public void getMaxAndMin_whenResponseIs400_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 3;
        String url = getUrlWithQuotations(currency, quotations);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("Bad Request"));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("Bad Request")));
    }

    @Test
    public void getMaxAndMin_whenQuotationsAreInvalid_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = -12;

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getMaxAndMin_whenQuotationsExceededSize_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 256;
        String url = getUrlWithQuotations(currency, quotations);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST));

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getMaxAndMin_whenCurrencyIsInvalid_shouldReturn400() throws Exception {
        //Arrange
        String currency = "XXXX";
        Integer quotations = 12;

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getMaxAndMin_whenQuotationsAreMissing_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";

        //Act and Assert
        mockMvc.perform(get("/api/exchange/" + currency + "/max-min")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("quotations parameter is required in the path")));
    }

    private String getUrlWithDate(String currency, String date) {
        return "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/" + date + "/";
    }

    private String getUrlWithQuotations(String currency, Integer quotations) {
        return "http://api.nbp.pl/api/exchangerates/rates/A/" + currency + "/last/" + quotations + "/";
    }
}
