const REPOSITORY_URL = 'https://github.com/Arthur-Schultz/deskforge'

export function DemoBanner() {
  return (
    <div className="bg-graphite px-4 py-2 text-center text-sm text-white">
      Loja de demonstração: empresa e produtos fictícios, nenhuma cobrança é feita.{' '}
      <a href={REPOSITORY_URL} target="_blank" rel="noreferrer" className="font-semibold underline underline-offset-4 hover:text-aluminum">
        Ver o código no GitHub
      </a>
    </div>
  )
}
