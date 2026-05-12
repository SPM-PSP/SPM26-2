<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { isAuthMockEnabled } from '@/config/runtime'
import { AUTH_LOGIN_PATH } from '@/constants/authEndpoints'
import { validateLoginPayload } from '@/utils/authValidators'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const err = ref('')
const loading = ref(false)

const mockMode = computed(() => isAuthMockEnabled())

async function onSubmit() {
  err.value = ''
  if (!mockMode.value) {
    const v = validateLoginPayload({ username: username.value, password: password.value })
    if (!v.ok) {
      err.value = v.message
      return
    }
  }
  loading.value = true
  try {
    const res = await login(username.value, password.value)
    if (res.code !== 200 || !res.data) {
      err.value = res.message || '登录失败'
      return
    }
    auth.setSession(res.data)
    const redirect = (route.query.redirect as string) || '/problems'
    await router.replace(redirect)
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <aside class="hero">
      <div class="hero-inner">
        <div class="logo-mark" />
        <h2 class="hero-title">OJ<span class="accent">Code</span></h2>
        <p class="hero-desc">智能在线编程练习平台 — 题库、评测与 AI 辅助学习与《03-接口文档》对齐。</p>
        <ul class="bullets">
          <li>登录接口：<code>{{ AUTH_LOGIN_PATH }}</code></li>
          <li>请求体字段：<code>username</code>、<code>password</code>（application/json）</li>
        </ul>
      </div>
    </aside>
    <section class="panel">
      <div class="panel-card">
        <p v-if="mockMode" class="mock-banner">演示模式已开启：任意内容均可登录（响应结构同文档 2.3.3）</p>
        <h1 class="title">账号登录</h1>
        <p class="subtitle">使用用户名与密码登录</p>
        <form class="form" @submit.prevent="onSubmit">
          <div class="field">
            <label for="login-user">用户名</label>
            <input
              id="login-user"
              v-model="username"
              name="username"
              type="text"
              autocomplete="username"
              placeholder="文档字段 username"
            />
          </div>
          <div class="field">
            <label for="login-pass">密码</label>
            <input
              id="login-pass"
              v-model="password"
              name="password"
              type="password"
              autocomplete="current-password"
              placeholder="文档字段 password"
            />
          </div>
          <p v-if="err" class="err">{{ err }}</p>
          <button type="submit" class="submit" :disabled="loading">
            {{ loading ? '登录中…' : '登录' }}
          </button>
        </form>
        <p class="switch">
          没有账号？
          <RouterLink class="link" to="/register">注册</RouterLink>
        </p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: calc(100vh - 52px);
  display: grid;
  grid-template-columns: 1fr 1fr;
  max-width: 960px;
  margin: 0 auto;
  align-items: stretch;
}

@media (max-width: 800px) {
  .auth-page {
    grid-template-columns: 1fr;
  }
  .hero {
    min-height: 200px;
  }
}

.hero {
  background: linear-gradient(160deg, #1a1208 0%, #101010 55%, #161616 100%);
  border: 1px solid var(--lc-border);
  border-right: none;
  border-radius: 12px 0 0 12px;
  padding: 40px 36px;
  display: flex;
  align-items: center;
}

@media (max-width: 800px) {
  .hero {
    border-right: 1px solid var(--lc-border);
    border-radius: 12px 12px 0 0;
    border-bottom: none;
  }
}

.hero-inner {
  max-width: 360px;
}

.logo-mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b35);
  margin-bottom: 16px;
}

.hero-title {
  margin: 0 0 12px;
  font-size: 1.75rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.accent {
  color: var(--lc-accent);
}

.hero-desc {
  margin: 0 0 20px;
  color: var(--lc-text-muted);
  font-size: 0.92rem;
  line-height: 1.55;
}

.bullets {
  margin: 0;
  padding-left: 18px;
  color: var(--lc-text-muted);
  font-size: 0.82rem;
  line-height: 1.7;
}

.bullets code {
  font-size: 0.78em;
  color: var(--lc-accent);
}

.panel {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 0 12px 12px 0;
  padding: 40px 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (max-width: 800px) {
  .panel {
    border-radius: 0 0 12px 12px;
    border-top: none;
  }
}

.panel-card {
  width: 100%;
  max-width: 360px;
}

.mock-banner {
  margin: 0 0 16px;
  padding: 10px 12px;
  border-radius: 8px;
  background: rgba(255, 161, 22, 0.1);
  border: 1px solid rgba(255, 161, 22, 0.35);
  color: var(--lc-accent);
  font-size: 0.8rem;
  line-height: 1.45;
}

.title {
  margin: 0 0 6px;
  font-size: 1.45rem;
  font-weight: 700;
}

.subtitle {
  margin: 0 0 24px;
  font-size: 0.88rem;
  color: var(--lc-text-muted);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.field {
  margin-bottom: 4px;
}

.field label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.field input {
  width: 100%;
  padding: 11px 12px;
  border-radius: 8px;
  font-size: 0.95rem;
}

.err {
  margin: 8px 0 0;
  color: var(--lc-red);
  font-size: 0.85rem;
}

.submit {
  margin-top: 18px;
  width: 100%;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: var(--lc-accent);
  color: #111;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
}

.submit:hover:not(:disabled) {
  filter: brightness(1.06);
}

.submit:disabled {
  opacity: 0.65;
  cursor: wait;
}

.switch {
  margin: 22px 0 0;
  text-align: center;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

.link {
  color: var(--lc-accent);
  font-weight: 600;
}

.link:hover {
  text-decoration: underline;
}
</style>
