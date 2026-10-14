import type { VueWrapper } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createRouter, createMemoryHistory, type Router } from 'vue-router'
import { beforeEach } from 'vitest'

export function useTestPinia(): void {
  beforeEach(() => {
    setActivePinia(createPinia())
  })
}

const stub = { template: '<div />' }

/** 与 src/router/index.ts 同名同路径的最小路由表，用于断言组件内的跳转目标。 */
export function makeRouter(): Router {
  return createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/agents', name: 'agents', component: stub },
      { path: '/agents/:id', name: 'agent-detail', component: stub },
      { path: '/:pathMatch(.*)*', component: stub }
    ]
  })
}

export type { VueWrapper }
