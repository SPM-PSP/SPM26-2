<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { validateLoginPayload } from '@/utils/authValidators'

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
      <div class="brand">
        <span class="mark" />
        <span class="name">OJ<span class="accent">Code</span></span>
      </div>
      <h1>登录</h1>
      <form class="form" @submit.prevent="onSubmit">
        <label for="login-user">用户名</label>
        <input
          id="login-user"
          v-model="username"
          type="text"
          autocomplete="username"
          placeholder="请输入用户名"
        />
        <label for="login-pass">密码</label>
        <input
          id="login-pass"
          v-model="password"
          type="password"
          autocomplete="current-password"
          placeholder="请输入密码"
        />
        <p v-if="err" class="err">{{ err }}</p>
        <button type="submit" class="submit" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</button>
      </form>
      <p class="foot">
        没有账号？
        <RouterLink to="/register">注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.card {
  width: 100%;
  max-width: 400px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 32px 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
}

.mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b35);
}

.name {
  font-weight: 800;
  font-size: 1.25rem;
}

.accent {
  color: var(--lc-accent);
}

h1 {
  margin: 0 0 20px;
  font-size: 1.35rem;
  font-weight: 700;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  margin-top: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

label:first-of-type {
  margin-top: 0;
}

input {
  padding: 11px 12px;
  border-radius: 8px;
  font-size: 0.95rem;
}

.err {
  margin-top: 8px;
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

.foot {
  margin-top: 22px;
  text-align: center;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

.foot a {
  color: var(--lc-accent);
  font-weight: 600;
}
</style>
