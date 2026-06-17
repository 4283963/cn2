package com.fishing.tracker.controller;

import com.fishing.tracker.common.Result;
import com.fishing.tracker.dto.WeatherGridQueryDTO;
import com.fishing.tracker.entity.WeatherGrid;
import com.fishing.tracker.service.WeatherGridService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherGridController {

    @Resource
    private WeatherGridService weatherGridService;

    @GetMapping("/grid")
    public Result<List<WeatherGrid>> getGrid(WeatherGridQueryDTO queryDTO) {
        List<WeatherGrid> grids = weatherGridService.getGridByTimeAndBounds(
                queryDTO.getGridTime(),
                queryDTO.getMinLon(),
                queryDTO.getMaxLon(),
                queryDTO.getMinLat(),
                queryDTO.getMaxLat()
        );
        return Result.success(grids);
    }

    @GetMapping("/times")
    public Result<List<LocalDateTime>> getAvailableTimes(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(weatherGridService.getAvailableTimes(startTime, endTime));
    }
}
