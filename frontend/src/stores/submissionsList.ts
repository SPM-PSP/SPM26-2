import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useSubmissionsListStore = defineStore('submissionsList', () => {
  const page = ref(1)
  const resultFilter = ref<number | ''>('')
  const listLoaded = ref(false)

  function reset() {
    page.value = 1
    resultFilter.value = ''
    listLoaded.value = false
  }

  return { page, resultFilter, listLoaded, reset }
})
