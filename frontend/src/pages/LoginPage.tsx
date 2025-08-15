import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'

export default function LoginPage() {
  const { login } = useAuth()
  const nav = useNavigate()
  const [form, setForm] = useState({ login: '', password: '' })
  const [err, setErr] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErr(null)
    setLoading(true)
    try {
      await login(form.login, form.password)
      const token = localStorage.getItem('jwt')
      if (token) nav('/account')
      else setErr('Login failed: No token returned')
    } catch (e: any) {
      setErr(e?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{ maxWidth: 420, margin: '4rem auto' }}>
      <h1>Sign in</h1>
      <form onSubmit={submit}>
        <div>
          <label>Login</label>
          <input
            value={form.login}
            onChange={(e) => setForm({ ...form, login: e.target.value })}
            placeholder="admin@local"
            required
          />
        </div>
        <div style={{ marginTop: 12 }}>
          <label>Password</label>
          <input
            type="password"
            value={form.password}
            onChange={(e) => setForm({ ...form, password: e.target.value })}
            required
          />
        </div>
        {err && <p style={{ color: 'crimson' }}>{err}</p>}
        <button style={{ marginTop: 16 }} disabled={loading}>
          {loading ? 'Signing in...' : 'Sign in'}
        </button>
      </form>
    </div>
  )
}