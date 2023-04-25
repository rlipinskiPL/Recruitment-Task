package org.example.service;

import org.example.dto.DifferenceDto;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BuyAndSellRateService {

    public DifferenceDto computeMajorDifference(TableDto table) {
        if (table == null || !table.getTable().equals("C") || table.getRates().isEmpty()) {
            throw new IllegalStateException();
        }
        List<RateDto> rates = table.getRates();

        BigDecimal majorDifference = new BigDecimal(-1);
        RateDto rateWithMajorDifference = null;
        for (var rate : rates) {
            BigDecimal ask = rate.getAsk();
            BigDecimal bid = rate.getBid();
            if (ask == null || bid == null)
                throw new IllegalStateException();

            BigDecimal currentDifference = ask.subtract(bid).abs();
            if (currentDifference.compareTo(majorDifference) > 0) {
                majorDifference = currentDifference;
                rateWithMajorDifference = rate;
            }
        }

        return new DifferenceDto(majorDifference, rateWithMajorDifference);
    }
}
