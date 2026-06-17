package com.fishing.tracker.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fishing.tracker.dto.FishingZoneDTO;
import com.fishing.tracker.entity.VesselGpsTrack;
import com.fishing.tracker.entity.WeatherGrid;
import com.fishing.tracker.mapper.VesselGpsTrackMapper;
import com.fishing.tracker.service.VesselGpsTrackService;
import com.fishing.tracker.service.WeatherGridService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VesselGpsTrackServiceImpl extends ServiceImpl<VesselGpsTrackMapper, VesselGpsTrack>
        implements VesselGpsTrackService {

    @Resource
    private WeatherGridService weatherGridService;

    private static final BigDecimal SPEED_THRESHOLD = new BigDecimal("2.0");
    private static final BigDecimal WIND_SPEED_THRESHOLD = new BigDecimal("3.0");
    private static final BigDecimal WAVE_HEIGHT_THRESHOLD = new BigDecimal("1.5");
    private static final int MIN_POINTS_PER_ZONE = 4;
    private static final int MIN_DURATION_MINUTES = 60;

    @Override
    public List<VesselGpsTrack> getTracksByVesselAndTimeRange(Long vesselId, LocalDateTime startTime, LocalDateTime endTime) {
        return this.baseMapper.selectTracksByVesselAndTimeRange(vesselId, startTime, endTime);
    }

    @Override
    public List<FishingZoneDTO> analyzeFishingZones(Long vesselId, LocalDateTime startTime, LocalDateTime endTime) {
        List<VesselGpsTrack> tracks = getTracksByVesselAndTimeRange(vesselId, startTime, endTime);
        if (tracks == null || tracks.size() < MIN_POINTS_PER_ZONE) {
            return new ArrayList<>();
        }

        List<FishingZoneDTO> zones = new ArrayList<>();
        List<VesselGpsTrack> currentSlowSegment = new ArrayList<>();
        long zoneIdCounter = 1;

        for (int i = 0; i < tracks.size(); i++) {
            VesselGpsTrack point = tracks.get(i);
            BigDecimal speed = point.getSpeed();
            boolean isSlow = speed != null && speed.compareTo(SPEED_THRESHOLD) < 0;

            if (isSlow) {
                currentSlowSegment.add(point);
            }

            if ((!isSlow || i == tracks.size() - 1) && currentSlowSegment.size() >= MIN_POINTS_PER_ZONE) {
                VesselGpsTrack segStart = currentSlowSegment.get(0);
                VesselGpsTrack segEnd = currentSlowSegment.get(currentSlowSegment.size() - 1);
                long durationMinutes = Duration.between(segStart.getTrackTime(), segEnd.getTrackTime()).toMinutes();

                if (durationMinutes >= MIN_DURATION_MINUTES) {
                    int startIdx = i - currentSlowSegment.size() + (isSlow ? 1 : 0);
                    int endIdx = isSlow ? i : i - 1;
                    FishingZoneDTO zone = buildZone(zoneIdCounter++, segStart, segEnd, currentSlowSegment, startIdx, endIdx);
                    if (zone != null) {
                        zones.add(zone);
                    }
                }
                currentSlowSegment.clear();
            } else if (!isSlow) {
                currentSlowSegment.clear();
            }
        }

        return zones;
    }

    private FishingZoneDTO buildZone(Long id, VesselGpsTrack start, VesselGpsTrack end,
                                     List<VesselGpsTrack> segment, int startIdx, int endIdx) {
        BigDecimal minSpeed = null, maxSpeed = null, sumSpeed = BigDecimal.ZERO;
        for (VesselGpsTrack p : segment) {
            BigDecimal sp = p.getSpeed();
            if (sp == null) continue;
            if (minSpeed == null || sp.compareTo(minSpeed) < 0) minSpeed = sp;
            if (maxSpeed == null || sp.compareTo(maxSpeed) > 0) maxSpeed = sp;
            sumSpeed = sumSpeed.add(sp);
        }

        if (minSpeed == null) return null;

        BigDecimal avgSpeed = sumSpeed.divide(BigDecimal.valueOf(segment.size()), 2, RoundingMode.HALF_UP);
        long durationMinutes = Duration.between(start.getTrackTime(), end.getTrackTime()).toMinutes();

        BigDecimal minLon = start.getLongitude().min(end.getLongitude());
        BigDecimal maxLon = start.getLongitude().max(end.getLongitude());
        BigDecimal minLat = start.getLatitude().min(end.getLatitude());
        BigDecimal maxLat = start.getLatitude().max(end.getLatitude());

        BigDecimal avgWindSpeed = null;
        BigDecimal avgWaveHeight = null;
        BigDecimal avgPressure = null;
        boolean isCalm = false;

        try {
            LocalDateTime midTime = start.getTrackTime().plusMinutes(durationMinutes / 2);
            List<WeatherGrid> weatherData = weatherGridService.getGridByTimeAndBounds(
                    midTime,
                    minLon.subtract(BigDecimal.ONE),
                    maxLon.add(BigDecimal.ONE),
                    minLat.subtract(BigDecimal.ONE),
                    maxLat.add(BigDecimal.ONE)
            );

            if (weatherData != null && !weatherData.isEmpty()) {
                BigDecimal sumWind = BigDecimal.ZERO;
                BigDecimal sumWave = BigDecimal.ZERO;
                BigDecimal sumPress = BigDecimal.ZERO;
                int count = 0;

                for (WeatherGrid g : weatherData) {
                    if (g.getWindSpeed() != null) {
                        sumWind = sumWind.add(g.getWindSpeed());
                    }
                    if (g.getWaveHeight() != null) {
                        sumWave = sumWave.add(g.getWaveHeight());
                    }
                    if (g.getPressure() != null) {
                        sumPress = sumPress.add(g.getPressure());
                    }
                    count++;
                }

                if (count > 0) {
                    avgWindSpeed = sumWind.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                    avgWaveHeight = sumWave.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);
                    avgPressure = sumPress.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

                    isCalm = avgWindSpeed.compareTo(WIND_SPEED_THRESHOLD) < 0
                            && avgWaveHeight.compareTo(WAVE_HEIGHT_THRESHOLD) < 0;
                }
            }
        } catch (Exception e) {
            isCalm = true;
        }

        if (!isCalm) return null;

        FishingZoneDTO zone = new FishingZoneDTO();
        zone.setId(id);
        zone.setStartIndex(startIdx);
        zone.setEndIndex(endIdx);
        zone.setStartTime(start.getTrackTime());
        zone.setEndTime(end.getTrackTime());
        zone.setDurationMinutes(durationMinutes);
        zone.setAvgSpeed(avgSpeed);
        zone.setMinSpeed(minSpeed);
        zone.setMaxSpeed(maxSpeed);
        zone.setStartLongitude(start.getLongitude());
        zone.setStartLatitude(start.getLatitude());
        zone.setEndLongitude(end.getLongitude());
        zone.setEndLatitude(end.getLatitude());
        zone.setAvgWindSpeed(avgWindSpeed);
        zone.setAvgWaveHeight(avgWaveHeight);
        zone.setAvgPressure(avgPressure);
        zone.setStatus("FISHING");

        return zone;
    }
}
