export function formatAcceptRate(v: number | string | undefined): string {
  if (v === undefined || v === null) return '—'
  const n = typeof v === 'string' ? parseFloat(v) : v
  if (Number.isNaN(n)) return '—'
  return `${n.toFixed(1)}%`
}

export function difficultyLabel(d: string): string {
  const m: Record<string, string> = { easy: '简单', medium: '中等', hard: '困难' }
  return m[d] ?? d
}

export function difficultyClass(d: string): string {
  if (d === 'easy') return 'diff-easy'
  if (d === 'medium') return 'diff-medium'
  if (d === 'hard') return 'diff-hard'
  return ''
}

/** 提交记录 status / 详情 result：0 通过 1 CE 2 RE 3 TLE */
export function verdictText(result: number): string {
  switch (result) {
    case 0:
      return '通过'
    case 1:
      return '编译错误'
    case 2:
      return '运行错误'
    case 3:
      return '超时'
    default:
      return '未知'
  }
}

export function verdictClass(result: number): string {
  if (result === 0) return 'verdict-ac'
  return 'verdict-wa'
}

/** Docker 判题接口 code：0 AC, 2 CE, 137 TLE, 3 RE, 4 WA */
export function judgeVerdict(code: number, status: string): string {
  if (code === 0) return '答案正确 (AC)'
  if (code === 2) return '编译错误 (CE)'
  if (code === 137) return '超时 / 超限 (TLE)'
  if (code === 3) return '运行错误 (RE)'
  if (code === 4) return '答案错误 (WA)'
  return status || '评测结束'
}
