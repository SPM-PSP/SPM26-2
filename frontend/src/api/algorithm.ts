import http from './http'
import type { CodeAnalysisResponse, ProblemGenerateResponse } from '@/types/api'

/** 后端直接返回 DTO，无 Result 包装 */
export async function generateProblem(payload: {
  plate: string
  difficulty: string
  targetLanguage: string
}) {
  const { data } = await http.post<ProblemGenerateResponse>('/api/algorithm/generate-problem', payload)
  return data
}

export async function analyzeCode(code: string, language: string) {
  const { data } = await http.post<CodeAnalysisResponse>('/api/algorithm/analyze-code', { code, language })
  return data
}
