import { useEffect, useState } from 'react'
import { api } from '../api/client'
import { useNavigate, useParams } from 'react-router-dom'

export default function OrderDetailPage() {
  const { id } = useParams()
  const [order, setOrder] = useState<any>(null)
  const nav = useNavigate()

  useEffect(() => {
    if (!id) return
    api.orders.get(Number(id)).then((o) => { setOrder(o); try { const ids = JSON.parse(localStorage.getItem('my_order_ids') || '[]'); if (!ids.includes(o.id)) { ids.unshift(o.id); localStorage.setItem('my_order_ids', JSON.stringify(ids.slice(0, 50))); } } catch {} })
  }, [id])

  if (!order) return <div style={{ padding: 24 }}>Loading…</div>

  const total = Number(order.total ?? 0)

  const pay = () => {
    api.payments.pay(order.id, total).then((pmt) => {
      alert(`Payment ${pmt.status} (txn: ${pmt.providerTxnId ?? '-'})`)
      api.orders.get(order.id).then(setOrder)
    }).catch(err => {
      alert(err?.message || 'Payment failed')
    })
  }

  const cancel = async () => {
    if (!confirm('Cancel this order?')) return
    try {
      await api.orders.cancel(order.id)
      alert('Order cancelled')
      nav('/orders')
    } catch (e: any) {
      alert(e?.message || 'Cancel failed')
    }
  }

  return (
    <div style={{ maxWidth: 720, margin: '2rem auto' }}>
      <h1>Order #{order.id}</h1>
      <div>Status: <b>{order.status}</b></div>
      <div>Total: <b>${total.toFixed(2)}</b></div>
      <h3 style={{ marginTop: 16 }}>Items</h3>
      <ul>
        {order.items?.map((it: any) => (
          <li key={it.sku}>
            {it.name} ({it.sku}) × {it.quantity} — ${Number(it.lineTotal).toFixed(2)}
          </li>
        ))}
      </ul>
      <div style={{ marginTop: 16, display:'flex', gap:8 }}>
        <button onClick={pay}>Pay now</button>
        <button onClick={() => nav('/items')}>Back to catalog</button>
        <button onClick={cancel} style={{ marginLeft: 'auto' }}>Cancel order</button>
      </div>
    </div>
  )
}