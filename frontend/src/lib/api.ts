import type { AuthResponse } from './types'

const BASE_URL = import.meta.env.VITE_API_URL ?? ''

export class ApiError extends Error {
  readonly status: number
  readonly fieldErrors: Record<string, string>

  constructor(status: number, message: string, fieldErrors: Record<string, string> = {}) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

let accessToken: string | null = null
let pendingRefresh: Promise<AuthResponse | null> | null = null

export const tokenStore = {
  get: (): string | null => accessToken,
  set: (token: string) => {
    accessToken = token
  },
  clear: () => {
    accessToken = null
  },
}

export function refreshSession(): Promise<AuthResponse | null> {
  pendingRefresh ??= fetch(`${BASE_URL}/api/auth/refresh`, {
    method: 'POST',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  })
    .then(async (response) => {
      if (!response.ok) {
        tokenStore.clear()
        return null
      }
      const session = (await response.json()) as AuthResponse
      tokenStore.set(session.accessToken)
      return session
    })
    .catch(() => null)
    .finally(() => {
      pendingRefresh = null
    })
  return pendingRefresh
}

type Method = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'

async function send(method: Method, path: string, body?: unknown): Promise<Response> {
  const headers: Record<string, string> = { Accept: 'application/json' }
  const token = tokenStore.get()
  if (token) headers.Authorization = `Bearer ${token}`
  if (body !== undefined) headers['Content-Type'] = 'application/json'
  try {
    return await fetch(`${BASE_URL}${path}`, {
      method,
      headers,
      credentials: 'include',
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar à loja. Verifique sua conexão e tente novamente.')
  }
}

async function request<T>(method: Method, path: string, body?: unknown): Promise<T> {
  const hadToken = tokenStore.get() !== null
  let response = await send(method, path, body)

  if (response.status === 401 && hadToken && !path.startsWith('/api/auth/')) {
    const session = await refreshSession()
    if (session) {
      window.dispatchEvent(new CustomEvent('deskforge:session-refreshed', { detail: session.user }))
      response = await send(method, path, body)
    } else {
      window.dispatchEvent(new Event('deskforge:session-expired'))
    }
  }

  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new ApiError(
      response.status,
      problem?.detail ?? 'Algo deu errado. Tente novamente em instantes.',
      problem?.errors ?? {},
    )
  }

  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  get: <T>(path: string) => request<T>('GET', path),
  post: <T>(path: string, body?: unknown) => request<T>('POST', path, body ?? {}),
  put: <T>(path: string, body: unknown) => request<T>('PUT', path, body),
  patch: <T>(path: string, body: unknown) => request<T>('PATCH', path, body),
  delete: <T>(path: string) => request<T>('DELETE', path),
}
