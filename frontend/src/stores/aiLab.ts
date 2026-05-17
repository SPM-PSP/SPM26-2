import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { ProblemGenerateResponse } from '@/types/api'

export const useAiLabStore = defineStore('aiLab', () => {
  const plate = ref('数组')
  const difficulty = ref('中等')
  const targetLanguage = ref('C++')
  const generatedProblem = ref<ProblemGenerateResponse | null>(null)

  const analyzeLang = ref('C++')
  const analyzeCode = ref(`#include <iostream>
using namespace std;
int main() { return 0; }
`)

  return {
    plate,
    difficulty,
    targetLanguage,
    generatedProblem,
    analyzeLang,
    analyzeCode,
  }
})
