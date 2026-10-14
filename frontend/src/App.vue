<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-shell">
    <header class="app-header">
      <RouterLink
        to="/"
        class="app-brand"
        data-testid="app-brand"
      >
        智能体工作台
      </RouterLink>
      <nav class="app-nav">
        <RouterLink
          to="/"
          data-testid="nav-agents"
        >
          智能体
        </RouterLink>
      </nav>
    </header>
    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>
