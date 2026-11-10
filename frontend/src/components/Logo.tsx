export function Logo({ tone = 'dark' }: { tone?: 'dark' | 'light' }) {
  const body = tone === 'dark' ? '#1c1f24' : '#ffffff'
  const base = tone === 'dark' ? '#e8eaed' : '#1c1f24'
  return (
    <span className={`inline-flex items-center gap-2 ${tone === 'light' ? 'text-white' : 'text-graphite'}`}>
      <svg viewBox="0 0 32 32" className="size-7" aria-hidden>
        <rect x="2" y="4" width="28" height="24" rx="6" fill={body} />
        <rect x="6" y="8" width="20" height="13" rx="3" fill="#2747e0" />
        <rect x="6" y="23" width="20" height="2" rx="1" fill={base} />
      </svg>
      <span className="font-wide text-lg font-extrabold tracking-tight">DeskForge</span>
    </span>
  )
}
