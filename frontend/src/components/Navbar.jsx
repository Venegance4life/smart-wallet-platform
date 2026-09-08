import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  if (!user) return null

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <div className="navbar-brand">💠 Smart Wallet</div>
      <div className="navbar-links">
        <NavLink to="/" end className={({ isActive }) => (isActive ? 'active' : '')}>
          Dashboard
        </NavLink>
        <NavLink to="/transactions" className={({ isActive }) => (isActive ? 'active' : '')}>
          Transactions
        </NavLink>
        <NavLink to="/budgets" className={({ isActive }) => (isActive ? 'active' : '')}>
          Budgets
        </NavLink>
        <NavLink to="/analytics" className={({ isActive }) => (isActive ? 'active' : '')}>
          Analytics
        </NavLink>
        <NavLink to="/insights" className={({ isActive }) => (isActive ? 'active' : '')}>
          AI Insights
        </NavLink>
      </div>
      <div className="navbar-user">
        <span>{user.fullName}</span>
        <button onClick={handleLogout} className="btn-ghost">
          Logout
        </button>
      </div>
    </nav>
  )
}
