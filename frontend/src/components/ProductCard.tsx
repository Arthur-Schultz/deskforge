import { Link } from 'react-router'
import type { ProductSummary } from '../lib/types'
import { Price } from './Price'
import { ProductArt } from './ProductArt'

export function ProductCard({ product }: { product: ProductSummary }) {
  const soldOut = product.stock === 0
  return (
    <article className="group relative flex flex-col">
      <div
        className="relative aspect-[4/3] overflow-hidden rounded-xl"
        style={{ backgroundColor: `color-mix(in srgb, ${product.accentColor} 14%, #f6f7f8)` }}
      >
        <ProductArt
          art={product.art}
          accent={product.accentColor}
          className="h-full w-full p-6 transition-transform duration-300 group-hover:scale-[1.04] motion-reduce:transition-none"
        />
        {soldOut ? (
          <span className="absolute left-3 top-3 rounded-md bg-graphite px-2 py-1 text-xs font-semibold text-white">
            Esgotado
          </span>
        ) : null}
      </div>
      <div className="mt-4 flex flex-1 flex-col gap-1">
        <p className="text-sm text-slate">{product.categoryName}</p>
        <h3 className="text-base font-semibold leading-snug">
          <Link to={`/produtos/${product.slug}`} className="after:absolute after:inset-0 focus-visible:outline-none">
            {product.name}
          </Link>
        </h3>
        <p className="line-clamp-2 text-sm text-slate">{product.shortDescription}</p>
        <div className="mt-auto pt-3">
          <Price price={product.price} compareAtPrice={product.compareAtPrice} />
        </div>
      </div>
    </article>
  )
}

export function ProductGridSkeleton({ count = 8 }: { count?: number }) {
  return (
    <div className="grid grid-cols-2 gap-x-5 gap-y-10 md:grid-cols-3 lg:grid-cols-4" aria-hidden>
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className="animate-pulse">
          <div className="aspect-[4/3] rounded-xl bg-aluminum-deep" />
          <div className="mt-4 h-4 w-1/3 rounded bg-aluminum-deep" />
          <div className="mt-2 h-5 w-2/3 rounded bg-aluminum-deep" />
          <div className="mt-4 h-6 w-1/2 rounded bg-aluminum-deep" />
        </div>
      ))}
    </div>
  )
}
