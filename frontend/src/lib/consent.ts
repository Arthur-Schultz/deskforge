import { api } from './api'

const STORAGE_KEY = 'deskforge.cookie-consent'
const VISITOR_KEY = 'deskforge.visitor'

export interface ConsentChoice {
  analytics: boolean
  marketing: boolean
  policyVersion: string
  recordedAt: string
}

export function readConsent(): ConsentChoice | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as ConsentChoice) : null
  } catch {
    return null
  }
}

function visitorId(): string {
  let id = localStorage.getItem(VISITOR_KEY)
  if (!id) {
    id = crypto.randomUUID()
    localStorage.setItem(VISITOR_KEY, id)
  }
  return id
}

export async function saveConsent(analytics: boolean, marketing: boolean, policyVersion: string) {
  const choice: ConsentChoice = { analytics, marketing, policyVersion, recordedAt: new Date().toISOString() }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(choice))
  window.dispatchEvent(new CustomEvent('deskforge:consent', { detail: choice }))
  await api.post('/api/privacy/consents', { visitorId: visitorId(), analytics, marketing }).catch(() => undefined)
  return choice
}

export function reopenConsent() {
  window.dispatchEvent(new Event('deskforge:open-consent'))
}
