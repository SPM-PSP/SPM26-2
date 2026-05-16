import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/problems' },
    {
      path: '/',
      component: () => import('@/layouts/AuthLayout.vue'),
      children: [
        { path: 'login', name: 'login', component: () => import('@/views/LoginView.vue') },
        { path: 'register', name: 'register', component: () => import('@/views/RegisterView.vue') },
      ],
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      children: [
        { path: 'problems', name: 'problems', component: () => import('@/views/ProblemListView.vue') },
        {
          path: 'problems/:id',
          name: 'problem-detail',
          component: () => import('@/views/ProblemDetailView.vue'),
          props: true,
        },
        {
          path: 'submissions',
          name: 'submissions',
          component: () => import('@/views/SubmissionsView.vue'),
          meta: { requiresAuth: true },
        },
        {
          path: 'submissions/:id',
          name: 'submission-detail',
          component: () => import('@/views/SubmissionDetailView.vue'),
          props: true,
          meta: { requiresAuth: true },
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('@/views/ProfileView.vue'),
          meta: { requiresAuth: true },
        },
        { path: 'ai', name: 'ai-lab', component: () => import('@/views/AiLabView.vue') },
      ],
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      children: [
        { path: '', redirect: { name: 'admin-users' } },
        { path: 'users', name: 'admin-users', component: () => import('@/views/admin/AdminUsersView.vue') },
        {
          path: 'categories',
          name: 'admin-categories',
          component: () => import('@/views/admin/AdminCategoriesView.vue'),
        },
        { path: 'problems', name: 'admin-problems', component: () => import('@/views/admin/AdminProblemsView.vue') },
        {
          path: 'problems/new',
          name: 'admin-problem-new',
          component: () => import('@/views/admin/AdminProblemEditView.vue'),
        },
        {
          path: 'problems/:id',
          name: 'admin-problem-edit',
          component: () => import('@/views/admin/AdminProblemEditView.vue'),
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if ((to.name === 'login' || to.name === 'register') && auth.isLoggedIn) {
    const redirect = (to.query.redirect as string) || '/problems'
    return { path: redirect }
  }
  if (to.meta.requiresAdmin) {
    if (!auth.isLoggedIn) {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
    if (!auth.isAdmin) {
      return { path: '/problems' }
    }
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
})

export default router
