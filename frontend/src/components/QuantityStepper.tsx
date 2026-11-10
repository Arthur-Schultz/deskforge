import { Minus, Plus } from 'lucide-react'

interface Props {
  value: number
  max: number
  onChange: (value: number) => void
  label: string
}

export function QuantityStepper({ value, max, onChange, label }: Props) {
  return (
    <div role="group" aria-label={label} className="inline-flex items-center rounded-lg border border-line bg-white">
      <button
        type="button"
        className="grid size-10 place-items-center rounded-l-lg hover:bg-aluminum disabled:opacity-40"
        onClick={() => onChange(value - 1)}
        disabled={value <= 1}
        aria-label="Diminuir quantidade"
      >
        <Minus className="size-4" aria-hidden />
      </button>
      <output className="tabular w-10 text-center font-semibold" aria-live="polite">
        {value}
      </output>
      <button
        type="button"
        className="grid size-10 place-items-center rounded-r-lg hover:bg-aluminum disabled:opacity-40"
        onClick={() => onChange(value + 1)}
        disabled={value >= max}
        aria-label="Aumentar quantidade"
      >
        <Plus className="size-4" aria-hidden />
      </button>
    </div>
  )
}
