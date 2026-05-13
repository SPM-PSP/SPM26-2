import http from './http'
import type { ApiResult, LoginVO } from '@/types/api'
import { AUTH_LOGIN_PATH, AUTH_REGISTER_PATH } from '@/constants/authEndpoints'

export async function login(username: string, password: string): Promise<ApiResult<LoginVO>> {
  const { data } = await http.post<ApiResult<LoginVO>>(AUTH_LOGIN_PATH, { username, password })
  return data
}

export async function register(payload: {
  username: string
  password: string
  email: string
  nickname?: string
}): Promise<ApiResult<null>> {
  const { data } = await http.post<ApiResult<null>>(AUTH_REGISTER_PATH, payload)
  return data
}
