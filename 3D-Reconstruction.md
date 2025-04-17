服务端设计文档 (Python FastAPI + WebSocket) - 修订版

1. 目标

提供一个单一的 WebSocket 端点。客户端连接后，发送图片二进制数据。服务端使用默认参数执行 AI 模型推理（图像预处理、pipeline、3D 生成）。服务端通过同一 WebSocket 连接，依次发送：

    标识下一个数据块内容的 JSON 消息 ({"type": "result_part", "name": "pixel_images.png", ...})

    生成的 pixel_images.png 的二进制数据

    标识下一个数据块内容的 JSON 消息 ({"type": "result_part", "name": "xyz_images.png", ...})

    生成的 xyz_images.png 的二进制数据

    标识下一个数据块内容的 JSON 消息 ({"type": "result_part", "name": "output3d.zip", ...})

    生成的 output3d.zip 的二进制数据

    最终表示任务完成的 JSON 状态消息 ({"type": "status", "status": "completed"})

服务端不存储任何跨连接的任务状态或文件（generate3d 生成的临时文件在发送后会被清理）。

2. 架构

   Web框架: FastAPI (提供 WebSocket 端点)

   Web服务器: Uvicorn (运行 FastAPI 应用)

   通信协议: WebSocket (用于建立持久连接、双向传输二进制数据和多种 JSON 消息)

   核心依赖: PyTorch, PIL, NumPy, Rembg, io, json, asyncio, 以及你的项目特定库 (imagedream, model, pipelines, inference) (zipfile 可能仍被 generate3d 内部使用，但服务端本身不再进行最终打包)

   并发控制: 使用 asyncio.Lock 确保同一时间只有一个 WebSocket 连接在执行耗时的模型推理和结果发送部分（保护 GPU 资源）。

3. WebSocket 端点

   WS /generate3d: 处理 3D 生成任务的 WebSocket 连接

        连接生命周期:

            连接建立: 客户端向 ws://<server_ip>:<port>/generate3d 发起连接，服务端接受。

            接收图片: 服务端等待接收第一个二进制消息帧，并假定其为完整的图片数据。

            发送处理中状态: 收到图片后，服务端立即发送一条 JSON 文本消息，告知客户端处理已开始，例如：{"type": "status", "status": "processing"}。

            执行AI处理与分步发送 (受锁保护):

                获取 asyncio.Lock。

                从内存中的图片数据加载 (io.BytesIO, PIL.Image)。

                执行 preprocess_image (使用默认参数)。

                执行 pipeline (使用默认参数 scale, step)，获取 np_imgs_combined 和 np_xyzs_combined。

                发送 Pixel Images:

                    将 np_imgs_combined 转换为 PNG 格式保存在内存 (io.BytesIO)。

                    发送标识 JSON: {"type": "result_part", "name": "pixel_images.png", "content_type": "image/png"}。

                    发送内存中的 PNG 二进制数据。

                    释放 PNG 内存。

                发送 XYZ Images:

                    将 np_xyzs_combined 转换为 PNG 格式保存在内存 (io.BytesIO)。

                    发送标识 JSON: {"type": "result_part", "name": "xyz_images.png", "content_type": "image/png"}。

                    发送内存中的 PNG 二进制数据。

                    释放 PNG 内存。

                发送 3D Output:

                    执行 generate3d (使用默认参数)，获取生成的 output3d.zip 文件路径 (该文件可能位于临时位置)。

                    发送标识 JSON: {"type": "result_part", "name": "output3d.zip", "content_type": "application/zip"}。

                    从磁盘读取 output3d.zip 文件内容。

                    发送 ZIP 文件的二进制数据。

                    清理: 删除磁盘上的临时 output3d.zip 文件。

                释放 asyncio.Lock。

            发送完成状态: 在成功发送完所有三个结果部分后，发送一条 JSON 文本消息，告知客户端任务成功，例如：{"type": "status", "status": "completed"}。

            (可选) 关闭连接: 服务器可以选择在发送完成状态后关闭连接。

        错误处理:

            如果在处理过程中的任何步骤（图片解码、AI处理、任何一部分结果的生成或发送）发生错误，服务端应捕获异常。

            立即发送一条 JSON 文本消息，告知客户端任务失败及错误原因（如果连接仍然可用），例如：{"type": "status", "status": "failed", "error": "Detailed error message"}。

            处理流程终止，不再发送后续结果部分或 "completed" 状态。

            服务器随后可以关闭连接。

        消息格式:

            客户端 -> 服务器:

                单个二进制消息帧 (图片数据)。

            服务器 -> 客户端 (按顺序):

                JSON 文本消息 (初始状态): {"type": "status", "status": "processing"}

                JSON 文本消息 (结果部分标识): {"type": "result_part", "name": "pixel_images.png", "content_type": "image/png"}

                单个二进制消息帧 (pixel_images.png 数据)

                JSON 文本消息 (结果部分标识): {"type": "result_part", "name": "xyz_images.png", "content_type": "image/png"}

                单个二进制消息帧 (xyz_images.png 数据)

                JSON 文本消息 (结果部分标识): {"type": "result_part", "name": "output3d.zip", "content_type": "application/zip"}

                单个二进制消息帧 (output3d.zip 数据)

                JSON 文本消息 (最终状态): {"type": "status", "status": "completed"}

                或者 (出错时):

                    在出错点之后，发送 JSON 文本消息 (错误状态): {"type": "status", "status": "failed", "error": "..."}

4. 关键实现细节

   无状态: 服务器不维护任何 task_id 或跨连接的任务状态。每个 WebSocket 连接处理一个独立的任务流。

   默认参数: 所有 AI 处理步骤的参数均使用代码中硬编码的默认值。

   内存与临时文件处理: 输入图片在内存中处理。中间 PNG 结果在内存中生成后立即发送并释放。generate3d 的输出 (output3d.zip) 产生在磁盘临时位置，读取发送后必须清理。需要监控内存和临时磁盘空间使用。

   渐进式发送: 结果按顺序分部分发送，利用 WebSocket 的多消息能力，允许客户端实现渐进式加载。

   并发与排队: FastAPI 可同时接受多个 WebSocket 连接。asyncio.Lock 确保模型推理和结果发送部分的串行执行，形成隐式的处理队列。

   模型加载: AI 模型在 FastAPI 应用启动时全局加载一次。加载失败会阻止后续处理。

   原子性: 整个任务的成功由最后的 completed 状态标识。如果在发送任何一部分时失败，客户端应认为整个任务失败。

   超时: 考虑 WebSocket 连接空闲超时和模型推理超时。

客户端设计文档 (Java + OkHttp WebSocket Client) - 修订版

1. 目标

使用 OkHttp WebSocket 客户端连接到 Python 服务端点，发送图片二进制数据，接收状态更新，并按顺序接收服务端分步发送的多个结果部分（PNG 图像和 ZIP 文件），根据标识信息进行处理和展示/保存。

2. 连接策略

   单一连接模式: 建议维护一个 WebSocket 连接处理所有请求。

   按需连接: 首次请求或断线后尝试连接。

   最小重试: 连接失败时快速反馈。

   心跳检测: （可选）使用 OkHttp 的 ping 机制维持连接活跃。

3. 交互流程

   准备图片: 获取待处理图片。

   检查/建立连接: 尝试连接 ws://<server_ip>:<port>/generate3d。失败则通知用户。

   发送图片: 读取图片为 byte[]，通过 WebSocket 发送二进制消息。（可选） 为此请求生成本地上下文或 ID，用于追踪后续响应。

   处理服务器消息 (核心变化): 在 WebSocket 的 onMessage 回调中处理：

        文本消息处理:

            解析收到的 JSON 文本。

            如果 {"type": "status"}:

                根据 status 值 ("processing", "completed", "failed") 更新 UI 或请求状态。

                如果是 "completed"，表示所有结果部分已成功接收（或至少服务端已发送完毕），标记整个请求成功完成。

                如果是 "failed"，根据 error 信息通知用户，标记整个请求失败。

            如果 {"type": "result_part"}:

                记录状态: 暂存 name (e.g., "pixel_images.png") 和 content_type。这表明下一个接收到的二进制消息将是这个文件的数据。

        二进制消息处理 (onMessage 收到 ByteString 或 byte[]):

            关联数据: 使用上一个收到的 result_part 消息中记录的 name 和 content_type 来识别这个二进制数据块。

            处理数据:

                如果 name 是 "pixel_images.png" 或 "xyz_images.png"，将 byte[] 解码为图像，更新 UI 显示。

                如果 name 是 "output3d.zip"，将 byte[] 提供给用户下载或进行后续处理。

            清除状态: 处理完二进制数据后，清除暂存的 result_part 信息，准备接收下一个 result_part 或 status 消息。

   错误处理:

        连接失败/发送失败：通知用户。

        收到 {"status": "failed"} 消息：标记请求失败。

        连接意外关闭 (onClosed, onFailure)：处理所有进行中的请求，标记为失败。

        协议错误（如收到未预期的二进制数据，即没有先收到 result_part 标识）：记录错误，可能需要重置状态或断开连接。

4. 关键实现细节

   OkHttp WebSocket 实现:

        使用 OkHttp 库提供的 WebSocket 和 WebSocketListener 接口。

        在 WebSocketListener 的 onOpen, onMessage(String text), onMessage(ByteString bytes), onClosing, onClosed, onFailure 回调方法中实现核心的连接管理和消息处理逻辑。

   客户端请求流控制 (重要):

        推荐策略：单一活动请求。 在客户端（例如 ViewModel 或 Presenter 层）维护一个状态标记，表示当前是否有请求正在进行中（从发送图片到收到最终 completed 或 failed 状态）。

        当用户尝试发起新请求时，检查此状态：

            如果空闲，则发送图片，并将状态标记为“处理中”。

            如果“处理中”，则阻止发送新请求（例如，禁用按钮，显示提示信息），避免向服务端并发发送多个处理任务。

        当收到 completed 或 failed 状态消息后，将状态标记改回“空闲”，允许用户发起新请求。

        这种方式简化了客户端逻辑，与服务端串行处理模型保持一致。

   消息处理与状态机 (针对渐进式结果):

        在 WebSocketListener 实现内部（或其关联的处理器类中），维护一个临时的状态变量，用于存储上一个收到的 result_part 消息中的 name 和 content_type。

        onMessage(String text):

            解析 JSON。

            如果 type == "status"，根据 status 更新全局请求状态（处理中/完成/失败），并相应更新 UI。如果是最终状态，则重置活动请求状态（见上一点）。

            如果 type == "result_part"，将 name 和 content_type 存入上述临时状态变量，准备接收紧随其后的二进制数据。

        onMessage(ByteString bytes):

            检查临时状态变量中是否存有 name 和 content_type。

            如果有，说明这是预期的结果部分数据。根据 name (e.g., "pixel_images.png") 处理 bytes（解码图像、存为文件等），更新对应 UI。

            处理完毕后，必须清除临时状态变量（设为 null 或默认值），表示当前二进制块已处理，等待下一个 result_part 或 status 消息。

            如果没有，则说明收到了未预期的二进制数据，应记录错误。

   渐进式 UI 更新:

        充分利用分步接收的特性。在 onMessage(ByteString bytes) 中成功识别并处理某个结果部分（如 pixel_images.png）后，立即将数据显示在 UI 上（例如，更新 ImageView）。

        当收到 output3d.zip 数据后，可以激活下载按钮或传递给 3D 渲染组件。

        这提供了比等待所有内容完成后一次性展示更快的用户感知响应。

   线程安全:

        OkHttp 的 WebSocketListener 回调方法默认在后台 I/O 线程执行。

        任何需要更新 UI 组件（如 ImageView, TextView, Button 状态）的操作，或者访问/修改需要在主线程维护的共享数据，都必须通过 runOnUiThread (Android), Platform.runLater (JavaFX), SwingUtilities.invokeLater (Swing) 等机制切换回主/UI 线程执行。

   连接管理与资源:

        连接复用: 应用程序生命周期内尽量复用同一个 WebSocket 实例，避免频繁创建和销毁连接带来的网络和系统开销。

        生命周期: 在适当的时机（如应用启动、首次需要连接时）创建 WebSocket 连接，在应用退出或不再需要时调用 websocket.close() 显式关闭连接，释放资源。

        断线处理与重连: 在 onFailure 或 onClosed 回调中处理连接中断。可以实现一个有限次的按需重连机制：当下次需要发送请求时，如果发现连接已断开，则尝试重新建立连接。避免后台无限自动重连。

        心跳/保活: 优先依赖服务端配置的 ws-ping-interval（Uvicorn 默认会发送 Ping）。客户端 OkHttp 默认会响应 Pong。通常不需要客户端实现应用层心跳。如果需要客户端主动发起，可配置 OkHttp 的 pingInterval。

        内存注意: 处理 ByteString/byte[] 数据时，特别是较大的图片或 ZIP 文件，要确保及时处理并允许垃圾回收器回收内存，避免 OOM。解码后的图像对象 (如 Android Bitmap) 也要注意管理。

   超时处理:

        连接建立超时: 配置 OkHttpClient 的 connectTimeoutMillis。

        请求整体超时: 由于 WebSocket 是长连接，依赖 TCP/IP 和 Ping/Pong 保活，传统的读写超时不太适用。应实现一个应用层面的超时监控：在发送图片后启动一个定时器。如果在设定的合理时间内（例如 2-5 分钟，取决于模型处理时间）没有收到最终的 completed 或 failed 状态，则认为任务超时。此时应：

            通知用户任务超时。

            更新内部状态为“失败/超时”。

            主动关闭当前的 WebSocket 连接 (websocket.close()) 以清理状态并中断可能仍在进行的服务器处理（服务器端代码也应能处理客户端断开）。

            重置客户端状态，允许发起新请求。