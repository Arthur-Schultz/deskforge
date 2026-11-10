import { NavLink, Outlet } from 'react-router'

const tabs = [
  { to: '/admin', label: 'Visão geral', end: true },
  { to: '/admin/pedidos', label: 'Pedidos', end: false },
  { to: '/admin/produtos', label: 'Produtos', end: false },
]

export function AdminLayout() {
  return (
    <div className="mx-auto max-w-7xl px-4 pt-10 sm:px-6">
      <h1 className="font-wide text-3xl font-extrabold tracking-tight">Painel da loja</h1>
      <nav aria-label="Seções do painel" className="mt-6 border-b border-line">
        <ul className="flex gap-6">
          {tabs.map((tab) => (
            <li key={tab.to}>
              <NavLink
                to={tab.to}
                end={tab.end}
                className={({ isActive }) =>
                  `-mb-px block border-b-2 py-3 text-sm font-semibold ${isActive ? 'border-cobalt text-graphite' : 'border-transparent text-slate hover:text-graphite'}`
                }
              >
                {tab.label}
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
      <div className="pt-8">
        <Outlet />
      </div>
    </div>
  )
}
