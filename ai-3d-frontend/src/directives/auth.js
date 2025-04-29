/**
 * 权限控制指令
 * 用于基于用户角色和资源所有权控制元素的显示和交互
 * 
 * 使用示例：
 * 
 * 1. 需要登录才能访问：
 * <button v-auth="{ requiresAuth: true }">下载</button>
 * 
 * 2. 需要管理员权限：
 * <button v-auth="{ requiresAdmin: true }">删除</button>
 * 
 * 3. 需要资源所有者权限：
 * <button v-auth="{ requiresOwner: true, resourceOwnerId: model.userId }">编辑</button>
 * 
 * 4. 自定义提示消息：
 * <button v-auth="{ requiresAuth: true, message: '请登录后下载' }">下载</button>
 * 
 * 5. 完全隐藏元素：
 * <button v-auth="{ requiresAdmin: true, hide: true }">管理员功能</button>
 */

import { useUserStore } from '@/stores/user'

export const authDirective = {
  mounted(el, binding) {
    const { value } = binding
    if (!value) return

    const userStore = useUserStore()
    const isLoggedIn = userStore.isLoggedIn
    const isAdmin = userStore.isAdmin
    const userId = userStore.user?.id

    // 检查权限
    const hasNoPermission = (
      (value.requiresAuth && !isLoggedIn) || 
      (value.requiresAdmin && !isAdmin) ||
      (value.requiresOwner && value.resourceOwnerId !== userId && !isAdmin)
    )

    if (hasNoPermission) {
      // 如果没有权限，根据配置隐藏元素或禁用交互
      if (value.hide) {
        el.style.display = 'none'
      } else {
        el.disabled = true
        el.classList.add('disabled')
        el.style.cursor = 'not-allowed'
        el.style.opacity = '0.6'
        
        // 添加提示信息
        el.title = value.message || '没有权限执行此操作'
        
        // 阻止点击事件
        el.addEventListener('click', (e) => {
          e.preventDefault()
          e.stopPropagation()
          if (window.$toast && value.showToast !== false) {
            window.$toast.warning(value.message || '没有权限执行此操作')
          }
          return false
        }, true)
      }
    }
  }
}

export default {
  install(app) {
    app.directive('auth', authDirective)
  }
}
