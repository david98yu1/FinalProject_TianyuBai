import { useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { api, type Item } from '../api/client'
import { useAuth } from '../context/AuthContext'

export default function ItemsPage() {
  const nav = useNavigate()
  const { user, refreshMe } = useAuth()

  const [q, setQ] = useState<string>('')
  const [page, setPage] = useState<number>(0)
  const [size, setSize] = useState<number>(12)

  const [items, setItems] = useState<Item[]>([])
  const [totalPages, setTotalPages] = useState<number>(0)
  const [totalElements, setTotalElements] = useState<number>(0)
  const [loading, setLoading] = useState<boolean>(false)
  const [err, setErr] = useState<string | null>(null)

  const [cart, setCart] = useState<{ sku: string; quantity: number }[]>([])

  const params = useMemo(() => ({ q: q || undefined, page, size }), [q, page, size])

  useEffect(() => {
    let alive = true
    setLoading(true)
    setErr(null)

    api.items.search<any>(params.q, params.page, params.size)
      .then((data) => {
        if (!alive) return
        setItems(data.content ?? [])
        setTotalPages(data.totalPages ?? 0)
        setTotalElements(data.totalElements ?? 0)
      })
      .catch((e) => {
        if (!alive) return
        setErr((e as any)?.message || 'Failed to load items')
      })
      .finally(() => alive && setLoading(false))

    return () => { alive = false }
  }, [params])

  const add = (sku: string) => {
    setCart((c) => {
      const found = c.find((i) => i.sku === sku)
      if (found) return c.map((i) => (i.sku === sku ? { ...i, quantity: i.quantity + 1 } : i))
      return [...c, { sku, quantity: 1 }]
    })
  }

  const checkout = async () => {
    // Ensure we know the account id
    if (!user) await refreshMe()
    if (!user) {
      alert('Please sign in first.')
      nav('/login')
      return
    }
    try {
      const order = await api.orders.create(user.id as number, cart)
      setCart([])
      try { const ids = JSON.parse(localStorage.getItem('my_order_ids') || '[]'); ids.unshift(order.id); localStorage.setItem('my_order_ids', JSON.stringify(ids.slice(0, 50))); } catch {}
      nav(`/orders/${order.id}`)
    } catch (err: any) {
      alert(err?.message || 'Failed to create order')
    }
  }

  const canPrev = page > 0
  const canNext = page + 1 < totalPages

  return (
    <div style={{ maxWidth: 980, margin: '2rem auto' }}>
      <h1>Catalog</h1>

      <div style={{ display: 'flex', gap: 12, alignItems: 'center', margin: '12px 0 20px' }}>
        <input
          value={q}
          onChange={(e) => { setQ(e.target.value); setPage(0) }}
          placeholder="Search items…"
          style={{ flex: 1, padding: 8 }}
        />
        <label>
          Page size:&nbsp;
          <select value={size} onChange={(e) => { setSize(Number(e.target.value)); setPage(0) }}>
            {[6, 12, 24, 48].map((n) => <option key={n} value={n}>{n}</option>)}
          </select>
        </label>
      </div>

      {loading && <div style={{ padding: 8 }}>Loading…</div>}
      {err && <div style={{ padding: 8, color: 'crimson' }}>{err}</div>}

      <ul style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: 16, listStyle: 'none', padding: 0, margin: 0 }}>
        {items.map((x) => (
          <li key={x.id} style={{ border: '1px solid #eee', padding: 12, borderRadius: 8 }}>
            {x.pictureUrl ? (
              <img src={x.pictureUrl} alt={x.name} style={{ width: '100%', height: 140, objectFit: 'cover', borderRadius: 6, marginBottom: 8 }} />
            ) : null}
            <strong style={{ display: 'block', marginBottom: 4 }}>{x.name}</strong>
            <div style={{ fontSize: 12, color: '#666', minHeight: 36 }}>{x.description || '—'}</div>
            <div style={{ marginTop: 6 }}>SKU: {x.sku}</div>
            <div style={{ marginTop: 4 }}>${(x.price ?? 0).toFixed(2)}</div>
            <div style={{ marginTop: 2 }}>Stock: {x.stock}</div>
            <button onClick={() => add(x.sku)} disabled={x.stock <= 0} style={{ marginTop: 8 }} title={x.stock <= 0 ? 'Out of stock' : 'Add to cart'}>
              Add to cart
            </button>
          </li>
        ))}
      </ul>

      <div style={{ marginTop: 16, display: 'flex', alignItems: 'center', gap: 12 }}>
        <button disabled={!canPrev} onClick={() => setPage((p) => Math.max(0, p - 1))}>← Prev</button>
        <span>Page <b>{page + 1}</b> of <b>{Math.max(totalPages, 1)}</b> ({totalElements} items)</span>
        <button disabled={!canNext} onClick={() => setPage((p) => p + 1)}>Next →</button>
      </div>

      <div style={{ marginTop: 24, borderTop: '1px solid #ddd', paddingTop: 12 }}>
        <h2>Cart</h2>
        {cart.length === 0 ? <p>Empty</p> : (
          <>
            <ul>{cart.map((c) => <li key={c.sku}>{c.sku} × {c.quantity}</li>)}</ul>
            <button onClick={checkout}>Checkout</button>
          </>
        )}
      </div>
    </div>
  )
}