<script setup lang="ts">
import { onMounted, ref } from 'vue'

defineOptions({ name: 'AdminProblemsView' })
import { useRouter } from 'vue-router'
import { adminProblemList } from '@/api/admin'
import type { AdminProblemListItem } from '@/types/api'
import { difficultyLabel } from '@/utils/format'

const router = useRouter()
const list = ref<AdminProblemListItem[]>([])
const total = ref(0)
const pages = ref(0)
const page = ref(1)
const keyword = ref('')
const difficulty = ref('')
const loading = ref(false)
const err = ref('')

async function load() {
  loading.value = true
  err.value = ''
  try {
    const res = await adminProblemList({
      page: page.value,
      size: 20,
      keyword: keyword.value || undefined,
      difficulty: difficulty.value || undefined,
    })
    if (res.code !== 200) {
      err.value = res.message || '加载失败'
      return
    }
    const d = res.data
    list.value = d?.list ?? []
    total.value = Number(d?.total ?? 0)
    pages.value = Number(d?.pages ?? 0)
    page.value = Number(d?.currentPage ?? 1)
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    loading.value = false
  }
}

function goNew() {
  router.push({ name: 'admin-problem-new' })
}

function goEdit(id: number) {
  router.push({ name: 'admin-problem-edit', params: { id: String(id) } })
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <div class="head">
      <h1>题库管理</h1>
      <button type="button" class="btn primary" @click="goNew">新增题目</button>
    </div>

    <div class="toolbar card">
      <input v-model="keyword" type="search" placeholder="关键词" @keyup.enter="page = 1; load()" />
      <select v-model="difficulty" @change="page = 1; load()">
        <option value="">全部难度</option>
        <option value="easy">简单</option>
        <option value="medium">中等</option>
        <option value="hard">困难</option>
      </select>
      <button type="button" class="btn" @click="page = 1; load()">搜索</button>
    </div>

    <p v-if="err" class="err">{{ err }}</p>

    <div class="card table-wrap">
      <div v-if="loading" class="muted pad">加载中…</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标题</th>
            <th>难度</th>
            <th>分类</th>
            <th>通过率</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in list" :key="p.problemId">
            <td>{{ p.problemId }}</td>
            <td>{{ p.title }}</td>
            <td>{{ difficultyLabel(p.difficulty) }}</td>
            <td class="muted">{{ p.categoryNames?.join('、') }}</td>
            <td class="muted">{{ p.acceptRate }}</td>
            <td>
              <button type="button" class="link" @click="goEdit(p.problemId)">编辑</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-if="pages > 1" class="pager">
      <button type="button" class="btn" :disabled="page <= 1" @click="page--; load()">上一页</button>
      <span class="muted">{{ page }} / {{ pages }}</span>
      <button type="button" class="btn" :disabled="page >= pages" @click="page++; load()">下一页</button>
    </div>
  </div>
</template>

<style scoped>
.page {
  width: 100%;
}
.head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
h1 {
  margin: 0;
  font-size: 1.25rem;
}
.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 12px 14px;
  margin-bottom: 12px;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.toolbar input {
  flex: 1;
  min-width: 160px;
  padding: 8px 10px;
}
select {
  padding: 8px 10px;
}
.btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  cursor: pointer;
}
.btn.primary {
  background: var(--lc-accent);
  color: #111;
  border: none;
  font-weight: 600;
}
.err {
  color: var(--lc-red);
}
.table-wrap {
  padding: 0;
  overflow: auto;
}
.pad {
  padding: 24px;
}
.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.85rem;
}
.table th,
.table td {
  padding: 10px 12px;
  border-bottom: 1px solid var(--lc-border);
  text-align: left;
}
.table th {
  color: var(--lc-text-muted);
  background: var(--lc-surface-2);
}
.muted {
  color: var(--lc-text-muted);
}
.link {
  background: none;
  border: none;
  color: var(--lc-accent);
  cursor: pointer;
  font-size: 0.85rem;
}
.pager {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}
</style>
