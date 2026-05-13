import http from './http'
import type { AiEvaluationDetail, ApiResult } from '@/types/api'

/** 文档 6.3：获取指定提交的 AI 多维评价 */
export async function fetchAiEvaluation(submissionId: number) {
  const { data } = await http.get<ApiResult<AiEvaluationDetail>>(
    `/api/v1/ai/evaluation/${submissionId}`,
  )
  return data
}
