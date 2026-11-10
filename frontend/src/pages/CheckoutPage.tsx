import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { Checkbox, Field, FormAlert } from '../components/Field'
import { OrderSummary } from '../components/OrderSummary'
import { PageHeader } from '../components/PageHeader'
import { useAuth } from '../context/auth'
import { useCart } from '../context/cart'
import { ApiError, api } from '../lib/api'
import { formatPrice, maskZip } from '../lib/format'
import { summarize } from '../lib/pricing'
import { useStoreInfo } from '../lib/queries'
import type { Address, Order } from '../lib/types'

const states = 'AC AL AP AM BA CE DF ES GO MA MT MS MG PA PB PR PE PI RJ RN RS RO RR SC SP SE TO'.split(' ')

export function CheckoutPage() {
  const { user } = useAuth()
  const { lines, subtotal, clear } = useCart()
  const { data: store } = useStoreInfo()
  const navigate = useNavigate()
  const totals = summarize(subtotal, store)

  const [address, setAddress] = useState<Address>({
    recipientName: user?.fullName ?? '',
    zipCode: '',
    street: '',
    number: '',
    complement: '',
    district: '',
    city: '',
    state: '',
  })
  const [acceptTerms, setAcceptTerms] = useState(false)

  const placeOrder = useMutation({
    mutationFn: () =>
      api.post<Order>('/api/orders', {
        items: lines.map((line) => ({ productId: line.productId, quantity: line.quantity })),
        shippingAddress: address,
        acceptTerms,
      }),
    onSuccess: (order) => {
      clear()
      navigate(`/conta/pedidos/${order.id}`, { replace: true })
    },
  })

  if (lines.length === 0 && !placeOrder.isSuccess) return <Navigate to="/carrinho" replace />

  const errors = placeOrder.error instanceof ApiError ? placeOrder.error.fieldErrors : {}
  const fieldError = (name: keyof Address) => errors[`shippingAddress.${name}`]
  const set = (name: keyof Address) => (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) =>
    setAddress((current) => ({
      ...current,
      [name]: name === 'zipCode' ? maskZip(event.target.value) : event.target.value,
    }))

  const submit = (event: FormEvent) => {
    event.preventDefault()
    placeOrder.mutate()
  }

  return (
    <>
      <PageHeader title="Entrega e pagamento" />
      <form onSubmit={submit} noValidate className="mx-auto grid max-w-7xl gap-10 px-4 sm:px-6 lg:grid-cols-[minmax(0,1fr)_24rem]">
        <section aria-labelledby="address-title">
          <h2 id="address-title" className="font-wide text-lg font-bold">
            Endereço de entrega
          </h2>
          <div className="mt-6 grid gap-5 sm:grid-cols-6">
            <Field className="sm:col-span-6" label="Nome de quem recebe" required autoComplete="name" value={address.recipientName} onChange={set('recipientName')} error={fieldError('recipientName')} />
            <Field className="sm:col-span-2" label="CEP" required inputMode="numeric" autoComplete="postal-code" placeholder="00000-000" value={address.zipCode} onChange={set('zipCode')} error={fieldError('zipCode')} />
            <Field className="sm:col-span-4" label="Rua" required autoComplete="address-line1" value={address.street} onChange={set('street')} error={fieldError('street')} />
            <Field className="sm:col-span-2" label="Número" required value={address.number} onChange={set('number')} error={fieldError('number')} />
            <Field className="sm:col-span-4" label="Complemento" autoComplete="address-line2" value={address.complement} onChange={set('complement')} />
            <Field className="sm:col-span-3" label="Bairro" required value={address.district} onChange={set('district')} error={fieldError('district')} />
            <Field className="sm:col-span-2" label="Cidade" required autoComplete="address-level2" value={address.city} onChange={set('city')} error={fieldError('city')} />
            <div className="sm:col-span-1">
              <label htmlFor="state" className="mb-1.5 block text-sm font-semibold">
                UF
              </label>
              <select id="state" required className="field" value={address.state} onChange={set('state')} aria-invalid={fieldError('state') ? true : undefined}>
                <option value="">--</option>
                {states.map((uf) => (
                  <option key={uf}>{uf}</option>
                ))}
              </select>
              {fieldError('state') ? <p className="mt-1 text-sm font-medium text-danger">{fieldError('state')}</p> : null}
            </div>
          </div>

          <h2 className="font-wide mt-12 text-lg font-bold">Pagamento</h2>
          <div className="mt-4 rounded-xl border border-dashed border-line bg-panel p-5 text-sm leading-relaxed">
            {store?.paymentProvider === 'stripe' ? (
              <>
                <p className="font-semibold">Cartão de crédito pelo Stripe</p>
                <p className="mt-1 text-slate">
                  Depois de fazer o pedido, você paga na página segura do Stripe. Os dados do cartão não passam pela
                  loja. Ambiente de teste: use o cartão 4242 4242 4242 4242, qualquer data futura e qualquer CVC.
                </p>
              </>
            ) : (
              <>
                <p className="font-semibold">Pagamento simulado</p>
                <p className="mt-1 text-slate">
                  Depois de fazer o pedido, você confirma o pagamento na próxima tela. Nenhum valor é cobrado.
                </p>
              </>
            )}
          </div>
        </section>

        <aside aria-labelledby="checkout-summary" className="h-fit rounded-2xl bg-panel p-6 lg:sticky lg:top-28">
          <h2 id="checkout-summary" className="font-wide text-lg font-bold">
            Seu pedido
          </h2>
          <ul className="mt-4 space-y-2 border-b border-line pb-4 text-sm">
            {lines.map((line) => (
              <li key={line.productId} className="flex justify-between gap-4">
                <span>
                  {line.quantity}× {line.name}
                </span>
                <span className="tabular shrink-0">{formatPrice(line.price * line.quantity)}</span>
              </li>
            ))}
          </ul>
          <div className="mt-4">
            <OrderSummary subtotal={subtotal} {...totals} />
          </div>
          {store ? (
            <p className="mt-4 text-xs leading-relaxed text-slate">
              Depois de fazer o pedido, você tem {store.paymentWindowMinutes} minutos para confirmar o pagamento.
              Sem confirmação, o pedido é cancelado e os produtos voltam ao estoque.
            </p>
          ) : null}
          <div className="mt-5">
            <Checkbox
              checked={acceptTerms}
              onChange={(event) => setAcceptTerms(event.target.checked)}
              error={errors.acceptTerms}
              label={
                <>
                  Li e aceito os{' '}
                  <Link to="/termos" target="_blank" className="text-cobalt underline underline-offset-2">
                    Termos de Serviço
                  </Link>{' '}
                  e a{' '}
                  <Link to="/reembolso" target="_blank" className="text-cobalt underline underline-offset-2">
                    Política de Reembolso
                  </Link>
                  .
                </>
              }
            />
          </div>
          <div className="mt-4">
            <FormAlert message={placeOrder.error && Object.keys(errors).length === 0 ? placeOrder.error.message : null} />
          </div>
          <button type="submit" className="btn btn-primary mt-4 w-full" disabled={placeOrder.isPending}>
            {placeOrder.isPending ? 'Reservando produtos…' : `Fazer pedido de ${formatPrice(totals.total)}`}
          </button>
        </aside>
      </form>
    </>
  )
}
