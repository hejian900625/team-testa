<script setup lang="ts">
defineProps<{ message: string }>()
</script>

<template>
  <div
    class="error-banner"
    role="alert"
    data-testid="error-banner"
  >
    {{ message }}
  </div>
</template>
