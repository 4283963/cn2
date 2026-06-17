package com.fishing.tracker.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WeatherGridQueryDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime gridTime;

    private BigDecimal minLon;

    private BigDecimal maxLon;

    private BigDecimal minLat;

    private BigDecimal maxLat;

    public LocalDateTime getGridTime() { return gridTime; }

    public void setGridTime(LocalDateTime gridTime) { this.gridTime = gridTime; }

    public BigDecimal getMinLon() { return minLon; }

    public void setMinLon(BigDecimal minLon) { this.minLon = minLon; }

    public BigDecimal getMaxLon() { return maxLon; }

    public void setMaxLon(BigDecimal maxLon) { this.maxLon = maxLon; }

    public BigDecimal getMinLat() { return minLat; }

    public void setMinLat(BigDecimal minLat) { this.minLat = minLat; }

    public BigDecimal getMaxLat() { return maxLat; }

    public void setMaxLat(BigDecimal maxLat) { this.maxLat = maxLat; }
}
