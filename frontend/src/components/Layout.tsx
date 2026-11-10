import { Outlet, ScrollRestoration } from 'react-router'
import { CookieBanner } from './CookieBanner'
import { DemoBanner } from './DemoBanner'
import { Footer } from './Footer'
import { Header } from './Header'

export function Layout() {
  return (
    <>
      <a
        href="#conteudo"
        className="sr-only focus:not-sr-only focus:fixed focus:left-3 focus:top-3 focus:z-[60] focus:rounded-lg focus:bg-cobalt focus:px-4 focus:py-2 focus:text-white"
      >
        Pular para o conteúdo
      </a>
      <DemoBanner />
      <Header />
      <main id="conteudo" tabIndex={-1} className="focus:outline-none">
        <Outlet />
      </main>
      <Footer />
      <CookieBanner />
      <ScrollRestoration />
    </>
  )
}
