import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// 导入优化的样式
import './assets/animations.css'
import './assets/loading.css'

// 导入新设计样式
import './assets/new-theme.css'
import './assets/3d-effects.css'
import './assets/glassmorphism.css'

// 导入模态框修复样式
import './assets/disable-modal-backdrop.css'

// 导入模态框修复工具
import { setupModalFix, cleanupModals } from './utils/modalFix'

// 创建应用实例
const app = createApp(App)

// 导入懒加载指令
import lazyLoad from './directives/lazyLoad'

// 注册全局性能指令
app.directive('lazy', lazyLoad)

app.use(router)
app.use(store)

// 设置模态框修复
setupModalFix()

// 在路由变化时清除模态框背景
router.beforeEach((to, from, next) => {
  // 清除模态框背景
  cleanupModals()
  next()
})

app.mount('#app')