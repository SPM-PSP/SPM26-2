<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchUserInfo } from '@/api/user'
import type { UserInfo } from '@/types/api'

const info = ref<UserInfo | null>(null)
const err = ref('')
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await fetchUserInfo()
    if (res.code !== 200) {
      err.value = res.message || '获取失败'
      return
    }
    info.value = res.data
  } catch (e: unknown) {
    err.value = e instanceof Error ? e.message : '网络错误'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page">
    <h1>个人资料</h1>
    <p class="sub"><code>/api/v1/user/info</code></p>
    <div v-if="loading" class="card muted">加载中…</div>
    <div v-else-if="err" class="card err">{{ err }}</div>
    <div v-else-if="info" class="card">
      <div class="row">
        <img v-if="info.avatar" :src="info.avatar" alt="" class="avatar" />
        <div v-else class="avatar ph">{{ info.nickname?.slice(0, 1) || '?' }}</div>
        <div>
          <div class="name">{{ info.nickname }}</div>
          <div class="muted">@{{ info.username }}</div>
        </div>
      </div>
      <dl>
        <dt>邮箱</dt>
        <dd>{{ info.email }}</dd>
        <dt v-if="info.phone">手机</dt>
        <dd v-if="info.phone">{{ info.phone }}</dd>
        <dt>用户 ID</dt>
        <dd>{{ info.userId }}</dd>
      </dl>
    </div>
  </div>
</template>

<style scoped>
.page {
  max-width: 480px;
}

h1 {
  margin: 0 0 6px;
}

.sub {
  margin: 0 0 16px;
  color: var(--lc-text-muted);
  font-size: 0.85rem;
}

.card {
  background: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: 10px;
  padding: 20px;
}

.row {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 20px;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar.ph {
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--lc-border);
  color: var(--lc-text-muted);
  font-size: 1.5rem;
}

.name {
  font-weight: 700;
  font-size: 1.1rem;
}

dl {
  margin: 0;
  display: grid;
  grid-template-columns: 80px 1fr;
  gap: 8px 12px;
  font-size: 0.9rem;
}

dt {
  color: var(--lc-text-muted);
}

dd {
  margin: 0;
}

.muted {
  color: var(--lc-text-muted);
}

.err {
  color: var(--lc-red);
}

code {
  color: var(--lc-accent);
}
</style>
