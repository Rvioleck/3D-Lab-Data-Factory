const express = require('express');
const cors = require('cors');
const multer = require('multer');
const path = require('path');
const fs = require('fs');

// 创建Express应用
const app = express();
const port = 3000;
const host = '0.0.0.0'; // 监听所有网络接口，使其可以从其他机器访问

// 启用CORS
app.use(cors());

// 解析JSON请求体
app.use(express.json());

// 解析URL编码的请求体
app.use(express.urlencoded({ extended: true }));

// 配置文件上传
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    const dir = path.join(__dirname, 'uploads');
    if (!fs.existsSync(dir)) {
      fs.mkdirSync(dir, { recursive: true });
    }
    cb(null, dir);
  },
  filename: function (req, file, cb) {
    const taskId = req.params.taskId || 'unknown';
    cb(null, `${taskId}_${file.originalname}`);
  }
});

const upload = multer({ storage: storage });

// 存储SSE客户端连接
const clients = [];

// 存储任务状态和结果
const tasks = new Map();

// 静态文件服务
app.use(express.static(__dirname));

// SSE端点
app.get('/events', (req, res) => {
  // 设置SSE头
  res.writeHead(200, {
    'Content-Type': 'text/event-stream',
    'Cache-Control': 'no-cache',
    'Connection': 'keep-alive'
  });

  // 发送初始连接成功事件
  res.write(`event: connect\ndata: Connected successfully\n\n`);

  // 将客户端添加到连接列表
  const clientId = Date.now();
  const newClient = {
    id: clientId,
    res
  };
  clients.push(newClient);

  console.log(`客户端 ${clientId} 已连接，当前连接数: ${clients.length}`);

  // 发送心跳以保持连接
  const heartbeatInterval = setInterval(() => {
    res.write(': heartbeat\n\n');
  }, 30000);

  // 当客户端断开连接时清理
  req.on('close', () => {
    console.log(`客户端 ${clientId} 已断开连接`);
    clearInterval(heartbeatInterval);
    const index = clients.findIndex(client => client.id === clientId);
    if (index !== -1) {
      clients.splice(index, 1);
      console.log(`客户端已移除，当前连接数: ${clients.length}`);
    }
  });
});

// 回调接口 - 接收结果文件
app.post('/callback/result/:taskId', upload.single('file'), (req, res) => {
  const taskId = req.params.taskId;
  const name = req.body.name;
  const file = req.file;

  console.log(`收到任务 ${taskId} 的结果文件: ${name}`);

  if (!file) {
    return res.status(400).json({ error: '没有文件上传' });
  }

  // 存储文件信息
  if (!tasks.has(taskId)) {
    tasks.set(taskId, { files: {} });
  }
  
  const task = tasks.get(taskId);
  // 使用内网IP而不是localhost
  const fileUrl = `http://10.0.0.123:${port}/uploads/${file.filename}`;
  
  task.files[name] = {
    url: fileUrl,
    path: file.path
  };

  // 向所有客户端发送结果事件
  const eventData = {
    taskId: taskId,
    name: name,
    url: fileUrl
  };
  
  sendEventToAll('result', eventData);

  res.status(200).json({
    status: 'success',
    taskId: taskId,
    name: name,
    url: fileUrl
  });
});

// 回调接口 - 接收状态更新
app.post('/callback/status', (req, res) => {
  const { taskId, status, error } = req.body;

  console.log(`收到任务 ${taskId} 的状态更新: ${status}`);

  // 存储任务状态
  if (!tasks.has(taskId)) {
    tasks.set(taskId, { status: status });
  } else {
    tasks.get(taskId).status = status;
  }

  if (error) {
    tasks.get(taskId).error = error;
  }

  // 向所有客户端发送状态事件
  const eventData = {
    taskId: taskId,
    status: status
  };
  
  if (error) {
    eventData.error = error;
  }
  
  sendEventToAll('status', eventData);

  res.status(200).json({
    status: 'success',
    taskId: taskId,
    message: '状态已更新'
  });
});

// 提供上传的文件
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// 向所有SSE客户端发送事件
function sendEventToAll(event, data) {
  const eventString = `event: ${event}\ndata: ${JSON.stringify(data)}\n\n`;
  clients.forEach(client => {
    client.res.write(eventString);
  });
}

// 启动服务器
app.listen(port, host, () => {
  console.log(`测试服务器运行在 http://${host}:${port}`);
  console.log(`本机访问: http://localhost:${port}`);
  console.log(`内网访问: http://10.0.0.123:${port}`);
  console.log(`SSE端点: http://10.0.0.123:${port}/events`);
  console.log(`回调接口 - 结果文件: http://10.0.0.123:${port}/callback/result/:taskId`);
  console.log(`回调接口 - 状态更新: http://10.0.0.123:${port}/callback/status`);
});
