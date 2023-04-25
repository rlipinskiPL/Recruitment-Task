package org.example.unit.service;

import org.example.dto.DifferenceDto;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.example.service.BuyAndSellRateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BuyAndSellRateServiceTest {

    @Autowired
    private BuyAndSellRateService service;

    @Test(expected = IllegalStateException.class)
    public void computeMajorDifference_whenTableIsNull_shouldThrowException() {
        service.computeMajorDifference(null);
    }

    @Test(expected = IllegalStateException.class)
    public void computeMajorDifference_whenTableIsNotC_shouldThrowException() {
        //Arrange
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                "GBP",
                List.of(new RateDto())
        );

        //Act
        service.computeMajorDifference(tableDto);
    }

    @Test(expected = IllegalStateException.class)
    public void computeMajorDifference_whenRatesInTableIsEmpty_shouldThrowException() {
        //Arrange
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                "GBP",
                Collections.emptyList()
        );

        //Act
        service.computeMajorDifference(tableDto);
    }

    @Test
    public void computeMajorDifference_whenOnlyOneRateHasMajorDifference_shouldReturnThisValue() {
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

        //Act
        DifferenceDto result = service.computeMajorDifference(tableDto);

        //Assert
        assertThat(result.getDifference(), equalTo(new BigDecimal("0.2")));
        assertThat(result.getRate(), equalTo(firstRate));
    }

    @Test
    public void computeMajorDifference_whenOnlyManyRateHasMajorDifference_shouldReturnLastValue() {
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
                new BigDecimal("1.5"),
                new BigDecimal("1.7"),
                null
        );
        TableDto tableDto = new TableDto(
                "C",
                "funt szterling",
                "GBP",
                List.of(firstRate, secondRate)
        );

        //Act
        DifferenceDto result = service.computeMajorDifference(tableDto);

        //Assert
        assertThat(result.getDifference(), equalTo(new BigDecimal("0.2")));
        assertThat(result.getRate(), equalTo(firstRate));
    }

    @Test
    public void computeMajorDifference_whenAllDifferencesAreEqualToZero_shouldReturnLastValue() {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/C/NBP/2012",
                "2022-09-08",
                new BigDecimal("1.7"),
                new BigDecimal("1.7"),
                null
        );
        RateDto secondRate = new RateDto(
                "2/C/NBP/2012",
                "2022-09-07",
                new BigDecimal("1.7"),
                new BigDecimal("1.7"),
                null
        );
        TableDto tableDto = new TableDto(
                "C",
                "funt szterling",
                "GBP",
                List.of(firstRate, secondRate)
        );

        //Act
        DifferenceDto result = service.computeMajorDifference(tableDto);

        //Assert
        assertThat(result.getDifference(), comparesEqualTo(new BigDecimal(0)));
        assertThat(result.getRate(), equalTo(firstRate));
    }

    @Test(expected = IllegalStateException.class)
    public void computeMajorDifference_whenAskOrBidAreNull_shouldThrowException() {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/C/NBP/2012",
                "2022-09-08",
                null,
                null,
                null
        );
        RateDto secondRate = new RateDto(
                "2/C/NBP/2012",
                "2022-09-07",
                null,
                null,
                null
        );
        TableDto tableDto = new TableDto(
                "C",
                "funt szterling",
                "GBP",
                List.of(firstRate, secondRate)
        );

        //Act
        service.computeMajorDifference(tableDto);
    }
}
