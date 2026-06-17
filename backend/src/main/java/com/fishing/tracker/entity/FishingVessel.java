package com.fishing.tracker.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("fishing_vessel")
public class FishingVessel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String vesselName;

    private String imoNumber;

    private BigDecimal length;

    private BigDecimal tonnage;

    private String homePort;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getVesselName() { return vesselName; }
    public void setVesselName(String vesselName) { this.vesselName = vesselName; }
    public String getImoNumber() { return imoNumber; }
    public void setImoNumber(String imoNumber) { this.imoNumber = imoNumber; }
    public BigDecimal getLength() { return length; }
    public void setLength(BigDecimal length) { this.length = length; }
    public BigDecimal getTonnage() { return tonnage; }
    public void setTonnage(BigDecimal tonnage) { this.tonnage = tonnage; }
    public String getHomePort() { return homePort; }
    public void setHomePort(String homePort) { this.homePort = homePort; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}
