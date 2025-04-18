# 设计文档：3D重建模块重构（V2.0）

## 1. 引言

### 1.1 目的

本文档详细说明了项目中3D重建模块的重构设计。涵盖了建议的架构、组件职责、API规范、数据模型和实现注意事项，以实现预期的改进。

### 1.2 范围

本文档涵盖端到端3D重建工作流程的重构，包括：

- 前端交互，用于图像上传和结果展示。
- 后端服务逻辑，用于任务管理、通信和存储。
- Python AI服务接口和处理逻辑。
- 前端 <-> 后端 和 后端 <-> Python服务之间的通信协议。
- 集成腾讯云对象存储（COS）。

### 1.3 背景

现有的3D重建模块使用WebSocket进行后端与Python服务的通信，并通过前端轮询获取结果。尽管功能正常，但该架构在可靠性、可扩展性和用户体验方面存在挑战。此次重构旨在通过采用现代且可靠的架构模式解决这些问题。

### 1.4 目标

- 将后端与Python之间的WebSocket通信替换为HTTP异步请求+回调机制。
- 使用服务器发送事件（SSE）替代前端轮询，实现实时状态和结果更新。
- 在腾讯云对象存储（COS）上标准化文件存储，移除本地文件依赖。
- 提高系统的可靠性、容错能力和可扩展性。
- 通过更快的反馈和渐进式结果加载提升用户体验。
- 维持或改进Python服务提供的核心3D重建功能。

### 1.5 目标受众

软件开发人员（前端、后端、Python）、质量保证工程师、系统架构师、项目经理。

---

## 2. 当前架构及局限性

### 2.1 当前状态（概述）

- 前端通过HTTP上传图像。
- 后端与Python服务建立WebSocket连接并发送图像数据。
- Python服务处理图像并通过同一WebSocket返回多个结果部分（图像、压缩包等）。
- 后端可能将结果存储在本地和/或上传到COS。
- 前端通过HTTP轮询检查任务状态并从后端获取结果URL。

### 2.2 局限性

- **WebSocket复杂性**：维护持久的WebSocket连接复杂，容易断开，难以水平扩展。跨连接的状态管理增加了开销。
- **轮询低效**：前端轮询产生大量网络流量，增加后端负载，提供延迟更新，导致次优的用户体验。
- **紧耦合**：WebSocket在后端和Python服务生命周期之间创建了紧密耦合。
- **可靠性问题**：WebSocket断开可能导致消息丢失或任务不完整，缺乏强大的恢复机制。

---

## 3. 拟议架构

### 3.1 概述

重构后的架构采用异步、事件驱动的方式，使用标准HTTP协议、服务器发送事件（SSE）和云对象存储（COS）。

- 前端 <-> 后端：HTTP用于上传，SSE用于实时更新。
- 后端 <-> Python服务：HTTP异步请求用于任务提交，HTTP回调用于结果/状态。
- 存储：集中于腾讯COS。

### 3.2 架构图

```mermaid
graph LR
    A[前端 (浏览器)] -- 1a. HTTP POST /picture/upload --> B(后端 - Java);
    B -- 1b. 存储图片 --> D{对象存储 (COS)};
    B -- 1c. 返回图片信息（包含图片ID和URL） --> A;
    A -- 2a. HTTP POST /api/reconstruction/create-from-image (包含图片ID) --> B;
    B -- 2b. 返回 TaskID, SSE URL --> A;
    A -- 3. GET /api/reconstruction/events/{taskId} (SSE 连接) --> B;
    B -- 4. HTTP POST /generate3d (异步任务) --> C(Python 服务 - FastAPI);
    C -- 5. (后台处理) --> C;
    C -- 6a. POST /api/reconstruction/callback/result/{taskId} (pixel_images.png) --> B;
    B -- 7a. 存储到 COS, 更新数据库 --> D;
    B -- 8a. 推送 SSE (结果: pixel_images) --> A;
    C -- 6b. POST /api/reconstruction/callback/result/{taskId} (xyz_images.png) --> B;
    B -- 7b. 存储到 COS, 更新数据库 --> D;
    B -- 8b. 推送 SSE (结果: xyz_images) --> A;
    C -- 6c. POST /api/reconstruction/callback/result/{taskId} (output3d.zip) --> B;
    B -- 7c. 存储到 COS, 更新数据库 --> D;
    B -- 8c. 推送 SSE (结果: output3d) --> A;
    C -- 6d. POST /api/reconstruction/callback/status (完成/失败) --> B;
    B -- 7d. 更新数据库状态 --> B;
    B -- 8d. 推送 SSE (状态: 完成/失败) --> A;
```

### 3.3 核心流程

1. **图片上传**：前端通过HTTP POST /picture/upload将图像文件发送到后端。后端将图片存储到COS，在数据库中创建图片记录，并返回包含图片ID和URL的完整图片信息。
2. **任务创建与响应**：前端通过HTTP POST /api/reconstruction/create-from-image发送已上传图片的ID，后端生成唯一taskId，在reconstruction_tasks表中创建记录（状态：PENDING或PROCESSING），并立即向前端返回taskId和sseUrl（例如，/api/reconstruction/events/{taskId}）。
3. **SSE连接**：前端使用收到的sseUrl建立SSE连接（EventSource）与后端端点（GET /api/reconstruction/events/{taskId}）。
4. **任务分发（异步）**：后端异步发送HTTP POST请求到Python服务的/generate3d端点，包含图像数据（或存储在COS中的URL）、taskId和后端的回调基础URL。
5. **Python处理**：Python服务接收任务，向后端的HTTP请求返回已接受状态，并在后台开始3D重建过程。
6. **迭代结果回调**：Python服务生成每个结果文件（如pixel_images.png、xyz_images.png、output3d.zip）时，向后端的回调端点（/api/reconstruction/callback/result/{taskId}）发送HTTP POST请求，通过multipart/form-data发送文件数据。
7. **后端回调处理（结果）**：后端的回调控制器接收文件，将其上传到COS中的指定路径（例如，reconstruction/{taskId}/pixel_images.png），并在reconstruction_tasks表中更新对应taskId的URL字段。
8. **SSE推送（结果）**：成功存储文件并更新数据库后，后端使用SseEmitterService通过对应的SSE连接发送结果事件，包含taskId、文件名和新生成的COS URL。
9. **状态回调**：Python处理完成后（成功或失败），通过HTTP POST请求向后端的状态回调端点（/api/reconstruction/callback/status）发送JSON格式的数据，包含taskId、最终状态（completed或failed）和可选的错误消息。
10. **后端回调处理（状态）**：后端的回调控制器接收状态更新，在reconstruction_tasks表中更新对应taskId的状态和错误消息字段。
11. **SSE推送（状态）**：更新数据库后，后端通过SSE连接发送状态事件，指示完成或失败。如果任务完成，后端可能会关闭对应taskId的SSE连接。
12. **前端更新**：前端的EventSource监听器接收结果和状态事件，更新UI，显示进度、预览图像（通过COS URL逐步可用）、启用最终3D模型的下载/查看器，并显示最终状态/错误消息。

---

## 4. 详细设计

### 4.1 Python服务（FastAPI）

- **职责**：通过HTTP接收图像处理任务，执行3D重建，通过HTTP回调发送结果和状态，管理GPU资源。
- **API端点**：

  - POST /generate3d：
    - 请求：multipart/form-data包含图像（文件）、task_id（字符串）、callback_url（字符串）。
    - 响应（成功 202 Accepted）：{"status": "accepted", "task_id": "..."}。
    - 响应（错误 4xx/5xx）：标准FastAPI错误响应。
  - GET /health：
    - 响应（成功 200 OK）：{"status": "healthy", "models_loaded": true/false}。
    - 响应（错误 503 Service Unavailable）：{"status": "unhealthy", ...}。
- **回调逻辑**：

  - 使用异步HTTP客户端（如httpx）。
  - 结果回调（POST {callback_url}/result/{taskId}）：通过multipart/form-data发送名称（文件名）和文件（二进制内容）。实现失败重试。
  - 状态回调（POST {callback_url}/status）：发送application/json格式的数据，包含{"taskId": "...", "status": "completed|failed", "error": "..."}。实现失败重试。
- **并发**：使用asyncio.Lock序列化对GPU密集型处理步骤的访问。
- **临时文件**：确保清理通过回调发送后生成的任何本地中间文件（如output3d.zip）。

### 4.2 后端服务（Java/Spring Boot）

- **职责**：处理前端请求，管理图片和重建任务生命周期（数据库），与Python服务通信（异步HTTP），处理Python回调，管理SSE连接，与COS交互。
- **关键组件/服务**：
  - PictureController：处理图片上传和管理相关的API端点（/picture/upload、/picture/{id}、/picture/list/page等）。
  - ReconstructionController：处理公共API端点（/api/reconstruction/create-from-image、/api/reconstruction/events、/api/reconstruction/status）。
  - ReconstructionCallbackController：处理由Python服务调用的内部回调端点（/api/reconstruction/callback/result、/api/reconstruction/callback/status）。
  - EventStreamService：管理按taskId映射的SSE连接（SseEmitter实例），处理事件发送、超时和错误。
  - ReconstructionHttpService：客户端服务，通过WebClient或异步RestTemplate/OkHttp发送异步HTTP请求到Python的/generate3d端点。
  - PictureService：管理数据库中Picture实体的CRUD操作。
  - ReconstructionTaskService：管理数据库中ReconstructionTask实体的CRUD操作和状态更新。
  - FileStorageService：用于将文件（InputStreams）上传到腾讯COS并生成URL的接口/实现。
  - ModelService（可选）：管理重建完成后生成的模型数据。

### 4.3 前端（Vue.js）

- **职责**：提供图像上传UI，管理图片库，创建3D重建任务，建立/管理SSE连接，监听并响应服务器事件，逐步显示进度和结果，处理错误。
- **关键组件**：
  - PictureView.vue（图片上传、图片列表显示）
  - ReconstructionView.vue（创建重建任务、状态显示）
  - ModelViewer.vue（显示3D模型）
  - SseService.js（管理SSE连接）
  - Vuex模块（管理图片和重建任务状态）
- **交互流程**：

  - **图片上传流程**：
    - 用户使用文件输入上传图像。
    - 调用后端/picture/upload API。
    - 成功后：存储图片信息（ID、URL等）并显示在图片库中。
  - **3D重建流程**：
    - 用户从图片库中选择图片并点击"创建3D模型"。
    - 调用后端/api/reconstruction/create-from-image API，传入图片ID。
    - 成功后：存储taskId，获取sseUrl。
    - 实例化EventSource(sseUrl)。
    - 实现onopen、onerror、onmessage处理器。
    - onmessage：解析event.type（如果使用）或从event.data（如果是JSON）解析事件名称。解析event.data（JSON）。根据状态和结果事件更新Vuex存储或组件状态（更新状态文本、图像预览src、启用下载/查看器）。
    - onerror：处理连接错误，可能实现重试逻辑或回退到/api/reconstruction/status/{taskId} API。
- **状态管理**：使用Vuex或组件状态跟踪任务状态、结果URL和错误消息。

### 4.4 数据模型（数据库）

- 表：reconstruction_tasks（示例名称，如果有现有表如model、picture可以调整/使用）
- 列：
  - task_id（VARCHAR/UUID，主键）
  - user_id（VARCHAR/BIGINT，外键 - 如果适用）
  - status（VARCHAR/ENUM：PENDING、PROCESSING、COMPLETED、FAILED）
  - original_image_url（VARCHAR，可空） - COS中的URL
  - pixel_images_url（VARCHAR，可空） - COS中的URL
  - xyz_images_url（VARCHAR，可空） - COS中的URL
  - output_zip_url（VARCHAR，可空） - COS中的URL（包含obj、mtl、纹理）
  - error_message（TEXT，可空）
  - created_at（TIMESTAMP）
  - updated_at（TIMESTAMP）
  - （如果需要，考虑添加字段跟踪Python处理的开始/结束时间）

### 4.5 数据模型（对象存储 - COS）

- 存储桶：your-project-bucket（适当配置）
- 路径结构：reconstruction/{taskId}/{filename}
  - 示例：reconstruction/abc-123-def-456/pixel_images.png
  - 示例：reconstruction/abc-123-def-456/output3d.zip
- 权限：适当配置存储桶/对象ACL，用于后端写入访问，可能为前端检索提供公共读取访问（或签名URL）。

---

## 5. 非功能性需求

- **性能**：
  - 后端/upload响应时间：< 500ms（不包括图像传输时间）。
  - SSE事件交付延迟：应接近实时（< 1-2秒，后端处理后）。
  - Python处理时间：取决于模型复杂性和输入图像大小（记录预期范围）。
- **可扩展性**：
  - 后端服务应无状态（除了SSE连接）且水平可扩展。
  - Python服务理想情况下应无状态且水平可扩展（GPU亲和性可能需要考虑）。
  - COS提供了固有的存储可扩展性。
- **可靠性**：
  - 在Python->后端回调中实现带退避的重试。
  - 所有服务中的强大错误处理和日志记录。
  - 前端应优雅处理SSE断开（例如，自动重连或回退）。
  - 考虑数据库事务管理以实现原子更新。
- **安全性**：
  - 安全回调端点：实现机制（如共享密钥、HMAC签名、JWT令牌在头中）以确保回调来自受信任的Python服务。
  - 安全COS访问：使用适当的凭据和策略。如果结果不应公开，则为前端访问生成限时签名URL。
  - 标准安全实践用于API端点（输入验证、身份验证、授权）。

---

## 6. 实施计划（高层次阶段）

- **后端 - API & 核心逻辑**：
  - 实现/picture/upload端点用于图片上传。
  - 实现/api/reconstruction/create-from-image、/api/reconstruction/events、/api/reconstruction/status端点。
  - 实现EventStreamService。
  - 实现数据库实体（ReconstructionTask）和ReconstructionTaskService。
  - 实现FileStorageService以与COS交互。
- **Python服务 - API & 回调**：
  - 实现/generate3d HTTP端点。
  - 集成后台任务处理。
  - 实现HTTP回调逻辑（send_result_callback、send_status_callback）使用httpx。
  - 调整核心AI处理以触发回调。
- **后端 - 回调 & 集成**：
  - 实现ReconstructionCallbackController（/api/reconstruction/callback/result、/api/reconstruction/callback/status）。
  - 实现ReconstructionHttpService调用Python API。
  - 集成组件：/api/reconstruction/create-from-image触发调用Python，回调更新DB/COS并推送SSE事件。
- **前端**：
  - 实现图片上传逻辑，调用/picture/upload API。
  - 实现图片库管理组件，显示已上传的图片。
  - 实现创建重建任务逻辑，调用/api/reconstruction/create-from-image API。
  - 实现SSE连接处理（EventSource）。
  - 实现事件监听器以根据状态和结果事件更新UI状态。
  - 更新结果显示组件（图像预览、3D查看器/下载器）。
- **测试**：
  - 单元测试用于单个组件/服务。
  - 集成测试用于后端 <-> Python通信（包括回调）。
  - 端到端测试模拟用户流程。
  - 负载/压力测试（可选但推荐）。
- **部署**：部署更新后的后端、Python服务和前端。配置环境变量（COS密钥、回调URL等）。

---

## 7. 风险和缓解措施

- **风险**：Python服务回调失败（网络问题、后端宕机）。
  - **缓解措施**：在Python的回调机制中实现带指数退避的强大重试逻辑。持久记录失败。考虑死信队列或手动对账流程以应对严重失败。
- **风险**：后端在回调处理期间失败（COS错误、数据库错误）。
  - **缓解措施**：后端回调端点应在可能的情况下保持幂等性。向Python返回适当的5xx错误以触发重试。实现详细的日志记录和监控。
- **风险**：SSE连接断开；前端错过更新。
  - **缓解措施**：前端EventSource应处理onerror并尝试重新连接。提供可选的GET /api/reconstruction/status/{taskId}端点作为用户手动检查/刷新状态的回退。
- **风险**：Python处理挂起或静默失败，未发送最终状态回调。
  - **缓解措施**：在后端实现超时机制。如果在任务分发后合理时间内未收到完成或失败回调，将任务标记为FAILED（超时）在数据库中，并通过SSE通知前端。
- **风险**：回调端点存在安全漏洞。
  - **缓解措施**：为回调端点实现身份验证/授权（例如，基于共享密钥的HMAC签名检查）。

---

## 8. 开放问题/未来考虑

- 详细的重试策略（尝试次数、退避因子）。
- 回调的具体安全机制（HMAC、Token？）。
- 如果负载显著增加或未来需要更复杂的任务编排，是否需要专用的消息队列（如RabbitMQ、Kafka）？（当前的HTTP回调在此特定用例中更简单）。
- 显示进度和中间结果的详细UI设计。
- 监控和告警策略。
