import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginVO } from '@/types/api'
import { useProblemListStore } from '@/stores/problemList'
import { useSubmissionsListStore } from '@/stores/submissionsList'
import { debugLog } from '@/utils/debugLog'

const TOKEN_KEY = 'oj_token'
const USER_KEY = 'oj_user_preview'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem(TOKEN_KEY))
  const userPreview = ref<Partial<LoginVO> | null>(null)

  try {
    const raw = localStorage.getItem(USER_KEY)
    if (raw) userPreview.value = JSON.parse(raw) as Partial<LoginVO>
  } catch {
    /* ignore */
  }

  const isLoggedIn = computed(() => !!token.value)

  const isAdmin = computed(() => userPreview.value?.role === 'admin')

  function setSession(payload: LoginVO) {
    token.value = payload.token
    localStorage.setItem(TOKEN_KEY, payload.token)
    const preview = {
      userId: payload.userId,
      nickname: payload.nickname,
      avatar: payload.avatar,
      role: payload.role,
    }
    userPreview.value = preview
    localStorage.setItem(USER_KEY, JSON.stringify(preview))
  }

  function clear() {
    token.value = null
    userPreview.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
    useProblemListStore().reset()
    useSubmissionsListStore().reset()
    // #region agent log
    debugLog('auth.ts:clear', 'session cleared, stores reset', {}, 'H1')
    // #endregion
  }

  return { token, userPreview, isLoggedIn, isAdmin, setSession, clear }
})
