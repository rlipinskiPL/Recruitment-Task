package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RateDto {
    private String no;

    private String effectiveDate;

    @Nullable
    private BigDecimal bid;

    @Nullable
    private BigDecimal ask;

    @Nullable
    private BigDecimal mid;
}
