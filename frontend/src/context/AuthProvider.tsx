import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api, tokenStore } from '../lib/api'
import type { AuthResponse, User } from '../lib/types'
import { AuthContext, type RegisterInput } from './auth'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [ready, setReady] = useState(() => !tokenStore.get())

  useEffect(() => {
    if (!tokenStore.get()) return
    api
      .get<User>('/api/me')
      .then(setUser)
      .catch(() => tokenStore.clear())
      .finally(() => setReady(true))
  }, [])

  useEffect(() => {
    const expire = () => setUser(null)
    window.addEventListener('deskforge:session-expired', expire)
    return () => window.removeEventListener('deskforge:session-expired', expire)
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

  const logout = useCallback(() => {
    tokenStore.clear()
    setUser(null)
  }, [])

  const value = useMemo(
    () => ({ user, ready, login, register, logout, setUser }),
    [user, ready, login, register, logout],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
