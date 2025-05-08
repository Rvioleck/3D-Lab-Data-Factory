<template>
  <div class="result-files-viewer">
    <!-- 结果文件列表 -->
    <div v-if="!hasAnyFile" class="text-center py-4 text-muted">
      <i class="bi bi-hourglass-split fs-1"></i>
      <p class="mt-2">等待生成结果文件...</p>
    </div>

    <div v-else class="result-files-grid">
      <!-- 像素图像 -->
      <div class="result-file-item" :class="{ 'file-available': pixelImagesUrl }">
        <div class="file-card">
          <div class="file-preview" @click="previewFile('pixel_images')">
            <img v-if="pixelImagesUrl" :src="pixelImagesUrl" alt="像素图像" class="img-preview">
            <div v-else class="placeholder-preview">
              <i class="bi bi-image fs-1"></i>
            </div>
          </div>
          <div class="file-info">
            <div class="file-name">像素图像</div>
            <div class="file-actions">
              <button
                class="btn btn-sm btn-outline-primary"
                :disabled="!pixelImagesUrl"
                @click="previewFile('pixel_images')"
              >
                <i class="bi bi-eye me-1"></i> 预览
              </button>
              <button
                class="btn btn-sm btn-outline-secondary"
                :disabled="!pixelImagesUrl"
                @click="downloadFile('pixel_images')"
              >
                <i class="bi bi-download me-1"></i> 下载
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- XYZ图像 -->
      <div class="result-file-item" :class="{ 'file-available': xyzImagesUrl }">
        <div class="file-card">
          <div class="file-preview" @click="previewFile('xyz_images')">
            <img v-if="xyzImagesUrl" :src="xyzImagesUrl" alt="深度图像" class="img-preview">
            <div v-else class="placeholder-preview">
              <i class="bi bi-layers fs-1"></i>
            </div>
          </div>
          <div class="file-info">
            <div class="file-name">深度图像</div>
            <div class="file-actions">
              <button
                class="btn btn-sm btn-outline-primary"
                :disabled="!xyzImagesUrl"
                @click="previewFile('xyz_images')"
              >
                <i class="bi bi-eye me-1"></i> 预览
              </button>
              <button
                class="btn btn-sm btn-outline-secondary"
                :disabled="!xyzImagesUrl"
                @click="downloadFile('xyz_images')"
              >
                <i class="bi bi-download me-1"></i> 下载
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 3D模型文件 -->
      <div class="result-file-item" :class="{ 'file-available': has3DModelFiles }">
        <div class="file-card">
          <div class="file-preview">
            <div class="model-preview">
              <i class="bi bi-box fs-1"></i>
              <div class="model-format">OBJ</div>
            </div>
          </div>
          <div class="file-info">
            <div class="file-name">3D模型文件包</div>
            <div class="file-actions">
              <button
                class="btn btn-sm btn-outline-primary"
                :disabled="!has3DModelFiles"
                @click="view3DModel"
              >
                <i class="bi bi-box me-1"></i> 查看
              </button>
              <button
                class="btn btn-sm btn-outline-secondary"
                :disabled="!has3DModelFiles"
                @click="downloadAllFiles"
              >
                <i class="bi bi-download me-1"></i> 下载
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 图像预览对话框 -->
    <div v-if="previewDialogVisible" class="preview-dialog">
      <div class="preview-dialog-content">
        <div class="preview-header">
          <h5>{{ previewTitle }}</h5>
          <button class="btn-close" @click="closePreview"></button>
        </div>
        <div class="preview-body">
          <img :src="previewUrl" :alt="previewTitle" class="preview-image">
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import JSZip from 'jszip'
import { saveAs } from 'file-saver'

export default {
  name: 'ResultFilesViewer',
  props: {
    pixelImagesUrl: String,
    xyzImagesUrl: String,
    objFileUrl: String,
    mtlFileUrl: String,
    textureImageUrl: String,
    taskId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      previewDialogVisible: false,
      previewType: '',
      previewUrl: '',
      previewTitle: ''
    }
  },
  computed: {
    hasAnyFile() {
      return this.pixelImagesUrl || this.xyzImagesUrl || this.has3DModelFiles;
    },
    has3DModelFiles() {
      return this.objFileUrl && (this.mtlFileUrl || this.textureImageUrl);
    }
  },
  methods: {
    previewFile(type) {
      switch(type) {
        case 'pixel_images':
          if (!this.pixelImagesUrl) return;
          this.previewUrl = this.pixelImagesUrl;
          this.previewTitle = '像素图像预览';
          break;
        case 'xyz_images':
          if (!this.xyzImagesUrl) return;
          this.previewUrl = this.xyzImagesUrl;
          this.previewTitle = '深度图像预览';
          break;
        default:
          return;
      }

      this.previewType = type;
      this.previewDialogVisible = true;
      
      // 添加ESC键监听
      document.addEventListener('keydown', this.handleEscKey);
      
      // 阻止背景滚动
      document.body.style.overflow = 'hidden';
    },

    closePreview() {
      this.previewDialogVisible = false;
      this.previewUrl = '';
      this.previewType = '';
      
      // 移除ESC键监听
      document.removeEventListener('keydown', this.handleEscKey);
      
      // 恢复背景滚动
      document.body.style.overflow = '';
    },
    
    handleEscKey(event) {
      if (event.key === 'Escape') {
        this.closePreview();
      }
    },

    downloadFile(type) {
      let url = '';
      let filename = '';

      switch(type) {
        case 'pixel_images':
          url = this.pixelImagesUrl;
          filename = `pixel_images_${this.taskId}.png`;
          break;
        case 'xyz_images':
          url = this.xyzImagesUrl;
          filename = `xyz_images_${this.taskId}.png`;
          break;
        case 'output_zip':
          url = this.outputZipUrl;
          filename = `output3d_${this.taskId}.zip`;
          break;
        default:
          return;
      }

      if (!url) return;

      // 创建下载链接
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      link.target = '_blank';
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },

    view3DModel() {
      // 发出事件，通知父组件显示3D模型查看器
      this.$emit('view-model', {
        objUrl: this.objFileUrl,
        mtlUrl: this.mtlFileUrl,
        textureUrl: this.textureImageUrl
      });
      
      // 如果父组件没有处理这个事件，我们可以尝试滚动到3D模型查看器
      const modelViewer = document.querySelector('.model-viewer');
      if (modelViewer) {
        modelViewer.scrollIntoView({ behavior: 'smooth' });
      }
    },
    async downloadAllFiles() {
      try {
        const zip = new JSZip()

        // 下载所有文件并添加到zip
        const files = [
          { url: this.objFileUrl, name: 'model.obj' },
          { url: this.mtlFileUrl, name: 'model.mtl' },
          { url: this.textureImageUrl, name: 'texture.png' },
          { url: this.pixelImagesUrl, name: 'pixel_images.png' },
          { url: this.xyzImagesUrl, name: 'xyz_images.png' }
        ]

        // 并行下载所有文件
        const downloads = files
          .filter(file => file.url) // 只下载有URL的文件
          .map(async file => {
            const response = await fetch(file.url)
            const blob = await response.blob()
            zip.file(file.name, blob)
          })

        await Promise.all(downloads)

        // 生成并下载zip文件
        const content = await zip.generateAsync({ type: 'blob' })
        saveAs(content, `output3d_${this.taskId}.zip`)
      } catch (error) {
        console.error('下载文件失败:', error)
        // 这里可以添加错误提示UI
      }
    }
  },
  beforeUnmount() {
    // 确保在组件卸载时移除事件监听器
    if (this.previewDialogVisible) {
      document.removeEventListener('keydown', this.handleEscKey);
      document.body.style.overflow = '';
    }
  }
}
</script>

<style scoped>
.result-files-viewer {
  width: 100%;
  position: relative;
}

.result-files-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.result-file-item {
  transition: all 0.3s ease;
  opacity: 0.7;
}

.file-available {
  opacity: 1;
}

.file-card {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  overflow: hidden;
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;
}

.file-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.file-preview {
  height: 180px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  cursor: pointer;
}

.img-preview {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.placeholder-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #adb5bd;
  height: 100%;
  width: 100%;
}

.model-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #6c757d;
  height: 100%;
  width: 100%;
  position: relative;
}

.model-format {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background-color: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
}

.file-info {
  padding: 15px;
  background-color: white;
  flex-grow: 1;
  display: flex;
  flex-direction: column;
}

.file-name {
  font-weight: 500;
  margin-bottom: 10px;
}

.file-actions {
  display: flex;
  gap: 10px;
  margin-top: auto;
}

/* 预览对话框 */
.preview-dialog {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
}

.preview-dialog-content {
  background-color: white;
  border-radius: 8px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.preview-header {
  padding: 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #dee2e6;
}

.preview-header h5 {
  margin: 0;
}

.preview-body {
  padding: 20px;
  overflow: auto;
  text-align: center;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
}

/* 响应式样式 */
@media (max-width: 767.98px) {
  .result-files-grid {
    grid-template-columns: 1fr;
  }
  
  .file-preview {
    height: 150px;
  }
  
  .preview-dialog-content {
    width: 95%;
  }
}
</style>
