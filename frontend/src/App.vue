<template>
  <div class="app-container">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>远洋渔船监控系统</h2>
        <div class="sub-title" v-if="trackPoints.length">
          轨迹点数: {{ trackPoints.length }}
        </div>
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
              <el-button @click="resetPlayback">
                <el-icon><Refresh /></el-icon>
              </el-button>
              <el-button @click="togglePlayback" :type="isPlaying ? 'warning' : 'success'">
                <el-icon v-if="isPlaying"><Pause /></el-icon>
                <el-icon v-else><VideoPlay /></el-icon>
              </el-button>
              <el-button @click="stepForward" :disabled="uiIndex >= trackPoints.length - 1">
                <el-icon><Right /></el-icon>
              </el-button>
            </el-button-group>
            <div class="speed-row">
              <span class="speed-label">速度:</span>
              <el-select v-model="playSpeed" size="small" class="speed-select">
                <el-option :value="0.5" label="0.5x" />
                <el-option :value="1" label="1x" />
                <el-option :value="2" label="2x" />
                <el-option :value="5" label="5x" />
                <el-option :value="10" label="10x" />
                <el-option :value="30" label="30x" />
                <el-option :value="60" label="60x" />
              </el-select>
            </div>
          </div>
          <div class="slider-block">
            <el-slider
              :model-value="uiIndex"
              :min="0"
              :max="trackPoints.length - 1"
              :step="1"
              :show-tooltip="false"
              @input="onSliderInput"
              @change="onSliderChange"
            />
            <div class="time-row">
              <span class="time-label">{{ startLabel }}</span>
              <span class="time-label time-current">{{ currentLabel }}</span>
              <span class="time-label">{{ endLabel }}</span>
            </div>
          </div>
        </div>

        <div class="panel" v-if="uiPoint">
          <h3>当前信息</h3>
          <div class="info-item"><span class="label">时间:</span><span class="value">{{ uiPoint.trackTime }}</span></div>
          <div class="info-item"><span class="label">经度:</span><span class="value">{{ uiPoint.longitude }}</span></div>
          <div class="info-item"><span class="label">纬度:</span><span class="value">{{ uiPoint.latitude }}</span></div>
          <div class="info-item"><span class="label">航速:</span><span class="value">{{ uiPoint.speed }} 节</span></div>
          <div class="info-item"><span class="label">航向:</span><span class="value">{{ uiPoint.heading }}°</span></div>
        </div>

        <div class="panel">
          <h3>气象叠加</h3>
          <el-select v-model="weatherType" size="small" style="width: 100%">
            <el-option value="windSpeed" label="风速 (m/s)" />
            <el-option value="pressure" label="气压 (hPa)" />
            <el-option value="waveHeight" label="浪高 (m)" />
          </el-select>
          <div class="switch-row">
            <el-switch v-model="showWeather" active-text="显示气象" />
            <span class="weather-status" :class="weatherStatusClass">
              {{ weatherStatusText }}
            </span>
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
        :display-index="uiIndex"
        :weather-grids="displayWeatherGrids"
        :show-weather="showWeather"
        :weather-type="weatherType"
        :fishing-zones="fishingZones"
      />

      <div class="zone-stats-panel" v-if="fishingZones.length > 0 || analyzingZones">
        <div class="zone-stats-header">
          <el-icon :size="16" color="#ffc107"><Warning /></el-icon>
          <span class="zone-stats-title">捕鱼作业区分析</span>
          <el-loading v-if="analyzingZones" size="small" style="margin-left: auto" />
        </div>
        <div class="zone-stats-body" v-if="!analyzingZones">
          <div class="zone-stat-row">
            <span class="stat-label">作业次数</span>
            <span class="stat-value highlight">{{ fishingZones.length }}</span>
            <span class="stat-unit">次</span>
          </div>
          <div class="zone-stat-row">
            <span class="stat-label">总作业时长</span>
            <span class="stat-value">{{ totalFishingHours }}</span>
            <span class="stat-unit">小时</span>
          </div>
          <div class="zone-stat-row" v-if="avgSpeed">
            <span class="stat-label">平均航速</span>
            <span class="stat-value">{{ avgSpeed }}</span>
            <span class="stat-unit">节</span>
          </div>
        </div>
        <div class="zone-stats-body" v-else>
          <div class="analyzing-text">正在分析航迹...</div>
        </div>
        <div class="zone-list" v-if="!analyzingZones && fishingZones.length > 0">
          <div class="zone-item" v-for="(zone, idx) in fishingZones" :key="zone.id">
            <div class="zone-badge">#{{ idx + 1 }}</div>
            <div class="zone-info">
              <div class="zone-time">{{ formatZoneTime(zone) }}</div>
              <div class="zone-detail">
                时长: {{ formatDuration(zone.durationMinutes) }} · 航速: {{ zone.avgSpeed }}节
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, shallowRef } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { vesselApi, trackApi, weatherApi } from './api'
import FishingMap from './components/FishingMap.vue'

const mapRef = ref(null)
const vesselList = ref([])
const selectedVesselId = ref(null)
const timeRange = ref([])
const trackPoints = shallowRef([])
const loadingTrack = ref(false)

const isPlaying = ref(false)
const uiIndex = ref(0)
const playSpeed = ref(1)

const showWeather = ref(true)
const weatherType = ref('windSpeed')
const displayWeatherGrids = shallowRef([])

const fishingZones = shallowRef([])
const analyzingZones = ref(false)

const totalFishingHours = computed(() => {
  if (!fishingZones.value.length) return '0'
  const totalMin = fishingZones.value.reduce((sum, z) => sum + (z.durationMinutes || 0), 0)
  return (totalMin / 60).toFixed(1)
})

const avgSpeed = computed(() => {
  if (!fishingZones.value.length) return null
  const sum = fishingZones.value.reduce((s, z) => s + parseFloat(z.avgSpeed || 0), 0)
  return (sum / fishingZones.value.length).toFixed(1)
})

const weatherCache = new Map()
const weatherTimes = []
let weatherPrefetchPromise = null

const trackBounds = { minLon: 0, maxLon: 0, minLat: 0, maxLat: 0 }
const rawTrackTimes = []

let rafId = null
let lastFrameTs = 0
let virtualFloatIndex = 0
let accumulator = 0

const UI_UPDATE_INTERVAL_MS = 120
let lastUiSyncTs = 0
const WEATHER_SYNC_INTERVAL_MS = 800
let lastWeatherSyncTs = 0
let lastAppliedWeatherIdx = -1

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
      color: getWeatherColorHex(val, config.min, config.max)
    })
  }
  return {
    title: config.title,
    min: config.min,
    max: config.max,
    colors
  }
})

function getWeatherColorHex(value, min, max) {
  const ratio = Math.max(0, Math.min(1, (value - min) / (max - min)))
  if (ratio < 0.2) return '#313695'
  if (ratio < 0.4) return '#4575b4'
  if (ratio < 0.6) return '#74add1'
  if (ratio < 0.8) return '#fee090'
  if (ratio < 0.9) return '#f46d43'
  return '#d73027'
}

const uiPoint = computed(() => trackPoints.value[uiIndex.value] || null)
const startLabel = computed(() => trackPoints.value[0]?.trackTime?.slice(5, 16) || '--')
const endLabel = computed(() => trackPoints.value[trackPoints.value.length - 1]?.trackTime?.slice(5, 16) || '--')
const currentLabel = computed(() => uiPoint.value?.trackTime?.slice(5, 16) || '--')

const weatherStatusText = computed(() => {
  if (!showWeather.value) return '已关闭'
  if (!weatherTimes.length) return '无数据'
  const ready = weatherCache.size
  const total = weatherTimes.length
  if (ready === 0) return '加载中...'
  if (ready < total) return `预取 ${ready}/${total}`
  return `就绪 (${ready})`
})
const weatherStatusClass = computed(() => {
  if (!showWeather.value) return 'status-off'
  const ready = weatherCache.size
  const total = weatherTimes.length
  if (ready === 0) return 'status-loading'
  if (ready < total) return 'status-partial'
  return 'status-ready'
})

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

function parseDate(s) {
  if (!s) return 0
  const t = Date.parse(s.replace(' ', 'T'))
  return isNaN(t) ? 0 : t
}

function formatZoneTime(zone) {
  if (!zone || !zone.startTime) return ''
  const st = String(zone.startTime).slice(5, 16)
  const et = String(zone.endTime).slice(5, 16)
  return `${st} → ${et}`
}

function formatDuration(minutes) {
  if (!minutes) return '0分钟'
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  if (h > 0 && m > 0) return `${h}小时${m}分`
  if (h > 0) return `${h}小时`
  return `${m}分钟`
}

function onVesselChange() {
  queryTrack()
}

async function queryTrack() {
  if (!selectedVesselId.value || !timeRange.value || timeRange.value.length < 2) return

  stopPlayback()
  loadingTrack.value = true
  clearWeatherCache()
  displayWeatherGrids.value = []
  fishingZones.value = []
  analyzingZones.value = false
  uiIndex.value = 0
  virtualFloatIndex = 0
  accumulator = 0

  try {
    const tracks = await trackApi.query({
      vesselId: selectedVesselId.value,
      startTime: timeRange.value[0],
      endTime: timeRange.value[1]
    })

    trackPoints.value = tracks || []

    if (trackPoints.value.length > 0) {
      precomputeTrackMeta()
      scheduleWeatherPrefetch()
      applyWeatherForIndex(0, true)
      analyzeFishingZones()
    }
  } catch (e) {
    console.error('查询轨迹失败', e)
    ElMessage.error('查询轨迹失败')
  } finally {
    loadingTrack.value = false
  }
}

async function analyzeFishingZones() {
  if (!selectedVesselId.value || !timeRange.value || timeRange.value.length < 2) return
  analyzingZones.value = true
  fishingZones.value = []
  try {
    const zones = await trackApi.analyzeZones({
      vesselId: selectedVesselId.value,
      startTime: timeRange.value[0],
      endTime: timeRange.value[1]
    })
    fishingZones.value = zones || []
    if (zones && zones.length > 0) {
      ElMessage.success(`识别到 ${zones.length} 个捕鱼作业区`)
    }
  } catch (e) {
    console.error('作业区分析失败', e)
  } finally {
    analyzingZones.value = false
  }
}

function precomputeTrackMeta() {
  const pts = trackPoints.value
  const lons = new Array(pts.length)
  const lats = new Array(pts.length)
  const times = new Array(pts.length)
  for (let i = 0; i < pts.length; i++) {
    lons[i] = parseFloat(pts[i].longitude)
    lats[i] = parseFloat(pts[i].latitude)
    times[i] = parseDate(pts[i].trackTime)
  }
  trackBounds.minLon = Math.min(...lons) - 2
  trackBounds.maxLon = Math.max(...lons) + 2
  trackBounds.minLat = Math.min(...lats) - 2
  trackBounds.maxLat = Math.max(...lats) + 2
  rawTrackTimes.length = 0
  for (let i = 0; i < times.length; i++) rawTrackTimes.push(times[i])
}

function clearWeatherCache() {
  weatherCache.clear()
  weatherTimes.length = 0
  weatherPrefetchPromise = null
  lastAppliedWeatherIdx = -1
}

function scheduleWeatherPrefetch() {
  if (!showWeather.value) return
  weatherPrefetchPromise = (async () => {
    try {
      const pts = trackPoints.value
      if (pts.length < 2) return

      const t0 = rawTrackTimes[0]
      const tN = rawTrackTimes[rawTrackTimes.length - 1]
      const stepMs = 3 * 3600 * 1000
      const bucketTimes = []
      let aligned = Math.floor(t0 / stepMs) * stepMs
      while (aligned <= tN + stepMs) {
        bucketTimes.push(aligned)
        aligned += stepMs
      }

      weatherTimes.length = 0
      for (let i = 0; i < bucketTimes.length; i++) weatherTimes.push(bucketTimes[i])

      for (let i = 0; i < bucketTimes.length; i++) {
        if (isPlaying.value === false && i > 0) break
        const key = bucketTimes[i]
        if (weatherCache.has(key)) continue

        const d = new Date(key)
        const pad = (n) => n.toString().padStart(2, '0')
        const timeStr = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`

        try {
          const data = await weatherApi.getGrid({
            gridTime: timeStr,
            minLon: trackBounds.minLon,
            maxLon: trackBounds.maxLon,
            minLat: trackBounds.minLat,
            maxLat: trackBounds.maxLat
          })
          if (data && data.length) {
            weatherCache.set(key, data)
          }
        } catch (e) {
          // ignore
        }
      }
    } catch (e) {
      console.warn('气象预取异常', e)
    }
  })()
}

function findWeatherBucketIndex(trackIdx) {
  const trackTs = rawTrackTimes[trackIdx] || 0
  if (weatherTimes.length === 0 || !trackTs) return -1

  const stepMs = 3 * 3600 * 1000
  const bucketStart = Math.floor(trackTs / stepMs) * stepMs
  for (let i = 0; i < weatherTimes.length; i++) {
    if (weatherTimes[i] === bucketStart) return i
    if (weatherTimes[i] > bucketStart) return i > 0 ? i - 1 : 0
  }
  return weatherTimes.length - 1
}

function applyWeatherForIndex(trackIdx, force = false) {
  if (!showWeather.value) return
  const bucketIdx = findWeatherBucketIndex(trackIdx)
  if (bucketIdx < 0) return
  if (!force && bucketIdx === lastAppliedWeatherIdx) return

  const key = weatherTimes[bucketIdx]
  const cached = key != null ? weatherCache.get(key) : null
  if (cached) {
    displayWeatherGrids.value = cached
    lastAppliedWeatherIdx = bucketIdx
  } else if (!weatherPrefetchPromise && force) {
    scheduleWeatherPrefetch()
  }
}

function togglePlayback() {
  if (isPlaying.value) stopPlayback()
  else startPlayback()
}

function startPlayback() {
  if (trackPoints.value.length < 2) return
  if (uiIndex.value >= trackPoints.value.length - 1) {
    uiIndex.value = 0
    virtualFloatIndex = 0
    accumulator = 0
  }

  isPlaying.value = true
  lastFrameTs = 0
  lastUiSyncTs = performance.now()
  lastWeatherSyncTs = performance.now()
  rafId = requestAnimationFrame(playLoop)

  scheduleWeatherPrefetch()
}

function stopPlayback() {
  isPlaying.value = false
  if (rafId != null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
  lastFrameTs = 0
}

function playLoop(ts) {
  if (!isPlaying.value) return

  if (!lastFrameTs) lastFrameTs = ts
  const deltaMs = Math.min(100, ts - lastFrameTs)
  lastFrameTs = ts

  const pts = trackPoints.value
  const count = pts.length
  if (count < 2) return

  const baseDurationMs = (rawTrackTimes[count - 1] - rawTrackTimes[0]) || (count * 30 * 60 * 1000)
  const targetPlaybackMs = baseDurationMs / playSpeed.value
  const idxPerMs = (count - 1) / targetPlaybackMs

  accumulator += deltaMs
  const stepFloat = idxPerMs * accumulator
  accumulator = 0

  virtualFloatIndex = Math.min(count - 1, virtualFloatIndex + stepFloat)
  const intIndex = Math.min(count - 1, Math.floor(virtualFloatIndex))

  const map = mapRef.value
  if (map && typeof map.setProgressByIndex === 'function') {
    const ratio = virtualFloatIndex / (count - 1)
    map.setProgressByRatio(ratio, intIndex)
  }

  const now = ts
  if (now - lastUiSyncTs >= UI_UPDATE_INTERVAL_MS) {
    uiIndex.value = intIndex
    lastUiSyncTs = now
  }

  if (now - lastWeatherSyncTs >= WEATHER_SYNC_INTERVAL_MS) {
    applyWeatherForIndex(intIndex)
    lastWeatherSyncTs = now
  }

  if (virtualFloatIndex >= count - 1) {
    uiIndex.value = count - 1
    applyWeatherForIndex(count - 1)
    stopPlayback()
    return
  }

  rafId = requestAnimationFrame(playLoop)
}

function resetPlayback() {
  stopPlayback()
  uiIndex.value = 0
  virtualFloatIndex = 0
  accumulator = 0
  const map = mapRef.value
  if (map && typeof map.setProgressByIndex === 'function') {
    map.setProgressByIndex(0)
  }
  applyWeatherForIndex(0, true)
}

function stepForward() {
  const next = Math.min(trackPoints.value.length - 1, uiIndex.value + Math.max(1, Math.floor(trackPoints.value.length / 200)))
  uiIndex.value = next
  virtualFloatIndex = next
  const map = mapRef.value
  if (map && typeof map.setProgressByIndex === 'function') {
    map.setProgressByIndex(next)
  }
  applyWeatherForIndex(next, true)
}

function onSliderInput(val) {
  const v = Number(val) || 0
  uiIndex.value = v
  virtualFloatIndex = v
  const map = mapRef.value
  if (map && typeof map.setProgressByIndex === 'function') {
    map.setProgressByIndex(v)
  }
}

function onSliderChange(val) {
  const v = Number(val) || 0
  applyWeatherForIndex(v, true)
}

watch(playSpeed, () => {
  // 无需重启，下一帧自动用新速度
})

watch(weatherType, () => {
  if (trackPoints.value.length > 0) {
    applyWeatherForIndex(uiIndex.value, true)
  }
})

watch(showWeather, (val) => {
  if (val && trackPoints.value.length > 0) {
    scheduleWeatherPrefetch()
    applyWeatherForIndex(uiIndex.value, true)
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
  padding: 14px 16px;
  background: linear-gradient(135deg, #1e88e5, #1565c0);
  color: #fff;
}

.sidebar-header h2 {
  font-size: 16px;
  margin: 0;
  font-weight: 600;
}

.sub-title {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.panel {
  margin-bottom: 14px;
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
  max-width: 55%;
  text-align: right;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.play-controls {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.speed-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.speed-label {
  font-size: 12px;
  color: #666;
}

.speed-select {
  width: 100px;
}

.slider-block {
  margin-top: 10px;
}

.time-row {
  display: flex;
  justify-content: space-between;
  gap: 4px;
  font-size: 11px;
  color: #666;
  margin-top: 6px;
}

.time-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.time-label:first-child {
  text-align: left;
}
.time-label:last-child {
  text-align: right;
}
.time-current {
  text-align: center;
  color: #1976d2;
  font-weight: 600;
  flex: 1.2;
}

.switch-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}

.weather-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}
.status-off { color: #999; background: #f0f0f0; }
.status-loading { color: #e6a23c; background: #faecd8; }
.status-partial { color: #909399; background: #f4f4f5; }
.status-ready { color: #67c23a; background: #f0f9eb; }

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

.zone-stats-panel {
  position: absolute;
  left: 12px;
  bottom: 12px;
  z-index: 5;
  min-width: 240px;
  max-width: 320px;
  background: rgba(20, 20, 28, 0.92);
  backdrop-filter: blur(6px);
  border: 1px solid rgba(255, 193, 7, 0.3);
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.4);
  overflow: hidden;
}

.zone-stats-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: linear-gradient(90deg, rgba(255, 193, 7, 0.15), transparent);
  border-bottom: 1px solid rgba(255, 193, 7, 0.2);
}

.zone-stats-title {
  font-size: 13px;
  font-weight: 600;
  color: #ffc107;
}

.zone-stats-body {
  padding: 10px 12px;
}

.analyzing-text {
  font-size: 12px;
  color: #aaa;
  text-align: center;
  padding: 8px 0;
}

.zone-stat-row {
  display: flex;
  align-items: baseline;
  padding: 3px 0;
  font-size: 12px;
}

.zone-stat-row .stat-label {
  color: #888;
  flex: 1;
}

.zone-stat-row .stat-value {
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  margin-right: 4px;
}

.zone-stat-row .stat-value.highlight {
  color: #ffc107;
  font-size: 18px;
}

.zone-stat-row .stat-unit {
  color: #888;
  font-size: 11px;
}

.zone-list {
  max-height: 160px;
  overflow-y: auto;
  border-top: 1px solid rgba(255, 193, 7, 0.15);
}

.zone-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.15s;
}

.zone-item:hover {
  background: rgba(255, 193, 7, 0.08);
}

.zone-item:last-child {
  border-bottom: none;
}

.zone-badge {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffc107, #ff8f00);
  color: #1a1a1a;
  font-size: 11px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.zone-info {
  flex: 1;
  min-width: 0;
}

.zone-time {
  font-size: 12px;
  color: #ffc107;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.zone-detail {
  font-size: 11px;
  color: #999;
  margin-top: 2px;
}
</style>
