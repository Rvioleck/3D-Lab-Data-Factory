# 3D-Lab-Data-Factory API 文档

## 产品简介

本产品是一个基于Spring Boot的AI 3D后端项目，提供用户管理、聊天会话管理、AI对话功能和3D重建功能，支持流式响应和权限控制。

## 基础信息

- 基础路径: `/`（注意：3D重建相关接口以`/api`开头）
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
- 权限要求: 需要管理员权限（通过`@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)`注解实现）
- 请求体:
```json
{
    "id": "1234567890123456789"  // 用户ID（字符串类型的雪花ID）
}
```

> 注意：请求体使用`GetRequest`对象，必须包含`id`字段。
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
- 权限要求: 需要管理员权限（通过`@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)`注解实现）
- 请求体:
```json
{
    "id": "1234567890123456789"  // 用户ID（字符串类型的雪花ID）
}
```

> 注意：请求体使用`GetRequest`对象，必须包含`id`字段。
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

### 1.8 更新当前用户个人资料
- 请求路径: `/user/profile/update`
- 请求方法: POST
- 请求体:
```json
{
    "userAccount": "string",    // 用户账号（可选）
    "userName": "string",       // 用户名（可选）
    "userAvatar": "string",     // 用户头像URL（可选）
    "userProfile": "string"     // 用户简介（可选）
}
```
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
- 说明：
  - 该接口允许已登录用户更新自己的个人资料
  - 可以更新用户账号、用户名、头像和简介
  - 如果更新账号，会检查新账号是否已存在
  - 返回更新后的用户信息
  - 前端可以只传需要更新的字段，不需要的字段可以不传或传null

### 1.9 上传用户头像
- 请求路径: `/user/avatar/upload`
- 请求方法: POST
- 权限要求: 需要用户权限（通过`@AuthCheck(mustRole = UserConstant.USER_ROLE)`注解实现）
- 请求头: `Content-Type: multipart/form-data`
- 请求参数:
  - file: 头像图片文件（必选）
- 响应数据:
```json
{
    "code": 0,
    "data": "https://example.com/avatars/uuid/avatar.jpg",  // 头像URL
    "message": "ok"
}
```
- 说明：
  - 该接口允许已登录用户上传自己的头像图片
  - 支持的图片格式包括: jpg, jpeg, png, gif等
  - 文件大小限制为5MB
  - 上传成功后返回头像URL
  - 系统会自动更新用户信息中的头像URL

### 1.10 修改当前用户密码
- 请求路径: `/user/password/update`
- 请求方法: POST
- 请求体:
```json
{
    "oldPassword": "string",    // 旧密码
    "newPassword": "string",    // 新密码
    "checkPassword": "string"   // 确认新密码
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": true,    // 修改成功返回 true
    "message": "ok"
}
```
- 说明：
  - 该接口允许已登录用户修改自己的密码
  - 需要提供正确的旧密码
  - 新密码长度不能小于8
  - 两次输入的新密码必须一致

### 1.10 获取所有用户信息（仅管理员可访问）
- 请求路径: `/user/list`
- 请求方法: POST
- 权限要求: 需要管理员权限（通过`@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)`注解实现）
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
- 说明：
  - 该接口返回当前登录用户创建的所有对话会话
  - 会话按创建时间倒序排列（最新的会话排在前面）
  - 可用于在用户界面上展示会话历史

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
- 说明：
  - 该接口返回指定会话下的所有历史消息
  - 消息按时间顺序排列（最早的消息排在前面）
  - 消息包括用户发送的内容和AI的回复
  - 如果会话ID无效，将返回空列表

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
  - 删除操作会验证当前用户是否有权限删除该消息（即消息是否属于该用户）

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

## 5. 图片资源接口

### 5.1 上传图片（需要用户权限）
- 请求路径: `/picture/upload`
- 请求方法: POST
- 权限要求: 需要用户权限（通过`@AuthCheck(mustRole = UserConstant.USER_ROLE)`注解实现）
- Content-Type: `multipart/form-data`
- 请求参数:
  - file: 图片文件（必需）
  - name: 图片名称（可选）
  - category: 图片分类（可选）
  - tags: 图片标签，逗号分隔（可选）
  - introduction: 图片简介（可选）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",
        "url": "https://example.com/images/uuid/image.jpg",
        "name": "示例图片",
        "introduction": "这是一张示例图片",
        "category": "家具",
        "tags": "桌子,椅子",
        "picSize": 1024000,
        "picWidth": 1920,
        "picHeight": 1080,
        "picScale": 1.78,
        "picFormat": "jpg",
        "userId": "1234567890123456789",
        "createTime": "2023-01-01 12:00:00",
        "hasModel": false
    },
    "message": "ok"
}
```
- 说明：
  - 需要用户权限才能访问此接口
  - 该接口负责上传图片并保存图片信息
  - 返回完整的图片信息，包括ID、URL、尺寸等
  - 文件大小限制为10MB
  - 仅支持图片类型文件（Content-Type以`image/`开头）
  - hasModel字段表示该图片是否已关联3D模型

### 5.2 获取图片详情
- 请求路径: `/picture/{id}`
- 请求方法: GET
- 路径参数:
  - id: 图片ID
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",
        "url": "https://example.com/images/uuid/image.jpg",
        "name": "示例图片",
        "introduction": "这是一张示例图片",
        "category": "家具",
        "tags": "桌子,椅子",
        "picSize": 1024000,
        "picWidth": 1920,
        "picHeight": 1080,
        "picScale": 1.78,
        "picFormat": "jpg",
        "userId": "1234567890123456789",
        "createTime": "2023-01-01 12:00:00",
        "hasModel": false
    },
    "message": "ok"
}
```

### 5.3 分页获取图片列表
- 请求路径: `/picture/list/page`
- 请求方法: POST
- 请求体:
```json
{
    "current": 1,
    "pageSize": 10,
    "name": "示例",
    "category": "家具",
    "tags": "桌子",
    "userId": "1234567890123456789"
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "records": [
            {
                "id": "1234567890123456789",
                "url": "https://example.com/images/uuid/image.jpg",
                "name": "示例图片",
                "introduction": "这是一张示例图片",
                "category": "家具",
                "tags": "桌子,椅子",
                "picSize": 1024000,
                "picWidth": 1920,
                "picHeight": 1080,
                "picScale": 1.78,
                "picFormat": "jpg",
                "userId": "1234567890123456789",
                "createTime": "2023-01-01 12:00:00",
                "hasModel": false
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "message": "ok"
}
```
- 说明：
  - 所有查询参数都是可选的
  - name参数支持模糊查询
  - tags参数支持模糊查询
  - 返回的hasModel字段表示该图片是否已关联3D模型

### 5.4 更新图片信息（需要用户权限）
- 请求路径: `/picture/update`
- 请求方法: POST
- 权限要求: 需要用户权限（通过`@AuthCheck(mustRole = UserConstant.USER_ROLE)`注解实现）
- 请求体:
```json
{
    "id": "1234567890123456789",
    "name": "更新后的名称",
    "introduction": "更新后的简介",
    "category": "更新后的分类",
    "tags": "更新,标签"
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": true,
    "message": "ok"
}
```
- 说明：
  - 只能更新图片的基本信息，不能更新图片文件本身
  - 只有图片的创建者或管理员可以更新图片信息

### 5.5 删除图片（需要用户权限）
- 请求路径: `/picture/delete`
- 请求方法: POST
- 权限要求: 需要用户权限（通过`@AuthCheck(mustRole = UserConstant.USER_ROLE)`注解实现）
- 请求体:
```json
{
    "id": "1234567890123456789"
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": true,
    "message": "ok"
}
```
- 说明：
  - 需要用户权限才能访问此接口
  - 只有图片的创建者或管理员可以删除图片
  - 如果图片已关联3D模型，则无法删除

### 5.6 获取图片分类列表
- 请求路径: `/picture/categories`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": ["家具", "玩具", "装饰品", "其他"],
    "message": "ok"
}
```

### 5.7 获取图片标签列表
- 请求路径: `/picture/tags`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": ["桌子", "椅子", "沙发", "床"],
    "message": "ok"
}
```

### 5.6 获取图片分类列表
- 请求路径: `/picture/categories`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": ["家具", "玩具", "装饰品", "其他"],
    "message": "ok"
}
```
- 说明：
  - 该接口返回系统中所有不重复的图片分类
  - 分类列表按字母顺序排序
  - 可用于前端分类筛选功能

### 5.7 获取图片标签列表
- 请求路径: `/picture/tags`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": ["桌子", "椅子", "沙发", "床"],
    "message": "ok"
}
```
- 说明：
  - 该接口返回系统中所有不重复的图片标签
  - 标签从所有图片的tags字段中提取（逗号分隔）并去重
  - 可用于前端标签筛选功能

### 5.8 批量获取图片信息
- 请求路径: `/picture/batch`
- 请求方法: POST
- 请求体:
```json
["1234567890123456789", "9876543210987654321"]
```
- 响应数据:
```json
{
    "code": 0,
    "data": [
        {
            "id": "1234567890123456789",
            "url": "https://example.com/images/uuid/image1.jpg",
            "name": "示例图片1",
            "introduction": "这是一张示例图片",
            "category": "家具",
            "tags": "桌子,椅子",
            "picSize": 1024000,
            "picWidth": 1920,
            "picHeight": 1080,
            "picScale": 1.78,
            "picFormat": "jpg",
            "userId": "1234567890123456789",
            "createTime": "2023-01-01 12:00:00",
            "hasModel": false
        },
        {
            "id": "9876543210987654321",
            "url": "https://example.com/images/uuid/image2.jpg",
            "name": "示例图片2",
            "introduction": "这是另一张示例图片",
            "category": "玩具",
            "tags": "积木,玩偶",
            "picSize": 2048000,
            "picWidth": 1280,
            "picHeight": 720,
            "picScale": 1.78,
            "picFormat": "png",
            "userId": "1234567890123456789",
            "createTime": "2023-01-02 12:00:00",
            "hasModel": true
        }
    ],
    "message": "ok"
}
```
- 说明：
  - 该接口用于根据ID列表批量获取多个图片的详细信息
  - 请求体为图片ID数组
  - 返回的每个图片对象包含完整的图片信息
  - hasModel字段表示该图片是否已关联3D模型

### 5.9 上传图片（已弃用）
- 请求路径: `/file/upload/image`
- 请求方法: POST
- Content-Type: `multipart/form-data`
- 请求参数:
  - file: 图片文件（必需）
- 响应数据:
```json
{
    "code": 0,
    "data": "https://example.com/images/uuid/image.jpg",
    "message": "ok"
}
```
- 说明：
  - **此接口已弃用，请使用 `/picture/upload` 接口代替**
  - 该接口仅负责上传图片并存储到COS，不会创建重建任务
  - 返回图片的URL
  - 文件大小限制为10MB
  - 仅支持图片类型文件（Content-Type以`image/`开头）

## 6. 3D模型接口

### 6.1 获取模型详情
- 请求路径: `/model/{id}`
- 请求方法: GET
- 路径参数:
  - id: 模型ID
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "id": "1234567890123456789",
        "name": "3D模型示例",
        "introduction": "这是一个3D模型示例",
        "category": "家具",
        "tags": "桌子,椅子",
        "modelUrl": "https://example.com/models/uuid/model.obj",
        "textureUrl": "https://example.com/models/uuid/texture.png",
        "previewUrl": "https://example.com/models/uuid/preview.png",
        "sourceImageId": "9876543210987654321",
        "taskId": "abcdef1234567890",
        "userId": "1234567890123456789",
        "createTime": "2023-01-01 12:00:00",
        "updateTime": "2023-01-01 12:00:00"
    },
    "message": "ok"
}
```

### 6.2 分页获取模型列表
- 请求路径: `/model/list/page`
- 请求方法: POST
- 请求体:
```json
{
    "current": 1,
    "pageSize": 10,
    "name": "桌子",
    "category": "家具",
    "tags": "木质",
    "userId": "1234567890123456789"
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "records": [
            {
                "id": "1234567890123456789",
                "name": "3D模型示例1",
                "introduction": "这是一个3D模型示例",
                "category": "家具",
                "tags": "桌子,椅子",
                "modelUrl": "https://example.com/models/uuid/model1.obj",
                "textureUrl": "https://example.com/models/uuid/texture1.png",
                "previewUrl": "https://example.com/models/uuid/preview1.png",
                "sourceImageId": "9876543210987654321",
                "taskId": "abcdef1234567890",
                "userId": "1234567890123456789",
                "createTime": "2023-01-01 12:00:00",
                "updateTime": "2023-01-01 12:00:00"
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "message": "ok"
}
```
- 说明：
  - 所有查询参数都是可选的
  - name参数支持模糊查询
  - tags参数支持模糊查询

### 6.3 更新模型信息（需要用户权限）
- 请求路径: `/model/update`
- 请求方法: POST
- 权限要求: 需要用户权限（通过`@AuthCheck(mustRole = UserConstant.USER_ROLE)`注解实现）
- 请求体:
```json
{
    "id": "1234567890123456789",
    "name": "更新后的名称",
    "introduction": "更新后的简介",
    "category": "更新后的分类",
    "tags": "更新,标签"
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": true,
    "message": "ok"
}
```
- 说明：
  - 只能更新模型的基本信息，不能更新模型文件本身
  - 只有模型的创建者或管理员可以更新模型信息

### 6.4 删除模型（需要用户权限）
- 请求路径: `/model/delete`
- 请求方法: POST
- 权限要求: 需要用户权限（通过`@AuthCheck(mustRole = UserConstant.USER_ROLE)`注解实现）
- 请求体:
```json
{
    "id": "1234567890123456789"
}
```
- 响应数据:
```json
{
    "code": 0,
    "data": true,
    "message": "ok"
}
```
- 说明：
  - 需要用户权限才能访问此接口
  - 只有模型的创建者或管理员可以删除模型

### 6.5 获取模型分类列表
- 请求路径: `/model/categories`
- 请求方法: GET
- 响应数据:
```json
{
    "code": 0,
    "data": ["家具", "玩具", "装饰品", "其他"],
    "message": "ok"
}
```
- 说明：
  - 该接口返回系统中所有不重复的模型分类
  - 分类列表按字母顺序排序
  - 可用于前端分类筛选功能

### 6.6 批量获取模型信息
- 请求路径: `/model/batch`
- 请求方法: POST
- 请求体:
```json
["1234567890123456789", "9876543210987654321"]
```
- 响应数据:
```json
{
    "code": 0,
    "data": [
        {
            "id": "1234567890123456789",
            "name": "3D模型示例1",
            "introduction": "这是一个3D模型示例",
            "category": "家具",
            "tags": "桌子,椅子",
            "modelUrl": "https://example.com/models/uuid/model1.obj",
            "textureUrl": "https://example.com/models/uuid/texture1.png",
            "previewUrl": "https://example.com/models/uuid/preview1.png",
            "sourceImageId": "9876543210987654321",
            "taskId": "abcdef1234567890",
            "userId": "1234567890123456789",
            "createTime": "2023-01-01 12:00:00",
            "updateTime": "2023-01-01 12:00:00"
        },
        {
            "id": "9876543210987654321",
            "name": "3D模型示例2",
            "introduction": "这是另一个3D模型示例",
            "category": "玩具",
            "tags": "积木,玩偶",
            "modelUrl": "https://example.com/models/uuid/model2.obj",
            "textureUrl": "https://example.com/models/uuid/texture2.png",
            "previewUrl": "https://example.com/models/uuid/preview2.png",
            "sourceImageId": "1234567890123456789",
            "taskId": "1234567890abcdef",
            "userId": "1234567890123456789",
            "createTime": "2023-01-02 12:00:00",
            "updateTime": "2023-01-02 12:00:00"
        }
    ],
    "message": "ok"
}
```
- 说明：
  - 该接口用于根据ID列表批量获取多个模型的详细信息
  - 请求体为模型ID数组
  - 返回的每个模型对象包含完整的模型信息

## 7. 3D重建接口

### 7.1 创建3D重建任务
- 请求路径: `/api/reconstruction/create`
- 请求方法: POST
- 请求参数:
  - imageId: 图片ID（必需）
  - name: 模型名称（可选）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "taskId": "1234567890abcdef1234567890abcdef",
        "sseUrl": "/api/reconstruction/events/1234567890abcdef1234567890abcdef"
    },
    "message": "ok"
}
```
- 说明：
  - 该接口使用已上传的图片创建3D重建任务
  - imageId是从`/picture/upload`接口返回的图片ID
  - 返回任务ID和SSE事件流URL
  - 客户端可以通过SSE事件流实时获取处理进度和结果
  - 需要管理员权限才能访问此接口（通过`@AuthCheck(mustRole = "admin")`注解实现）

### 7.2 获取任务状态
- 请求路径: `/api/reconstruction/status/{taskId}`
- 请求方法: GET
- 路径参数:
  - taskId: 任务ID
```
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "taskId": "1234567890abcdef1234567890abcdef",
        "sseUrl": "/api/reconstruction/events/1234567890abcdef1234567890abcdef"
    },
    "message": "ok"
}
```
- 说明：
  - 该接口使用图片ID创建3D重建任务
  - imageId必须是已存在的图片ID
  - name和category参数是可选的
  - 返回任务ID和SSE事件流URL
  - 需要管理员权限才能访问此接口

### 7.3 SSE事件流
- 请求路径: `/api/reconstruction/events/{taskId}`
- 请求方法: GET
- 路径参数:
  - taskId: 任务ID
- 响应类型: `text/event-stream`
- 响应数据: Server-Sent Events流，包含以下事件类型：
  - `connect`: 连接建立成功
  ```
  event: connect
  id: 1
  data: Connected successfully
  ```
  - `status`: 任务状态更新
  ```
  event: status
  id: 2
  data: {"taskId":"1234567890abcdef1234567890abcdef","status":"PROCESSING"}
  ```
  - `result`: 结果文件可用
  ```
  event: result
  id: 3
  data: {"taskId":"1234567890abcdef1234567890abcdef","name":"pixel_images.png","url":"https://example.com/reconstruction/1234567890abcdef1234567890abcdef/pixel_images.png"}
  ```
- 说明：
  - 客户端需要使用EventSource API或其他SSE客户端库来处理事件流
  - 当任务完成或失败时，服务器会自动关闭连接
  - 客户端可以根据status事件中的状态来判断任务是否完成
  - 如果任务已经有状态或结果文件，连接建立后会立即发送相应事件

### 7.4 获取任务状态
- 请求路径: `/api/reconstruction/status/{taskId}`
- 请求方法: GET
- 路径参数:
  - taskId: 任务ID
- 权限要求: 无特殊权限要求，但需要登录
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "taskId": "1234567890abcdef1234567890abcdef",
        "status": "COMPLETED",
        "sourceImageId": "123456789",
        "originalImageUrl": "https://example.com/images/image.jpg",
        "pixelImagesUrl": "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/pixel_images.png",
        "xyzImagesUrl": "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/xyz_images.png",
        "outputZipUrl": "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/output3d.zip",
        "resultModelId": "987654321",
        "errorMessage": null,
        "processingTime": 120,
        "createTime": "2023-01-01T00:00:00Z",
        "updateTime": "2023-01-01T00:02:00Z"
    },
    "message": "ok"
}
```
- 说明：
  - status可能的值："PENDING"（等待处理）、"PROCESSING"（处理中）、"COMPLETED"（完成）、"FAILED"（失败）
  - 当任务完成（status为"COMPLETED"）时，会返回所有结果文件的URL
  - 当任务失败（status为"FAILED"）时，errorMessage字段会包含错误信息

### 7.5 获取任务列表
- 请求路径: `/api/reconstruction/tasks`
- 请求方法: GET
- 权限要求: 需要管理员权限（通过`@AuthCheck(mustRole = "admin")`注解实现）
- 请求参数:
  - status: 任务状态筛选（可选）
  - current: 当前页码（默认1）
  - pageSize: 每页大小（默认10）
- 响应数据:
```json
{
    "code": 0,
    "data": {
        "records": [
            {
                "id": "123456789",
                "taskId": "1234567890abcdef1234567890abcdef",
                "status": "COMPLETED",
                "sourceImageId": "123456789",
                "originalImageUrl": "https://example.com/images/image.jpg",
                "pixelImagesUrl": "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/pixel_images.png",
                "xyzImagesUrl": "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/xyz_images.png",
                "outputZipUrl": "https://example.com/reconstruction/1234567890abcdef1234567890abcdef/output3d.zip",
                "resultModelId": "987654321",
                "errorMessage": null,
                "processingTime": 120,
                "userId": "123456789",
                "createTime": "2023-01-01T00:00:00Z",
                "updateTime": "2023-01-01T00:02:00Z"
            }
        ],
        "total": 100,
        "size": 10,
        "current": 1,
        "pages": 10
    },
    "message": "ok"
}
```
- 说明：
  - 需要管理员权限才能访问此接口
  - 返回当前登录用户创建的任务列表
  - 可以通过status参数筛选特定状态的任务

### 7.6 获取重建结果文件
- 请求路径: `/api/reconstruction/files/{taskId}/{fileName}`
- 请求方法: GET
- 路径参数:
  - taskId: 任务ID
  - fileName: 文件名（如 model.obj, model.mtl, pixel_images.png 等）
- 响应类型: 根据文件类型自动设置
- 响应数据: 二进制文件数据
- 说明：
  - 返回指定的单个文件
  - 常见文件包括：model.obj（3D模型）、model.mtl（材质）、pixel_images.png（纹理图像）、xyz_images.png（深度图）、output3d.zip（完整输出包）
  - 如果任务尚未完成或文件不存在，将返回404错误
  - 响应头中包含适当的Content-Type和Content-Disposition
  - 文件从存储服务（如腾讯COS）中获取，路径格式为"reconstruction/{taskId}/{fileName}"

## 7. 前端测试页面

系统提供了一个简单的前端测试页面，可以用于测试3D重建功能：

- 访问地址：`http://服务器IP:8123/api/reconstruction.html`
- 功能：
  - 上传图片进行3D重建
  - 支持同步和异步模式
  - 检查任务状态
  - 下载重建结果
  - 3D模型预览（使用Three.js）

## 8. 未实现的接口

以下接口在API文档中定义，但尚未在系统中实现：

- 3D-Lab Data Factory相关接口（资源管理、场景生成、机器人虚拟操作等）

这些接口将在后续版本中实现。

## 9. 跨域支持

本服务已配置跨域支持：
- 允许所有源
- 允许携带认证信息（Cookie）
- 支持的HTTP方法：GET、POST、PUT、DELETE、OPTIONS
- 跨域请求有效期：3600秒
