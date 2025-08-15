import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { Link } from 'react-router-dom'

export default function OrdersPage() {
  const [orders, setOrders] = useState<any[]>([])
  const [err, setErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let alive = true
    setLoading(true)
    setErr(null)
    async function run() {
      try {
        const ids: number[] = JSON.parse(localStorage.getItem('my_order_ids') || '[]')
        const data = await Promise.all(ids.map((id) => api.orders.get(id).catch(() => null)))
        if (!alive) return
        setOrders(data.filter(Boolean) as any[])
      } catch (e: any) {
        if (!alive) return
        setErr(e?.message || 'Failed to load orders')
      } finally {
        if (alive) setLoading(false)
      }
    }
    run()
    return () => { alive = false }
  }, [])

  if (loading) return <div style={{ padding: 24 }}>Loading…</div>
  if (err) return <div style={{ padding: 24, color: 'crimson' }}>{err}</div>

  return (
    <div style={{ maxWidth: 720, margin: '2rem auto' }}>
      <h1>My Orders (this session)</h1>
      {orders.length === 0 ? <p>No orders yet (create one from the catalog).</p> : (
        <ul>
          {orders.map((o) => (
            <li key={o.id}>
              <Link to={`/orders/${o.id}`}>Order #{o.id}</Link> — {o.status} — ${Number(o.total ?? 0).toFixed(2)}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}