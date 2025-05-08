<template>
  <nav class="navbar" :class="{ 'scrolled': isScrolled, 'dark-theme': isDarkTheme }">
    <div class="container">
      <div class="navbar-content">
        <!-- 品牌标志 -->
        <router-link class="navbar-brand" to="/home">
          <div class="brand-logo">
            <i class="bi bi-box-fill"></i>
          </div>
          <span class="brand-text">AI 3D 平台</span>
        </router-link>

        <!-- 导航链接 -->
        <div class="navbar-links" :class="{ 'active': menuOpen }">
          <router-link to="/home" class="nav-link" @click="closeMenu">
            <i class="bi bi-house"></i>
            <span>首页</span>
          </router-link>

          <router-link to="/images" class="nav-link" @click="closeMenu">
            <i class="bi bi-image"></i>
            <span>图片库</span>
          </router-link>

          <router-link to="/models" class="nav-link" @click="closeMenu">
            <i class="bi bi-box"></i>
            <span>模型库</span>
          </router-link>

          <router-link v-if="isAdmin" :to="{ path: '/chat' }" class="nav-link" @click="closeMenu">
            <i class="bi bi-chat-dots"></i>
            <span>AI聊天</span>
          </router-link>

          <router-link v-if="isAdmin" :to="{ path: '/admin/users' }" class="nav-link" @click="closeMenu">
            <i class="bi bi-people"></i>
            <span>用户管理</span>
          </router-link>
        </div>

        <!-- 右侧用户菜单 -->
        <div class="navbar-actions">
          <!-- 主题切换按钮 -->
          <button class="theme-toggle" @click="toggleTheme">
            <i class="bi" :class="isDarkTheme ? 'bi-sun' : 'bi-moon'"></i>
          </button>

          <!-- 未登录状态 -->
          <router-link v-if="!isLoggedIn" to="/login" class="login-button">
            <i class="bi bi-box-arrow-in-right"></i>
            <span>登录</span>
          </router-link>

          <!-- 已登录状态 -->
          <div v-if="isLoggedIn" class="user-menu">
            <div class="user-avatar" @click="toggleUserMenu">
              <i class="bi bi-person-circle"></i>
              <span class="user-name">{{ currentUser.userName || currentUser.userAccount }}</span>
              <i class="bi bi-chevron-down"></i>
            </div>

            <div class="user-dropdown" v-if="userMenuOpen" @click.stop>
              <div class="dropdown-header">
                <div class="dropdown-user-info">
                  <i class="bi bi-person-circle"></i>
                  <div>
                    <div class="dropdown-user-name">{{ currentUser.userName || currentUser.userAccount }}</div>
                    <div class="dropdown-user-role" :class="roleClass">{{ roleText }}</div>
                  </div>
                </div>
              </div>

              <div class="dropdown-body">
                <router-link to="/profile" class="dropdown-item" @click="closeUserMenu">
                  <i class="bi bi-person"></i>
                  <span>个人资料</span>
                </router-link>

                <router-link to="/settings" class="dropdown-item" @click="closeUserMenu">
                  <i class="bi bi-gear"></i>
                  <span>设置</span>
                </router-link>

                <router-link v-if="isAdmin" :to="{ path: '/admin/dashboard' }" class="dropdown-item" @click="closeUserMenu">
                  <i class="bi bi-speedometer2"></i>
                  <span>管理控制台</span>
                </router-link>
              </div>

              <div class="dropdown-footer">
                <a href="#" class="dropdown-item logout-item" @click="handleLogout">
                  <i class="bi bi-box-arrow-right"></i>
                  <span>退出登录</span>
                </a>
              </div>
            </div>
          </div>

          <!-- 移动端菜单按钮 -->
          <button class="menu-toggle" @click="toggleMenu">
            <i class="bi" :class="menuOpen ? 'bi-x-lg' : 'bi-list'"></i>
          </button>
        </div>
      </div>
    </div>
  </nav>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useStore } from '@/utils/storeCompat'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

export default {
  name: 'Navbar',
  props: {
    isDarkTheme: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle-theme'],
  setup(props, { emit }) {
    const store = useStore()
    const userStore = useUserStore()
    const router = useRouter()

    const isScrolled = ref(false)
    const menuOpen = ref(false)
    const userMenuOpen = ref(false)

    const currentUser = computed(() => store.getters['user/currentUser'] || {})
    const isLoggedIn = computed(() => store.getters['user/isLoggedIn'])
    const isAdmin = computed(() => {
      console.log('Navbar isAdmin check:', store.getters['user/isAdmin'])
      return store.getters['user/isAdmin']
    })

    const roleText = computed(() => {
      if (!currentUser.value) return '未登录'

      switch (currentUser.value.userRole) {
        case 'admin': return '管理员'
        case 'user': return '普通用户'
        case 'ban': return '已封禁'
        default: return '未知角色'
      }
    })

    const roleClass = computed(() => {
      if (!currentUser.value) return 'role-unknown'

      switch (currentUser.value.userRole) {
        case 'admin': return 'role-admin'
        case 'user': return 'role-user'
        case 'ban': return 'role-banned'
        default: return 'role-unknown'
      }
    })

    // 监听滚动事件
    const handleScroll = () => {
      isScrolled.value = window.scrollY > 20
    }

    // 切换主题
    const toggleTheme = () => {
      emit('toggle-theme')
    }

    // 切换菜单
    const toggleMenu = () => {
      menuOpen.value = !menuOpen.value
      if (menuOpen.value) {
        userMenuOpen.value = false
      }
    }

    // 关闭菜单
    const closeMenu = () => {
      menuOpen.value = false
    }

    // 切换用户菜单
    const toggleUserMenu = () => {
      userMenuOpen.value = !userMenuOpen.value
      if (userMenuOpen.value) {
        menuOpen.value = false
      }
    }

    // 关闭用户菜单
    const closeUserMenu = () => {
      userMenuOpen.value = false
    }

    // 处理登出
    const handleLogout = async () => {
      try {
        await userStore.logout()
        closeUserMenu()
        // 强制刷新导航栏状态
        userStore.clearUserData()
        // 重定向到登录页面
        router.push('/login')
        if (window.$toast) {
          window.$toast.success('登出成功')
        }
      } catch (error) {
        console.error('登出失败:', error)
        // 即使登出失败，也清除用户数据并重定向
        userStore.clearUserData()
        router.push('/login')
        if (window.$toast) {
          window.$toast.error('登出失败')
        }
      }
    }

    // 点击外部关闭菜单
    const handleClickOutside = (event) => {
      const userMenu = document.querySelector('.user-menu')
      if (userMenu && !userMenu.contains(event.target) && userMenuOpen.value) {
        userMenuOpen.value = false
      }
    }

    // 监听登录状态变化
    watch(isLoggedIn, (newValue) => {
      console.log('登录状态变化:', newValue)
      // 如果用户登出，强制刷新导航栏状态
      if (!newValue) {
        // 确保清除了用户数据
        userStore.clearUserData()
      }
    })

    onMounted(() => {
      window.addEventListener('scroll', handleScroll)
      document.addEventListener('click', handleClickOutside)
      handleScroll() // 初始检查

      // 初始化时检查用户状态
      if (!isLoggedIn.value && localStorage.getItem('token')) {
        // 如果有token但未登录，尝试获取用户信息
        userStore.fetchCurrentUser().catch(() => {
          // 如果获取失败，确保清除用户数据
          userStore.clearUserData()
        })
      }
    })

    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll)
      document.removeEventListener('click', handleClickOutside)
    })

    return {
      isScrolled,
      menuOpen,
      userMenuOpen,
      currentUser,
      isLoggedIn,
      isAdmin,
      roleText,
      roleClass,
      toggleTheme,
      toggleMenu,
      closeMenu,
      toggleUserMenu,
      closeUserMenu,
      handleLogout
    }
  }
}
</script>

<style scoped>
.navbar {
  --navbar-height: 70px;
  --navbar-padding: 0.75rem 0;
  --navbar-bg: rgba(255, 255, 255, 0.95);
  --navbar-border: rgba(0, 0, 0, 0.05);
  --navbar-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  --navbar-text: #333;
  --navbar-link: #555;
  --navbar-link-hover: var(--primary-color, #4f46e5);
  --navbar-link-active-bg: rgba(79, 70, 229, 0.1);
  --navbar-dark-bg: rgba(30, 30, 40, 0.95);
  --navbar-dark-border: rgba(255, 255, 255, 0.05);
  --navbar-dark-text: #fff;
  --navbar-dark-link: #ddd;
  --navbar-dark-link-hover: #fff;
  --navbar-dark-link-active-bg: rgba(255, 255, 255, 0.1);

  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: var(--navbar-padding);
  background-color: var(--navbar-bg);
  border-bottom: 1px solid var(--navbar-border);
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.new-navbar.scrolled {
  --navbar-padding: 0.5rem 0;
  box-shadow: var(--navbar-shadow);
}

.new-navbar.dark-theme {
  --navbar-bg: var(--navbar-dark-bg);
  --navbar-border: var(--navbar-dark-border);
  --navbar-text: var(--navbar-dark-text);
  --navbar-link: var(--navbar-dark-link);
  --navbar-link-hover: var(--navbar-dark-link-hover);
  --navbar-link-active-bg: var(--navbar-dark-link-active-bg);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--navbar-height);
}

/* 品牌标志 */
.navbar-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--navbar-text);
  font-weight: 600;
  font-size: 1.25rem;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--primary-color, #4f46e5);
  color: white;
  border-radius: 10px;
  margin-right: 0.75rem;
  transition: transform 0.3s ease;
}

.navbar-brand:hover .brand-logo {
  transform: rotate(10deg);
}

/* 导航链接 */
.navbar-links {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  color: var(--navbar-link);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.3s ease;
  font-weight: 500;
}

.nav-link:hover {
  color: var(--navbar-link-hover);
}

.nav-link.router-link-active {
  color: var(--navbar-link-hover);
  background-color: var(--navbar-link-active-bg);
}

/* 用户菜单 */
.navbar-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.theme-toggle {
  background: transparent;
  border: none;
  color: var(--navbar-link);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
}

.theme-toggle:hover {
  background-color: var(--navbar-link-active-bg);
  color: var(--navbar-link-hover);
}

.login-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  background-color: var(--primary-color, #4f46e5);
  color: white;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-button:hover {
  background-color: var(--primary-hover, #4338ca);
  transform: translateY(-2px);
}

.user-menu {
  position: relative;
}

.user-avatar {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  background-color: var(--navbar-link-active-bg);
}

.user-name {
  font-weight: 500;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 0.5rem);
  right: 0;
  width: 280px;
  background-color: var(--navbar-bg);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
  border: 1px solid var(--navbar-border);
}

.dropdown-header {
  padding: 1rem;
  border-bottom: 1px solid var(--navbar-border);
}

.dropdown-user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.dropdown-user-info i {
  font-size: 2rem;
}

.dropdown-user-name {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.dropdown-user-role {
  display: inline-block;
  padding: 0.125rem 0.5rem;
  border-radius: 50px;
  font-size: 0.75rem;
  font-weight: 500;
}

.role-admin {
  background-color: var(--primary-light, #e0e7ff);
  color: var(--primary-color, #4f46e5);
}

.role-user {
  background-color: var(--success-light, #d1fae5);
  color: var(--success-color, #10b981);
}

.role-banned {
  background-color: var(--error-light, #fee2e2);
  color: var(--error-color, #ef4444);
}

.role-unknown {
  background-color: var(--neutral-200, #e5e7eb);
  color: var(--neutral-600, #4b5563);
}

.dropdown-body {
  padding: 0.5rem 0;
}

.dropdown-footer {
  padding: 0.5rem 0;
  border-top: 1px solid var(--navbar-border);
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: var(--navbar-link);
  text-decoration: none;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background-color: var(--navbar-link-active-bg);
  color: var(--navbar-link-hover);
}

.logout-item {
  color: #ef4444;
}

.logout-item:hover {
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

/* 移动端菜单按钮 */
.menu-toggle {
  display: none;
  background: transparent;
  border: none;
  color: var(--navbar-link);
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0.5rem;
  transition: all 0.3s ease;
}

/* 响应式调整 */
@media (max-width: 991.98px) {
  .navbar-links {
    position: fixed;
    top: var(--navbar-height);
    left: 0;
    right: 0;
    flex-direction: column;
    background-color: var(--navbar-bg);
    padding: 1rem;
    gap: 0.5rem;
    box-shadow: 0 10px 15px rgba(0, 0, 0, 0.1);
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
    z-index: 999;
  }

  .navbar-links.active {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .nav-link {
    width: 100%;
    padding: 0.75rem 1rem;
  }

  .menu-toggle {
    display: block;
  }

  .user-name {
    display: none;
  }
}

@media (max-width: 575.98px) {
  .user-dropdown {
    width: 250px;
    right: -70px;
  }

  .user-dropdown::before {
    content: '';
    position: absolute;
    top: -8px;
    right: 80px;
    width: 16px;
    height: 16px;
    background-color: var(--navbar-bg);
    transform: rotate(45deg);
    border-top: 1px solid var(--navbar-border);
    border-left: 1px solid var(--navbar-border);
  }
}
</style>
