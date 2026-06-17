package com.fishing.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fishing.tracker.entity.WeatherGrid;
import com.fishing.tracker.mapper.WeatherGridMapper;
import com.fishing.tracker.service.WeatherGridService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WeatherGridServiceImpl extends ServiceImpl<WeatherGridMapper, WeatherGrid>
        implements WeatherGridService {

    @Override
    public List<WeatherGrid> getGridByTimeAndBounds(LocalDateTime gridTime,
                                                    BigDecimal minLon, BigDecimal maxLon,
                                                    BigDecimal minLat, BigDecimal maxLat) {
        return this.baseMapper.selectGridByTimeAndBounds(gridTime, minLon, maxLon, minLat, maxLat);
    }

    @Override
    public List<LocalDateTime> getAvailableTimes(LocalDateTime startTime, LocalDateTime endTime) {
        return this.baseMapper.selectAvailableTimes(startTime, endTime);
    }
}
