export function douglasPeucker(points, tolerance, getX, getY) {
  if (points.length <= 2) return points.slice()

  const first = 0
  const last = points.length - 1
  const keep = new Array(points.length).fill(false)
  keep[first] = true
  keep[last] = true

  const stack = [[first, last]]
  const tolSq = tolerance * tolerance

  while (stack.length) {
    const [start, end] = stack.pop()
    let maxDistSq = -1
    let index = -1

    const x1 = getX(points[start])
    const y1 = getY(points[start])
    const x2 = getX(points[end])
    const y2 = getY(points[end])

    for (let i = start + 1; i < end; i++) {
      const pX = getX(points[i])
      const pY = getY(points[i])
      const distSq = perpendicularDistSq(x1, y1, x2, y2, pX, pY)
      if (distSq > maxDistSq) {
        maxDistSq = distSq
        index = i
      }
    }

    if (maxDistSq > tolSq && index !== -1) {
      keep[index] = true
      stack.push([start, index], [index, end])
    }
  }

  return points.filter((_, i) => keep[i])
}

function perpendicularDistSq(x1, y1, x2, y2, px, py) {
  const dx = x2 - x1
  const dy = y2 - y1
  if (dx === 0 && dy === 0) {
    const a = px - x1
    const b = py - y1
    return a * a + b * b
  }
  const t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)
  const clampedT = Math.max(0, Math.min(1, t))
  const projX = x1 + clampedT * dx
  const projY = y1 + clampedT * dy
  const ddx = px - projX
  const ddy = py - projY
  return ddx * ddx + ddy * ddy
}

export function adaptiveSimplify(points, getX, getY, targetRatio = 0.25, minTol = 0.0005, maxTol = 0.05) {
  if (points.length <= 100) return points.slice()

  const targetCount = Math.max(80, Math.floor(points.length * targetRatio))
  let lo = minTol
  let hi = maxTol
  let best = points
  for (let iter = 0; iter < 12; iter++) {
    const mid = (lo + hi) / 2
    const simplified = douglasPeucker(points, mid, getX, getY)
    best = simplified
    if (simplified.length < targetCount * 0.85) {
      hi = mid
    } else if (simplified.length > targetCount * 1.2) {
      lo = mid
    } else {
      break
    }
  }
  return best
}

export function getWeatherColor(value, min, max) {
  if (value == null || isNaN(value)) return 'rgba(0,0,0,0)'
  const ratio = Math.max(0, Math.min(1, (value - min) / (max - min)))
  const stops = [
    [0.0, 49, 54, 149],
    [0.2, 69, 117, 180],
    [0.4, 116, 173, 209],
    [0.6, 254, 224, 144],
    [0.8, 244, 109, 67],
    [1.0, 215, 48, 39]
  ]
  for (let i = 1; i < stops.length; i++) {
    const [t0, r0, g0, b0] = stops[i - 1]
    const [t1, r1, g1, b1] = stops[i]
    if (ratio <= t1) {
      const t = (ratio - t0) / (t1 - t0)
      const r = Math.round(r0 + (r1 - r0) * t)
      const g = Math.round(g0 + (g1 - g0) * t)
      const b = Math.round(b0 + (b1 - b0) * t)
      return `rgba(${r}, ${g}, ${b}, 0.55)`
    }
  }
  return `rgba(215, 48, 39, 0.55)`
}
