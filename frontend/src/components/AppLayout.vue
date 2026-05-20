<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProblemEditStore } from '@/stores/problemEdit'
import { adminProblemList } from '@/api/admin'

const auth = useAuthStore()
const router = useRouter()
const problemEditStore = useProblemEditStore()

const cachedViews = ['ProblemListView', 'SubmissionsView', 'AiLabView', 'ProblemDetailView']

function logout() {
  auth.clear()
  void router.push({ name: 'login' })
}

// 点击播放按钮：进入上次浏览的题目或第一道题
async function handlePlayClick() {
  // 如果有上次浏览的记录，直接进入
  if (problemEditStore.lastProblemId) {
    void router.push({ name: 'problem-detail', params: { id: String(problemEditStore.lastProblemId) } })
    return
  }
  
  // 否则获取题库第一道题
  try {
    const res = await adminProblemList({ page: 1, size: 1 })
    if (res.code === 200 && res.data?.list && res.data.list.length > 0) {
      const firstProblemId = res.data.list[0].problemId
      // 记住这个 ID
      problemEditStore.lastProblemId = firstProblemId
      void router.push({ name: 'problem-detail', params: { id: String(firstProblemId) } })
    } else {
      // 题库为空，进入题库列表页
      void router.push({ name: 'problems' })
    }
  } catch (error) {
    console.error('获取题目列表失败:', error)
    void router.push({ name: 'problems' })
  }
}
</script>

<script lang="ts">
export default { name: 'AppLayout' }
</script>

<template>
  <div class="layout">
    <header class="nav">
      <div class="nav-inner">
        <RouterLink to="/problems" class="brand">
          <span class="brand-mark" aria-hidden="true" />
          <span class="brand-text">OJ<span class="accent">Code</span></span>
        </RouterLink>
        <nav class="links">
          <RouterLink to="/problems" class="nav-link">题库</RouterLink>
          <RouterLink v-if="auth.isLoggedIn" to="/submissions" class="nav-link">提交记录</RouterLink>
          <RouterLink to="/ai" class="nav-link">AI 实验室</RouterLink>
        </nav>
        
        <!-- 中间的正方形播放按钮 -->
        <button class="nav-play-btn" @click="handlePlayClick" title="浏览编辑题目">
          <span class="play-icon">▶</span>
        </button>
        
        <nav class="links links-right">
          <RouterLink v-if="auth.isAdmin" to="/admin" class="nav-link">管理后台</RouterLink>
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
      <RouterView v-slot="{ Component }">
        <KeepAlive :include="cachedViews">
          <component :is="Component" />
        </KeepAlive>
      </RouterView>
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
  background: rgba(12, 14, 20, 0.88);
  border-bottom: 1px solid var(--lc-border);
  backdrop-filter: blur(12px);
}

.nav-inner {
  max-width: 1320px;
  margin: 0 auto;
  padding: 0 24px;
  height: 56px;
  display: flex;
  align-items: center;
  gap: 24px;
  position: relative;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  font-size: 1.12rem;
  letter-spacing: -0.02em;
}

.brand-mark {
  width: 24px;
  height: 24px;
  border-radius: 7px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  box-shadow: 0 0 20px rgba(255, 161, 22, 0.35);
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
  gap: 6px;
  flex: 1;
}

.links-right {
  flex: 0 0 auto;
}

/* 正方形播放按钮样式 */
.nav-play-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  border: none;
  color: #fff;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 2px 8px rgba(255, 161, 22, 0.3);
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  overflow: hidden;
}

.nav-play-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #ff6b4a, var(--lc-accent));
  opacity: 0;
  transition: opacity 0.25s ease;
}

.nav-play-btn:hover {
  transform: translate(-50%, -50%) scale(1.08);
  box-shadow: 0 4px 12px rgba(255, 161, 22, 0.45);
}

.nav-play-btn:hover::before {
  opacity: 1;
}

.nav-play-btn:active {
  transform: translate(-50%, -50%) scale(0.95);
}

.play-icon {
  font-size: 0.85rem;
  font-weight: bold;
  position: relative;
  z-index: 1;
  margin-left: 2px;
  line-height: 1;
}

.nav-link {
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
  transition: color 0.15s, background 0.15s;
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
  padding: 4px 12px 4px 4px;
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
  border-radius: 8px;
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
  background: linear-gradient(135deg, var(--lc-accent), #ff8c42);
  color: #0c0e14;
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
  max-width: 1320px;
  width: 100%;
  margin: 0 auto;
  padding: 24px;
}
</style>
