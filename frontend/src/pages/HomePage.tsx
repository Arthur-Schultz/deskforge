import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { DeskScene } from '../components/DeskScene'
import { ProductCard, ProductGridSkeleton } from '../components/ProductCard'
import { api } from '../lib/api'
import { formatPrice } from '../lib/format'
import { useCategories, useStoreInfo } from '../lib/queries'
import type { ProductSummary } from '../lib/types'

export function HomePage() {
  const { data: store } = useStoreInfo()
  const { data: categories } = useCategories()
  const featured = useQuery({
    queryKey: ['products', 'featured'],
    queryFn: () => api.get<ProductSummary[]>('/api/products/featured'),
  })

  return (
    <>
      <section className="mx-auto grid max-w-7xl items-center gap-12 px-4 pb-16 pt-10 sm:px-6 lg:grid-cols-[minmax(0,5fr)_minmax(0,7fr)] lg:pt-16">
        <div>
          <h1 className="font-wide text-4xl font-extrabold leading-[1.05] tracking-tight sm:text-5xl xl:text-6xl">
            Monte uma mesa que trabalha a seu favor.
          </h1>
          <p className="mt-6 max-w-md text-lg leading-relaxed text-slate">
            Teclados, mouses, headsets e acessórios escolhidos para longas horas de foco, de código ou de jogo.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link to="/produtos" className="btn btn-primary">
              Ver todos os produtos
            </Link>
            <Link to="/produtos?ofertas=1" className="btn btn-secondary">
              Ver ofertas
            </Link>
          </div>
          {store ? (
            <ul className="mt-10 grid max-w-md gap-3 text-sm sm:grid-cols-3 sm:gap-6">
              <li>
                <strong className="block font-semibold">Frete grátis</strong>
                <span className="text-slate">acima de {formatPrice(store.freeShippingThreshold)}</span>
              </li>
              <li>
                <strong className="block font-semibold">{store.withdrawalDays} dias</strong>
                <span className="text-slate">para desistir da compra</span>
              </li>
              <li>
                <strong className="block font-semibold">90 dias</strong>
                <span className="text-slate">de garantia legal</span>
              </li>
            </ul>
          ) : null}
        </div>
        <DeskScene products={featured.data ?? []} />
      </section>

      <section aria-labelledby="categories-title" className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <h2 id="categories-title" className="font-wide text-2xl font-bold tracking-tight">
          Comece pela categoria
        </h2>
        <ul className="mt-6 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-7">
          {categories?.map((category) => (
            <li key={category.slug}>
              <Link
                to={`/produtos?categoria=${category.slug}`}
                className="keycap flex h-full min-h-28 flex-col justify-between p-4"
              >
                <span className="font-wide text-sm font-bold">{category.name}</span>
                <span className="mt-3 line-clamp-3 text-xs leading-snug text-slate">{category.description}</span>
              </Link>
            </li>
          ))}
        </ul>
      </section>

      <section aria-labelledby="featured-title" className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
        <div className="flex items-end justify-between gap-4">
          <h2 id="featured-title" className="font-wide text-2xl font-bold tracking-tight">
            Em destaque
          </h2>
          <Link to="/produtos" className="text-sm font-semibold text-cobalt underline-offset-4 hover:underline">
            Ver catálogo completo
          </Link>
        </div>
        <div className="mt-8">
          {featured.isPending ? (
            <ProductGridSkeleton />
          ) : featured.isError ? (
            <p role="alert" className="text-danger">
              Não foi possível carregar os destaques. Recarregue a página para tentar de novo.
            </p>
          ) : (
            <div className="grid grid-cols-2 gap-x-5 gap-y-10 md:grid-cols-3 lg:grid-cols-4">
              {featured.data.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}
        </div>
      </section>

      {store ? (
        <section aria-labelledby="promise-title" className="mx-auto max-w-7xl px-4 py-12 sm:px-6">
          <div className="rounded-3xl bg-felt px-6 py-10 text-[#e4ebe7] sm:px-10 sm:py-12">
            <h2 id="promise-title" className="font-wide text-2xl font-bold tracking-tight text-white">
              Compra sem letras miúdas
            </h2>
            <div className="mt-8 grid gap-8 md:grid-cols-3">
              <div>
                <h3 className="font-semibold text-white">Preço final antes de pagar</h3>
                <p className="mt-2 text-sm leading-relaxed">
                  Produtos, frete e tributos aproximados aparecem no resumo antes da confirmação. Nada é somado
                  depois.
                </p>
              </div>
              <div>
                <h3 className="font-semibold text-white">Desistência em {store.withdrawalDays} dias</h3>
                <p className="mt-2 text-sm leading-relaxed">
                  Mudou de ideia? Cancele pela sua conta em até {store.withdrawalDays} dias após receber, com
                  reembolso integral.{' '}
                  <Link to="/reembolso" className="text-white underline underline-offset-4">
                    Como funciona
                  </Link>
                </p>
              </div>
              <div>
                <h3 className="font-semibold text-white">Garantia de 90 dias</h3>
                <p className="mt-2 text-sm leading-relaxed">
                  Se o produto apresentar defeito, a troca ou o reembolso são resolvidos pelo SAC, com protocolo e prazo
                  de resposta.
                </p>
              </div>
            </div>
          </div>
        </section>
      ) : null}
    </>
  )
}
