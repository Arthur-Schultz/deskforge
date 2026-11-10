import { useMutation } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { Field, FormAlert, TextArea } from '../../components/Field'
import { ApiError, api } from '../../lib/api'
import { useStoreInfo } from '../../lib/queries'
import type { DataSubjectRequestType, Protocol } from '../../lib/types'
import { LegalLayout } from './LegalLayout'
import { ProtocolReceipt } from './ProtocolReceipt'

const types: { value: DataSubjectRequestType; label: string }[] = [
  { value: 'CONFIRMATION', label: 'Confirmar se a loja trata meus dados' },
  { value: 'ACCESS', label: 'Acessar meus dados' },
  { value: 'CORRECTION', label: 'Corrigir dados incompletos ou errados' },
  { value: 'ANONYMIZATION', label: 'Anonimizar ou bloquear dados desnecessários' },
  { value: 'PORTABILITY', label: 'Portabilidade dos dados' },
  { value: 'DELETION', label: 'Eliminar dados tratados com consentimento' },
  { value: 'SHARING_INFO', label: 'Saber com quem meus dados são compartilhados' },
  { value: 'CONSENT_REVOCATION', label: 'Revogar um consentimento' },
]

export function DataRequestPage() {
  const { data: store } = useStoreInfo()
  const [form, setForm] = useState({ fullName: '', email: '', type: '' as DataSubjectRequestType | '', details: '' })

  const submit = useMutation({
    mutationFn: () => api.post<Protocol>('/api/privacy/requests', { ...form, type: form.type || null }),
  })

  const errors = submit.error instanceof ApiError ? submit.error.fieldErrors : {}

  const onSubmit = (event: FormEvent) => {
    event.preventDefault()
    submit.mutate()
  }

  return (
    <LegalLayout
      title="Seus dados (LGPD)"
      intro={
        <>
          Use este formulário para exercer qualquer direito do art. 18 da LGPD. Se você tem conta, baixar seus dados e
          excluir a conta é imediato em <Link to="/conta" className="text-cobalt underline underline-offset-4">Minha conta</Link>.
        </>
      }
    >
      {submit.isSuccess ? (
        <ProtocolReceipt protocol={submit.data}>
          <p className="mt-3 text-sm text-slate">
            Guarde o protocolo. Vamos confirmar sua identidade pelo e-mail informado antes de atender o pedido.
          </p>
        </ProtocolReceipt>
      ) : (
        <form onSubmit={onSubmit} noValidate className="not-prose max-w-xl space-y-5">
          <FormAlert message={submit.error && Object.keys(errors).length === 0 ? submit.error.message : null} />
          <Field label="Nome completo" required autoComplete="name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} error={errors.fullName} />
          <Field
            label="E-mail"
            type="email"
            required
            autoComplete="email"
            value={form.email}
            onChange={(e) => setForm({ ...form, email: e.target.value })}
            error={errors.email}
            hint="Use o mesmo e-mail da sua conta, se tiver uma."
          />
          <fieldset>
            <legend className="mb-2 text-sm font-semibold">O que você quer fazer?</legend>
            <div className="space-y-2">
              {types.map((type) => (
                <label key={type.value} className="flex items-center gap-3 text-sm">
                  <input
                    type="radio"
                    name="type"
                    value={type.value}
                    checked={form.type === type.value}
                    onChange={() => setForm({ ...form, type: type.value })}
                    className="size-4 accent-cobalt"
                  />
                  {type.label}
                </label>
              ))}
            </div>
            {errors.type ? <p className="mt-1 text-sm font-medium text-danger">{errors.type}</p> : null}
          </fieldset>
          <TextArea label="Detalhes" value={form.details} onChange={(e) => setForm({ ...form, details: e.target.value })} error={errors.details} maxLength={1000} />
          <p className="text-xs leading-relaxed text-slate">
            Usamos estes dados apenas para responder a esta solicitação. Prazo de resposta: até{' '}
            {store?.dataSubjectResponseDays} dias.
          </p>
          <button type="submit" className="btn btn-primary" disabled={submit.isPending}>
            {submit.isPending ? 'Enviando…' : 'Enviar solicitação'}
          </button>
        </form>
      )}
    </LegalLayout>
  )
}
