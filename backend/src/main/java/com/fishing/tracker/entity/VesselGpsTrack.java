package com.fishing.tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getVesselId() { return vesselId; }

    public void setVesselId(Long vesselId) { this.vesselId = vesselId; }

    public BigDecimal getLongitude() { return longitude; }

    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public BigDecimal getLatitude() { return latitude; }

    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getSpeed() { return speed; }

    public void setSpeed(BigDecimal speed) { this.speed = speed; }

    public BigDecimal getHeading() { return heading; }

    public void setHeading(BigDecimal heading) { this.heading = heading; }

    public LocalDateTime getTrackTime() { return trackTime; }

    public void setTrackTime(LocalDateTime trackTime) { this.trackTime = trackTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getDeleted() { return deleted; }

    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
