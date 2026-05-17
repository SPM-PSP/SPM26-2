<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminAddUser, adminDeleteUser, adminUserList } from '@/api/admin'
import type { AdminUserListItem } from '@/types/api'

const list = ref<AdminUserListItem[]>([])
const total = ref(0)
const pages = ref(0)
const page = ref(1)
const keyword = ref('')
const loading = ref(false)
const err = ref('')
const msg = ref('')

const showAdd = ref(false)
const form = ref({ username: '', password: '', email: '', nickname: '' })
const submitting = ref(false)

async function load() {
  loading.value = true
  err.value = ''
  try {
    const res = await adminUserList({ page: page.value, size: 10, keyword: keyword.value || undefined })
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

async function onDelete(id: number) {
  if (!confirm('确定删除该用户？')) return
  msg.value = ''
  try {
    const res = await adminDeleteUser(id)
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

async function onAdd() {
  submitting.value = true
  err.value = ''
  msg.value = ''
  try {
    const res = await adminAddUser({
      username: form.value.username.trim(),
      password: form.value.password,
      email: form.value.email.trim(),
      nickname: form.value.nickname.trim() || undefined,
    })
    if (res.code !== 200) {
      err.value = res.message || '添加失败'
      return
    }
    msg.value = res.message || '添加成功'
    showAdd.value = false
    form.value = { username: '', password: '', email: '', nickname: '' }
    await load()
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '请求失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  void load()
})
</script>

<template>
  <div class="page">
    <div class="head">
      <h1>用户管理</h1>
      <button type="button" class="btn" @click="showAdd = !showAdd">{{ showAdd ? '关闭' : '新增用户' }}</button>
    </div>

    <div v-if="showAdd" class="card form-card">
      <h3>新增用户</h3>
      <div class="grid">
        <label>用户名</label>
        <input v-model="form.username" type="text" />
        <label>密码</label>
        <input v-model="form.password" type="password" />
        <label>邮箱</label>
        <input v-model="form.email" type="email" />
        <label>昵称</label>
        <input v-model="form.nickname" type="text" placeholder="可选" />
      </div>
      <button type="button" class="btn primary" :disabled="submitting" @click="onAdd">提交</button>
    </div>

    <div class="toolbar card">
      <input v-model="keyword" type="search" placeholder="搜索用户名/邮箱/昵称" @keyup.enter="page = 1; load()" />
      <button type="button" class="btn" @click="page = 1; load()">搜索</button>
    </div>

    <p v-if="err" class="err">{{ err }}</p>
    <p v-if="msg" class="msg">{{ msg }}</p>

    <div class="card table-wrap">
      <div v-if="loading" class="muted pad">加载中…</div>
      <table v-else class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>昵称</th>
            <th>创建时间</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="u in list" :key="u.id">
            <td>{{ u.id }}</td>
            <td>{{ u.username }}</td>
            <td>{{ u.email }}</td>
            <td>{{ u.nickname }}</td>
            <td class="muted">{{ u.createTime }}</td>
            <td>
              <button type="button" class="link-btn" @click="onDelete(u.id)">删除</button>
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
  padding: 14px 16px;
  margin-bottom: 12px;
}
.form-card h3 {
  margin: 0 0 12px;
  font-size: 0.95rem;
}
.grid {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 10px 12px;
  align-items: center;
  margin-bottom: 12px;
}
.grid label {
  font-size: 0.82rem;
  color: var(--lc-text-muted);
}
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.toolbar input {
  flex: 1;
  min-width: 160px;
  padding: 8px 10px;
}
.btn {
  padding: 8px 14px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface-2);
  color: var(--lc-text);
  cursor: pointer;
  font-size: 0.85rem;
}
.btn.primary {
  background: var(--lc-accent);
  color: #111;
  border: none;
  font-weight: 600;
}
.btn:disabled {
  opacity: 0.6;
  cursor: wait;
}
.err {
  color: var(--lc-red);
  font-size: 0.88rem;
}
.msg {
  color: var(--lc-green);
  font-size: 0.88rem;
}
.table-wrap {
  padding: 0;
  overflow: auto;
}
.pad {
  padding: 24px;
  text-align: center;
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
  font-weight: 500;
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
  font-size: 0.82rem;
}
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-top: 12px;
}
</style>
