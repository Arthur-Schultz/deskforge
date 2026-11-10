import { Trash2 } from 'lucide-react'
import { Link } from 'react-router'
import { OrderSummary } from '../components/OrderSummary'
import { PageHeader } from '../components/PageHeader'
import { ProductArt } from '../components/ProductArt'
import { QuantityStepper } from '../components/QuantityStepper'
import { MAX_PER_ITEM, useCart } from '../context/cart'
import { formatPrice } from '../lib/format'
import { summarize } from '../lib/pricing'
import { useStoreInfo } from '../lib/queries'

export function CartPage() {
  const { lines, subtotal, setQuantity, remove } = useCart()
  const { data: store } = useStoreInfo()
  const totals = summarize(subtotal, store)

  if (lines.length === 0) {
    return (
      <>
        <PageHeader title="Seu carrinho está vazio" />
        <div className="mx-auto max-w-7xl px-4 sm:px-6">
          <p className="text-slate">Escolha um produto no catálogo e ele aparece aqui.</p>
          <Link to="/produtos" className="btn btn-primary mt-6">
            Ver produtos
          </Link>
        </div>
      </>
    )
  }

  return (
    <>
      <PageHeader title="Carrinho" />
      <div className="mx-auto grid max-w-7xl gap-10 px-4 sm:px-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <ul className="divide-y divide-line border-y border-line">
          {lines.map((line) => (
            <li key={line.productId} className="flex gap-4 py-5">
              <Link
                to={`/produtos/${line.slug}`}
                className="grid size-24 shrink-0 place-items-center rounded-xl sm:size-28"
                style={{ backgroundColor: `color-mix(in srgb, ${line.accentColor} 14%, #f6f7f8)` }}
                tabIndex={-1}
                aria-hidden
              >
                <ProductArt art={line.art} accent={line.accentColor} className="w-full p-2" />
              </Link>
              <div className="flex flex-1 flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <Link to={`/produtos/${line.slug}`} className="font-semibold hover:underline">
                    {line.name}
                  </Link>
                  <p className="tabular text-sm text-slate">{formatPrice(line.price)} cada</p>
                </div>
                <div className="flex items-center gap-4">
                  <QuantityStepper
                    value={line.quantity}
                    max={Math.min(MAX_PER_ITEM, line.stock)}
                    onChange={(quantity) => setQuantity(line.productId, quantity)}
                    label={`Quantidade de ${line.name}`}
                  />
                  <p className="tabular w-28 text-right font-semibold">{formatPrice(line.price * line.quantity)}</p>
                  <button
                    type="button"
                    className="btn btn-quiet p-2"
                    onClick={() => remove(line.productId)}
                    aria-label={`Remover ${line.name} do carrinho`}
                  >
                    <Trash2 className="size-4" aria-hidden />
                  </button>
                </div>
              </div>
            </li>
          ))}
        </ul>

        <aside aria-labelledby="summary-title" className="h-fit rounded-2xl bg-panel p-6 lg:sticky lg:top-28">
          <h2 id="summary-title" className="font-wide text-lg font-bold">
            Resumo
          </h2>
          <div className="mt-4">
            <OrderSummary subtotal={subtotal} {...totals} />
          </div>
          {totals.missingForFreeShipping > 0 ? (
            <p className="mt-4 rounded-lg bg-aluminum px-3 py-2 text-sm">
              Faltam <strong className="tabular">{formatPrice(totals.missingForFreeShipping)}</strong> para o frete
              grátis.
            </p>
          ) : null}
          <Link to="/checkout" className="btn btn-primary mt-6 w-full">
            Continuar para entrega e pagamento
          </Link>
          <p className="mt-3 text-xs text-slate">
            Os preços e o estoque são confirmados novamente quando você fecha o pedido.
          </p>
        </aside>
      </div>
    </>
  )
}
