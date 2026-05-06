/**
 * 与《03-接口文档》2.2 / 2.3 参数说明一致的前端校验（仅真实请求时使用）
 */
const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function validateRegisterPayload(input: {
  username: string
  password: string
  email: string
}): { ok: true } | { ok: false; message: string } {
  const u = input.username.trim()
  if (u.length < 3 || u.length > 20) {
    return { ok: false, message: '用户名长度应为 3–20 位' }
  }
  const p = input.password
  if (p.length < 8 || p.length > 20) {
    return { ok: false, message: '密码长度应为 8–20 位' }
  }
  if (!/[A-Za-z]/.test(p) || !/\d/.test(p)) {
    return { ok: false, message: '密码须同时包含字母与数字' }
  }
  if (!EMAIL_RE.test(input.email.trim())) {
    return { ok: false, message: '邮箱格式不符合规范' }
  }
  return { ok: true }
}

export function validateLoginPayload(input: { username: string; password: string }): { ok: true } | { ok: false; message: string } {
  if (!input.username.trim()) {
    return { ok: false, message: '请输入用户名' }
  }
  if (!input.password) {
    return { ok: false, message: '请输入密码' }
  }
  return { ok: true }
}
