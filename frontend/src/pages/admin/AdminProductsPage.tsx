import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useState } from 'react'
import { ProductArt } from '../../components/ProductArt'
import { api } from '../../lib/api'
import { formatPrice } from '../../lib/format'
import type { Page, ProductSummary } from '../../lib/types'

function StockEditor({ product }: { product: ProductSummary }) {
  const queryClient = useQueryClient()
  const [value, setValue] = useState(String(product.stock))
  const save = useMutation({
    mutationFn: () => api.patch(`/api/admin/products/${product.id}/stock`, { stock: Number(value) }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin'] }),
  })
  const changed = Number(value) !== product.stock

  return (
    <form
      className="flex items-center gap-2"
      onSubmit={(event) => {
        event.preventDefault()
        save.mutate()
      }}
    >
      <label className="sr-only" htmlFor={`stock-${product.id}`}>
        Estoque de {product.name}
      </label>
      <input
        id={`stock-${product.id}`}
        type="number"
        min={0}
        value={value}
        onChange={(event) => setValue(event.target.value)}
        className="field tabular w-20 py-1.5"
      />
      {changed ? (
        <button type="submit" className="btn btn-primary px-3 py-1.5 text-sm" disabled={save.isPending}>
          Salvar
        </button>
      ) : null}
      {save.error ? <span role="alert" className="text-xs text-danger">{save.error.message}</span> : null}
    </form>
  )
}

export function AdminProductsPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const products = useQuery({
    queryKey: ['admin', 'products', search],
    queryFn: () => api.get<Page<ProductSummary>>(`/api/admin/products?size=100${search ? `&q=${encodeURIComponent(search)}` : ''}`),
  })

  const toggle = useMutation({
    mutationFn: (product: ProductSummary) =>
      product.active
        ? api.delete(`/api/admin/products/${product.id}/activation`)
        : api.post(`/api/admin/products/${product.id}/activation`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['admin'] })
      queryClient.invalidateQueries({ queryKey: ['products'] })
    },
  })

  return (
    <div>
      <label htmlFor="admin-search" className="sr-only">
        Buscar produto
      </label>
      <input
        id="admin-search"
        type="search"
        placeholder="Buscar por nome"
        value={search}
        onChange={(event) => setSearch(event.target.value)}
        className="field max-w-sm"
      />
      <div className="mt-6 overflow-x-auto">
        <table className="w-full min-w-[46rem] text-left text-sm">
          <caption className="sr-only">Produtos cadastrados</caption>
          <thead className="border-b border-line text-slate">
            <tr>
              <th scope="col" className="py-3 pr-4 font-medium">Produto</th>
              <th scope="col" className="py-3 pr-4 font-medium">Categoria</th>
              <th scope="col" className="py-3 pr-4 text-right font-medium">Preço</th>
              <th scope="col" className="py-3 pr-4 font-medium">Estoque</th>
              <th scope="col" className="py-3 font-medium">Vitrine</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {products.data?.content.map((product) => (
              <tr key={product.id} className={product.active ? '' : 'text-slate'}>
                <td className="py-3 pr-4">
                  <div className="flex items-center gap-3">
                    <span
                      className="grid size-12 shrink-0 place-items-center rounded-lg"
                      style={{ backgroundColor: `color-mix(in srgb, ${product.accentColor} 14%, #f6f7f8)` }}
                    >
                      <ProductArt art={product.art} accent={product.accentColor} className="w-full p-1" />
                    </span>
                    <span>
                      <span className="font-semibold">{product.name}</span>
                      <span className="block text-xs text-slate">{product.sku}</span>
                    </span>
                  </div>
                </td>
                <td className="py-3 pr-4">{product.categoryName}</td>
                <td className="tabular py-3 pr-4 text-right">{formatPrice(product.price)}</td>
                <td className="py-3 pr-4">
                  <StockEditor key={product.stock} product={product} />
                </td>
                <td className="py-3">
                  <button
                    type="button"
                    className="btn btn-quiet px-3 py-1.5 text-sm"
                    onClick={() => toggle.mutate(product)}
                    disabled={toggle.isPending}
                  >
                    {product.active ? 'Ocultar da loja' : 'Mostrar na loja'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
