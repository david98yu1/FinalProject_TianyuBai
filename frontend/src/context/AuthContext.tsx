import React, { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { api, parseJwtClaims } from '../api/client'

type User = {
  id: number | null
  email?: string | null
  username?: string | null
  roles?: string[] | string | null
}

type AuthContextType = {
  user: User | null
  token: string | null
  login: (login: string, password: string) => Promise<void>
  logout: () => void
  refreshMe: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [token, setToken] = useState<string | null>(localStorage.getItem('jwt'))
  const [user, setUser] = useState<User | null>(null)

  useEffect(() => {
    if (token) {
      localStorage.setItem('jwt', token)
      refreshMe()
    } else {
      localStorage.removeItem('jwt')
      setUser(null)
    }
  }, [token])

  async function refreshMe() {
    const t = localStorage.getItem('jwt')
    const claims = parseJwtClaims(t)
    let id: number | null = null
    let username: string | null = null
    let email: string | null = null
    let roles: any = null

    if (claims) {
      // Common fields: id, userId, sub, username, email, roles
      if (typeof claims.id === 'number') id = claims.id
      else if (typeof claims.userId === 'number') id = claims.userId
      else if (typeof claims.sub === 'string' && /^\d+$/.test(claims.sub)) id = Number(claims.sub)

      username = (claims.username ?? claims.name ?? null) as any
      email = (claims.email ?? null) as any
      roles = (claims.roles ?? claims.authorities ?? null)
    }

    // If we have an id, fetch full account details from /accounts/{id}
    if (id != null) {
      try {
        const acc = await api.accounts.getById(id)
        setUser({ id, username: acc.username ?? username, email: acc.email ?? email, roles: acc.roles ?? roles })
        return
      } catch {
        // fall through with claims only
      }
    }

    setUser({ id, username, email, roles })
  }

  async function login(login: string, password: string) {
    const res = await api.auth.login(login, password)
    setToken(res.token)
  }

  function logout() {
    setToken(null)
  }

  const value = useMemo(() => ({ user, token, login, logout, refreshMe }), [user, token])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}