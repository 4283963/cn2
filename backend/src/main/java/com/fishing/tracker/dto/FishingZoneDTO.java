package com.fishing.tracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FishingZoneDTO {

    private Long id;

    private Integer startIndex;

    private Integer endIndex;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMinutes;

    private BigDecimal avgSpeed;

    private BigDecimal minSpeed;

    private BigDecimal maxSpeed;

    private BigDecimal startLongitude;

    private BigDecimal startLatitude;

    private BigDecimal endLongitude;

    private BigDecimal endLatitude;

    private BigDecimal avgWindSpeed;

    private BigDecimal avgWaveHeight;

    private BigDecimal avgPressure;

    private String status;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Integer getStartIndex() { return startIndex; }

    public void setStartIndex(Integer startIndex) { this.startIndex = startIndex; }

    public Integer getEndIndex() { return endIndex; }

    public void setEndIndex(Integer endIndex) { this.endIndex = endIndex; }

    public LocalDateTime getStartTime() { return startTime; }

    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }

    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public Long getDurationMinutes() { return durationMinutes; }

    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public BigDecimal getAvgSpeed() { return avgSpeed; }

    public void setAvgSpeed(BigDecimal avgSpeed) { this.avgSpeed = avgSpeed; }

    public BigDecimal getMinSpeed() { return minSpeed; }

    public void setMinSpeed(BigDecimal minSpeed) { this.minSpeed = minSpeed; }

    public BigDecimal getMaxSpeed() { return maxSpeed; }

    public void setMaxSpeed(BigDecimal maxSpeed) { this.maxSpeed = maxSpeed; }

    public BigDecimal getStartLongitude() { return startLongitude; }

    public void setStartLongitude(BigDecimal startLongitude) { this.startLongitude = startLongitude; }

    public BigDecimal getStartLatitude() { return startLatitude; }

    public void setStartLatitude(BigDecimal startLatitude) { this.startLatitude = startLatitude; }

    public BigDecimal getEndLongitude() { return endLongitude; }

    public void setEndLongitude(BigDecimal endLongitude) { this.endLongitude = endLongitude; }

    public BigDecimal getEndLatitude() { return endLatitude; }

    public void setEndLatitude(BigDecimal endLatitude) { this.endLatitude = endLatitude; }

    public BigDecimal getAvgWindSpeed() { return avgWindSpeed; }

    public void setAvgWindSpeed(BigDecimal avgWindSpeed) { this.avgWindSpeed = avgWindSpeed; }

    public BigDecimal getAvgWaveHeight() { return avgWaveHeight; }

    public void setAvgWaveHeight(BigDecimal avgWaveHeight) { this.avgWaveHeight = avgWaveHeight; }

    public BigDecimal getAvgPressure() { return avgPressure; }

    public void setAvgPressure(BigDecimal avgPressure) { this.avgPressure = avgPressure; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
