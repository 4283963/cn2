package com.fishing.tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("weather_grid")
public class WeatherGrid {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDateTime gridTime;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal pressure;

    private BigDecimal windSpeed;

    private BigDecimal windDirection;

    private BigDecimal waveHeight;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDateTime getGridTime() { return gridTime; }

    public void setGridTime(LocalDateTime gridTime) { this.gridTime = gridTime; }

    public BigDecimal getLongitude() { return longitude; }

    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getLatitude() { return latitude; }

    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getPressure() { return pressure; }

    public void setPressure(BigDecimal pressure) { this.pressure = pressure; }

    public BigDecimal getWindSpeed() { return windSpeed; }

    public void setWindSpeed(BigDecimal windSpeed) { this.windSpeed = windSpeed; }

    public BigDecimal getWindDirection() { return windDirection; }

    public void setWindDirection(BigDecimal windDirection) { this.windDirection = windDirection; }

    public BigDecimal getWaveHeight() { return waveHeight; }

    public void setWaveHeight(BigDecimal waveHeight) { this.waveHeight = waveHeight; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getDeleted() { return deleted; }

    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
