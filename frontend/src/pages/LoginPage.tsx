import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router'
import { Field, FormAlert } from '../components/Field'
import { useAuth } from '../context/auth'
import { ApiError } from '../lib/api'

export function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<ApiError | null>(null)
  const [pending, setPending] = useState(false)

  const back = params.get('voltar') ?? '/'

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setPending(true)
    setError(null)
    try {
      const user = await login(email, password)
      navigate(user.role === 'ADMIN' && back === '/' ? '/admin' : back, { replace: true })
    } catch (err) {
      setError(err as ApiError)
    } finally {
      setPending(false)
    }
  }

  return (
    <div className="mx-auto max-w-md px-4 py-14 sm:px-6">
      <h1 className="font-wide text-3xl font-extrabold tracking-tight">Entrar</h1>
      <p className="mt-2 text-slate">
        Ainda não tem conta?{' '}
        <Link to={`/criar-conta?voltar=${encodeURIComponent(back)}`} className="font-semibold text-cobalt underline underline-offset-4">
          Criar conta
        </Link>
      </p>
      <form onSubmit={submit} noValidate className="mt-8 space-y-5">
        <FormAlert message={error && Object.keys(error.fieldErrors).length === 0 ? error.message : null} />
        <Field label="E-mail" type="email" required autoComplete="email" value={email} onChange={(e) => setEmail(e.target.value)} error={error?.fieldErrors.email} />
        <Field label="Senha" type="password" required autoComplete="current-password" value={password} onChange={(e) => setPassword(e.target.value)} error={error?.fieldErrors.password} />
        <button type="submit" className="btn btn-primary w-full" disabled={pending}>
          {pending ? 'Entrando…' : 'Entrar'}
        </button>
      </form>
      <div className="mt-10 rounded-xl border border-dashed border-line p-4 text-sm">
        <p className="font-semibold">Contas de demonstração</p>
        <dl className="mt-2 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-slate">
          <dt>Cliente</dt>
          <dd>cliente@deskforge.dev / Cliente@2026</dd>
          <dt>Admin</dt>
          <dd>admin@deskforge.dev / Admin@2026</dd>
        </dl>
      </div>
    </div>
  )
}
