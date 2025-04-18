# AI 3D Backend 项目

这是一个基于Spring Boot的AI 3D后端项目，提供用户管理、聊天会话管理和AI对话功能，支持流式响应和权限控制。

## 3D-Lab Data Factory 产品概述

### 用户需求
本产品旨在打造一座7/24无间断的AI驱动3D数据工厂，持续自动化生成和提供高质量的实验室场景3D数据，以满足科研机构、生物医学实验室及AI算法团队对高精度三维数据的迫切需求。系统核心功能包括自动化的3D资源管理、高效稳定的3D场景自动生成以及机器人的虚拟操作训练演示。用户通过便捷高效的界面，无需人工干预，即可获得连续稳定的高质量三维数据支持。

### 产品核心特点
- **7/24持续运行**：系统可全天候无中断自动化运行。
- **自动化高效生产**：依靠先进的AI模型与算法，无需人工干涉即可自动完成三维数据的采集、生成、校准与存储。
- **数据高质量**：严格的数据筛选机制确保所有输出的3D模型和场景数据精确可靠。
- **高交互可视化界面**：直观易用的交互界面，支持数据的快速浏览、筛选、使用与管理。

## 主要功能

1. **用户管理**：注册、登录、获取用户信息
   - 重构用户模块，优化实体类设计
   - 新增 UserVO 和 UserDetailVO 分层设计，更清晰的数据层次
   - 新增用户详情接口，支持获取更多用户信息
2. **权限控制**：基于用户角色的权限管理，支持管理员和普通用户权限区分
   - 新增 `AuthCheck` 注解和 `AuthInterceptor` 拦截器，实现细粒度权限控制
   - 支持在类和方法级别进行权限校验
3. **聊天会话**：创建会话、获取会话列表、删除会话
   - 当前仅允许管理员访问聊天功能
4. **AI对话**：发送消息并获取AI回复，支持流式响应
   - 优化聊天接口，提高响应速度和稳定性
5. **消息管理**：
   - 支持删除单条消息，重构消息删除逻辑
   - 自动合并连续同类型消息，提高 API 调用效率
6. **3D重建**：上传图片并生成3D模型
   - 利用现有的文件上传接口，避免功能重复
   - 将上传图片和创建重建任务分离，提供更灵活的用户体验
   - 使用HTTP+SSE方式与重建服务通信，替代原有的WebSocket方式
   - 支持异步处理模式，通过SSE实时接收处理进度和结果
   - 新增重建任务表，管理重建任务的状态和结果
   - 支持创建重建任务、查看处理状态和获取重建结果文件
   - 支持分页获取用户的任务列表
   - 使用腾讯云COS存储服务存储文件
7. **图片与3D资源管理**：管理图片和生成的3D资源
   - 支持已登录用户上传图片，通过AuthCheck注解进行权限控制
   - 支持用户更新和删除自己的图片，管理员可以管理所有图片
   - 支持管理员触发3D重建任务
   - 支持所有用户查看、搜索和下载图片与3D资源
   - 提供图片预览和3D模型在线预览功能
   - 支持按类型、分类和标签筛选资源
   - 自动提取图片信息和3D模型基本参数
8. **健康检查**：系统状态监控

## 技术栈

- Java 8+
- Spring Boot
- MyBatis
- MySQL
- DeepSeek AI API
- Server-Sent Events (SSE)
- 腾讯云对象存储服务 (COS)

## 项目结构

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/elwg/ai3dbackend/
│   │   │       ├── annotation/          # 注解定义
│   │   │       │   └── AuthCheck.java   # 权限校验注解
│   │   │       ├── aop/                 # 面向切面编程
│   │   │       │   └── AuthInterceptor.java # 权限校验拦截器
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
│   │   │       │   ├── MybatisPlusConfig.java
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
│   │   │       │   │   ├── UserAddRequest.java
│   │   │       │   │   ├── UserUpdateRequest.java
│   │   │       │   │   └── UserRegisterRequest.java
│   │   │       │   ├── entity/          # 数据库实体
│   │   │       │   │   ├── ChatMessage.java
│   │   │       │   │   ├── ChatSession.java
│   │   │       │   │   └── User.java
│   │   │       │   ├── enums/           # 枚举类型
│   │   │       │   │   └── UserRoleEnum.java
│   │   │       │   └── vo/              # 视图对象
│   │   │       │       └── UserVO.java
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

## 最近更新

### 2025-04-18
- 修复了 `getUserById` 和 `getUserDetailById` 接口的 JSON 反序列化问题
  - 前端调用时现在使用 `{ id }` 对象而不是直接传递 ID 值
  - 更新了 API 文档以反映正确的请求格式

## 权限控制功能

本项目实现了基于注解的权限控制功能，主要特性包括：

1. 使用`@AuthCheck`注解标记需要进行权限校验的类或方法
2. 通过AOP实现权限拦截，无需修改业务代码即可实现权限控制
3. 支持指定必须具有的角色，如管理员角色
4. 支持在类级别和方法级别进行权限控制

## 流式响应功能

本项目实现了流式响应功能，主要特性包括：

1. 使用Server-Sent Events (SSE)协议实现流式数据传输
2. 支持与DeepSeek AI API的流式集成
3. 实现了异步处理机制，提高系统并发能力
4. 优化消息处理逻辑，自动合并连续同类型消息，提高API调用效率

## 注意事项

1. 确保已正确配置数据库连接信息
2. 确保DeepSeek API密钥配置正确，否则AI对话功能将无法使用
3. 详细API文档请参考 `API.md`
4. 前端项目请参考 `ai-3d-frontend` 目录
