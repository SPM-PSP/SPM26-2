<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { analyzeCode } from '@/api/ai'
import { fetchSubmissionDetail } from '@/api/user'
import type { CodeAnalysisResponse, SubmissionDetail } from '@/types/api'
import { verdictClass, verdictText } from '@/utils/format'

const props = defineProps<{ id: string }>()
const router = useRouter()
const detail = ref<SubmissionDetail | null>(null)
const loading = ref(true)
const err = ref('')

const aiEval = ref<CodeAnalysisResponse | null>(null)
const aiLoading = ref(false)
const aiErr = ref('')

async function load() {
  loading.value = true
  err.value = ''
  aiEval.value = null
  aiErr.value = ''
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

async function loadAiEvaluation() {
  if (!detail.value) {
    aiErr.value = '请先加载提交详情'
    return
  }

  aiLoading.value = true
  aiErr.value = ''
  aiEval.value = null

  try {
    const res = await analyzeCode({
      code: detail.value.code,
      language: detail.value.language,
    })

    if (!res) {
      aiErr.value = 'AI 分析失败'
      return
    }

    aiEval.value = res
  } catch (e: unknown) {
    aiErr.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    aiLoading.value = false
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
    <button type="button" class="back" @click="router.push('/submissions')">← 返回</button>
    <div v-if="loading" class="muted">加载中…</div>
    <div v-else-if="err || !detail" class="err">{{ err || '无数据' }}</div>
    <div v-else class="card">
      <div class="head">
        <h1>{{ detail.problemTitle }}</h1>
        <span class="verdict" :class="verdictClass(detail.result)">{{ verdictText(detail.result) }}</span>
      </div>
      <p class="meta">
        #{{ detail.submissionId }} · {{ detail.language }} · {{ detail.submitTime }} · {{ detail.passCount }}/{{
          detail.totalCount
        }}
        · {{ detail.runTime }} ms · {{ detail.memory }} KB
      </p>
      <p v-if="detail.errorMsg" class="err-msg">{{ detail.errorMsg }}</p>
      <h3 class="h">代码</h3>
      <pre class="code">{{ detail.code }}</pre>

      <div class="ai-block">
        <div class="ai-head">
          <h3 class="h mb0">AI 多维评测</h3>
          <button type="button" class="btn-ai" :disabled="aiLoading" @click="loadAiEvaluation">
            {{ aiLoading ? '分析中…' : '获取 AI 评价' }}
          </button>
        </div>
        <p class="muted sm">基于本次提交的代码，向服务端请求 AI 多维分析。</p>
        <p v-if="aiErr" class="err-msg">{{ aiErr }}</p>
        <template v-else-if="aiEval">
          <dl class="ai-dl">
            <dt>算法分类</dt>
            <dd>{{ aiEval.plateCategory }}</dd>
            <dt>复杂度分析</dt>
            <dd>{{ aiEval.complexityAnalysis }}</dd>
            <dt>代码风格</dt>
            <dd>{{ aiEval.codeStyleAnalysis }}</dd>
          </dl>
        </template>
      </div>
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

.ai-block {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--lc-border);
}

.ai-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.mb0 {
  margin-bottom: 0;
}

.btn-ai {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid var(--lc-accent);
  background: rgba(255, 161, 22, 0.12);
  color: var(--lc-accent);
  font-weight: 600;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-ai:disabled {
  opacity: 0.5;
  cursor: wait;
}

.muted.sm {
  font-size: 0.78rem;
  margin: 6px 0 12px;
}

.ai-dl {
  margin: 0;
  font-size: 0.88rem;
}

.ai-dl dt {
  margin-top: 12px;
  font-weight: 600;
  color: var(--lc-text-muted);
  font-size: 0.8rem;
}

.ai-dl dt:first-child {
  margin-top: 0;
}

.ai-dl dd {
  margin: 4px 0 0;
  white-space: pre-wrap;
  line-height: 1.55;
}
</style>
