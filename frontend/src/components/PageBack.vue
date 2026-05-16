<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = withDefaults(
  defineProps<{
    label?: string
    to?: string | { name: string; params?: Record<string, string> }
  }>(),
  { label: '返回上一级' },
)

const router = useRouter()

function goBack() {
  if (props.to) {
    void router.push(props.to)
    return
  }
  if (window.history.length > 1) {
    router.back()
    return
  }
  void router.push('/problems')
}
</script>

<template>
  <button type="button" class="page-back" @click="goBack">
    <span class="arrow">←</span>
    {{ label }}
  </button>
</template>

<style scoped>
.page-back {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
  padding: 6px 12px;
  border-radius: 8px;
  border: 1px solid var(--lc-border);
  background: var(--lc-surface);
  color: var(--lc-text-muted);
  font-size: 0.85rem;
  cursor: pointer;
}

.page-back:hover {
  color: var(--lc-accent);
  border-color: var(--lc-accent-dim);
}

.arrow {
  font-size: 1rem;
}
</style>
