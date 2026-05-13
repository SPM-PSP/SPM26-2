import http from './http'
import type { JudgeResponse } from '@/types/api'

export interface JudgeCppBody {
  code: string
  input: string
  answer: string
  language?: string
  timeLimit?: number
  memoryLimit?: string
}

export async function judgeCpp(body: JudgeCppBody) {
  const { data } = await http.post<JudgeResponse>('/api/judge/submit', body)
  return data
}
