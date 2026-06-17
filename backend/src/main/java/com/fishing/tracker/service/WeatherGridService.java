package com.fishing.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fishing.tracker.entity.WeatherGrid;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface WeatherGridService extends IService<WeatherGrid> {

    List<WeatherGrid> getGridByTimeAndBounds(LocalDateTime gridTime,
                                             BigDecimal minLon, BigDecimal maxLon,
                                             BigDecimal minLat, BigDecimal maxLat);

    List<LocalDateTime> getAvailableTimes(LocalDateTime startTime, LocalDateTime endTime);
}
