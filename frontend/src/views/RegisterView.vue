<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { validateRegisterPayload } from '@/utils/authValidators'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const email = ref('')
const nickname = ref('')
const err = ref('')
const loading = ref(false)

onMounted(() => {
  if (auth.isLoggedIn) {
    void router.replace('/problems')
  }
})

async function onSubmit() {
  err.value = ''
  const v = validateRegisterPayload({
    username: username.value,
    password: password.value,
    email: email.value,
  })
  if (!v.ok) {
    err.value = v.message
    return
  }
  loading.value = true
  try {
    const res = await register({
      username: username.value.trim(),
      password: password.value,
      email: email.value.trim(),
      nickname: nickname.value.trim() || undefined,
    })
    if (res.code !== 200) {
      err.value = res.message || '注册失败'
      return
    }
    await router.replace('/login')
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
            d="M15 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm-9-2V7H4v3H1v2h3v3h2v-3h3v-2H6zm9 4c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"
          />
        </svg>
      </div>
      <div class="brand">
        <span class="mark" />
        <span class="name">OJ<span class="accent">Code</span></span>
      </div>
      <h1>创建账号</h1>
      <p class="lead">加入平台，开始你的算法练习之旅</p>
      <form class="form" @submit.prevent="onSubmit">
        <label for="reg-user">用户名</label>
        <input id="reg-user" v-model="username" type="text" autocomplete="username" maxlength="20" placeholder="3–20 位" />
        <label for="reg-email">邮箱</label>
        <input id="reg-email" v-model="email" type="email" autocomplete="email" placeholder="name@example.com" />
        <label for="reg-pass">密码</label>
        <input
          id="reg-pass"
          v-model="password"
          type="password"
          autocomplete="new-password"
          maxlength="20"
          placeholder="8–20 位，含字母与数字"
        />
        <label for="reg-nick">昵称（可选）</label>
        <input id="reg-nick" v-model="nickname" type="text" maxlength="20" placeholder="展示名称" />
        <p v-if="err" class="err">{{ err }}</p>
        <button type="submit" class="submit" :disabled="loading">
          <span v-if="loading" class="spinner" aria-hidden="true" />
          {{ loading ? '提交中…' : '注册' }}
        </button>
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
  border: 1px solid rgba(91, 124, 250, 0.25);
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
  background: linear-gradient(135deg, #5b7cfa, #7c5cbf);
  color: #fff;
  box-shadow: 0 8px 24px rgba(91, 124, 250, 0.35);
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
  background: linear-gradient(135deg, #5b7cfa, #7c5cbf);
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
}

.lead {
  margin: 0 0 24px;
  font-size: 0.88rem;
  color: var(--lc-text-muted);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label {
  margin-top: 10px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

label:first-of-type {
  margin-top: 0;
}

input {
  padding: 12px 14px;
  border-radius: 10px;
  font-size: 0.95rem;
}

.err {
  margin-top: 12px;
  color: var(--lc-red);
  font-size: 0.85rem;
}

.submit {
  margin-top: 20px;
  width: 100%;
  padding: 14px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #5b7cfa, #7c5cbf);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: transform 0.15s, box-shadow 0.15s;
}

.submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 28px rgba(91, 124, 250, 0.35);
}

.submit:disabled {
  opacity: 0.65;
  cursor: wait;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.foot {
  margin-top: 24px;
  text-align: center;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

.foot a {
  color: #8aa4ff;
  font-weight: 600;
}
</style>
