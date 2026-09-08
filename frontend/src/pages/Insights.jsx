import React, { useEffect, useState } from 'react'
import { insightsApi } from '../api/services'

const ICONS = {
  WARNING: '⚠️',
  TIP: '💡',
  FORECAST: '🔮',
  ANOMALY: '📈'
}

export default function Insights() {
  const [insights, setInsights] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    insightsApi.getAll().then((data) => {
      setInsights(data)
      setLoading(false)
    })
  }, [])

  if (loading) return <div className="page-loading">Analyzing your spending patterns...</div>

  return (
    <div className="page">
      <div className="page-header">
        <h1>✨ AI Insights</h1>
      </div>
      <p className="page-subtitle">
        Generated from your transaction history using anomaly detection, budget tracking, and
        trend forecasting.
      </p>

      <div className="insight-grid">
        {insights.map((insight, idx) => (
          <div key={idx} className={`insight-card severity-${insight.severity.toLowerCase()}`}>
            <div className="insight-card-header">
              <span className="insight-icon">{ICONS[insight.type] || '📊'}</span>
              <span className={`badge badge-${insight.severity.toLowerCase()}`}>
                {insight.severity}
              </span>
            </div>
            <h3>{insight.title}</h3>
            <p>{insight.message}</p>
          </div>
        ))}
      </div>
    </div>
  )
}
