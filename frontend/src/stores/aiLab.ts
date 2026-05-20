import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ProblemGenerateResponse } from '@/types/api'

export const useAiLabStore = defineStore('aiLab', () => {
  const plates = ref<string[]>([])
  const difficulty = ref('中等')
  const targetLanguage = ref('C++')
  const generatedProblem = ref<ProblemGenerateResponse | null>(null)
  
  // 添加生成任务状态
  const isGenerating = ref(false)
  const generationError = ref('')
  const generationPromise = ref<Promise<ProblemGenerateResponse> | null>(null)

  const analyzeLang = ref('C++')
  const analyzeCode = ref(`#include <iostream>
using namespace std;
int main() { return 0; }
`)

  return {
    plates,
    difficulty,
    targetLanguage,
    generatedProblem,
    isGenerating,
    generationError,
    generationPromise,
    analyzeLang,
    analyzeCode,
  }
})
