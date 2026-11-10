import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { Checkbox, Field, FormAlert } from '../components/Field'
import { useAuth } from '../context/auth'
import { ApiError } from '../lib/api'

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [form, setForm] = useState({ fullName: '', email: '', password: '', acceptTerms: false, marketingConsent: false })
  const [error, setError] = useState<ApiError | null>(null)
  const [pending, setPending] = useState(false)

  const back = params.get('voltar') ?? '/'

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      await register(form)
      navigate(back, { replace: true })
    } catch (err) {
      setError(err as ApiError)
    } finally {
      setPending(false)
    }
  }

  const errors = error?.fieldErrors ?? {}

  return (
    <div className="mx-auto max-w-md px-4 py-14 sm:px-6">
      <h1 className="font-wide text-3xl font-extrabold tracking-tight">Criar conta</h1>
      <p className="mt-2 text-slate">
        Já tem conta?{' '}
        <Link to={`/entrar?voltar=${encodeURIComponent(back)}`} className="font-semibold text-cobalt underline underline-offset-4">
          Entrar
        </Link>
      </p>
      <form onSubmit={submit} noValidate className="mt-8 space-y-5">
        <FormAlert message={error && Object.keys(errors).length === 0 ? error.message : null} />
        <Field label="Nome completo" required autoComplete="name" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} error={errors.fullName} />
        <Field label="E-mail" type="email" required autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} error={errors.email} />
        <Field
          label="Senha"
          type="password"
          required
          autoComplete="new-password"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          error={errors.password}
          hint="Pelo menos 8 caracteres, com letras e números."
        />
        <Checkbox
          checked={form.acceptTerms}
          onChange={(e) => setForm({ ...form, acceptTerms: e.target.checked })}
          error={errors.acceptTerms}
          label={
            <>
              Li e aceito os{' '}
              <Link to="/termos" target="_blank" className="text-cobalt underline underline-offset-2">
                Termos de Serviço
              </Link>{' '}
              e a{' '}
              <Link to="/privacidade" target="_blank" className="text-cobalt underline underline-offset-2">
                Política de Privacidade
              </Link>
              .
            </>
          }
        />
        <Checkbox
          checked={form.marketingConsent}
          onChange={(e) => setForm({ ...form, marketingConsent: e.target.checked })}
          label="Quero receber novidades e ofertas por e-mail. Opcional, e dá para cancelar quando quiser."
        />
        <button type="submit" className="btn btn-primary w-full" disabled={pending}>
          {pending ? 'Criando conta…' : 'Criar conta'}
        </button>
      </form>
    </div>
  )
}
