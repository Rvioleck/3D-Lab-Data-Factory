# 3D-Lab Data Factory Frontend 项目

这是一个用于测试 3D-Lab Data Factory Backend 接口的前端项目，特别关注流式响应功能的正确性。项目已经进行了全面重构，提高了代码质量和可维护性。

## 功能特点

- 用户登录/注册
- 聊天会话管理
- 聊天对话界面
- 流式响应实时显示
- 健康检查测试
- 3D模型展示

## 技术栈

- Vue 3
- Pinia (状态管理)
- Vue Router 4
- Bootstrap 5
- Axios
- Three.js
- Vite

## 项目结构

```

```

## 安装与运行

1. 安装依赖：

```bash
npm install
```

2. 启动开发服务器：

```bash
npm run dev
```

3. 构建生产版本：

```bash
npm run build
```

## 流式响应测试

本项目重点测试后端的流式响应功能，实现了以下特性：

1. 使用 fetch API 和 ReadableStream 处理流式响应
2. 实时显示流式响应内容，带有打字机效果
3. 正确处理流式响应的开始、数据块和结束事件
4. 在流式响应过程中禁用输入，防止并发请求

## 代理配置

项目已配置代理，将 `/api` 开头的请求转发到后端服务：

```javascript
// vite.config.js
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8123',
        changeOrigin: true
      }
    }
  }
})
```

## 项目重构说明

本项目已经进行了全面重构，主要包括以下几个方面：

### 1. 状态管理迁移：从 Vuex 到 Pinia

- 将原有的 Vuex 存储迁移到了 Pinia，提高了代码的可维护性和性能
- 创建了三个主要的 Pinia 存储：
  - `userStore`：用户认证和信息管理
  - `chatStore`：聊天会话和消息管理
  - `reconstructionStore`：3D 重建任务管理
- 添加了兼容层(`storeCompat.js`)，确保平滑过渡，支持旧组件继续使用类 Vuex 接口

### 2. API 层标准化

- 创建了统一的 API 客户端(`apiClient.js`)，提供一致的接口
- 移除了冗余的 console.log 语句
- 实现了统一的错误处理机制
- 支持 RESTful 请求和流式请求

### 3. 工具函数增强

- 添加了错误处理工具(`errorHandler.js`)
- 添加了日期时间格式化工具(`dateTime.js`)
- 添加了 Markdown 解析工具(`markdown.js`)
- 增强了性能优化工具(`performance.js`)
- 添加了字符串处理工具(`string.js`)
- 添加了验证工具(`validation.js`)
- 添加了 Toast 通知工具(`toast.js`)

### 4. UI 组件优化

- 重新设计了登录页面，采用全屏背景+浮动登录框的现代设计
- 优化了 3D 模型展示效果
- 添加了响应式设计，适配不同屏幕尺寸
- 创建了通用组件：Modal, Button, Toast 等

### 5. 全局错误处理

- 实现了全局错误处理系统
- 添加了更好的错误日志记录
- 改进了用户错误反馈机制

### 6. 新的样式系统

项目采用了模块化的CSS样式系统，包含以下文件：

- `main.css` - 主样式文件，导入其他所有样式模块
- `styles.css` - 基础样式和变量定义
- `typography.css` - 文本和排版相关样式
- `components.css` - 通用组件样式（按钮、卡片、表单等）
- `layouts.css` - 布局相关样式
- `effects.css` - 特效和动画样式

#### 主要特点：

1. **现代科技风格**
   - 采用现代科技风格的配色方案
   - 主色调：科技蓝 (#3a86ff)
   - 辅助色：活力紫 (#8338ec)
   - 强调色：活力橙 (#ff9f1c)

2. **暗色主题支持**
   - 完全支持暗色模式
   - 自动切换颜色、背景和文本样式

3. **玻璃态设计**
   - 支持玻璃态效果的卡片和按钮
   - 模糊背景和半透明效果

4. **3D效果**
   - 3D卡片悬浮效果
   - 3D按钮效果
   - 3D文本效果

5. **动画和过渡**
   - 页面过渡动画
   - 元素进入/离开动画
   - 悬浮和点击效果

6. **响应式设计**
   - 全面的响应式布局
   - 自适应网格系统
   - 移动设备优化

## 使用新的存储

### 使用 Pinia 存储

```js
// 在组件中使用 Pinia 存储
import { useUserStore } from '@/stores/user'

export default {
  setup() {
    const userStore = useUserStore()

    // 直接访问状态
    console.log(userStore.user)

    // 调用操作
    userStore.login(credentials)

    return {
      // 暴露给模板
      user: userStore.user
    }
  }
}
```

### 使用兼容层（旧组件）

```js
// 在旧组件中使用兼容层
import { useStore } from '@/utils/storeCompat'

export default {
  setup() {
    const store = useStore()

    // Vuex 风格的访问
    console.log(store.getters['user/currentUser'])

    // Vuex 风格的操作
    store.dispatch('user/login', credentials)

    return {
      // 暴露给模板
      user: store.getters['user/currentUser']
    }
  }
}
```

## 注意事项

1. 新组件应直接使用 Pinia 存储，而不是兼容层
2. 兼容层仅用于确保现有组件在迁移期间继续工作
3. 逐步将所有组件迁移到直接使用 Pinia 存储
4. 确保后端服务已启动并运行在 `http://localhost:8123`
5. 如果后端服务地址有变化，请修改 `vite.config.js` 中的代理配置
6. 流式响应测试需要后端正确实现 SSE (Server-Sent Events) 协议
7. 详细API文档请参考后端项目中的 `API.md`