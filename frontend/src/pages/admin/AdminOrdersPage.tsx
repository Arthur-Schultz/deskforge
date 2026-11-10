import { keepPreviousData, useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useSearchParams } from 'react-router'
import { StatusBadge } from '../../components/StatusBadge'
import { api } from '../../lib/api'
import { formatDateTime, formatPrice, statusLabel } from '../../lib/format'
import type { AdminOrderSummary, OrderStatus, Page } from '../../lib/types'

const statuses: OrderStatus[] = ['AWAITING_PAYMENT', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELED']
const manual: OrderStatus[] = ['SHIPPED', 'DELIVERED', 'CANCELED']

export function AdminOrdersPage() {
  const [params, setParams] = useSearchParams()
  const queryClient = useQueryClient()
  const status = params.get('status') ?? ''
  const page = Number(params.get('pagina') ?? '0')

  const orders = useQuery({
    queryKey: ['admin', 'orders', status, page],
    queryFn: () =>
      api.get<Page<AdminOrderSummary>>(`/api/admin/orders?page=${page}&size=20${status ? `&status=${status}` : ''}`),
    placeholderData: keepPreviousData,
  })

  const advance = useMutation({
    mutationFn: ({ id, target }: { id: string; target: OrderStatus }) =>
      api.patch(`/api/admin/orders/${id}/status`, { status: target }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['admin'] }),
  })

  return (
    <div>
      <label className="flex items-center gap-2 text-sm">
        <span className="font-semibold">Status</span>
        <select
          className="field w-auto py-2"
          value={status}
          onChange={(event) => setParams(event.target.value ? { status: event.target.value } : {})}
        >
          <option value="">Todos</option>
          {statuses.map((s) => (
            <option key={s} value={s}>
              {statusLabel[s]}
            </option>
          ))}
        </select>
      </label>

      {advance.error ? <p role="alert" className="mt-4 text-sm text-danger">{advance.error.message}</p> : null}

      <div className="mt-6 overflow-x-auto">
        <table className="w-full min-w-[46rem] text-left text-sm">
          <caption className="sr-only">Pedidos da loja</caption>
          <thead className="border-b border-line text-slate">
            <tr>
              <th scope="col" className="py-3 pr-4 font-medium">Pedido</th>
              <th scope="col" className="py-3 pr-4 font-medium">Cliente</th>
              <th scope="col" className="py-3 pr-4 font-medium">Destino</th>
              <th scope="col" className="py-3 pr-4 font-medium">Status</th>
              <th scope="col" className="py-3 pr-4 text-right font-medium">Total</th>
              <th scope="col" className="py-3 font-medium">Próxima etapa</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-line">
            {orders.data?.content.map((o) => {
              const options = o.nextStatuses.filter((s) => manual.includes(s))
              return (
                <tr key={o.id}>
                  <td className="py-3 pr-4">
                    <span className="font-semibold">{o.number}</span>
                    <span className="block text-xs text-slate">{formatDateTime(o.createdAt)}</span>
                  </td>
                  <td className="py-3 pr-4">
                    {o.customerName}
                    <span className="block text-xs text-slate">{o.customerEmail}</span>
                  </td>
                  <td className="py-3 pr-4">
                    {o.city}/{o.state}
                  </td>
                  <td className="py-3 pr-4">
                    <StatusBadge status={o.status} />
                  </td>
                  <td className="tabular py-3 pr-4 text-right font-semibold">{formatPrice(o.total)}</td>
                  <td className="py-3">
                    {options.length > 0 ? (
                      <select
                        aria-label={`Mudar status do pedido ${o.number}`}
                        className="field w-auto py-1.5 text-sm"
                        value=""
                        disabled={advance.isPending}
                        onChange={(event) => advance.mutate({ id: o.id, target: event.target.value as OrderStatus })}
                      >
                        <option value="">Escolher…</option>
                        {options.map((s) => (
                          <option key={s} value={s}>
                            {s === 'CANCELED' ? 'Cancelar pedido' : `Marcar como ${statusLabel[s].toLowerCase()}`}
                          </option>
                        ))}
                      </select>
                    ) : (
                      <span className="text-xs text-slate">
                        {o.status === 'AWAITING_PAYMENT' ? 'Aguarda o cliente' : 'Finalizado'}
                      </span>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
        {orders.data?.content.length === 0 ? <p className="py-10 text-slate">Nenhum pedido com esse status.</p> : null}
      </div>

      {orders.data && orders.data.totalPages > 1 ? (
        <nav aria-label="Paginação" className="mt-6 flex gap-2">
          {Array.from({ length: orders.data.totalPages }, (_, i) => (
            <button
              key={i}
              type="button"
              aria-current={i === page ? 'page' : undefined}
              onClick={() => setParams({ ...(status ? { status } : {}), ...(i ? { pagina: String(i) } : {}) })}
              className={`tabular grid size-9 place-items-center rounded-lg text-sm ${i === page ? 'bg-graphite text-white' : 'hover:bg-aluminum-deep'}`}
            >
              {i + 1}
            </button>
          ))}
        </nav>
      ) : null}
    </div>
  )
}
