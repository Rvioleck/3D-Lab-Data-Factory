<template>
  <div class="reconstruction-container">
    <h1 class="page-title">3D重建</h1>

    <div class="section upload-section">
      <h2>上传图片</h2>
      <div class="upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleFileDrop">
        <input
          type="file"
          ref="fileInput"
          accept="image/*"
          style="display: none"
          @change="handleFileChange"
        />
        <div v-if="!selectedFile" class="upload-placeholder">
          <i class="el-icon-upload"></i>
          <div class="upload-text">点击或拖拽图片到此处上传</div>
          <div class="upload-hint">支持JPG、PNG格式，单张图片不超过10MB</div>
        </div>
        <div v-else class="preview-container">
          <img :src="previewUrl" class="image-preview" />
          <div class="file-info">
            <span>{{ selectedFile.name }}</span>
            <span>{{ formatFileSize(selectedFile.size) }}</span>
          </div>
          <el-button type="danger" size="small" @click.stop="removeFile">移除</el-button>
        </div>
      </div>
      <div class="upload-actions" v-if="selectedFile">
        <el-input
          v-model="modelName"
          placeholder="请输入模型名称（可选）"
          class="model-name-input"
        ></el-input>
        <el-button
          type="primary"
          :loading="uploading"
          :disabled="!selectedFile || uploading"
          @click="uploadImage"
        >
          {{ uploading ? '上传中...' : '开始上传' }}
        </el-button>
      </div>
    </div>

    <div class="section task-section" v-if="uploadedImageId">
      <h2>创建重建任务</h2>
      <div class="task-info">
        <div class="image-info">
          <img :src="uploadedImageUrl" class="uploaded-image" />
          <div>
            <div class="image-name">{{ uploadedImageName }}</div>
            <div class="image-id">ID: {{ uploadedImageId }}</div>
          </div>
        </div>
        <el-button
          type="primary"
          :loading="creating"
          :disabled="creating || taskId"
          @click="createReconstructionTask"
        >
          {{ creating ? '创建中...' : '创建重建任务' }}
        </el-button>
      </div>
    </div>

    <div class="section progress-section" v-if="taskId">
      <h2>重建进度</h2>
      <div class="task-progress">
        <div class="task-header">
          <div class="task-id">任务ID: {{ taskId }}</div>
          <div class="task-status" :class="statusClass">{{ taskStatusText }}</div>
        </div>

        <el-progress
          :percentage="progressPercentage"
          :status="progressStatus"
        ></el-progress>

        <div class="status-message" v-if="statusMessage">{{ statusMessage }}</div>
      </div>
    </div>

    <div class="section result-section" v-if="hasResults">
      <h2>重建结果</h2>
      <div class="result-files">
        <div class="result-file" v-for="(file, index) in resultFiles" :key="index">
          <div class="file-preview">
            <img v-if="file.type === 'image'" :src="file.url" class="result-image" />
            <div v-else class="file-icon">
              <i class="el-icon-document"></i>
            </div>
          </div>
          <div class="file-name">{{ file.name }}</div>
          <div class="file-actions">
            <el-button type="text" @click="previewFile(file)">预览</el-button>
            <el-button type="text" @click="downloadFile(file)">下载</el-button>
          </div>
        </div>
      </div>

      <div class="model-viewer" v-if="modelUrl">
        <h3>3D模型预览</h3>
        <div class="viewer-container">
          <!-- 这里将来可以集成Three.js或其他3D查看器 -->
          <div class="placeholder-viewer">
            <p>3D模型查看器将在此处显示</p>
            <el-button type="primary" @click="downloadModel">下载模型</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      title="文件预览"
      :visible.sync="previewDialogVisible"
      width="70%"
    >
      <div class="preview-dialog-content">
        <img v-if="previewFile && previewFile.type === 'image'" :src="previewFile.url" class="preview-dialog-image" />
        <div v-else class="preview-dialog-message">
          <p>此文件类型不支持预览，请下载后查看</p>
          <el-button type="primary" @click="downloadFile(previewFile)">下载文件</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'Reconstruction',
  data() {
    return {
      // 文件上传相关
      selectedFile: null,
      previewUrl: '',
      modelName: '',
      uploading: false,

      // 上传后的图片信息
      uploadedImageId: null,
      uploadedImageUrl: '',
      uploadedImageName: '',

      // 任务相关
      creating: false,
      taskId: null,
      taskStatus: 'PENDING',
      statusMessage: '',
      eventSource: null,

      // 结果相关
      resultFiles: [],
      modelUrl: '',

      // 预览对话框
      previewDialogVisible: false,
      previewFile: null
    };
  },
  computed: {
    progressPercentage() {
      switch (this.taskStatus) {
        case 'PENDING':
          return 0;
        case 'PROCESSING':
          return 50;
        case 'COMPLETED':
          return 100;
        case 'FAILED':
          return 100;
        default:
          return 0;
      }
    },
    progressStatus() {
      if (this.taskStatus === 'COMPLETED') return 'success';
      if (this.taskStatus === 'FAILED') return 'exception';
      return '';
    },
    statusClass() {
      return `status-${this.taskStatus.toLowerCase()}`;
    },
    taskStatusText() {
      const statusMap = {
        'PENDING': '等待处理',
        'PROCESSING': '处理中',
        'COMPLETED': '已完成',
        'FAILED': '处理失败'
      };
      return statusMap[this.taskStatus] || this.taskStatus;
    },
    hasResults() {
      return this.resultFiles.length > 0 || this.modelUrl;
    }
  },
  methods: {
    // 文件上传相关方法
    triggerFileInput() {
      this.$refs.fileInput.click();
    },
    handleFileChange(event) {
      const file = event.target.files[0];
      if (file) {
        this.processSelectedFile(file);
      }
    },
    handleFileDrop(event) {
      const file = event.dataTransfer.files[0];
      if (file) {
        this.processSelectedFile(file);
      }
    },
    processSelectedFile(file) {
      // 检查文件类型
      if (!file.type.startsWith('image/')) {
        this.$message.error('请上传图片文件');
        return;
      }

      // 检查文件大小
      if (file.size > 10 * 1024 * 1024) { // 10MB
        this.$message.error('图片大小不能超过10MB');
        return;
      }

      this.selectedFile = file;
      this.previewUrl = URL.createObjectURL(file);
    },
    removeFile() {
      this.selectedFile = null;
      this.previewUrl = '';
      this.modelName = '';
    },
    formatFileSize(size) {
      if (size < 1024) {
        return size + ' B';
      } else if (size < 1024 * 1024) {
        return (size / 1024).toFixed(2) + ' KB';
      } else {
        return (size / (1024 * 1024)).toFixed(2) + ' MB';
      }
    },

    // 上传图片
    async uploadImage() {
      if (!this.selectedFile) return;

      this.uploading = true;

      try {
        const formData = new FormData();
        formData.append('file', this.selectedFile);

        const response = await axios.post('/api/picture/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        });

        if (response.data.code === 0) {
          this.$message.success('图片上传成功');
          const imageData = response.data.data;
          this.uploadedImageId = imageData.id;
          this.uploadedImageUrl = imageData.url;
          this.uploadedImageName = this.selectedFile.name;
        } else {
          this.$message.error(`上传失败: ${response.data.message}`);
        }
      } catch (error) {
        console.error('上传图片出错:', error);
        this.$message.error('上传图片失败，请重试');
      } finally {
        this.uploading = false;
      }
    },

    // 创建重建任务
    async createReconstructionTask() {
      if (!this.uploadedImageId) return;

      this.creating = true;

      try {
        const params = new URLSearchParams();
        params.append('imageId', this.uploadedImageId);
        if (this.modelName) {
          params.append('name', this.modelName);
        }

        const response = await axios.post('/api/reconstruction/create', params);

        if (response.data.code === 0) {
          this.$message.success('重建任务创建成功');
          const taskData = response.data.data;
          this.taskId = taskData.taskId;

          // 建立SSE连接
          const sseUrl = `/api/reconstruction/events/${taskData.taskId}`;
          this.connectToEventStream(sseUrl);

          // 初始查询任务状态
          this.queryTaskStatus();
        } else {
          this.$message.error(`创建任务失败: ${response.data.message}`);
        }
      } catch (error) {
        console.error('创建重建任务出错:', error);
        this.$message.error('创建重建任务失败，请重试');
      } finally {
        this.creating = false;
      }
    },

    // 查询任务状态
    async queryTaskStatus() {
      if (!this.taskId) return;

      try {
        const response = await axios.get(`/api/reconstruction/status/${this.taskId}`);

        if (response.data.code === 0) {
          const taskData = response.data.data;
          this.taskStatus = taskData.status;

          // 如果有结果文件，添加到结果列表
          this.updateResultFiles(taskData);
        }
      } catch (error) {
        console.error('查询任务状态出错:', error);
      }
    },

    // 连接到事件流
    connectToEventStream(sseUrl) {
      // 关闭之前的连接
      if (this.eventSource) {
        this.eventSource.close();
      }

      // 创建新连接
      this.eventSource = new EventSource(sseUrl);

      // 连接建立
      this.eventSource.addEventListener('connect', (event) => {
        console.log('SSE连接已建立:', event.data);
      });

      // 状态更新事件
      this.eventSource.addEventListener('status', (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('收到状态更新:', data);

          this.taskStatus = data.status;
          this.statusMessage = data.error || '';

          // 如果任务完成或失败，关闭连接
          if (data.status === 'COMPLETED' || data.status === 'FAILED') {
            this.eventSource.close();
            this.eventSource = null;

            // 再次查询任务状态，获取完整信息
            this.queryTaskStatus();
          }
        } catch (error) {
          console.error('处理状态事件出错:', error);
        }
      });

      // 结果文件事件
      this.eventSource.addEventListener('result', (event) => {
        try {
          const data = JSON.parse(event.data);
          console.log('收到结果文件:', data);

          // 添加到结果文件列表
          this.addResultFile(data.name, data.url);
        } catch (error) {
          console.error('处理结果事件出错:', error);
        }
      });

      // 错误处理
      this.eventSource.onerror = (error) => {
        console.error('SSE连接错误:', error);

        // 尝试重新连接
        setTimeout(() => {
          if (this.eventSource) {
            this.eventSource.close();
            this.connectToEventStream(sseUrl);
          }
        }, 5000);
      };
    },

    // 更新结果文件
    updateResultFiles(taskData) {
      // 清空当前结果
      this.resultFiles = [];

      // 添加像素图像
      if (taskData.pixelImagesUrl) {
        this.addResultFile('像素图像', taskData.pixelImagesUrl);
      }

      // 添加XYZ图像
      if (taskData.xyzImagesUrl) {
        this.addResultFile('XYZ图像', taskData.xyzImagesUrl);
      }

      // 添加输出ZIP包
      if (taskData.outputZipUrl) {
        this.addResultFile('3D模型包', taskData.outputZipUrl, 'zip');
        this.modelUrl = taskData.outputZipUrl;
      }
    },

    // 添加结果文件
    addResultFile(name, url, type = 'image') {
      // 检查是否已存在
      const exists = this.resultFiles.some(file => file.url === url);
      if (!exists) {
        this.resultFiles.push({
          name,
          url,
          type
        });
      }
    },

    // 预览文件
    previewFile(file) {
      this.previewFile = file;
      this.previewDialogVisible = true;
    },

    // 下载文件
    downloadFile(file) {
      if (!file || !file.url) return;

      const link = document.createElement('a');
      link.href = file.url;
      link.download = file.name;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },

    // 下载3D模型
    downloadModel() {
      if (this.modelUrl) {
        this.downloadFile({
          name: '3D模型.zip',
          url: this.modelUrl
        });
      }
    }
  },
  beforeDestroy() {
    // 组件销毁前关闭SSE连接
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }

    // 释放预览URL
    if (this.previewUrl) {
      URL.revokeObjectURL(this.previewUrl);
    }
  }
};
</script>

<style scoped>
.reconstruction-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-title {
  margin-bottom: 30px;
  font-size: 28px;
  color: #303133;
}

.section {
  margin-bottom: 40px;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.section h2 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 20px;
  color: #303133;
}

/* 上传区域样式 */
.upload-area {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  border: 2px dashed #dcdfe6;
  border-radius: 6px;
  background-color: #f5f7fa;
  cursor: pointer;
  transition: border-color 0.3s;
}

.upload-area:hover {
  border-color: #409eff;
}

.upload-placeholder {
  text-align: center;
}

.upload-placeholder i {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 16px;
  color: #606266;
  margin-bottom: 6px;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
}

.preview-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 10px;
}

.image-preview {
  max-width: 100%;
  max-height: 120px;
  object-fit: contain;
  margin-bottom: 10px;
}

.file-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 10px;
  font-size: 14px;
  color: #606266;
}

.upload-actions {
  display: flex;
  align-items: center;
  margin-top: 20px;
}

.model-name-input {
  flex: 1;
  margin-right: 10px;
}

/* 任务信息样式 */
.task-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-info {
  display: flex;
  align-items: center;
}

.uploaded-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 15px;
}

.image-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 5px;
}

.image-id {
  font-size: 14px;
  color: #909399;
}

/* 进度样式 */
.task-progress {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.task-id {
  font-size: 14px;
  color: #606266;
}

.task-status {
  font-size: 14px;
  font-weight: 500;
}

.status-pending {
  color: #909399;
}

.status-processing {
  color: #e6a23c;
}

.status-completed {
  color: #67c23a;
}

.status-failed {
  color: #f56c6c;
}

.status-message {
  margin-top: 10px;
  font-size: 14px;
  color: #f56c6c;
}

/* 结果文件样式 */
.result-files {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 30px;
}

.result-file {
  width: 200px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  overflow: hidden;
  transition: box-shadow 0.3s;
}

.result-file:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.file-preview {
  height: 150px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.result-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.file-icon {
  font-size: 48px;
  color: #909399;
}

.file-name {
  padding: 10px;
  font-size: 14px;
  color: #303133;
  text-align: center;
  border-top: 1px solid #ebeef5;
}

.file-actions {
  display: flex;
  justify-content: space-around;
  padding: 5px 10px 10px;
}

/* 模型查看器样式 */
.model-viewer {
  margin-top: 30px;
}

.model-viewer h3 {
  margin-bottom: 15px;
  font-size: 18px;
  color: #303133;
}

.viewer-container {
  height: 400px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.placeholder-viewer {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.placeholder-viewer p {
  margin-bottom: 20px;
  font-size: 16px;
  color: #909399;
}

/* 预览对话框样式 */
.preview-dialog-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

.preview-dialog-image {
  max-width: 100%;
  max-height: 500px;
  object-fit: contain;
}

.preview-dialog-message {
  text-align: center;
}
</style>
