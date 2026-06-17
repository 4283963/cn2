package com.fishing.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fishing.tracker.entity.VesselGpsTrack;
import com.fishing.tracker.mapper.VesselGpsTrackMapper;
import com.fishing.tracker.service.VesselGpsTrackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VesselGpsTrackServiceImpl extends ServiceImpl<VesselGpsTrackMapper, VesselGpsTrack>
        implements VesselGpsTrackService {

    @Override
    public List<VesselGpsTrack> getTracksByVesselAndTimeRange(Long vesselId, LocalDateTime startTime, LocalDateTime endTime) {
        return this.baseMapper.selectTracksByVesselAndTimeRange(vesselId, startTime, endTime);
    }
}
