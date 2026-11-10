import type { ReactNode } from 'react'

export function PageHeader({ title, children }: { title: string; children?: ReactNode }) {
  return (
    <div className="mx-auto max-w-7xl px-4 pb-8 pt-10 sm:px-6 sm:pt-14">
      <h1 className="font-wide text-3xl font-extrabold tracking-tight sm:text-4xl">{title}</h1>
      {children ? <div className="mt-3 max-w-2xl text-slate">{children}</div> : null}
    </div>
  )
}
