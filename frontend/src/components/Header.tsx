import { LogOut, Menu, Search, ShoppingBag, User, X } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router'
import { useAuth } from '../context/auth'
import { useCart } from '../context/cart'
import { useCategories } from '../lib/queries'
import { Logo } from './Logo'

export function Header() {
  const { user, logout } = useAuth()
  const { count } = useCart()
  const { data: categories } = useCategories()
  const navigate = useNavigate()
  const location = useLocation()
  const [query, setQuery] = useState('')
  const [menuOpenedAt, setMenuOpenedAt] = useState<string | null>(null)
  const menuOpen = menuOpenedAt === location.key

  const submit = (event: FormEvent) => {
    event.preventDefault()
    const term = query.trim()
    navigate(term ? `/produtos?q=${encodeURIComponent(term)}` : '/produtos')
  }

  return (
    <header className="border-b border-line bg-aluminum/95 backdrop-blur supports-[backdrop-filter]:bg-aluminum/80 sticky top-0 z-40">
      <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3 sm:px-6">
        <button
          type="button"
          className="btn btn-quiet -ml-2 px-2 lg:hidden"
          aria-expanded={menuOpen}
          aria-controls="mobile-nav"
          onClick={() => setMenuOpenedAt(menuOpen ? null : location.key)}
        >
          {menuOpen ? <X className="size-5" aria-hidden /> : <Menu className="size-5" aria-hidden />}
          <span className="sr-only">{menuOpen ? 'Fechar menu' : 'Abrir menu'}</span>
        </button>

        <Link to="/" aria-label="DeskForge, página inicial">
          <Logo />
        </Link>

        <form role="search" onSubmit={submit} className="relative ml-auto hidden max-w-md flex-1 md:block">
          <label htmlFor="site-search" className="sr-only">
            Buscar produtos
          </label>
          <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-slate" aria-hidden />
          <input
            id="site-search"
            type="search"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Buscar teclado, mouse, headset..."
            className="field py-2 pl-9"
          />
        </form>

        <nav aria-label="Conta e carrinho" className="ml-auto flex items-center gap-1 md:ml-0">
          {user ? (
            <>
              {user.role === 'ADMIN' ? (
                <Link to="/admin" className="btn btn-quiet hidden px-3 py-2 sm:inline-flex">
                  Painel
                </Link>
              ) : null}
              <Link to="/conta" className="btn btn-quiet px-3 py-2">
                <User className="size-5" aria-hidden />
                <span className="hidden sm:inline">{user.fullName.split(' ')[0]}</span>
                <span className="sr-only sm:hidden">Minha conta</span>
              </Link>
              <button type="button" onClick={logout} className="btn btn-quiet px-3 py-2">
                <LogOut className="size-5" aria-hidden />
                <span className="sr-only">Sair</span>
              </button>
            </>
          ) : (
            <Link to="/entrar" className="btn btn-quiet px-3 py-2">
              <User className="size-5" aria-hidden />
              <span className="hidden sm:inline">Entrar</span>
              <span className="sr-only sm:hidden">Entrar</span>
            </Link>
          )}
          <Link to="/carrinho" className="btn btn-quiet relative px-3 py-2">
            <ShoppingBag className="size-5" aria-hidden />
            <span className="sr-only">Carrinho, {count} {count === 1 ? 'item' : 'itens'}</span>
            {count > 0 ? (
              <span
                aria-hidden
                className="tabular absolute -right-0.5 -top-0.5 grid min-w-5 place-items-center rounded-full bg-cobalt px-1 text-xs font-bold text-white"
              >
                {count}
              </span>
            ) : null}
          </Link>
        </nav>
      </div>

      <nav
        id="mobile-nav"
        aria-label="Categorias"
        className={`${menuOpen ? 'block' : 'hidden'} border-t border-line lg:block`}
      >
        <ul className="mx-auto flex max-w-7xl flex-col gap-1 px-4 py-2 sm:px-6 lg:flex-row lg:gap-6 lg:py-0">
          <li className="md:hidden">
            <form role="search" onSubmit={submit} className="py-2">
              <label htmlFor="mobile-search" className="sr-only">
                Buscar produtos
              </label>
              <input
                id="mobile-search"
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder="Buscar produtos"
                className="field"
              />
            </form>
          </li>
          <li>
            <NavLink
              to="/produtos"
              end
              className={({ isActive }) =>
                `block py-2.5 text-sm font-medium lg:border-b-2 ${isActive && !location.search ? 'border-graphite text-graphite' : 'border-transparent text-slate hover:text-graphite'}`
              }
            >
              Todos os produtos
            </NavLink>
          </li>
          {categories?.map((category) => {
            const active = location.search.includes(`categoria=${category.slug}`)
            return (
              <li key={category.slug}>
                <Link
                  to={`/produtos?categoria=${category.slug}`}
                  aria-current={active ? 'page' : undefined}
                  className={`block py-2.5 text-sm font-medium lg:border-b-2 ${active ? 'border-graphite text-graphite' : 'border-transparent text-slate hover:text-graphite'}`}
                >
                  {category.name}
                </Link>
              </li>
            )
          })}
        </ul>
      </nav>
    </header>
  )
}
