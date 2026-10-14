<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import ErrorBanner from '../components/ErrorBanner.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { errorMessage, useAgentsStore } from '../stores/agents'
import { formatDateTime } from '../utils/format'

const props = defineProps<{ id: string }>()
const route = useRoute()
const router = useRouter()
const store = useAgentsStore()

const agentId = computed(() => props.id ?? String(route.params.id ?? ''))
const deleting = ref(false)
const deleteError = ref('')

onMounted(async () => {
  await store.fetchOne(agentId.value)
  void store.fetchRuns(agentId.value, 1)
})

function gotoEdit(): void {
  void router.push({ name: 'agent-edit', params: { id: agentId.value } })
}

function gotoList(): void {
  void router.push({ name: 'agents' })
}

async function removeAgent(): Promise<void> {
  const name = store.current?.name ?? '该智能体'
  if (!window.confirm(`确定删除「${name}」？此操作不可恢复。`)) return

  deleting.value = true
  deleteError.value = ''
  try {
    await store.remove(agentId.value)
    gotoList()
  } catch (err) {
    deleteError.value = errorMessage(err, '删除失败，请稍后重试')
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <section data-testid="agent-detail-page">
    <div
      v-if="store.currentLoading && !store.current"
      class="empty-state"
      data-testid="detail-loading"
    >
      加载中…
    </div>

    <template v-else-if="store.current">
      <div class="page-header">
        <h1
          class="page-title"
          data-testid="agent-name"
        >
          {{ store.current.name }}
        </h1>
        <div class="row-actions">
          <button
            class="btn"
            data-testid="back-to-list"
            @click="gotoList"
          >
            返回列表
          </button>
          <button
            class="btn btn-danger"
            :disabled="deleting"
            data-testid="delete-agent"
            @click="removeAgent"
          >
            {{ deleting ? '删除中…' : '删除' }}
          </button>
          <button
            class="btn btn-primary"
            data-testid="edit-agent"
            @click="gotoEdit"
          >
            编辑
          </button>
        </div>
      </div>

      <ErrorBanner
        v-if="deleteError"
        :message="deleteError"
      />

      <ErrorBanner
        v-if="store.error"
        :message="store.error"
      />

      <div class="card">
        <dl class="detail-grid">
          <dt>状态</dt>
          <dd><StatusBadge :status="store.current.status" /></dd>
          <dt>模型</dt>
          <dd
            class="mono"
            data-testid="agent-model"
          >
            {{ store.current.model }}
          </dd>
          <dt>描述</dt>
          <dd data-testid="agent-description">
            {{ store.current.description || '—' }}
          </dd>
          <dt>工具</dt>
          <dd data-testid="agent-tools">
            <span v-if="store.current.tools && store.current.tools.length">
              <span
                v-for="tool in store.current.tools"
                :key="tool"
                class="badge"
              >{{ tool }}</span>
            </span>
            <span v-else>—</span>
          </dd>
          <dt>创建时间</dt>
          <dd>{{ formatDateTime(store.current.createdAt) }}</dd>
          <dt>更新时间</dt>
          <dd>{{ formatDateTime(store.current.updatedAt) }}</dd>
        </dl>

        <h2 class="section-title">
          系统提示词
        </h2>
        <pre
          v-if="store.current.systemPrompt"
          class="prompt"
          data-testid="agent-system-prompt"
        >{{
          store.current.systemPrompt
        }}</pre>
        <p
          v-else
          class="muted"
        >
          未设置
        </p>
      </div>

      <h2 class="section-title">
        运行记录
      </h2>
      <div
        v-if="store.runsLoading"
        class="empty-state"
        data-testid="runs-loading"
      >
        加载中…
      </div>
      <EmptyState
        v-else-if="store.runs.length === 0"
        message="暂无运行记录"
      />
      <table
        v-else
        class="table"
        data-testid="runs-table"
      >
        <thead>
          <tr>
            <th>状态</th>
            <th>输入</th>
            <th>输出</th>
            <th>错误</th>
            <th>创建时间</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="run in store.runs"
            :key="run.id"
            :data-testid="`run-row-${run.id}`"
          >
            <td><StatusBadge :status="run.status" /></td>
            <td class="mono">
              {{ run.input || '—' }}
            </td>
            <td class="mono">
              {{ run.output || '—' }}
            </td>
            <td class="mono">
              {{ run.errorMessage || '—' }}
            </td>
            <td class="muted">
              {{ formatDateTime(run.createdAt) }}
            </td>
          </tr>
        </tbody>
      </table>
    </template>

    <div
      v-else
      class="empty-state"
      data-testid="detail-empty"
    >
      未找到该智能体。<a
        href="#"
        @click.prevent="gotoList"
      >返回列表</a>
    </div>
  </section>
</template>
