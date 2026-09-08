import React, { useEffect, useState } from 'react'
import { budgetApi, categoryApi } from '../api/services'

function currentPeriod() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

export default function Budgets() {
  const [budgets, setBudgets] = useState([])
  const [categories, setCategories] = useState([])
  const [categoryId, setCategoryId] = useState('')
  const [limit, setLimit] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  const period = currentPeriod()

  const load = async () => {
    setLoading(true)
    const [budgetData, catData] = await Promise.all([
      budgetApi.getAll(period),
      categoryApi.getAll()
    ])
    setBudgets(budgetData)
    setCategories(catData.filter((c) => c.type === 'EXPENSE'))
    setLoading(false)
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!categoryId || !limit || Number(limit) <= 0) {
      setError('Choose a category and a valid monthly limit')
      return
    }
    try {
      await budgetApi.save({ categoryId: Number(categoryId), monthlyLimit: Number(limit), period })
      setCategoryId('')
      setLimit('')
      await load()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save budget')
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Remove this budget?')) return
    await budgetApi.remove(id)
    await load()
  }

  if (loading) return <div className="page-loading">Loading budgets...</div>

  return (
    <div className="page">
      <div className="page-header">
        <h1>Budgets — {period}</h1>
      </div>

      <div className="panel">
        <h2>Set a Monthly Budget</h2>
        <form onSubmit={handleSubmit} className="inline-form">
          <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
            <option value="">Select category</option>
            {categories.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
          <input
            type="number"
            step="0.01"
            min="0"
            placeholder="Monthly limit"
            value={limit}
            onChange={(e) => setLimit(e.target.value)}
          />
          <button type="submit" className="btn-primary">
            Save Budget
          </button>
        </form>
        {error && <p className="form-error">{error}</p>}
      </div>

      <div className="budget-grid">
        {budgets.length === 0 ? (
          <p className="empty-state">No budgets set for this month yet.</p>
        ) : (
          budgets.map((b) => (
            <div key={b.id} className={`budget-card ${b.overBudget ? 'over' : ''}`}>
              <div className="budget-card-header">
                <h3>{b.categoryName}</h3>
                <button className="btn-ghost small" onClick={() => handleDelete(b.id)}>
                  Remove
                </button>
              </div>
              <div className="progress-bar">
                <div
                  className={`progress-fill ${b.overBudget ? 'over' : ''}`}
                  style={{ width: `${Math.min(b.percentUsed, 100)}%` }}
                />
              </div>
              <div className="budget-stats">
                <span>
                  ${Number(b.spent).toFixed(2)} / ${Number(b.monthlyLimit).toFixed(2)}
                </span>
                <span className={b.overBudget ? 'negative' : ''}>
                  {b.percentUsed.toFixed(0)}% used
                </span>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
