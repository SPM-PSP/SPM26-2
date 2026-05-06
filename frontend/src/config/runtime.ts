/** 演示模式：不请求真实认证接口，返回与《03-接口文档》一致的结构（任意非空或空均可登录） */
export function isAuthMockEnabled(): boolean {
  return import.meta.env.VITE_AUTH_MOCK === 'true'
}
