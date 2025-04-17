# AI 3D Platform API 文档

## 基础信息

- 基础路径: `/api`
- 服务端口: 8123
- 所有接口返回格式统一为：
```json
{
    "code": 0,       // 状态码，0表示成功
    "data": {},      // 数据
    "message": "ok"  // 消息
}
```

## 权限控制

本系统实现了基于角色的权限控制，主要有以下角色：

| 角色 | 权限说明 |
| ------ | ---- |
| user | 普通用户，可以访问基本功能 |
| admin | 管理员，可以访问所有功能，包括用户管理和聊天功能 |
| ban | 被封禁的用户，无法访问系统功能 |

**注意：当前聊天相关功能仅允许管理员访问。**

## 错误码说明

| 错误码 | 说明 |
| ------ | ---- |
| 0 | 成功 |
| 40000 | 请求参数错误 |
| 40100 | 未登录 |
| 40101 | 无权限 |
| 40300 | 禁止访问 |
| 40400 | 请求数据不存在 |
| 50000 | 系统内部异常 |
| 50001 | 操作失败 |

## 1. 用户接口

### 1.1 用户注册
- 请求路径: `/user/register`
- 请求方法: POST
- 请求体:
```json
{
    "userAccount": "string",    // 用户账号
    "userPassword": "string",   // 用户密码
    "checkPassword": "string"   // 确认密码
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": "1234567890123456789",    // 用户ID (字符串类型的雪花ID)
    "message": "ok"
}
```

### 1.2 用户登录
- 请求路径: `/user/login`
- 请求方法: POST
- 请求体:
```json
{
    "userAccount": "string",    // 用户账号
    "userPassword": "string"    // 用户密码
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",  // 字符串类型的雪花ID
        "userAccount": "string",
        "userName": "string",
        "userAvatar": "string",
        "userProfile": "string",     // 用户简介
        "userRole": "string",       // 用户角色：user/admin/ban
        "createTime": "2023-01-01T12:00:00",  // 创建时间
        "updateTime": "2023-01-01T12:00:00"   // 更新时间
    },
    "message": "ok"
}
```

### 1.3 获取当前登录用户
- 请求路径: `/user/get/login`
- 请求方法: POST
- 响应数据: 同登录接口响应

### 1.4 用户退出
- 请求路径: `/user/logout`
- 请求方法: POST
- 响应数据:
```json
{
    "code": 0,
    "data": true,    // 退出成功返回 true
    "message": "ok"
}
```

### 1.5 创建用户（仅管理员可访问）
- 请求路径: `/user/create`
- 请求方法: POST
- 权限要求: 需要管理员权限
- 请求体:
```json
{
    "userAccount": "string",    // 用户账号
    "userPassword": "string",   // 用户密码
    "checkPassword": "string",  // 确认密码
    "userName": "string",       // 用户名（可选）
    "userAvatar": "string",     // 用户头像（可选）
    "userProfile": "string",    // 用户简介（可选）
    "userRole": "string"        // 用户角色（可选）：user/admin/ban
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": "1234567890123456789",    // 新创建的用户ID
    "message": "ok"
}
```

### 1.6 根据ID获取用户基本信息（仅管理员可访问）
- 请求路径: `/user/get`
- 请求方法: POST
- 权限要求: 需要管理员权限
- 请求体: 用户ID（数字）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",
        "userAccount": "string",
        "userName": "string",
        "userAvatar": "string",
        "userProfile": "string",
        "userRole": "string",
        "createTime": "2023-01-01T12:00:00",
        "updateTime": "2023-01-01T12:00:00"
    },
    "message": "ok"
}
```

### 1.7 根据ID获取用户详细信息（仅管理员可访问）
- 请求路径: `/user/detail`
- 请求方法: POST
- 权限要求: 需要管理员权限
- 请求体: 用户ID（数字）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",
        "userAccount": "string",
        "userName": "string",
        "userAvatar": "string",
        "userProfile": "string",
        "userRole": "string",
        "createTime": "2023-01-01T12:00:00",
        "updateTime": "2023-01-01T12:00:00",
        "editTime": "2023-01-01T12:00:00",
        "lastLoginTime": "2023-01-01T12:00:00",
        "userStatus": "active"
    },
    "message": "ok"
}
```

### 1.8 获取所有用户信息（仅管理员可访问）
- 请求路径: `/user/list`
- 请求方法: POST
- 权限要求: 需要管理员权限
- 响应数据:
```json
{
    "code": 0,
    "data": [
        {
            "id": "1234567890123456789",
            "userAccount": "admin",
            "userName": "管理员",
            "userAvatar": "https://example.com/avatar.jpg",
            "userProfile": "系统管理员",
            "userRole": "admin",
            "createTime": "2023-01-01T12:00:00",
            "updateTime": "2023-01-01T12:00:00"
        },
        // ... 更多用户
    ],
    "message": "ok"
}
```

## 2. 聊天接口（仅管理员可访问）

**注意：所有聊天相关接口当前仅允许管理员访问。这是通过在 ChatController 类上添加 `@AuthCheck(mustRole = "admin")` 注解实现的。**

### 2.1 创建会话
- 请求路径: `/chat/session`
- 请求方法: POST
- 请求参数:
  - sessionName: string (会话名称)
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",    // 字符串类型的雪花ID
        "userId": "9876543210987654321",  // 字符串类型的用户ID
        "sessionName": "string",
        "createTime": "2023-01-01T12:00:00",  // 创建时间
        "updateTime": "2023-01-01T12:00:00"   // 更新时间
    },
    "message": "ok"
}
```

### 2.2 获取会话列表
- 请求路径: `/chat/session/list`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": [{
        "id": "1234567890123456789",    // 字符串类型的雪花ID
        "userId": "9876543210987654321",  // 字符串类型的用户ID
        "sessionName": "string",
        "createTime": "2023-01-01T12:00:00",  // 创建时间
        "updateTime": "2023-01-01T12:00:00"   // 更新时间
    }],
    "message": "ok"
}
```

### 2.3 获取会话消息历史
- 请求路径: `/chat/message/{sessionId}`
- 请求方法: GET
- 路径参数:
  - sessionId: string (会话ID)
- 响应数据:
```json
{
    "code": 0,
    "data": [{
        "id": "1234567890123456789",    // 字符串类型的雪花ID
        "sessionId": "9876543210987654321",  // 字符串类型的会话ID
        "role": "string",            // user 或 assistant
        "content": "string",
        "createTime": "2023-01-01T12:00:00",  // 创建时间
        "updateTime": "2023-01-01T12:00:00"   // 更新时间
    }],
    "message": "ok"
}
```

### 2.4 发送消息（支持自动创建会话）
- 请求路径: `/chat/message`
- 请求方法: POST
- 请求体:
```json
{
    "sessionId": "1234567890123456789",   // 会话ID (字符串类型的雪花ID)，first=true时可不传
    "message": "string",                // 消息内容
    "first": false                      // 是否是首次消息，true表示自动创建会话，false表示使用已有会话
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",    // 字符串类型的雪花ID
        "sessionId": "9876543210987654321",  // 字符串类型的会话ID
        "role": "assistant",
        "content": "string",
        "createTime": "2023-01-01T12:00:00",  // 创建时间
        "updateTime": "2023-01-01T12:00:00"   // 更新时间
    },
    "message": "ok"
}
```

> 注意：当 `first=true` 时，会自动创建新会话并发送消息，此时 `sessionId` 参数可以不传。

### 2.5 删除会话
- 请求路径: `/chat/session/{sessionId}`
- 请求方法: DELETE
- 路径参数:
  - sessionId: string (会话ID)
- 响应数据:
```json
{
    "code": 0,
    "data": true,    // 删除成功返回true
    "message": "ok"
}
```

### 2.6 流式发送消息（支持自动创建会话）
- 请求路径: `/chat/stream`
- 请求方法: POST
- Content-Type: `application/json`
- Accept: `text/event-stream`
- 请求体:
```json
{
    "sessionId": "1234567890123456789",   // 会话ID (字符串类型的雪花ID)，first=true时可不传
    "message": "string",                // 消息内容
    "first": false                      // 是否是首次消息，true表示自动创建会话，false表示使用已有会话
}
```
- 响应格式: Server-Sent Events (SSE)
- 响应示例:
```
data: 你好

data: ，

data: 我是

data: AI助手

data: [DONE]
```
- 说明：
  - 响应会以流式方式返回，每个数据块以 `data: ` 开头
  - 最后会发送 `data: [DONE]` 表示响应结束
  - 客户端需要使用 EventSource 或 fetch API 来处理流式响应
  - 如果发生错误，连接会被关闭，客户端需要处理错误情况

> 注意：当 `first=true` 时，会自动创建新会话并流式发送消息，此时 `sessionId` 参数可以不传。

### 2.7 删除消息
- 请求路径: `/chat/message/{messageId}`
- 请求方法: DELETE
- 路径参数:
  - messageId: string (消息ID)
- 响应数据:
```json
{
    "code": 0,
    "data": true,    // 删除成功返回true
    "message": "ok"
}
```
- 说明：
  - 删除指定的消息
  - 可以删除任意类型的消息（用户消息或AI回复）
  - 删除后的消息不会出现在历史记录中
  - 重构后的删除逻辑只删除单条消息，不会影响其他消息

## 3. 健康检查接口

### 3.1 简单健康检查
- 请求路径: `/health`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": "OK",
    "message": "ok"
}
```

### 3.2 详细健康检查
- 请求路径: `/health/detail`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "appName": "string",
        "port": "string",
        "contextPath": "string",
        "jvmTotalMemory": "string",
        "jvmFreeMemory": "string",
        "jvmMaxMemory": "string",
        "availableProcessors": "number",
        "javaVersion": "string",
        "osName": "string",
        "osArch": "string",
        "osVersion": "string",
        "serverTime": "number"
    },
    "message": "ok"
}
```

## 4. 测试接口

### 4.1 测试Long类型序列化
- 请求路径: `/test/long-serialization`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "longObjectId": "123456789012345",    // 注意这里是字符串类型
        "longPrimitiveId": "987654321098765", // 注意这里是字符串类型
        "intId": 100,                       // 整型保持不变
        "stringId": "string-id-123",
        "object": {
            "id": "567890123456789",        // 对象中的Long类型也会转为字符串
            "name": "测试对象"
        }
    },
    "message": "ok"
}
```

## 消息合并机制

为了提高API调用效率，系统实现了自动合并连续同类型消息的功能：

1. 当发送多条连续的同类型消息时（如多条用户消息或多条AI回复），系统会自动将它们合并
2. 合并后的消息内容会以"\n\n"（两个换行符）连接
3. 合并机制只在调用DeepSeek API时生效，不会影响数据库中的消息存储
4. 这种机制可以减少API调用次数，降低成本并提高响应速度

## 注意事项

1. 所有需要用户认证的接口都需要先登录
2. 所有包含Long类型的ID字段在JSON序列化时都会自动转换为字符串类型，以避免JavaScript中的精度丢失问题
3. 权限控制说明：
   - 系统使用基于注解的权限控制机制
   - 当前聊天相关功能仅允许管理员访问
   - 如需访问管理员功能，请使用预设的管理员账号
4. 流式接口特别说明：
   - 超时时间设置为3分钟
   - 建议客户端实现重连机制
   - 建议在弱网环境下降级使用普通接口

## 5. 3D重建接口

### 5.1 上传图片进行3D重建（渐进式异步模式）
- 请求路径: `/reconstruction/upload`
- 请求方法: POST
- Content-Type: `multipart/form-data`
- 请求参数:
  - file: 图片文件（必需）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "taskId": "a1b2c3d4e5f6g7h8i9j0",
        "status": "processing"
    },
    "message": "ok"
}
```
- 说明：
  - 该接口会异步处理图片，返回任务ID
  - 采用渐进式处理方式，可以在处理过程中获取部分结果
  - 需要通过状态检查接口查询处理进度和获取已完成的部分

### 5.2 同步上传图片进行3D重建（渐进式同步模式）
- 请求路径: `/reconstruction/upload/sync`
- 请求方法: POST
- Content-Type: `multipart/form-data`
- 请求参数:
  - file: 图片文件（必需）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "taskId": "a1b2c3d4e5f6g7h8i9j0",
        "status": "success",
        "pixelImagesUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/pixel_images.png",
        "xyzImagesUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/xyz_images.png",
        "objFileUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/model.obj",
        "mtlFileUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/model.mtl",
        "textureImageUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/texture.png"
    },
    "message": "ok"
}
```
- 说明：
  - 该接口会同步处理图片，等待处理完成后返回结果
  - 采用渐进式处理方式，但会等待所有结果都完成后才返回
  - 处理时间可能较长，请考虑设置足够的超时时间（建议2分钟以上）

### 5.3 获取任务状态
- 请求路径: `/reconstruction/status/{taskId}`
- 请求方法: GET
- 路径参数:
  - taskId: 任务ID
- 响应数据（处理中）:
```json
{
    "code": 0,
    "data": {
        "taskId": "a1b2c3d4e5f6g7h8i9j0",
        "status": "processing",
        "pixelImagesUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/pixel_images.png",
        "xyzImagesUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/xyz_images.png"
    },
    "message": "ok"
}
```

响应数据（完成）:
```json
{
    "code": 0,
    "data": {
        "taskId": "a1b2c3d4e5f6g7h8i9j0",
        "status": "success",
        "pixelImagesUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/pixel_images.png",
        "xyzImagesUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/xyz_images.png",
        "objFileUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/model.obj",
        "mtlFileUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/model.mtl",
        "textureImageUrl": "/api/reconstruction/files/a1b2c3d4e5f6g7h8i9j0/texture.png"
    },
    "message": "ok"
}
```
- 说明：
  - status可能的值："processing"（处理中）、"success"（成功）、"failed"（失败）
  - 当任务处于"processing"状态时，可能会返回已完成的部分结果的URL
  - 当任务完成（status为"success"）时，会返回所有结果文件的URL

### 5.4 获取重建结果文件
- 请求路径: `/reconstruction/files/{taskId}/{fileName}`
- 请求方法: GET
- 路径参数:
  - taskId: 任务ID
  - fileName: 文件名（如 model.obj, model.mtl, pixel_images.png 等）
- 响应类型: 根据文件类型自动设置
- 响应数据: 二进制文件数据
- 说明：
  - 返回指定的单个文件，而不是整个ZIP包
  - 常见文件包括：model.obj（3D模型）、model.mtl（材质）、pixel_images.png（纹理图像）
  - 如果任务尚未完成或文件不存在，将返回404错误

### 5.5 获取文件内容（用于图片预览）
- 请求路径: `/reconstruction/preview`
- 请求方法: GET
- 请求参数:
  - path: 文件的绝对路径
- 响应类型: 根据文件类型自动设置
- 响应数据: 二进制文件数据
- 说明：
  - 主要用于图片预览
  - 返回文件的实际内容，而不是下载链接
  - 如果文件不存在，将返回404错误

### 5.6 检查WebSocket连接状态
- 请求路径: `/reconstruction/connection/status`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": true,
    "message": "ok"
}
```
- 说明：
  - 返回true表示与WebSocket服务器连接正常
  - 返回false表示未连接或连接已断开
  - 系统使用OkHttp WebSocket客户端，实现了自动重连和心跳检测机制

## 6. 前端测试页面

系统提供了一个简单的前端测试页面，可以用于测试3D重建功能：

- 访问地址：`http://服务器IP:8123/api/reconstruction.html`
- 功能：
  - 上传图片进行3D重建
  - 支持同步和异步模式
  - 检查任务状态
  - 下载重建结果
  - 检查WebSocket连接状态

## 跨域支持

本服务已配置跨域支持：
- 允许所有源
- 允许携带认证信息（Cookie）
- 支持的HTTP方法：GET、POST、PUT、DELETE、OPTIONS
- 跨域请求有效期：3600秒
