# AI 3D Backend 项目

这是一个基于Spring Boot的AI 3D后端项目，提供用户管理、聊天会话管理和AI对话功能，支持流式响应。

## 主要功能

1. **用户管理**：注册、登录、获取用户信息
2. **聊天会话**：创建会话、获取会话列表、删除会话
3. **AI对话**：发送消息并获取AI回复，支持流式响应
4. **健康检查**：系统状态监控

## 技术栈

- Java 8+
- Spring Boot
- MyBatis
- MySQL
- DeepSeek AI API
- Server-Sent Events (SSE)

## 项目结构

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/elwg/ai3dbackend/
│   │   │       ├── common/              # 通用工具类和响应模型
│   │   │       │   ├── BaseResponse.java
│   │   │       │   ├── DeleteRequest.java
│   │   │       │   ├── PageRequest.java
│   │   │       │   ├── PageResponse.java
│   │   │       │   └── ResultUtils.java
│   │   │       ├── config/              # 配置类
│   │   │       │   ├── AsyncConfig.java
│   │   │       │   ├── CorsConfig.java
│   │   │       │   ├── DeepSeekConfig.java
│   │   │       │   ├── JacksonConfig.java
│   │   │       │   ├── TransactionConfig.java
│   │   │       │   └── WebMvcConfig.java
│   │   │       ├── constant/            # 常量定义
│   │   │       │   └── UserConstant.java
│   │   │       ├── controller/          # 控制器
│   │   │       │   ├── ChatController.java
│   │   │       │   ├── HealthController.java
│   │   │       │   ├── TestController.java
│   │   │       │   └── UserController.java
│   │   │       ├── exception/           # 异常处理
│   │   │       │   ├── BusinessException.java
│   │   │       │   ├── ErrorCode.java
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ThrowUtils.java
│   │   │       ├── mapper/              # MyBatis映射接口
│   │   │       │   ├── ChatMessageMapper.java
│   │   │       │   ├── ChatSessionMapper.java
│   │   │       │   └── UserMapper.java
│   │   │       ├── model/               # 数据模型
│   │   │       │   ├── dto/             # 数据传输对象
│   │   │       │   │   ├── ChatRequest.java
│   │   │       │   │   ├── UserLoginRequest.java
│   │   │       │   │   └── UserRegisterRequest.java
│   │   │       │   ├── entity/          # 数据库实体
│   │   │       │   │   ├── ChatMessage.java
│   │   │       │   │   ├── ChatSession.java
│   │   │       │   │   └── User.java
│   │   │       │   ├── enums/           # 枚举类型
│   │   │       │   │   └── UserRoleEnum.java
│   │   │       │   └── vo/              # 视图对象
│   │   │       │       └── LoginUserVO.java
│   │   │       ├── service/             # 服务接口和实现
│   │   │       │   ├── impl/
│   │   │       │   │   ├── ChatServiceImpl.java
│   │   │       │   │   └── UserServiceImpl.java
│   │   │       │   ├── ChatService.java
│   │   │       │   ├── DeepSeekService.java
│   │   │       │   └── UserService.java
│   │   │       └── Ai3dBackendApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── db/                      # 数据库脚本
│   │       │   ├── chat.sql
│   │       │   └── user.sql
│   │       ├── static/                  # 静态资源
│   │       └── application.yml          # 应用配置文件
│   └── test/                            # 测试代码
├── .idea/                               # IDE配置
├── target/                              # 编译输出
├── .gitignore                           # Git忽略文件
├── API.md                               # API文档
├── pom.xml                              # Maven配置
└── README.md                            # 项目说明
```

## 安装与运行

1. 确保已安装Java 8+和Maven

2. 配置数据库：
   - 创建MySQL数据库
   - 执行`src/main/resources/db`目录下的SQL脚本
   - 在`application.yml`中配置数据库连接信息

3. 配置DeepSeek API密钥：
   - 在`application.yml`中配置DeepSeek API密钥

4. 构建并运行：

```bash
mvn clean package
java -jar target/ai-3d-backend-0.0.1-SNAPSHOT.jar
```

## 流式响应功能

本项目实现了流式响应功能，主要特性包括：

1. 使用Server-Sent Events (SSE)协议实现流式数据传输
2. 支持与DeepSeek AI API的流式集成
3. 实现了异步处理机制，提高系统并发能力

## 注意事项

1. 确保已正确配置数据库连接信息
2. 确保DeepSeek API密钥配置正确，否则AI对话功能将无法使用
3. 详细API文档请参考 `API.md`
4. 前端项目请参考 `ai-3d-frontend` 目录
