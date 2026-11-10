import { Link } from 'react-router'
import { useStoreInfo } from '../../lib/queries'
import { formatPrice } from '../../lib/format'
import { DemoNotice, LegalLayout } from './LegalLayout'

export function TermsPage() {
  const { data: store } = useStoreInfo()
  return (
    <LegalLayout
      title="Termos de Serviço"
      intro="As regras de uso da loja e das compras feitas nela, escritas de acordo com o Código de Defesa do Consumidor e o Decreto 7.962/2013."
    >
      <DemoNotice />

      <h2>Quem somos</h2>
      <p>
        {store?.tradeName} é o nome fantasia de {store?.legalName}, CNPJ {store?.cnpj}, com endereço em{' '}
        {store?.address}. Atendimento: {store?.supportHours}, pelo e-mail{' '}
        <a href={`mailto:${store?.supportEmail}`}>{store?.supportEmail}</a> ou pelo <Link to="/contato">formulário do SAC</Link>.
      </p>

      <h2>Conta</h2>
      <p>
        Para comprar é preciso criar uma conta com nome, e-mail e senha. Você é responsável por manter a senha em
        sigilo. Podemos suspender contas usadas para fraude ou que violem estes termos.
      </p>

      <h2>Preços, ofertas e estoque</h2>
      <ul>
        <li>Os preços estão em reais e já incluem os tributos. O valor aproximado dos tributos aparece no resumo do pedido.</li>
        <li>O preço riscado, quando existe, é o preço anterior praticado pela loja.</li>
        <li>
          O frete custa {store ? formatPrice(store.flatShippingCost) : ''} e é grátis para pedidos a partir de{' '}
          {store ? formatPrice(store.freeShippingThreshold) : ''}.
        </li>
        <li>O estoque só é reservado quando você faz o pedido. Itens no carrinho não ficam reservados.</li>
      </ul>

      <h2>Como a compra acontece</h2>
      <ol>
        <li>Você confere o resumo com produtos, frete, total e tributos antes de confirmar.</li>
        <li>Ao fazer o pedido, os produtos ficam reservados por {store?.paymentWindowMinutes} minutos.</li>
        <li>Se o pagamento não for confirmado nesse prazo, o pedido é cancelado automaticamente e nada é cobrado.</li>
        <li>Com o pagamento confirmado, o pedido aparece em Meus pedidos com todas as etapas.</li>
      </ol>

      <h2>Desistência, trocas e garantia</h2>
      <p>
        Você pode desistir em até {store?.withdrawalDays} dias após receber o produto, com reembolso integral (art. 49 do
        CDC). Produtos com defeito têm garantia legal de 90 dias. Os detalhes estão na{' '}
        <Link to="/reembolso">Política de Reembolso</Link>.
      </p>

      <h2>Atendimento</h2>
      <p>
        Toda solicitação recebe um número de protocolo e é respondida em até {store?.supportResponseDays} dias, como
        determina o Decreto 7.962/2013.
      </p>

      <h2>Propriedade intelectual</h2>
      <p>
        A marca DeskForge, os nomes dos produtos e as ilustrações são originais deste projeto. Veja os{' '}
        <Link to="/creditos">créditos e licenças</Link> das ferramentas de terceiros.
      </p>

      <h2>Lei aplicável</h2>
      <p>
        Estes termos seguem a legislação brasileira. Você pode registrar reclamações no Procon do seu estado ou na
        plataforma consumidor.gov.br.
      </p>
    </LegalLayout>
  )
}
