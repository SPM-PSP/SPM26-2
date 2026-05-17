import { defineStore } from 'pinia'
import { ref } from 'vue'

/** 题库列表筛选与分页，路由切换时保留 */
export const useProblemListStore = defineStore('problemList', () => {
  const keyword = ref('')
  const difficulty = ref('')
  const status = ref<number | ''>('')
  const selectedCats = ref<string[]>([])
  const currentPage = ref(1)
  const listLoaded = ref(false)

  function reset() {
    keyword.value = ''
    difficulty.value = ''
    status.value = ''
    selectedCats.value = []
    currentPage.value = 1
    listLoaded.value = false
  }

  return {
    keyword,
    difficulty,
    status,
    selectedCats,
    currentPage,
    listLoaded,
    reset,
  }
})
