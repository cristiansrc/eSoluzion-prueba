import React, { useState } from 'react'

function App() {
  const [applicationDate, setApplicationDate] = useState('2020-06-14T10:00:00')
  const [productId, setProductId] = useState('35455')
  const [brandId, setBrandId] = useState('1')
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError(null)
    setResult(null)

    try {
      const params = new URLSearchParams({ applicationDate, productId, brandId })
      const response = await fetch(`/api/prices?${params}`)

      if (!response.ok) {
        const errData = await response.json().catch(() => null)
        throw new Error(errData?.message || `Error ${response.status}`)
      }

      const data = await response.json()
      setResult(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  const testCases = [
    { label: 'T1: 14/06 10:00 → 35.50€', date: '2020-06-14T10:00:00', product: '35455', brand: '1' },
    { label: 'T2: 14/06 16:00 → 25.45€', date: '2020-06-14T16:00:00', product: '35455', brand: '1' },
    { label: 'T3: 14/06 21:00 → 35.50€', date: '2020-06-14T21:00:00', product: '35455', brand: '1' },
    { label: 'T4: 15/06 10:00 → 30.50€', date: '2020-06-15T10:00:00', product: '35455', brand: '1' },
    { label: 'T5: 16/06 21:00 → 38.95€', date: '2020-06-16T21:00:00', product: '35455', brand: '1' },
  ]

  return (
    <div className="app">
      <header className="header">
        <h1 className="title">Pricing</h1>
        <p className="subtitle">Consulta de tarifas de productos</p>
      </header>

      <div className="bento">
        <div className="card form-card">
          <form onSubmit={handleSubmit}>
            <div className="field">
              <label htmlFor="date">Fecha y hora</label>
              <input
                id="date"
                type="text"
                value={applicationDate}
                onChange={(e) => setApplicationDate(e.target.value)}
                placeholder="2020-06-14T10:00:00"
              />
            </div>
            <div className="field-row">
              <div className="field">
                <label htmlFor="product">Producto</label>
                <input
                  id="product"
                  type="text"
                  value={productId}
                  onChange={(e) => setProductId(e.target.value)}
                  placeholder="35455"
                />
              </div>
              <div className="field">
                <label htmlFor="brand">Cadena</label>
                <input
                  id="brand"
                  type="text"
                  value={brandId}
                  onChange={(e) => setBrandId(e.target.value)}
                  placeholder="1"
                />
              </div>
            </div>
            <button type="submit" className="btn" disabled={loading}>
              {loading ? 'Consultando...' : 'Consultar precio'}
            </button>
          </form>
        </div>

        <div className="card results-card">
          <h2 className="card-title">Resultado</h2>
          {loading && <p className="muted">Consultando tarifa...</p>}
          {error && (
            <div className="error-box">
              <span className="tag tag-red">Error</span>
              <p>{error}</p>
            </div>
          )}
          {result && !loading && (
            <div className="result-content">
              <div className="price-hero">
                <span className="price-value">{result.price} €</span>
                <span className="tag tag-blue">Tarifa {result.priceList}</span>
              </div>
              <div className="details">
                <div className="detail-item">
                  <span className="detail-label">Producto</span>
                  <span className="detail-value">{result.productId}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Cadena</span>
                  <span className="detail-value">{result.brandId}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Vigencia</span>
                  <span className="detail-value">{result.startDate} → {result.endDate}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Moneda</span>
                  <span className="detail-value">{result.currency}</span>
                </div>
              </div>
            </div>
          )}
          {!result && !loading && !error && (
            <p className="muted">Completa los parámetros y consulta un precio.</p>
          )}
        </div>
      </div>

      <div className="card test-card">
        <h2 className="card-title">Casos de prueba</h2>
        <p className="muted">Carga automática los 5 casos del enunciado:</p>
        <div className="test-grid">
          {testCases.map((tc, i) => (
            <button
              key={i}
              className="test-btn"
              onClick={() => {
                setApplicationDate(tc.date)
                setProductId(tc.product)
                setBrandId(tc.brand)
              }}
            >
              {tc.label}
            </button>
          ))}
        </div>
      </div>

      <footer className="footer">
        <p>eSoluzion Pricing API · Arquitectura Hexagonal · OpenAPI First</p>
      </footer>
    </div>
  )
}

export default App
