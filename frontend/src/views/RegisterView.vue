<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'

const router = useRouter()
const username = ref('')
const password = ref('')
const email = ref('')
const nickname = ref('')
const err = ref('')
const ok = ref('')
const loading = ref(false)

async function onSubmit() {
  err.value = ''
  ok.value = ''
  loading.value = true
  try {
    const res = await register({
      username: username.value,
      password: password.value,
      email: email.value,
      nickname: nickname.value || undefined,
    })
    if (res.code !== 200) {
      err.value = res.message || '注册失败'
      return
    }
    ok.value = res.message || '注册成功'
    setTimeout(() => {
      void router.replace('/login')
    }, 800)
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
      <h1>注册</h1>
      <p class="sub">对接 <code>/api/v1/auth/register</code></p>
      <form @submit.prevent="onSubmit">
        <label>用户名</label>
        <input v-model="username" required minlength="3" maxlength="20" />
        <label>邮箱</label>
        <input v-model="email" type="email" required />
        <label>密码（8–20 位，含字母与数字）</label>
        <input v-model="password" type="password" required minlength="8" maxlength="20" />
        <label>昵称（可选）</label>
        <input v-model="nickname" maxlength="20" />
        <p v-if="err" class="err">{{ err }}</p>
        <p v-if="ok" class="ok">{{ ok }}</p>
        <button type="submit" class="btn" :disabled="loading">{{ loading ? '提交中…' : '注册' }}</button>
      </form>
      <p class="foot">
        已有账号？
        <RouterLink to="/login">去登录</RouterLink>
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

.ok {
  color: var(--lc-green);
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
