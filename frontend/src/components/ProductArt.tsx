import type { ArtKind } from '../lib/types'

const BODY = '#23272e'
const BODY_SOFT = '#353a43'
const KEY = '#3d434d'
const HIGHLIGHT = '#59606c'

interface Props {
  art: ArtKind
  accent: string
  className?: string
  title?: string
}

export function ProductArt({ art, accent, className, title }: Props) {
  const Drawing = drawings[art]
  return (
    <svg
      viewBox="0 0 400 300"
      className={className}
      role={title ? 'img' : undefined}
      aria-hidden={title ? undefined : true}
      aria-label={title}
    >
      <Drawing accent={accent} />
    </svg>
  )
}

type Drawing = (props: { accent: string }) => React.JSX.Element

const keyboardRows = [
  [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
  [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2],
  [1.5, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1.5],
  [1.75, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2.25],
  [2.25, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1.75, 1],
  [1.25, 1.25, 1.25, 6.25, 1, 1, 1, 1, 1],
]

function Keyboard({ accent }: { accent: string }) {
  const unit = 18.2
  const gap = 2.6
  const originX = 45.3
  const originY = 97
  const keys: React.JSX.Element[] = []
  keyboardRows.forEach((row, r) => {
    let x = originX
    row.forEach((width, c) => {
      const w = width * unit + (width - 1) * gap
      const isAccent = (r === 0 && c === 0) || (r === 3 && c === row.length - 1) || (r === 5 && c === 3)
      keys.push(
        <rect
          key={`${r}-${c}`}
          x={x}
          y={originY + r * (unit + gap)}
          width={w}
          height={unit}
          rx={3.5}
          fill={isAccent ? accent : KEY}
        />,
      )
      x += w + gap
    })
  })
  return (
    <g>
      <rect x={38} y={78} width={324} height={160} rx={16} fill={BODY} />
      {keys}
    </g>
  )
}

function Mouse({ accent }: { accent: string }) {
  return (
    <g>
      <path
        d="M200 40c-52 0-78 44-78 104v40c0 52 34 84 78 84s78-32 78-84v-40c0-60-26-104-78-104Z"
        fill={BODY}
      />
      <path d="M200 40v92" stroke={HIGHLIGHT} strokeWidth={3} />
      <path d="M122 132h156" stroke={BODY_SOFT} strokeWidth={3} />
      <rect x={192} y={72} width={16} height={34} rx={8} fill={accent} />
      <path d="M128 196c18 30 46 44 72 44s54-14 72-44" stroke={accent} strokeWidth={4} fill="none" opacity={0.85} />
    </g>
  )
}

function Headset({ accent }: { accent: string }) {
  return (
    <g>
      <path d="M96 170c0-76 46-122 104-122s104 46 104 122" fill="none" stroke={BODY} strokeWidth={22} strokeLinecap="round" />
      <path d="M118 150c6-54 40-84 82-84s76 30 82 84" fill="none" stroke={accent} strokeWidth={6} strokeLinecap="round" opacity={0.9} />
      <rect x={66} y={150} width={66} height={102} rx={28} fill={BODY} />
      <rect x={268} y={150} width={66} height={102} rx={28} fill={BODY} />
      <rect x={112} y={162} width={22} height={78} rx={10} fill={accent} />
      <rect x={266} y={162} width={22} height={78} rx={10} fill={accent} />
      <path d="M86 236c-8 26 6 40 40 42" fill="none" stroke={BODY_SOFT} strokeWidth={7} strokeLinecap="round" />
      <circle cx={132} cy={278} r={9} fill={HIGHLIGHT} />
    </g>
  )
}

function DeskMat({ accent }: { accent: string }) {
  return (
    <g>
      <rect x={28} y={70} width={344} height={170} rx={18} fill={accent} />
      <rect x={28} y={70} width={344} height={170} rx={18} fill={BODY} opacity={0.55} />
      <rect
        x={38}
        y={80}
        width={324}
        height={150}
        rx={12}
        fill="none"
        stroke={accent}
        strokeWidth={2}
        strokeDasharray="6 5"
        opacity={0.9}
      />
      <rect x={300} y={196} width={44} height={18} rx={4} fill={accent} opacity={0.9} />
    </g>
  )
}

function Microphone({ accent }: { accent: string }) {
  const grille = Array.from({ length: 7 }, (_, i) => 74 + i * 14)
  return (
    <g>
      <rect x={156} y={36} width={88} height={150} rx={44} fill={BODY} />
      {grille.map((y) => (
        <rect key={y} x={170} y={y} width={60} height={4} rx={2} fill={HIGHLIGHT} opacity={0.6} />
      ))}
      <rect x={156} y={150} width={88} height={12} fill={accent} />
      <path d="M140 132v18c0 34 26 58 60 58s60-24 60-58v-18" fill="none" stroke={BODY_SOFT} strokeWidth={8} strokeLinecap="round" />
      <rect x={194} y={206} width={12} height={46} fill={BODY_SOFT} />
      <rect x={132} y={252} width={136} height={16} rx={8} fill={BODY} />
    </g>
  )
}

function Webcam({ accent }: { accent: string }) {
  return (
    <g>
      <rect x={70} y={92} width={260} height={96} rx={48} fill={BODY} />
      <circle cx={200} cy={140} r={38} fill={BODY_SOFT} />
      <circle cx={200} cy={140} r={28} fill="#0f1114" />
      <circle cx={200} cy={140} r={28} fill="none" stroke={accent} strokeWidth={4} />
      <circle cx={190} cy={130} r={7} fill="white" opacity={0.35} />
      <circle cx={290} cy={140} r={6} fill={accent} />
      <circle cx={110} cy={140} r={4} fill={HIGHLIGHT} />
      <path d="M170 188h60l14 38h-88z" fill={BODY_SOFT} />
      <rect x={130} y={226} width={140} height={14} rx={7} fill={BODY} />
    </g>
  )
}

function LightBar({ accent }: { accent: string }) {
  return (
    <g>
      <defs>
        <linearGradient id={`beam-${accent.slice(1)}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor={accent} stopOpacity={0.55} />
          <stop offset="1" stopColor={accent} stopOpacity={0} />
        </linearGradient>
      </defs>
      <path d="M104 72h192l70 176H34z" fill={`url(#beam-${accent.slice(1)})`} />
      <rect x={70} y={60} width={260} height={20} rx={10} fill={BODY} />
      <rect x={96} y={76} width={208} height={5} rx={2.5} fill={accent} />
      <rect x={186} y={80} width={28} height={18} rx={4} fill={BODY_SOFT} />
      <rect x={120} y={98} width={160} height={10} rx={3} fill={BODY} />
      <rect x={120} y={108} width={160} height={96} rx={6} fill={BODY_SOFT} opacity={0.5} />
    </g>
  )
}

function MonitorArm({ accent }: { accent: string }) {
  return (
    <g>
      <rect x={150} y={40} width={210} height={128} rx={10} fill={BODY} />
      <rect x={160} y={50} width={190} height={108} rx={4} fill={accent} opacity={0.85} />
      <rect x={160} y={50} width={190} height={108} rx={4} fill={BODY} opacity={0.35} />
      <path d="M240 168 170 196 110 160" fill="none" stroke={BODY_SOFT} strokeWidth={14} strokeLinecap="round" strokeLinejoin="round" />
      <circle cx={170} cy={196} r={11} fill={HIGHLIGHT} />
      <circle cx={110} cy={160} r={11} fill={HIGHLIGHT} />
      <rect x={100} y={160} width={20} height={88} fill={BODY_SOFT} />
      <rect x={76} y={244} width={68} height={18} rx={5} fill={BODY} />
      <rect x={232} y={162} width={16} height={16} rx={4} fill={accent} />
    </g>
  )
}

const drawings: Record<ArtKind, Drawing> = {
  KEYBOARD: Keyboard,
  MOUSE: Mouse,
  HEADSET: Headset,
  DESKMAT: DeskMat,
  MICROPHONE: Microphone,
  WEBCAM: Webcam,
  LIGHTBAR: LightBar,
  MONITOR_ARM: MonitorArm,
}
