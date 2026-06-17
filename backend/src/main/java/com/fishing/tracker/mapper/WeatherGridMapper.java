package com.fishing.tracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishing.tracker.entity.WeatherGrid;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface WeatherGridMapper extends BaseMapper<WeatherGrid> {

    @Select("SELECT * FROM weather_grid WHERE grid_time = #{gridTime} " +
            "AND longitude BETWEEN #{minLon} AND #{maxLon} " +
            "AND latitude BETWEEN #{minLat} AND #{maxLat} " +
            "AND deleted = 0")
    List<WeatherGrid> selectGridByTimeAndBounds(
            @Param("gridTime") LocalDateTime gridTime,
            @Param("minLon") BigDecimal minLon,
            @Param("maxLon") BigDecimal maxLon,
            @Param("minLat") BigDecimal minLat,
            @Param("maxLat") BigDecimal maxLat
    );

    @Select("SELECT DISTINCT grid_time FROM weather_grid " +
            "WHERE grid_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY grid_time ASC")
    List<LocalDateTime> selectAvailableTimes(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
