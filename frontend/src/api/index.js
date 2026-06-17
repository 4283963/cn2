import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res.data
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export const vesselApi = {
  list: () => request.get('/vessel/list'),
  getById: (id) => request.get(`/vessel/${id}`)
}

export const trackApi = {
  query: (params) => request.get('/track/query', { params })
}

export const weatherApi = {
  getGrid: (params) => request.get('/weather/grid', { params }),
  getTimes: (params) => request.get('/weather/times', { params })
}

export default request
