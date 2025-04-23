<template>
  <div class="reconstruction-status">
    <div class="status-card" :class="statusClass">
      <div class="status-body">
        <div v-if="status === 'PENDING'" class="text-center py-3">
          <div class="spinner-border text-warning" role="status">
            <span class="visually-hidden">等待中...</span>
          </div>
          <p class="mt-2">任务已创建，等待处理...</p>
        </div>
        
        <div v-else-if="status === 'PROCESSING'" class="text-center py-3">
          <div class="progress-container">
            <div class="progress" style="height: 10px;">
              <div 
                class="progress-bar progress-bar-striped progress-bar-animated" 
                role="progressbar" 
                :style="{ width: progressPercentage + '%' }"
                :aria-valuenow="progressPercentage" 
                aria-valuemin="0" 
                aria-valuemax="100">
              </div>
            </div>
            <div class="mt-2 text-muted">{{ progressText }}</div>
          </div>
        </div>
        
        <div v-else-if="status === 'COMPLETED'" class="py-3">
          <div class="d-flex align-items-center mb-3">
            <i class="bi bi-check-circle-fill text-success me-2 fs-4"></i>
            <span>重建任务已完成！</span>
          </div>
          <div v-if="processingTime" class="text-muted small">
            处理时间: {{ formatProcessingTime(processingTime) }}
          </div>
        </div>
        
        <div v-else-if="status === 'FAILED'" class="py-3">
          <div class="d-flex align-items-center mb-3">
            <i class="bi bi-x-circle-fill text-danger me-2 fs-4"></i>
            <span>重建任务失败</span>
          </div>
          <div v-if="errorMessage" class="alert alert-danger mt-2">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            {{ errorMessage }}
          </div>
        </div>
      </div>
    </div>

    <!-- 任务日志 -->
    <div v-if="showLogs && logs.length > 0" class="logs-container mt-3">
      <div class="logs-header d-flex justify-content-between align-items-center">
        <h6 class="mb-0">
          <i class="bi bi-journal-text me-2"></i>
          任务日志
        </h6>
        <button class="btn btn-sm btn-outline-secondary" @click="clearLogs">
          <i class="bi bi-trash me-1"></i>
          清空
        </button>
      </div>
      <div class="logs-content" ref="logsContent">
        <div v-for="(log, index) in logs" :key="index" class="log-entry">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-badge" :class="getLogBadgeClass(log.type)">{{ getLogTypeText(log.type) }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReconstructionStatus',
  props: {
    status: {
      type: String,
      required: true,
      validator: value => ['PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'].includes(value)
    },
    errorMessage: {
      type: String,
      default: ''
    },
    processingTime: {
      type: Number,
      default: null
    },
    progress: {
      type: Number,
      default: 0
    },
    showLogs: {
      type: Boolean,
      default: true
    },
    logs: {
      type: Array,
      default: () => []
    }
  },
  computed: {
    statusClass() {
      return {
        'status-pending': this.status === 'PENDING',
        'status-processing': this.status === 'PROCESSING',
        'status-completed': this.status === 'COMPLETED',
        'status-failed': this.status === 'FAILED'
      }
    },
    statusTitle() {
      switch(this.status) {
        case 'PENDING': return '等待处理中';
        case 'PROCESSING': return '正在处理';
        case 'COMPLETED': return '处理完成';
        case 'FAILED': return '处理失败';
        default: return '未知状态';
      }
    },
    statusIcon() {
      switch(this.status) {
        case 'PENDING': return 'bi bi-hourglass-split';
        case 'PROCESSING': return 'bi bi-arrow-repeat';
        case 'COMPLETED': return 'bi bi-check-circle';
        case 'FAILED': return 'bi bi-exclamation-triangle';
        default: return 'bi bi-question-circle';
      }
    },
    statusText() {
      switch(this.status) {
        case 'PENDING': return '等待中';
        case 'PROCESSING': return '处理中';
        case 'COMPLETED': return '已完成';
        case 'FAILED': return '失败';
        default: return '未知';
      }
    },
    statusBadgeClass() {
      switch(this.status) {
        case 'PENDING': return 'bg-warning text-dark';
        case 'PROCESSING': return 'bg-primary';
        case 'COMPLETED': return 'bg-success';
        case 'FAILED': return 'bg-danger';
        default: return 'bg-secondary';
      }
    },
    progressPercentage() {
      // 如果有明确的进度，使用它
      if (this.progress > 0) {
        return Math.min(this.progress, 100);
      }
      
      // 否则根据状态给出估计进度
      switch(this.status) {
        case 'PENDING': return 0;
        case 'PROCESSING': return 50; // 不确定进度时显示50%
        case 'COMPLETED': return 100;
        case 'FAILED': return 100;
        default: return 0;
      }
    },
    progressText() {
      if (this.progress > 0) {
        return `处理进度: ${Math.round(this.progress)}%`;
      }
      return '处理中...';
    }
  },
  methods: {
    formatProcessingTime(seconds) {
      if (seconds < 60) {
        return `${seconds}秒`;
      } else if (seconds < 3600) {
        const minutes = Math.floor(seconds / 60);
        const remainingSeconds = seconds % 60;
        return `${minutes}分${remainingSeconds}秒`;
      } else {
        const hours = Math.floor(seconds / 3600);
        const minutes = Math.floor((seconds % 3600) / 60);
        return `${hours}小时${minutes}分钟`;
      }
    },
    clearLogs() {
      this.$emit('clear-logs');
    },
    getLogBadgeClass(type) {
      switch(type) {
        case 'info': return 'badge-info';
        case 'success': return 'badge-success';
        case 'warning': return 'badge-warning';
        case 'error': return 'badge-error';
        default: return 'badge-info';
      }
    },
    getLogTypeText(type) {
      switch(type) {
        case 'info': return '信息';
        case 'success': return '成功';
        case 'warning': return '警告';
        case 'error': return '错误';
        default: return '信息';
      }
    }
  },
  watch: {
    logs: {
      handler() {
        this.$nextTick(() => {
          if (this.$refs.logsContent) {
            this.$refs.logsContent.scrollTop = this.$refs.logsContent.scrollHeight;
          }
        });
      },
      deep: true
    }
  }
}
</script>

<style scoped>
.reconstruction-status {
  width: 100%;
}

.status-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.status-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 15px;
}

.status-body {
  padding: 15px;
  background-color: #fff;
}

.progress-container {
  max-width: 400px;
  margin: 0 auto;
}

/* 日志样式 */
.logs-container {
  border: 1px solid #dee2e6;
  border-radius: 8px;
  overflow: hidden;
}

.logs-header {
  padding: 10px 15px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}

.logs-content {
  max-height: 200px;
  overflow-y: auto;
  padding: 10px 15px;
  background-color: #f8f9fa;
}

.log-entry {
  padding: 5px 0;
  border-bottom: 1px solid #eee;
  font-family: monospace;
  font-size: 0.9rem;
}

.log-time {
  color: #6c757d;
  margin-right: 10px;
}

.log-badge {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.75rem;
  margin-right: 10px;
  min-width: 50px;
  text-align: center;
}

.badge-info {
  background-color: #17a2b8;
  color: white;
}

.badge-success {
  background-color: #28a745;
  color: white;
}

.badge-warning {
  background-color: #ffc107;
  color: #212529;
}

.badge-error {
  background-color: #dc3545;
  color: white;
}

.log-message {
  word-break: break-word;
}

/* 状态特定样式 */
.status-pending .status-header {
  background-color: #ffc107;
  color: #212529;
}

.status-processing .status-header {
  background-color: #007bff;
  color: white;
}

.status-completed .status-header {
  background-color: #28a745;
  color: white;
}

.status-failed .status-header {
  background-color: #dc3545;
  color: white;
}
</style>
