import http from './http'
import type { ApiResult, CategoryVO, PageResult, ProblemDetail, ProblemListItem, ProblemSolution } from '@/types/api'

function appendRepeated(key: string, values: string[] | undefined, sp: URLSearchParams) {
  if (!values?.length) return
  for (const v of values) {
    sp.append(key, v)
  }
}

export async function fetchCategories() {
  const { data } = await http.get<ApiResult<CategoryVO[]>>('/api/v1/problem/category/list')
  return data
}

export interface ProblemListParams {
  page?: number
  size?: number
  problemCategory?: string[]
  difficulty?: string
  keyword?: string
  status?: number
}

export async function fetchProblemList(params: ProblemListParams) {
  const sp = new URLSearchParams()
  sp.set('page', String(params.page ?? 1))
  sp.set('size', String(params.size ?? 20))
  if (params.difficulty) sp.set('difficulty', params.difficulty)
  if (params.keyword) sp.set('keyword', params.keyword)
  if (params.status !== undefined && params.status !== null) sp.set('status', String(params.status))
  appendRepeated('problemCategory', params.problemCategory, sp)
  const { data } = await http.get<ApiResult<PageResult<ProblemListItem>>>(
    `/api/v1/problem/list?${sp.toString()}`,
  )
  return data
}

export async function fetchProblemDetail(problemId: number) {
  const { data } = await http.get<ApiResult<ProblemDetail>>(`/api/v1/problem/detail/${problemId}`)
  return data
}

export async function fetchProblemSolution(problemId: number) {
  const { data } = await http.get<ApiResult<ProblemSolution>>(`/api/v1/problem/solution/${problemId}`)
  return data
}
