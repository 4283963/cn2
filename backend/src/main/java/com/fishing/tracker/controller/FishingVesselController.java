package com.fishing.tracker.controller;

import com.fishing.tracker.common.Result;
import com.fishing.tracker.entity.FishingVessel;
import com.fishing.tracker.service.FishingVesselService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/vessel")
public class FishingVesselController {

    @Resource
    private FishingVesselService fishingVesselService;

    @GetMapping("/list")
    public Result<List<FishingVessel>> listAll() {
        return Result.success(fishingVesselService.listAllVessels());
    }

    @GetMapping("/{id}")
    public Result<FishingVessel> getById(@PathVariable Long id) {
        return Result.success(fishingVesselService.getVesselById(id));
    }
}
