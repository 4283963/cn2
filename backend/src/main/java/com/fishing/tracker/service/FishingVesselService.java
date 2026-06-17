package com.fishing.tracker.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fishing.tracker.entity.FishingVessel;

import java.util.List;

public interface FishingVesselService extends IService<FishingVessel> {

    List<FishingVessel> listAllVessels();

    FishingVessel getVesselById(Long id);
}
