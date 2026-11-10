import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { api } from '../../lib/api'
import { formatPrice, statusLabel } from '../../lib/format'
import type { Dashboard, OrderStatus } from '../../lib/types'

const order: OrderStatus[] = ['AWAITING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELED']

export function DashboardPage() {
  const dashboard = useQuery({ queryKey: ['admin', 'dashboard'], queryFn: () => api.get<Dashboard>('/api/admin/dashboard') })

  if (dashboard.isPending) return <p className="text-slate">Carregando indicadores…</p>
  if (dashboard.isError) return <p role="alert" className="text-danger">{dashboard.error.message}</p>

  const d = dashboard.data
  const totalOrders = order.reduce((sum, status) => sum + (d.ordersByStatus[status] ?? 0), 0)
  const max = Math.max(1, ...order.map((status) => d.ordersByStatus[status] ?? 0))

  return (
    <div className="space-y-12">
      <dl className="grid gap-8 sm:grid-cols-3">
        <div className="border-l-4 border-cobalt pl-4">
          <dt className="text-sm text-slate">Receita de pedidos pagos</dt>
          <dd className="font-wide tabular mt-1 text-3xl font-bold">{formatPrice(d.revenue)}</dd>
        </div>
        <div className="border-l-4 border-line pl-4">
          <dt className="text-sm text-slate">Clientes cadastrados</dt>
          <dd className="font-wide tabular mt-1 text-3xl font-bold">{d.customers}</dd>
        </div>
        <div className="border-l-4 border-line pl-4">
          <dt className="text-sm text-slate">Solicitações LGPD em aberto</dt>
          <dd className="font-wide tabular mt-1 text-3xl font-bold">{d.openPrivacyRequests}</dd>
        </div>
      </dl>

      <div className="grid gap-12 lg:grid-cols-2">
        <section aria-labelledby="status-title">
          <h2 id="status-title" className="font-wide text-lg font-bold">
            Pedidos por status <span className="tabular font-normal text-slate">({totalOrders})</span>
          </h2>
          <ul className="mt-5 space-y-3">
            {order.map((status) => {
              const value = d.ordersByStatus[status] ?? 0
              return (
                <li key={status}>
                  <Link to={`/admin/pedidos?status=${status}`} className="grid grid-cols-[10rem_1fr_2.5rem] items-center gap-3 text-sm hover:underline">
                    <span>{statusLabel[status]}</span>
                    <span className="h-2.5 rounded-full bg-aluminum-deep" aria-hidden>
                      <span className="block h-full rounded-full bg-cobalt" style={{ width: `${(value / max) * 100}%` }} />
                    </span>
                    <span className="tabular text-right font-semibold">{value}</span>
                  </Link>
                </li>
              )
            })}
          </ul>
        </section>

        <section aria-labelledby="stock-title">
          <h2 id="stock-title" className="font-wide text-lg font-bold">
            Estoque baixo
          </h2>
          {d.lowStock.length === 0 ? (
            <p className="mt-5 text-sm text-slate">Nenhum produto com 10 unidades ou menos.</p>
          ) : (
            <ul className="mt-5 divide-y divide-line border-y border-line text-sm">
              {d.lowStock.map((product) => (
                <li key={product.id} className="flex justify-between gap-4 py-3">
                  <span>{product.name}</span>
                  <span className={`tabular font-semibold ${product.stock <= 3 ? 'text-danger' : 'text-warn'}`}>
                    {product.stock} un.
                  </span>
                </li>
              ))}
            </ul>
          )}
          <Link to="/admin/produtos" className="mt-4 inline-block text-sm font-semibold text-cobalt hover:underline">
            Ajustar estoque
          </Link>
        </section>
      </div>
    </div>
  )
}
