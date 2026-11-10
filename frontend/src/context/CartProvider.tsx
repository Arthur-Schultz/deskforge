import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { track } from '../lib/analytics'
import type { ProductSummary } from '../lib/types'
import { CartContext, MAX_PER_ITEM, type CartLine } from './cart'

const STORAGE_KEY = 'deskforge.cart'
function load(): CartLine[] {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '[]') as CartLine[]
  } catch {
    return []
  }
}

const limit = (line: Pick<CartLine, 'stock'>, quantity: number) =>
  Math.max(1, Math.min(quantity, MAX_PER_ITEM, line.stock))

export function CartProvider({ children }: { children: ReactNode }) {
  const [lines, setLines] = useState<CartLine[]>(load)

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(lines))
  }, [lines])

  const add = useCallback((product: ProductSummary, quantity = 1) => {
    track('add_to_cart', { sku: product.sku, quantity })
    setLines((current) => {
      const existing = current.find((line) => line.productId === product.id)
      if (existing) {
        return current.map((line) =>
          line.productId === product.id ? { ...line, quantity: limit(line, line.quantity + quantity) } : line,
        )
      }
      const line: CartLine = {
        productId: product.id,
        slug: product.slug,
        name: product.name,
        price: product.price,
        art: product.art,
        accentColor: product.accentColor,
        stock: product.stock,
        quantity: 1,
      }
      return [...current, { ...line, quantity: limit(line, quantity) }]
    })
  }, [])

  const setQuantity = useCallback((productId: number, quantity: number) => {
    setLines((current) =>
      current.map((line) => (line.productId === productId ? { ...line, quantity: limit(line, quantity) } : line)),
    )
  }, [])

  const remove = useCallback((productId: number) => {
    setLines((current) => current.filter((line) => line.productId !== productId))
  }, [])

  const clear = useCallback(() => setLines([]), [])

  const value = useMemo(() => {
    const count = lines.reduce((sum, line) => sum + line.quantity, 0)
    const subtotal = lines.reduce((sum, line) => sum + line.price * line.quantity, 0)
    return { lines, count, subtotal, add, setQuantity, remove, clear }
  }, [lines, add, setQuantity, remove, clear])

  return <CartContext value={value}>{children}</CartContext>
}
