import React, { createContext, useContext, useEffect, useState } from 'react'
import { authApi } from '../api/services'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const stored = localStorage.getItem('sw_user')
    const token = localStorage.getItem('sw_token')
    if (stored && token) {
      setUser(JSON.parse(stored))
    }
    setLoading(false)
  }, [])

  const persist = (response) => {
    const { token, userId, fullName, email, role } = response
    const userObj = { userId, fullName, email, role }
    localStorage.setItem('sw_token', token)
    localStorage.setItem('sw_user', JSON.stringify(userObj))
    setUser(userObj)
    return userObj
  }

  const login = async (email, password) => {
    const response = await authApi.login({ email, password })
    return persist(response)
  }

  const register = async (fullName, email, password) => {
    const response = await authApi.register({ fullName, email, password })
    return persist(response)
  }

  const logout = () => {
    localStorage.removeItem('sw_token')
    localStorage.removeItem('sw_user')
    setUser(null)
  }

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
