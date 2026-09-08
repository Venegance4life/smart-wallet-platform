import React, { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { walletApi, analyticsApi, insightsApi, transactionApi, categoryApi } from '../api/services'
import TransactionForm from '../components/TransactionForm.jsx'

export default function Dashboard() {
  const [wallet, setWallet] = useState(null)
  const [summary, setSummary] = useState(null)
  const [insights, setInsights] = useState([])
  const [recentTx, setRecentTx] = useState([])
  const [categories, setCategories] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [loading, setLoading] = useState(true)

  const loadAll = async () => {
    setLoading(true)
    const [walletData, summaryData, insightsData, txData, catData] = await Promise.all([
      walletApi.getMyWallet(),
      analyticsApi.getSummary(),
      insightsApi.getAll(),
      transactionApi.getAll(),
      categoryApi.getAll()
    ])
    setWallet(walletData)
    setSummary(summaryData)
    setInsights(insightsData.slice(0, 3))
    setRecentTx(txData.slice(0, 5))
    setCategories(catData)
    setLoading(false)
  }

  useEffect(() => {
    loadAll()
  }, [])

  const handleCreateTransaction = async (payload) => {
    await transactionApi.create(payload)
    await loadAll()
  }

  if (loading) return <div className="page-loading">Loading your wallet...</div>

  return (
    <div className="page">
      <div className="page-header">
        <h1>Dashboard</h1>
        <button className="btn-primary" onClick={() => setShowForm(true)}>
          + Add Transaction
        </button>
      </div>

      <div className="stat-grid">
        <div className="stat-card highlight">
          <span className="stat-label">Wallet Balance</span>
          <span className="stat-value">
            {wallet.currency} {Number(wallet.balance).toFixed(2)}
          </span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Total Income</span>
          <span className="stat-value positive">${Number(summary.totalIncome).toFixed(2)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Total Expenses</span>
          <span className="stat-value negative">${Number(summary.totalExpense).toFixed(2)}</span>
        </div>
        <div className="stat-card">
          <span className="stat-label">Savings Rate</span>
          <span className="stat-value">{Number(summary.savingsRate).toFixed(1)}%</span>
        </div>
      </div>

      <div className="two-column">
        <div className="panel">
          <div className="panel-header">
            <h2>Recent Transactions</h2>
            <Link to="/transactions">View all →</Link>
          </div>
          {recentTx.length === 0 ? (
            <p className="empty-state">No transactions yet. Add your first one!</p>
          ) : (
            <ul className="tx-list">
              {recentTx.map((t) => (
                <li key={t.id} className="tx-row">
                  <div>
                    <strong>{t.description || t.merchant || t.categoryName}</strong>
                    <span className="tx-meta">
                      {t.categoryName} · {t.transactionDate}
                    </span>
                  </div>
                  <span className={t.type === 'INCOME' ? 'amount positive' : 'amount negative'}>
                    {t.type === 'INCOME' ? '+' : '-'}${Number(t.amount).toFixed(2)}
                  </span>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="panel">
          <div className="panel-header">
            <h2>✨ AI Insights</h2>
            <Link to="/insights">View all →</Link>
          </div>
          {insights.length === 0 ? (
            <p className="empty-state">No insights yet.</p>
          ) : (
            <ul className="insight-list">
              {insights.map((insight, idx) => (
                <li key={idx} className={`insight-item severity-${insight.severity.toLowerCase()}`}>
                  <span className="insight-title">{insight.title}</span>
                  <p className="insight-message">{insight.message}</p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {showForm && (
        <TransactionForm
          categories={categories}
          onSubmit={handleCreateTransaction}
          onClose={() => setShowForm(false)}
        />
      )}
    </div>
  )
}
