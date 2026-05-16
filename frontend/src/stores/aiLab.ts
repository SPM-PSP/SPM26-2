import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ProblemDetail } from '@/types/api'

/** AI 实验室表单与生成结果，切换路由时由 keep-alive + store 共同保留 */
export const useAiLabStore = defineStore('aiLab', () => {
  const activeTab = ref<'generate' | 'analyze'>('generate')
  const difficulty = ref('中等')
  const selectedCategories = ref<string[]>([])
  const lastGenerated = ref<ProblemDetail | null>(null)
  const genMarkdown = ref('')

  const analyzeLang = ref('C++')
  const analyzeCode = ref(`#include <iostream>
using namespace std;
int main() { return 0; }
`)

  function setGenerated(problem: ProblemDetail, preview: string) {
    lastGenerated.value = problem
    genMarkdown.value = preview
  }

  function resetGenerate() {
    lastGenerated.value = null
    genMarkdown.value = ''
  }

  return {
    activeTab,
    difficulty,
    selectedCategories,
    lastGenerated,
    genMarkdown,
    analyzeLang,
    analyzeCode,
    setGenerated,
    resetGenerate,
  }
})
