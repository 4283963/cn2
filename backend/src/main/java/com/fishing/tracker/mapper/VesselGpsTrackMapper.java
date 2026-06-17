package com.fishing.tracker.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fishing.tracker.entity.VesselGpsTrack;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VesselGpsTrackMapper extends BaseMapper<VesselGpsTrack> {

    @Select("SELECT * FROM vessel_gps_track WHERE vessel_id = #{vesselId} " +
            "AND track_time BETWEEN #{startTime} AND #{endTime} " +
            "AND deleted = 0 ORDER BY track_time ASC")
    List<VesselGpsTrack> selectTracksByVesselAndTimeRange(
            @Param("vesselId") Long vesselId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
