package com.fishing.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fishing.tracker.entity.VesselGpsTrack;

import java.time.LocalDateTime;
import java.util.List;

public interface VesselGpsTrackService extends IService<VesselGpsTrack> {

    List<VesselGpsTrack> getTracksByVesselAndTimeRange(Long vesselId, LocalDateTime startTime, LocalDateTime endTime);
}
