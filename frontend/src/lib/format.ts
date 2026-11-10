import type { OrderStatus } from './types'

const currency = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dateTime = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
const date = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'long' })

export const formatPrice = (value: number) => currency.format(value)
export const formatDateTime = (iso: string) => dateTime.format(new Date(iso))
export const formatDate = (iso: string) => date.format(new Date(iso))

export const discountPercent = (price: number, compareAt?: number) =>
  compareAt ? Math.round((1 - price / compareAt) * 100) : 0

export const statusLabel: Record<OrderStatus, string> = {
  AWAITING_PAYMENT: 'Aguardando pagamento',
  PAID: 'Pago',
  SHIPPED: 'Enviado',
  DELIVERED: 'Entregue',
  CANCELED: 'Cancelado',
}

export const maskZip = (value: string) => {
  const digits = value.replace(/\D/g, '').slice(0, 8)
  return digits.length > 5 ? `${digits.slice(0, 5)}-${digits.slice(5)}` : digits
}
