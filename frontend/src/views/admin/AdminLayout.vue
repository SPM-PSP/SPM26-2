<script setup lang="ts">
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const navItems = [
  { name: 'admin-users', label: '用户管理', path: '/admin/users' },
  { name: 'admin-categories', label: '分类管理', path: '/admin/categories' },
  { name: 'admin-problems', label: '题库管理', path: '/admin/problems' },
  { name: 'admin-problem-new', label: '添加题目', path: '/admin/problems/new' },
]

function logout() {
  auth.clear()
  void router.push({ name: 'login' })
}
</script>

<script lang="ts">
export default { name: 'AdminLayout' }
</script>

<template>
  <div class="admin-shell">
    <aside class="sidebar">
      <RouterLink to="/problems" class="back-site">
        <span class="arrow">←</span>
        返回前台
      </RouterLink>
      <div class="side-brand">
        <span class="mark" />
        <span>管理后台</span>
      </div>
      <nav class="side-nav">
        <RouterLink
          v-for="item in navItems"
          :key="item.name"
          :to="item.path"
          class="side-link"
          :class="{ active: route.name === item.name || (item.name === 'admin-problems' && String(route.name || '').startsWith('admin-problem')) }"
        >
          {{ item.label }}
        </RouterLink>
      </nav>
      <div class="side-foot">
        <span class="who">{{ auth.userPreview?.nickname || '管理员' }}</span>
        <button type="button" class="btn-out" @click="logout">退出登录</button>
      </div>
    </aside>
    <div class="admin-main">
      <header class="admin-top">
        <h1 class="top-title">OJCode 管理</h1>
        <RouterLink to="/problems" class="top-link">前往题库</RouterLink>
      </header>
      <div class="admin-content">
        <RouterView />
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: flex;
  background: var(--lc-bg);
}

.sidebar {
  width: 240px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 20px 16px;
  background: var(--lc-surface);
  border-right: 1px solid var(--lc-border);
}

.back-site {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  margin-bottom: 20px;
  border-radius: 8px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
  border: 1px solid var(--lc-border);
  transition: color 0.15s, border-color 0.15s;
}

.back-site:hover {
  color: var(--lc-accent);
  border-color: var(--lc-accent-dim);
}

.arrow {
  font-size: 1rem;
}

.side-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 1.05rem;
  margin-bottom: 24px;
  padding: 0 8px;
}

.mark {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
}

.side-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.side-link {
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
  transition: background 0.15s, color 0.15s;
}

.side-link:hover {
  color: var(--lc-text);
  background: var(--lc-surface-2);
}

.side-link.active {
  color: var(--lc-accent);
  background: rgba(255, 161, 22, 0.1);
  font-weight: 600;
}

.side-foot {
  padding-top: 16px;
  border-top: 1px solid var(--lc-border);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.who {
  font-size: 0.85rem;
  color: var(--lc-text-muted);
  padding: 0 8px;
}

.btn-out {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: transparent;
  color: var(--lc-text-muted);
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-out:hover {
  color: var(--lc-red);
  border-color: var(--lc-red);
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.admin-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  border-bottom: 1px solid var(--lc-border);
  background: rgba(12, 14, 20, 0.6);
}

.top-title {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.top-link {
  font-size: 0.85rem;
  color: var(--lc-accent);
}

.admin-content {
  flex: 1;
  padding: 24px 28px;
  overflow: auto;
}
</style>
