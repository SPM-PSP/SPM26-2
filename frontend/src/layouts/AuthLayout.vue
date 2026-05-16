<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="auth-root">
    <RouterView />
  </div>
</template>

<script lang="ts">
export default { name: 'AuthLayout' }
</script>

<style scoped>
.auth-root {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: radial-gradient(ellipse 80% 60% at 50% -20%, rgba(255, 161, 22, 0.12), transparent),
    var(--lc-bg);
}
</style>
