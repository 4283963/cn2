<template>
  <div ref="mapContainer" class="map-wrapper"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch, nextTick } from 'vue'
import mapboxgl from 'mapbox-gl'

const props = defineProps({
  trackPoints: {
    type: Array,
    default: () => []
  },
  currentIndex: {
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
  weatherLegend: {
    type: Object,
    default: () => ({})
  }
})

const mapContainer = ref(null)
let map = null
let vesselMarker = null
let bearingMarker = null

const weatherConfig = {
  windSpeed: { min: 0, max: 25 },
  pressure: { min: 980, max: 1030 },
  waveHeight: { min: 0, max: 8 }
}

function getWeatherColor(value, min, max) {
  if (value == null) return 'rgba(0,0,0,0)'
  const ratio = Math.max(0, Math.min(1, (value - min) / (max - min)))
  let r, g, b
  if (ratio < 0.2) {
    const t = ratio / 0.2
    r = Math.round(49 + (69 - 49) * t)
    g = Math.round(54 + (117 - 54) * t)
    b = Math.round(149 + (180 - 149) * t)
  } else if (ratio < 0.4) {
    const t = (ratio - 0.2) / 0.2
    r = Math.round(69 + (116 - 69) * t)
    g = Math.round(117 + (173 - 117) * t)
    b = Math.round(180 + (209 - 180) * t)
  } else if (ratio < 0.6) {
    const t = (ratio - 0.4) / 0.2
    r = Math.round(116 + (254 - 116) * t)
    g = Math.round(173 + (224 - 173) * t)
    b = Math.round(209 + (144 - 209) * t)
  } else if (ratio < 0.8) {
    const t = (ratio - 0.6) / 0.2
    r = Math.round(254 + (244 - 254) * t)
    g = Math.round(224 + (109 - 224) * t)
    b = Math.round(144 + (67 - 144) * t)
  } else {
    const t = (ratio - 0.8) / 0.2
    r = Math.round(244 + (215 - 244) * t)
    g = Math.round(109 + (48 - 109) * t)
    b = Math.round(67 + (39 - 67) * t)
  }
  return `rgba(${r}, ${g}, ${b}, 0.55)`
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  if (map) {
    map.remove()
    map = null
  }
})

function initMap() {
  mapboxgl.accessToken = 'pk.eyJ1IjoiZXhhbXBsZXVzZXIiLCJhIjoiY2w5eWV4b2FqMDJ0ZjNubnlydGg0MXgydCJ9.7GxqT8fK5exampleToken'

  map = new mapboxgl.Map({
    container: mapContainer.value,
    style: 'mapbox://styles/mapbox/dark-v11',
    center: [122.0, 30.0],
    zoom: 4,
    attributionControl: false
  })

  map.addControl(new mapboxgl.NavigationControl(), 'top-right')
  map.addControl(new mapboxgl.ScaleControl({ unit: 'nautical' }), 'bottom-left')

  map.on('load', () => {
    initLayers()
    if (props.trackPoints.length > 0) {
      drawTrack()
      fitToTrack()
      updateVesselMarker()
    }
    if (props.showWeather && props.weatherGrids.length > 0) {
      drawWeatherGrid()
    }
  })
}

function initLayers() {
  if (!map.getSource('track-source')) {
    map.addSource('track-source', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: []
      }
    })

    map.addLayer({
      id: 'track-line',
      type: 'line',
      source: 'track-source',
      layout: {
        'line-join': 'round',
        'line-cap': 'round'
      },
      paint: {
        'line-color': '#4fc3f7',
        'line-width': 3,
        'line-opacity': 0.8
      }
    })

    map.addSource('track-progress-source', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: []
      }
    })

    map.addLayer({
      id: 'track-progress-line',
      type: 'line',
      source: 'track-progress-source',
      layout: {
        'line-join': 'round',
        'line-cap': 'round'
      },
      paint: {
        'line-color': '#ff9800',
        'line-width': 4,
        'line-opacity': 0.9
      }
    })

    map.addSource('weather-source', {
      type: 'geojson',
      data: {
        type: 'FeatureCollection',
        features: []
      }
    })

    map.addLayer({
      id: 'weather-polygons',
      type: 'fill',
      source: 'weather-source',
      paint: {
        'fill-color': ['get', 'color'],
        'fill-opacity': 0.55
      }
    }, 'track-line')
  }
}

function drawTrack() {
  if (!map || !map.loaded()) return

  const coordinates = props.trackPoints.map(p => [
    parseFloat(p.longitude),
    parseFloat(p.latitude)
  ])

  if (coordinates.length < 2) return

  const fullGeoJson = {
    type: 'FeatureCollection',
    features: [{
      type: 'Feature',
      geometry: {
        type: 'LineString',
        coordinates
      },
      properties: {}
    }]
  }

  if (map.getSource('track-source')) {
    map.getSource('track-source').setData(fullGeoJson)
  }

  updateProgressLine()
}

function updateProgressLine() {
  if (!map || !map.loaded() || props.trackPoints.length === 0) return

  const progressCoordinates = props.trackPoints
    .slice(0, props.currentIndex + 1)
    .map(p => [parseFloat(p.longitude), parseFloat(p.latitude)])

  if (progressCoordinates.length < 2) {
    progressCoordinates.push([...progressCoordinates[0]])
  }

  const progressGeoJson = {
    type: 'FeatureCollection',
    features: [{
      type: 'Feature',
      geometry: {
        type: 'LineString',
        coordinates: progressCoordinates
      },
      properties: {}
    }]
  }

  if (map.getSource('track-progress-source')) {
    map.getSource('track-progress-source').setData(progressGeoJson)
  }
}

function updateVesselMarker() {
  if (!map || props.trackPoints.length === 0) return

  const point = props.trackPoints[props.currentIndex]
  if (!point) return

  const lng = parseFloat(point.longitude)
  const lat = parseFloat(point.latitude)

  if (!vesselMarker) {
    const el = document.createElement('div')
    el.className = 'vessel-marker'
    el.innerHTML = `
      <div style="
        width: 24px;
        height: 24px;
        background: #ff5722;
        border: 3px solid #fff;
        border-radius: 50%;
        box-shadow: 0 2px 8px rgba(0,0,0,0.4);
        position: relative;
      ">
        <div style="
          position: absolute;
          top: -6px;
          left: 50%;
          transform: translateX(-50%);
          width: 0;
          height: 0;
          border-left: 6px solid transparent;
          border-right: 6px solid transparent;
          border-bottom: 8px solid #ff5722;
        "></div>
      </div>
    `
    vesselMarker = new mapboxgl.Marker({ element: el, anchor: 'center' }).addTo(map)
  }

  vesselMarker.setLngLat([lng, lat])

  const popupHtml = `
    <div style="font-size: 12px; line-height: 1.6;">
      <div style="font-weight: bold; color: #1976d2; margin-bottom: 4px;">
        渔船位置
      </div>
      <div>时间: ${point.trackTime}</div>
      <div>经度: ${point.longitude}</div>
      <div>纬度: ${point.latitude}</div>
      <div>航速: ${point.speed} 节</div>
    </div>
  `
  vesselMarker.setPopup(new mapboxgl.Popup({ offset: 20 }).setHTML(popupHtml))
}

function drawWeatherGrid() {
  if (!map || !map.loaded() || props.weatherGrids.length === 0) return

  const config = weatherConfig[props.weatherType]
  const features = []

  const uniqueLons = [...new Set(props.weatherGrids.map(g => parseFloat(g.longitude)))].sort((a, b) => a - b)
  const uniqueLats = [...new Set(props.weatherGrids.map(g => parseFloat(g.latitude)))].sort((a, b) => a - b)

  if (uniqueLons.length < 2 || uniqueLats.length < 2) return

  const lonStep = uniqueLons.length > 1 ? (uniqueLons[1] - uniqueLons[0]) : 1
  const latStep = uniqueLats.length > 1 ? (uniqueLats[1] - uniqueLats[0]) : 1

  const gridMap = {}
  props.weatherGrids.forEach(g => {
    const key = `${parseFloat(g.longitude)}_${parseFloat(g.latitude)}`
    gridMap[key] = g
  })

  for (let i = 0; i < uniqueLons.length - 1; i++) {
    for (let j = 0; j < uniqueLats.length - 1; j++) {
      const lon0 = uniqueLons[i]
      const lon1 = uniqueLons[i + 1]
      const lat0 = uniqueLats[j]
      const lat1 = uniqueLats[j + 1]

      const key00 = `${lon0}_${lat0}`
      const key10 = `${lon1}_${lat0}`
      const key01 = `${lon0}_${lat1}`
      const key11 = `${lon1}_${lat1}`

      const cells = [gridMap[key00], gridMap[key10], gridMap[key01], gridMap[key11]].filter(Boolean)
      if (cells.length === 0) continue

      const avgValue = cells.reduce((sum, c) => sum + parseFloat(c[props.weatherType] || 0), 0) / cells.length
      const color = getWeatherColor(avgValue, config.min, config.max)

      features.push({
        type: 'Feature',
        geometry: {
          type: 'Polygon',
          coordinates: [[
            [lon0 - lonStep / 2, lat0 - latStep / 2],
            [lon1 + lonStep / 2, lat0 - latStep / 2],
            [lon1 + lonStep / 2, lat1 + latStep / 2],
            [lon0 - lonStep / 2, lat1 + latStep / 2],
            [lon0 - lonStep / 2, lat0 - latStep / 2]
          ]]
        },
        properties: {
          value: avgValue,
          color
        }
      })
    }
  }

  const geoJson = {
    type: 'FeatureCollection',
    features
  }

  if (map.getSource('weather-source')) {
    map.getSource('weather-source').setData(geoJson)
  }

  if (map.getLayer('weather-polygons')) {
    map.setLayoutProperty('weather-polygons', 'visibility', props.showWeather ? 'visible' : 'none')
  }
}

function fitToTrack() {
  if (!map || props.trackPoints.length === 0) return

  const coordinates = props.trackPoints.map(p => [
    parseFloat(p.longitude),
    parseFloat(p.latitude)
  ])

  const bounds = coordinates.reduce(
    (bounds, coord) => bounds.extend(coord),
    new mapboxgl.LngLatBounds(coordinates[0], coordinates[0])
  )

  map.fitBounds(bounds, {
    padding: 80,
    duration: 1000
  })
}

watch(() => props.trackPoints, (newVal) => {
  if (map && map.loaded() && newVal.length > 0) {
    nextTick(() => {
      drawTrack()
      fitToTrack()
      updateVesselMarker()
    })
  }
}, { deep: true })

watch(() => props.currentIndex, () => {
  if (map && map.loaded()) {
    nextTick(() => {
      updateProgressLine()
      updateVesselMarker()
    })
  }
})

watch(() => props.weatherGrids, () => {
  if (map && map.loaded()) {
    nextTick(() => {
      drawWeatherGrid()
    })
  }
}, { deep: true })

watch(() => props.showWeather, (val) => {
  if (map && map.loaded() && map.getLayer('weather-polygons')) {
    map.setLayoutProperty('weather-polygons', 'visibility', val ? 'visible' : 'none')
  }
})

watch(() => props.weatherType, () => {
  if (map && map.loaded()) {
    nextTick(() => {
      drawWeatherGrid()
    })
  }
})
</script>

<style scoped>
.map-wrapper {
  width: 100%;
  height: 100%;
}

:deep(.mapboxgl-popup-content) {
  padding: 12px;
  border-radius: 6px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
</style>
