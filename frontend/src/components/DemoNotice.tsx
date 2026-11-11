import { Info } from 'lucide-react'
import type { ReactNode } from 'react'

export function DemoNotice({ children }: { children: ReactNode }) {
  return (
    <p role="note" className="flex items-start gap-2 rounded-xl border border-warn/30 bg-[#fdf3dc] px-4 py-3 text-sm leading-relaxed text-warn">
      <Info className="mt-0.5 size-4 shrink-0" aria-hidden />
      <span>{children}</span>
    </p>
  )
}
