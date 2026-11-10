import { Link } from 'react-router'

export function NotFoundPage() {
  return (
    <div className="mx-auto max-w-7xl px-4 py-24 sm:px-6">
      <h1 className="font-wide text-4xl font-extrabold tracking-tight">Página não encontrada</h1>
      <p className="mt-4 max-w-md text-slate">
        O endereço pode ter mudado ou o produto saiu do catálogo. Volte para a vitrine ou use a busca no topo.
      </p>
      <Link to="/" className="btn btn-primary mt-8">
        Voltar para a página inicial
      </Link>
    </div>
  )
}
