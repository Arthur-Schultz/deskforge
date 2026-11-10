import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { CircleCheck } from 'lucide-react'
import { useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router'
import { FormAlert } from '../../components/Field'
import { OrderSummary } from '../../components/OrderSummary'
import { StatusBadge } from '../../components/StatusBadge'
import { api } from '../../lib/api'
import { formatDate, formatDateTime, formatPrice } from '../../lib/format'
import { useStoreInfo } from '../../lib/queries'
import type { Order } from '../../lib/types'
import { NotFoundPage } from '../NotFoundPage'

const steps = [
  { key: 'createdAt', label: 'Pedido feito' },
  { key: 'paidAt', label: 'Pagamento confirmado' },
  { key: 'shippedAt', label: 'Enviado' },
  { key: 'deliveredAt', label: 'Entregue' },
] as const

export function OrderDetailPage() {
  const { id } = useParams()
  const [params] = useSearchParams()
  const { data: store } = useStoreInfo()
  const queryClient = useQueryClient()
  const [confirmingCancel, setConfirmingCancel] = useState(false)
  const [waitingSince] = useState(() => Date.now())
  const returnedFromStripe = params.get('pagamento')

  const order = useQuery({
    queryKey: ['order', id],
    queryFn: () => api.get<Order>(`/api/orders/${id}`),
    refetchInterval: (query) =>
      returnedFromStripe === 'sucesso' &&
      query.state.data?.status === 'AWAITING_PAYMENT' &&
      Date.now() - waitingSince < 60_000
        ? 2000
        : false,
  })

  const refresh = (updated: Order) => {
    queryClient.setQueryData(['order', id], updated)
    queryClient.invalidateQueries({ queryKey: ['orders'] })
  }

  const pay = useMutation({
    mutationFn: () => api.post<{ order: Order; redirectUrl?: string }>(`/api/orders/${id}/payment`),
    onSuccess: (result) => {
      if (result.redirectUrl) window.location.assign(result.redirectUrl)
      else refresh(result.order)
    },
  })

  const cancel = useMutation({
    mutationFn: () => api.post<Order>(`/api/orders/${id}/cancellation`),
    onSuccess: (updated) => {
      setConfirmingCancel(false)
      refresh(updated)
    },
  })

  if (order.isPending) return <p className="mx-auto max-w-5xl px-6 py-20 text-slate">Carregando pedido…</p>
  if (order.isError) return <NotFoundPage />

  const o = order.data
  const awaiting = o.status === 'AWAITING_PAYMENT'
  const stripe = store?.paymentProvider === 'stripe'
  const confirming = awaiting && returnedFromStripe === 'sucesso'
  const justPaid = o.status === 'PAID' && (pay.isSuccess || returnedFromStripe === 'sucesso')

  return (
    <div className="mx-auto max-w-5xl px-4 pb-10 pt-10 sm:px-6">
      <Link to="/conta/pedidos" className="text-sm text-slate hover:text-graphite hover:underline">
        Todos os pedidos
      </Link>
      <div className="mt-4 flex flex-wrap items-center gap-3">
        <h1 className="font-wide text-3xl font-extrabold tracking-tight">Pedido {o.number}</h1>
        <StatusBadge status={o.status} />
      </div>
      <p className="mt-2 text-sm text-slate">Feito em {formatDateTime(o.createdAt)}</p>

      {confirming ? (
        <p role="status" className="mt-8 rounded-xl bg-[#e1e7ff] px-4 py-3 text-sm font-medium text-cobalt-deep">
          Recebemos você de volta do Stripe. Estamos aguardando a confirmação do pagamento, o que leva alguns
          segundos.
        </p>
      ) : null}

      {awaiting && !confirming ? (
        <section aria-labelledby="pay-title" className="mt-8 rounded-2xl bg-graphite p-6 text-white sm:p-8">
          <h2 id="pay-title" className="font-wide text-xl font-bold">
            {returnedFromStripe === 'cancelado' ? 'Pagamento não concluído' : 'Confirme o pagamento'}
          </h2>
          <p className="mt-2 max-w-xl text-sm leading-relaxed text-[#c9cdd3]">
            Os produtos estão reservados até {formatDateTime(o.expiresAt)}.{' '}
            {stripe
              ? 'Você vai para a página segura do Stripe. Ambiente de teste: use o cartão 4242 4242 4242 4242, qualquer data futura e qualquer CVC.'
              : 'Este é um pagamento simulado: nenhum valor é cobrado.'}
          </p>
          <div className="mt-6 flex flex-wrap items-center gap-3">
            <button type="button" className="btn bg-white text-graphite hover:bg-aluminum" onClick={() => pay.mutate()} disabled={pay.isPending}>
              {pay.isPending
                ? stripe
                  ? 'Abrindo o Stripe…'
                  : 'Confirmando…'
                : stripe
                  ? `Pagar ${formatPrice(o.total)} com cartão`
                  : `Pagar ${formatPrice(o.total)}`}
            </button>
          </div>
          {pay.error ? <p role="alert" className="mt-4 text-sm text-[#ffb4ab]">{pay.error.message}</p> : null}
        </section>
      ) : null}

      {justPaid ? (
        <p role="status" className="mt-8 flex items-center gap-2 rounded-xl bg-[#dcf3e6] px-4 py-3 text-sm font-medium text-ok">
          <CircleCheck className="size-5" aria-hidden />
          Pagamento confirmado. Agora é só aguardar o envio.
        </p>
      ) : null}

      {o.status !== 'CANCELED' ? (
        <ol className="mt-10 grid gap-4 sm:grid-cols-4" aria-label="Andamento do pedido">
          {steps.map((step) => {
            const at = o[step.key]
            return (
              <li key={step.key} className={`border-t-4 pt-3 ${at ? 'border-cobalt' : 'border-line'}`}>
                <p className={`text-sm font-semibold ${at ? '' : 'text-slate'}`}>{step.label}</p>
                <p className="text-xs text-slate">{at ? formatDateTime(at) : 'Pendente'}</p>
              </li>
            )
          })}
        </ol>
      ) : (
        <p className="mt-8 rounded-xl bg-aluminum-deep px-4 py-3 text-sm">
          Cancelado em {o.canceledAt ? formatDateTime(o.canceledAt) : ''}. {o.cancelReason}
          {o.paidAt ? ' O reembolso integral foi solicitado ao meio de pagamento.' : ''}
        </p>
      )}

      <div className="mt-12 grid gap-10 md:grid-cols-[minmax(0,1fr)_20rem]">
        <section aria-labelledby="items-title">
          <h2 id="items-title" className="font-wide text-lg font-bold">
            Itens
          </h2>
          <ul className="mt-4 divide-y divide-line border-y border-line text-sm">
            {o.items.map((item) => (
              <li key={item.productSku} className="flex justify-between gap-4 py-3">
                <span>
                  {item.quantity}× {item.productName}
                  <span className="block text-xs text-slate">
                    {item.productSku}, {formatPrice(item.unitPrice)} cada
                  </span>
                </span>
                <span className="tabular font-semibold">{formatPrice(item.lineTotal)}</span>
              </li>
            ))}
          </ul>
          <h2 className="font-wide mt-10 text-lg font-bold">Entrega</h2>
          <address className="mt-3 text-sm not-italic leading-relaxed">
            {o.shippingAddress.recipientName}
            <br />
            {o.shippingAddress.street}, {o.shippingAddress.number}
            {o.shippingAddress.complement ? `, ${o.shippingAddress.complement}` : ''}
            <br />
            {o.shippingAddress.district}, {o.shippingAddress.city}/{o.shippingAddress.state}
            <br />
            CEP {o.shippingAddress.zipCode}
          </address>
        </section>
        <aside className="h-fit rounded-2xl bg-panel p-6">
          <OrderSummary subtotal={o.subtotal} shipping={o.shippingCost} total={o.total} taxes={o.approximateTaxes} />
        </aside>
      </div>

      {o.cancelable ? (
        <section aria-labelledby="cancel-title" className="mt-14 border-t border-line pt-8">
          <h2 id="cancel-title" className="font-wide text-lg font-bold">
            Desistir da compra
          </h2>
          <p className="mt-2 max-w-2xl text-sm leading-relaxed text-slate">
            {o.withdrawalDeadline
              ? `Você pode desistir até ${formatDate(o.withdrawalDeadline)}, 7 dias após o recebimento (art. 49 do CDC).`
              : 'Você pode cancelar a qualquer momento antes da entrega e em até 7 dias após recebê-lo (art. 49 do CDC).'}{' '}
            {o.paidAt ? 'O reembolso é integral, incluindo o frete.' : ''}
          </p>
          {confirmingCancel ? (
            <div className="mt-4 flex flex-wrap items-center gap-3" role="group" aria-label="Confirmar cancelamento">
              <p className="text-sm font-semibold">Cancelar o pedido {o.number}?</p>
              <button type="button" className="btn bg-danger text-white hover:bg-[#8f1c13]" onClick={() => cancel.mutate()} disabled={cancel.isPending}>
                {cancel.isPending ? 'Cancelando…' : 'Sim, cancelar pedido'}
              </button>
              <button type="button" className="btn btn-quiet" onClick={() => setConfirmingCancel(false)}>
                Manter pedido
              </button>
            </div>
          ) : (
            <button type="button" className="btn btn-secondary mt-4" onClick={() => setConfirmingCancel(true)}>
              Cancelar pedido
            </button>
          )}
          <div className="mt-3">
            <FormAlert message={cancel.error?.message} />
          </div>
        </section>
      ) : null}
    </div>
  )
}
