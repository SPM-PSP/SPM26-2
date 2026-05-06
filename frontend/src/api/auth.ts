import http from './http'
import type { ApiResult, LoginVO } from '@/types/api'

export async function login(username: string, password: string) {
  const { data } = await http.post<ApiResult<LoginVO>>('/api/v1/auth/login', { username, password })
  return data
}

export async function register(payload: {
  username: string
  password: string
  email: string
  nickname?: string
}) {
  const { data } = await http.post<ApiResult<null>>('/api/v1/auth/register', payload)
  return data
}
