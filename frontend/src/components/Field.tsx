import { useId, type InputHTMLAttributes, type ReactNode, type TextareaHTMLAttributes } from 'react'

interface BaseProps {
  label: string
  error?: string
  hint?: ReactNode
}

export function Field({ label, error, hint, className, ...input }: BaseProps & InputHTMLAttributes<HTMLInputElement>) {
  const id = useId()
  const describedBy = [hint ? `${id}-hint` : null, error ? `${id}-error` : null].filter(Boolean).join(' ')
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1.5 block text-sm font-semibold">
        {label}
        {input.required ? null : <span className="font-normal text-slate"> (opcional)</span>}
      </label>
      <input
        id={id}
        className="field"
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy || undefined}
        {...input}
      />
      {hint ? (
        <p id={`${id}-hint`} className="mt-1 text-sm text-slate">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={`${id}-error`} className="mt-1 text-sm font-medium text-danger">
          {error}
        </p>
      ) : null}
    </div>
  )
}

export function TextArea({ label, error, hint, className, ...input }: BaseProps & TextareaHTMLAttributes<HTMLTextAreaElement>) {
  const id = useId()
  const describedBy = [hint ? `${id}-hint` : null, error ? `${id}-error` : null].filter(Boolean).join(' ')
  return (
    <div className={className}>
      <label htmlFor={id} className="mb-1.5 block text-sm font-semibold">
        {label}
        {input.required ? null : <span className="font-normal text-slate"> (opcional)</span>}
      </label>
      <textarea
        id={id}
        className="field min-h-32"
        aria-invalid={error ? true : undefined}
        aria-describedby={describedBy || undefined}
        {...input}
      />
      {hint ? (
        <p id={`${id}-hint`} className="mt-1 text-sm text-slate">
          {hint}
        </p>
      ) : null}
      {error ? (
        <p id={`${id}-error`} className="mt-1 text-sm font-medium text-danger">
          {error}
        </p>
      ) : null}
    </div>
  )
}

export function Checkbox({
  label,
  error,
  ...input
}: { label: ReactNode; error?: string } & InputHTMLAttributes<HTMLInputElement>) {
  const id = useId()
  return (
    <div>
      <div className="flex items-start gap-3">
        <input
          id={id}
          type="checkbox"
          className="mt-0.5 size-5 shrink-0 accent-cobalt"
          aria-invalid={error ? true : undefined}
          aria-describedby={error ? `${id}-error` : undefined}
          {...input}
        />
        <label htmlFor={id} className="text-sm leading-relaxed">
          {label}
        </label>
      </div>
      {error ? (
        <p id={`${id}-error`} className="mt-1 pl-8 text-sm font-medium text-danger">
          {error}
        </p>
      ) : null}
    </div>
  )
}

export function FormAlert({ message }: { message?: string | null }) {
  if (!message) return null
  return (
    <div role="alert" className="rounded-lg border border-danger/30 bg-danger/5 px-4 py-3 text-sm text-danger">
      {message}
    </div>
  )
}
