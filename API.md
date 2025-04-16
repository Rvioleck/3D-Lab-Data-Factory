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

## 2. 聊天接口

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

### 2.7 删除消息对
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
  - 删除指定的消息对（用户消息和对应的AI回复）
  - 如果指定的是用户消息，则会同时删除对应的AI回复
  - 如果指定的是AI回复，则会同时删除对应的用户消息

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

## 注意事项

1. 所有需要用户认证的接口都需要先登录
2. 所有包含Long类型的ID字段在JSON序列化时都会自动转换为字符串类型，以避免JavaScript中的精度丢失问题
3. 流式接口特别说明：
   - 超时时间设置为3分钟
   - 建议客户端实现重连机制
   - 建议在弱网环境下降级使用普通接口

## 跨域支持

本服务已配置跨域支持：
- 允许所有源
- 允许携带认证信息（Cookie）
- 支持的HTTP方法：GET、POST、PUT、DELETE、OPTIONS
- 跨域请求有效期：3600秒
