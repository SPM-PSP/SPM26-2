export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface LoginVO {
  userId: number
  role: string
  nickname: string
  avatar: string
  token: string
  expireTime: string
}

export interface CategoryVO {
  categoryId: number
  categoryName: string
}

export interface ProblemListItem {
  problemId: number
  title: string
  difficulty: string
  categoryNames: string[]
  acceptRate: number | string
  status?: number
}

export interface PageResult<T> {
  total: number
  pages: number
  currentPage: number
  list: T[]
}

export interface ProblemDetail {
  problemId: number
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
  acceptRate: number | string
  userStatus?: number
}

export interface SolutionItem {
  solutionId: number
  title: string
  content: string
  language: string
  code: string
  createUserName: string
  createTime: string
}

export interface ProblemSolution {
  problemId: number
  solution: SolutionItem[] | null
}

export interface JudgeResponse {
  code: number
  status: string
  message: string
  compileLog?: string
  runtimeLog?: string
  diffLog?: string
  userOutput?: string
  formattedAnswer?: string
  formattedOutput?: string
}

export interface SubmissionListItem {
  submissionId: number
  problemId: number
  problemTitle: string
  problemCategory: string
  status: number
  submitTime: string
}

export interface SubmissionDetail {
  submissionId: number
  problemId: number
  problemTitle: string
  language: string
  code: string
  result: number
  passCount: number
  totalCount: number
  runTime: number
  memory: number
  errorMsg: string
  submitTime: string
}

export interface UserInfo {
  userId: number
  username: string
  nickname: string
  avatar: string
  email: string
  phone?: string
}

export interface ProblemGenerateResponse {
  problemName: string
  problemDesc: string
  sampleInput: string[]
  sampleOutput: string[]
  inputFormat: string
  outputFormat: string
}

export interface CodeAnalysisResponse {
  plateCategory: string
  complexityAnalysis: string
  codeStyleAnalysis: string
}
