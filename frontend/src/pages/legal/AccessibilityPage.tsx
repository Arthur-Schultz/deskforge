import { Link } from 'react-router'
import { LegalLayout } from './LegalLayout'

export function AccessibilityPage() {
  return (
    <LegalLayout
      title="Acessibilidade"
      intro="A loja foi construída para funcionar com teclado, leitores de tela e ampliação, seguindo as diretrizes WCAG 2.2 nível AA e a Lei Brasileira de Inclusão (Lei 13.146/2015)."
    >
      <h2>O que já fizemos</h2>
      <ul>
        <li>Toda a navegação funciona pelo teclado, com foco visível e um atalho para pular direto ao conteúdo.</li>
        <li>Textos e botões têm contraste mínimo de 4,5:1 sobre o fundo.</li>
        <li>Todas as imagens de produto têm descrição em texto ou são marcadas como decorativas.</li>
        <li>Campos de formulário têm rótulos, e os erros são anunciados e associados ao campo correto.</li>
        <li>A ilustração interativa da página inicial pode ser percorrida com Tab e ativada com Enter.</li>
        <li>Animações são desativadas quando o sistema pede movimento reduzido.</li>
        <li>O layout se adapta a telas pequenas e a zoom de até 200% sem perder conteúdo.</li>
      </ul>
      <h2>Encontrou uma barreira?</h2>
      <p>
        Conte para nós pelo <Link to="/contato">SAC</Link>, informando a página e o que aconteceu. Toda mensagem recebe
        protocolo e resposta.
      </p>
    </LegalLayout>
  )
}
