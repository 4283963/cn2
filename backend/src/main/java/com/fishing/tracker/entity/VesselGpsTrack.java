package com.fishing.tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("vessel_gps_track")
public class VesselGpsTrack {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vesselId;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal speed;

    private BigDecimal heading;

    private LocalDateTime trackTime;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
