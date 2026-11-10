import { formatPrice } from '../lib/format'

interface Props {
  subtotal: number
  shipping: number
  total: number
  taxes: number
}

export function OrderSummary({ subtotal, shipping, total, taxes }: Props) {
  return (
    <dl className="tabular space-y-2 text-sm">
      <div className="flex justify-between">
        <dt className="text-slate">Subtotal</dt>
        <dd>{formatPrice(subtotal)}</dd>
      </div>
      <div className="flex justify-between">
        <dt className="text-slate">Frete</dt>
        <dd>{shipping === 0 ? 'Grátis' : formatPrice(shipping)}</dd>
      </div>
      <div className="flex justify-between border-t border-line pt-3 text-base font-bold">
        <dt>Total</dt>
        <dd>{formatPrice(total)}</dd>
      </div>
      <div className="flex justify-between text-xs text-slate">
        <dt>Tributos aproximados incluídos (Lei 12.741/2012)</dt>
        <dd>{formatPrice(taxes)}</dd>
      </div>
    </dl>
  )
}
