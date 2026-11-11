import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router'
import { Field, FormAlert } from '../components/Field'
import { api, ApiError } from '../lib/api'

function readTokenFromUrl(): string | null {
  const token = new URLSearchParams(window.location.hash.slice(1)).get('token')
  if (token) {
    window.history.replaceState(window.history.state, '', window.location.pathname)
  }
  return token
}

export function ResetPasswordPage() {
  const navigate = useNavigate()
  const [token] = useState(readTokenFromUrl)
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [mismatch, setMismatch] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)
  const [pending, setPending] = useState(false)

  if (!token) {
    return (
      <div className="mx-auto max-w-md px-4 py-14 sm:px-6">
        <h1 className="font-wide text-3xl font-extrabold tracking-tight">Link incompleto</h1>
        <p className="mt-3 text-slate">
          Abra o link exatamente como ele chegou no e-mail. Se ele expirou, peça um novo.
        </p>
        <Link to="/esqueci-senha" className="btn btn-primary mt-8">
          Pedir novo link
        </Link>
      </div>
    )
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setError(null)
    if (password !== confirmation) {
      setMismatch(true)
      return
    }
    setMismatch(false)
    setPending(true)
    try {
      await api.post('/api/auth/password/reset', { token, newPassword: password })
      navigate('/entrar', { replace: true, state: { notice: 'Senha alterada. Entre com a nova senha.' } })
    } catch (err) {
      setError(err as ApiError)
    } finally {
      setPending(false)
    }
  }

  const expired = error?.status === 422

  return (
    <div className="mx-auto max-w-md px-4 py-14 sm:px-6">
      <h1 className="font-wide text-3xl font-extrabold tracking-tight">Criar nova senha</h1>
      <p className="mt-2 text-slate">Escolha uma senha que você não usa em outros sites.</p>
      <form onSubmit={submit} noValidate className="mt-8 space-y-5">
        <FormAlert message={error && Object.keys(error.fieldErrors).length === 0 ? error.message : null} />
        {expired ? (
          <Link to="/esqueci-senha" className="btn btn-secondary w-full">
            Pedir novo link
          </Link>
        ) : null}
        <Field
          label="Nova senha"
          type="password"
          required
          autoComplete="new-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={error?.fieldErrors.newPassword}
          hint="Pelo menos 8 caracteres, com letras e números."
        />
        <Field
          label="Repita a nova senha"
          type="password"
          required
          autoComplete="new-password"
          value={confirmation}
          onChange={(e) => setConfirmation(e.target.value)}
          error={mismatch ? 'As senhas não são iguais' : undefined}
        />
        <button type="submit" className="btn btn-primary w-full" disabled={pending || expired}>
          {pending ? 'Salvando…' : 'Salvar nova senha'}
        </button>
      </form>
    </div>
  )
}
