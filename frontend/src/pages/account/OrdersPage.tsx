import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { PageHeader } from '../../components/PageHeader'
import { StatusBadge } from '../../components/StatusBadge'
import { api } from '../../lib/api'
import { formatDateTime, formatPrice } from '../../lib/format'
import type { Order } from '../../lib/types'

export function OrdersPage() {
  const orders = useQuery({ queryKey: ['orders'], queryFn: () => api.get<Order[]>('/api/orders') })

  return (
    <>
      <PageHeader title="Meus pedidos" />
      <div className="mx-auto max-w-5xl px-4 sm:px-6">
        {orders.isPending ? (
          <p className="text-slate">Carregando…</p>
        ) : orders.isError ? (
          <p role="alert" className="text-danger">{orders.error.message}</p>
        ) : orders.data.length === 0 ? (
          <div>
            <p className="text-slate">Você ainda não fez nenhum pedido.</p>
            <Link to="/produtos" className="btn btn-primary mt-6">
              Ver produtos
            </Link>
          </div>
        ) : (
          <ul className="divide-y divide-line border-y border-line">
            {orders.data.map((order) => (
              <li key={order.id}>
                <Link
                  to={`/conta/pedidos/${order.id}`}
                  className="flex flex-wrap items-center justify-between gap-4 py-5 hover:bg-panel sm:px-3"
                >
                  <div>
                    <p className="font-semibold">Pedido {order.number}</p>
                    <p className="text-sm text-slate">
                      {formatDateTime(order.createdAt)}, {order.items.reduce((sum, i) => sum + i.quantity, 0)}{' '}
                      {order.items.length === 1 && order.items[0].quantity === 1 ? 'item' : 'itens'}
                    </p>
                  </div>
                  <div className="flex items-center gap-4">
                    <StatusBadge status={order.status} />
                    <span className="tabular w-28 text-right font-semibold">{formatPrice(order.total)}</span>
                  </div>
                </Link>
              </li>
            ))}
          </ul>
        )}
      </div>
    </>
  )
}
