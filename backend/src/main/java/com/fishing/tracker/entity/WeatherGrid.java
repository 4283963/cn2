package com.fishing.tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
}
