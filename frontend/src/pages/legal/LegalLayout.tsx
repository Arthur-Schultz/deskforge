import type { ReactNode } from 'react'
import { NavLink } from 'react-router'
import { useStoreInfo } from '../../lib/queries'

const pages = [
  { to: '/termos', label: 'Termos de Serviço' },
  { to: '/privacidade', label: 'Política de Privacidade' },
  { to: '/cookies', label: 'Política de Cookies' },
  { to: '/reembolso', label: 'Trocas e devoluções' },
  { to: '/contato', label: 'Fale com o SAC' },
  { to: '/acessibilidade', label: 'Acessibilidade' },
]

export function LegalLayout({ title, intro, children }: { title: string; intro?: ReactNode; children: ReactNode }) {
  const { data: store } = useStoreInfo()
  return (
    <div className="mx-auto grid max-w-7xl gap-12 px-4 pt-10 sm:px-6 sm:pt-14 lg:grid-cols-[15rem_minmax(0,1fr)]">
      <nav aria-label="Páginas de transparência" className="order-2 lg:order-1">
        <ul className="space-y-1 text-sm lg:sticky lg:top-28">
          {pages.map((page) => (
            <li key={page.to}>
              <NavLink
                to={page.to}
                className={({ isActive }) =>
                  `block rounded-lg px-3 py-2 ${isActive ? 'bg-graphite font-semibold text-white' : 'text-slate hover:bg-aluminum-deep hover:text-graphite'}`
                }
              >
                {page.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
      <article className="order-1 lg:order-2">
        <h1 className="font-wide text-3xl font-extrabold tracking-tight sm:text-4xl">{title}</h1>
        {store ? <p className="mt-2 text-sm text-slate">Versão {store.privacyPolicyVersion}</p> : null}
        {intro ? <div className="mt-6 max-w-[68ch] text-lg leading-relaxed text-slate">{intro}</div> : null}
        <div className="prose-legal mt-8">{children}</div>
      </article>
    </div>
  )
}
