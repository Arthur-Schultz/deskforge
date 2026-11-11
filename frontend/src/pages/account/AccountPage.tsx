import { useMutation } from '@tanstack/react-query'
import { Download, LogOut } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router'
import { DemoNotice } from '../../components/DemoNotice'
import { FormAlert } from '../../components/Field'
import { PageHeader } from '../../components/PageHeader'
import { useAuth } from '../../context/auth'
import { api } from '../../lib/api'
import { useIsSharedDemoAccount } from '../../lib/queries'
import { formatDate } from '../../lib/format'
import type { User } from '../../lib/types'

export function AccountPage() {
  const { user, setUser, logout, logoutEverywhere } = useAuth()
  const navigate = useNavigate()
  const [confirmDelete, setConfirmDelete] = useState(false)
  const sharedAccount = useIsSharedDemoAccount(user?.email)

  const consent = useMutation({
    mutationFn: (marketingConsent: boolean) => api.patch<User>('/api/me/consents', { marketingConsent }),
    onSuccess: setUser,
  })

  const exportData = useMutation({
    mutationFn: () => api.get<unknown>('/api/me/data-export'),
    onSuccess: (data) => {
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = 'deskforge-meus-dados.json'
      link.click()
      URL.revokeObjectURL(url)
    },
  })

  const endSessions = useMutation({
    mutationFn: logoutEverywhere,
    onSuccess: () => {
      navigate('/entrar', { replace: true, state: { notice: 'Você saiu de todos os dispositivos.' } })
      void logout()
    },
  })

  const remove = useMutation({
    mutationFn: () => api.delete<void>('/api/me'),
    onSuccess: () => {
      navigate('/', { replace: true })
      void logout()
    },
  })

  if (!user) return null

  return (
    <>
      <PageHeader title="Minha conta" />
      <div className="mx-auto max-w-3xl space-y-12 px-4 sm:px-6">
        {sharedAccount ? (
          <DemoNotice>
            Esta é a conta de demonstração compartilhada. Para testar a exclusão da conta e o encerramento das sessões,{' '}
            <Link to="/criar-conta" className="font-semibold underline underline-offset-2">crie sua própria conta</Link>.
          </DemoNotice>
        ) : null}
        <section aria-labelledby="profile-title">
          <h2 id="profile-title" className="font-wide text-lg font-bold">
            Dados da conta
          </h2>
          <dl className="mt-4 divide-y divide-line border-y border-line text-sm">
            <div className="grid grid-cols-[8rem_1fr] gap-4 py-3">
              <dt className="text-slate">Nome</dt>
              <dd>{user.fullName}</dd>
            </div>
            <div className="grid grid-cols-[8rem_1fr] gap-4 py-3">
              <dt className="text-slate">E-mail</dt>
              <dd>{user.email}</dd>
            </div>
            <div className="grid grid-cols-[8rem_1fr] gap-4 py-3">
              <dt className="text-slate">Cliente desde</dt>
              <dd>{formatDate(user.createdAt)}</dd>
            </div>
          </dl>
          <Link to="/conta/pedidos" className="btn btn-secondary mt-6">
            Ver meus pedidos
          </Link>
        </section>

        <section aria-labelledby="privacy-title">
          <h2 id="privacy-title" className="font-wide text-lg font-bold">
            Privacidade
          </h2>
          <label className="mt-4 flex items-start gap-3 rounded-xl bg-panel p-4 text-sm">
            <input
              type="checkbox"
              className="mt-0.5 size-5 accent-cobalt"
              checked={user.marketingConsent}
              disabled={consent.isPending}
              onChange={(event) => consent.mutate(event.target.checked)}
            />
            <span>
              <strong className="font-semibold">Receber novidades e ofertas por e-mail</strong>
              <span className="block text-slate">Você pode ativar ou desativar quando quiser.</span>
            </span>
          </label>

          <div className="mt-6 flex flex-wrap gap-3">
            <button type="button" className="btn btn-secondary" onClick={() => exportData.mutate()} disabled={exportData.isPending}>
              <Download className="size-4" aria-hidden />
              {exportData.isPending ? 'Gerando arquivo…' : 'Baixar meus dados (JSON)'}
            </button>
            <Link to="/lgpd" className="btn btn-quiet">
              Outras solicitações (LGPD)
            </Link>
          </div>
          <div className="mt-3">
            <FormAlert message={exportData.error?.message ?? consent.error?.message} />
          </div>
        </section>

        <section aria-labelledby="sessions-title" className="border-t border-line pt-8">
          <h2 id="sessions-title" className="font-wide text-lg font-bold">
            Sessões
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-slate">
            Você continua conectado neste navegador por até 14 dias sem usar a loja. Se entrou em um computador que não é
            seu ou acha que alguém usou sua conta, encerre todas as sessões. Trocar a senha também encerra todas.
          </p>
          <button type="button" className="btn btn-secondary mt-4" onClick={() => endSessions.mutate()} disabled={sharedAccount || endSessions.isPending}>
            <LogOut className="size-4" aria-hidden />
            {endSessions.isPending ? 'Encerrando…' : 'Sair de todos os dispositivos'}
          </button>
          <div className="mt-3">
            <FormAlert message={endSessions.error?.message} />
          </div>
        </section>

        <section aria-labelledby="delete-title" className="border-t border-line pt-8">
          <h2 id="delete-title" className="font-wide text-lg font-bold">
            Excluir conta
          </h2>
          <p className="mt-2 text-sm leading-relaxed text-slate">
            Seu nome, e-mail e senha são apagados e você perde o acesso. Os pedidos já feitos ficam guardados sem os
            seus dados de contato, porque a loja precisa mantê-los por obrigação fiscal.
          </p>
          {confirmDelete ? (
            <div className="mt-4 flex flex-wrap items-center gap-3" role="group" aria-label="Confirmar exclusão">
              <p className="text-sm font-semibold">Essa ação não pode ser desfeita.</p>
              <button type="button" className="btn bg-danger text-white hover:bg-[#8f1c13]" onClick={() => remove.mutate()} disabled={remove.isPending}>
                {remove.isPending ? 'Excluindo…' : 'Excluir minha conta'}
              </button>
              <button type="button" className="btn btn-quiet" onClick={() => setConfirmDelete(false)}>
                Manter conta
              </button>
            </div>
          ) : (
            <button type="button" className="btn btn-secondary mt-4 border-danger text-danger hover:bg-danger hover:text-white" onClick={() => setConfirmDelete(true)} disabled={sharedAccount}>
              Excluir conta
            </button>
          )}
          <div className="mt-3">
            <FormAlert message={remove.error?.message} />
          </div>
        </section>
      </div>
    </>
  )
}
