import http from './http'
import type {
  AdminProblemDetail,
  AdminProblemListItem,
  AdminTestCaseAddResult,
  AdminTestCaseFileResult,
  AdminUserListItem,
  ApiResult,
  PageResult,
} from '@/types/api'

const BASE = '/api/v1/admin'

function appendRepeated(key: string, values: string[] | undefined, sp: URLSearchParams) {
  if (!values?.length) return
  for (const v of values) {
    sp.append(key, v)
  }
}

export async function adminUserList(params: {
  page?: number
  size?: number
  keyword?: string
  sortOrder?: string
}) {
  const sp = new URLSearchParams()
  sp.set('page', String(params.page ?? 1))
  sp.set('size', String(params.size ?? 10))
  if (params.keyword) sp.set('keyword', params.keyword)
  if (params.sortOrder) sp.set('sortOrder', params.sortOrder)
  const { data } = await http.get<ApiResult<PageResult<AdminUserListItem>>>(`${BASE}/list?${sp.toString()}`)
  return data
}

export async function adminDeleteUser(userId: number) {
  const { data } = await http.delete<ApiResult<null>>(`${BASE}/user/delete`, { params: { userId } })
  return data
}

export async function adminAddUser(payload: {
  username: string
  password: string
  email: string
  nickname?: string
}) {
  const { data } = await http.post<ApiResult<null>>(`${BASE}/user/add`, payload)
  return data
}

export async function adminAddCategory(name: string) {
  const { data } = await http.post<ApiResult<null>>(`${BASE}/category/add`, { name })
  return data
}

export async function adminDeleteCategory(categoryId: number) {
  const { data } = await http.delete<ApiResult<null>>(`${BASE}/category/delete`, { params: { categoryId } })
  return data
}

export async function adminProblemList(params: {
  page?: number
  size?: number
  problemCategory?: string[]
  difficulty?: string
  keyword?: string
}) {
  const sp = new URLSearchParams()
  sp.set('page', String(params.page ?? 1))
  sp.set('size', String(params.size ?? 20))
  if (params.difficulty) sp.set('difficulty', params.difficulty)
  if (params.keyword) sp.set('keyword', params.keyword)
  appendRepeated('problemCategory', params.problemCategory, sp)
  const { data } = await http.get<ApiResult<PageResult<AdminProblemListItem>>>(
    `${BASE}/problem/list?${sp.toString()}`,
  )
  return data
}

export async function adminProblemDetail(problemId: number) {
  const { data } = await http.get<ApiResult<AdminProblemDetail>>(`${BASE}/problem/detail/${problemId}`)
  return data
}

export interface AdminProblemUpsertBody {
  problemId?: number
  title: string
  difficulty: string
  categoryNames: string[]
  description: string
  inputFormat: string
  outputFormat: string
  sampleInput: string
  sampleOutput: string
  timeLimit: number
  memoryLimit: number
}

export async function adminAddProblem(body: AdminProblemUpsertBody) {
  const { data } = await http.post<ApiResult<null>>(`${BASE}/problem`, body)
  return data
}

export async function adminUpdateProblem(body: AdminProblemUpsertBody & { problemId: number }) {
  const { data } = await http.put<ApiResult<null>>(`${BASE}/problem`, body)
  return data
}

export async function adminDeleteProblem(problemId: number) {
  const { data } = await http.delete<ApiResult<null>>(`${BASE}/problem`, { params: { problemId } })
  return data
}

export async function adminAddTestCase(problemId: number, input: File, output: File) {
  const fd = new FormData()
  fd.append('problemId', String(problemId))
  fd.append('input', input)
  fd.append('output', output)
  const { data } = await http.post<ApiResult<AdminTestCaseAddResult>>(`${BASE}/problem/case/add`, fd)
  return data
}

export async function adminUpdateTestCase(caseId: number, input: File, output: File) {
  const fd = new FormData()
  fd.append('caseId', String(caseId))
  fd.append('input', input)
  fd.append('output', output)
  const { data } = await http.post<ApiResult<AdminTestCaseFileResult>>(`${BASE}/problem/case`, fd)
  return data
}

export async function adminDeleteTestCase(caseId: number) {
  const { data } = await http.delete<ApiResult<null>>(`${BASE}/problem/case`, { params: { caseId } })
  return data
}

export async function adminAddSolution(payload: {
  problemId: number
  title: string
  content: string
  language: string
  code: string
}) {
  const { data } = await http.post<ApiResult<null>>(`${BASE}/solution`, payload)
  return data
}

export async function adminUpdateSolution(payload: {
  solutionId: number
  title: string
  content: string
  language: string
  code: string
}) {
  const { data } = await http.put<ApiResult<null>>(`${BASE}/solution`, payload)
  return data
}

export async function adminDeleteSolution(solutionId: number) {
  const { data } = await http.delete<ApiResult<null>>(`${BASE}/solution`, { params: { solutionId } })
  return data
}
