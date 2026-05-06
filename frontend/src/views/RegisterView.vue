<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { isAuthMockEnabled } from '@/config/runtime'
import { AUTH_REGISTER_PATH } from '@/constants/authEndpoints'
import { validateRegisterPayload } from '@/utils/authValidators'

const router = useRouter()

const username = ref('')
const password = ref('')
const email = ref('')
const nickname = ref('')
const err = ref('')
const ok = ref('')
const loading = ref(false)

const mockMode = computed(() => isAuthMockEnabled())

async function onSubmit() {
  err.value = ''
  ok.value = ''
  if (!mockMode.value) {
    const v = validateRegisterPayload({
      username: username.value,
      password: password.value,
      email: email.value,
    })
    if (!v.ok) {
      err.value = v.message
      return
    }
  }
  loading.value = true
  try {
    const payload = mockMode.value
      ? {
          username: username.value.trim() || 'guest',
          password: password.value || 'Demo123456',
          email: email.value.trim() || 'guest@example.com',
          nickname: nickname.value.trim() || undefined,
        }
      : {
          username: username.value.trim(),
          password: password.value,
          email: email.value.trim(),
          nickname: nickname.value.trim() || undefined,
        }
    const res = await register(payload)
    if (res.code !== 200) {
      err.value = res.message || '注册失败'
      return
    }
    ok.value = res.message || '注册成功'
    setTimeout(() => {
      void router.replace('/login')
    }, 900)
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
        <h2 class="hero-title">创建账号</h2>
        <p class="hero-desc">注册接口与文档 2.2 一致：校验账号与邮箱唯一性，密码加密存储由后端完成。</p>
        <ul class="bullets">
          <li>注册接口：<code>{{ AUTH_REGISTER_PATH }}</code></li>
          <li>必填：<code>username</code>、<code>password</code>、<code>email</code></li>
          <li>可选：<code>nickname</code>（不传则默认与用户名一致，见文档说明）</li>
        </ul>
      </div>
    </aside>
    <section class="panel">
      <div class="panel-card">
        <p v-if="mockMode" class="mock-banner">演示模式：不请求后端，直接返回文档 2.2.3 成功结构</p>
        <h1 class="title">注册</h1>
        <p class="subtitle">填写下列字段（与接口 JSON 字段名一致）</p>
        <form class="form" @submit.prevent="onSubmit">
          <div class="field">
            <label for="reg-user">用户名 <span class="req">*</span></label>
            <input
              id="reg-user"
              v-model="username"
              name="username"
              type="text"
              autocomplete="username"
              maxlength="20"
              placeholder="3–20 位，唯一（演示模式可随意）"
            />
          </div>
          <div class="field">
            <label for="reg-email">邮箱 <span class="req">*</span></label>
            <input
              id="reg-email"
              v-model="email"
              name="email"
              type="email"
              autocomplete="email"
              placeholder="符合邮箱格式（演示模式可随意）"
            />
          </div>
          <div class="field">
            <label for="reg-pass">密码 <span class="req">*</span></label>
            <input
              id="reg-pass"
              v-model="password"
              name="password"
              type="password"
              autocomplete="new-password"
              maxlength="20"
              placeholder="8–20 位，含字母与数字（演示模式可随意）"
            />
          </div>
          <div class="field">
            <label for="reg-nick">昵称</label>
            <input
              id="reg-nick"
              v-model="nickname"
              name="nickname"
              type="text"
              maxlength="20"
              placeholder="可选，对应 nickname"
            />
          </div>
          <p v-if="err" class="err">{{ err }}</p>
          <p v-if="ok" class="ok">{{ ok }}</p>
          <button type="submit" class="submit" :disabled="loading">
            {{ loading ? '提交中…' : '注册' }}
          </button>
        </form>
        <p class="switch">
          已有账号？
          <RouterLink class="link" to="/login">登录</RouterLink>
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
}

.hero {
  background: linear-gradient(200deg, #101010 0%, #1a1510 50%, #161616 100%);
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
    min-height: auto;
  }
}

.hero-inner {
  max-width: 380px;
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
  font-size: 1.6rem;
  font-weight: 800;
}

.hero-desc {
  margin: 0 0 18px;
  color: var(--lc-text-muted);
  font-size: 0.9rem;
  line-height: 1.55;
}

.bullets {
  margin: 0;
  padding-left: 18px;
  color: var(--lc-text-muted);
  font-size: 0.82rem;
  line-height: 1.75;
}

.bullets code {
  font-size: 0.78em;
  color: var(--lc-accent);
}

.panel {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 0 12px 12px 0;
  padding: 36px 32px;
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
  max-width: 400px;
}

.mock-banner {
  margin: 0 0 14px;
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
  font-size: 1.4rem;
  font-weight: 700;
}

.subtitle {
  margin: 0 0 20px;
  font-size: 0.86rem;
  color: var(--lc-text-muted);
}

.form {
  display: flex;
  flex-direction: column;
}

.field {
  margin-bottom: 12px;
}

.field label {
  display: block;
  margin-bottom: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.req {
  color: var(--lc-hard);
}

.field input {
  width: 100%;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 0.92rem;
}

.err {
  margin: 4px 0 0;
  color: var(--lc-red);
  font-size: 0.85rem;
}

.ok {
  margin: 4px 0 0;
  color: var(--lc-green);
  font-size: 0.85rem;
}

.submit {
  margin-top: 14px;
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

.switch {
  margin: 20px 0 0;
  text-align: center;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

.link {
  color: var(--lc-accent);
  font-weight: 600;
}
</style>
