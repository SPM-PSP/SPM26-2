<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="admin">
    <aside class="side">
      <h2 class="side-title">管理后台</h2>
      <nav class="nav">
        <RouterLink class="item" to="/admin/users" active-class="on">用户</RouterLink>
        <RouterLink class="item" to="/admin/categories" active-class="on">题目分类</RouterLink>
        <RouterLink class="item" to="/admin/problems" active-class="on">题库</RouterLink>
      </nav>
      <RouterLink class="back" to="/problems">返回前台</RouterLink>
    </aside>
    <div class="content">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.admin {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
  align-items: start;
  max-width: 1200px;
  margin: 0 auto;
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

.item.on {
  color: var(--lc-accent);
  background: rgba(255, 161, 22, 0.08);
}

.back {
  display: block;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--lc-border);
  font-size: 0.82rem;
  color: var(--lc-text-muted);
}

.back:hover {
  color: var(--lc-accent);
}

.content {
  min-width: 0;
}
</style>
