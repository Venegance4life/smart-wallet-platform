import React, { useEffect, useState } from 'react'
import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  Legend,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  LineChart,
  Line
} from 'recharts'
import { analyticsApi } from '../api/services'

const COLORS = ['#6366f1', '#22c55e', '#f59e0b', '#ef4444', '#06b6d4', '#a855f7', '#ec4899', '#84cc16']

export default function Analytics() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    analyticsApi.getSummary().then((data) => {
      setSummary(data)
      setLoading(false)
    })
  }, [])

  if (loading) return <div className="page-loading">Crunching your numbers...</div>

  return (
    <div className="page">
      <div className="page-header">
        <h1>Analytics</h1>
      </div>

      <div className="two-column">
        <div className="panel">
          <h2>Spending by Category</h2>
          {summary.expenseByCategory.length === 0 ? (
            <p className="empty-state">No expense data yet.</p>
          ) : (
            <ResponsiveContainer width="100%" height={320}>
              <PieChart>
                <Pie
                  data={summary.expenseByCategory}
                  dataKey="amount"
                  nameKey="category"
                  cx="50%"
                  cy="50%"
                  outerRadius={110}
                  label={(entry) => `${entry.category} (${entry.percentage.toFixed(0)}%)`}
                >
                  {summary.expenseByCategory.map((entry, idx) => (
                    <Cell key={entry.category} fill={COLORS[idx % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="panel">
          <h2>Income vs. Expense (6 months)</h2>
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={summary.monthlyTrends}>
              <CartesianGrid strokeDasharray="3 3" stroke="#2a2f45" />
              <XAxis dataKey="month" stroke="#8b93b0" />
              <YAxis stroke="#8b93b0" />
              <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
              <Legend />
              <Bar dataKey="income" fill="#22c55e" name="Income" radius={[4, 4, 0, 0]} />
              <Bar dataKey="expense" fill="#ef4444" name="Expense" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="panel">
        <h2>Net Savings Trend</h2>
        <ResponsiveContainer width="100%" height={280}>
          <LineChart data={summary.monthlyTrends}>
            <CartesianGrid strokeDasharray="3 3" stroke="#2a2f45" />
            <XAxis dataKey="month" stroke="#8b93b0" />
            <YAxis stroke="#8b93b0" />
            <Tooltip formatter={(value) => `$${Number(value).toFixed(2)}`} />
            <Line type="monotone" dataKey="net" stroke="#6366f1" strokeWidth={3} name="Net Savings" />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  )
}
