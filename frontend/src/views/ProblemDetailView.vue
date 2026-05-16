<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { fetchProblemDetail, fetchProblemSolution } from '@/api/problem'
import { judgeSubmit, type JudgeApiLanguage } from '@/api/judge'
import type { JudgeSubmitResult, ProblemDetail, ProblemSolution, SolutionItem } from '@/types/api'
import { difficultyClass, difficultyLabel, formatAcceptRate, verdictClass, verdictText } from '@/utils/format'
import PageBack from '@/components/PageBack.vue'

defineOptions({ name: 'ProblemDetailView' })

const props = defineProps<{ id: string }>()
const route = useRoute()
const router = useRouter()

const tab = ref<'desc' | 'solution'>('desc')
const detail = ref<ProblemDetail | null>(null)
const solutions = ref<SolutionItem[]>([])
const loading = ref(true)
const solLoading = ref(false)
const err = ref('')

const cppTemplate = `#include <iostream>
using namespace std;

int main() {

    return 0;
}
`

const javaTemplate = `public class Main {
    public static void main(String[] args) {

    }
}
`

const code = ref(cppTemplate)
const judgeResult = ref<JudgeSubmitResult | null>(null)
const submitApiMessage = ref('')
const judging = ref(false)
const judgeErr = ref('')
const judgeLang = ref<JudgeApiLanguage>('C++')

const problemId = computed(() => Number(props.id || route.params.id))

watch(judgeLang, (lang) => {
  if (lang === 'C++' && code.value.trim().startsWith('public class Main')) {
    code.value = cppTemplate
  } else if (lang === 'Java' && code.value.includes('#include')) {
    code.value = javaTemplate
  }
})

async function loadDetail() {
  loading.value = true
  err.value = ''
  try {
    const res = await fetchProblemDetail(problemId.value)
    if (res.code !== 200) {
      err.value = res.message || '加载失败'
      detail.value = null
      return
    }
    detail.value = res.data
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

async function loadSolution() {
  solLoading.value = true
  try {
    const res = await fetchProblemSolution(problemId.value)
    if (res.code === 200 && res.data) {
      const s = (res.data as ProblemSolution).solution
      solutions.value = Array.isArray(s) ? s : []
    } else {
      solutions.value = []
    }
  } catch {
    solutions.value = []
  } finally {
    solLoading.value = false
  }
}

watch(
  () => tab.value,
  (t) => {
    if (t === 'solution' && !solutions.value.length && !solLoading.value) void loadSolution()
  },
)

watch(
  () => props.id,
  () => {
    void loadDetail()
    solutions.value = []
    judgeResult.value = null
    submitApiMessage.value = ''
  },
)

onMounted(() => {
  void loadDetail()
})

/** 文档 5.2：提交代码并触发服务端全量用例评测（含运行与判题） */
async function submitAndJudge() {
  if (!detail.value) return
  judging.value = true
  judgeErr.value = ''
  judgeResult.value = null
  submitApiMessage.value = ''
  try {
    const res = await judgeSubmit({
      problemId: problemId.value,
      language: judgeLang.value,
      code: code.value,
    })
    submitApiMessage.value = res.message || ''
    if (res.code !== 200 || !res.data) {
      judgeErr.value = res.message || '提交失败'
      return
    }
    judgeResult.value = res.data
  } catch (e: unknown) {
    judgeErr.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    judging.value = false
  }
}

function goSubmissionDetail() {
  if (!judgeResult.value?.submissionId) return
  void router.push({ name: 'submission-detail', params: { id: String(judgeResult.value.submissionId) } })
}
</script>

<template>
  <PageBack label="返回题库" to="/problems" />
  <div v-if="loading" class="state">加载题目…</div>
  <div v-else-if="err || !detail" class="state err">{{ err || '题目不存在' }}</div>
  <div v-else class="split">
    <section class="left card">
      <div class="title-bar">
        <h1>{{ detail.title }}</h1>
        <span class="pill" :class="difficultyClass(detail.difficulty)">{{ difficultyLabel(detail.difficulty) }}</span>
      </div>
      <div class="meta">
        <span>{{ detail.categoryNames?.join(' · ') }}</span>
        <span class="dot">·</span>
        <span>通过率 {{ formatAcceptRate(detail.acceptRate) }}</span>
        <span class="dot">·</span>
        <span>时间限制 {{ detail.timeLimit }} ms</span>
        <span class="dot">·</span>
        <span>内存 {{ detail.memoryLimit }} KB</span>
      </div>

      <div class="tabs">
        <button type="button" class="tab" :class="{ on: tab === 'desc' }" @click="tab = 'desc'">题目描述</button>
        <button type="button" class="tab" :class="{ on: tab === 'solution' }" @click="tab = 'solution'">题解</button>
      </div>

      <div v-show="tab === 'desc'" class="prose">
        <h3>描述</h3>
        <p class="pre">{{ detail.description }}</p>
        <h3>输入格式</h3>
        <p class="pre">{{ detail.inputFormat }}</p>
        <h3>输出格式</h3>
        <p class="pre">{{ detail.outputFormat }}</p>
        <h3>样例</h3>
        <div class="sample">
          <div>
            <div class="sample-h">输入</div>
            <pre>{{ detail.sampleInput }}</pre>
          </div>
          <div>
            <div class="sample-h">输出</div>
            <pre>{{ detail.sampleOutput }}</pre>
          </div>
        </div>
      </div>

      <div v-show="tab === 'solution'" class="prose">
        <div v-if="solLoading" class="muted">加载题解…</div>
        <template v-else-if="solutions.length">
          <article v-for="s in solutions" :key="s.solutionId" class="sol">
            <h3>{{ s.title }}</h3>
            <p class="muted sm">{{ s.createUserName }} · {{ s.language }} · {{ s.createTime }}</p>
            <p class="pre">{{ s.content }}</p>
            <pre class="code-block">{{ s.code }}</pre>
          </article>
        </template>
        <p v-else class="muted">暂无题解</p>
      </div>
    </section>

    <section class="right card">
      <div class="editor-head">
        <label class="lang-wrap">
          <span class="lang-label">语言</span>
          <select v-model="judgeLang" class="lang-select">
            <option value="C++">C++</option>
            <option value="Java">Java</option>
          </select>
        </label>
        <div class="actions">
          <button type="button" class="btn-run" :disabled="judging" @click="submitAndJudge">
            {{ judging ? '提交评测中…' : '提交并评测' }}
          </button>
        </div>
      </div>
      <p class="hint sm">提交后由评测机在服务端运行代码，并对全部测试用例判题（文档 5.2）。</p>
      <textarea v-model="code" class="editor" spellcheck="false" />

      <div class="io-block">
        <h4 class="io-title">题目样例（参考）</h4>
        <p class="muted sm">样例与左侧描述一致，不参与本次请求；自测请在本地 IDE 运行。</p>
        <label>样例输入</label>
        <pre class="io ro">{{ detail.sampleInput }}</pre>
        <label>样例输出</label>
        <pre class="io ro">{{ detail.sampleOutput }}</pre>
      </div>

      <div class="verdict">
        <p v-if="judgeErr" class="judge-err">{{ judgeErr }}</p>
        <template v-else-if="judgeResult">
          <div class="verdict-line" :class="{ ac: judgeResult.result === 0 }">
            <span class="verdict-pill" :class="verdictClass(judgeResult.result)">{{
              verdictText(judgeResult.result)
            }}</span>
            <span v-if="submitApiMessage" class="muted sm"> — {{ submitApiMessage }}</span>
          </div>
          <p class="meta-line">
            用例 {{ judgeResult.passCount }} / {{ judgeResult.totalCount }} · {{ judgeResult.runTime }} ms ·
            {{ judgeResult.memory }} KB · {{ judgeResult.submitTime }}
          </p>
          <p v-if="judgeResult.errorMsg" class="err-msg">{{ judgeResult.errorMsg }}</p>
          <div class="row-actions">
            <button type="button" class="btn-link" @click="goSubmissionDetail">
              查看提交详情 #{{ judgeResult.submissionId }}
            </button>
          </div>
        </template>
        <p v-else class="muted hint">提交代码后在此查看评测摘要。</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.state {
  padding: 48px;
  text-align: center;
  color: var(--lc-text-muted);
}

.state.err {
  color: var(--lc-red);
}

.split {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(360px, 480px);
  gap: 16px;
  align-items: start;
}

@media (max-width: 960px) {
  .split {
    grid-template-columns: 1fr;
  }
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 18px 20px;
}

.title-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.title-bar h1 {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 700;
}

.pill {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.diff-easy {
  color: var(--lc-easy);
  background: rgba(0, 184, 163, 0.12);
}
.diff-medium {
  color: var(--lc-medium);
  background: rgba(255, 192, 30, 0.12);
}
.diff-hard {
  color: var(--lc-hard);
  background: rgba(255, 55, 95, 0.12);
}

.meta {
  margin-top: 8px;
  font-size: 0.8rem;
  color: var(--lc-text-muted);
}

.dot {
  margin: 0 6px;
}

.tabs {
  display: flex;
  gap: 0;
  margin: 18px 0 12px;
  border-bottom: 1px solid var(--lc-border);
}

.tab {
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: var(--lc-text-muted);
  cursor: pointer;
  font-size: 0.9rem;
  margin-bottom: -1px;
  border-bottom: 2px solid transparent;
}

.tab.on {
  color: var(--lc-accent);
  border-bottom-color: var(--lc-accent);
}

.prose h3 {
  margin: 16px 0 8px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
  font-weight: 600;
}

.pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 0.9rem;
  line-height: 1.6;
}

.sample {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

@media (max-width: 600px) {
  .sample {
    grid-template-columns: 1fr;
  }
}

.sample-h {
  font-size: 0.75rem;
  color: var(--lc-text-muted);
  margin-bottom: 4px;
}

.sample pre {
  margin: 0;
  padding: 10px;
  background: var(--lc-bg);
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  font-size: 0.8rem;
  overflow: auto;
}

.sol {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--lc-border);
}

.code-block {
  margin: 8px 0 0;
  padding: 12px;
  background: #0d0d0d;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  font-size: 0.8rem;
  overflow: auto;
}

.muted {
  color: var(--lc-text-muted);
}

.muted.sm {
  font-size: 0.8rem;
}

.hint {
  font-size: 0.75rem;
}

.hint.sm {
  margin: 0 0 8px;
  line-height: 1.4;
}

.editor-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.lang-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--lc-text-muted);
}

.lang-label {
  white-space: nowrap;
}

.lang-select {
  padding: 5px 10px;
  border-radius: 6px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  font-size: 0.8rem;
  cursor: pointer;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn-run {
  padding: 6px 16px;
  border-radius: 6px;
  border: none;
  background: var(--lc-accent);
  color: #111;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-run:disabled {
  opacity: 0.6;
  cursor: wait;
}

.editor {
  width: 100%;
  min-height: 220px;
  padding: 12px;
  font-family: var(--font-mono);
  font-size: 0.8rem;
  line-height: 1.45;
  resize: vertical;
  background: #0d0d0d;
  border-radius: 8px;
}

.io-block {
  margin-top: 12px;
}

.io-title {
  margin: 0 0 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.io-block label {
  display: block;
  margin-top: 10px;
  margin-bottom: 4px;
  font-size: 0.75rem;
  color: var(--lc-text-muted);
}

.io {
  width: 100%;
  min-height: 72px;
  padding: 8px;
  font-family: var(--font-mono);
  font-size: 0.78rem;
  resize: vertical;
  background: #0d0d0d;
  border-radius: 8px;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.io.ro {
  border: 1px solid var(--lc-border);
}

.verdict {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--lc-border);
}

.judge-err {
  color: var(--lc-red);
  font-size: 0.85rem;
}

.verdict-line {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--lc-red);
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
}

.verdict-line.ac {
  color: var(--lc-green);
}

.verdict-pill {
  font-weight: 700;
}

.meta-line {
  margin: 8px 0 0;
  font-size: 0.8rem;
  color: var(--lc-text-muted);
}

.err-msg {
  margin: 8px 0 0;
  color: var(--lc-red);
  font-size: 0.85rem;
  white-space: pre-wrap;
}

.row-actions {
  margin-top: 10px;
}

.btn-link {
  padding: 0;
  border: none;
  background: none;
  color: var(--lc-accent);
  cursor: pointer;
  font-size: 0.85rem;
  text-decoration: underline;
}

code {
  font-family: var(--font-mono);
  font-size: 0.8em;
  color: var(--lc-accent);
}
</style>
