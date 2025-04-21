<template>
  <nav class="modern-navbar glass-navbar" :class="{ 'scrolled': isScrolled }">
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
          
          <router-link v-if="isAdmin" to="/chat" class="nav-link" @click="closeMenu">
            <i class="bi bi-chat-dots"></i>
            <span>AI聊天</span>
          </router-link>
          
          <router-link v-if="isAdmin" to="/admin/users" class="nav-link" @click="closeMenu">
            <i class="bi bi-people"></i>
            <span>用户管理</span>
          </router-link>
        </div>
        
        <!-- 右侧用户菜单 -->
        <div class="navbar-actions">
          <!-- 主题切换按钮 -->
          <button class="theme-toggle icon-3d" @click="toggleTheme">
            <i class="bi" :class="isDarkTheme ? 'bi-sun' : 'bi-moon'"></i>
          </button>
          
          <!-- 未登录状态 -->
          <router-link v-if="!isLoggedIn" to="/login" class="login-button glass-button">
            <i class="bi bi-box-arrow-in-right"></i>
            <span>登录</span>
          </router-link>
          
          <!-- 已登录状态 -->
          <div v-if="isLoggedIn" class="user-menu">
            <div class="user-avatar" @click="toggleUserMenu">
              <i class="bi bi-person-circle"></i>
            </div>
            
            <div class="user-dropdown" v-if="userMenuOpen">
              <div class="user-info">
                <div class="user-avatar-lg">
                  <i class="bi bi-person-circle"></i>
                </div>
                <div class="user-details">
                  <h5>{{ currentUser.userName || currentUser.userAccount }}</h5>
                  <div class="badge" :class="roleClass">{{ roleText }}</div>
                </div>
              </div>
              
              <div class="dropdown-divider"></div>
              
              <router-link to="/profile" class="dropdown-item" @click="closeUserMenu">
                <i class="bi bi-person"></i>
                <span>个人资料</span>
              </router-link>
              
              <router-link to="/settings" class="dropdown-item" @click="closeUserMenu">
                <i class="bi bi-gear"></i>
                <span>设置</span>
              </router-link>
              
              <div class="dropdown-divider"></div>
              
              <a href="#" class="dropdown-item" @click="handleLogout">
                <i class="bi bi-box-arrow-right"></i>
                <span>退出登录</span>
              </a>
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
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'

export default {
  name: 'ModernNavbar',
  props: {
    isDarkTheme: {
      type: Boolean,
      default: false
    }
  },
  emits: ['toggle-theme'],
  setup(props, { emit }) {
    const store = useStore()
    const router = useRouter()
    
    const isScrolled = ref(false)
    const menuOpen = ref(false)
    const userMenuOpen = ref(false)
    
    const currentUser = computed(() => store.getters['user/currentUser'] || {})
    const isLoggedIn = computed(() => store.getters['user/isLoggedIn'])
    const isAdmin = computed(() => store.getters['user/isAdmin'])
    
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
      if (!currentUser.value) return 'bg-secondary'

      switch (currentUser.value.userRole) {
        case 'admin': return 'bg-primary'
        case 'user': return 'bg-success'
        case 'ban': return 'bg-danger'
        default: return 'bg-secondary'
      }
    })
    
    // 监听滚动事件
    const handleScroll = () => {
      isScrolled.value = window.scrollY > 50
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
        await store.dispatch('user/logout')
        closeUserMenu()
        router.push('/login')
      } catch (error) {
        console.error('登出失败:', error)
      }
    }
    
    // 点击外部关闭菜单
    const handleClickOutside = (event) => {
      const userMenu = document.querySelector('.user-menu')
      if (userMenu && !userMenu.contains(event.target) && userMenuOpen.value) {
        userMenuOpen.value = false
      }
    }
    
    onMounted(() => {
      window.addEventListener('scroll', handleScroll)
      document.addEventListener('click', handleClickOutside)
      handleScroll() // 初始检查
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
.modern-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  padding: 1rem 0;
  transition: all 0.3s ease;
}

.modern-navbar.scrolled {
  padding: 0.5rem 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.navbar-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* 品牌标志 */
.navbar-brand {
  display: flex;
  align-items: center;
  text-decoration: none;
  color: var(--text-primary);
  font-weight: 600;
  font-size: 1.25rem;
}

.brand-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background: var(--primary-color);
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
  gap: 1rem;
}

.nav-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  color: var(--text-primary);
  text-decoration: none;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--primary-color);
  background-color: rgba(var(--primary-rgb), 0.1);
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
  color: var(--text-primary);
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
}

.theme-toggle:hover {
  background-color: rgba(var(--primary-rgb), 0.1);
  color: var(--primary-color);
}

.login-button {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  border-radius: var(--radius-md);
  text-decoration: none;
}

.user-menu {
  position: relative;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(var(--primary-rgb), 0.1);
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.user-avatar:hover {
  background-color: rgba(var(--primary-rgb), 0.2);
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 0.75rem);
  right: 0;
  width: 280px;
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  z-index: 1000;
  animation: dropdown-fade 0.2s ease;
}

.user-info {
  padding: 1.25rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.user-avatar-lg {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: rgba(var(--primary-rgb), 0.1);
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
}

.user-details h5 {
  margin-bottom: 0.25rem;
  font-weight: 600;
}

.dropdown-divider {
  height: 1px;
  background-color: var(--border-color);
  margin: 0;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1.25rem;
  color: var(--text-primary);
  text-decoration: none;
  transition: all 0.2s ease;
}

.dropdown-item:hover {
  background-color: rgba(var(--primary-rgb), 0.1);
  color: var(--primary-color);
}

/* 移动端菜单按钮 */
.menu-toggle {
  display: none;
  background: transparent;
  border: none;
  color: var(--text-primary);
  font-size: 1.5rem;
  cursor: pointer;
}

@keyframes dropdown-fade {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式调整 */
@media (max-width: 992px) {
  .navbar-links {
    position: fixed;
    top: 70px;
    left: 0;
    right: 0;
    background-color: var(--bg-primary);
    flex-direction: column;
    padding: 1rem;
    gap: 0;
    box-shadow: var(--shadow-md);
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
}

@media (max-width: 576px) {
  .brand-text {
    display: none;
  }
  
  .user-dropdown {
    width: 250px;
  }
}
</style>
