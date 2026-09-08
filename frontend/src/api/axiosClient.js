import axios from 'axios'

const apiClient = axios.create({
  baseURL: '/api'
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('sw_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('sw_token')
      localStorage.removeItem('sw_user')
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default apiClient
