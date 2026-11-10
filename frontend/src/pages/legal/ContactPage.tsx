import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Field, FormAlert, TextArea } from '../../components/Field'
import { useAuth } from '../../context/auth'
import { ApiError, api } from '../../lib/api'
import { useStoreInfo } from '../../lib/queries'
import type { Protocol } from '../../lib/types'
import { LegalLayout } from './LegalLayout'
import { ProtocolReceipt } from './ProtocolReceipt'

export function ContactPage() {
  const { user } = useAuth()
  const { data: store } = useStoreInfo()
  const [form, setForm] = useState({
    fullName: user?.fullName ?? '',
    email: user?.email ?? '',
    subject: '',
    orderNumber: '',
    message: '',
  })

  const submit = useMutation({ mutationFn: () => api.post<Protocol>('/api/contact', form) })
  const errors = submit.error instanceof ApiError ? submit.error.fieldErrors : {}

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    submit.mutate()
  }

  return (
    <LegalLayout
      title="Fale com o SAC"
      intro={
        store
          ? `Dúvidas, trocas, defeitos ou sugestões. Toda mensagem recebe um protocolo e resposta em até ${store.supportResponseDays} dias. Atendimento: ${store.supportHours}.`
          : undefined
      }
    >
      {submit.isSuccess ? (
        <ProtocolReceipt protocol={submit.data} />
      ) : (
        <form onSubmit={onSubmit} noValidate className="not-prose max-w-xl space-y-5">
          <FormAlert message={submit.error && Object.keys(errors).length === 0 ? submit.error.message : null} />
          <Field label="Nome" required autoComplete="name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} error={errors.fullName} />
          <Field label="E-mail" type="email" required autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} error={errors.email} />
          <Field label="Assunto" required value={form.subject} onChange={(e) => setForm({ ...form, subject: e.target.value })} error={errors.subject} />
          <Field label="Número do pedido" placeholder="DF-1001" value={form.orderNumber} onChange={(e) => setForm({ ...form, orderNumber: e.target.value.toUpperCase() })} error={errors.orderNumber} />
          <TextArea label="Mensagem" required maxLength={2000} value={form.message} onChange={(e) => setForm({ ...form, message: e.target.value })} error={errors.message} />
          <button type="submit" className="btn btn-primary" disabled={submit.isPending}>
            {submit.isPending ? 'Enviando…' : 'Enviar mensagem'}
          </button>
        </form>
      )}
      {store ? (
        <p className="mt-10 text-sm">
          Também atendemos pelo e-mail <a href={`mailto:${store.supportEmail}`}>{store.supportEmail}</a>.
        </p>
      ) : null}
    </LegalLayout>
  )
}
