import { createRouter, createWebHistory } from 'vue-router'
import store from '../store'

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
    path: '/reconstruction/:imageId',
    name: 'ReconstructionWithImage',
    component: () => import('../views/ReconstructionView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/resources',
    name: 'Resources',
    redirect: '/images',
    meta: { requiresAuth: true }
  },
  {
    path: '/health',
    name: 'Health',
    component: () => import('../views/HealthView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    redirect: '/home',
    meta: { requiresAuth: true }
  },
  {
    path: '/admin/users',
    name: 'UserManagement',
    redirect: '/home',
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: '/chat',
    name: 'Chat',
    component: () => import('../views/ChatView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true }
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

// 导航守卫
router.beforeEach(async (to, from, next) => {
  console.log('路由守卫: 从', from.path, '到', to.path)

  // 检查用户登录状态
  const isLoggedIn = store.getters['user/isLoggedIn']
  const hasToken = !!localStorage.getItem('token')

  console.log('当前登录状态:', isLoggedIn ? '已登录' : '未登录', 'Token状态:', hasToken ? '存在' : '不存在')

  // 如果有token但没有用户信息，尝试获取用户信息
  if (hasToken && !isLoggedIn) {
    try {
      console.log('有token但没有用户信息，尝试获取用户信息')
      await store.dispatch('user/fetchCurrentUser')
      // 重新检查登录状态
      const updatedLoginStatus = store.getters['user/isLoggedIn']
      console.log('获取用户信息后的登录状态:', updatedLoginStatus ? '已登录' : '未登录')
    } catch (error) {
      console.error('获取用户信息失败:', error)
      localStorage.removeItem('token')
    }
  }

  // 重新获取登录状态（可能已经更新）
  const finalLoginStatus = store.getters['user/isLoggedIn']
  const isAdmin = store.getters['user/isAdmin']

  // 如果需要登录但用户未登录，重定向到登录页面
  if (to.meta.requiresAuth && !finalLoginStatus) {
    console.log('页面需要登录权限但用户未登录，重定向到登录页面')
    next('/login')
  }
  // 如果需要管理员权限但用户不是管理员，重定向到首页
  else if (to.meta.requiresAdmin && !isAdmin) {
    console.log('页面需要管理员权限但用户不是管理员，重定向到首页')
    next('/home')
  }
  // 如果用户已登录但访问登录页面，重定向到主页
  else if (finalLoginStatus && (to.path === '/login' || to.path === '/register')) {
    console.log('用户已登录但访问登录/注册页面，重定向到主页')
    next('/home')
  } else {
    console.log('允许访问页面:', to.path)
    next()
  }
})

export default router
