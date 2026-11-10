import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { readConsent, saveConsent } from '../lib/consent'
import { useStoreInfo } from '../lib/queries'

export function CookieBanner() {
  const { data: store } = useStoreInfo()
  const [decidedVersion, setDecidedVersion] = useState(() => readConsent()?.policyVersion ?? null)
  const [reopened, setReopened] = useState(false)
  const [customizing, setCustomizing] = useState(false)
  const [analytics, setAnalytics] = useState(false)
  const [marketing, setMarketing] = useState(false)

  useEffect(() => {
    const reopen = () => {
      const current = readConsent()
      setAnalytics(current?.analytics ?? false)
      setMarketing(current?.marketing ?? false)
      setCustomizing(true)
      setReopened(true)
    }
    window.addEventListener('deskforge:open-consent', reopen)
    return () => window.removeEventListener('deskforge:open-consent', reopen)
  }, [])

  if (!store) return null
  const open = reopened || decidedVersion !== store.privacyPolicyVersion
  if (!open) return null

  const decide = async (allowAnalytics: boolean, allowMarketing: boolean) => {
    setDecidedVersion(store.privacyPolicyVersion)
    setReopened(false)
    setCustomizing(false)
    await saveConsent(allowAnalytics, allowMarketing, store.privacyPolicyVersion)
  }

  return (
    <section
      aria-labelledby="cookie-title"
      className="fixed inset-x-3 bottom-3 z-50 mx-auto max-w-3xl rounded-2xl border border-line bg-white p-5 shadow-[0_18px_50px_-12px_rgba(28,31,36,0.35)] sm:p-6"
    >
      <h2 id="cookie-title" className="font-wide text-lg font-bold">
        Cookies nesta loja
      </h2>
      <p className="mt-2 text-sm leading-relaxed text-slate">
        Usamos cookies essenciais para o login e o carrinho. Os de análise só com a sua permissão.{' '}
        <Link to="/cookies" className="text-cobalt underline underline-offset-4">
          Saiba mais
        </Link>
      </p>

      {customizing ? (
        <fieldset className="mt-4 space-y-3 border-t border-line pt-4">
          <legend className="sr-only">Escolha quais cookies permitir</legend>
          <label className="flex items-start gap-3 text-sm">
            <input type="checkbox" checked disabled className="mt-0.5 size-5 accent-cobalt" />
            <span>
              <strong className="font-semibold">Essenciais</strong>
              <span className="block text-slate">Login, carrinho e segurança. Sempre ativos.</span>
            </span>
          </label>
          <label className="flex items-start gap-3 text-sm">
            <input
              type="checkbox"
              checked={analytics}
              onChange={(event) => setAnalytics(event.target.checked)}
              className="mt-0.5 size-5 accent-cobalt"
            />
            <span>
              <strong className="font-semibold">Análise</strong>
              <span className="block text-slate">Medem visitas e uso das páginas, de forma agregada.</span>
            </span>
          </label>
          <label className="flex items-start gap-3 text-sm">
            <input
              type="checkbox"
              checked={marketing}
              onChange={(event) => setMarketing(event.target.checked)}
              className="mt-0.5 size-5 accent-cobalt"
            />
            <span>
              <strong className="font-semibold">Marketing</strong>
              <span className="block text-slate">Personalizam ofertas. Esta loja não usa nenhum no momento.</span>
            </span>
          </label>
        </fieldset>
      ) : null}

      <div className="mt-5 flex flex-col gap-2 sm:flex-row sm:flex-wrap">
        {customizing ? (
          <button type="button" className="btn btn-primary" onClick={() => decide(analytics, marketing)}>
            Salvar escolhas
          </button>
        ) : (
          <>
            <button type="button" className="btn btn-primary" onClick={() => decide(true, true)}>
              Aceitar todos
            </button>
            <button type="button" className="btn btn-secondary" onClick={() => decide(false, false)}>
              Recusar opcionais
            </button>
            <button type="button" className="btn btn-quiet" onClick={() => setCustomizing(true)}>
              Escolher quais aceitar
            </button>
          </>
        )}
      </div>
    </section>
  )
}
