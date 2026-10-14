<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <section
    class="empty-state"
    data-testid="not-found-page"
  >
    <p>页面不存在。</p>
    <button
      class="btn"
      data-testid="not-found-home"
      @click="router.push({ name: 'agents' })"
    >
      返回智能体列表
    </button>
  </section>
</template>
