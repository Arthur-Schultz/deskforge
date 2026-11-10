import { Link } from 'react-router'
import { useStoreInfo } from '../../lib/queries'
import { DemoNotice, LegalLayout } from './LegalLayout'

export function PrivacyPage() {
  const { data: store } = useStoreInfo()
  return (
    <LegalLayout
      title="Política de Privacidade"
      intro="Como a DeskForge coleta, usa, guarda e protege seus dados pessoais, de acordo com a Lei Geral de Proteção de Dados (Lei 13.709/2018)."
    >
      <DemoNotice />

      <h2>Quem é responsável pelos seus dados</h2>
      <p>
        O controlador é {store?.legalName}, CNPJ {store?.cnpj}, {store?.address}. O encarregado pelo tratamento de
        dados (DPO) é {store?.dataProtectionOfficer}, que atende pelo e-mail{' '}
        <a href={`mailto:${store?.dataProtectionEmail}`}>{store?.dataProtectionEmail}</a>.
      </p>

      <h2>Quais dados coletamos e por quê</h2>
      <p>Coletamos apenas o necessário para cada finalidade:</p>
      <table>
        <thead>
          <tr>
            <th>Dado</th>
            <th>Para quê</th>
            <th>Base legal (LGPD)</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Nome, e-mail e senha (armazenada com hash)</td>
            <td>Criar e proteger sua conta</td>
            <td>Execução de contrato (art. 7º, V)</td>
          </tr>
          <tr>
            <td>Endereço de entrega</td>
            <td>Entregar o pedido e emitir nota fiscal</td>
            <td>Execução de contrato e obrigação legal (art. 7º, II e V)</td>
          </tr>
          <tr>
            <td>Histórico de pedidos</td>
            <td>Atendimento, garantia e obrigações fiscais</td>
            <td>Obrigação legal e exercício de direitos (art. 7º, II e VI)</td>
          </tr>
          <tr>
            <td>IP, data e hora de acesso</td>
            <td>Segurança e guarda exigida pelo Marco Civil da Internet</td>
            <td>Obrigação legal (art. 15 da Lei 12.965/2014)</td>
          </tr>
          <tr>
            <td>E-mail para ofertas</td>
            <td>Enviar novidades, somente se você autorizar</td>
            <td>Consentimento (art. 7º, I)</td>
          </tr>
          <tr>
            <td>Cookies de análise</td>
            <td>Entender o uso das páginas, somente se você autorizar</td>
            <td>Consentimento (art. 7º, I)</td>
          </tr>
        </tbody>
      </table>
      <p>
        Não pedimos CPF, telefone, data de nascimento nem dados sensíveis. Os dados de cartão nunca passam pelos
        nossos servidores: eles são enviados diretamente ao processador de pagamento.
      </p>

      <h2>Com quem compartilhamos</h2>
      <ul>
        <li>Processador de pagamentos, para confirmar e estornar pagamentos.</li>
        <li>Transportadora, para entregar o pedido (somente nome e endereço).</li>
        <li>Autoridades públicas, quando houver obrigação legal ou ordem judicial.</li>
      </ul>
      <p>Não vendemos nem alugamos dados pessoais.</p>

      <h2>Por quanto tempo guardamos</h2>
      <ul>
        <li>Dados da conta: enquanto a conta existir.</li>
        <li>Pedidos e notas fiscais: 5 anos, pelo prazo da legislação tributária.</li>
        <li>Registros de acesso: 6 meses, conforme o Marco Civil da Internet, e depois são apagados automaticamente.</li>
        <li>Registros de consentimento de cookies: enquanto forem necessários para comprovar sua escolha.</li>
      </ul>

      <h2>Seus direitos</h2>
      <p>
        Pelo art. 18 da LGPD, você pode confirmar se tratamos seus dados, acessá-los, corrigi-los, pedir anonimização,
        portabilidade ou eliminação, saber com quem compartilhamos e revogar consentimentos. Na sua conta é possível
        baixar todos os seus dados e excluir a conta na hora. Para os demais pedidos, use o{' '}
        <Link to="/lgpd">formulário de solicitação</Link>. Respondemos em até {store?.dataSubjectResponseDays} dias.
      </p>
      <p>
        Se não ficar satisfeito com a resposta, você pode recorrer à Autoridade Nacional de Proteção de Dados (ANPD).
      </p>

      <h2>Segurança</h2>
      <p>
        Usamos conexão criptografada (HTTPS), senhas com hash BCrypt, tokens de acesso com validade curta e controle
        de acesso por perfil. Em caso de incidente de segurança que possa causar risco relevante, avisaremos você e a
        ANPD, como determina o art. 48 da LGPD.
      </p>

      <h2>Mudanças nesta política</h2>
      <p>
        Quando esta política mudar, a versão no topo da página será atualizada e pediremos novamente sua escolha de
        cookies.
      </p>
    </LegalLayout>
  )
}
