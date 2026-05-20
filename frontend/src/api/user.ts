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
      `/api/v1/user/detail/${submissionId}`,
  )
  return data
}

/**
 * 更新用户信息
 */
export interface UpdateUserInfoParams {
  nickname?: string
  phone?: string
}

export async function updateUserInfo(params: UpdateUserInfoParams) {
  const { data } = await http.put<ApiResult<null>>('/api/v1/user/info', params)
  return data
}

/**
 * 上传头像
 */
export async function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  
  const { data } = await http.post<ApiResult<{ avatarUrl: string; avatarSize: number; avatarType: string }>>(
    '/api/v1/user/avatar/upload',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  )
  return data
}
