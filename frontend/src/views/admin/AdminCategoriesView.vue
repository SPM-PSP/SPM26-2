<script setup lang="ts">
import { onMounted, ref } from 'vue'

defineOptions({ name: 'AdminCategoriesView' })
import { fetchCategories } from '@/api/problem'
import { adminAddCategory, adminDeleteCategory } from '@/api/admin'
import type { CategoryVO } from '@/types/api'

const list = ref<CategoryVO[]>([])
const newName = ref('')
const err = ref('')
const msg = ref('')
const loading = ref(false)

async function load() {
  loading.value = true
  err.value = ''
  try {
    const res = await fetchCategories()
    if (res.code !== 200) {
      err.value = res.message || '加载失败'
      return
    }
    list.value = res.data ?? []
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    loading.value = false
  }
}

async function onAdd() {
  const name = newName.value.trim()
  if (!name) return
  err.value = ''
  msg.value = ''
  try {
    const res = await adminAddCategory(name)
    if (res.code !== 200) {
      err.value = res.message || '添加失败'
      return
    }
    msg.value = res.message || '已添加'
    newName.value = ''
    await load()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

async function onDelete(c: CategoryVO) {
  if (!confirm(`删除分类「${c.categoryName}」？（若已被题目使用将无法删除）`)) return
  err.value = ''
  msg.value = ''
  try {
    const res = await adminDeleteCategory(c.categoryId)
    if (res.code !== 200) {
      err.value = res.message || '删除失败'
      return
    }
    msg.value = res.message || '已删除'
    await load()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <h1>题目分类</h1>

    <div class="card add-row">
      <input v-model="newName" type="text" placeholder="新分类名称" @keyup.enter="onAdd" />
      <button type="button" class="btn primary" @click="onAdd">添加</button>
    </div>

    <p v-if="err" class="err">{{ err }}</p>
    <p v-if="msg" class="msg">{{ msg }}</p>

    <div class="card table-wrap">
      <div v-if="loading" class="muted pad">加载中…</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in list" :key="c.categoryId">
            <td>{{ c.categoryId }}</td>
            <td>{{ c.categoryName }}</td>
            <td>
              <button type="button" class="link-btn" @click="onDelete(c)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<style scoped>
.page {
  width: 100%;
}
h1 {
  margin: 0 0 16px;
  font-size: 1.25rem;
}
.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 14px 16px;
  margin-bottom: 12px;
}
.add-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.add-row input {
  flex: 1;
  min-width: 200px;
  padding: 8px 10px;
}
.btn {
  padding: 8px 16px;
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
.msg {
  color: var(--lc-green);
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
  font-size: 0.88rem;
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
.link-btn {
  background: none;
  border: none;
  color: var(--lc-red);
  cursor: pointer;
}
</style>
