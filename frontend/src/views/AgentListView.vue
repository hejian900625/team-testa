<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import ErrorBanner from '../components/ErrorBanner.vue'
import EmptyState from '../components/EmptyState.vue'
import StatusBadge from '../components/StatusBadge.vue'
import { useAgentsStore } from '../stores/agents'
import { formatDateTime } from '../utils/format'
import type { AgentStatus } from '../api/client'

const router = useRouter()
const store = useAgentsStore()

const keywordInput = ref(store.keyword)
const statusFilter = ref<AgentStatus | ''>(store.status)

const totalPages = computed(() =>
  store.total === 0 ? 1 : Math.ceil(store.total / store.size)
)

onMounted(() => {
  void store.fetchList()
})

function onSearch(): void {
  void store.fetchList({ keyword: keywordInput.value, status: statusFilter.value, page: 1 })
}

function onReset(): void {
  keywordInput.value = ''
  statusFilter.value = ''
  void store.fetchList({ keyword: '', status: '', page: 1 })
}

function gotoDetail(id: string): void {
  void router.push({ name: 'agent-detail', params: { id } })
}

function gotoCreate(): void {
  void router.push({ name: 'agent-create' })
}

function prevPage(): void {
  if (store.page > 1) void store.fetchList({ page: store.page - 1 })
}

function nextPage(): void {
  if (store.page < totalPages.value) void store.fetchList({ page: store.page + 1 })
}
</script>

<template>
  <section data-testid="agent-list-page">
    <div class="page-header">
      <h1 class="page-title">
        智能体
      </h1>
      <button
        class="btn btn-primary"
        data-testid="create-agent"
        @click="gotoCreate"
      >
        新建智能体
      </button>
    </div>

    <div class="toolbar">
      <input
        v-model="keywordInput"
        class="input"
        style="flex: 1; min-width: 200px"
        type="search"
        placeholder="按名称搜索"
        aria-label="按名称搜索"
        data-testid="keyword-input"
        @keyup.enter="onSearch"
      >
      <select
        v-model="statusFilter"
        class="select"
        aria-label="按状态筛选"
        data-testid="status-filter"
      >
        <option value="">
          全部状态
        </option>
        <option value="DRAFT">
          草稿
        </option>
        <option value="ACTIVE">
          启用
        </option>
        <option value="DISABLED">
          停用
        </option>
      </select>
      <button
        class="btn"
        data-testid="search-button"
        @click="onSearch"
      >
        搜索
      </button>
      <button
        class="btn"
        data-testid="reset-button"
        @click="onReset"
      >
        重置
      </button>
    </div>

    <ErrorBanner
      v-if="store.error"
      :message="store.error"
    />

    <div
      v-if="store.loading"
      class="empty-state"
      data-testid="list-loading"
    >
      加载中…
    </div>

    <EmptyState
      v-else-if="store.items.length === 0"
      message="还没有智能体，点击右上角「新建智能体」创建第一个。"
    />

    <table
      v-else
      class="table"
      data-testid="agent-table"
    >
      <thead>
        <tr>
          <th>名称</th>
          <th>模型</th>
          <th>状态</th>
          <th>更新时间</th>
        </tr>
      </thead>
      <tbody>
        <tr
          v-for="agent in store.items"
          :key="agent.id"
          :data-testid="`agent-row-${agent.id}`"
          @click="gotoDetail(agent.id)"
        >
          <td>
            <a
              href="#"
              @click.prevent="gotoDetail(agent.id)"
            >{{ agent.name }}</a>
          </td>
          <td class="mono">
            {{ agent.model }}
          </td>
          <td><StatusBadge :status="agent.status" /></td>
          <td class="muted">
            {{ formatDateTime(agent.updatedAt) }}
          </td>
        </tr>
      </tbody>
    </table>

    <div
      class="pagination"
      data-testid="pagination"
    >
      <button
        class="btn btn-sm"
        data-testid="prev-page"
        :disabled="store.page <= 1"
        @click="prevPage"
      >
        上一页
      </button>
      <span data-testid="page-info">第 {{ store.page }} / {{ totalPages }} 页 · 共 {{ store.total }} 条</span>
      <button
        class="btn btn-sm"
        data-testid="next-page"
        :disabled="store.page >= totalPages"
        @click="nextPage"
      >
        下一页
      </button>
    </div>
  </section>
</template>
