import http from './http'
import type { AiEvaluationDetail, ApiResult, ProblemDetail } from '@/types/api'

/** 文档 6.2：AI 生成题目（入库后返回题目详情含 problemId） */
export async function aiCreateProblem(body: { difficulty: string; categoryNames: string[] }) {
  const { data } = await http.post<ApiResult<ProblemDetail>>('/api/v1/ai/judge/create', body)
  return data
}

/** 文档 6.3：获取指定提交的 AI 多维评价 */
export async function fetchAiEvaluation(submissionId: number) {
  const { data } = await http.get<ApiResult<AiEvaluationDetail>>(
    `/api/v1/ai/evaluation/${submissionId}`,
  )
  return data
}
