<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const err = ref('')
const loading = ref(false)

async function onSubmit() {
  err.value = ''
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
      <h1>登录</h1>
      <p class="sub">使用用户名与密码登录，与后端 <code>/api/v1/auth/login</code> 一致。</p>
      <form @submit.prevent="onSubmit">
        <label>用户名</label>
        <input v-model="username" required autocomplete="username" />
        <label>密码</label>
        <input v-model="password" type="password" required autocomplete="current-password" />
        <p v-if="err" class="err">{{ err }}</p>
        <button type="submit" class="btn" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</button>
      </form>
      <p class="foot">
        没有账号？
        <RouterLink to="/register">去注册</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 400px;
  margin: 40px auto;
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 28px;
}

h1 {
  margin: 0 0 8px;
  font-size: 1.35rem;
}

.sub {
  margin: 0 0 20px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  font-size: 0.8rem;
  color: var(--lc-text-muted);
  margin-top: 8px;
}

input {
  padding: 10px 12px;
}

.err {
  color: var(--lc-red);
  font-size: 0.85rem;
}

.btn {
  margin-top: 16px;
  padding: 10px;
  border: none;
  border-radius: 8px;
  background: var(--lc-accent);
  color: #111;
  font-weight: 600;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.foot {
  margin-top: 18px;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

code {
  font-size: 0.75em;
  color: var(--lc-accent);
}
</style>
