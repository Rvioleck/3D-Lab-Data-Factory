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
   - 分层设计的用户数据模型，包括 UserVO 和 UserDetailVO
   - 完整的用户生命周期管理：注册、登录、信息查询、更新和删除
   - 支持管理员创建和管理用户账号
   - 严格的用户角色管理，确保系统至少保留一个管理员

2. **权限控制**：基于用户角色的权限管理，支持管理员和普通用户权限区分
   - 使用 `@AuthCheck` 注解和 `AuthInterceptor` 拦截器实现细粒度权限控制
   - 支持在类和方法级别进行权限校验
   - 防止非管理员用户提升权限或降级唯一管理员

3. **聊天会话**：创建会话、获取会话列表、删除会话
   - 支持自动创建会话和手动创建会话两种模式
   - 基于会话ID的消息历史管理
   - 当前仅允许管理员访问聊天功能

4. **AI对话**：发送消息并获取AI回复，支持流式响应
   - 集成DeepSeek AI API实现智能对话
   - 支持同步和流式两种响应模式
   - 自动合并连续同类型消息，提高API调用效率
   - 使用事务管理确保消息一致性

5. **3D重建**：上传图片并生成3D模型
   - 基于HTTP+SSE的异步处理架构
   - 分离的上传图片和创建重建任务流程，提供更灵活的用户体验
   - 完整的任务状态管理和实时进度反馈
   - 支持创建重建任务、查看处理状态和获取重建结果文件
   - 支持分页获取用户的任务列表

6. **图片与3D资源管理**：管理图片和生成的3D模型资源
   - 支持图片上传、更新、删除和查询
   - 支持按类型、分类和标签筛选资源
   - 自动提取图片信息和3D模型基本参数
   - 提供图片预览和3D模型在线预览功能
   - 基于权限的资源访问控制

7. **文件存储服务**：统一的文件存储接口
   - 基于腾讯云对象存储服务(COS)的实现
   - 支持文件上传、下载、删除和查询
   - 自动生成文件URL和管理文件路径
   - 支持目录列表和文件检查功能

8. **健康检查**：系统状态监控
   - 提供简单和详细两种健康检查接口
   - 监控应用程序、JVM和系统状态
   - 支持Python服务健康状态检查
   - 前端可视化健康状态展示

## 技术栈

- Java 21
- Spring Boot 3
- MyBatis Plus
- MySQL
- DeepSeek AI API
- Server-Sent Events (SSE)
- 腾讯云对象存储服务 (COS)
- OkHttp3
- Vue.js (前端)
- Three.js (3D模型渲染)

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
│   │   │       │   ├── CosClientConfig.java
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
│   │   │       │   ├── ModelController.java
│   │   │       │   ├── PictureController.java
│   │   │       │   ├── ReconstructionCallbackController.java
│   │   │       │   ├── ReconstructionController.java
│   │   │       │   ├── TestController.java
│   │   │       │   └── UserController.java
│   │   │       ├── exception/           # 异常处理
│   │   │       │   ├── BusinessException.java
│   │   │       │   ├── ErrorCode.java
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── ThrowUtils.java
│   │   │       ├── manager/             # 管理器
│   │   │       │   └── CosManager.java  # 腾讯云对象存储管理器
│   │   │       ├── mapper/              # MyBatis映射接口
│   │   │       │   ├── ChatMessageMapper.java
│   │   │       │   ├── ChatSessionMapper.java
│   │   │       │   ├── ModelMapper.java
│   │   │       │   ├── PictureMapper.java
│   │   │       │   ├── ReconstructionTaskMapper.java
│   │   │       │   └── UserMapper.java
│   │   │       ├── model/               # 数据模型
│   │   │       │   ├── dto/             # 数据传输对象
│   │   │       │   │   ├── ChatRequest.java
│   │   │       │   │   ├── GetRequest.java
│   │   │       │   │   ├── ModelUpdateRequest.java
│   │   │       │   │   ├── PictureUpdateRequest.java
│   │   │       │   │   ├── ReconstructionUploadResponse.java
│   │   │       │   │   ├── UserCreateRequest.java
│   │   │       │   │   ├── UserLoginRequest.java
│   │   │       │   │   ├── UserRegisterRequest.java
│   │   │       │   │   └── UserUpdateRequest.java
│   │   │       │   ├── entity/          # 数据库实体
│   │   │       │   │   ├── ChatMessage.java
│   │   │       │   │   ├── ChatSession.java
│   │   │       │   │   ├── Model.java
│   │   │       │   │   ├── Picture.java
│   │   │       │   │   ├── ReconstructionTask.java
│   │   │       │   │   └── User.java
│   │   │       │   ├── enums/           # 枚举类型
│   │   │       │   │   ├── TaskStatus.java
│   │   │       │   │   └── UserRoleEnum.java
│   │   │       │   └── vo/              # 视图对象
│   │   │       │       ├── ModelVO.java
│   │   │       │       ├── PictureVO.java
│   │   │       │       ├── ReconstructionTaskVO.java
│   │   │       │       ├── UserDetailVO.java
│   │   │       │       └── UserVO.java
│   │   │       ├── service/             # 服务接口和实现
│   │   │       │   ├── impl/
│   │   │       │   │   ├── ChatServiceImpl.java
│   │   │       │   │   ├── CosFileStorageServiceImpl.java
│   │   │       │   │   ├── DeepSeekServiceImpl.java
│   │   │       │   │   ├── EventStreamServiceImpl.java
│   │   │       │   │   ├── ModelServiceImpl.java
│   │   │       │   │   ├── PictureServiceImpl.java
│   │   │       │   │   ├── ReconstructionHttpServiceImpl.java
│   │   │       │   │   ├── ReconstructionTaskServiceImpl.java
│   │   │       │   │   └── UserServiceImpl.java
│   │   │       │   ├── ChatService.java
│   │   │       │   ├── DeepSeekService.java
│   │   │       │   ├── EventStreamService.java
│   │   │       │   ├── FileStorageService.java
│   │   │       │   ├── ModelService.java
│   │   │       │   ├── PictureService.java
│   │   │       │   ├── ReconstructionHttpService.java
│   │   │       │   ├── ReconstructionTaskService.java
│   │   │       │   └── UserService.java
│   │   │       ├── utils/               # 工具类
│   │   │       │   └── ImageUtils.java  # 图片处理工具
│   │   │       └── Ai3dBackendApplication.java  # 应用入口
│   │   └── resources/
│   │       ├── db/                      # 数据库脚本
│   │       │   ├── chat.sql
│   │       │   ├── model.sql
│   │       │   ├── picture.sql
│   │       │   ├── reconstruction_task.sql
│   │       │   └── user.sql
│   │       ├── static/                  # 静态资源
│   │       └── application.yml          # 应用配置文件
│   └── test/                            # 测试代码
├── .idea/                               # IDE配置
├── target/                              # 编译输出
├── .gitignore                           # Git忽略文件
├── API.md                               # API文档
├── 3D-Reconstruction.md                 # 3D重建模块设计文档
├── pom.xml                              # Maven配置
└── README.md                            # 项目说明
```

## 安装与运行

1. 确保已安装Java 21和Maven

2. 配置数据库：
   - 创建MySQL数据库
   - 执行`src/main/resources/db`目录下的SQL脚本
   - 在`application.yml`中配置数据库连接信息

3. 配置腾讯云COS：
   - 创建腾讯云COS存储桶
   - 在`application.yml`中配置COS相关参数或设置环境变量

4. 配置DeepSeek API密钥：
   - 在`application.yml`中配置DeepSeek API密钥

5. 配置3D重建服务：
   - 部署Python 3D重建服务
   - 在`application.yml`中配置重建服务URL和回调参数

6. 构建并运行：

```bash
mvn clean package
java -jar target/ai-3d-backend-0.0.1-SNAPSHOT.jar
```

## 权限控制功能

本项目实现了基于注解的权限控制功能，主要特性包括：

1. 使用`@AuthCheck`注解标记需要进行权限校验的类或方法
2. 通过AOP实现权限拦截，无需修改业务代码即可实现权限控制
3. 支持指定必须具有的角色，如管理员角色
4. 支持在类级别和方法级别进行权限控制
5. 严格的用户角色管理，确保系统至少保留一个管理员

## 流式响应功能

本项目实现了两种流式响应功能：

1. **AI对话流式响应**：
   - 使用Server-Sent Events (SSE)协议实现流式数据传输
   - 支持与DeepSeek AI API的流式集成
   - 实现了异步处理机制，提高系统并发能力
   - 优化消息处理逻辑，自动合并连续同类型消息，提高API调用效率

2. **3D重建流式进度反馈**：
   - 使用SSE实现重建任务进度和结果的实时推送
   - 支持任务状态变更通知和文件生成事件
   - 实现了事件流管理服务，处理连接、超时和错误

## 3D模型预览功能

本项目实现了基于Three.js的3D模型在线预览功能：

1. 支持OBJ/MTL/纹理文件的加载和渲染
2. 提供模型旋转、缩放、平移等交互操作
3. 支持切换线框模式和纹理显示
4. 自适应模型大小和位置
5. 支持全屏预览和重置视图

## 文件存储服务

本项目使用腾讯云对象存储服务(COS)实现文件存储功能：

1. 统一的文件存储接口，支持多种存储实现
2. 支持文件上传、下载、删除和查询
3. 自动生成文件URL和管理文件路径
4. 支持目录列表和文件检查功能
5. 高效的大文件处理能力

## 注意事项

1. 确保已正确配置数据库连接信息
2. 确保腾讯云COS配置正确，否则文件存储功能将无法使用
3. 确保DeepSeek API密钥配置正确，否则AI对话功能将无法使用
4. 确保Python 3D重建服务正常运行，否则3D重建功能将无法使用
5. 详细API文档请参考 `API.md`
6. 3D重建模块设计文档请参考 `3D-Reconstruction.md`
7. 前端项目请参考 `ai-3d-frontend` 目录
