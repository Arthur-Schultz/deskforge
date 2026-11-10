import type { StoreInfo } from './types'

export function summarize(subtotal: number, store?: StoreInfo) {
  if (!store) return { shipping: 0, total: subtotal, taxes: 0, missingForFreeShipping: 0 }
  const shipping = subtotal >= store.freeShippingThreshold || subtotal === 0 ? 0 : store.flatShippingCost
  const total = subtotal + shipping
  return {
    shipping,
    total,
    taxes: Math.round(total * store.approximateTaxRate * 100) / 100,
    missingForFreeShipping: Math.max(0, store.freeShippingThreshold - subtotal),
  }
}
