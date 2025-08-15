import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { api } from '../api/client'
import {jwtDecode, type JwtPayload as StdJwtPayload} from 'jwt-decode';

type MyJwtPayload = StdJwtPayload & {
  uid?: string;
  userId?: string;
};

export default function SignUp() {
  const nav = useNavigate()
  const [form, setForm] = useState({ email: '', username: '', password: '' })
  const [showPw, setShowPw] = useState(false)
  const [loading, setLoading] = useState(false)
  const [err, setErr] = useState<string | null>(null)

  const onChange = (k: keyof typeof form) => (e: React.ChangeEvent<HTMLInputElement>) =>
    setForm({ ...form, [k]: e.target.value })

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setErr(null)
    setLoading(true)
    try {
      const data = await api.auth.register(form.email, form.username, form.password)
      const token: string = data.token;
      localStorage.setItem('jwt', token)

      const payload = jwtDecode<MyJwtPayload>(token);

      const authUserId = payload.sub ?? payload.uid ?? payload.userId;
      if (!authUserId) throw new Error('JWT missing subject');

      await api.accounts.createAccount({
        authUserId,
        email: form.email,
        username: form.username,
      }, token);
      nav('/items')
    } catch (e: any) {
      setErr(e.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  const disabled = !form.email.trim() || !form.username.trim() || form.password.length < 6 || loading

  return (
    <div className="mx-auto max-w-md px-4 py-8">
      <h1 className="text-2xl font-semibold mb-6">Create account</h1>

      <form onSubmit={onSubmit} className="space-y-4">
        <div>
          <label className="block text-sm mb-1">Email</label>
          <input type="email" className="w-full border rounded px-3 py-2" value={form.email} onChange={onChange('email')} required />
        </div>

        <div>
          <label className="block text-sm mb-1">Username</label>
          <input type="text" className="w-full border rounded px-3 py-2" value={form.username} onChange={onChange('username')} required />
        </div>

        <div>
          <label className="block text-sm mb-1">Password</label>
          <div className="flex gap-2">
            <input
              type={showPw ? 'text' : 'password'}
              className="w-full border rounded px-3 py-2"
              value={form.password}
              onChange={onChange('password')}
              minLength={6}
              required
            />
            <button type="button" className="border rounded px-3" onClick={() => setShowPw((s) => !s)}>
              {showPw ? 'Hide' : 'Show'}
            </button>
          </div>
          <p className="text-xs text-gray-500 mt-1">At least 6 characters</p>
        </div>

        {err && <p className="text-sm text-red-600">{err}</p>}

        <button type="submit" disabled={disabled} className={`w-full rounded px-4 py-2 text-white ${disabled ? 'bg-gray-400' : 'bg-blue-600 hover:bg-blue-700'}`}>
          {loading ? 'Creating...' : 'Create account'}
        </button>

        <p className="text-sm">
          Already have an account? <Link to="/login" className="text-blue-600 underline">Log in</Link>
        </p>
      </form>
    </div>
  )
}