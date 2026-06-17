package com.fishing.tracker.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WeatherGridQueryDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gridTime;

    private BigDecimal minLon;

    private BigDecimal maxLon;

    private BigDecimal minLat;

    private BigDecimal maxLat;
}
