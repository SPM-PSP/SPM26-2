/**
 * 与《03-接口文档》「2. 认证管理」及后端实现一致
 */
export const AUTH_API_BASE = '/api/v1/auth' as const
export const AUTH_LOGIN_PATH = `${AUTH_API_BASE}/login` as const
export const AUTH_REGISTER_PATH = `${AUTH_API_BASE}/register` as const
