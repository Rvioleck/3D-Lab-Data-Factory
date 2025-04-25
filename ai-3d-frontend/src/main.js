import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { Modal, Button, Toast } from './components/common'

// 导入样式
import '@/assets/styles.css'

const app = createApp(App)

// 注册全局组件
app.component('Modal', Modal)
app.component('AppButton', Button)
app.component('Toast', Toast)

// 使用插件
app.use(createPinia())
app.use(router)

app.mount('#app')