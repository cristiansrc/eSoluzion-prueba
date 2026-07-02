import React, { useState, useRef } from 'react'

const TEST_CASES = [
  { id: 'T1', date: '2020-06-14T10:00:00', product: '35455', brand: '1', expected: '35.50€', priceList: 1 },
  { id: 'T2', date: '2020-06-14T16:00:00', product: '35455', brand: '1', expected: '25.45€', priceList: 2 },
  { id: 'T3', date: '2020-06-14T21:00:00', product: '35455', brand: '1', expected: '35.50€', priceList: 1 },
  { id: 'T4', date: '2020-06-15T10:00:00', product: '35455', brand: '1', expected: '30.50€', priceList: 3 },
  { id: 'T5', date: '2020-06-16T21:00:00', product: '35455', brand: '1', expected: '38.95€', priceList: 4 },
]

function formatDate(iso) {
  if (!iso) return '—'
  const d = new Date(iso)
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function App() {
  const [results, setResults] = useState({})
  const [loading, setLoading] = useState({})
  const [errors, setErrors] = useState({})
  const [customDate, setCustomDate] = useState('2020-06-14')
  const [customTime, setCustomTime] = useState('10:00')
  const [customProduct, setCustomProduct] = useState('35455')
  const [customBrand, setCustomBrand] = useState('1')
  const [customResult, setCustomResult] = useState(null)
  const [customError, setCustomError] = useState(null)
  const [customLoading, setCustomLoading] = useState(false)
  const [log, setLog] = useState([])
  const logEndRef = useRef(null)

  const addLog = (entry) => {
    setLog(prev => [entry, ...prev])
    setTimeout(() => logEndRef.current?.scrollIntoView({ behavior: 'smooth' }), 50)
  }

  const executeTest = async (tc) => {
    setLoading(prev => ({ ...prev, [tc.id]: true }))
    setErrors(prev => ({ ...prev, [tc.id]: null }))

    try {
      const params = new URLSearchParams({ applicationDate: tc.date, productId: tc.product, brandId: tc.brand })
      const url = `/api/prices?${params}`
      const response = await fetch(url)
      let data, errorMsg
      if (!response.ok) {
        const errData = await response.json().catch(() => null)
        errorMsg = errData?.message || `Error ${response.status}`
        throw new Error(errorMsg)
      }
      data = await response.json()
      setResults(prev => ({ ...prev, [tc.id]: data }))
      addLog({ time: new Date().toLocaleTimeString(), method: 'GET', url, response: JSON.stringify(data, null, 2), test: tc.id })
    } catch (err) {
      setErrors(prev => ({ ...prev, [tc.id]: err.message }))
      addLog({ time: new Date().toLocaleTimeString(), method: 'GET', url: `/api/prices?applicationDate=${tc.date}&productId=${tc.product}&brandId=${tc.brand}`, response: `ERROR: ${err.message}`, test: tc.id, isError: true })
    } finally {
      setLoading(prev => ({ ...prev, [tc.id]: false }))
    }
  }

  const executeAll = async () => {
    for (const tc of TEST_CASES) {
      await executeTest(tc)
    }
  }

  const handleCustomSubmit = async (e) => {
    e.preventDefault()
    setCustomLoading(true)
    setCustomError(null)
    setCustomResult(null)

    const applicationDate = `${customDate}T${customTime}:00`

    try {
      const params = new URLSearchParams({ applicationDate, productId: customProduct, brandId: customBrand })
      const url = `/api/prices?${params}`
      const response = await fetch(url)
      let data, errorMsg
      if (!response.ok) {
        const errData = await response.json().catch(() => null)
        errorMsg = errData?.message || `Error ${response.status}`
        throw new Error(errorMsg)
      }
      data = await response.json()
      setCustomResult(data)
      addLog({ time: new Date().toLocaleTimeString(), method: 'GET', url, response: JSON.stringify(data, null, 2), test: 'Custom' })
    } catch (err) {
      setCustomError(err.message)
      addLog({ time: new Date().toLocaleTimeString(), method: 'GET', url: `/api/prices?applicationDate=${applicationDate}&productId=${customProduct}&brandId=${customBrand}`, response: `ERROR: ${err.message}`, test: 'Custom', isError: true })
    } finally {
      setCustomLoading(false)
    }
  }

  return (
    <div className="app">
      <header className="header">
        <h1 className="title">Pricing</h1>
        <p className="subtitle">Consulta de tarifas de productos · 5 casos de prueba</p>
      </header>

      {/* Tabla de 5 tests obligatorios */}
      <div className="card" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <h2 className="card-title" style={{ marginBottom: 0 }}>Casos obligatorios</h2>
          <button className="btn" style={{ width: 'auto', padding: '8px 20px', fontSize: '0.8rem' }} onClick={executeAll}>
            Ejecutar todos
          </button>
        </div>
        <div className="test-table">
          <div className="test-table-header">
            <span>Test</span>
            <span>Fecha</span>
            <span>Hora</span>
            <span>Esperado</span>
            <span>Obtenido</span>
            <span>Estado</span>
            <span></span>
          </div>
          {TEST_CASES.map(tc => {
            const result = results[tc.id]
            const error = errors[tc.id]
            const isLoading = loading[tc.id]
            const passed = result && result.priceList === tc.priceList

            return (
              <div key={tc.id} className={`test-table-row ${passed ? 'row-pass' : ''}`}>
                <span className="test-id">{tc.id}</span>
                <span className="test-date">{tc.date.split('T')[0]}</span>
                <span className="test-time">{tc.date.split('T')[1]}</span>
                <span className="test-expected">{tc.expected}</span>
                <span className="test-obtained">
                  {isLoading ? '...' : error ? 'Error' : result ? `${result.price}€` : '—'}
                </span>
                <span className="test-status">
                  {isLoading ? <span className="tag tag-yellow">Consultando</span> :
                   error ? <span className="tag tag-red">Error</span> :
                   passed ? <span className="tag tag-green">✓ Correcto</span> :
                   result ? <span className="tag tag-red">✗ Fallo</span> :
                   <span className="tag" style={{ background: '#F0F0F0', color: '#999' }}>Pendiente</span>}
                </span>
                <button className="test-btn" onClick={() => executeTest(tc)} disabled={isLoading}
                  style={{ padding: '4px 12px', fontSize: '0.75rem' }}>
                  {isLoading ? '...' : 'Ejecutar'}
                </button>
              </div>
            )
          })}
        </div>
      </div>

      {/* Consulta personalizada */}
      <div className="bento">
        <div className="card form-card">
          <h2 className="card-title">Consulta personalizada</h2>
          <form onSubmit={handleCustomSubmit}>
            <div className="field-row">
              <div className="field">
                <label htmlFor="cDate">Fecha</label>
                <input id="cDate" type="date" value={customDate}
                  onChange={(e) => setCustomDate(e.target.value)} />
              </div>
              <div className="field field-time">
                <label htmlFor="cTime">Hora</label>
                <input id="cTime" type="time" value={customTime}
                  onChange={(e) => setCustomTime(e.target.value)} />
              </div>
            </div>
            <div className="field-row">
              <div className="field">
                <label htmlFor="cProduct">Producto</label>
                <input id="cProduct" type="number" value={customProduct}
                  onChange={(e) => setCustomProduct(e.target.value)} />
              </div>
              <div className="field">
                <label htmlFor="cBrand">Cadena</label>
                <input id="cBrand" type="number" value={customBrand}
                  onChange={(e) => setCustomBrand(e.target.value)} />
              </div>
            </div>
            <button type="submit" className="btn" disabled={customLoading}>
              {customLoading ? 'Consultando...' : 'Consultar precio'}
            </button>
          </form>
        </div>

        <div className="card results-card">
          <h2 className="card-title">Resultado</h2>
          {customLoading && <p className="muted">Consultando tarifa...</p>}
          {customError && (
            <div className="error-box">
              <span className="tag tag-red">Error</span>
              <p>{customError}</p>
            </div>
          )}
          {customResult && !customLoading && (
            <div className="result-content">
              <div className="price-hero">
                <span className="price-value">{customResult.price} €</span>
                <span className="tag tag-blue">Tarifa {customResult.priceList}</span>
              </div>
              <div className="details">
                <div className="detail-item">
                  <span className="detail-label">Producto</span>
                  <span className="detail-value">{customResult.productId}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Cadena</span>
                  <span className="detail-value">{customResult.brandId}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Vigencia</span>
                  <span className="detail-value">{formatDate(customResult.startDate)} → {formatDate(customResult.endDate)}</span>
                </div>
                <div className="detail-item">
                  <span className="detail-label">Moneda</span>
                  <span className="detail-value">{customResult.currency}</span>
                </div>
              </div>
            </div>
          )}
          {!customResult && !customLoading && !customError && (
            <p className="muted">Completa los parámetros y consulta un precio.</p>
          )}
        </div>
      </div>

      {/* Log de peticiones */}
      <div className="card log-card">
        <h2 className="card-title">Historial de peticiones</h2>
        {log.length === 0 && <p className="log-empty">Aún no se han realizado peticiones.</p>}
        {log.map((entry, i) => (
          <div key={i} className="log-entry">
            <div className="log-header">
              <span className="log-method">{entry.method}</span>
              <span className="log-time">{entry.time} · {entry.test}</span>
            </div>
            <div className="log-url">{entry.url}</div>
            <div className="log-response" style={entry.isError ? { color: '#9F2F2D' } : {}}>{entry.response}</div>
          </div>
        ))}
        <div ref={logEndRef} />
      </div>

      <footer className="footer">
        <p>eSoluzion Pricing API · Arquitectura Hexagonal · OpenAPI First</p>
      </footer>
    </div>
  )
}

export default App
