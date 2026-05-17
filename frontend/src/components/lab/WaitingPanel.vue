<script setup lang="ts">
import { onUnmounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    active: boolean
    mode?: 'generate' | 'analyze'
  }>(),
  { mode: 'generate' },
)

const TIPS = [
  '二分查找前务必确认数组已排序，否则结果不可信。',
  '动态规划：先定义状态 dp[i] 的含义，再写转移方程。',
  '图论 BFS 适合求最短步数；DFS 更适合路径枚举与连通性。',
  '贪心要证明「局部最优 ⇒ 全局最优」，不能凭直觉硬套。',
  '字符串哈希 + 滚动窗口可高效处理子串匹配类问题。',
  '写递归时先想边界条件，再写递归体，避免栈溢出。',
  '复杂度估算：嵌套循环看层数，分治看 master 定理或递归树。',
  '提交前用边界样例自测：空输入、单元素、最大值、重复元素。',
  '链表题画图：标出 prev / cur / next，再动手改指针。',
  '优先队列 + 懒删除是处理「动态最值」的常用技巧。',
]

const tipIndex = ref(0)
const tab = ref<'tips' | 'game'>('tips')
const score = ref(0)
const bugs = ref<{ id: number; x: number; y: number }[]>([])

let tipTimer: ReturnType<typeof setInterval> | null = null
let bugSpawnTimer: ReturnType<typeof setInterval> | null = null
let bugId = 0

const statusTitle = () =>
  props.mode === 'analyze' ? 'AI 正在审阅你的代码…' : 'AI 正在为你构思题目…'

function stopTimers() {
  if (tipTimer) {
    clearInterval(tipTimer)
    tipTimer = null
  }
  if (bugSpawnTimer) {
    clearInterval(bugSpawnTimer)
    bugSpawnTimer = null
  }
}

function startTimers() {
  stopTimers()
  tipIndex.value = 0
  score.value = 0
  bugs.value = []
  bugId = 0
  tipTimer = setInterval(() => {
    tipIndex.value = (tipIndex.value + 1) % TIPS.length
  }, 4200)
  bugSpawnTimer = setInterval(spawnBug, 750)
}

function spawnBug() {
  const id = bugId++
  const pad = 8
  const x = pad + Math.random() * (84 - pad)
  const y = pad + Math.random() * (72 - pad)
  bugs.value = [...bugs.value, { id, x, y }]
  window.setTimeout(() => {
    bugs.value = bugs.value.filter((b) => b.id !== id)
  }, 950)
}

function squish(id: number) {
  const hit = bugs.value.some((b) => b.id === id)
  if (!hit) return
  bugs.value = bugs.value.filter((b) => b.id !== id)
  score.value += 1
}

watch(
  () => props.active,
  (on) => {
    if (on) startTimers()
    else stopTimers()
  },
  { immediate: true },
)

onUnmounted(stopTimers)
</script>

<template>
  <div v-if="active" class="wait-panel" role="status" aria-live="polite">
    <div class="wait-glow" aria-hidden="true" />
    <div class="wait-head">
      <span class="pulse-dot" aria-hidden="true" />
      <p class="wait-title">{{ statusTitle() }}</p>
      <p class="wait-sub">等待期间可以浏览贴士，或玩一局「捉虫」放松片刻</p>
    </div>

    <div class="wait-tabs">
      <button type="button" class="tab" :class="{ active: tab === 'tips' }" @click="tab = 'tips'">
        <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
          <path
            fill="currentColor"
            d="M12 2a7 7 0 0 0-4 12.9V17a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1v-2.1A7 7 0 0 0 12 2zm0 18a2 2 0 1 0 0-4 2 2 0 0 0 0 4z"
          />
        </svg>
        编程贴士
      </button>
      <button type="button" class="tab" :class="{ active: tab === 'game' }" @click="tab = 'game'">
        <svg viewBox="0 0 24 24" width="16" height="16" aria-hidden="true">
          <path
            fill="currentColor"
            d="M21 6H3a2 2 0 0 0-2 2v8a2 2 0 0 0 2 2h18a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2zm-9 8.5a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zm6 0a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3zM6 12.5a1.5 1.5 0 1 1 0-3 1.5 1.5 0 0 1 0 3z"
          />
        </svg>
        捉虫小游戏
      </button>
    </div>

    <div v-show="tab === 'tips'" class="tips-box">
      <div class="tip-icon" aria-hidden="true">💡</div>
      <p class="tip-text" :key="tipIndex">{{ TIPS[tipIndex] }}</p>
      <div class="tip-dots">
        <span
          v-for="(_, i) in TIPS"
          :key="i"
          class="dot"
          :class="{ on: i === tipIndex }"
        />
      </div>
    </div>

    <div v-show="tab === 'game'" class="game-box">
      <p class="game-score">得分 <strong>{{ score }}</strong> · 点击出现的 bug 消灭它</p>
      <div class="game-field">
        <button
          v-for="b in bugs"
          :key="b.id"
          type="button"
          class="bug"
          :style="{ left: `${b.x}%`, top: `${b.y}%` }"
          aria-label="消灭 bug"
          @click="squish(b.id)"
        >
          🐛
        </button>
        <p v-if="!bugs.length" class="game-hint">bug 马上出现…</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.wait-panel {
  position: relative;
  margin-top: 20px;
  padding: 22px 24px;
  border-radius: var(--radius-md);
  border: 1px solid rgba(91, 124, 250, 0.35);
  background: linear-gradient(145deg, rgba(20, 24, 36, 0.95), rgba(28, 34, 52, 0.9));
  overflow: hidden;
}

.wait-glow {
  position: absolute;
  inset: -40% -20% auto;
  height: 120px;
  background: radial-gradient(circle, rgba(91, 124, 250, 0.2), transparent 70%);
  pointer-events: none;
}

.wait-head {
  position: relative;
  margin-bottom: 16px;
}

.pulse-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #5b7cfa;
  box-shadow: 0 0 12px #5b7cfa;
  animation: pulse 1.2s ease-in-out infinite;
  vertical-align: middle;
  margin-right: 8px;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.85);
  }
}

.wait-title {
  display: inline;
  margin: 0;
  font-weight: 700;
  font-size: 1rem;
  vertical-align: middle;
}

.wait-sub {
  margin: 8px 0 0;
  font-size: 0.82rem;
  color: var(--lc-text-muted);
}

.wait-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}

.tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  font-size: 0.82rem;
  cursor: pointer;
  transition: border-color 0.15s, color 0.15s, background 0.15s;
}

.tab.active {
  border-color: rgba(91, 124, 250, 0.6);
  background: rgba(91, 124, 250, 0.12);
  color: #a8b8ff;
}

.tips-box {
  position: relative;
  padding: 16px 18px 14px 52px;
  background: rgba(0, 0, 0, 0.25);
  border-radius: var(--radius-sm);
  min-height: 88px;
}

.tip-icon {
  position: absolute;
  left: 16px;
  top: 16px;
  font-size: 1.4rem;
}

.tip-text {
  margin: 0;
  font-size: 0.92rem;
  line-height: 1.65;
  color: var(--lc-text);
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.tip-dots {
  display: flex;
  gap: 4px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--lc-border);
}

.dot.on {
  background: #5b7cfa;
  width: 14px;
  border-radius: 3px;
}

.game-box {
  margin-top: 0;
}

.game-score {
  margin: 0 0 10px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

.game-score strong {
  color: #7ee8a2;
  font-size: 1.1rem;
}

.game-field {
  position: relative;
  height: 140px;
  border-radius: var(--radius-sm);
  border: 1px dashed rgba(126, 232, 162, 0.35);
  background: repeating-linear-gradient(
    0deg,
    transparent,
    transparent 18px,
    rgba(255, 255, 255, 0.02) 18px,
    rgba(255, 255, 255, 0.02) 19px
  );
  overflow: hidden;
}

.game-hint {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

.bug {
  position: absolute;
  padding: 4px 8px;
  border: none;
  background: transparent;
  font-size: 1.5rem;
  cursor: pointer;
  transform: translate(-50%, -50%);
  animation: pop 0.2s ease;
  filter: drop-shadow(0 2px 6px rgba(0, 0, 0, 0.4));
}

.bug:hover {
  transform: translate(-50%, -50%) scale(1.15);
}

@keyframes pop {
  from {
    transform: translate(-50%, -50%) scale(0.3);
  }
  to {
    transform: translate(-50%, -50%) scale(1);
  }
}
</style>
