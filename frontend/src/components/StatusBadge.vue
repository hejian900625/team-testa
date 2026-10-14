<script setup lang="ts">
import { computed } from 'vue'

import type { AgentStatus, RunStatus } from '../api/client'

const props = defineProps<{ status: AgentStatus | RunStatus }>()

const STATUS_LABEL: Record<AgentStatus | RunStatus, string> = {
  DRAFT: '草稿',
  ACTIVE: '启用',
  DISABLED: '停用',
  PENDING: '等待',
  RUNNING: '运行中',
  SUCCEEDED: '成功',
  FAILED: '失败'
}

const label = computed(() => STATUS_LABEL[props.status] ?? props.status)
</script>

<template>
  <span
    class="badge"
    :class="`badge-${status}`"
    :data-testid="`status-${status}`"
  >
    {{ label }}
  </span>
</template>
