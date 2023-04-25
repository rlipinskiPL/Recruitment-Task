package org.example.service;

import org.example.dto.MaxAndMinDto;
import org.example.dto.RateDto;
import org.example.dto.TableDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ExchangeRateService {

    public MaxAndMinDto computeMaxAndMinValue(TableDto table) {
        if (table == null || !table.getTable().equals("A") || table.getRates().isEmpty()) {
            throw new IllegalStateException();
        }
        List<RateDto> rates = table.getRates();

        RateDto maxRate = rates.stream()
                .max(Comparator.comparing(rate -> Optional.ofNullable(rate.getMid()).orElseThrow(IllegalStateException::new)))
                .orElseThrow(IllegalStateException::new);

        RateDto minRate = rates.stream()
                .min(Comparator.comparing(rate -> Optional.ofNullable(rate.getMid()).orElseThrow(IllegalStateException::new)))
                .orElseThrow(IllegalStateException::new);

        return new MaxAndMinDto(maxRate, minRate);
    }
}
