# 设计文档：3D重建模块设计（V2.0）
## 1. 引言
### 1.1 目的
本文档详细说明了项目中3D重建模块的设计。涵盖了架构、组件职责、API规范、数据模型和实现注意事项，以实现预期的功能和性能。
### 1.2 范围
本文档涵盖端到端3D重建工作流程的设计，包括：
- 前端交互，用于图像上传和结果展示。
- 后端服务逻辑，用于任务管理、通信和存储。
- Python AI服务接口和处理逻辑。
- 前端 <-> 后端 和 后端 <-> Python服务之间的通信协议。
- 集成腾讯云对象存储（COS）。
### 1.3 背景
此次设计旨在通过采用现代且可靠的架构模式，提升系统的可靠性、可扩展性和用户体验。
### 1.4 目标
- 使用HTTP异步请求+回调机制进行后端与Python服务的通信。
- 使用服务器发送事件（SSE）实现实时状态和结果更新。
- 在腾讯云对象存储（COS）上标准化文件存储，移除本地文件依赖。
- 提高系统的可靠性、容错能力和可扩展性。
- 通过更快的反馈和渐进式结果加载提升用户体验。
- 维持或改进Python服务提供的核心3D重建功能。
### 1.5 目标受众
软件开发人员（前端、后端、Python）、质量保证工程师、系统架构师、项目经理。
---
## 2. 架构设计
### 2.1 概述
设计的架构采用异步、事件驱动的方式，使用标准HTTP协议、服务器发送事件（SSE）和云对象存储（COS）。
- 前端 <-> 后端：HTTP用于上传，SSE用于实时更新。
- 后端 <-> Python服务：HTTP异步请求用于任务提交，HTTP回调用于结果/状态。
- 存储：集中于腾讯COS。
### 2.2 架构图
```startuml
@startuml
title 3D重建流程时序图

participant "前端 (浏览器)" as Frontend
participant "后端 - Java" as BackendJava
participant "Python 服务 - FastAPI" as PythonService
participant "对象存储 (COS)" as ObjectStorage
database "数据库 (MySQL)" as Database

skinparam sequenceMessageAlign center

activate Frontend

== 图片上传 ==
Frontend -> BackendJava ++ : POST /picture/upload\n(图片文件, 图片信息)
BackendJava -> ObjectStorage ++ : 存储图片
ObjectStorage --> BackendJava -- : 返回图片URL
BackendJava -> Database ++ : 保存图片记录\n(URL, 用户信息)
Database --> BackendJava -- : 返回图片ID
BackendJava --> Frontend -- : 返回图片ID/URL

== 创建与触发重建任务 ==
Frontend -> BackendJava ++ : POST /api/reconstruction/create\n(图片ID)
BackendJava -> Database ++ : 创建任务记录\n(状态: PENDING)
Database --> BackendJava -- : 返回 TaskID

note over BackendJava : 立即异步调用 Python 服务
BackendJava -> PythonService ++ : POST /generate3d\n(TaskID, 图片URL/Bytes)
PythonService --> BackendJava -- : 响应 202 Accepted\n(任务已接收)
note right of PythonService : 后台开始处理...

' 任务已接收，后端返回 TaskID 给前端
BackendJava --> Frontend -- : 返回 TaskID

== 建立 SSE 连接以接收事件 ==
note over Frontend : 前端使用 TaskID 建立 SSE 连接
Frontend -> BackendJava ++ : GET /api/reconstruction/events/{taskId}\n(建立 SSE 连接)
' 后端保持连接，准备推送事件

== Python 服务回调与结果处理 ==
note over PythonService, BackendJava : Python 服务处理中...\n完成后回调 Java 后端

PythonService -> BackendJava ++ : POST /callback/result/{taskId}\n(含 pixel_images.png)
activate BackendJava
BackendJava -> ObjectStorage ++ : 存储 pixel_images.png
ObjectStorage --> BackendJava -- : 返回 pixel_image URL
BackendJava -> Database ++ : 更新任务记录 (pixel_image URL)
Database --> BackendJava --
BackendJava -> Frontend : SSE 推送: Pixel图 URL
deactivate BackendJava

PythonService -> BackendJava ++ : POST /callback/result/{taskId}\n(含 xyz_images.png)
activate BackendJava
BackendJava -> ObjectStorage ++ : 存储 xyz_images.png
ObjectStorage --> BackendJava -- : 返回 xyz_image URL
BackendJava -> Database ++ : 更新任务记录 (xyz_image URL)
Database --> BackendJava --
BackendJava -> Frontend : SSE 推送: XYZ图 URL
deactivate BackendJava

PythonService -> BackendJava ++ : POST /callback/result/{taskId}\n(含 output3d.zip)
activate BackendJava
note over BackendJava : **处理 3D 模型**
BackendJava -> BackendJava : 解压 output3d.zip\n(获取 obj, mtl, png)
loop 上传解压后的文件
    BackendJava -> ObjectStorage ++ : 存储 obj/mtl/png 文件
    ObjectStorage --> BackendJava -- : 返回对应文件 URL
end
BackendJava -> Database ++ : 更新任务记录\n(obj, mtl, texture URLs)
Database --> BackendJava --
BackendJava -> Frontend : SSE 推送: 3D模型文件 URLs
deactivate BackendJava

PythonService -> BackendJava ++ : POST /callback/status\n(含 TaskID, Status: COMPLETED/FAILED)
deactivate PythonService
activate BackendJava
BackendJava -> Database ++ : 更新任务最终状态
Database --> BackendJava --
BackendJava -> Frontend -- : SSE 推送: 最终状态 (COMPLETED/FAILED)
note right of BackendJava : 可关闭 SSE 连接
deactivate BackendJava

deactivate Frontend

@enduml
```
### 2.3 核心流程
1. **图片上传**：前端通过HTTP POST /picture/upload将图像文件发送到后端。后端将图片存储到COS，在数据库中创建图片记录，并返回包含图片ID和URL的完整图片信息。
2. **任务创建与响应**：前端通过HTTP POST /reconstruction/create发送已上传图片的ID，后端生成唯一taskId，在reconstruction_tasks表中创建记录（状态：PENDING）。后端获得TaskID后，立即触发异步重建任务，将任务发送到Python服务。Python服务确认接收任务后，后端将taskId返回给前端。
3. **SSE连接**：前端使用返回的taskId构建SSE URL，并建立SSE连接（EventSource）与后端端点（GET /reconstruction/events/{taskId}）。这时任务可能已经在处理中。
4. **Python服务处理**：Python服务在后台处理重建任务，完成后通过回调通知后端。
5. **结果回调处理**：Python服务生成每个结果文件时，向后端发送回调：
  - 先发送pixel_images.png（POST /reconstruction/callback/result/{taskId}），后端将其存储到COS，更新数据库，并通过SSE将URL推送给前端
  - 然后发送xyz_images.png（POST /reconstruction/callback/result/{taskId}），后端同样处理并推送给前端
  - 最后发送output3d.zip（POST /reconstruction/callback/result/{taskId}），后端同样处理并推送给前端
6. **状态回调处理**：Python服务处理完成后，发送最终状态更新（POST /reconstruction/callback/status），包含状态（完成/失败）和可能的错误信息。后端更新数据库中的任务状态，并通过SSE将最终状态推送给前端。
7. **SSE连接完成**：当前端收到最终状态后，可能会关闭SSE连接，或者后端在发送最终状态后关闭与此任务相关的SSE连接。
---
## 3. 详细设计
### 3.1 Python服务（FastAPI）
- **职责**：通过HTTP接收图像处理任务，执行3D重建，通过HTTP回调发送结果和状态，管理GPU资源。
- **API端点**：
  API 文档：图像转 3D 模型服务
  概述
  本服务提供一个 HTTP API，用于接收 2D 图像并异步生成 3D 模型。由于 3D 重建过程耗时较长且占用大量资源（GPU），本服务采用异步处理和回调机制。
  调用者通过 /generate3d 端点提交任务，服务会立即响应表示任务已被接受。处理完成后，服务会将生成的中间文件、最终的 3D 模型（ZIP 压缩包）以及最终处理状态，通过 HTTP POST 请求发送到调用者指定的 callback_url。
  重要： 调用者必须实现一个 HTTP 服务器，用于接收本服务发送的回调请求。
  端点
1. 提交 3D 生成任务
   路径: /generate3d
   方法: POST
   描述: 提交一张图像进行 3D 重建。此请求会立即返回，实际处理在后台进行。
   请求格式: multipart/form-data
   表单参数:
   image (File): 用户上传的图像文件。建议使用常见的格式如 PNG 或 JPEG。图像会被预处理（例如，自动去背景、调整大小、填充为方形）。
   task_id (String): 调用者提供的唯一任务标识符。此 ID 将在后续的回调请求中使用，用于关联结果。调用者必须确保此 ID 的唯一性。
   callback_url (String): 调用者提供的 基础 URL。服务会将结果和状态 POST 到此 URL 的特定子路径下。例如，如果提供 http://example.com/mycallback，结果会发送到 http://example.com/mycallback/result/<task_id>，状态会发送到 http://example.com/mycallback/status。
   响应:
   成功 (200 OK):
   {
   "status": "accepted",
   "task_id": "your_provided_task_id"
   }
   注意: 这仅表示任务已被接收并排队，不代表处理已开始或一定会成功。
   服务不可用 (503 Service Unavailable):
   {
   "detail": "Service Unavailable: Models not loaded."
   // or "Service Unavailable: HTTP client not ready."
   }
   这通常发生在服务启动时模型加载失败，或内部HTTP客户端初始化失败。
   请求无效 (422 Unprocessable Entity):
   如果缺少 image, task_id, 或 callback_url 参数，或者参数类型不正确，FastAPI 会自动返回此错误。
2. 健康检查
   路径: /health
   方法: GET
   描述: 检查服务是否正在运行以及依赖的模型是否已成功加载。
   响应:
   健康 (200 OK):
   {
   "status": "healthy",
   "models_loaded": true
   // "http_client_ready": true (可能包含)
   }
   不健康 (200 OK, 但内容指示问题):
   {
   "status": "unhealthy",
   "models_loaded": false,
   // "http_client_ready": false (可能包含)
   }
   注意: 即使状态是 "unhealthy"，HTTP 状态码仍可能是 200。需要检查 status 字段的值。
   回调机制 (调用者需要实现)
   服务处理完成后，会向调用者提供的 callback_url 发送 HTTP POST 请求。调用者需要实现以下两个端点来接收回调：
   A. 接收结果文件
   路径: <callback_url>/result/<task_id>
   例如，如果 callback_url 是 http://example.com/mycallback，且 task_id 是 job123，则此端点的完整路径是 http://example.com/mycallback/result/job123。
   方法: POST
   请求格式: multipart/form-data
   描述: 服务通过此端点发送生成的中间图像和最终的 3D 模型文件。对于同一个 task_id，此端点可能会被调用多次（一次为一个文件）。
   表单参数:
   file (File): 包含结果文件的部分。包含原始文件名、Content-Type 和文件字节。
   name (String): 结果文件的名称（冗余信息，方便处理）。
   可能收到的文件 (name 字段的值):
   pixel_images.png (Content-Type: image/png): Stage 1 的多视角像素图拼接。
   xyz_images.png (Content-Type: image/png): Stage 2 的多视角 XYZ 图拼接。
   output3d.zip (Content-Type: application/zip): 包含最终 3D 模型（如 .obj, .mtl, 纹理贴图）的 ZIP 压缩包。
   调用者责任:
   实现一个能处理 POST 请求到 /<your_base_path>/result/{task_id} 路径的路由。
   从 multipart/form-data 请求中解析 file 部分和 name 字段。
   根据 task_id 和 name 存储接收到的文件。
   接收成功后，应返回 2xx 状态码（例如 200 OK 或 204 No Content）。如果返回 4xx 或 5xx，或者请求超时（服务端默认 60 秒），服务端可能会认为回调失败。
   B. 接收最终状态
   路径: <callback_url>/status
   例如，如果 callback_url 是 http://example.com/mycallback，则此端点的完整路径是 http://example.com/mycallback/status。
   方法: POST
   请求格式: application/json
   描述: 服务在任务处理完成（无论成功或失败）后，通过此端点发送最终状态。通常在所有结果文件回调发送完毕后（如果成功），或者在处理过程中发生不可恢复错误时发送。
   JSON 请求体:
   {
   "taskId": "your_provided_task_id",
   "status": "completed" // 或者 "failed"
   // "error": "Optional error message string" // 仅在 status 为 "failed" 时出现
   }
   字段说明:
   taskId (String): 对应原始请求中的 task_id。
   status (String): 任务的最终状态，值为 "completed" 或 "failed"。
   error (String, 可选): 如果 status 是 "failed"，此字段可能包含简短的错误描述信息。
   调用者责任:
   实现一个能处理 POST 请求到 /<your_base_path>/status 路径的路由。
   解析 JSON 请求体。
   根据 taskId 更新任务的最终状态，并处理可能的 error 信息。
   接收成功后，应返回 2xx 状态码。处理逻辑同结果文件回调。
   重要注意事项
   异步性: 调用 /generate3d 后，请勿原地等待结果。依赖回调机制来获取产出物和状态。
   回调服务器: 调用者必须部署一个可公开访问的 HTTP(S) 服务器来接收来自本服务的回调请求。确保防火墙允许本服务访问你的 callback_url。
   任务排队: 由于 GPU 资源限制，任务是串行处理的。高并发请求将导致任务排队等待。
   回调超时与失败: 如果调用者的回调端点响应缓慢（超过 60 秒）或返回错误，本服务可能会将该任务标记为失败。请确保回调端点稳定且快速响应。
   Task ID: 务必使用唯一的 task_id 来提交任务，以便正确地将回调结果与原始请求关联起来。
### 3.2 后端服务（Java/Spring Boot）
- **职责**：处理前端请求，管理图片和重建任务生命周期（数据库），与Python服务通信（异步HTTP），处理Python回调，管理SSE连接，与COS交互。
- **关键组件/服务**：
  - PictureController：处理图片上传和管理相关的API端点（/picture/upload、/picture/{id}、/picture/list/page等）。
  - ReconstructionController：处理公共API端点（/reconstruction/create、/reconstruction/events/{taskId}、/reconstruction/status/{taskId}）。
  - ReconstructionCallbackController：处理由Python服务调用的内部回调端点（/reconstruction/callback/result/{taskId}、/reconstruction/callback/status）。
  - EventStreamService：管理按taskId映射的SSE连接（SseEmitter实例），处理事件发送、超时和错误。
  - ReconstructionHttpService：客户端服务，通过WebClient或异步RestTemplate/OkHttp发送异步HTTP请求到Python的/generate3d端点。
  - PictureService：管理数据库中Picture实体的CRUD操作。
  - ReconstructionTaskService：管理数据库中ReconstructionTask实体的CRUD操作和状态更新。
  - FileStorageService：用于将文件（InputStreams）上传到腾讯COS并生成URL的接口/实现。
  - ModelService（可选）：管理重建完成后生成的模型数据。
### 3.3 前端（Vue.js）
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
    - 调用后端/reconstruction/create API，传入图片ID。
    - 成功后：存储taskId。
    - 使用taskId构建SSE URL，并实例化EventSource。
    - 实现事件监听器：
      - connect：处理连接成功事件
      - status：处理状态更新事件（PENDING、PROCESSING、COMPLETED、FAILED）
      - result：处理结果文件事件（pixel_images.png、xyz_images.png、output3d.zip）
      - error：处理错误事件
      - heartbeat：处理心跳事件，保持连接活跃
    - 根据状态和结果事件更新UI（更新状态文本、图像预览、启用下载/查看器）。
    - 当收到最终状态（COMPLETED或FAILED）时，可能关闭SSE连接。
- **状态管理**：使用Vuex或组件状态跟踪任务状态、结果URL和错误消息。
### 3.4 数据模型（数据库）
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
  - processing_time（INTEGER，可空）- 处理时间（秒）
### 3.5 数据模型（对象存储 - COS）
- 存储桶：your-project-bucket（适当配置）
- 路径结构：reconstruction/{taskId}/{filename}
  - 示例：reconstruction/abc-123-def-456/pixel_images.png
  - 示例：reconstruction/abc-123-def-456/xyz_images.png
  - 示例：reconstruction/abc-123-def-456/output3d.zip
- 权限：适当配置存储桶/对象ACL，用于后端写入访问，可能为前端检索提供公共读取访问（或签名URL）。
---
## 4. 非功能性需求
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
## 5. 实施计划（高层次阶段）
- **后端 - API & 核心逻辑**：
  - 实现/picture/upload端点用于图片上传。
  - 实现/reconstruction/create、/reconstruction/events/{taskId}、/reconstruction/status/{taskId}端点。
  - 实现EventStreamService。
  - 实现数据库实体（ReconstructionTask）和ReconstructionTaskService。
  - 实现FileStorageService以与COS交互。
- **Python服务 - API & 回调**：
  - 实现/generate3d HTTP端点。
  - 集成后台任务处理。
  - 实现HTTP回调逻辑（send_result_callback、send_status_callback）使用httpx。
  - 调整核心AI处理以触发回调。
- **后端 - 回调 & 集成**：
  - 实现ReconstructionCallbackController（/reconstruction/callback/result/{taskId}、/reconstruction/callback/status）。
  - 实现ReconstructionHttpService调用Python API。
  - 集成组件：/reconstruction/create触发调用Python，回调更新DB/COS并推送SSE事件。
- **前端**：
  - 实现图片上传逻辑，调用/picture/upload API。
  - 实现图片库管理组件，显示已上传的图片。
  - 实现创建重建任务逻辑，调用/reconstruction/create API。
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
## 6. 风险和缓解措施
- **风险**：Python服务回调失败（网络问题、后端宕机）。
  - **缓解措施**：在Python的回调机制中实现带指数退避的强大重试逻辑。持久记录失败。考虑死信队列或手动对账流程以应对严重失败。
- **风险**：后端在回调处理期间失败（COS错误、数据库错误）。
  - **缓解措施**：后端回调端点应在可能的情况下保持幂等性。向Python返回适当的5xx错误以触发重试。实现详细的日志记录和监控。
- **风险**：SSE连接断开；前端错过更新。
  - **缓解措施**：前端EventSource应处理onerror并尝试重新连接。提供可选的GET /reconstruction/status/{taskId}端点作为用户手动检查/刷新状态的回退。
- **风险**：Python处理挂起或静默失败，未发送最终状态回调。
  - **缓解措施**：在后端实现超时机制。如果在任务分发后合理时间内未收到完成或失败回调，将任务标记为FAILED（超时）在数据库中，并通过SSE通知前端。
- **风险**：回调端点存在安全漏洞。
  - **缓解措施**：为回调端点实现身份验证/授权（例如，基于共享密钥的HMAC签名检查）。
---
## 7. 开放问题/未来考虑
- 详细的重试策略（尝试次数、退避因子）。
- 回调的具体安全机制（HMAC、Token？）。
- 如果负载显著增加或未来需要更复杂的任务编排，是否需要专用的消息队列（如RabbitMQ、Kafka）？（当前的HTTP回调在此特定用例中更简单）。
- 显示进度和中间结果的详细UI设计。
- 监控和告警策略。