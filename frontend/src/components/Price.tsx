import { discountPercent, formatPrice } from '../lib/format'

interface Props {
  price: number
  compareAtPrice?: number
  size?: 'sm' | 'lg'
}

export function Price({ price, compareAtPrice, size = 'sm' }: Props) {
  const off = discountPercent(price, compareAtPrice)
  return (
    <div className="tabular">
      {compareAtPrice ? (
        <p className="text-sm text-slate">
          <span className="sr-only">Preço anterior: </span>
          <s>{formatPrice(compareAtPrice)}</s>
          <span className="ml-2 font-semibold text-ok">{off}% off</span>
        </p>
      ) : null}
      <p className={size === 'lg' ? 'font-wide text-3xl font-bold' : 'text-lg font-bold'}>
        <span className="sr-only">Preço: </span>
        {formatPrice(price)}
      </p>
    </div>
  )
}
