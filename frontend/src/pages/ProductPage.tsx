import { useQuery } from '@tanstack/react-query'
import { Check, ShieldCheck, Truck, Undo2 } from 'lucide-react'
import { useState } from 'react'
import { Link, useParams } from 'react-router'
import { Price } from '../components/Price'
import { ProductArt } from '../components/ProductArt'
import { ProductCard } from '../components/ProductCard'
import { QuantityStepper } from '../components/QuantityStepper'
import { MAX_PER_ITEM, useCart } from '../context/cart'
import { api } from '../lib/api'
import { formatPrice } from '../lib/format'
import { useStoreInfo } from '../lib/queries'
import type { ProductDetail } from '../lib/types'
import { NotFoundPage } from './NotFoundPage'

export function ProductPage() {
  const { slug } = useParams()
  const { add } = useCart()
  const { data: store } = useStoreInfo()
  const [quantity, setQuantity] = useState(1)
  const [added, setAdded] = useState(false)

  const product = useQuery({
    queryKey: ['product', slug],
    queryFn: () => api.get<ProductDetail>(`/api/products/${slug}`),
  })

  if (product.isPending) {
    return (
      <div className="mx-auto grid max-w-7xl gap-10 px-4 py-14 sm:px-6 lg:grid-cols-2" aria-busy>
        <div className="aspect-[4/3] animate-pulse rounded-2xl bg-aluminum-deep" />
        <div className="space-y-4">
          <div className="h-10 w-3/4 animate-pulse rounded bg-aluminum-deep" />
          <div className="h-24 animate-pulse rounded bg-aluminum-deep" />
        </div>
      </div>
    )
  }

  if (product.isError) return <NotFoundPage />

  const p = product.data
  const soldOut = p.stock === 0
  const max = Math.min(MAX_PER_ITEM, p.stock)

  const addToCart = () => {
    add({ ...p, categorySlug: p.category.slug, categoryName: p.category.name }, quantity)
    setAdded(true)
  }

  return (
    <div className="mx-auto max-w-7xl px-4 sm:px-6">
      <nav aria-label="Trilha" className="pt-8 text-sm text-slate">
        <ol className="flex flex-wrap gap-2">
          <li>
            <Link to="/produtos" className="hover:text-graphite hover:underline">
              Produtos
            </Link>
            <span aria-hidden className="ml-2">/</span>
          </li>
          <li>
            <Link to={`/produtos?categoria=${p.category.slug}`} className="hover:text-graphite hover:underline">
              {p.category.name}
            </Link>
            <span aria-hidden className="ml-2">/</span>
          </li>
          <li aria-current="page" className="text-graphite">
            {p.name}
          </li>
        </ol>
      </nav>

      <div className="mt-6 grid gap-10 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)] lg:gap-14">
        <div
          className="grid aspect-[4/3] place-items-center rounded-3xl"
          style={{ backgroundColor: `color-mix(in srgb, ${p.accentColor} 16%, #f6f7f8)` }}
        >
          <ProductArt art={p.art} accent={p.accentColor} title={`Ilustração do ${p.name}`} className="w-full p-10" />
        </div>

        <div>
          <h1 className="font-wide text-3xl font-extrabold leading-tight tracking-tight sm:text-4xl">{p.name}</h1>
          <p className="mt-3 text-lg text-slate">{p.shortDescription}</p>
          <div className="mt-6">
            <Price price={p.price} compareAtPrice={p.compareAtPrice} size="lg" />
          </div>

          <p className={`mt-4 text-sm font-medium ${soldOut ? 'text-danger' : p.stock <= 5 ? 'text-warn' : 'text-ok'}`}>
            {soldOut ? 'Esgotado no momento' : p.stock <= 5 ? `Restam ${p.stock} unidades em estoque` : 'Em estoque'}
          </p>

          {!soldOut ? (
            <div className="mt-6 flex flex-wrap items-center gap-3">
              <QuantityStepper value={quantity} max={max} onChange={setQuantity} label="Quantidade" />
              <button type="button" className="btn btn-primary flex-1 sm:flex-none" onClick={addToCart}>
                Adicionar ao carrinho
              </button>
            </div>
          ) : null}

          <div aria-live="polite">
            {added ? (
              <p className="mt-4 flex items-center gap-2 text-sm">
                <Check className="size-4 text-ok" aria-hidden />
                Adicionado ao carrinho.
                <Link to="/carrinho" className="font-semibold text-cobalt underline underline-offset-4">
                  Ir para o carrinho
                </Link>
              </p>
            ) : null}
          </div>

          {store ? (
            <ul className="mt-8 space-y-3 border-t border-line pt-6 text-sm">
              <li className="flex gap-3">
                <Truck className="mt-0.5 size-5 shrink-0 text-slate" aria-hidden />
                <span>
                  {p.price >= store.freeShippingThreshold
                    ? 'Frete grátis para todo o Brasil.'
                    : `Frete de ${formatPrice(store.flatShippingCost)}, grátis em pedidos acima de ${formatPrice(store.freeShippingThreshold)}.`}
                </span>
              </li>
              <li className="flex gap-3">
                <Undo2 className="mt-0.5 size-5 shrink-0 text-slate" aria-hidden />
                <span>
                  {store.withdrawalDays} dias para desistir após o recebimento.{' '}
                  <Link to="/reembolso" className="text-cobalt underline underline-offset-4">
                    Política de reembolso
                  </Link>
                </span>
              </li>
              <li className="flex gap-3">
                <ShieldCheck className="mt-0.5 size-5 shrink-0 text-slate" aria-hidden />
                <span>Garantia legal de 90 dias contra defeitos.</span>
              </li>
            </ul>
          ) : null}
        </div>
      </div>

      <div className="mt-16 grid gap-12 lg:grid-cols-[minmax(0,7fr)_minmax(0,5fr)] lg:gap-14">
        <section aria-labelledby="about-title">
          <h2 id="about-title" className="font-wide text-xl font-bold">
            Sobre o produto
          </h2>
          <p className="mt-4 max-w-prose leading-relaxed text-[#2f343c]">{p.description}</p>
        </section>
        <section aria-labelledby="specs-title">
          <h2 id="specs-title" className="font-wide text-xl font-bold">
            Especificações
          </h2>
          <dl className="mt-4 divide-y divide-line border-y border-line text-sm">
            {p.specs.map((spec) => (
              <div key={spec.label} className="grid grid-cols-[9rem_1fr] gap-4 py-3">
                <dt className="text-slate">{spec.label}</dt>
                <dd className="font-medium">{spec.value}</dd>
              </div>
            ))}
            <div className="grid grid-cols-[9rem_1fr] gap-4 py-3">
              <dt className="text-slate">Código (SKU)</dt>
              <dd className="font-medium">{p.sku}</dd>
            </div>
          </dl>
          <p className="mt-3 text-xs text-slate">Imagem ilustrativa. Especificações de produto fictício.</p>
        </section>
      </div>

      {p.related.length > 0 ? (
        <section aria-labelledby="related-title" className="mt-20">
          <h2 id="related-title" className="font-wide text-xl font-bold">
            Da mesma categoria
          </h2>
          <div className="mt-6 grid grid-cols-2 gap-x-5 gap-y-10 md:grid-cols-4">
            {p.related.map((related) => (
              <ProductCard key={related.id} product={related} />
            ))}
          </div>
        </section>
      ) : null}
    </div>
  )
}
