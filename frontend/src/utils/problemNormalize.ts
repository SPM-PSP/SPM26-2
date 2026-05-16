import type { ProblemDetail } from '@/types/api'

/** 兼容接口文档 snake_case 与 camelCase 字段 */
export function normalizeProblemDetail(raw: Record<string, unknown>): ProblemDetail {
  return {
    problemId: Number(raw.problemId ?? 0),
    title: String(raw.title ?? ''),
    difficulty: String(raw.difficulty ?? 'easy'),
    categoryNames: Array.isArray(raw.categoryNames) ? (raw.categoryNames as string[]) : [],
    description: String(raw.description ?? ''),
    inputFormat: String(raw.inputFormat ?? ''),
    outputFormat: String(raw.outputFormat ?? ''),
    sampleInput: String(raw.sampleInput ?? ''),
    sampleOutput: String(raw.sampleOutput ?? ''),
    timeLimit: Number(raw.timeLimit ?? raw.time_limit ?? 1000),
    memoryLimit: Number(raw.memoryLimit ?? raw.memory_limit ?? 65536),
    acceptRate: (raw.acceptRate ?? raw.passRate ?? 0) as number | string,
    userStatus: raw.userStatus !== undefined ? Number(raw.userStatus) : undefined,
  }
}

export function problemPreviewMarkdown(p: ProblemDetail): string {
  const lines: string[] = [`## ${p.title}`, '', p.description]
  if (p.inputFormat) lines.push('', '### 输入格式', p.inputFormat)
  if (p.outputFormat) lines.push('', '### 输出格式', p.outputFormat)
  if (p.sampleInput) lines.push('', '### 样例输入', '```', p.sampleInput, '```')
  if (p.sampleOutput) lines.push('', '### 样例输出', '```', p.sampleOutput, '```')
  return lines.join('\n')
}
