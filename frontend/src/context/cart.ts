import { createContext, use } from 'react'
import type { ArtKind, ProductSummary } from '../lib/types'

export interface CartLine {
  productId: number
  slug: string
  name: string
  price: number
  art: ArtKind
  accentColor: string
  stock: number
  quantity: number
}

export interface CartState {
  lines: CartLine[]
  count: number
  subtotal: number
  add: (product: ProductSummary, quantity?: number) => void
  setQuantity: (productId: number, quantity: number) => void
  remove: (productId: number) => void
  clear: () => void
}

export const MAX_PER_ITEM = 10

export const CartContext = createContext<CartState | null>(null)

export function useCart() {
  const context = use(CartContext)
  if (!context) throw new Error('useCart must be used inside CartProvider')
  return context
}
