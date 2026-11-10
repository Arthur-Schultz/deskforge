import { createContext, use } from 'react'
import type { User } from '../lib/types'

export interface RegisterInput {
  fullName: string
  email: string
  password: string
  acceptTerms: boolean
  marketingConsent: boolean
}

export interface AuthState {
  user: User | null
  ready: boolean
  login: (email: string, password: string) => Promise<User>
  register: (input: RegisterInput) => Promise<User>
  logout: () => void
  setUser: (user: User) => void
}

export const AuthContext = createContext<AuthState | null>(null)

export function useAuth() {
  const context = use(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
