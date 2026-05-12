import http from './http'
import type { ApiResult, LoginVO } from '@/types/api'
import { AUTH_LOGIN_PATH, AUTH_REGISTER_PATH } from '@/constants/authEndpoints'
import { isAuthMockEnabled } from '@/config/runtime'
import { mockLogin, mockRegister } from './auth.mock'

/** 文档 2.3：POST /api/v1/auth/login，JSON：username, password */
export async function login(username: string, password: string): Promise<ApiResult<LoginVO>> {
  if (isAuthMockEnabled()) {
    return mockLogin(username, password)
  }
  const { data } = await http.post<ApiResult<LoginVO>>(AUTH_LOGIN_PATH, { username, password })
  return data
}

/** 文档 2.2：POST /api/v1/auth/register，JSON：username, password, email, nickname? */
export async function register(payload: {
  username: string
  password: string
  email: string
  nickname?: string
}): Promise<ApiResult<null>> {
  if (isAuthMockEnabled()) {
    return mockRegister()
  }
  const { data } = await http.post<ApiResult<null>>(AUTH_REGISTER_PATH, payload)
  return data
}
