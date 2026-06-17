<template>
  <div class="app-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>远洋渔船监控系统</h2>
      </div>

      <div class="sidebar-content">
        <div class="panel">
          <h3>渔船选择</h3>
          <el-select
            v-model="selectedVesselId"
            placeholder="请选择渔船"
            style="width: 100%"
            @change="onVesselChange"
          >
            <el-option
              v-for="vessel in vesselList"
              :key="vessel.id"
              :label="vessel.vesselName + ' (' + vessel.vesselCode + ')'"
              :value="vessel.id"
            />
          </el-select>
        </div>

        <div class="panel">
          <h3>时间范围</h3>
          <el-date-picker
            v-model="timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            @change="onTimeRangeChange"
          />
          <el-button
            type="primary"
            style="width: 100%; margin-top: 10px"
            :loading="loadingTrack"
            @click="queryTrack"
          >
            查询轨迹
          </el-button>
        </div>

        <div class="panel" v-if="trackPoints.length > 0">
          <h3>轨迹播放</h3>
          <div class="play-controls">
            <el-button-group>
              <el-button @click="resetPlayback" :disabled="!isPlaying && currentIndex === 0">
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-button @click="togglePlayback">
                <el-icon v-if="isPlaying"><Pause /></el-icon>
                <el-icon v-else><VideoPlay /></el-icon>
              </el-button>
              <el-button @click="stepForward" :disabled="currentIndex >= trackPoints.length - 1">
                <el-icon><Right /></el-icon>
              </el-button>
            </el-button-group>
            <div style="margin-top: 10px">
              <span style="font-size: 12px; color: #666">速度:</span>
              <el-select v-model="playSpeed" size="small" style="width: 80px; margin-left: 8px">
                <el-option :value="1" label="1x" />
                <el-option :value="2" label="2x" />
                <el-option :value="5" label="5x" />
                <el-option :value="10" label="10x" />
              </el-select>
            </div>
          </div>
          <div style="margin-top: 10px">
            <el-slider
              v-model="currentIndex"
              :min="0"
              :max="trackPoints.length - 1"
              :step="1"
              :show-tooltip="false"
              @change="onSliderChange"
            />
            <div style="display: flex; justify-content: space-between; font-size: 12px; color: #666">
              <span>{{ trackPoints[0]?.trackTime?.slice(0, 16) || '--' }}</span>
              <span>{{ currentPoint?.trackTime?.slice(0, 16) || '--' }}</span>
              <span>{{ trackPoints[trackPoints.length - 1]?.trackTime?.slice(0, 16) || '--' }}</span>
            </div>
          </div>
        </div>

        <div class="panel" v-if="currentPoint">
          <h3>当前信息</h3>
          <div class="info-item">
            <span class="label">时间:</span>
            <span class="value">{{ currentPoint.trackTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">经度:</span>
            <span class="value">{{ currentPoint.longitude }}</span>
          </div>
          <div class="info-item">
            <span class="label">纬度:</span>
            <span class="value">{{ currentPoint.latitude }}</span>
          </div>
          <div class="info-item">
            <span class="label">航速:</span>
            <span class="value">{{ currentPoint.speed }} 节</span>
          </div>
          <div class="info-item">
            <span class="label">航向:</span>
            <span class="value">{{ currentPoint.heading }}°</span>
          </div>
        </div>

        <div class="panel">
          <h3>气象叠加</h3>
          <el-select v-model="weatherType" size="small" style="width: 100%">
            <el-option value="windSpeed" label="风速 (m/s)" />
            <el-option value="pressure" label="气压 (hPa)" />
            <el-option value="waveHeight" label="浪高 (m)" />
          </el-select>
          <div style="margin-top: 10px">
            <el-switch v-model="showWeather" active-text="显示气象" />
          </div>
          <div class="legend" v-if="showWeather">
            <div class="legend-title">{{ weatherLegend.title }}</div>
            <div class="legend-bar">
              <div
                v-for="(item, idx) in weatherLegend.colors"
                :key="idx"
                class="legend-segment"
                :style="{ backgroundColor: item.color, flex: 1 }"
              ></div>
            </div>
            <div class="legend-labels">
              <span>{{ weatherLegend.min }}</span>
              <span>{{ weatherLegend.max }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="map-container">
      <FishingMap
        ref="mapRef"
        :track-points="trackPoints"
        :current-index="currentIndex"
        :weather-grids="weatherGrids"
        :show-weather="showWeather"
        :weather-type="weatherType"
        :weather-legend="weatherLegend"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { vesselApi, trackApi, weatherApi } from './api'
import FishingMap from './components/FishingMap.vue'

const mapRef = ref(null)
const vesselList = ref([])
const selectedVesselId = ref(null)
const timeRange = ref([])
const trackPoints = ref([])
const loadingTrack = ref(false)

const isPlaying = ref(false)
const currentIndex = ref(0)
const playSpeed = ref(1)
let playTimer = null

const showWeather = ref(true)
const weatherType = ref('windSpeed')
const weatherGrids = ref([])

const weatherConfig = {
  windSpeed: { title: '风速 (m/s)', min: 0, max: 25, unit: 'm/s' },
  pressure: { title: '气压 (hPa)', min: 980, max: 1030, unit: 'hPa' },
  waveHeight: { title: '浪高 (m)', min: 0, max: 8, unit: 'm' }
}

const weatherLegend = computed(() => {
  const config = weatherConfig[weatherType.value]
  const steps = 6
  const stepVal = (config.max - config.min) / (steps - 1)
  const colors = []
  for (let i = 0; i < steps; i++) {
    const val = config.min + stepVal * i
    colors.push({
      value: val,
      color: getWeatherColor(val, config.min, config.max)
    })
  }
  return {
    title: config.title,
    min: config.min,
    max: config.max,
    colors
  }
})

function getWeatherColor(value, min, max) {
  const ratio = Math.max(0, Math.min(1, (value - min) / (max - min)))
  if (ratio < 0.2) return '#313695'
  if (ratio < 0.4) return '#4575b4'
  if (ratio < 0.6) return '#74add1'
  if (ratio < 0.8) return '#fee090'
  if (ratio < 0.9) return '#f46d43'
  return '#d73027'
}

const currentPoint = computed(() => trackPoints.value[currentIndex.value] || null)

onMounted(async () => {
  const now = new Date()
  const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
  timeRange.value = [formatDate(weekAgo), formatDate(now)]

  try {
    vesselList.value = await vesselApi.list()
    if (vesselList.value.length > 0) {
      selectedVesselId.value = vesselList.value[0].id
      queryTrack()
    }
  } catch (e) {
    console.error('加载渔船列表失败', e)
  }
})

onUnmounted(() => {
  stopPlayback()
})

function formatDate(d) {
  const pad = (n) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function onVesselChange() {
  queryTrack()
}

function onTimeRangeChange() {
  queryTrack()
}

async function queryTrack() {
  if (!selectedVesselId.value || !timeRange.value || timeRange.value.length < 2) return

  loadingTrack.value = true
  stopPlayback()
  currentIndex.value = 0

  try {
    trackPoints.value = await trackApi.query({
      vesselId: selectedVesselId.value,
      startTime: timeRange.value[0],
      endTime: timeRange.value[1]
    })

    if (trackPoints.value.length > 0) {
      await loadWeatherData(trackPoints.value[0].trackTime)
    }
  } catch (e) {
    console.error('查询轨迹失败', e)
  } finally {
    loadingTrack.value = false
  }
}

async function loadWeatherData(timeStr) {
  if (!timeStr) return
  try {
    const trackLons = trackPoints.value.map(p => parseFloat(p.longitude))
    const trackLats = trackPoints.value.map(p => parseFloat(p.latitude))
    const minLon = Math.min(...trackLons) - 2
    const maxLon = Math.max(...trackLons) + 2
    const minLat = Math.min(...trackLats) - 2
    const maxLat = Math.max(...trackLats) + 2

    weatherGrids.value = await weatherApi.getGrid({
      gridTime: timeStr,
      minLon,
      maxLon,
      minLat,
      maxLat
    })
  } catch (e) {
    console.error('加载气象数据失败', e)
  }
}

function togglePlayback() {
  if (isPlaying.value) {
    stopPlayback()
  } else {
    startPlayback()
  }
}

function startPlayback() {
  if (currentIndex.value >= trackPoints.value.length - 1) {
    currentIndex.value = 0
  }
  isPlaying.value = true
  const interval = 1000 / playSpeed.value
  playTimer = setInterval(() => {
    if (currentIndex.value < trackPoints.value.length - 1) {
      currentIndex.value++
      if (currentIndex.value % 10 === 0) {
        loadWeatherData(trackPoints.value[currentIndex.value].trackTime)
      }
    } else {
      stopPlayback()
    }
  }, interval)
}

function stopPlayback() {
  isPlaying.value = false
  if (playTimer) {
    clearInterval(playTimer)
    playTimer = null
  }
}

function resetPlayback() {
  stopPlayback()
  currentIndex.value = 0
  if (trackPoints.value.length > 0) {
    loadWeatherData(trackPoints.value[0].trackTime)
  }
}

function stepForward() {
  if (currentIndex.value < trackPoints.value.length - 1) {
    currentIndex.value++
    loadWeatherData(trackPoints.value[currentIndex.value].trackTime)
  }
}

function onSliderChange() {
  if (trackPoints.value[currentIndex.value]) {
    loadWeatherData(trackPoints.value[currentIndex.value].trackTime)
  }
}

watch(playSpeed, () => {
  if (isPlaying.value) {
    stopPlayback()
    startPlayback()
  }
})
</script>

<style scoped>
.app-container {
  display: flex;
  width: 100%;
  height: 100%;
}

.sidebar {
  width: 320px;
  height: 100%;
  background: #fff;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 10;
}

.sidebar-header {
  padding: 16px;
  background: linear-gradient(135deg, #1e88e5, #1565c0);
  color: #fff;
}

.sidebar-header h2 {
  font-size: 16px;
  margin: 0;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.panel {
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 6px;
}

.panel h3 {
  font-size: 13px;
  color: #333;
  margin: 0 0 10px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.info-item {
  display: flex;
  justify-content: space-between;
  padding: 4px 0;
  font-size: 13px;
}

.info-item .label {
  color: #666;
}

.info-item .value {
  color: #333;
  font-weight: 500;
}

.play-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.legend {
  margin-top: 12px;
}

.legend-title {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  text-align: center;
}

.legend-bar {
  display: flex;
  height: 12px;
  border-radius: 2px;
  overflow: hidden;
}

.legend-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #999;
  margin-top: 4px;
}

.map-container {
  flex: 1;
  position: relative;
}
</style>
