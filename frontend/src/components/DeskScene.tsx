import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router'
import { formatPrice } from '../lib/format'
import type { ArtKind, ProductSummary } from '../lib/types'
import { ProductArt } from './ProductArt'

interface SceneItem {
  slug: string
  fallbackName: string
  art: ArtKind
  accent: string
  x: number
  y: number
  width: number
  rotate: number
}

const items: SceneItem[] = [
  { slug: 'quench-pro-wireless', fallbackName: 'Quench Pro Wireless', art: 'HEADSET', accent: '#FF6B2C', x: 34, y: 58, width: 250, rotate: -9 },
  { slug: 'bellows-usb-cardioid', fallbackName: 'Bellows USB Cardioid', art: 'MICROPHONE', accent: '#FF6B2C', x: 520, y: 40, width: 200, rotate: 14 },
  { slug: 'anvil-75-hall-effect', fallbackName: 'Anvil 75 Hall Effect', art: 'KEYBOARD', accent: '#2747E0', x: 120, y: 190, width: 430, rotate: -2 },
  { slug: 'ember-ultralight-x', fallbackName: 'Ember Ultralight X', art: 'MOUSE', accent: '#2747E0', x: 548, y: 236, width: 180, rotate: 8 },
]

const MAT_SLUG = 'slag-xxl-control'

export function DeskScene({ products }: { products: ProductSummary[] }) {
  const navigate = useNavigate()
  const [active, setActive] = useState<string | null>(null)
  const bySlug = useMemo(() => new Map(products.map((p) => [p.slug, p])), [products])

  const describe = (slug: string, fallback: string) => {
    const product = bySlug.get(slug)
    return product ? `${product.name}, ${formatPrice(product.price)}` : fallback
  }

  const activeProduct = active ? bySlug.get(active) : null
  const activeName = active
    ? (activeProduct?.name ?? items.find((i) => i.slug === active)?.fallbackName ?? 'Slag XXL Control')
    : null

  const interactive = (slug: string, label: string) => ({
    role: 'link',
    tabIndex: 0,
    'aria-label': `${label}. Abrir produto`,
    onClick: () => navigate(`/produtos/${slug}`),
    onKeyDown: (event: React.KeyboardEvent) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault()
        navigate(`/produtos/${slug}`)
      }
    },
    onMouseEnter: () => setActive(slug),
    onMouseLeave: () => setActive(null),
    onFocus: () => setActive(slug),
    onBlur: () => setActive(null),
    style: { cursor: 'pointer', outline: 'none' },
  })

  return (
    <figure className="relative">
      <svg viewBox="0 0 760 520" className="w-full drop-shadow-[0_30px_40px_rgba(28,31,36,0.18)]">
        <title>Setup de mesa visto de cima com headset, microfone, teclado e mouse da DeskForge</title>
        <g {...interactive(MAT_SLUG, describe(MAT_SLUG, 'Desk mat Slag XXL Control'))}>
          <rect x={14} y={24} width={732} height={472} rx={30} fill="#2e3a34" />
          <rect
            x={26}
            y={36}
            width={708}
            height={448}
            rx={22}
            fill="none"
            stroke={active === MAT_SLUG ? '#ffffff' : '#56685e'}
            strokeWidth={2}
            strokeDasharray="7 6"
            className="transition-[stroke] duration-200"
          />
          <text x={700} y={468} textAnchor="end" fill="#56685e" fontSize={13} fontWeight={700} style={{ fontStretch: '125%' }}>
            DESKFORGE
          </text>
        </g>
        {items.map((item) => {
          const height = item.width * 0.75
          const cx = item.x + item.width / 2
          const cy = item.y + height / 2
          const isActive = active === item.slug
          return (
            <g
              key={item.slug}
              {...interactive(item.slug, describe(item.slug, item.fallbackName))}
              transform={`rotate(${item.rotate} ${cx} ${cy})`}
            >
              <g
                className="transition-[transform,filter] duration-200 ease-out motion-reduce:transition-none"
                style={{
                  transform: isActive ? 'translateY(-8px)' : 'none',
                  filter: isActive
                    ? 'drop-shadow(0 0 1.5px #fff) drop-shadow(0 0 1.5px #fff) drop-shadow(0 16px 12px rgba(0,0,0,0.45))'
                    : 'drop-shadow(0 4px 4px rgba(0,0,0,0.25))',
                }}
              >
                <svg x={item.x} y={item.y} width={item.width} height={height} viewBox="0 0 400 300" overflow="visible">
                  <ProductArt art={item.art} accent={item.accent} />
                </svg>
              </g>
            </g>
          )
        })}
      </svg>
      <figcaption className="mt-4 flex min-h-12 items-center justify-between gap-4 text-sm" aria-live="polite">
        {activeName ? (
          <>
            <span className="font-semibold">{activeName}</span>
            {activeProduct ? <span className="tabular font-semibold">{formatPrice(activeProduct.price)}</span> : null}
          </>
        ) : (
          <span className="text-slate">Passe o mouse ou use Tab para ver cada item da mesa. Clique para abrir.</span>
        )}
      </figcaption>
    </figure>
  )
}
