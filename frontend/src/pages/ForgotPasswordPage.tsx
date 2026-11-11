import { MailCheck } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { Field, FormAlert } from '../components/Field'
import { api, ApiError } from '../lib/api'

export function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState<ApiError | null>(null)
  const [pending, setPending] = useState(false)
  const [sentTo, setSentTo] = useState<string | null>(null)

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      await api.post('/api/auth/password/forgot', { email })
      setSentTo(email.trim())
    } catch (err) {
      setError(err as ApiError)
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-14 sm:px-6">
      <h1 className="font-wide text-3xl font-extrabold tracking-tight">Esqueci minha senha</h1>
      {sentTo ? (
        <div role="status" className="mt-8 rounded-2xl border border-ok/30 bg-[#eef8f2] p-6">
          <p className="flex items-center gap-2 font-semibold text-ok">
            <MailCheck className="size-5" aria-hidden />
            Confira sua caixa de entrada
          </p>
          <p className="mt-3 text-sm leading-relaxed">
            Se existir uma conta com <strong className="break-all">{sentTo}</strong>, enviamos um link para criar uma nova senha. Ele
            vale por 30 minutos e só pode ser usado uma vez.
          </p>
          <p className="mt-3 text-sm leading-relaxed text-slate">
            Não chegou? Olhe a pasta de spam ou peça de novo daqui a um minuto.
          </p>
          <button type="button" className="btn btn-secondary mt-5" onClick={() => setSentTo(null)}>
            Usar outro e-mail
          </button>
        </div>
      ) : (
        <>
          <p className="mt-2 text-slate">Informe o e-mail da sua conta e enviaremos um link para você criar uma nova senha.</p>
          <form onSubmit={submit} noValidate className="mt-8 space-y-5">
            <FormAlert message={error && Object.keys(error.fieldErrors).length === 0 ? error.message : null} />
            <Field
              label="E-mail"
              type="email"
              required
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={error?.fieldErrors.email}
            />
            <button type="submit" className="btn btn-primary w-full" disabled={pending}>
              {pending ? 'Enviando…' : 'Enviar link'}
            </button>
          </form>
        </>
      )}
      <p className="mt-8 text-sm">
        <Link to="/entrar" className="font-semibold text-cobalt underline underline-offset-4">
          Voltar para o login
        </Link>
      </p>
    </div>
  )
}
