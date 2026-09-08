import apiClient from './axiosClient'

export const authApi = {
  login: (data) => apiClient.post('/auth/login', data).then((r) => r.data),
  register: (data) => apiClient.post('/auth/register', data).then((r) => r.data)
}

export const walletApi = {
  getMyWallet: () => apiClient.get('/wallet/me').then((r) => r.data)
}

export const categoryApi = {
  getAll: () => apiClient.get('/categories').then((r) => r.data),
  create: (data) => apiClient.post('/categories', data).then((r) => r.data),
  remove: (id) => apiClient.delete(`/categories/${id}`).then((r) => r.data)
}

export const transactionApi = {
  getAll: () => apiClient.get('/transactions').then((r) => r.data),
  getFiltered: (params) =>
    apiClient.get('/transactions/filtered', { params }).then((r) => r.data),
  create: (data) => apiClient.post('/transactions', data).then((r) => r.data),
  update: (id, data) => apiClient.put(`/transactions/${id}`, data).then((r) => r.data),
  remove: (id) => apiClient.delete(`/transactions/${id}`).then((r) => r.data)
}

export const budgetApi = {
  getAll: (period) =>
    apiClient.get('/budgets', { params: period ? { period } : {} }).then((r) => r.data),
  save: (data) => apiClient.post('/budgets', data).then((r) => r.data),
  remove: (id) => apiClient.delete(`/budgets/${id}`).then((r) => r.data)
}

export const analyticsApi = {
  getSummary: () => apiClient.get('/analytics/summary').then((r) => r.data)
}

export const insightsApi = {
  getAll: () => apiClient.get('/insights').then((r) => r.data)
}

/** Extract a user-friendly error message from an Axios error */
export function getErrorMessage(error, fallback = 'Something went wrong') {
  if (!error) return fallback
  const data = error.response?.data
  if (data?.message) return data.message
  if (data?.fields) {
    const first = Object.values(data.fields)[0]
    if (first) return first
  }
  if (error.message) return error.message
  return fallback
}
