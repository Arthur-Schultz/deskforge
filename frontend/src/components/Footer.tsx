import { Link } from 'react-router'
import { reopenConsent } from '../lib/consent'
import { useStoreInfo } from '../lib/queries'
import { Logo } from './Logo'

const help = [
  { to: '/contato', label: 'Fale com o SAC' },
  { to: '/reembolso', label: 'Trocas e devoluções' },
  { to: '/conta/pedidos', label: 'Meus pedidos' },
]

const legal = [
  { to: '/termos', label: 'Termos de Serviço' },
  { to: '/privacidade', label: 'Política de Privacidade' },
  { to: '/acessibilidade', label: 'Acessibilidade' },
]

export function Footer() {
  const { data: store } = useStoreInfo()
  return (
    <footer className="mt-24 bg-graphite text-[#d6d9de]">
      <div className="mx-auto grid max-w-7xl gap-10 px-4 py-14 sm:px-6 md:grid-cols-[1.4fr_1fr_1fr]">
        <div className="max-w-sm">
          <Logo tone="light" />
          <p className="mt-4 text-sm leading-relaxed">
            Periféricos e acessórios para montar uma mesa confortável de trabalhar e de jogar.
          </p>
          {store ? (
            <p className="mt-4 text-sm leading-relaxed">
              Atendimento: {store.supportHours}
              <br />
              <a className="underline underline-offset-4 hover:text-white" href={`mailto:${store.supportEmail}`}>
                {store.supportEmail}
              </a>
            </p>
          ) : null}
        </div>
        <nav aria-labelledby="footer-help">
          <h2 id="footer-help" className="font-wide text-sm font-bold text-white">
            Ajuda
          </h2>
          <ul className="mt-4 space-y-2.5 text-sm">
            {help.map((link) => (
              <li key={link.to}>
                <Link to={link.to} className="hover:text-white hover:underline underline-offset-4">
                  {link.label}
                </Link>
              </li>
            ))}
          </ul>
        </nav>
        <nav aria-labelledby="footer-legal">
          <h2 id="footer-legal" className="font-wide text-sm font-bold text-white">
            Institucional
          </h2>
          <ul className="mt-4 space-y-2.5 text-sm">
            {legal.map((link) => (
              <li key={link.to}>
                <Link to={link.to} className="hover:text-white hover:underline underline-offset-4">
                  {link.label}
                </Link>
              </li>
            ))}
            <li>
              <button type="button" onClick={reopenConsent} className="text-left hover:text-white hover:underline underline-offset-4">
                Cookies
              </button>
            </li>
          </ul>
        </nav>
      </div>
      {store ? (
        <div className="border-t border-white/10">
          <div className="mx-auto max-w-7xl px-4 py-6 text-xs leading-relaxed text-[#aab0ba] sm:px-6">
            <p>
              {store.legalName}, CNPJ {store.cnpj}. {store.address}.
            </p>
          </div>
        </div>
      ) : null}
    </footer>
  )
}
