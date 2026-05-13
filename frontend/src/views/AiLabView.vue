<script setup lang="ts">
import { ref } from 'vue'
import { analyzeCode, generateProblem } from '@/api/algorithm'

const plates = ['数组', '链表', '动态规划', '贪心', '字符串', '图论', '数学']
const diffs = ['简单', '中等', '困难']
const langs = ['C++', 'Java', 'Python']

const plate = ref('数组')
const difficulty = ref('中等')
const targetLanguage = ref('C++')
const genLoading = ref(false)
const genErr = ref('')
const genOut = ref('')

const code = ref(`#include <iostream>
using namespace std;
int main() { return 0; }
`)
const lang = ref('C++')
const anaLoading = ref(false)
const anaErr = ref('')
const anaOut = ref<{ plate?: string; complexity?: string; style?: string } | null>(null)

async function onGenerate() {
  genLoading.value = true
  genErr.value = ''
  genOut.value = ''
  try {
    const data = await generateProblem({
      plate: plate.value,
      difficulty: difficulty.value,
      targetLanguage: targetLanguage.value,
    })
    const parts: string[] = []
    parts.push(`## ${data.problemName}\n\n${data.problemDesc}`)
    if (data.inputFormat) parts.push(`### 输入格式\n${data.inputFormat}`)
    if (data.outputFormat) parts.push(`### 输出格式\n${data.outputFormat}`)
    if (data.sampleInput?.length) parts.push(`### 样例输入\n${data.sampleInput.join('\n---\n')}`)
    if (data.sampleOutput?.length) parts.push(`### 样例输出\n${data.sampleOutput.join('\n---\n')}`)
    genOut.value = parts.join('\n\n')
  } catch (e: unknown) {
    genErr.value = e instanceof Error ? e.message : '生成失败'
  } finally {
    genLoading.value = false
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
    <section class="card">
      <h2>生成练习题</h2>
      <div class="row">
        <select v-model="plate">
          <option v-for="p in plates" :key="p" :value="p">{{ p }}</option>
        </select>
        <select v-model="difficulty">
          <option v-for="d in diffs" :key="d" :value="d">{{ d }}</option>
        </select>
        <select v-model="targetLanguage">
          <option v-for="l in langs" :key="l" :value="l">{{ l }}</option>
        </select>
        <button type="button" class="btn" :disabled="genLoading" @click="onGenerate">
          {{ genLoading ? '生成中…' : '生成' }}
        </button>
      </div>
      <p v-if="genErr" class="err">{{ genErr }}</p>
      <pre v-if="genOut" class="out">{{ genOut }}</pre>
    </section>
    <section class="card">
      <h2>代码分析</h2>
      <div class="row">
        <select v-model="lang">
          <option v-for="l in langs" :key="l" :value="l">{{ l }}</option>
        </select>
        <button type="button" class="btn" :disabled="anaLoading" @click="onAnalyze">
          {{ anaLoading ? '分析中…' : '分析' }}
        </button>
      </div>
      <textarea v-model="code" class="editor" spellcheck="false" />
      <p v-if="anaErr" class="err">{{ anaErr }}</p>
      <div v-if="anaOut" class="ana">
        <h3>算法板块</h3>
        <p>{{ anaOut.plate }}</p>
        <h3>复杂度</h3>
        <p class="pre">{{ anaOut.complexity }}</p>
        <h3>代码风格</h3>
        <p class="pre">{{ anaOut.style }}</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page {
  max-width: 900px;
}
h1 {
  margin: 0 0 16px;
}
h2 {
  margin: 0 0 12px;
  font-size: 1.05rem;
}
h3 {
  margin: 12px 0 6px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}
.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 18px;
  margin-bottom: 16px;
}
.row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}
select {
  padding: 6px 10px;
}
.btn {
  padding: 8px 18px;
  border-radius: 8px;
  border: none;
  background: var(--lc-accent);
  color: #111;
  font-weight: 600;
  cursor: pointer;
}
.btn:disabled {
  opacity: 0.6;
  cursor: wait;
}
.editor {
  width: 100%;
  min-height: 140px;
  padding: 12px;
  font-family: var(--font-mono);
  font-size: 0.8rem;
  background: #0d0d0d;
  border-radius: 8px;
  margin-bottom: 10px;
}
.out {
  margin: 12px 0 0;
  padding: 14px;
  background: #0d0d0d;
  border-radius: 8px;
  font-size: 0.85rem;
  white-space: pre-wrap;
  max-height: 360px;
  overflow: auto;
}
.ana p {
  margin: 0;
  font-size: 0.9rem;
  line-height: 1.55;
}
.pre {
  white-space: pre-wrap;
}
.err {
  color: var(--lc-red);
  font-size: 0.88rem;
}
</style>
