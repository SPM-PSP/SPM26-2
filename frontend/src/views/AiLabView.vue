<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { analyzeCode } from '@/api/algorithm'
import { aiCreateProblem } from '@/api/ai'
import { fetchCategories } from '@/api/problem'
import { useAiLabStore } from '@/stores/aiLab'
import { useAuthStore } from '@/stores/auth'
import type { CategoryVO } from '@/types/api'
import { normalizeProblemDetail, problemPreviewMarkdown } from '@/utils/problemNormalize'
import { difficultyLabel, formatAcceptRate } from '@/utils/format'

defineOptions({ name: 'AiLabView' })

const router = useRouter()
const auth = useAuthStore()
const lab = useAiLabStore()

const diffs = ['简单', '中等', '困难']
const langs = ['C++', 'Java', 'Python']

const categories = ref<CategoryVO[]>([])
const catsLoading = ref(true)
const genLoading = ref(false)
const genErr = ref('')
const anaLoading = ref(false)
const anaErr = ref('')
const anaOut = ref<{ plate?: string; complexity?: string; style?: string } | null>(null)

onMounted(async () => {
  catsLoading.value = true
  try {
    const res = await fetchCategories()
    if (res.code === 200) categories.value = res.data ?? []
  } finally {
    catsLoading.value = false
  }
})

function toggleCat(name: string) {
  const i = lab.selectedCategories.indexOf(name)
  if (i >= 0) lab.selectedCategories.splice(i, 1)
  else lab.selectedCategories.push(name)
}

async function onGenerate() {
  if (!auth.isLoggedIn) {
    void router.push({ name: 'login', query: { redirect: '/ai' } })
    return
  }
  if (!lab.selectedCategories.length) {
    genErr.value = '请至少选择一个题目分类'
    return
  }
  genLoading.value = true
  genErr.value = ''
  try {
    const res = await aiCreateProblem({
      difficulty: lab.difficulty,
      categoryNames: [...lab.selectedCategories],
    })
    if (res.code !== 200 || !res.data) {
      genErr.value = res.message || '生成失败'
      return
    }
    const problem = normalizeProblemDetail(res.data as unknown as Record<string, unknown>)
    if (!problem.problemId) {
      genErr.value = '服务端未返回题目 ID，无法开始做题'
      return
    }
    lab.setGenerated(problem, problemPreviewMarkdown(problem))
    void router.push({ name: 'problem-detail', params: { id: String(problem.problemId) } })
  } catch (e: unknown) {
    genErr.value = e instanceof Error ? e.message : '生成失败'
  } finally {
    genLoading.value = false
  }
}

function startSolve() {
  const id = lab.lastGenerated?.problemId
  if (!id) return
  void router.push({ name: 'problem-detail', params: { id: String(id) } })
}

async function onAnalyze() {
  anaLoading.value = true
  anaErr.value = ''
  anaOut.value = null
  try {
    const data = await analyzeCode(lab.analyzeCode, lab.analyzeLang)
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

<template>
  <div class="page">
    <header class="hero">
      <h1>AI 实验室</h1>
      <p class="sub">生成题目将写入题库并可直接进入做题；切换页签或返回题库时保留当前配置与结果。</p>
    </header>

    <div class="tabs">
      <button type="button" class="tab" :class="{ on: lab.activeTab === 'generate' }" @click="lab.activeTab = 'generate'">
        AI 出题
      </button>
      <button type="button" class="tab" :class="{ on: lab.activeTab === 'analyze' }" @click="lab.activeTab = 'analyze'">
        代码分析
      </button>
    </div>

    <section v-show="lab.activeTab === 'generate'" class="panel card">
      <h2>生成练习题</h2>
      <p class="hint">接口：POST /api/v1/ai/judge/create · 需登录</p>

      <div class="field-row">
        <label>难度</label>
        <select v-model="lab.difficulty">
          <option v-for="d in diffs" :key="d" :value="d">{{ d }}</option>
        </select>
      </div>

      <div class="field-block">
        <label>分类（可多选）</label>
        <div v-if="catsLoading" class="muted">加载分类…</div>
        <div v-else-if="categories.length" class="tag-row">
          <button
            v-for="c in categories"
            :key="c.categoryId"
            type="button"
            class="tag"
            :class="{ on: lab.selectedCategories.includes(c.categoryName) }"
            @click="toggleCat(c.categoryName)"
          >
            {{ c.categoryName }}
          </button>
        </div>
        <p v-else class="muted">暂无分类，请管理员先在后台添加。</p>
      </div>

      <div class="actions">
        <button type="button" class="btn primary" :disabled="genLoading" @click="onGenerate">
          {{ genLoading ? '生成并入库中…' : '生成题目' }}
        </button>
        <button v-if="lab.lastGenerated" type="button" class="btn ghost" @click="lab.resetGenerate()">清空结果</button>
      </div>

      <p v-if="genErr" class="err">{{ genErr }}</p>

      <article v-if="lab.lastGenerated" class="preview">
        <div class="preview-head">
          <h3>{{ lab.lastGenerated.title }}</h3>
          <span class="pill">{{ difficultyLabel(lab.lastGenerated.difficulty) }}</span>
        </div>
        <p class="meta">
          {{ lab.lastGenerated.categoryNames?.join(' · ') }} · 通过率
          {{ formatAcceptRate(lab.lastGenerated.acceptRate) }} · #{{ lab.lastGenerated.problemId }}
        </p>
        <pre class="out">{{ lab.genMarkdown }}</pre>
        <button type="button" class="btn primary lg" @click="startSolve">开始做题</button>
      </article>
    </section>

    <section v-show="lab.activeTab === 'analyze'" class="panel card">
      <h2>代码多维分析</h2>
      <div class="field-row">
        <label>语言</label>
        <select v-model="lab.analyzeLang">
          <option v-for="l in langs" :key="l" :value="l">{{ l }}</option>
        </select>
        <button type="button" class="btn primary" :disabled="anaLoading" @click="onAnalyze">
          {{ anaLoading ? '分析中…' : '开始分析' }}
        </button>
      </div>
      <textarea v-model="lab.analyzeCode" class="editor" spellcheck="false" />
      <p v-if="anaErr" class="err">{{ anaErr }}</p>
      <div v-if="anaOut" class="ana-grid">
        <div class="ana-card">
          <h4>算法板块</h4>
          <p>{{ anaOut.plate }}</p>
        </div>
        <div class="ana-card">
          <h4>复杂度</h4>
          <p class="pre">{{ anaOut.complexity }}</p>
        </div>
        <div class="ana-card">
          <h4>代码风格</h4>
          <p class="pre">{{ anaOut.style }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page {
  max-width: 920px;
}

.hero h1 {
  margin: 0 0 8px;
  font-size: 1.6rem;
}

.sub {
  margin: 0 0 20px;
  color: var(--lc-text-muted);
  font-size: 0.9rem;
}

.tabs {
  display: flex;
  gap: 0;
  margin-bottom: 16px;
  border-bottom: 1px solid var(--lc-border);
}

.tab {
  padding: 12px 20px;
  border: none;
  background: transparent;
  color: var(--lc-text-muted);
  cursor: pointer;
  font-size: 0.95rem;
  margin-bottom: -1px;
  border-bottom: 2px solid transparent;
}

.tab.on {
  color: var(--lc-accent);
  border-bottom-color: var(--lc-accent);
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 22px;
  margin-bottom: 16px;
}

.panel h2 {
  margin: 0 0 8px;
  font-size: 1.1rem;
}

.hint {
  margin: 0 0 16px;
  font-size: 0.78rem;
  color: var(--lc-text-muted);
}

.field-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.field-row label,
.field-block label {
  font-size: 0.82rem;
  color: var(--lc-text-muted);
  font-weight: 600;
}

.field-block {
  margin-bottom: 16px;
}

.field-block label {
  display: block;
  margin-bottom: 8px;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 0.82rem;
  cursor: pointer;
}

.tag.on {
  border-color: var(--lc-accent);
  color: var(--lc-accent);
  background: rgba(255, 161, 22, 0.1);
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  font-weight: 600;
  cursor: pointer;
  font-size: 0.88rem;
}

.btn.primary {
  background: var(--lc-accent);
  color: #111;
  border: none;
}

.btn.primary.lg {
  margin-top: 14px;
  width: 100%;
  padding: 12px;
}

.btn.ghost {
  background: transparent;
}

.btn:disabled {
  opacity: 0.6;
  cursor: wait;
}

.err {
  color: var(--lc-red);
  font-size: 0.88rem;
}

.preview {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--lc-border);
}

.preview-head {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.preview-head h3 {
  margin: 0;
  font-size: 1.05rem;
}

.pill {
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  background: rgba(255, 161, 22, 0.15);
  color: var(--lc-accent);
}

.meta {
  font-size: 0.82rem;
  color: var(--lc-text-muted);
  margin: 8px 0 12px;
}

.out {
  margin: 0 0 8px;
  padding: 14px;
  background: #0d0d0d;
  border-radius: 8px;
  font-size: 0.85rem;
  white-space: pre-wrap;
  max-height: 280px;
  overflow: auto;
}

.editor {
  width: 100%;
  min-height: 180px;
  padding: 12px;
  font-family: var(--font-mono);
  font-size: 0.8rem;
  margin-top: 10px;
}

.ana-grid {
  display: grid;
  gap: 12px;
  margin-top: 12px;
}

@media (min-width: 640px) {
  .ana-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

.ana-card {
  padding: 14px;
  background: var(--lc-surface-2);
  border-radius: 8px;
  border: 1px solid var(--lc-border);
}

.ana-card h4 {
  margin: 0 0 8px;
  font-size: 0.8rem;
  color: var(--lc-text-muted);
}

.ana-card p {
  margin: 0;
  font-size: 0.88rem;
  line-height: 1.5;
}

.pre {
  white-space: pre-wrap;
}

.muted {
  color: var(--lc-text-muted);
  font-size: 0.85rem;
}
</style>
