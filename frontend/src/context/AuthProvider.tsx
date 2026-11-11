import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, refreshSession, tokenStore } from '../lib/api'
import type { AuthResponse, User } from '../lib/types'
import { AuthContext, type RegisterInput } from './auth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [ready, setReady] = useState(false)

  useEffect(() => {
    let active = true
    refreshSession()
      .then((session) => {
        if (active) setUser(session?.user ?? null)
      })
      .finally(() => {
        if (active) setReady(true)
      })
    return () => {
      active = false
    }
  }, [])

  useEffect(() => {
    const expire = () => setUser(null)
    const refreshed = (event: Event) => setUser((event as CustomEvent<User>).detail)
    window.addEventListener('deskforge:session-expired', expire)
    window.addEventListener('deskforge:session-refreshed', refreshed)
    return () => {
      window.removeEventListener('deskforge:session-expired', expire)
      window.removeEventListener('deskforge:session-refreshed', refreshed)
    }
  }, [])

  const start = useCallback((response: AuthResponse) => {
    tokenStore.set(response.accessToken)
    setUser(response.user)
    return response.user
  }, [])

  const login = useCallback(
    async (email: string, password: string) =>
      start(await api.post<AuthResponse>('/api/auth/login', { email, password })),
    [start],
  )

  const register = useCallback(
    async (input: RegisterInput) => start(await api.post<AuthResponse>('/api/auth/register', input)),
    [start],
  )

  const logout = useCallback(async () => {
    await api.post<void>('/api/auth/logout').catch(() => undefined)
    tokenStore.clear()
    setUser(null)
  }, [])

  const logoutEverywhere = useCallback(async () => {
    await api.post<void>('/api/auth/logout-all')
  }, [])

  const value = useMemo(
    () => ({ user, ready, login, register, logout, logoutEverywhere, setUser }),
    [user, ready, login, register, logout, logoutEverywhere],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
