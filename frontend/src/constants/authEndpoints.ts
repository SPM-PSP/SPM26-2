/**
 * 认证模块路径 —— 与《03-接口文档》「2. 认证管理」一致
 * 基础路径文档写作 `/auth`，实际后端为 `/api/v1/auth`
 */
export const AUTH_API_BASE = '/api/v1/auth' as const
export const AUTH_LOGIN_PATH = `${AUTH_API_BASE}/login` as const
export const AUTH_REGISTER_PATH = `${AUTH_API_BASE}/register` as const
export const AUTH_LOGOUT_PATH = `${AUTH_API_BASE}/logout` as const
