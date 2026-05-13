<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { useAuthStore } from '@/stores/auth'
import { validateRegisterPayload } from '@/utils/authValidators'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const email = ref('')
const nickname = ref('')
const err = ref('')
const ok = ref('')
const loading = ref(false)

onMounted(() => {
  if (auth.isLoggedIn) {
    void router.replace('/problems')
  }
})

async function onSubmit() {
  err.value = ''
  ok.value = ''
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
      <div class="brand">
        <span class="mark" />
        <span class="name">OJ<span class="accent">Code</span></span>
      </div>
      <h1>注册</h1>
      <form class="form" @submit.prevent="onSubmit">
        <label for="reg-user">用户名</label>
        <input id="reg-user" v-model="username" type="text" autocomplete="username" maxlength="20" placeholder="3–20 位" />
        <label for="reg-email">邮箱</label>
        <input id="reg-email" v-model="email" type="email" autocomplete="email" placeholder="常用邮箱" />
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
        <input id="reg-nick" v-model="nickname" type="text" maxlength="20" placeholder="不填则与用户名相同" />
        <p v-if="err" class="err">{{ err }}</p>
        <p v-if="ok" class="ok">{{ ok }}</p>
        <button type="submit" class="submit" :disabled="loading">{{ loading ? '提交中…' : '注册' }}</button>
      </form>
      <p class="foot">
        已有账号？
        <RouterLink to="/login">登录</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.wrap {
  min-height: calc(100vh - 52px);
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
  margin-top: 6px;
  color: var(--lc-red);
  font-size: 0.85rem;
}

.ok {
  margin-top: 6px;
  color: var(--lc-green);
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
