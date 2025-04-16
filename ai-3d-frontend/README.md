# AI 3D Frontend 项目

这是一个用于测试 AI 3D Backend 接口的前端项目，特别关注流式响应功能的正确性。

## 功能特点

- 用户登录/注册
- 聊天会话管理
- 聊天对话界面
- 流式响应实时显示
- 健康检查测试

## 技术栈

- Vue 3
- Vuex 4
- Vue Router 4
- Bootstrap 5
- Axios
- Vite

## 项目结构

```
├── index.html
├── package.json
├── vite.config.js
└── src/
    ├── main.js
    ├── App.vue
    ├── router/
    │   └── index.js
    ├── store/
    │   ├── index.js
    │   └── modules/
    │       ├── user.js
    │       └── chat.js
    ├── api/
    │   ├── user.js
    │   ├── chat.js
    │   └── health.js
    ├── views/
    │   ├── LoginView.vue
    │   ├── RegisterView.vue
    │   ├── ChatView.vue
    │   └── HealthView.vue
    └── components/
        ├── ChatSession.vue
        ├── ChatMessage.vue
        └── StreamingResponse.vue
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

## 注意事项

1. 确保后端服务已启动并运行在 `http://localhost:8123`
2. 如果后端服务地址有变化，请修改 `vite.config.js` 中的代理配置
3. 流式响应测试需要后端正确实现 SSE (Server-Sent Events) 协议
4. 详细API文档请参考后端项目中的 `API.md`