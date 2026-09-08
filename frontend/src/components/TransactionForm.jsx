import React, { useState, useEffect } from 'react'
import { getErrorMessage } from '../api/services'

export default function TransactionForm({ categories, onSubmit, onClose, initial = null }) {
  const isEdit = Boolean(initial)
  const [type, setType] = useState(initial?.type || 'EXPENSE')
  const [amount, setAmount] = useState(initial?.amount != null ? String(initial.amount) : '')
  const [categoryId, setCategoryId] = useState(initial?.categoryId != null ? String(initial.categoryId) : '')
  const [description, setDescription] = useState(initial?.description || '')
  const [merchant, setMerchant] = useState(initial?.merchant || '')
  const [transactionDate, setTransactionDate] = useState(
    initial?.transactionDate || new Date().toISOString().slice(0, 10)
  )
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    if (initial) {
      setType(initial.type)
      setAmount(String(initial.amount))
      setCategoryId(initial.categoryId != null ? String(initial.categoryId) : '')
      setDescription(initial.description || '')
      setMerchant(initial.merchant || '')
      setTransactionDate(initial.transactionDate)
    }
  }, [initial])

  const filteredCategories = categories.filter((c) => c.type === type)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const num = Number(amount)
    if (!amount || Number.isNaN(num) || num <= 0) {
      setError('Enter a valid positive amount')
      return
    }
    if (!transactionDate) {
      setError('Date is required')
      return
    }
    setSubmitting(true)
    try {
      await onSubmit({
        type,
        amount: num,
        categoryId: categoryId ? Number(categoryId) : null,
        description: description.trim() || null,
        merchant: merchant.trim() || null,
        transactionDate
      })
      onClose()
    } catch (err) {
      setError(getErrorMessage(err, 'Failed to save transaction'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(e) => e.stopPropagation()}>
        <h3>{isEdit ? 'Edit Transaction' : 'Add Transaction'}</h3>
        <form onSubmit={handleSubmit} className="form">
          <div className="type-toggle">
            <button
              type="button"
              className={type === 'EXPENSE' ? 'toggle active expense' : 'toggle'}
              onClick={() => {
                setType('EXPENSE')
                setCategoryId('')
              }}
            >
              Expense
            </button>
            <button
              type="button"
              className={type === 'INCOME' ? 'toggle active income' : 'toggle'}
              onClick={() => {
                setType('INCOME')
                setCategoryId('')
              }}
            >
              Income
            </button>
          </div>

          <label>
            Amount
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0.00"
              required
            />
          </label>

          <label>
            Category
            <select value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
              <option value="">Uncategorized</option>
              {filteredCategories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            Merchant / Source
            <input
              type="text"
              value={merchant}
              onChange={(e) => setMerchant(e.target.value)}
              placeholder="e.g. Whole Foods"
              maxLength={200}
            />
          </label>

          <label>
            Description
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Optional note"
              maxLength={500}
            />
          </label>

          <label>
            Date
            <input
              type="date"
              value={transactionDate}
              onChange={(e) => setTransactionDate(e.target.value)}
              required
            />
          </label>

          {error && <p className="form-error">{error}</p>}

          <div className="form-actions">
            <button type="button" className="btn-secondary" onClick={onClose} disabled={submitting}>
              Cancel
            </button>
            <button type="submit" className="btn-primary" disabled={submitting}>
              {submitting ? 'Saving...' : isEdit ? 'Update Transaction' : 'Save Transaction'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
