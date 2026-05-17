<script setup lang="ts">
import { RouterView } from 'vue-router'

const highlights = [
  { icon: '📚', title: '精选题库', desc: '按难度与算法标签分类，循序渐进' },
  { icon: '🤖', title: 'AI 实验室', desc: '智能出题与代码分析，辅助学习' },
  { icon: '📊', title: '提交追踪', desc: '完整评测记录，复盘每一次尝试' },
]
</script>

<template>
  <div class="auth-root">
    <div class="auth-bg" aria-hidden="true">
      <div class="grid" />
      <div class="orb orb-a" />
      <div class="orb orb-b" />
    </div>

    <aside class="showcase" aria-hidden="true">
      <p class="showcase-tag">Program Platform</p>
      <h2 class="showcase-title">
        用代码<br />
        <span>征服每一道题</span>
      </h2>
      <ul class="showcase-list">
        <li v-for="item in highlights" :key="item.title">
          <span class="hi-icon">{{ item.icon }}</span>
          <div>
            <strong>{{ item.title }}</strong>
            <span>{{ item.desc }}</span>
          </div>
        </li>
      </ul>
    </aside>

    <main class="auth-main">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.auth-root {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr;
  position: relative;
  overflow: hidden;
}

@media (min-width: 900px) {
  .auth-root {
    grid-template-columns: 1fr 1fr;
  }
}

.auth-bg {
  position: absolute;
  inset: 0;
  background: var(--lc-bg);
  pointer-events: none;
}

.grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 48px 48px;
  mask-image: radial-gradient(ellipse 80% 70% at 50% 40%, black, transparent);
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
}

.orb-a {
  width: 420px;
  height: 420px;
  top: -120px;
  left: -80px;
  background: rgba(91, 124, 250, 0.18);
}

.orb-b {
  width: 360px;
  height: 360px;
  bottom: -100px;
  right: 10%;
  background: rgba(255, 161, 22, 0.12);
}

.showcase {
  display: none;
  flex-direction: column;
  justify-content: center;
  padding: 48px 56px;
  position: relative;
  z-index: 1;
}

@media (min-width: 900px) {
  .showcase {
    display: flex;
  }
}

.showcase-tag {
  margin: 0 0 16px;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: #8aa4ff;
}

.showcase-title {
  margin: 0 0 36px;
  font-size: 2.4rem;
  font-weight: 800;
  line-height: 1.2;
  letter-spacing: -0.03em;
}

.showcase-title span {
  background: linear-gradient(135deg, var(--lc-accent), #ff8c42);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

.showcase-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.showcase-list li {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  padding: 16px 18px;
  border-radius: 12px;
  background: rgba(20, 24, 32, 0.6);
  border: 1px solid var(--lc-border);
}

.hi-icon {
  font-size: 1.5rem;
  line-height: 1;
}

.showcase-list strong {
  display: block;
  font-size: 0.95rem;
  margin-bottom: 4px;
}

.showcase-list span {
  font-size: 0.82rem;
  color: var(--lc-text-muted);
}

.auth-main {
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 1;
  min-height: 100vh;
}
</style>
