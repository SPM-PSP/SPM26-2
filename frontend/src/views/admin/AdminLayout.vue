<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
import PageBack from '@/components/PageBack.vue'

const keepAliveNames = ['AdminUsersView', 'AdminCategoriesView', 'AdminProblemsView']
</script>

<template>
  <div class="admin-shell">
    <header class="admin-top">
      <PageBack label="返回前台" to="/problems" />
      <span class="admin-badge">管理后台</span>
    </header>
    <div class="admin">
      <aside class="side">
        <h2 class="side-title">功能菜单</h2>
        <nav class="nav">
          <RouterLink class="item" :to="{ name: 'admin-users' }">用户管理</RouterLink>
          <RouterLink class="item" :to="{ name: 'admin-categories' }">题目分类</RouterLink>
          <RouterLink class="item" :to="{ name: 'admin-problems' }">题库管理</RouterLink>
        </nav>
      </aside>
      <div class="content">
        <RouterView v-slot="{ Component }">
          <KeepAlive :include="keepAliveNames">
            <component :is="Component" v-if="Component" />
          </KeepAlive>
        </RouterView>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-shell {
  min-height: 100vh;
  background: var(--lc-bg);
  padding: 16px 20px 32px;
  max-width: 1200px;
  margin: 0 auto;
}

.admin-top {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.admin-badge {
  font-size: 0.85rem;
  font-weight: 700;
  color: var(--lc-accent);
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(255, 161, 22, 0.12);
  border: 1px solid rgba(255, 161, 22, 0.35);
}

.admin {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
  align-items: start;
}

@media (max-width: 720px) {
  .admin {
    grid-template-columns: 1fr;
  }
}

.side {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 16px;
  position: sticky;
  top: 16px;
}

.side-title {
  margin: 0 0 12px;
  font-size: 0.95rem;
  font-weight: 700;
}

.nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item {
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 0.88rem;
  color: var(--lc-text-muted);
}

.item:hover {
  color: var(--lc-text);
  background: var(--lc-surface-2);
}

.item.router-link-active {
  color: var(--lc-accent);
  background: rgba(255, 161, 22, 0.08);
  font-weight: 600;
}

.content {
  min-width: 0;
}
</style>
