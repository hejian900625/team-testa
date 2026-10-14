<script setup lang="ts">
withDefaults(
  defineProps<{
    message?: string
  }>(),
  { message: '暂无数据' }
)
</script>

<template>
  <div
    class="empty-state"
    data-testid="empty-state"
  >
    {{ message }}
  </div>
</template>
