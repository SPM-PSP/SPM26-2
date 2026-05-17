<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { validateLoginPayload } from '@/utils/authValidators'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const err = ref('')
const loading = ref(false)

onMounted(() => {
  if (auth.isLoggedIn) {
    void router.replace('/problems')
  }
})

async function onSubmit() {
  err.value = ''
  const v = validateLoginPayload({ username: username.value, password: password.value })
  if (!v.ok) {
    err.value = v.message
    return
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
  <div class="wrap">
    <div class="card">
      <div class="card-badge" aria-hidden="true">
        <svg viewBox="0 0 24 24" width="22" height="22">
          <path
            fill="currentColor"
            d="M12 1 3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 10.99h7c-.53 4.12-3.28 7.79-7 8.94V12H5V6.3l7-3.11v8.8z"
          />
        </svg>
      </div>
      <div class="brand">
        <span class="mark" />
        <span class="name">OJ<span class="accent">Code</span></span>
      </div>
      <h1>欢迎回来</h1>
      <p class="lead">登录后即可刷题、提交代码并使用 AI 实验室</p>
      <form class="form" @submit.prevent="onSubmit">
        <label for="login-user">
          <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
            <path
              fill="currentColor"
              d="M12 12c2.7 0 5-2.3 5-5s-2.3-5-5-5-5 2.3-5 5 2.3 5 5 5zm0 2c-3.3 0-10 1.7-10 5v3h20v-3c0-3.3-6.7-5-10-5z"
            />
          </svg>
          用户名
        </label>
        <input id="login-user" v-model="username" type="text" autocomplete="username" placeholder="请输入用户名" />
        <label for="login-pass">
          <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
            <path
              fill="currentColor"
              d="M18 8h-1V6a5 5 0 0 0-10 0v2H6a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V10a2 2 0 0 0-2-2zm-6 9a2 2 0 1 1 0-4 2 2 0 0 1 0 4zm3-9H9V6a3 3 0 0 1 6 0v2z"
            />
          </svg>
          密码
        </label>
        <input
          id="login-pass"
          v-model="password"
          type="password"
          autocomplete="current-password"
          placeholder="请输入密码"
        />
        <p v-if="err" class="err">
          <svg viewBox="0 0 24 24" width="14" height="14" aria-hidden="true">
            <path fill="currentColor" d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z" />
          </svg>
          {{ err }}
        </p>
        <button type="submit" class="submit" :disabled="loading">
          <span v-if="loading" class="spinner" aria-hidden="true" />
          {{ loading ? '登录中…' : '登录' }}
        </button>
      </form>
      <p class="foot">
        没有账号？
        <RouterLink to="/register">立即注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 24px;
}

.card {
  position: relative;
  width: 100%;
  background: linear-gradient(160deg, rgba(20, 24, 32, 0.98), rgba(14, 18, 26, 0.98));
  border: 1px solid rgba(255, 161, 22, 0.2);
  border-radius: 16px;
  padding: 40px 36px 36px;
  box-shadow:
    0 24px 48px rgba(0, 0, 0, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.card-badge {
  position: absolute;
  top: -14px;
  right: 24px;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  color: #0c0e14;
  box-shadow: 0 8px 24px rgba(255, 161, 22, 0.35);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.mark {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  box-shadow: 0 0 24px rgba(255, 161, 22, 0.3);
}

.name {
  font-weight: 800;
  font-size: 1.35rem;
}

.accent {
  color: var(--lc-accent);
}

h1 {
  margin: 0 0 8px;
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.lead {
  margin: 0 0 28px;
  font-size: 0.88rem;
  color: var(--lc-text-muted);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-top: 12px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

label:first-of-type {
  margin-top: 0;
}

label svg {
  opacity: 0.75;
}

input {
  padding: 13px 14px;
  border-radius: 10px;
  font-size: 0.95rem;
  border-color: rgba(42, 51, 68, 0.9);
}

.err {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  margin-top: 12px;
  color: var(--lc-red);
  font-size: 0.85rem;
  line-height: 1.4;
}

.err svg {
  flex-shrink: 0;
  margin-top: 2px;
}

.submit {
  margin-top: 22px;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--lc-accent), #ff8c42);
  color: #0c0e14;
  font-weight: 700;
  font-size: 0.98rem;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.15s, box-shadow 0.15s;
}

.submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 28px rgba(255, 161, 22, 0.35);
}

.submit:disabled {
  opacity: 0.65;
  cursor: wait;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(12, 14, 20, 0.25);
  border-top-color: #0c0e14;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.foot {
  margin-top: 26px;
  text-align: center;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

.foot a {
  color: var(--lc-accent);
  font-weight: 600;
}
</style>
