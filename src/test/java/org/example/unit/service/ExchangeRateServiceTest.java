package org.example.unit.service;

import org.example.dto.MaxAndMinDto;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.example.service.ExchangeRateService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ExchangeRateServiceTest {

    @Autowired
    private ExchangeRateService service;

    @Test(expected = IllegalStateException.class)
    public void computeMaxAndMin_whenTableIsNull_shouldThrowException() {
        service.computeMaxAndMinValue(null);
    }

    @Test(expected = IllegalStateException.class)
    public void computeMaxAndMin_whenTableIsNotA_shouldThrowException() {
        //Arrange
        TableDto tableDto = new TableDto(
                "C",
                "funt szterling",
                "GBP",
                List.of(new RateDto())
        );

        //Act
        service.computeMaxAndMinValue(tableDto);
    }

    @Test(expected = IllegalStateException.class)
    public void computeMaxAndMin_whenRatesInTableIsEmpty_shouldThrowException() {
        //Arrange
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                "GBP",
                Collections.emptyList()
        );

        //Act
        service.computeMaxAndMinValue(tableDto);
    }

    @Test
    public void computeMaxAndMin_whenOnlyOneRateHasMajorDifference_shouldReturnThisValue() {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/C/NBP/2012",
                "2022-09-08",
                null,
                null,
                new BigDecimal("1.7")
        );
        RateDto secondRate = new RateDto(
                "2/C/NBP/2012",
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

        //Act
        MaxAndMinDto result = service.computeMaxAndMinValue(tableDto);

        //Assert
        assertThat(result.getMaxRate(), equalTo(secondRate));
        assertThat(result.getMinRate(), equalTo(firstRate));
    }

    @Test
    public void computeMaxAndMin_whenAllValuesAreTheSame_shouldReturnLastValueAsMaxAndMin() {
        //Arrange
        RateDto firstRate = new RateDto(
                "1/C/NBP/2012",
                "2022-09-08",
                null,
                null,
                new BigDecimal("1.6")
        );
        RateDto secondRate = new RateDto(
                "2/C/NBP/2012",
                "2022-09-07",
                null,
                null,
                new BigDecimal("1.6")
        );
        TableDto tableDto = new TableDto(
                "A",
                "funt szterling",
                "GBP",
                List.of(firstRate, secondRate)
        );

        //Act
        MaxAndMinDto result = service.computeMaxAndMinValue(tableDto);

        //Assert
        assertThat(result.getMaxRate(), equalTo(firstRate));
        assertThat(result.getMinRate(), equalTo(firstRate));
    }

    @Test(expected = IllegalStateException.class)
    public void computeMaxAndMin_whenMisIsNull_shouldThrowException() {
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
                "A",
                "funt szterling",
                "GBP",
                List.of(firstRate, secondRate)
        );

        //Act
        service.computeMaxAndMinValue(tableDto);
    }
}
