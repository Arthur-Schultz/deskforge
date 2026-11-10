import { readConsent } from './consent'

let enabled = false

export function syncAnalytics() {
  enabled = readConsent()?.analytics === true
}

export function track(event: string, data: Record<string, unknown> = {}) {
  if (!enabled) return
  if (import.meta.env.DEV) {
    console.info('[analytics]', event, data)
  }
}

window.addEventListener('deskforge:consent', syncAnalytics)
syncAnalytics()
