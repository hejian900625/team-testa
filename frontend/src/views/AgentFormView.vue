<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import ErrorBanner from '../components/ErrorBanner.vue'
import { useAgentsStore } from '../stores/agents'
import { errorMessage } from '../stores/agents'
import type { AgentStatus } from '../api/client'

const props = defineProps<{ id?: string }>()
const route = useRoute()
const router = useRouter()
const store = useAgentsStore()

const agentId = computed(() => props.id ?? (route.params.id as string | undefined))
const isEdit = computed(() => Boolean(agentId.value))

const form = reactive({
  name: '',
  description: '',
  model: '',
  systemPrompt: '',
  toolsText: '',
  status: 'DRAFT' as AgentStatus
})

const errors = reactive({
  name: '',
  model: ''
})
const formError = ref('')
const submitting = ref(false)
const loading = ref(false)

onMounted(async () => {
  if (!agentId.value) return
  loading.value = true
  const agent = await store.fetchOne(agentId.value)
  loading.value = false
  if (agent) {
    form.name = agent.name
    form.description = agent.description ?? ''
    form.model = agent.model
    form.systemPrompt = agent.systemPrompt ?? ''
    form.toolsText = (agent.tools ?? []).join(', ')
    form.status = agent.status
  } else {
    formError.value = '加载智能体失败'
  }
})

function parseTools(text: string): string[] {
  return text
    .split(',')
    .map((t) => t.trim())
    .filter((t) => t.length > 0)
}

function validate(): boolean {
  errors.name = form.name.trim() ? '' : '名称为必填项'
  errors.model = form.model.trim() ? '' : '模型为必填项'
  return !errors.name && !errors.model
}

async function onSubmit(): Promise<void> {
  formError.value = ''
  if (!validate()) return

  const tools = parseTools(form.toolsText)
  submitting.value = true
  try {
    if (isEdit.value && agentId.value) {
      await store.update(agentId.value, {
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        model: form.model.trim(),
        systemPrompt: form.systemPrompt || undefined,
        tools,
        status: form.status
      })
      void router.push({ name: 'agent-detail', params: { id: agentId.value } })
    } else {
      const created = await store.create({
        name: form.name.trim(),
        description: form.description.trim() || undefined,
        model: form.model.trim(),
        systemPrompt: form.systemPrompt || undefined,
        tools,
        status: form.status
      })
      void router.push({ name: 'agent-detail', params: { id: created.id } })
    }
  } catch (err) {
    formError.value = errorMessage(err, '保存失败，请重试')
  } finally {
    submitting.value = false
  }
}

function onCancel(): void {
  if (isEdit.value && agentId.value) {
    void router.push({ name: 'agent-detail', params: { id: agentId.value } })
  } else {
    void router.push({ name: 'agents' })
  }
}
</script>

<template>
  <section data-testid="agent-form-page">
    <div class="page-header">
      <h1 class="page-title">
        {{ isEdit ? '编辑智能体' : '新建智能体' }}
      </h1>
    </div>

    <div
      v-if="loading"
      class="empty-state"
      data-testid="form-loading"
    >
      加载中…
    </div>

    <form
      v-else
      class="card"
      data-testid="agent-form"
      novalidate
      @submit.prevent="onSubmit"
    >
      <ErrorBanner
        v-if="formError"
        :message="formError"
      />

      <div class="form-field">
        <label for="agent-name">名称 *</label>
        <input
          id="agent-name"
          v-model="form.name"
          class="input"
          type="text"
          maxlength="64"
          data-testid="input-name"
        >
        <span
          v-if="errors.name"
          class="field-error"
          data-testid="error-name"
        >{{ errors.name }}</span>
      </div>

      <div class="form-field">
        <label for="agent-model">模型 *</label>
        <input
          id="agent-model"
          v-model="form.model"
          class="input"
          type="text"
          maxlength="128"
          placeholder="例如 gpt-4o-mini"
          data-testid="input-model"
        >
        <span
          v-if="errors.model"
          class="field-error"
          data-testid="error-model"
        >{{ errors.model }}</span>
      </div>

      <div class="form-field">
        <label for="agent-description">描述</label>
        <input
          id="agent-description"
          v-model="form.description"
          class="input"
          type="text"
          maxlength="512"
          data-testid="input-description"
        >
      </div>

      <div class="form-field">
        <label for="agent-tools">工具（逗号分隔）</label>
        <input
          id="agent-tools"
          v-model="form.toolsText"
          class="input"
          type="text"
          placeholder="web-search, code-interpreter"
          data-testid="input-tools"
        >
      </div>

      <div class="form-field">
        <label for="agent-status">状态</label>
        <select
          id="agent-status"
          v-model="form.status"
          class="select"
          data-testid="input-status"
        >
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
      </div>

      <div class="form-field">
        <label for="agent-system-prompt">系统提示词</label>
        <textarea
          id="agent-system-prompt"
          v-model="form.systemPrompt"
          class="textarea"
          maxlength="8000"
          data-testid="input-system-prompt"
        />
      </div>

      <div class="row-actions">
        <button
          class="btn btn-primary"
          type="submit"
          :disabled="submitting"
          data-testid="submit-agent"
        >
          {{ submitting ? '保存中…' : '保存' }}
        </button>
        <button
          class="btn"
          type="button"
          data-testid="cancel-agent"
          @click="onCancel"
        >
          取消
        </button>
      </div>
    </form>
  </section>
</template>
