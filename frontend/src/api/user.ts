import http from './http'
import type { ApiResult, PageResult, SubmissionDetail, SubmissionListItem, UserInfo } from '@/types/api'

function appendRepeated(key: string, values: string[] | undefined, sp: URLSearchParams) {
  if (!values?.length) return
  for (const v of values) {
    sp.append(key, v)
  }
}

export async function fetchUserInfo() {
  const { data } = await http.get<ApiResult<UserInfo>>('/api/v1/user/info')
  return data
}

export interface SubmissionListParams {
  page?: number
  size?: number
  result?: number
  keyword?: string
  problemCategory?: string[]
  sortOrder?: string
}

export async function fetchSubmissionList(params: SubmissionListParams) {
  const sp = new URLSearchParams()
  sp.set('page', String(params.page ?? 1))
  sp.set('size', String(params.size ?? 10))
  if (params.result !== undefined && params.result !== null) sp.set('result', String(params.result))
  if (params.keyword) sp.set('keyword', params.keyword)
  if (params.sortOrder) sp.set('sortOrder', params.sortOrder)
  appendRepeated('problemCategory', params.problemCategory, sp)
  const { data } = await http.get<ApiResult<PageResult<SubmissionListItem>>>(
    `/api/v1/user/submission/list?${sp.toString()}`,
  )
  return data
}

export async function fetchSubmissionDetail(submissionId: number) {
  const { data } = await http.get<ApiResult<SubmissionDetail>>(
    `/api/v1/user/study/statistics/detail/${submissionId}`,
  )
  return data
}
