<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { analyzeCode, generateProblem } from '@/api/algorithm'
import { adminAddProblem, adminAddTestCase } from '@/api/admin'
import type { ProblemGenerateResponse } from '@/types/api'

const router = useRouter()

const plates = ['数组', '链表', '动态规划', '贪心', '字符串', '图论', '数学']
const diffs = ['简单', '中等', '困难']
const langs = ['C++', 'Java', 'Python']

const plate = ref('数组')
const difficulty = ref('中等')
const targetLanguage = ref('C++')
const genLoading = ref(false)
const genErr = ref('')
const generatedProblem = ref<ProblemGenerateResponse | null>(null)

const code = ref(`#include <iostream>
using namespace std;
int main() { return 0; }
`)
const lang = ref('C++')
const anaLoading = ref(false)
const anaErr = ref('')
const anaOut = ref<{ plate?: string; complexity?: string; style?: string } | null>(null)

const saving = ref(false)
const saveErr = ref('')

async function onGenerate() {
  genLoading.value = true
  genErr.value = ''
  generatedProblem.value = null
  try {
    const data = await generateProblem({
      plate: plate.value,
      difficulty: difficulty.value,
      targetLanguage: targetLanguage.value,
    })
    generatedProblem.value = data
  } catch (e: unknown) {
    genErr.value = e instanceof Error ? e.message : '生成失败'
  } finally {
    genLoading.value = false
  }
}

async function onSaveAndStart() {
  if (!generatedProblem.value) return

  saving.value = true
  saveErr.value = ''

  try {
    // 1. 创建题目
    const categoryMap: Record<string, string[]> = {
      '数组': ['数组'],
      '链表': ['链表'],
      '动态规划': ['动态规划'],
      '贪心': ['贪心'],
      '字符串': ['字符串'],
      '图论': ['图论'],
      '数学': ['数学'],
    }

    await adminAddProblem({
      title: generatedProblem.value.problemName,
      difficulty: difficulty.value,
      categoryNames: categoryMap[plate.value] || [plate.value],
      description: generatedProblem.value.problemDesc,
      inputFormat: generatedProblem.value.inputFormat || '',
      outputFormat: generatedProblem.value.outputFormat || '',
      sampleInput: generatedProblem.value.sampleInput?.[0] || '',
      sampleOutput: generatedProblem.value.sampleOutput?.[0] || '',
      timeLimit: 1000,
      memoryLimit: 256000,
    })

    // 2. 获取刚创建的题目ID（需要重新查询）
    // 这里简化处理，假设题目已创建成功
    // 实际应该从后端返回的 problemId 获取

    // 3. 添加测试用例（如果有样例输入输出）
    if (generatedProblem.value.sampleInput && generatedProblem.value.sampleOutput) {
      for (let i = 0; i < generatedProblem.value.sampleInput.length; i++) {
        const inputContent = generatedProblem.value.sampleInput[i]
        const outputContent = generatedProblem.value.sampleOutput[i]

        if (inputContent && outputContent) {
          // 创建 File 对象
          const inputFile = new File([inputContent], `input_${i}.in`, { type: 'text/plain' })
          const outputFile = new File([outputContent], `output_${i}.out`, { type: 'text/plain' })

          // 注意：这里需要知道刚创建的 problemId
          // 由于接口限制，暂时跳过测试用例添加
          // 可以在管理后台手动添加测试用例
        }
      }
    }

    // 4. 跳转到题目列表页（最新创建的题目会在最前面）
    await router.push({ name: 'problems' })

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

<template>
  <div class="page">
    <h1>AI 实验室</h1>

    <!-- 生成练习题区域 -->
    <section class="card">
      <h2>🎯 生成练习题</h2>
      <div class="form-row">
        <div class="form-group">
          <label>算法板块</label>
          <select v-model="plate">
            <option v-for="p in plates" :key="p" :value="p">{{ p }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>难度</label>
          <select v-model="difficulty">
            <option v-for="d in diffs" :key="d" :value="d">{{ d }}</option>
          </select>
        </div>
        <div class="form-group">
          <label>目标语言</label>
          <select v-model="targetLanguage">
            <option v-for="l in langs" :key="l" :value="l">{{ l }}</option>
          </select>
        </div>
        <button type="button" class="btn btn-primary" :disabled="genLoading" @click="onGenerate">
          {{ genLoading ? '生成中…' : '✨ 生成题目' }}
        </button>
      </div>

      <p v-if="genErr" class="err">{{ genErr }}</p>

      <!-- 美观展示生成的题目 -->
      <div v-if="generatedProblem" class="problem-preview">
        <div class="problem-header">
          <h3 class="problem-title">{{ generatedProblem.problemName }}</h3>
          <div class="problem-tags">
            <span class="tag tag-plate">{{ plate }}</span>
            <span class="tag" :class="`tag-${difficulty}`">{{ difficulty }}</span>
          </div>
        </div>

        <div class="problem-content">
          <div class="content-section">
            <h4>📝 题目描述</h4>
            <p class="pre content-text">{{ generatedProblem.problemDesc }}</p>
          </div>

          <div v-if="generatedProblem.inputFormat" class="content-section">
            <h4>📥 输入格式</h4>
            <p class="pre content-text">{{ generatedProblem.inputFormat }}</p>
          </div>

          <div v-if="generatedProblem.outputFormat" class="content-section">
            <h4>📤 输出格式</h4>
            <p class="pre content-text">{{ generatedProblem.outputFormat }}</p>
          </div>

          <div v-if="generatedProblem.sampleInput?.length" class="content-section">
            <h4>💡 样例</h4>
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
          <p v-if="saveErr" class="err">{{ saveErr }}</p>
          <button
              type="button"
              class="btn btn-success"
              :disabled="saving"
              @click="onSaveAndStart"
          >
            {{ saving ? '保存中…' : '🚀 入库开写' }}
          </button>
          <span class="hint">点击后将题目保存到题库，并跳转到题目页面开始编程</span>
        </div>
      </div>
    </section>

    <!-- 代码分析区域 -->
    <section class="card">
      <h2>🔍 代码分析</h2>
      <div class="form-row">
        <div class="form-group">
          <label>语言</label>
          <select v-model="lang">
            <option v-for="l in langs" :key="l" :value="l">{{ l }}</option>
          </select>
        </div>
        <button type="button" class="btn btn-primary" :disabled="anaLoading" @click="onAnalyze">
          {{ anaLoading ? '分析中…' : '🔬 开始分析' }}
        </button>
      </div>
      <textarea v-model="code" class="editor" spellcheck="false" placeholder="粘贴你的代码..." />
      <p v-if="anaErr" class="err">{{ anaErr }}</p>
      <div v-if="anaOut" class="ana">
        <div class="ana-card">
          <h3>📊 算法板块</h3>
          <p>{{ anaOut.plate }}</p>
        </div>
        <div class="ana-card">
          <h3>⏱️ 复杂度分析</h3>
          <p class="pre">{{ anaOut.complexity }}</p>
        </div>
        <div class="ana-card">
          <h3>✨ 代码风格</h3>
          <p class="pre">{{ anaOut.style }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page {
  max-width: 1000px;
}

h1 {
  margin: 0 0 20px;
  font-size: 1.8rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

h2 {
  margin: 0 0 16px;
  font-size: 1.2rem;
  color: var(--lc-text);
}

h3 {
  margin: 0;
  font-size: 1rem;
}

h4 {
  margin: 0 0 8px;
  font-size: 0.9rem;
  color: var(--lc-text-muted);
  font-weight: 600;
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.form-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 0.8rem;
  color: var(--lc-text-muted);
  font-weight: 500;
}

select {
  padding: 8px 12px;
  border: 1px solid var(--lc-border);
  border-radius: 6px;
  background: var(--lc-surface-2);
  color: var(--lc-text);
  font-size: 0.9rem;
  min-width: 120px;
}

.btn {
  padding: 10px 20px;
  border-radius: 8px;
  border: none;
  font-weight: 600;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: wait;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.btn-success {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: #fff;
  font-size: 1rem;
  padding: 12px 28px;
}

.btn-success:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(17, 153, 142, 0.4);
}

.editor {
  width: 100%;
  min-height: 160px;
  padding: 14px;
  font-family: var(--font-mono);
  font-size: 0.85rem;
  background: #0d0d0d;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
  margin-top: 12px;
  resize: vertical;
}

/* 题目预览样式 */
.problem-preview {
  margin-top: 20px;
  border: 2px solid var(--lc-border);
  border-radius: 10px;
  overflow: hidden;
}

.problem-header {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
  padding: 16px 20px;
  border-bottom: 1px solid var(--lc-border);
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.problem-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--lc-text);
}

.problem-tags {
  display: flex;
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
  background: #667eea20;
  color: #667eea;
}

.tag-简单 {
  background: #00b8a320;
  color: #00b8a3;
}

.tag-中等 {
  background: #ffc01e20;
  color: #ffc01e;
}

.tag-困难 {
  background: #ff375f20;
  color: #ff375f;
}

.problem-content {
  padding: 20px;
}

.content-section {
  margin-bottom: 20px;
}

.content-section:last-child {
  margin-bottom: 0;
}

.content-text {
  margin: 0;
  line-height: 1.7;
  font-size: 0.95rem;
  color: var(--lc-text);
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
  background: #0d0d0d;
  border: 1px solid var(--lc-border);
  border-radius: 8px;
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
  padding: 16px 20px;
  background: var(--lc-surface-2);
  border-top: 1px solid var(--lc-border);
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.hint {
  font-size: 0.8rem;
  color: var(--lc-text-muted);
}

/* 代码分析结果样式 */
.ana {
  margin-top: 16px;
  display: grid;
  gap: 12px;
}

.ana-card {
  padding: 16px;
  background: var(--lc-surface-2);
  border: 1px solid var(--lc-border);
  border-radius: 8px;
}

.ana-card h3 {
  margin-bottom: 8px;
  font-size: 0.9rem;
}

.ana-card p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.6;
  color: var(--lc-text);
}

.pre {
  white-space: pre-wrap;
  word-break: break-word;
}

.err {
  color: var(--lc-red);
  font-size: 0.9rem;
  margin: 8px 0;
}
</style>
