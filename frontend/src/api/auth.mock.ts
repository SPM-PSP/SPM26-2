import type { ApiResult, LoginVO } from '@/types/api'

function delay(ms: number) {
  return new Promise((r) => setTimeout(r, ms))
}

/** 文档 2.3.3 成功响应 data 结构 */
function buildMockLoginVO(username: string, password: string): LoginVO {
  const nick = username.trim() || '演示用户'
  const exp = new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)
  const pad = (n: number) => String(n).padStart(2, '0')
  const expireTime = `${exp.getFullYear()}-${pad(exp.getMonth() + 1)}-${pad(exp.getDate())} ${pad(exp.getHours())}:${pad(exp.getMinutes())}:${pad(exp.getSeconds())}`
  const tokenPayload = JSON.stringify({ userId: 10001, mock: true, u: nick, pLen: password.length })
  const token = `mock.${typeof btoa !== 'undefined' ? btoa(unescape(encodeURIComponent(tokenPayload))) : tokenPayload}`

  return {
    userId: 10001,
    role: 'user',
    nickname: nick,
    avatar: 'https://assets.leetcode.com/users/avatar.png',
    token,
    expireTime,
  }
}

/**
 * 演示用：不访问网络，返回与文档一致的 Result 包装。
 * 路径常量仅用于日志说明，与真实接口路径一致。
 */
export async function mockLogin(username: string, password: string): Promise<ApiResult<LoginVO>> {
  await delay(280)
  return {
    code: 200,
    message: '登录成功',
    data: buildMockLoginVO(username, password),
  }
}

/** 文档 2.2.3 成功：code 200，message 注册成功，data 可为 null */
export async function mockRegister(): Promise<ApiResult<null>> {
  await delay(320)
  return {
    code: 200,
    message: '注册成功',
    data: null,
  }
}
