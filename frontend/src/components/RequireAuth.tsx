import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router'
import { useAuth } from '../context/auth'
import type { Role } from '../lib/types'

export function RequireAuth({ children, role }: { children: ReactNode; role?: Role }) {
  const { user, ready } = useAuth()
  const location = useLocation()

  if (!ready) {
    return <p className="mx-auto max-w-7xl px-6 py-20 text-slate">Carregando sua conta…</p>
  }
  if (!user) {
    return <Navigate to={`/entrar?voltar=${encodeURIComponent(location.pathname + location.search)}`} replace />
  }
  if (role && user.role !== role) {
    return <Navigate to="/" replace />
  }
  return children
}
