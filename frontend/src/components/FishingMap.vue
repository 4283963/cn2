<template>
  <div ref="mapContainer" class="map-wrapper"></div>
</template>

<script setup>
import { ref, shallowRef, onMounted, onUnmounted, watch, nextTick } from 'vue'
import mapboxgl from 'mapbox-gl'
import { adaptiveSimplify, getWeatherColor } from '../utils/geo'

const props = defineProps({
  trackPoints: {
    type: Array,
    default: () => []
  },
  displayIndex: {
    type: Number,
    default: 0
  },
  weatherGrids: {
    type: Array,
    default: () => []
  },
  showWeather: {
    type: Boolean,
    default: true
  },
  weatherType: {
    type: String,
    default: 'windSpeed'
  },
  fishingZones: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['progress'])

const mapContainer = ref(null)
const map = shallowRef(null)
const mapReady = ref(false)

const rawPoints = shallowRef([])
const simplifiedCoords = shallowRef([])
const cumulativeLen = shallowRef([])
const totalLen = ref(0)

let vesselMarker = null
let vesselPopup = null
const lastPopupIndex = { value: -1 }
const lastProgressPct = { value: -1 }
const lastWeatherType = { value: null }
const lastWeatherGridsKey = { value: '' }

const weatherConfig = {
  windSpeed: { min: 0, max: 25 },
  pressure: { min: 980, max: 1030 },
  waveHeight: { min: 0, max: 8 }
}

defineExpose({
  setProgressByIndex(index) {
    if (!mapReady.value || rawPoints.value.length < 2) return
    const i = Math.max(0, Math.min(rawPoints.value.length - 1, index))
    const cumLen = cumulativeLen.value
    const ratio = cumLen.length ? cumLen[i] / totalLen.value : 0
    applyProgressRatio(ratio, i)
  },
  setProgressByRatio(ratio, snapIndex = null) {
    if (!mapReady.value || rawPoints.value.length < 2) return
    const r = Math.max(0, Math.min(1, ratio))
    let idx = snapIndex
    if (idx == null) {
      const target = r * totalLen.value
      const arr = cumulativeLen.value
      let lo = 0, hi = arr.length - 1
      while (lo < hi) {
        const mid = (lo + hi) >> 1
        if (arr[mid] < target) lo = mid + 1
        else hi = mid
      }
      idx = lo
    }
    applyProgressRatio(r, idx)
  },
  setWeatherGridsImmediate(grids, type) {
    if (!mapReady.value) return
    renderWeatherGrid(grids, type)
  },
  setFishingZones(zones) {
    if (!mapReady.value) return
    drawFishingZones(zones)
  },
  fitToTrack() {
    doFitToTrack()
  },
  getMapInstance() {
    return map.value
  }
})

function applyProgressRatio(ratio, pointIndex) {
  const m = map.value
  if (!m) return

  const pct = Number(ratio.toFixed(5))
  if (pct !== lastProgressPct.value) {
    const trim = [0, 1 - pct]
    if (m.getLayer('track-progress-line')) {
      m.setPaintProperty('track-progress-line', 'line-trim-offset', trim)
    }
    lastProgressPct.value = pct
  }

  if (pointIndex != null && rawPoints.value[pointIndex]) {
    updateVesselMarker(pointIndex)
  }
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map.value) {
    map.value.remove()
    map.value = null
  }
})

function initMap() {
  mapboxgl.accessToken = 'pk.eyJ1IjoiZXhhbXBsZXVzZXIiLCJhIjoiY2w5eWV4b2FqMDJ0ZjNubnlydGg0MXgydCJ9.7GxqT8fK5exampleToken'

  const m = new mapboxgl.Map({
    container: mapContainer.value,
    style: 'mapbox://styles/mapbox/dark-v11',
    center: [122.0, 30.0],
    zoom: 4,
    attributionControl: false,
    preserveDrawingBuffer: false
  })

  map.value = m

  m.addControl(new mapboxgl.NavigationControl({ showCompass: false }), 'top-right')
  m.addControl(new mapboxgl.ScaleControl({ unit: 'nautical' }), 'bottom-left')

  m.on('load', () => {
    initLayers()
    mapReady.value = true
    if (props.trackPoints.length > 0) {
      processAndDrawTrack(props.trackPoints)
      doFitToTrack()
      setProgressByIndex(props.displayIndex)
    }
    if (props.showWeather && props.weatherGrids.length > 0) {
      renderWeatherGrid(props.weatherGrids, props.weatherType)
    }
    if (props.fishingZones && props.fishingZones.length > 0) {
      drawFishingZones(props.fishingZones)
    }
  })
}

function initLayers() {
  const m = map.value
  if (!m || m.getSource('track-source')) return

  m.addSource('track-source', {
    type: 'geojson',
    data: { type: 'FeatureCollection', features: [] }
  })
  m.addLayer({
    id: 'track-line',
    type: 'line',
    source: 'track-source',
    layout: { 'line-join': 'round', 'line-cap': 'round' },
    paint: {
      'line-color': '#4fc3f7',
      'line-width': 3,
      'line-opacity': 0.7
    }
  })

  m.addSource('track-progress-source', {
    type: 'geojson',
    data: { type: 'FeatureCollection', features: [] }
  })
  m.addLayer({
    id: 'track-progress-line',
    type: 'line',
    source: 'track-progress-source',
    layout: { 'line-join': 'round', 'line-cap': 'round' },
    paint: {
      'line-color': '#ff9800',
      'line-width': 4,
      'line-opacity': 0.95,
      'line-trim-offset': [0, 1]
    }
  })

  m.addSource('weather-source', {
    type: 'geojson',
    data: { type: 'FeatureCollection', features: [] }
  })
  m.addLayer({
    id: 'weather-polygons',
    type: 'fill',
    source: 'weather-source',
    paint: {
      'fill-color': ['get', 'color'],
      'fill-opacity': 0.55
    }
  }, 'track-line')

  m.addSource('fishing-zones-source', {
    type: 'geojson',
    data: { type: 'FeatureCollection', features: [] }
  })
  m.addLayer({
    id: 'fishing-zones-line',
    type: 'line',
    source: 'fishing-zones-source',
    layout: {
      'line-join': 'round',
      'line-cap': 'round'
    },
    paint: {
      'line-color': '#ffc107',
      'line-width': 8,
      'line-opacity': 0.85
    }
  }, 'track-progress-line')

  m.addLayer({
    id: 'fishing-zones-outline',
    type: 'line',
    source: 'fishing-zones-source',
    layout: {
      'line-join': 'round',
      'line-cap': 'round'
    },
    paint: {
      'line-color': '#ff8f00',
      'line-width': 2,
      'line-opacity': 1,
      'line-dasharray': [1, 0]
    }
  }, 'fishing-zones-line')

  lastProgressPct.value = -1
}

function processAndDrawTrack(points) {
  rawPoints.value = points

  const getX = p => parseFloat(p.longitude)
  const getY = p => parseFloat(p.latitude)

  const simplified = adaptiveSimplify(points, getX, getY, 0.3, 0.0008, 0.08)
  const coords = simplified.map(p => [getX(p), getY(p)])
  simplifiedCoords.value = coords

  const fullCoords = points.map(p => [getX(p), getY(p)])
  const cum = new Float64Array(points.length)
  let sum = 0
  cum[0] = 0
  for (let i = 1; i < points.length; i++) {
    const dx = fullCoords[i][0] - fullCoords[i - 1][0]
    const dy = fullCoords[i][1] - fullCoords[i - 1][1]
    sum += Math.sqrt(dx * dx + dy * dy)
    cum[i] = sum
  }
  cumulativeLen.value = cum
  totalLen.value = sum || 1

  const m = map.value
  if (!m) return

  m.getSource('track-source').setData({
    type: 'FeatureCollection',
    features: [{
      type: 'Feature',
      geometry: { type: 'LineString', coordinates: coords },
      properties: {}
    }]
  })

  const allCoords = points.length < 800 ? fullCoords : coords
  m.getSource('track-progress-source').setData({
    type: 'FeatureCollection',
    features: [{
      type: 'Feature',
      geometry: { type: 'LineString', coordinates: allCoords },
      properties: {}
    }]
  })

  lastProgressPct.value = -1
}

function updateVesselMarker(index) {
  const m = map.value
  if (!m || !rawPoints.value[index]) return

  const point = rawPoints.value[index]
  const lng = parseFloat(point.longitude)
  const lat = parseFloat(point.latitude)
  const coord = [lng, lat]

  if (!vesselMarker) {
    const el = document.createElement('div')
    el.style.cssText = `
      width: 18px; height: 18px;
      background: #ff5722;
      border: 3px solid #fff;
      border-radius: 50%;
      box-shadow: 0 2px 8px rgba(0,0,0,0.4);
    `
    vesselMarker = new mapboxgl.Marker({ element: el, anchor: 'center' }).addTo(m)
  }

  vesselMarker.setLngLat(coord)

  if (index !== lastPopupIndex.value) {
    if (!vesselPopup) {
      vesselPopup = new mapboxgl.Popup({ offset: 18, closeButton: false, closeOnClick: false })
      vesselMarker.setPopup(vesselPopup)
    }
    vesselPopup.setHTML(`
      <div style="font-size:12px;line-height:1.6;min-width:160px">
        <div style="font-weight:bold;color:#1976d2;margin-bottom:4px">渔船位置</div>
        <div>时间: ${point.trackTime}</div>
        <div>经纬度: ${point.longitude}, ${point.latitude}</div>
        <div>航速: ${point.speed} 节 / 航向: ${point.heading}°</div>
      </div>
    `)
    lastPopupIndex.value = index
  }
}

let weatherRenderLock = false
let weatherRenderQueued = null

function renderWeatherGrid(grids, type) {
  if (!mapReady.value || !grids || !grids.length) return
  const key = grids.length + '_' + (grids[0]?.id || '') + '_' + (grids[grids.length - 1]?.id || '')

  if (key === lastWeatherGridsKey.value && type === lastWeatherType.value) return

  weatherRenderQueued = { grids, type, key }
  if (weatherRenderLock) return

  runWeatherRender()
}

function runWeatherRender() {
  if (!weatherRenderQueued) return
  weatherRenderLock = true

  const { grids, type, key } = weatherRenderQueued
  weatherRenderQueued = null

  requestAnimationFrame(() => {
    try {
      doRenderWeatherGrid(grids, type)
      lastWeatherGridsKey.value = key
      lastWeatherType.value = type
    } finally {
      weatherRenderLock = false
      if (weatherRenderQueued) runWeatherRender()
    }
  })
}

function doRenderWeatherGrid(grids, type) {
  const m = map.value
  if (!m) return

  const config = weatherConfig[type] || weatherConfig.windSpeed
  const features = []

  const lonSet = new Set()
  const latSet = new Set()
  const gridMap = new Map()

  for (let i = 0; i < grids.length; i++) {
    const g = grids[i]
    const lon = parseFloat(g.longitude)
    const lat = parseFloat(g.latitude)
    lonSet.add(lon)
    latSet.add(lat)
    gridMap.set(lon + '_' + lat, g)
  }

  const uniqueLons = Array.from(lonSet).sort((a, b) => a - b)
  const uniqueLats = Array.from(latSet).sort((a, b) => a - b)

  if (uniqueLons.length < 2 || uniqueLats.length < 2) return

  const lonStep = uniqueLons[1] - uniqueLons[0]
  const latStep = uniqueLats[1] - uniqueLats[0]
  const halfLon = lonStep / 2
  const halfLat = latStep / 2

  const getVal = (g) => g ? (parseFloat(g[type]) || 0) : NaN

  for (let i = 0; i < uniqueLons.length - 1; i++) {
    const lon0 = uniqueLons[i]
    const lon1 = uniqueLons[i + 1]
    for (let j = 0; j < uniqueLats.length - 1; j++) {
      const lat0 = uniqueLats[j]
      const lat1 = uniqueLats[j + 1]

      const c00 = gridMap.get(lon0 + '_' + lat0)
      const c10 = gridMap.get(lon1 + '_' + lat0)
      const c01 = gridMap.get(lon0 + '_' + lat1)
      const c11 = gridMap.get(lon1 + '_' + lat1)

      let sum = 0, n = 0
      const v00 = getVal(c00); if (!isNaN(v00)) { sum += v00; n++ }
      const v10 = getVal(c10); if (!isNaN(v10)) { sum += v10; n++ }
      const v01 = getVal(c01); if (!isNaN(v01)) { sum += v01; n++ }
      const v11 = getVal(c11); if (!isNaN(v11)) { sum += v11; n++ }
      if (n === 0) continue

      const avg = sum / n
      const color = getWeatherColor(avg, config.min, config.max)
      const x0 = lon0 - halfLon, x1 = lon1 + halfLon
      const y0 = lat0 - halfLat, y1 = lat1 + halfLat
      features.push({
        type: 'Feature',
        geometry: {
          type: 'Polygon',
          coordinates: [[[x0, y0], [x1, y0], [x1, y1], [x0, y1], [x0, y0]]]
        },
        properties: { color }
      })
    }
  }

  const src = m.getSource('weather-source')
  if (src) {
    src.setData({ type: 'FeatureCollection', features })
  }

  if (m.getLayer('weather-polygons')) {
    m.setLayoutProperty('weather-polygons', 'visibility', props.showWeather ? 'visible' : 'none')
  }
}

function drawFishingZones(zones) {
  const m = map.value
  if (!m || !zones || !zones.length) {
    if (m && m.getSource('fishing-zones-source')) {
      m.getSource('fishing-zones-source').setData({ type: 'FeatureCollection', features: [] })
    }
    return
  }

  const allPoints = rawPoints.value
  if (!allPoints.length) return

  const features = []

  for (let zi = 0; zi < zones.length; zi++) {
    const zone = zones[zi]
    let startIdx = zone.startIndex != null ? Number(zone.startIndex) : -1
    let endIdx = zone.endIndex != null ? Number(zone.endIndex) : -1

    if (startIdx < 0 || endIdx < 0 || startIdx >= allPoints.length || endIdx >= allPoints.length || startIdx >= endIdx) {
      const startLon = parseFloat(zone.startLongitude)
      const startLat = parseFloat(zone.startLatitude)
      const endLon = parseFloat(zone.endLongitude)
      const endLat = parseFloat(zone.endLatitude)
      let minStartDist = Infinity, minEndDist = Infinity
      for (let i = 0; i < allPoints.length; i++) {
        const p = allPoints[i]
        const lon = parseFloat(p.longitude)
        const lat = parseFloat(p.latitude)
        const dStart = Math.hypot(lon - startLon, lat - startLat)
        const dEnd = Math.hypot(lon - endLon, lat - endLat)
        if (dStart < minStartDist) { minStartDist = dStart; startIdx = i }
        if (dEnd < minEndDist) { minEndDist = dEnd; endIdx = i }
      }
      if (startIdx > endIdx) { const t = startIdx; startIdx = endIdx; endIdx = t }
    }

    if (startIdx < 0 || endIdx < 0 || endIdx - startIdx < 2) continue

    const coords = []
    for (let i = startIdx; i <= endIdx; i++) {
      coords.push([parseFloat(allPoints[i].longitude), parseFloat(allPoints[i].latitude)])
    }

    const dur = zone.durationMinutes || 0
    const hours = Math.floor(dur / 60)
    const mins = dur % 60
    const desc = `作业区 #${zi + 1}\n${hours ? hours + '小时' : ''}${mins}分钟\n平均航速 ${zone.avgSpeed} 节`

    features.push({
      type: 'Feature',
      geometry: { type: 'LineString', coordinates: coords },
      properties: {
        id: zone.id,
        description: desc,
        duration: dur,
        avgSpeed: zone.avgSpeed,
        zoneIndex: zi + 1
      }
    })
  }

  const src = m.getSource('fishing-zones-source')
  if (src) {
    src.setData({ type: 'FeatureCollection', features })
  }
}

function doFitToTrack() {
  const m = map.value
  if (!m || !simplifiedCoords.value.length) return
  const coords = simplifiedCoords.value
  if (coords.length < 2) return
  const bounds = coords.reduce(
    (b, c) => b.extend(c),
    new mapboxgl.LngLatBounds(coords[0], coords[0])
  )
  m.fitBounds(bounds, { padding: 80, duration: 800, linear: false })
}

watch(() => props.trackPoints, (newVal) => {
  if (!mapReady.value) return
  nextTick(() => {
    processAndDrawTrack(newVal || [])
    doFitToTrack()
    if (newVal && newVal.length) {
      setProgressByIndex(Math.min(props.displayIndex, newVal.length - 1))
    }
  })
})

watch(() => props.displayIndex, (idx) => {
  if (!mapReady.value) return
  setProgressByIndex(idx)
})

watch(() => props.weatherGrids, (val) => {
  if (!mapReady.value) return
  renderWeatherGrid(val, props.weatherType)
})

watch(() => props.weatherType, (type) => {
  if (!mapReady.value) return
  renderWeatherGrid(props.weatherGrids, type)
})

watch(() => props.showWeather, (val) => {
  const m = map.value
  if (!m || !mapReady.value || !m.getLayer('weather-polygons')) return
  m.setLayoutProperty('weather-polygons', 'visibility', val ? 'visible' : 'none')
})

watch(() => props.fishingZones, (val) => {
  if (!mapReady.value) return
  drawFishingZones(val || [])
})
</script>

<style scoped>
.map-wrapper {
  width: 100%;
  height: 100%;
}

:deep(.mapboxgl-popup-content) {
  padding: 10px 12px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
