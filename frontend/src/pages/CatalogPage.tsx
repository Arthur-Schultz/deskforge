import { keepPreviousData, useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router'
import { PageHeader } from '../components/PageHeader'
import { ProductCard, ProductGridSkeleton } from '../components/ProductCard'
import { api } from '../lib/api'
import { useCategories } from '../lib/queries'
import type { Page, ProductSummary } from '../lib/types'

const sorts = [
  { value: 'FEATURED', label: 'Destaques' },
  { value: 'PRICE_ASC', label: 'Menor preço' },
  { value: 'PRICE_DESC', label: 'Maior preço' },
  { value: 'NEWEST', label: 'Novidades' },
  { value: 'NAME', label: 'Nome' },
]

const PAGE_SIZE = 12

export function CatalogPage() {
  const [params, setParams] = useSearchParams()
  const { data: categories } = useCategories()

  const category = params.get('categoria') ?? ''
  const q = params.get('q') ?? ''
  const sort = params.get('ordem') ?? 'FEATURED'
  const onSale = params.get('ofertas') === '1'
  const inStock = params.get('estoque') === '1'
  const page = Number(params.get('pagina') ?? '0')

  const query = new URLSearchParams({ sort, page: String(page), size: String(PAGE_SIZE) })
  if (category) query.set('category', category)
  if (q) query.set('q', q)
  if (onSale) query.set('onSale', 'true')
  if (inStock) query.set('inStock', 'true')

  const products = useQuery({
    queryKey: ['products', query.toString()],
    queryFn: () => api.get<Page<ProductSummary>>(`/api/products?${query}`),
    placeholderData: keepPreviousData,
  })

  const update = (changes: Record<string, string | null>) => {
    const next = new URLSearchParams(params)
    Object.entries(changes).forEach(([key, value]) => (value ? next.set(key, value) : next.delete(key)))
    if (!('pagina' in changes)) next.delete('pagina')
    setParams(next)
  }

  const current = categories?.find((c) => c.slug === category)
  const title = q ? `Resultados para “${q}”` : (current?.name ?? (onSale ? 'Ofertas' : 'Todos os produtos'))

  return (
    <>
      <PageHeader title={title}>{current?.description}</PageHeader>
      <div className="mx-auto max-w-7xl px-4 sm:px-6">
        <div className="flex flex-col gap-4 border-y border-line py-4 md:flex-row md:items-center md:justify-between">
          <fieldset className="flex flex-wrap items-center gap-x-6 gap-y-2">
            <legend className="sr-only">Filtros</legend>
            <label className="flex items-center gap-2 text-sm">
              <span className="font-semibold">Categoria</span>
              <select
                value={category}
                onChange={(event) => update({ categoria: event.target.value || null })}
                className="field w-auto py-2"
              >
                <option value="">Todas</option>
                {categories?.map((c) => (
                  <option key={c.slug} value={c.slug}>
                    {c.name}
                  </option>
                ))}
              </select>
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                className="size-4 accent-cobalt"
                checked={onSale}
                onChange={(event) => update({ ofertas: event.target.checked ? '1' : null })}
              />
              Só ofertas
            </label>
            <label className="flex items-center gap-2 text-sm">
              <input
                type="checkbox"
                className="size-4 accent-cobalt"
                checked={inStock}
                onChange={(event) => update({ estoque: event.target.checked ? '1' : null })}
              />
              Só com estoque
            </label>
          </fieldset>
          <label className="flex items-center gap-2 text-sm">
            <span className="font-semibold">Ordenar por</span>
            <select value={sort} onChange={(event) => update({ ordem: event.target.value })} className="field w-auto py-2">
              {sorts.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <p className="mt-6 text-sm text-slate" aria-live="polite">
          {products.data ? `${products.data.totalElements} ${products.data.totalElements === 1 ? 'produto' : 'produtos'}` : ''}
        </p>

        <div className="mt-4">
          {products.isPending ? (
            <ProductGridSkeleton count={PAGE_SIZE} />
          ) : products.isError ? (
            <p role="alert" className="py-16 text-danger">
              {products.error.message}
            </p>
          ) : products.data.content.length === 0 ? (
            <div className="py-16">
              <p className="font-wide text-xl font-bold">Nenhum produto com esses filtros.</p>
              <p className="mt-2 text-slate">Tente remover um filtro ou buscar por outro termo.</p>
              <button type="button" className="btn btn-secondary mt-6" onClick={() => setParams(new URLSearchParams())}>
                Limpar filtros
              </button>
            </div>
          ) : (
            <div
              className={`grid grid-cols-2 gap-x-5 gap-y-10 md:grid-cols-3 lg:grid-cols-4 ${products.isPlaceholderData ? 'opacity-60' : ''}`}
            >
              {products.data.content.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          )}
        </div>

        {products.data && products.data.totalPages > 1 ? (
          <nav aria-label="Paginação" className="mt-12 flex items-center justify-center gap-2">
            {Array.from({ length: products.data.totalPages }, (_, i) => (
              <button
                key={i}
                type="button"
                onClick={() => update({ pagina: i === 0 ? null : String(i) })}
                aria-current={i === page ? 'page' : undefined}
                className={`tabular grid size-10 place-items-center rounded-lg text-sm font-semibold ${i === page ? 'bg-graphite text-white' : 'hover:bg-aluminum-deep'}`}
              >
                <span className="sr-only">Página </span>
                {i + 1}
              </button>
            ))}
          </nav>
        ) : null}
      </div>
    </>
  )
}
