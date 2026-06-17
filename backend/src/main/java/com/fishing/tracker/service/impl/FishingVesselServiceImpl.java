package com.fishing.tracker.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fishing.tracker.entity.FishingVessel;
import com.fishing.tracker.mapper.FishingVesselMapper;
import com.fishing.tracker.service.FishingVesselService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FishingVesselServiceImpl extends ServiceImpl<FishingVesselMapper, FishingVessel>
        implements FishingVesselService {

    @Override
    public List<FishingVessel> listAllVessels() {
        LambdaQueryWrapper<FishingVessel> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(FishingVessel::getId);
        return this.list(wrapper);
    }

    @Override
    public FishingVessel getVesselById(Long id) {
        return this.getById(id);
    }
}
