import { Link } from 'react-router'
import { useStoreInfo } from '../../lib/queries'
import { DemoNotice, LegalLayout } from './LegalLayout'

export function RefundPage() {
  const { data: store } = useStoreInfo()
  const days = store?.withdrawalDays ?? 7
  return (
    <LegalLayout
      title="Trocas, devoluções e reembolso"
      intro={`Você pode desistir de qualquer compra em até ${days} dias após receber o produto, sem precisar explicar o motivo.`}
    >
      <DemoNotice />
      <h2>Direito de arrependimento</h2>
      <p>
        Compras pela internet podem ser canceladas em até {days} dias corridos a partir do recebimento (art. 49 do
        Código de Defesa do Consumidor e art. 5º do Decreto 7.962/2013).
      </p>
      <ol>
        <li>
          Abra <Link to="/conta/pedidos">Meus pedidos</Link>, escolha o pedido e clique em Cancelar pedido.
        </li>
        <li>O cancelamento é confirmado na hora e o estoque volta para a loja.</li>
        <li>Se o pedido já foi pago, o estorno integral, incluindo o frete, é solicitado ao meio de pagamento no mesmo momento.</li>
        <li>Se o produto já foi entregue, o SAC envia as instruções de devolução, sem custo para você.</li>
      </ol>
      <p>Pedidos que ainda não foram pagos ou entregues podem ser cancelados a qualquer momento.</p>

      <h2>Produto com defeito</h2>
      <p>
        Todos os produtos têm garantia legal de 90 dias contra defeitos (art. 26 do CDC). Se o defeito não for
        resolvido em até 30 dias, você escolhe entre a troca, o reembolso ou o abatimento proporcional do preço (art.
        18 do CDC). Para abrir uma solicitação, use o <Link to="/contato">SAC</Link> e informe o número do pedido.
      </p>

      <h2>Prazo do reembolso</h2>
      <ul>
        <li>Cartão de crédito: o estorno aparece na fatura atual ou na seguinte, conforme o banco emissor.</li>
        <li>Outros meios: até 10 dias úteis após a confirmação do cancelamento.</li>
      </ul>
    </LegalLayout>
  )
}
