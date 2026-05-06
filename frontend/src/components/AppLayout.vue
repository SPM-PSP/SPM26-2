<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

function logout() {
  auth.clear()
}
</script>

<template>
  <div class="layout">
    <header class="nav">
      <div class="nav-inner">
        <RouterLink to="/problems" class="brand">
          <span class="brand-mark" />
          <span class="brand-text">OJ<span class="accent">Code</span></span>
        </RouterLink>
        <nav class="links">
          <RouterLink to="/problems" class="nav-link">题库</RouterLink>
          <RouterLink v-if="auth.isLoggedIn" to="/submissions" class="nav-link">提交记录</RouterLink>
          <RouterLink to="/ai" class="nav-link">AI 实验室</RouterLink>
        </nav>
        <div class="nav-right">
          <template v-if="auth.isLoggedIn">
            <RouterLink to="/profile" class="user-chip">
              <img v-if="auth.userPreview?.avatar" :src="auth.userPreview.avatar" alt="" class="avatar" />
              <span v-else class="avatar placeholder">{{ (auth.userPreview?.nickname || '?').slice(0, 1) }}</span>
              <span class="nick">{{ auth.userPreview?.nickname || '用户' }}</span>
            </RouterLink>
            <button type="button" class="btn-ghost" @click="logout">退出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="btn-ghost">登录</RouterLink>
            <RouterLink to="/register" class="btn-primary sm">注册</RouterLink>
          </template>
        </div>
      </div>
    </header>
    <main class="main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}

.nav {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(22, 22, 22, 0.92);
  border-bottom: 1px solid var(--lc-border);
  backdrop-filter: blur(8px);
}

.nav-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  height: 52px;
  display: flex;
  align-items: center;
  gap: 28px;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 700;
  font-size: 1.1rem;
  letter-spacing: -0.02em;
}

.brand-mark {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b35);
}

.brand-text {
  color: var(--lc-text);
}

.accent {
  color: var(--lc-accent);
}

.links {
  display: flex;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.nav-link {
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
}

.nav-link.router-link-active {
  color: var(--lc-text);
  background: var(--lc-surface-2);
}

.nav-link:hover {
  color: var(--lc-accent);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 10px 4px 4px;
  border-radius: 999px;
  background: var(--lc-surface-2);
  border: 1px solid var(--lc-border);
  font-size: 0.85rem;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar.placeholder {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: var(--lc-border);
  color: var(--lc-text-muted);
  font-size: 0.75rem;
}

.nick {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-ghost {
  background: transparent;
  border: none;
  color: var(--lc-text-muted);
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.9rem;
}

.btn-ghost:hover {
  color: var(--lc-text);
  background: var(--lc-surface-2);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 18px;
  border-radius: 8px;
  background: var(--lc-accent);
  color: #1a1a1a;
  font-weight: 600;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
}

.btn-primary.sm {
  padding: 6px 14px;
  font-size: 0.85rem;
}

.btn-primary:hover {
  filter: brightness(1.06);
}

.main {
  flex: 1;
  max-width: 1280px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}
</style>
