import http from './http'
import type { JudgeResponse } from '@/types/api'

export interface JudgeCppBody {
  code: string
  input: string
  answer: string
  timeLimit?: number
  memoryLimit?: string
}

export async function judgeCpp(body: JudgeCppBody) {
  const { data } = await http.post<JudgeResponse>('/api/judge/cpp', body)
  return data
}
