import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

// Define routes
const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('../views/HomeView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/RegisterView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/images',
    name: 'ImageLibrary',
    component: () => import('../views/ImageLibraryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/images/:id',
    name: 'ImageDetail',
    component: () => import('../views/ImageDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/models',
    name: 'ModelLibrary',
    component: () => import('../views/ModelLibraryView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/models/:id',
    name: 'ModelDetail',
    component: () => import('../views/ModelDetailView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/reconstruction',
    name: 'Reconstruction',
    component: () => import('../views/ReconstructionView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/health',
    name: 'Health',
    component: () => import('../views/HealthView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    component: () => import('../views/admin/UserManagementView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true }
  },
  {
    // 捕获所有未匹配的路由，重定向到首页
    path: '/:pathMatch(.*)*',
    redirect: '/home'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guards
router.beforeEach(async (to, from, next) => {
  // Get user store
  const userStore = useUserStore()

  // Check login status
  const isLoggedIn = userStore.isLoggedIn
  const hasToken = !!localStorage.getItem('token')

  // If has token but not logged in, try to fetch user info
  if (hasToken && !isLoggedIn) {
    try {
      await userStore.fetchCurrentUser()
    } catch (error) {
      // Clear token on error
      localStorage.removeItem('token')
    }
  }

  // Get final login status
  const finalLoginStatus = userStore.isLoggedIn
  const isAdmin = userStore.isAdmin

  console.log('Router guard - isLoggedIn:', finalLoginStatus)
  console.log('Router guard - isAdmin:', isAdmin)
  console.log('Router guard - user:', userStore.user)

  // Handle route access based on auth requirements
  if (to.meta.requiresAuth && !finalLoginStatus) {
    // Redirect to login if auth required but not logged in
    next('/login')
    // Show toast notification
    if (window.$toast) {
      window.$toast.warning('Please log in to access this page')
    }
  } else if (to.meta.requiresAdmin && !isAdmin) {
    // Redirect to home if admin required but not admin
    next('/home')
    // Show toast notification
    if (window.$toast) {
      window.$toast.warning('Admin access required')
    }
  } else if (finalLoginStatus && (to.path === '/login' || to.path === '/register')) {
    // Redirect to home if logged in but accessing login/register
    next('/home')
  } else {
    // Allow access
    next()
  }
})

export default router
