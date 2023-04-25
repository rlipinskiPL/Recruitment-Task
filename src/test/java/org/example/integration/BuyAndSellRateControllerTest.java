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
public class BuyAndSellRateControllerTest {

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
    public void getMajorDifference_whenResponseIsOk_shouldReturnJson() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 3;
        String url = getUrlWithQuotations(currency, quotations);

        RateDto rateDto1 = new RateDto(
                "174/A/NBP/2022",
                "2022-09-08",
                new BigDecimal("4.4321"),
                new BigDecimal("4.4300"),
                null);
        RateDto rateDto2 = new RateDto(
                "174/A/NBP/2022",
                "2022-09-09",
                new BigDecimal("4.3987"),
                new BigDecimal("4.3887"),
                null);
        RateDto rateDto3 = new RateDto(
                "174/A/NBP/2022",
                "2022-09-08",
                new BigDecimal("4.3456"),
                new BigDecimal("4.3448"),
                null);
        TableDto tableDto = new TableDto(
                "C",
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
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations + "&detailed=true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.difference", is(0.01)))
                .andExpect(jsonPath("$.rate.ask", is(4.3887)))
                .andExpect(jsonPath("$.rate.bid", is(4.3987)));

        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(0.01)));
    }

    @Test
    public void getMajorDifference_whenResponseIs404_shouldReturn404() throws Exception {
        //Arrange
        String currency = "GBP";
        Integer quotations = 3;
        String url = getUrlWithQuotations(currency, quotations);

        mockServer.expect(ExpectedCount.once(),
                        requestTo(new URI(url)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        //Act and Assert
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations)
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
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations)
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
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations)
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
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("Bad Request"));

        //Act and Assert
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getMaxAndMin_whenCurrencyIsInvalid_shouldReturn400() throws Exception {
        //Arrange
        String currency = "XXXX";
        Integer quotations = 12;

        //Act and Assert
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference?quotations=" + quotations)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    public void getMaxAndMin_whenQuotationsAreMissing_shouldReturn400() throws Exception {
        //Arrange
        String currency = "GBP";

        //Act and Assert
        mockMvc.perform(get("/api/buy-and-sell/" + currency + "/difference")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$", is("quotations parameter is required in the path")));
    }

    private String getUrlWithQuotations(String currency, Integer quotations) {
        return "http://api.nbp.pl/api/exchangerates/rates/C/" + currency + "/last/" + quotations + "/";
    }
}
