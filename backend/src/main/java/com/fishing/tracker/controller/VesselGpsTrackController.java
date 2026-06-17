package com.fishing.tracker.controller;

import com.fishing.tracker.common.Result;
import com.fishing.tracker.dto.TrackQueryDTO;
import com.fishing.tracker.entity.VesselGpsTrack;
import com.fishing.tracker.service.VesselGpsTrackService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/track")
public class VesselGpsTrackController {

    @Resource
    private VesselGpsTrackService vesselGpsTrackService;

    @GetMapping("/query")
    public Result<List<VesselGpsTrack>> queryTracks(TrackQueryDTO queryDTO) {
        List<VesselGpsTrack> tracks = vesselGpsTrackService.getTracksByVesselAndTimeRange(
                queryDTO.getVesselId(),
                queryDTO.getStartTime(),
                queryDTO.getEndTime()
        );
        return Result.success(tracks);
    }
}
