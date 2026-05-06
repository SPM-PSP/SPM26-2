<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { fetchSubmissionDetail } from '@/api/user'
import type { SubmissionDetail } from '@/types/api'
import { verdictClass, verdictText } from '@/utils/format'

const props = defineProps<{ id: string }>()
const router = useRouter()
const detail = ref<SubmissionDetail | null>(null)
const loading = ref(true)
const err = ref('')

async function load() {
  loading.value = true
  err.value = ''
  try {
    const res = await fetchSubmissionDetail(Number(props.id))
    if (res.code !== 200) {
      err.value = res.message || '加载失败'
      detail.value = null
      return
    }
    detail.value = res.data
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.id,
  () => {
    void load()
  },
)

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <button type="button" class="back" @click="router.push('/submissions')">← 返回列表</button>
    <div v-if="loading" class="muted">加载中…</div>
    <div v-else-if="err || !detail" class="err">{{ err || '无数据' }}</div>
    <div v-else class="card">
      <div class="head">
        <h1>{{ detail.problemTitle }}</h1>
        <span class="verdict" :class="verdictClass(detail.result)">{{ verdictText(detail.result) }}</span>
      </div>
      <p class="meta">
        提交 #{{ detail.submissionId }} · {{ detail.language }} · {{ detail.submitTime }} · 通过
        {{ detail.passCount }}/{{ detail.totalCount }} · 用时 {{ detail.runTime }} ms · 内存 {{ detail.memory }} KB
      </p>
      <p v-if="detail.errorMsg" class="err-msg">{{ detail.errorMsg }}</p>
      <h3 class="h">代码</h3>
      <pre class="code">{{ detail.code }}</pre>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 900px;
}

.back {
  margin-bottom: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text-muted);
  cursor: pointer;
}

.back:hover {
  color: var(--lc-text);
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 20px;
}

.head {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

h1 {
  margin: 0;
  font-size: 1.2rem;
}

.verdict {
  font-weight: 700;
  font-size: 0.9rem;
}

.verdict-ac {
  color: var(--lc-green);
}

.verdict-wa {
  color: var(--lc-red);
}

.meta {
  font-size: 0.85rem;
  color: var(--lc-text-muted);
  margin: 10px 0;
}

.err-msg {
  color: var(--lc-red);
  font-size: 0.88rem;
}

.h {
  margin: 16px 0 8px;
  font-size: 0.85rem;
  color: var(--lc-text-muted);
}

.code {
  margin: 0;
  padding: 14px;
  background: #0d0d0d;
  border-radius: 8px;
  font-size: 0.8rem;
  overflow: auto;
  max-height: 480px;
}

.muted,
.err {
  padding: 20px;
}

.err {
  color: var(--lc-red);
}
</style>
