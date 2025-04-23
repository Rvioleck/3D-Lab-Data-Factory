# Python 3D重建服务测试工具 (IP版)

这个测试工具用于直接测试Python 3D重建服务的接口，包括发送请求和接收回调。它由一个HTML测试页面和一个Node.js回调服务器组成。

## 功能

- 直接向Python服务发送图片进行3D重建
- 接收Python服务的回调（结果文件和状态更新）
- 实时显示处理进度和结果
- 查看和下载生成的3D模型文件

## 前提条件

- Node.js (v14+)
- npm
- Python 3D重建服务已启动并运行

## 安装和运行

1. 安装依赖：

```bash
npm install express cors multer
```

2. 启动回调服务器（IP版）：

```bash
npm start
```

或者启动原始版本：

```bash
npm run start:original
```

3. 在浏览器中打开测试页面：

```
# IP版（推荐，解决回调问题）
http://localhost:3000/python-service-test-ip.html

# 原始版本
http://localhost:3000/python-service-test.html
```

## 使用说明

1. 配置：
   - Python服务URL：输入Python 3D重建服务的URL（默认为`http://10.0.0.2:8001/generate3d`）
   - 回调服务URL：输入回调服务的URL（默认为`http://10.0.0.123:3000/callback`）
   - **重要**：使用内网IP而不是localhost，确保Python服务可以访问回调服务

2. 上传图片：
   - 点击"选择图片"按钮，选择一张图片
   - 图片预览会显示在下方

3. 发送请求：
   - 点击"发送到Python服务"按钮
   - 系统会生成一个任务ID并发送请求到Python服务

4. 查看结果：
   - 日志区域会显示请求和回调的详细信息
   - 右侧区域会显示任务状态和结果文件
   - 当收到结果文件时，会自动显示图像和下载链接

## 回调接口说明

回调服务器提供以下接口：

1. 结果文件回调：
   - URL: `http://10.0.0.123:3000/callback/result/:taskId`
   - 方法: POST
   - 格式: multipart/form-data
   - 参数:
     - file: 文件数据
     - name: 文件名称

2. 状态更新回调：
   - URL: `http://10.0.0.123:3000/callback/status`
   - 方法: POST
   - 格式: application/json
   - 参数:
     - taskId: 任务ID
     - status: 任务状态（"completed"或"failed"）
     - error: 错误信息（可选，仅在status为"failed"时）

## 为什么需要使用IP版？

在分布式环境中，使用`localhost`会导致问题，因为：

1. 当Python服务收到带有`localhost`的回调URL时，它会尝试连接到自己的本地服务器，而不是发送请求的机器
2. 这会导致回调请求失败，因为Python服务器上没有运行回调服务

IP版解决了这个问题：
- 使用内网IP（10.0.0.123）替代localhost
- 确保Python服务可以正确地将回调请求发送到运行回调服务的机器
- 服务器监听所有网络接口（0.0.0.0），使其可以从其他机器访问

## 测试流程

1. 启动Python 3D重建服务
2. 启动回调服务器（IP版）
3. 打开测试页面（IP版）
4. 上传图片并发送到Python服务
5. 观察回调结果和生成的3D模型

## 故障排除

- 如果无法连接到SSE服务，请确保回调服务器已启动
- 如果无法发送请求到Python服务，请检查Python服务URL是否正确
- 如果没有收到回调，请检查回调服务URL是否正确，以及Python服务是否能够访问回调服务
- 如果使用防火墙，请确保允许Python服务访问回调服务的端口（3000）
- 如果仍然无法接收回调，请尝试使用`ping`或`telnet`测试Python服务器是否可以访问回调服务器

## 注意事项

- 测试工具仅用于开发和测试环境
- 上传的图片和生成的文件会保存在`uploads`目录中
- 重启回调服务器会清除所有任务状态和结果
- 确保您的内网IP（10.0.0.123）是正确的，如果不是，请修改代码中的IP地址
