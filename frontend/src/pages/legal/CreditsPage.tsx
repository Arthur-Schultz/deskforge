import { DemoNotice, LegalLayout } from './LegalLayout'

const credits = [
  { name: 'Ilustrações de produtos e logotipo', license: 'Originais deste projeto, licença MIT' },
  { name: 'Fonte Archivo', license: 'SIL Open Font License 1.1, hospedada no próprio site' },
  { name: 'Ícones Lucide', license: 'ISC License' },
  { name: 'React, React Router e TanStack Query', license: 'MIT License' },
  { name: 'Tailwind CSS', license: 'MIT License' },
  { name: 'Spring Boot e Spring Security', license: 'Apache License 2.0' },
  { name: 'PostgreSQL', license: 'PostgreSQL License' },
]

export function CreditsPage() {
  return (
    <LegalLayout
      title="Créditos e licenças"
      intro="Nenhuma imagem, marca ou foto de terceiros é usada na loja. Abaixo estão as ferramentas abertas que tornam o projeto possível."
    >
      <DemoNotice />
      <table>
        <thead>
          <tr>
            <th>Item</th>
            <th>Licença</th>
          </tr>
        </thead>
        <tbody>
          {credits.map((credit) => (
            <tr key={credit.name}>
              <td>{credit.name}</td>
              <td>{credit.license}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <p>
        Os produtos, nomes de linhas (Anvil, Ember, Quench, Slag, Bellows, Crucible, Spark e Tong) e as
        especificações são fictícios. A loja não exibe avaliações de clientes porque não tem clientes reais.
      </p>
    </LegalLayout>
  )
}
