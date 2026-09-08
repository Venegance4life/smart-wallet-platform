import React, { useEffect, useState } from 'react'
import { transactionApi, categoryApi, getErrorMessage } from '../api/services'
import TransactionForm from '../components/TransactionForm.jsx'

export default function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [categories, setCategories] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [editing, setEditing] = useState(null)
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [actionError, setActionError] = useState('')

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const [txData, catData] = await Promise.all([
        transactionApi.getAll(),
        categoryApi.getAll()
      ])
      setTransactions(txData)
      setCategories(catData)
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to load transactions'))
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const handleCreate = async (payload) => {
    await transactionApi.create(payload)
    setActionError('')
    await load()
  }

  const handleUpdate = async (payload) => {
    if (!editing) return
    await transactionApi.update(editing.id, payload)
    setActionError('')
    setEditing(null)
    await load()
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this transaction? This will update your wallet balance.')) return
    setActionError('')
    try {
      await transactionApi.remove(id)
      await load()
    } catch (err) {
      setActionError(getErrorMessage(err, 'Failed to delete transaction'))
    }
  }

  const filtered =
    filter === 'ALL' ? transactions : transactions.filter((t) => t.type === filter)

  if (loading) return <div className="page-loading">Loading transactions...</div>

  if (error) {
    return (
      <div className="page">
        <div className="page-header">
          <h1>Transactions</h1>
        </div>
        <div className="panel">
          <p className="form-error">{error}</p>
          <button className="btn-primary" onClick={load}>
            Retry
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Transactions</h1>
        <button
          className="btn-primary"
          onClick={() => {
            setEditing(null)
            setShowForm(true)
          }}
        >
          + Add Transaction
        </button>
      </div>

      {actionError && <p className="form-error banner-error">{actionError}</p>}

      <div className="filter-row">
        {['ALL', 'INCOME', 'EXPENSE'].map((f) => (
          <button
            key={f}
            className={filter === f ? 'chip active' : 'chip'}
            onClick={() => setFilter(f)}
          >
            {f === 'ALL' ? 'All' : f === 'INCOME' ? 'Income' : 'Expenses'}
          </button>
        ))}
      </div>

      <div className="panel">
        {filtered.length === 0 ? (
          <p className="empty-state">No transactions found. Add your first one to get started.</p>
        ) : (
          <table className="tx-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Description</th>
                <th>Category</th>
                <th>Amount</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((t) => (
                <tr key={t.id}>
                  <td>{t.transactionDate}</td>
                  <td>{t.description || t.merchant || '—'}</td>
                  <td>
                    <span className="category-badge">{t.categoryName}</span>
                  </td>
                  <td className={t.type === 'INCOME' ? 'amount positive' : 'amount negative'}>
                    {t.type === 'INCOME' ? '+' : '-'}${Number(t.amount).toFixed(2)}
                  </td>
                  <td className="actions-cell">
                    <button
                      className="btn-ghost small"
                      onClick={() => {
                        setEditing(t)
                        setShowForm(true)
                      }}
                    >
                      Edit
                    </button>
                    <button className="btn-ghost small danger" onClick={() => handleDelete(t.id)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showForm && (
        <TransactionForm
          categories={categories}
          initial={editing}
          onSubmit={editing ? handleUpdate : handleCreate}
          onClose={() => {
            setShowForm(false)
            setEditing(null)
          }}
        />
      )}
    </div>
  )
}
