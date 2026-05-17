import http from './http'
import type { ApiResult, CodeAnalysisResponse } from '@/types/api'

export interface CodeAnalysisRequest {
  code: string
  language: string
}

/** 调用后端已有的代码分析接口 */
export async function analyzeCode(request: CodeAnalysisRequest) {
  const { data } = await http.post<CodeAnalysisResponse>(
      '/api/algorithm/analyze-code',
      request,
  )
  return data
}
