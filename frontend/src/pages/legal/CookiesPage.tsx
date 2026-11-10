import { reopenConsent } from '../../lib/consent'
import { LegalLayout } from './LegalLayout'

export function CookiesPage() {
  return (
    <LegalLayout
      title="Política de Cookies"
      intro="Quais cookies e armazenamentos locais a loja usa, para que servem e como mudar sua escolha."
    >
      <h2>O que usamos</h2>
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Tipo</th>
            <th>Para quê</th>
            <th>Duração</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>deskforge.token</td>
            <td>Essencial</td>
            <td>Manter você conectado</td>
            <td>Até fechar o navegador</td>
          </tr>
          <tr>
            <td>deskforge.cart</td>
            <td>Essencial</td>
            <td>Guardar os itens do carrinho</td>
            <td>Até você limpar o carrinho</td>
          </tr>
          <tr>
            <td>deskforge.cookie-consent</td>
            <td>Essencial</td>
            <td>Lembrar sua escolha de cookies</td>
            <td>Até a política mudar</td>
          </tr>
          <tr>
            <td>deskforge.visitor</td>
            <td>Essencial</td>
            <td>Identificador aleatório que comprova sua escolha de cookies</td>
            <td>Permanente, até você apagar</td>
          </tr>
        </tbody>
      </table>
      <h2>Análise e marketing</h2>
      <p>
        Nenhum script de análise ou de marketing é carregado antes da sua permissão. Hoje a loja não usa ferramentas de
        terceiros para isso: quando você permite análise, os eventos ficam apenas no próprio site. Se uma ferramenta
        externa for adicionada, ela será listada aqui antes de ser ativada.
      </p>
      <h2>Mudar sua escolha</h2>
      <p>Você pode mudar sua escolha a qualquer momento, sem precisar de conta.</p>
      <p>
        <button type="button" className="btn btn-secondary" onClick={reopenConsent}>
          Abrir preferências de cookies
        </button>
      </p>
    </LegalLayout>
  )
}
