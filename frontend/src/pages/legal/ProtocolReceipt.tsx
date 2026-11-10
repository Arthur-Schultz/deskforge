import { CircleCheck } from 'lucide-react'
import { formatDate } from '../../lib/format'
import type { Protocol } from '../../lib/types'

export function ProtocolReceipt({ protocol, children }: { protocol: Protocol; children?: React.ReactNode }) {
  return (
    <div role="status" className="not-prose rounded-2xl border border-ok/30 bg-[#eef8f2] p-6">
      <p className="flex items-center gap-2 font-semibold text-ok">
        <CircleCheck className="size-5" aria-hidden />
        Solicitação registrada
      </p>
      <p className="mt-3 text-sm">
        Protocolo <strong className="tabular text-base">{protocol.protocol}</strong>
      </p>
      <p className="mt-1 text-sm">Prazo de resposta: até {formatDate(protocol.respondBy)}.</p>
      {children}
    </div>
  )
}
