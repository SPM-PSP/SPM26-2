import http from './http'
import type { ApiResult, JudgeSubmitResult } from '@/types/api'

/** 与接口文档 5.2 一致：C++、Java */
export type JudgeApiLanguage = 'C++' | 'Java'

export interface JudgeSubmitBody {
  problemId: number
  language: JudgeApiLanguage
  code: string
}

export async function judgeSubmit(body: JudgeSubmitBody) {
  const { data } = await http.post<ApiResult<JudgeSubmitResult>>('/api/v1/judge/submit', body)
  return data
}
