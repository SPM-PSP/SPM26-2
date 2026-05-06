<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { fetchCategories, fetchProblemList, type ProblemListParams } from '@/api/problem'
import { useAuthStore } from '@/stores/auth'
import type { CategoryVO, ProblemListItem } from '@/types/api'
import { difficultyClass, difficultyLabel, formatAcceptRate } from '@/utils/format'

const router = useRouter()
const auth = useAuthStore()

const categories = ref<CategoryVO[]>([])
const list = ref<ProblemListItem[]>([])
const total = ref(0)
const pages = ref(0)
const currentPage = ref(1)
const loading = ref(false)
const err = ref('')

const keyword = ref('')
const difficulty = ref('')
const status = ref<number | ''>('')
const selectedCats = ref<string[]>([])

async function loadCats() {
  try {
    const res = await fetchCategories()
    if (res.code === 200) categories.value = res.data ?? []
  } catch {
    /* optional */
  }
}

async function load() {
  loading.value = true
  err.value = ''
  try {
    const params: ProblemListParams = {
      page: currentPage.value,
      size: 20,
      keyword: keyword.value || undefined,
      difficulty: difficulty.value || undefined,
      problemCategory: selectedCats.value.length ? selectedCats.value : undefined,
    }
    if (auth.isLoggedIn && status.value !== '') params.status = status.value as number
    const res = await fetchProblemList(params)
    if (res.code !== 200) {
      err.value = res.message || '加载失败'
      return
    }
    const d = res.data
    list.value = d?.list ?? []
    total.value = Number(d?.total ?? 0)
    pages.value = Number(d?.pages ?? 0)
    currentPage.value = Number(d?.currentPage ?? 1)
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
}

function toggleCat(name: string) {
  const i = selectedCats.value.indexOf(name)
  if (i >= 0) selectedCats.value.splice(i, 1)
  else selectedCats.value.push(name)
}

function openProblem(id: number) {
  router.push({ name: 'problem-detail', params: { id: String(id) } })
}

onMounted(async () => {
  await loadCats()
  await load()
})

function search() {
  currentPage.value = 1
  void load()
}
</script>

<template>
  <div class="page">
    <div class="head">
      <h1>题库</h1>
      <p class="sub">筛选题目，点击标题进入做题页（界面风格参考 LeetCode 暗色题库）。</p>
    </div>

    <div class="toolbar card">
      <input v-model="keyword" class="grow" type="search" placeholder="搜索题目…" @keyup.enter="search" />
      <select v-model="difficulty" @change="search">
        <option value="">全部难度</option>
        <option value="easy">简单</option>
        <option value="medium">中等</option>
        <option value="hard">困难</option>
      </select>
      <select v-if="auth.isLoggedIn" v-model="status" @change="search">
        <option value="">全部状态</option>
        <option :value="0">未通过</option>
        <option :value="1">已通过</option>
      </select>
      <button type="button" class="btn-accent" @click="search">搜索</button>
    </div>

    <div v-if="categories.length" class="tags card">
      <span class="tags-label">分类</span>
      <button
        v-for="c in categories"
        :key="c.categoryId"
        type="button"
        class="tag"
        :class="{ on: selectedCats.includes(c.categoryName) }"
        @click="toggleCat(c.categoryName); search()"
      >
        {{ c.categoryName }}
      </button>
    </div>

    <p v-if="err" class="err">{{ err }}</p>

    <div class="card table-wrap">
      <div v-if="loading" class="loading">加载中…</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th class="col-status" />
            <th>题目</th>
            <th class="col-diff">难度</th>
            <th class="col-rate">通过率</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in list" :key="p.problemId" class="row" @click="openProblem(p.problemId)">
            <td class="col-status">
              <span v-if="p.status === 1" class="dot solved" title="已通过" />
              <span v-else-if="p.status === 0" class="dot tried" title="未通过" />
              <span v-else class="dot none" />
            </td>
            <td>
              <div class="title-row">
                <span class="title">{{ p.title }}</span>
                <span class="cats">{{ p.categoryNames?.join(' · ') }}</span>
              </div>
            </td>
            <td>
              <span class="pill" :class="difficultyClass(p.difficulty)">{{ difficultyLabel(p.difficulty) }}</span>
            </td>
            <td class="muted">{{ formatAcceptRate(p.acceptRate) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="!loading && !list.length" class="empty">暂无题目</div>
    </div>

    <div v-if="pages > 1" class="pager">
      <button
        type="button"
        :disabled="currentPage <= 1"
        class="btn-page"
        @click="
          currentPage--;
          load()
        "
      >
        上一页
      </button>
      <span class="page-info">{{ currentPage }} / {{ pages }}</span>
      <button
        type="button"
        :disabled="currentPage >= pages"
        class="btn-page"
        @click="
          currentPage++;
          load()
        "
      >
        下一页
      </button>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 1100px;
}

.head h1 {
  margin: 0 0 6px;
  font-size: 1.5rem;
  font-weight: 700;
}

.sub {
  margin: 0 0 18px;
  color: var(--lc-text-muted);
  font-size: 0.9rem;
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 14px;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.toolbar input.grow {
  flex: 1;
  min-width: 180px;
  padding: 10px 12px;
}

.toolbar select {
  padding: 9px 12px;
}

.btn-accent {
  padding: 9px 20px;
  border-radius: 8px;
  border: none;
  background: var(--lc-accent);
  color: #111;
  font-weight: 600;
  cursor: pointer;
}

.btn-accent:hover {
  filter: brightness(1.05);
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.tags-label {
  color: var(--lc-text-muted);
  font-size: 0.8rem;
  margin-right: 4px;
}

.tag {
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text-muted);
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 0.8rem;
  cursor: pointer;
}

.tag.on {
  border-color: var(--lc-accent);
  color: var(--lc-accent);
  background: rgba(255, 161, 22, 0.08);
}

.err {
  color: var(--lc-red);
  font-size: 0.9rem;
}

.table-wrap {
  padding: 0;
  overflow: hidden;
}

.loading,
.empty {
  padding: 40px;
  text-align: center;
  color: var(--lc-text-muted);
}

.table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.9rem;
}

.table th {
  text-align: left;
  padding: 12px 16px;
  color: var(--lc-text-muted);
  font-weight: 500;
  border-bottom: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
}

.table td {
  padding: 12px 16px;
  border-bottom: 1px solid var(--lc-border);
}

.row {
  cursor: pointer;
}

.row:hover {
  background: rgba(255, 161, 22, 0.04);
}

.col-status {
  width: 44px;
  text-align: center;
}

.col-diff {
  width: 100px;
}

.col-rate {
  width: 100px;
}

.dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 2px solid var(--lc-border);
}

.dot.solved {
  background: var(--lc-green);
  border-color: var(--lc-green);
}

.dot.tried {
  border-color: var(--lc-medium);
}

.title {
  font-weight: 500;
}

.title-row {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.cats {
  font-size: 0.75rem;
  color: var(--lc-text-muted);
}

.pill {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.diff-easy {
  color: var(--lc-easy);
  background: rgba(0, 184, 163, 0.12);
}

.diff-medium {
  color: var(--lc-medium);
  background: rgba(255, 192, 30, 0.12);
}

.diff-hard {
  color: var(--lc-hard);
  background: rgba(255, 55, 95, 0.12);
}

.muted {
  color: var(--lc-text-muted);
}

.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 16px;
}

.btn-page {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text);
  cursor: pointer;
}

.btn-page:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.page-info {
  color: var(--lc-text-muted);
  font-size: 0.9rem;
}
</style>
