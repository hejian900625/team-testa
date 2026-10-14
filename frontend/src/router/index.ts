import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/agents' },
    {
      path: '/agents',
      name: 'agents',
      component: () => import('../views/AgentListView.vue')
    },
    {
      path: '/agents/new',
      name: 'agent-create',
      component: () => import('../views/AgentFormView.vue')
    },
    {
      path: '/agents/:id',
      name: 'agent-detail',
      component: () => import('../views/AgentDetailView.vue'),
      props: true
    },
    {
      path: '/agents/:id/edit',
      name: 'agent-edit',
      component: () => import('../views/AgentFormView.vue'),
      props: (route) => ({ id: route.params.id })
    },
    { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('../views/NotFoundView.vue') }
  ]
})

export { router }
