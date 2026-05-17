<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { analyzeCode, generateProblem } from '@/api/algorithm'
import { adminAddProblem, adminAddTestCase } from '@/api/admin'
import { fetchProblemList } from '@/api/problem'
import PageBack from '@/components/PageBack.vue'
import WaitingPanel from '@/components/lab/WaitingPanel.vue'
import { useAiLabStore } from '@/stores/aiLab'
import { useAuthStore } from '@/stores/auth'
import type { ProblemGenerateResponse } from '@/types/api'
import { difficultyToApi } from '@/utils/difficulty'

const router = useRouter()
const lab = useAiLabStore()
const auth = useAuthStore()

const plates = ['数组', '链表', '动态规划', '贪心', '字符串', '图论', '数学']
const plateIcons: Record<string, string> = {
  数组: '▦',
  链表: '⛓',
  动态规划: '◇',
  贪心: '★',
  字符串: 'Aa',
  图论: '◎',
  数学: '∑',
}
const diffs = ['简单', '中等', '困难']
const langs = ['C++', 'Java', 'Python']
const langIcons: Record<string, string> = { 'C++': 'C+', Java: 'Jv', Python: 'Py' }

const plate = ref(lab.plate || '数组')
const difficulty = ref(lab.difficulty || '中等')
const targetLanguage = ref(lab.targetLanguage || 'C++')
const genLoading = ref(false)
const genErr = ref('')
const generatedProblem = ref<ProblemGenerateResponse | null>(lab.generatedProblem)

const code = ref(lab.analyzeCode)
const lang = ref(lab.analyzeLang)
const anaLoading = ref(false)
const anaErr = ref('')
const anaOut = ref<{ plate?: string; complexity?: string; style?: string } | null>(null)

const saving = ref(false)
const saveErr = ref('')

const categoryMap: Record<string, string[]> = {
  数组: ['数组'],
  链表: ['链表'],
  动态规划: ['动态规划'],
  贪心: ['贪心'],
  字符串: ['字符串'],
  图论: ['图论'],
  数学: ['数学'],
}

async function onGenerate() {
  genLoading.value = true
  genErr.value = ''
  generatedProblem.value = null
  lab.generatedProblem = null
  try {
    const data = await generateProblem({
      plate: plate.value,
      difficulty: difficulty.value,
      targetLanguage: targetLanguage.value,
    })
    generatedProblem.value = data
    lab.generatedProblem = data
    lab.plate = plate.value
    lab.difficulty = difficulty.value
    lab.targetLanguage = targetLanguage.value
  } catch (e: unknown) {
    genErr.value = e instanceof Error ? e.message : '生成失败'
  } finally {
    genLoading.value = false
  }
}

async function resolveProblemId(title: string): Promise<number | null> {
  const res = await fetchProblemList({ page: 1, size: 10, keyword: title })
  if (res.code !== 200 || !res.data?.list?.length) return null
  const exact = res.data.list.find((p) => p.title === title)
  return exact?.problemId ?? res.data.list[0]?.problemId ?? null
}

async function onSaveAndStart() {
  if (!generatedProblem.value) return
  if (!auth.isAdmin) {
    saveErr.value = '请使用管理员账号登录后再入库'
    return
  }

  saving.value = true
  saveErr.value = ''

  try {
    const title = generatedProblem.value.problemName
    const addRes = await adminAddProblem({
      title,
      difficulty: difficultyToApi(difficulty.value),
      categoryNames: categoryMap[plate.value] || [plate.value],
      description: generatedProblem.value.problemDesc,
      inputFormat: generatedProblem.value.inputFormat || '',
      outputFormat: generatedProblem.value.outputFormat || '',
      sampleInput: generatedProblem.value.sampleInput?.[0] || '',
      sampleOutput: generatedProblem.value.sampleOutput?.[0] || '',
      timeLimit: 1000,
      memoryLimit: 256000,
    })

    if (addRes.code !== 200) {
      saveErr.value = addRes.message || '保存失败'
      return
    }

    const problemId = await resolveProblemId(title)
    if (!problemId) {
      saveErr.value = '题目已入库，请从题库列表进入'
      await router.push({ name: 'problems' })
      return
    }

    const samples = generatedProblem.value.sampleInput
    const outputs = generatedProblem.value.sampleOutput
    if (samples?.length && outputs?.length) {
      const len = Math.min(samples.length, outputs.length)
      for (let i = 0; i < len; i++) {
        const inputContent = samples[i]
        const outputContent = outputs[i]
        if (!inputContent || !outputContent) continue
        const inputFile = new File([inputContent], `sample_${i}.in`, { type: 'text/plain' })
        const outputFile = new File([outputContent], `sample_${i}.out`, { type: 'text/plain' })
        await adminAddTestCase(problemId, inputFile, outputFile)
      }
    }

    await router.push({ name: 'problem-detail', params: { id: String(problemId) } })
  } catch (e: unknown) {
    saveErr.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
  }
}

async function onAnalyze() {
  anaLoading.value = true
  anaErr.value = ''
  anaOut.value = null
  lab.analyzeCode = code.value
  lab.analyzeLang = lang.value
  try {
    const data = await analyzeCode(code.value, lang.value)
    anaOut.value = {
      plate: data.plateCategory,
      complexity: data.complexityAnalysis,
      style: data.codeStyleAnalysis,
    }
  } catch (e: unknown) {
    anaErr.value = e instanceof Error ? e.message : '分析失败'
  } finally {
    anaLoading.value = false
  }
}
</script>

<script lang="ts">
export default { name: 'AiLabView' }
</script>

<template>
  <div class="lab-page">
    <PageBack to="/problems" label="返回题库" />

    <header class="hero">
      <div class="hero-text">
        <p class="hero-kicker">
          <span class="spark" aria-hidden="true">✦</span>
          AI Powered
        </p>
        <h1 class="hero-title">AI 实验室</h1>
        <p class="hero-desc">智能出题 · 代码审阅 · 一键入库，让练习更高效</p>
      </div>
      <div class="hero-badges" aria-hidden="true">
        <span class="hero-badge">🧠 智能出题</span>
        <span class="hero-badge">🔍 深度分析</span>
        <span class="hero-badge">⚡ 快速上手</span>
      </div>
    </header>

    <section class="panel panel-gen">
      <div class="panel-head">
        <span class="panel-icon gen-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path
              fill="currentColor"
              d="M19 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2zm-7 14l-5-5 1.41-1.41L12 14.17l7.59-7.59L21 8l-9 9z"
            />
          </svg>
        </span>
        <div>
          <h2>生成练习题</h2>
          <p class="panel-sub">选择算法板块与难度，AI 将为你定制一道编程题</p>
        </div>
      </div>

      <div class="chip-section">
        <p class="chip-label">算法板块</p>
        <div class="chips">
          <button
            v-for="p in plates"
            :key="p"
            type="button"
            class="chip"
            :class="{ active: plate === p }"
            :disabled="genLoading"
            @click="plate = p"
          >
            <span class="chip-ico">{{ plateIcons[p] }}</span>
            {{ p }}
          </button>
        </div>
      </div>

      <div class="row-2">
        <div class="chip-section">
          <p class="chip-label">难度</p>
          <div class="chips chips-sm">
            <button
              v-for="d in diffs"
              :key="d"
              type="button"
              class="chip chip-diff"
              :class="[`diff-${d}`, { active: difficulty === d }]"
              :disabled="genLoading"
              @click="difficulty = d"
            >
              {{ d }}
            </button>
          </div>
        </div>
        <div class="chip-section">
          <p class="chip-label">目标语言</p>
          <div class="chips chips-sm">
            <button
              v-for="l in langs"
              :key="l"
              type="button"
              class="chip"
              :class="{ active: targetLanguage === l }"
              :disabled="genLoading"
              @click="targetLanguage = l"
            >
              <span class="chip-ico mono">{{ langIcons[l] }}</span>
              {{ l }}
            </button>
          </div>
        </div>
      </div>

      <button type="button" class="btn-action btn-gen" :disabled="genLoading" @click="onGenerate">
        <svg v-if="!genLoading" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
          <path fill="currentColor" d="M19 8l-4 4h3c0 3.31-2.69 6-6 6-1.01 0-1.97-.25-2.8-.7l-1.46 1.46A7.93 7.93 0 0 0 12 20c4.42 0 8-3.58 8-8h3l-4-4zM6 12c0-3.31 2.69-6 6-6 1.01 0 1.97.25 2.8.7l1.46-1.46A7.93 7.93 0 0 0 12 4c-4.42 0-8 3.58-8 8H1l4 4 4-4H6z" />
        </svg>
        <span v-else class="btn-spinner" aria-hidden="true" />
        {{ genLoading ? '正在生成题目…' : '生成题目' }}
      </button>

      <p v-if="genErr" class="msg-err">{{ genErr }}</p>

      <WaitingPanel :active="genLoading" mode="generate" />

      <div v-if="generatedProblem && !genLoading" class="problem-preview">
        <div class="problem-header">
          <h3 class="problem-title">{{ generatedProblem.problemName }}</h3>
          <div class="problem-tags">
            <span class="tag tag-plate">{{ plateIcons[plate] }} {{ plate }}</span>
            <span class="tag" :class="`tag-${difficulty}`">{{ difficulty }}</span>
            <span class="tag tag-lang">{{ targetLanguage }}</span>
          </div>
        </div>

        <div class="problem-content">
          <div class="content-section">
            <h4><span class="sec-ico">📋</span> 题目描述</h4>
            <p class="pre content-text">{{ generatedProblem.problemDesc }}</p>
          </div>
          <div v-if="generatedProblem.inputFormat" class="content-section">
            <h4><span class="sec-ico">📥</span> 输入格式</h4>
            <p class="pre content-text">{{ generatedProblem.inputFormat }}</p>
          </div>
          <div v-if="generatedProblem.outputFormat" class="content-section">
            <h4><span class="sec-ico">📤</span> 输出格式</h4>
            <p class="pre content-text">{{ generatedProblem.outputFormat }}</p>
          </div>
          <div v-if="generatedProblem.sampleInput?.length" class="content-section">
            <h4><span class="sec-ico">🧪</span> 样例</h4>
            <div class="sample-box">
              <div class="sample-item">
                <div class="sample-label">输入</div>
                <pre class="sample-code">{{ generatedProblem.sampleInput[0] }}</pre>
              </div>
              <div class="sample-item">
                <div class="sample-label">输出</div>
                <pre class="sample-code">{{ generatedProblem.sampleOutput?.[0] }}</pre>
              </div>
            </div>
          </div>
        </div>

        <div class="problem-actions">
          <p v-if="saveErr" class="msg-err">{{ saveErr }}</p>
          <button type="button" class="btn-action btn-save" :disabled="saving" @click="onSaveAndStart">
            <svg v-if="!saving" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path fill="currentColor" d="M17 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z" />
            </svg>
            <span v-else class="btn-spinner dark" aria-hidden="true" />
            {{ saving ? '保存中…' : '入库并开始做题' }}
          </button>
          <p v-if="!auth.isAdmin" class="admin-hint">入库功能需管理员账号</p>
        </div>
      </div>
    </section>

    <section class="panel panel-ana">
      <div class="panel-head">
        <span class="panel-icon ana-icon" aria-hidden="true">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path
              fill="currentColor"
              d="M9.4 16.6 4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0 4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z"
            />
          </svg>
        </span>
        <div>
          <h2>代码分析</h2>
          <p class="panel-sub">粘贴你的代码，获取算法归类、复杂度与风格建议</p>
        </div>
      </div>

      <div class="lang-row">
        <p class="chip-label">语言</p>
        <div class="chips chips-sm">
          <button
            v-for="l in langs"
            :key="l"
            type="button"
            class="chip"
            :class="{ active: lang === l }"
            :disabled="anaLoading"
            @click="lang = l"
          >
            <span class="chip-ico mono">{{ langIcons[l] }}</span>
            {{ l }}
          </button>
        </div>
        <button type="button" class="btn-action btn-ana" :disabled="anaLoading" @click="onAnalyze">
          <svg v-if="!anaLoading" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
            <path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z" />
          </svg>
          <span v-else class="btn-spinner" aria-hidden="true" />
          {{ anaLoading ? '分析中…' : '开始分析' }}
        </button>
      </div>

      <div class="editor-wrap">
        <div class="editor-bar">
          <span class="dot red" /><span class="dot yellow" /><span class="dot green" />
          <span class="editor-fname">solution.{{ lang === 'Python' ? 'py' : lang === 'Java' ? 'java' : 'cpp' }}</span>
        </div>
        <textarea v-model="code" class="editor" spellcheck="false" placeholder="// 在此粘贴你的代码…" />
      </div>

      <p v-if="anaErr" class="msg-err">{{ anaErr }}</p>

      <WaitingPanel :active="anaLoading" mode="analyze" />

      <div v-if="anaOut && !anaLoading" class="ana-grid">
        <div class="ana-card">
          <div class="ana-card-head">
            <span class="ana-ico">🧩</span>
            <h3>算法板块</h3>
          </div>
          <p>{{ anaOut.plate }}</p>
        </div>
        <div class="ana-card">
          <div class="ana-card-head">
            <span class="ana-ico">⏱</span>
            <h3>复杂度分析</h3>
          </div>
          <p class="pre">{{ anaOut.complexity }}</p>
        </div>
        <div class="ana-card ana-wide">
          <div class="ana-card-head">
            <span class="ana-ico">✨</span>
            <h3>代码风格</h3>
          </div>
          <p class="pre">{{ anaOut.style }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.lab-page {
  max-width: 1080px;
}

.hero {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 28px;
  padding: 28px 32px;
  border-radius: 16px;
  border: 1px solid rgba(91, 124, 250, 0.25);
  background:
    linear-gradient(135deg, rgba(91, 124, 250, 0.1), transparent 50%),
    linear-gradient(225deg, rgba(255, 161, 22, 0.06), transparent 40%),
    var(--lc-surface);
  box-shadow: var(--shadow-card);
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #8aa4ff;
}

.spark {
  color: var(--lc-accent);
}

.hero-title {
  margin: 0 0 8px;
  font-size: 2rem;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.hero-desc {
  margin: 0;
  color: var(--lc-text-muted);
  font-size: 0.95rem;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.hero-badge {
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 0.8rem;
  background: rgba(0, 0, 0, 0.25);
  border: 1px solid var(--lc-border);
  color: var(--lc-text-muted);
}

.panel {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 16px;
  padding: 26px 28px;
  margin-bottom: 24px;
  box-shadow: var(--shadow-card);
}

.panel-gen {
  border-color: rgba(91, 124, 250, 0.2);
}

.panel-ana {
  border-color: rgba(17, 153, 142, 0.2);
}

.panel-head {
  display: flex;
  gap: 14px;
  margin-bottom: 22px;
}

.panel-icon {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gen-icon {
  background: rgba(91, 124, 250, 0.15);
  color: #8aa4ff;
}

.ana-icon {
  background: rgba(17, 153, 142, 0.15);
  color: #5ddeb8;
}

.panel-head h2 {
  margin: 0 0 4px;
  font-size: 1.2rem;
  font-weight: 700;
}

.panel-sub {
  margin: 0;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

.chip-section {
  margin-bottom: 16px;
}

.chip-label {
  margin: 0 0 8px;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--lc-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  font-size: 0.85rem;
  cursor: pointer;
  transition: border-color 0.15s, background 0.15s, color 0.15s, transform 0.12s;
}

.chip:hover:not(:disabled) {
  border-color: rgba(91, 124, 250, 0.5);
  color: var(--lc-text);
}

.chip.active {
  border-color: rgba(91, 124, 250, 0.7);
  background: rgba(91, 124, 250, 0.12);
  color: #a8b8ff;
  transform: translateY(-1px);
}

.chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chip-ico {
  font-size: 0.9rem;
  opacity: 0.85;
}

.chip-ico.mono {
  font-family: var(--font-mono);
  font-size: 0.72rem;
  font-weight: 700;
}

.chip-diff.diff-简单.active {
  border-color: rgba(0, 184, 163, 0.6);
  background: rgba(0, 184, 163, 0.12);
  color: var(--lc-easy);
}

.chip-diff.diff-中等.active {
  border-color: rgba(255, 192, 30, 0.6);
  background: rgba(255, 192, 30, 0.12);
  color: var(--lc-medium);
}

.chip-diff.diff-困难.active {
  border-color: rgba(255, 55, 95, 0.6);
  background: rgba(255, 55, 95, 0.12);
  color: var(--lc-hard);
}

.row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

@media (max-width: 640px) {
  .row-2 {
    grid-template-columns: 1fr;
  }
}

.btn-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 28px;
  border: none;
  border-radius: 12px;
  font-weight: 700;
  font-size: 0.95rem;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  margin-top: 8px;
}

.btn-action:disabled {
  opacity: 0.65;
  cursor: wait;
}

.btn-gen {
  background: linear-gradient(135deg, #5b7cfa, #7c5cbf);
  color: #fff;
  box-shadow: 0 4px 20px rgba(91, 124, 250, 0.3);
}

.btn-gen:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 28px rgba(91, 124, 250, 0.4);
}

.btn-save {
  background: linear-gradient(135deg, #11998e, #38ef7d);
  color: #0c0e14;
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(17, 153, 142, 0.35);
}

.btn-ana {
  background: linear-gradient(135deg, #11998e, #2cbb5d);
  color: #fff;
  margin-top: 0;
  margin-left: auto;
  flex-shrink: 0;
}

.btn-ana:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(44, 187, 93, 0.35);
}

.btn-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.btn-spinner.dark {
  border-color: rgba(12, 14, 20, 0.2);
  border-top-color: #0c0e14;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.msg-err {
  color: var(--lc-red);
  font-size: 0.9rem;
  margin: 12px 0 0;
}

.problem-preview {
  margin-top: 24px;
  border: 1px solid var(--lc-border);
  border-radius: 14px;
  overflow: hidden;
}

.problem-header {
  background: linear-gradient(135deg, rgba(91, 124, 250, 0.1), rgba(124, 92, 191, 0.08));
  padding: 18px 22px;
  border-bottom: 1px solid var(--lc-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.problem-title {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 700;
}

.problem-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
}

.tag-plate {
  background: rgba(91, 124, 250, 0.15);
  color: #8aa4ff;
}

.tag-lang {
  background: rgba(255, 161, 22, 0.12);
  color: var(--lc-accent);
}

.tag-简单 {
  background: rgba(0, 184, 163, 0.15);
  color: var(--lc-easy);
}

.tag-中等 {
  background: rgba(255, 192, 30, 0.15);
  color: var(--lc-medium);
}

.tag-困难 {
  background: rgba(255, 55, 95, 0.15);
  color: var(--lc-hard);
}

.problem-content {
  padding: 20px 22px;
}

.content-section {
  margin-bottom: 18px;
}

.content-section:last-child {
  margin-bottom: 0;
}

.content-section h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 8px;
  font-size: 0.88rem;
  color: var(--lc-text-muted);
  font-weight: 600;
}

.sec-ico {
  font-size: 1rem;
}

.content-text {
  margin: 0;
  line-height: 1.7;
  font-size: 0.95rem;
}

.sample-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 600px) {
  .sample-box {
    grid-template-columns: 1fr;
  }
}

.sample-item {
  background: #080a10;
  border: 1px solid var(--lc-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.sample-label {
  padding: 8px 12px;
  background: var(--lc-surface-2);
  font-size: 0.75rem;
  color: var(--lc-text-muted);
  font-weight: 600;
  border-bottom: 1px solid var(--lc-border);
}

.sample-code {
  margin: 0;
  padding: 12px;
  font-size: 0.85rem;
  line-height: 1.5;
  overflow: auto;
  max-height: 200px;
}

.problem-actions {
  padding: 16px 22px;
  background: var(--lc-surface-2);
  border-top: 1px solid var(--lc-border);
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.admin-hint {
  margin: 0;
  font-size: 0.82rem;
  color: var(--lc-text-muted);
}

.lang-row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 14px;
}

.lang-row .chip-label {
  width: 100%;
}

.editor-wrap {
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  overflow: hidden;
  background: #080a10;
}

.editor-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 14px;
  background: var(--lc-surface-2);
  border-bottom: 1px solid var(--lc-border);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.dot.red {
  background: #ff5f57;
}
.dot.yellow {
  background: #febc2e;
}
.dot.green {
  background: #28c840;
}

.editor-fname {
  margin-left: 8px;
  font-size: 0.78rem;
  font-family: var(--font-mono);
  color: var(--lc-text-muted);
}

.editor {
  width: 100%;
  min-height: 200px;
  padding: 16px;
  font-family: var(--font-mono);
  font-size: 0.85rem;
  background: transparent;
  border: none;
  border-radius: 0;
  resize: vertical;
  line-height: 1.55;
}

.editor:focus {
  box-shadow: none;
}

.ana-grid {
  margin-top: 20px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

.ana-wide {
  grid-column: 1 / -1;
}

@media (max-width: 640px) {
  .ana-grid {
    grid-template-columns: 1fr;
  }
  .ana-wide {
    grid-column: auto;
  }
}

.ana-card {
  padding: 18px;
  background: linear-gradient(160deg, var(--lc-surface-2), rgba(20, 24, 32, 0.5));
  border: 1px solid var(--lc-border);
  border-radius: 12px;
}

.ana-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.ana-ico {
  font-size: 1.1rem;
}

.ana-card h3 {
  margin: 0;
  font-size: 0.9rem;
  font-weight: 600;
}

.ana-card p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.65;
}

.pre {
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
