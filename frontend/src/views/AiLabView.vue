<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { analyzeCode, generateProblem } from '@/api/algorithm'
import { adminAddProblem, adminAddTestCase } from '@/api/admin'
import { fetchProblemList, fetchCategories } from '@/api/problem'
import PageBack from '@/components/PageBack.vue'
import WaitingPanel from '@/components/lab/WaitingPanel.vue'
import { useAiLabStore } from '@/stores/aiLab'
import { useAuthStore } from '@/stores/auth'
import type { ProblemGenerateResponse, CategoryVO } from '@/types/api'
import { difficultyToApi } from '@/utils/difficulty'

const router = useRouter()
const lab = useAiLabStore()
const auth = useAuthStore()

// 从后端获取的分类列表
const categories = ref<CategoryVO[]>([])
const platesLoading = ref(false)

// 图标映射（用于显示）
const plateIcons: Record<string, string> = {
  数组: '▦',
  链表: '⛓',
  动态规划: 'dp[]',
  贪心: '★',
  字符串: 'Aa',
  图论: '◎-◎',
  数学: '∑',
}
const diffs = ['简单', '中等', '困难']
const langs = ['C++', 'Java', 'Python']
const langIcons: Record<string, string> = { 'C++': 'C+', Java: 'Jv', Python: 'Py' }

// 支持多选的板块
const selectedPlates = ref<string[]>(lab.plates?.length ? [...lab.plates] : [])
const difficulty = ref(lab.difficulty || '中等')
const targetLanguage = ref(lab.targetLanguage || 'C++')
const genLoading = ref(lab.isGenerating || false)
const genErr = ref(lab.generationError || '')
const generatedProblem = ref<ProblemGenerateResponse | null>(lab.generatedProblem)

const code = ref(lab.analyzeCode)
const lang = ref(lab.analyzeLang)
const anaLoading = ref(false)
const anaErr = ref('')
const anaOut = ref<{ plate?: string; complexity?: string; style?: string } | null>(null)

const saving = ref(false)
const saveErr = ref('')

/**
 * 处理 Tab 键缩进
 */
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

/**
 * 处理 Enter 键自动缩进
 */
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



// 组件挂载时加载分类列表并检查是否有正在进行的生成任务
onMounted(async () => {
  // 加载分类列表
  await loadCategories()
  
  if (lab.isGenerating && lab.generationPromise) {
    genLoading.value = true
    try {
      const data = await lab.generationPromise
      generatedProblem.value = data
      lab.generatedProblem = data
      lab.plates = [...selectedPlates.value]
      lab.difficulty = difficulty.value
      lab.targetLanguage = targetLanguage.value
    } catch (e: unknown) {
      genErr.value = e instanceof Error ? e.message : '生成失败'
      lab.generationError = genErr.value
    } finally {
      genLoading.value = false
      lab.isGenerating = false
      lab.generationPromise = null
    }
  }
})

async function loadCategories() {
  platesLoading.value = true
  try {
    const res = await fetchCategories()
    if (res.code === 200) {
      categories.value = res.data ?? []
    }
  } catch (e) {
    console.error('加载分类失败:', e)
  } finally {
    platesLoading.value = false
  }
}

function togglePlate(plateName: string) {
  const index = selectedPlates.value.indexOf(plateName)
  if (index > -1) {
    // 如果已选中，则取消选中
    selectedPlates.value.splice(index, 1)
  } else {
    // 如果未选中，则添加
    selectedPlates.value.push(plateName)
  }
}

async function onGenerate() {
  if (!selectedPlates.value.length) {
    genErr.value = '请至少选择一个算法板块'
    return
  }
  
  genLoading.value = true
  genErr.value = ''
  generatedProblem.value = null
  lab.generatedProblem = null
  lab.generationError = ''
  
  // 创建生成任务，固定使用 C++ 语言
  const generatePromise = generateProblem({
    plates: selectedPlates.value,
    difficulty: difficulty.value,
    targetLanguage: 'C++', // 固定使用 C++
  })
  
  // 保存任务到 store
  lab.generationPromise = generatePromise
  lab.isGenerating = true
  
  try {
    const data = await generatePromise
    generatedProblem.value = data
    lab.generatedProblem = data
    lab.plates = [...selectedPlates.value]
    lab.difficulty = difficulty.value
    lab.targetLanguage = 'C++'
  } catch (e: unknown) {
    genErr.value = e instanceof Error ? e.message : '生成失败'
    lab.generationError = genErr.value
  } finally {
    genLoading.value = false
    lab.isGenerating = false
    lab.generationPromise = null
  }
}

async function resolveProblemId(title: string): Promise<number | null> {
  const res = await fetchProblemList({ page: 1, size: 10, keyword: title })
  if (res.code !== 200 || !res.data?.list?.length) return null
  const exact = res.data.list.find((p) => p.title === title)
  return exact?.problemId ?? res.data.list[0]?.problemId ?? null
}

async function onSaveAndStart() {
  // 防止重复点击
  if (saving.value) {
    console.warn('[AI Lab] 正在保存中，忽略重复点击')
    return
  }
  if (!generatedProblem.value) {
    console.warn('[AI Lab] 没有可保存的题目')
    return
  }
  
  // 检查用户是否登录
  if (!auth.isLoggedIn) {
    saveErr.value = '请先登录后再保存题目'
    return
  }

  console.log('[AI Lab] 开始入库题目:', generatedProblem.value.problemName)
  saving.value = true
  saveErr.value = ''

  try {
    const title = generatedProblem.value.problemName
    const addRes = await adminAddProblem({
      title,
      difficulty: difficultyToApi(difficulty.value),
      categoryNames: selectedPlates.value.length > 0 ? selectedPlates.value : ['其他'],
      description: generatedProblem.value.problemDesc,
      inputFormat: generatedProblem.value.inputFormat || '',
      outputFormat: generatedProblem.value.outputFormat || '',
      sampleInput: generatedProblem.value.sampleInput?.[0] || '',
      sampleOutput: generatedProblem.value.sampleOutput?.[0] || '',
      timeLimit: 1000,
      memoryLimit: 256000,
    })

    console.log('[AI Lab] 题目入库结果:', addRes)
    
    if (addRes.code !== 200) {
      saveErr.value = addRes.message || '保存失败'
      return
    }

    const problemId = await resolveProblemId(title)
    console.log('[AI Lab] 获取到的题目ID:', problemId)
    
    if (!problemId) {
      saveErr.value = '题目已入库，请从题库列表进入'
      await router.push({ name: 'problems' })
      return
    }

    const samples = generatedProblem.value.sampleInput
    const outputs = generatedProblem.value.sampleOutput
    if (samples?.length && outputs?.length) {
      const len = Math.min(samples.length, outputs.length)
      console.log(`[AI Lab] 添加 ${len} 个测试用例`)
      for (let i = 0; i < len; i++) {
        const inputContent = samples[i]
        const outputContent = outputs[i]
        if (!inputContent || !outputContent) continue
        const inputFile = new File([inputContent], `sample_${i}.in`, { type: 'text/plain' })
        const outputFile = new File([outputContent], `sample_${i}.out`, { type: 'text/plain' })
        await adminAddTestCase(problemId, inputFile, outputFile)
      }
    }

    // 清空已生成的题目，防止重复入库
    console.log('[AI Lab] 清空题目数据，准备跳转')
    generatedProblem.value = null
    lab.generatedProblem = null
    
    await router.push({ name: 'problem-detail', params: { id: String(problemId) } })
  } catch (e: unknown) {
    console.error('[AI Lab] 保存失败:', e)
    saveErr.value = e instanceof Error ? e.message : '保存失败'
  } finally {
    saving.value = false
    console.log('[AI Lab] 保存流程结束')
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
        <p class="chip-label">算法板块（可多选）</p>
        <div v-if="platesLoading" class="muted">加载中...</div>
        <div v-else class="chips">
          <button
              v-for="cat in categories"
              :key="cat.categoryId"
              type="button"
              class="chip"
              :class="{ active: selectedPlates.includes(cat.categoryName) }"
              :disabled="genLoading"
              @click="togglePlate(cat.categoryName)"
          >
            <span class="chip-ico">{{ plateIcons[cat.categoryName] || '📌' }}</span>
            {{ cat.categoryName }}
          </button>
        </div>
      </div>

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
            <span v-for="p in selectedPlates" :key="p" class="tag tag-plate">{{ plateIcons[p] || '📌' }} {{ p }}</span>
            <span class="tag" :class="`tag-${difficulty}`">{{ difficulty }}</span>
            <span class="tag tag-lang">C++</span>
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
          <!-- 修复点：增加 generatedProblem.sampleInput 存在性检查 -->
          <div v-if="generatedProblem.sampleInput && generatedProblem.sampleInput.length" class="content-section">
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
          <button type="button" class="btn-action btn-save" :disabled="saving || !generatedProblem" @click.once="onSaveAndStart">
            <svg v-if="!saving" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path fill="currentColor" d="M17 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z" />
            </svg>
            <span v-else class="btn-spinner dark" aria-hidden="true" />
            {{ saving ? '保存中…' : '入库并开始做题' }}
          </button>
          <p v-if="!auth.isLoggedIn" class="admin-hint">请先登录后再保存题目</p>
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
      </div>

      <button type="button" class="btn-action btn-ana" :disabled="anaLoading" @click="onAnalyze">
        <svg v-if="!anaLoading" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
          <path fill="currentColor" d="M15.5 14h-.79l-.28-.27A6.47 6.47 0 0 0 16 9.5 6.5 6.5 0 1 0 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z" />
        </svg>
        <span v-else class="btn-spinner" aria-hidden="true" />
        {{ anaLoading ? '分析中…' : '开始分析' }}
      </button>

      <div class="editor-wrap">
        <div class="editor-bar">
          <span class="dot red" /><span class="dot yellow" /><span class="dot green" />
          <span class="editor-fname">solution.{{ lang === 'Python' ? 'py' : lang === 'Java' ? 'java' : 'cpp' }}</span>
        </div>
        <textarea v-model="code" class="editor" spellcheck="false" placeholder="// 在此粘贴你的代码…" @keydown="handleCodeEditorKeydown" />
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.hero {
  background: linear-gradient(135deg, rgba(255, 161, 22, 0.1), rgba(255, 107, 74, 0.05));
  border: 1px solid var(--lc-border);
  border-radius: 16px;
  padding: 48px;
  margin-bottom: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.hero-text {
  flex: 1;
}

.hero-kicker {
  color: var(--lc-accent);
  font-size: 0.85rem;
  font-weight: 600;
  margin-bottom: 8px;
}

.spark {
  color: var(--lc-accent);
}

.hero-title {
  font-size: 2.5rem;
  font-weight: 700;
  margin: 0 0 12px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  color: var(--lc-text-muted);
  font-size: 1rem;
  margin: 0;
}

.hero-badges {
  display: flex;
  gap: 12px;
}

.hero-badge {
  padding: 8px 16px;
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

.panel {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
}

.panel-head {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 24px;
}

.panel-icon {
  padding: 12px;
  background: rgba(255, 161, 22, 0.1);
  border-radius: 12px;
  color: var(--lc-accent);
}

.ana-icon {
  background: rgba(0, 184, 163, 0.1);
  color: var(--lc-easy);
}

.panel-head h2 {
  margin: 0 0 4px;
  font-size: 1.5rem;
  font-weight: 700;
}

.panel-sub {
  margin: 0;
  color: var(--lc-text-muted);
  font-size: 0.9rem;
}

.chip-section {
  margin-bottom: 20px;
}

.chip-label {
  margin: 0 0 12px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--lc-text-muted);
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chips-sm {
  gap: 6px;
}

.chip {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.chip:hover:not(:disabled) {
  border-color: var(--lc-accent-dim);
  color: var(--lc-text);
}

.chip.active {
  border-color: var(--lc-accent);
  background: rgba(255, 161, 22, 0.1);
  color: var(--lc-accent);
  font-weight: 600;
}

.chip:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.chip-ico {
  font-size: 1.1rem;
}

.mono {
  font-family: var(--font-mono);
  font-size: 0.8rem;
}

.chip-diff.diff-简单.active {
  border-color: var(--lc-easy);
  background: rgba(0, 184, 163, 0.1);
  color: var(--lc-easy);
}

.chip-diff.diff-中等.active {
  border-color: var(--lc-medium);
  background: rgba(255, 192, 30, 0.1);
  color: var(--lc-medium);
}

.chip-diff.diff-困难.active {
  border-color: var(--lc-hard);
  background: rgba(255, 55, 95, 0.1);
  color: var(--lc-hard);
}

.row-2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.btn-action {
  padding: 12px 24px;
  border-radius: 10px;
  border: none;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  justify-content: center;
}

.btn-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-gen {
  width: 100%;
  margin-top: 8px;
  background: linear-gradient(135deg, var(--lc-accent), #ff6b4a);
  color: #111;
}

.btn-gen:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 161, 22, 0.3);
}

.btn-ana {
  width: 100%;
  margin-top: 16px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, var(--lc-easy), #00b8a3);
  color: #fff;
}

.btn-ana:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 184, 163, 0.3);
}

.btn-save {
  width: 100%;
  margin-top: 16px;
  background: linear-gradient(135deg, var(--lc-medium), #ffc01e);
  color: #111;
}

.btn-save:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 192, 30, 0.3);
}

.btn-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.msg-err {
  color: var(--lc-red);
  font-size: 0.9rem;
  margin: 12px 0;
}

.admin-hint {
  color: var(--lc-text-muted);
  font-size: 0.85rem;
  margin-top: 8px;
}

.problem-preview {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--lc-border);
}

.problem-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.problem-title {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 700;
}

.problem-tags {
  display: flex;
  gap: 8px;
}

.tag {
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 0.8rem;
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
}

.tag-plate {
  background: rgba(255, 161, 22, 0.1);
  color: var(--lc-accent);
}

.tag-简单 {
  background: rgba(0, 184, 163, 0.1);
  color: var(--lc-easy);
}

.tag-中等 {
  background: rgba(255, 192, 30, 0.1);
  color: var(--lc-medium);
}

.tag-困难 {
  background: rgba(255, 55, 95, 0.1);
  color: var(--lc-hard);
}

.tag-lang {
  background: rgba(100, 100, 255, 0.1);
  color: #6464ff;
}

.problem-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.content-section h4 {
  margin: 0 0 8px;
  font-size: 1rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.sec-ico {
  font-size: 1.2rem;
}

.content-text {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--lc-text);
}

.sample-box {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.sample-item {
  background: var(--lc-bg);
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  padding: 12px;
}

.sample-label {
  font-size: 0.8rem;
  color: var(--lc-text-muted);
  margin-bottom: 8px;
  font-weight: 600;
}

.sample-code {
  margin: 0;
  padding: 8px;
  background: #0d0d0d;
  border-radius: 6px;
  font-size: 0.8rem;
  overflow: auto;
}

.problem-actions {
  margin-top: 24px;
}

.editor-wrap {
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--lc-border);
}

.editor-bar {
  padding: 8px 12px;
  background: var(--lc-surface-2);
  display: flex;
  align-items: center;
  gap: 8px;
}

.dot {
  width: 12px;
  height: 12px;
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
  font-size: 0.85rem;
  color: var(--lc-text-muted);
  font-family: var(--font-mono);
}

.editor {
  width: 100%;
  min-height: 300px;
  padding: 16px;
  font-family: var(--font-mono);
  font-size: 0.9rem;
  line-height: 1.6;
  background: #0d0d0d;
  color: var(--lc-text);
  border: none;
  resize: vertical;
}

.editor:focus {
  outline: none;
}

.ana-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 24px;
}

.ana-card {
  background: var(--lc-bg);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 20px;
}

.ana-wide {
  grid-column: 1 / -1;
}

.ana-card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.ana-ico {
  font-size: 1.5rem;
}

.ana-card h3 {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
}

.ana-card p {
  margin: 0;
  color: var(--lc-text);
  font-size: 0.9rem;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .row-2 {
    grid-template-columns: 1fr;
  }
  
  .sample-box {
    grid-template-columns: 1fr;
  }
  
  .ana-grid {
    grid-template-columns: 1fr;
  }
  
  .hero {
    flex-direction: column;
    gap: 24px;
  }
}
</style>
