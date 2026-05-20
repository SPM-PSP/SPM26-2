<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageBack from '@/components/PageBack.vue'
import { fetchProblemDetail, fetchProblemSolution } from '@/api/problem'
import { judgeSubmit, type JudgeApiLanguage } from '@/api/judge'
import http from '@/api/http'
import type { JudgeSubmitResult, ProblemDetail, ProblemSolution, SolutionItem } from '@/types/api'
import { difficultyClass, difficultyLabel, formatAcceptRate, verdictClass, verdictText } from '@/utils/format'
import { useProblemEditStore } from '@/stores/problemEdit'

const props = defineProps<{ id: string }>()
const route = useRoute()
const router = useRouter()
const problemEditStore = useProblemEditStore()

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

const pythonTemplate = `def main():
    pass

if __name__ == "__main__":
    main()
`

const code = ref(cppTemplate)
const judgeResult = ref<JudgeSubmitResult | null>(null)
const submitApiMessage = ref('')
const judging = ref(false)
const running = ref(false)
const judgeErr = ref('')
const judgeLang = ref<JudgeApiLanguage>('C++')
const codeEditor = ref<HTMLTextAreaElement | null>(null)

// 自定义测试输入输出
const customInput = ref('')
const customOutput = ref('')

const problemId = computed(() => Number(props.id || route.params.id))

// 从 store 恢复代码或使用默认模板
function loadCodeFromCache() {
  const cached = problemEditStore.codeCache[problemId.value]
  if (cached) {
    code.value = cached.code
    judgeLang.value = cached.language as JudgeApiLanguage
  } else {
    // 使用默认模板
    code.value = cppTemplate
    judgeLang.value = 'C++'
  }
}

// 保存代码到 store
function saveCodeToCache() {
  problemEditStore.codeCache[problemId.value] = {
    code: code.value,
    language: judgeLang.value,
  }
}

// 监听代码变化，自动保存
watch(code, () => {
  saveCodeToCache()
})

watch(judgeLang, () => {
  saveCodeToCache()
})

watch(judgeLang, (lang) => {
  // 只在代码是其他语言的模板时才切换
  if (lang === 'C++' && (code.value.trim().startsWith('public class Main') || code.value.includes('def main'))) {
    code.value = cppTemplate
    saveCodeToCache()
  } else if (lang === 'Java' && (code.value.includes('#include') || code.value.includes('def main'))) {
    code.value = javaTemplate
    saveCodeToCache()
  } else if (lang === 'Python' && (code.value.includes('#include') || code.value.includes('public class Main'))) {
    code.value = pythonTemplate
    saveCodeToCache()
  }
})

function handleTabKey(e: KeyboardEvent) {
  if (e.key === 'Tab') {
    e.preventDefault()
    const target = e.target as HTMLTextAreaElement
    const start = target.selectionStart
    const end = target.selectionEnd
    const value = target.value

    // 插入 4 个空格作为缩进
    target.value = value.substring(0, start) + '    ' + value.substring(end)

    // 移动光标位置
    target.selectionStart = target.selectionEnd = start + 4
  }
}

/**
 * 处理括号自动补全和配对
 */
function handleBracketAutoComplete(e: KeyboardEvent, target: HTMLTextAreaElement) {
  const brackets: Record<string, string> = {
    '(': ')',
    '[': ']',
    '{': '}',
    '"': '"',
    "'": "'",
    '`': '`',
  }

  const openBracket = e.key
  const closeBracket = brackets[openBracket]

  if (!closeBracket) return false

  e.preventDefault()

  const start = target.selectionStart
  const end = target.selectionEnd
  const value = target.value

  // 插入配对括号
  const newValue = value.substring(0, start) + openBracket + closeBracket + value.substring(end)
  target.value = newValue

  // 光标放在两个括号中间
  target.selectionStart = target.selectionEnd = start + 1

  return true
}

/**
 * 处理在闭合括号前输入时，直接跳过而不是重复输入
 */
function handleSkipCloseBracket(e: KeyboardEvent, target: HTMLTextAreaElement): boolean {
  const closeBrackets = [')', ']', '}', '"', "'", '`']
  
  if (!closeBrackets.includes(e.key)) return false

  const start = target.selectionStart
  const value = target.value

  // 如果光标后面就是相同的闭合括号，则跳过它
  if (value[start] === e.key) {
    e.preventDefault()
    target.selectionStart = target.selectionEnd = start + 1
    return true
  }

  return false
}

/**
 * 处理退格键删除配对括号
 */
function handleBackspaceWithBrackets(e: KeyboardEvent, target: HTMLTextAreaElement): boolean {
  if (e.key !== 'Backspace') return false

  const start = target.selectionStart
  const end = target.selectionEnd
  const value = target.value

  // 只有当光标在配对括号中间且没有选中文本时才处理
  if (start === end && start > 0) {
    const beforeCursor = value[start - 1]
    const afterCursor = value[start]

    const bracketPairs: Record<string, string> = {
      '(': ')',
      '[': ']',
      '{': '}',
      '"': '"',
      "'": "'",
      '`': '`',
    }

    // 如果前后是配对的括号，则一起删除
    if (bracketPairs[beforeCursor] === afterCursor) {
      e.preventDefault()
      target.value = value.substring(0, start - 1) + value.substring(start + 1)
      target.selectionStart = target.selectionEnd = start - 1
      return true
    }
  }

  return false
}

function handleEnterKey(e: KeyboardEvent) {
  if (e.key === 'Enter') {
    e.preventDefault()
    const target = e.target as HTMLTextAreaElement
    const start = target.selectionStart
    const value = target.value

    // 获取当前行的内容
    const lastNewline = value.lastIndexOf('\n', start - 1)
    const currentLine = value.substring(lastNewline + 1, start)

    // 提取当前行开头的空白字符（缩进）
    const indentMatch = currentLine.match(/^(\s*)/)
    const currentIndent = indentMatch ? indentMatch[1] : ''

    // 检查是否需要增加缩进（行尾有 { 或 : ）
    const trimmedLine = currentLine.trimEnd()
    let extraIndent = ''
    if (trimmedLine.endsWith('{') || trimmedLine.endsWith(':')) {
      extraIndent = '    '
    }

    // 检查光标后是否有闭合括号，如果有，需要特殊处理
    const nextChar = value[start]
    if (nextChar === '}' || nextChar === ')' || nextChar === ']') {
      // 计算减少一级缩进后的位置
      const reducedIndent = currentIndent.length >= 4 ? currentIndent.substring(4) : ''
      
      // 构建新内容：
      // 1. 光标前的内容
      // 2. 换行 + 当前缩进 + 额外缩进（新行，用于输入代码）
      // 3. 换行 + 减少的缩进 + 闭合括号
      // 4. 闭合括号后面的内容
      const beforeCursor = value.substring(0, start)
      const afterClosingBracket = value.substring(start + 1)
      
      // 注意：需要在第3行包含闭合括号本身
      const newContent = beforeCursor + 
                        '\n' + currentIndent + extraIndent + 
                        '\n' + reducedIndent + nextChar + 
                        afterClosingBracket
      
      target.value = newContent
      
      // 光标移动到中间的新行
      const cursorPos = start + 1 + currentIndent.length + extraIndent.length
      target.selectionStart = target.selectionEnd = cursorPos
    } else {
      // 普通情况：直接换行并保持缩进
      const insertion = '\n' + currentIndent + extraIndent
      target.value = value.substring(0, start) + insertion + value.substring(start)
      
      // 移动光标到新行
      target.selectionStart = target.selectionEnd = start + insertion.length
    }
  }
}

/**
 * 统一的代码编辑器键盘事件处理
 */
function handleCodeEditorKeydown(e: KeyboardEvent) {
  const target = e.target as HTMLTextAreaElement

  // 1. 处理 Tab 键缩进
  if (e.key === 'Tab') {
    handleTabKey(e)
    return
  }

  // 2. 处理括号自动补全（输入开括号时）
  const openBrackets = ['(', '[', '{', '"', "'", '`']
  if (openBrackets.includes(e.key)) {
    if (handleBracketAutoComplete(e, target)) {
      return
    }
  }

  // 3. 处理跳过闭合括号
  if (handleSkipCloseBracket(e, target)) {
    return
  }

  // 4. 处理退格键删除配对括号
  if (handleBackspaceWithBrackets(e, target)) {
    return
  }

  // 5. 处理 Enter 键自动缩进
  if (e.key === 'Enter') {
    handleEnterKey(e)
    return
  }
}

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
      // 修复点：增加 solutions.value 的非空判断
      if (t === 'solution' && (!solutions.value || !solutions.value.length) && !solLoading.value) void loadSolution()
    },
)

watch(
    () => props.id,
    () => {
      void loadDetail()
      solutions.value = []
      judgeResult.value = null
      submitApiMessage.value = ''
      // 记住新题目 ID
      problemEditStore.lastProblemId = problemId.value
      // 恢复新题目的代码
      loadCodeFromCache()
    },
)

onMounted(() => {
  void loadDetail()
  // 记住当前题目 ID
  problemEditStore.lastProblemId = problemId.value
  // 恢复代码
  loadCodeFromCache()
})

/** 提交测评 - 使用数据库中的测试用例 */
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

    // 后端现在直接返回 JudgeSubmitResult 格式的数据
    if (res.code !== 200 || !res.data) {
      judgeErr.value = res.message || '提交失败'
      return
    }

    judgeResult.value = res.data
    submitApiMessage.value = '提交成功'
  } catch (e: unknown) {
    judgeErr.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    judging.value = false
  }
}

/** 运行 - 使用自定义输入输出进行测试 */
async function runCode() {
  if (!detail.value) return
  
  // 验证测试输入和输出都必须填写
  if (!customInput.value.trim()) {
    judgeErr.value = '请输入测试输入'
    return
  }
  
  if (!customOutput.value.trim()) {
    judgeErr.value = '请输入测试输出（期望结果）'
    return
  }

  running.value = true
  judgeErr.value = ''
  judgeResult.value = null
  submitApiMessage.value = ''

  try {
    const { data } = await http.post('/api/judge/submit', {
      code: code.value,
      language: mapLanguageToBackend(judgeLang.value),
      input: customInput.value,
      answer: customOutput.value || '',
      timeLimit: detail.value.timeLimit,
      memoryLimit: String(detail.value.memoryLimit),
    })

    const backendResult = data

    // 运行接口返回的是 JudgeResponse，status 是字符串（AC/WA/CE/RE/TLE 等）
    // 需要转换为前端格式
    const statusMap: Record<string, number> = {
      'AC': 0,
      'CE': 1,
      'RE': 2,
      'TLE': 3,
      'MLE': 2,
      'WA': 2,
      'SYSTEM_ERROR': 2,
    }

    const result = statusMap[backendResult.status] ?? 2

    judgeResult.value = {
      submissionId: 0, // 运行不保存提交记录
      runTime: 0,
      memory: 0,
      errorMsg: backendResult.compileLog || backendResult.runtimeLog || backendResult.diffLog || '',
      submitTime: new Date().toLocaleString('zh-CN'),
      result: result,
      passCount: result === 0 ? 1 : 0,
      totalCount: 1,
    }
    
    // 根据状态设置提示信息
    if (result === 0) {
      submitApiMessage.value = '答案正确'
    } else {
      submitApiMessage.value = backendResult.message || '运行完成'
    }
  } catch (e: unknown) {
    judgeErr.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    running.value = false
  }
}

/** 使用样例数据填充自定义测试 */
function useSample() {
  if (!detail.value?.sampleInput) return
  customInput.value = detail.value.sampleInput
  customOutput.value = detail.value.sampleOutput || ''
}

/** 映射前端语言到后端语言 */
function mapLanguageToBackend(lang: JudgeApiLanguage): string {
  const map: Record<JudgeApiLanguage, string> = {
    'C++': 'cpp',
    'Java': 'java',
    'Python': 'python',
  }
  return map[lang]
}

/** 转换后端结果为前端格式 */
function convertBackendResult(backendResult: any): JudgeSubmitResult {
  const statusMap: Record<string, number> = {
    'ACCEPTED': 0,
    'COMPILE_ERROR': 1,
    'RUNTIME_ERROR': 2,
    'TIME_LIMIT_EXCEEDED': 3,
    'MEMORY_LIMIT_EXCEEDED': 2,
    'WRONG_ANSWER': 2,
    'SYSTEM_ERROR': 2,
  }

  const result = statusMap[backendResult.status] ?? 2 // 默认归为运行错误

  return {
    submissionId: backendResult.submissionId || 0,
    runTime: backendResult.runTime || 0,
    memory: backendResult.memory || 0,
    errorMsg: backendResult.errorMsg || '',
    submitTime: backendResult.submitTime || new Date().toLocaleString('zh-CN'),
    result: result,
    passCount: backendResult.passCount || 0,
    totalCount: backendResult.totalCount || 1,
  }
}

function goSubmissionDetail() {
  if (!judgeResult.value?.submissionId) return
  void router.push({ name: 'submission-detail', params: { id: String(judgeResult.value.submissionId) } })
}
</script>

<template>
  <div v-if="loading" class="state">加载题目…</div>
  <div v-else-if="err || !detail" class="state err">{{ err || '题目不存在' }}</div>
  <div v-else class="detail-wrap">
    <PageBack to="/problems" label="返回题库" />
    <div class="split">
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
            <option value="Python">Python</option>
          </select>
        </label>
        <div class="actions">
          <button type="button" class="btn-run" :disabled="running" @click="runCode">
            {{ running ? '运行中…' : '运行' }}
          </button>
          <button type="button" class="btn-submit" :disabled="judging" @click="submitAndJudge">
            {{ judging ? '提交评测中…' : '提交测评' }}
          </button>
        </div>
      </div>
      <textarea
          ref="codeEditor"
          v-model="code"
          class="editor"
          spellcheck="false"
          @keydown="handleCodeEditorKeydown"
      />

      <div class="io-block">
        <div class="io-title-row">
          <h4 class="io-title">自定义测试用例</h4>
          <button type="button" class="btn-use-sample" @click="useSample" :disabled="!detail?.sampleInput">
            <span class="icon">📋</span>
            使用样例
          </button>
        </div>
        <label>测试输入</label>
        <textarea v-model="customInput" class="io custom-io" placeholder="输入测试数据..." />
        <label>期望输出（可选）</label>
        <textarea v-model="customOutput" class="io custom-io" placeholder="输入期望的输出结果..." />
      </div>

      <div class="verdict">
        <p v-if="judgeErr" class="judge-err">{{ judgeErr }}</p>
        <template v-else-if="judgeResult">
          <div class="verdict-line" :class="{ ac: judgeResult.result === 0, wa: judgeResult.result !== 0 }">
            <span class="verdict-pill" :class="verdictClass(judgeResult.result)">{{
                verdictText(judgeResult.result)
              }}</span>
            <span v-if="submitApiMessage" class="muted sm"> — {{ submitApiMessage }}</span>
          </div>
          <p class="meta-line">
            用例 {{ judgeResult.passCount }} / {{ judgeResult.totalCount }} · {{ judgeResult.runTime }} ms ·
            {{ judgeResult.memory }} KB · {{ judgeResult.submitTime }}
          </p>
          
          <!-- 错误信息（编译错误、运行时错误等） -->
          <pre v-if="judgeResult.errorMsg" class="err-msg">{{ judgeResult.errorMsg }}</pre>
          
          <div class="row-actions">
            <button v-if="judgeResult.submissionId > 0" type="button" class="btn-link" @click="goSubmissionDetail">
              查看提交详情 #{{ judgeResult.submissionId }}
            </button>
          </div>
        </template>
        <p v-else class="muted hint">运行或提交后将显示结果</p>
      </div>
    </section>
    </div>
  </div>
</template>

<style scoped>
.detail-wrap {
  display: flex;
  flex-direction: column;
  gap: 0;
}

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
  background: var(--lc-green);
  color: #fff;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-run:disabled {
  opacity: 0.6;
  cursor: wait;
}

.btn-submit {
  padding: 6px 16px;
  border-radius: 6px;
  border: none;
  background: var(--lc-accent);
  color: #111;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.8rem;
}

.btn-submit:disabled {
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

.io-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.io-title {
  margin: 0;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.btn-use-sample {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-use-sample:hover:not(:disabled) {
  background: var(--lc-accent);
  color: #fff;
  border-color: var(--lc-accent);
}

.btn-use-sample:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.btn-use-sample .icon {
  font-size: 0.9rem;
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

.custom-io {
  min-height: 60px;
  border: 1px solid var(--lc-border);
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

.verdict-line.wa {
  color: var(--lc-red);
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
  background: rgba(255, 55, 95, 0.05);
  padding: 10px;
  border-radius: 6px;
  border-left: 3px solid var(--lc-red);
}

.output-block {
  margin-top: 12px;
}

.output-title {
  margin: 0 0 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.output-title.success {
  color: var(--lc-green);
}

.user-output {
  margin: 0;
  padding: 10px;
  background: #0d0d0d;
  border-radius: 6px;
  border: 1px solid var(--lc-border);
  font-size: 0.8rem;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-output.success {
  border-color: var(--lc-green);
  background: rgba(0, 184, 163, 0.05);
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
