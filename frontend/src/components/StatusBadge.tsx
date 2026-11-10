import { statusLabel } from '../lib/format'
import type { OrderStatus } from '../lib/types'

const tone: Record<OrderStatus, string> = {
  AWAITING_PAYMENT: 'bg-[#fff4d6] text-warn',
  PAID: 'bg-[#e1e7ff] text-cobalt-deep',
  SHIPPED: 'bg-[#e1e7ff] text-cobalt-deep',
  DELIVERED: 'bg-[#dcf3e6] text-ok',
  CANCELED: 'bg-aluminum-deep text-slate',
}

export function StatusBadge({ status }: { status: OrderStatus }) {
  return (
    <span className={`inline-flex rounded-md px-2 py-0.5 text-xs font-semibold ${tone[status]}`}>
      {statusLabel[status]}
    </span>
  )
}
