<script setup lang="ts">
import { onMounted, ref, toRefs } from 'vue'
import { useRouter } from 'vue-router'
import PageBack from '@/components/PageBack.vue'
import { fetchSubmissionList } from '@/api/user'
import { useSubmissionsListStore } from '@/stores/submissionsList'
import type { SubmissionListItem } from '@/types/api'
import { verdictClass, verdictText } from '@/utils/format'

function listResult(s: SubmissionListItem): number {
  if (s.result !== undefined && s.result !== null) return s.result
  if (s.status !== undefined && s.status !== null) return s.status
  return -1
}

const router = useRouter()
const store = useSubmissionsListStore()
const { page, resultFilter } = toRefs(store)
const list = ref<SubmissionListItem[]>([])
const total = ref(0)
const pages = ref(0)
const loading = ref(false)
const err = ref('')

async function load() {
  loading.value = true
  err.value = ''
  try {
    const params = {
      page: page.value,
      size: 15,
      result: resultFilter.value === '' ? undefined : (resultFilter.value as number),
    }
    console.log('请求参数:', params)
    const res = await fetchSubmissionList(params)
    console.log('API完整响应:', res)
    if (res.code !== 200) {
      err.value = res.message || '加载失败'
      console.error('API返回错误:', res.message)
      return
    }
    const d = res.data
    console.log('数据部分(data):', d)
    console.log('列表(list):', d?.list)
    console.log('总数(total):', d?.total)
    list.value = d?.list ?? []
    total.value = Number(d?.total ?? 0)
    pages.value = Number(d?.pages ?? 0)
    page.value = Number(d?.currentPage ?? 1)
    store.listLoaded = true
    console.log('最终列表长度:', list.value.length)
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
    console.error('加载提交记录失败:', e)
  } finally {
    loading.value = false
  }
}

function open(id: number) {
  router.push({ name: 'submission-detail', params: { id: String(id) } })
}

onMounted(() => {
  void load()
})

function applyFilter() {
  page.value = 1
  void load()
}
</script>

<script lang="ts">
export default { name: 'SubmissionsView' }
</script>

<template>
  <div class="page">
    <PageBack to="/problems" label="返回题库" />
    <h1 class="page-title">提交记录</h1>
    <div class="toolbar card">
      <select v-model="resultFilter" @change="applyFilter">
        <option value="">全部结果</option>
        <option :value="0">通过</option>
        <option :value="1">编译错误</option>
        <option :value="2">运行错误</option>
        <option :value="3">超时</option>
      </select>
      <span class="muted">共 {{ total }} 条</span>
    </div>
    <p v-if="err" class="err">{{ err }}</p>
    <div class="card table-wrap">
      <div v-if="loading" class="muted pad">加载中…</div>
      <table v-else class="table">
        <thead>
        <tr>
          <th>状态</th>
          <th>题目</th>
          <th>分类</th>
          <th>提交时间</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="s in list" :key="s.submissionId" class="row" @click="open(s.submissionId)">
          <td>
            <span class="verdict" :class="verdictClass(listResult(s))">{{ verdictText(listResult(s)) }}</span>
          </td>
          <td>#{{ s.problemId }} {{ s.problemTitle }}</td>
          <td class="muted">{{ s.problemCategory }}</td>
          <td class="muted">{{ s.submitTime }}</td>
        </tr>
        </tbody>
      </table>
      <div v-if="!loading && (!list || !list.length)" class="muted pad">暂无提交</div>
    </div>
    <div v-if="pages > 1" class="pager">
      <button type="button" class="btn" :disabled="page.value <= 1" @click="page.value--; load()">上一页</button>
      <span>{{ page.value }} / {{ pages.value }}</span>
      <button type="button" class="btn" :disabled="page.value >= pages.value" @click="page.value++; load()">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 900px;
}
h1 {
  margin: 0 0 16px;
}
.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 12px 16px;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
}
select {
  padding: 8px 12px;
}
.muted {
  color: var(--lc-text-muted);
}
.pad {
  padding: 32px;
  text-align: center;
}
.err {
  color: var(--lc-red);
}
.table-wrap {
  padding: 0;
  overflow: hidden;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.88rem;
}
.table th {
  text-align: left;
  padding: 10px 14px;
  color: var(--lc-text-muted);
  border-bottom: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
}
.table td {
  padding: 10px 14px;
  border-bottom: 1px solid var(--lc-border);
}
.row {
  cursor: pointer;
}
.row:hover {
  background: rgba(255, 161, 22, 0.05);
}
.verdict {
  font-weight: 600;
  font-size: 0.85rem;
}
.verdict-ac {
  color: var(--lc-green);
}
.verdict-wa {
  color: var(--lc-text-muted);
}
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 12px;
}
.btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text);
  cursor: pointer;
}
.btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
</style>
