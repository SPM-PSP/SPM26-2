import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { AdminProblemDetail, AdminTestCaseItem, SolutionItem } from '@/types/api'

export const useProblemEditStore = defineStore('problemEdit', () => {
  // 基本信息
  const title = ref('')
  const difficulty = ref('easy')
  const description = ref('')
  const inputFormat = ref('')
  const outputFormat = ref('')
  const sampleInput = ref('')
  const sampleOutput = ref('')
  const timeLimit = ref(1000)
  const memoryLimit = ref(65536)
  const selectedCats = ref<string[]>([])
  
  // 测试用例和题解
  const testCases = ref<AdminTestCaseItem[]>([])
  const solutions = ref<SolutionItem[]>([])
  
  // 表单状态
  const newCaseInput = ref<File | null>(null)
  const newCaseOutput = ref<File | null>(null)
  const replaceCaseId = ref<number | null>(null)
  const repInput = ref<File | null>(null)
  const repOutput = ref<File | null>(null)
  
  const solForm = ref({ title: '', content: '', language: 'C++', code: '' })
  const editSol = ref<SolutionItem | null>(null)
  
  // 页面状态
  const isDirty = ref(false) // 是否有未保存的更改
  
  // 记住上次浏览的题目 ID
  const lastProblemId = ref<number | null>(null)
  
  // 保存各题目的代码编辑状态（按题目 ID 存储）
  const codeCache = ref<Record<number, { code: string; language: string }>>({})
  
  // 重置所有状态
  function reset() {
    title.value = ''
    difficulty.value = 'easy'
    description.value = ''
    inputFormat.value = ''
    outputFormat.value = ''
    sampleInput.value = ''
    sampleOutput.value = ''
    timeLimit.value = 1000
    memoryLimit.value = 65536
    selectedCats.value = []
    testCases.value = []
    solutions.value = []
    newCaseInput.value = null
    newCaseOutput.value = null
    replaceCaseId.value = null
    repInput.value = null
    repOutput.value = null
    solForm.value = { title: '', content: '', language: 'C++', code: '' }
    editSol.value = null
    isDirty.value = false
    // 不清理 codeCache，保留所有题目的代码
  }
  
  // 从详情数据加载
  function loadFromDetail(d: AdminProblemDetail) {
    title.value = d.title
    difficulty.value = d.difficulty
    description.value = d.description
    inputFormat.value = d.inputFormat
    outputFormat.value = d.outputFormat
    sampleInput.value = d.sampleInput
    sampleOutput.value = d.sampleOutput
    timeLimit.value = d.timeLimit
    memoryLimit.value = d.memoryLimit
    selectedCats.value = [...(d.categoryNames ?? [])]
    testCases.value = d.testCases ?? []
    isDirty.value = false
  }
  
  return {
    title,
    difficulty,
    description,
    inputFormat,
    outputFormat,
    sampleInput,
    sampleOutput,
    timeLimit,
    memoryLimit,
    selectedCats,
    testCases,
    solutions,
    newCaseInput,
    newCaseOutput,
    replaceCaseId,
    repInput,
    repOutput,
    solForm,
    editSol,
    isDirty,
    lastProblemId,
    codeCache,
    reset,
    loadFromDetail,
  }
})
