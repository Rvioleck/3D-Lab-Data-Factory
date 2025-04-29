<template>
  <div class="system-logs-view">
    <div class="container py-4">
      <div class="page-header">
        <h1 class="page-title">系统日志</h1>
        <p class="page-subtitle">查看系统操作和错误日志</p>
      </div>

      <div class="card mb-4">
        <div class="card-body">
          <div class="row">
            <div class="col-md-4 mb-3 mb-md-0">
              <div class="input-group">
                <input
                  type="text"
                  class="form-control"
                  placeholder="搜索日志内容..."
                  v-model="searchQuery"
                  @input="debounceSearch"
                >
                <button class="btn btn-primary" type="button" @click="search">
                  <i class="bi bi-search"></i>
                </button>
              </div>
            </div>
            <div class="col-md-3 mb-3 mb-md-0">
              <select class="form-select" v-model="selectedLevel" @change="search">
                <option value="">所有级别</option>
                <option value="INFO">信息</option>
                <option value="WARNING">警告</option>
                <option value="ERROR">错误</option>
                <option value="DEBUG">调试</option>
              </select>
            </div>
            <div class="col-md-3 mb-3 mb-md-0">
              <select class="form-select" v-model="selectedModule" @change="search">
                <option value="">所有模块</option>
                <option v-for="module in modules" :key="module" :value="module">{{ module }}</option>
              </select>
            </div>
            <div class="col-md-2">
              <button class="btn btn-outline-secondary w-100" @click="exportLogs">
                <i class="bi bi-download me-2"></i>导出
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-body">
          <div v-if="loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <p class="mt-3">正在加载日志数据...</p>
          </div>

          <div v-else-if="logs.length === 0" class="text-center py-5">
            <i class="bi bi-journal-text display-1 text-muted"></i>
            <p class="mt-3">没有找到符合条件的日志</p>
          </div>

          <div v-else>
            <div class="table-responsive">
              <table class="table table-hover">
                <thead>
                  <tr>
                    <th>时间</th>
                    <th>级别</th>
                    <th>模块</th>
                    <th>用户</th>
                    <th>操作</th>
                    <th>消息</th>
                    <th>详情</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(log, index) in logs" :key="index">
                    <td>{{ formatDateTime(log.timestamp) }}</td>
                    <td>
                      <span class="log-level" :class="getLevelClass(log.level)">
                        {{ log.level }}
                      </span>
                    </td>
                    <td>{{ log.module }}</td>
                    <td>{{ log.userName || log.userId || '-' }}</td>
                    <td>{{ log.action }}</td>
                    <td class="log-message">{{ log.message }}</td>
                    <td>
                      <button 
                        class="btn btn-sm btn-outline-secondary" 
                        @click="showLogDetails(log)"
                      >
                        <i class="bi bi-info-circle"></i>
                      </button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <!-- 分页 -->
            <div class="d-flex justify-content-between align-items-center mt-4">
              <div class="pagination-info">
                显示 {{ logs.length }} 条，共 {{ totalItems }} 条
              </div>
              <nav aria-label="Page navigation">
                <ul class="pagination">
                  <li class="page-item" :class="{ disabled: currentPage === 1 }">
                    <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">
                      <i class="bi bi-chevron-left"></i>
                    </a>
                  </li>
                  <li 
                    v-for="page in paginationItems" 
                    :key="page" 
                    class="page-item"
                    :class="{ active: page === currentPage }"
                  >
                    <a class="page-link" href="#" @click.prevent="changePage(page)">
                      {{ page }}
                    </a>
                  </li>
                  <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                    <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">
                      <i class="bi bi-chevron-right"></i>
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 日志详情模态框 -->
    <Modal v-if="selectedLog" @close="selectedLog = null">
      <template #header>
        <h5 class="modal-title">日志详情</h5>
      </template>
      <template #body>
        <div class="log-details">
          <div class="log-detail-item">
            <div class="detail-label">时间:</div>
            <div class="detail-value">{{ formatDateTime(selectedLog.timestamp) }}</div>
          </div>
          <div class="log-detail-item">
            <div class="detail-label">级别:</div>
            <div class="detail-value">
              <span class="log-level" :class="getLevelClass(selectedLog.level)">
                {{ selectedLog.level }}
              </span>
            </div>
          </div>
          <div class="log-detail-item">
            <div class="detail-label">模块:</div>
            <div class="detail-value">{{ selectedLog.module }}</div>
          </div>
          <div class="log-detail-item">
            <div class="detail-label">用户:</div>
            <div class="detail-value">{{ selectedLog.userName || selectedLog.userId || '-' }}</div>
          </div>
          <div class="log-detail-item">
            <div class="detail-label">操作:</div>
            <div class="detail-value">{{ selectedLog.action }}</div>
          </div>
          <div class="log-detail-item">
            <div class="detail-label">消息:</div>
            <div class="detail-value">{{ selectedLog.message }}</div>
          </div>
          <div class="log-detail-item" v-if="selectedLog.details">
            <div class="detail-label">详细信息:</div>
            <div class="detail-value">
              <pre class="log-details-pre">{{ formatLogDetails(selectedLog.details) }}</pre>
            </div>
          </div>
          <div class="log-detail-item" v-if="selectedLog.stackTrace">
            <div class="detail-label">堆栈跟踪:</div>
            <div class="detail-value">
              <pre class="log-details-pre">{{ selectedLog.stackTrace }}</pre>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <button class="btn btn-secondary" @click="selectedLog = null">关闭</button>
      </template>
    </Modal>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import debounce from 'lodash/debounce'
import { formatDate } from '@/utils/dateTime'
import Modal from '@/components/common/Modal.vue'

export default {
  name: 'SystemLogsView',
  components: {
    Modal
  },
  setup() {
    // 状态变量
    const loading = ref(false)
    const logs = ref([])
    const searchQuery = ref('')
    const selectedLevel = ref('')
    const selectedModule = ref('')
    const currentPage = ref(1)
    const pageSize = ref(20)
    const totalItems = ref(0)
    const totalPages = ref(0)
    const selectedLog = ref(null)
    
    // 模拟数据
    const modules = ref([
      '用户管理', '模型处理', '图片处理', '认证', '系统', '文件存储', 'API'
    ])
    
    // 计算属性
    const paginationItems = computed(() => {
      const items = []
      const maxVisiblePages = 5
      
      if (totalPages.value <= maxVisiblePages) {
        // 如果总页数小于等于最大可见页数，显示所有页码
        for (let i = 1; i <= totalPages.value; i++) {
          items.push(i)
        }
      } else {
        // 否则，显示当前页附近的页码
        let startPage = Math.max(currentPage.value - Math.floor(maxVisiblePages / 2), 1)
        let endPage = startPage + maxVisiblePages - 1
        
        if (endPage > totalPages.value) {
          endPage = totalPages.value
          startPage = Math.max(endPage - maxVisiblePages + 1, 1)
        }
        
        for (let i = startPage; i <= endPage; i++) {
          items.push(i)
        }
      }
      
      return items
    })
    
    // 方法
    const loadLogs = async () => {
      loading.value = true
      try {
        // 这里应该调用实际的API获取日志数据
        // 模拟API调用
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // 模拟数据
        const mockLogs = generateMockLogs()
        
        // 筛选日志
        let filteredLogs = [...mockLogs]
        
        if (searchQuery.value) {
          const query = searchQuery.value.toLowerCase()
          filteredLogs = filteredLogs.filter(log => 
            log.message.toLowerCase().includes(query) || 
            (log.details && log.details.toLowerCase().includes(query))
          )
        }
        
        if (selectedLevel.value) {
          filteredLogs = filteredLogs.filter(log => log.level === selectedLevel.value)
        }
        
        if (selectedModule.value) {
          filteredLogs = filteredLogs.filter(log => log.module === selectedModule.value)
        }
        
        // 分页
        totalItems.value = filteredLogs.length
        totalPages.value = Math.ceil(totalItems.value / pageSize.value)
        
        const startIndex = (currentPage.value - 1) * pageSize.value
        const endIndex = startIndex + pageSize.value
        logs.value = filteredLogs.slice(startIndex, endIndex)
      } catch (error) {
        console.error('获取日志数据失败:', error)
        if (window.$toast) {
          window.$toast.error('获取日志数据失败')
        }
        logs.value = []
        totalItems.value = 0
        totalPages.value = 0
      } finally {
        loading.value = false
      }
    }
    
    // 生成模拟日志数据
    const generateMockLogs = () => {
      const mockLogs = []
      const levels = ['INFO', 'WARNING', 'ERROR', 'DEBUG']
      const actions = [
        '登录', '注销', '创建用户', '更新用户', '删除用户', 
        '上传模型', '处理模型', '删除模型', 
        '上传图片', '处理图片', '删除图片',
        '系统启动', '系统配置更新', '数据库备份'
      ]
      const users = ['admin', 'user1', 'user2', 'system']
      
      // 生成100条模拟日志
      for (let i = 0; i < 100; i++) {
        const level = levels[Math.floor(Math.random() * levels.length)]
        const module = modules.value[Math.floor(Math.random() * modules.value.length)]
        const action = actions[Math.floor(Math.random() * actions.length)]
        const user = users[Math.floor(Math.random() * users.length)]
        
        // 创建日志时间，最近30天内的随机时间
        const date = new Date()
        date.setDate(date.getDate() - Math.floor(Math.random() * 30))
        date.setHours(Math.floor(Math.random() * 24))
        date.setMinutes(Math.floor(Math.random() * 60))
        date.setSeconds(Math.floor(Math.random() * 60))
        
        const log = {
          id: i + 1,
          timestamp: date.toISOString(),
          level,
          module,
          action,
          userId: user === 'system' ? null : user,
          userName: user === 'system' ? 'System' : user === 'admin' ? 'Administrator' : `User ${user.slice(4)}`,
          message: `${action}操作${level === 'ERROR' ? '失败' : '成功'}`
        }
        
        // 为错误日志添加详细信息和堆栈跟踪
        if (level === 'ERROR') {
          log.details = JSON.stringify({
            errorCode: Math.floor(Math.random() * 1000),
            errorType: ['ValidationError', 'DatabaseError', 'AuthenticationError', 'FileProcessingError'][Math.floor(Math.random() * 4)],
            requestId: `req-${Math.random().toString(36).substring(2, 10)}`,
            timestamp: date.toISOString()
          }, null, 2)
          
          log.stackTrace = `java.lang.Exception: 操作失败\n    at com.elwg.ai3dbackend.service.${module}Service.${action.replace(/\s+/g, '')}(${module}Service.java:123)\n    at com.elwg.ai3dbackend.controller.${module}Controller.${action.replace(/\s+/g, '')}(${module}Controller.java:45)\n    at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1128)\n    at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:628)\n    at java.base/java.lang.Thread.run(Thread.java:829)`
        }
        
        mockLogs.push(log)
      }
      
      // 按时间降序排序
      return mockLogs.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    }
    
    // 搜索
    const search = () => {
      currentPage.value = 1
      loadLogs()
    }
    
    // 防抖搜索
    const debounceSearch = debounce(() => {
      search()
    }, 500)
    
    // 切换页码
    const changePage = (page) => {
      if (page < 1 || page > totalPages.value) return
      currentPage.value = page
      loadLogs()
    }
    
    // 显示日志详情
    const showLogDetails = (log) => {
      selectedLog.value = log
    }
    
    // 导出日志
    const exportLogs = () => {
      if (window.$toast) {
        window.$toast.info('正在准备导出日志...')
      }
      
      // 这里应该调用实际的API导出日志
      // 模拟导出操作
      setTimeout(() => {
        if (window.$toast) {
          window.$toast.success('日志导出成功')
        }
      }, 1500)
    }
    
    // 格式化日期时间
    const formatDateTime = (dateString) => {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return `${formatDate(date)} ${date.toLocaleTimeString()}`
    }
    
    // 获取日志级别类名
    const getLevelClass = (level) => {
      switch (level) {
        case 'INFO': return 'info'
        case 'WARNING': return 'warning'
        case 'ERROR': return 'error'
        case 'DEBUG': return 'debug'
        default: return ''
      }
    }
    
    // 格式化日志详情
    const formatLogDetails = (details) => {
      if (!details) return ''
      
      try {
        // 如果是JSON字符串，尝试格式化
        const parsed = JSON.parse(details)
        return JSON.stringify(parsed, null, 2)
      } catch (e) {
        // 如果不是JSON，直接返回
        return details
      }
    }
    
    // 生命周期钩子
    onMounted(() => {
      loadLogs()
    })
    
    return {
      loading,
      logs,
      searchQuery,
      selectedLevel,
      selectedModule,
      currentPage,
      pageSize,
      totalItems,
      totalPages,
      paginationItems,
      selectedLog,
      modules,
      
      search,
      debounceSearch,
      changePage,
      showLogDetails,
      exportLogs,
      formatDateTime,
      getLevelClass,
      formatLogDetails
    }
  }
}
</script>

<style scoped>
.system-logs-view {
  min-height: calc(100vh - 70px);
  background-color: var(--bg-secondary);
}

.page-header {
  margin-bottom: 2rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  margin-bottom: 0.5rem;
  color: var(--text-primary);
}

.page-subtitle {
  color: var(--text-secondary);
}

.log-level {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-full);
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
}

.log-level.info {
  background-color: var(--info-light);
  color: var(--info-color);
}

.log-level.warning {
  background-color: var(--warning-light);
  color: var(--warning-color);
}

.log-level.error {
  background-color: var(--error-light);
  color: var(--error-color);
}

.log-level.debug {
  background-color: var(--neutral-200);
  color: var(--neutral-700);
}

.log-message {
  max-width: 300px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.pagination-info {
  color: var(--text-secondary);
  font-size: 0.875rem;
}

/* 日志详情样式 */
.log-details {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.log-detail-item {
  display: flex;
  flex-direction: column;
}

.detail-label {
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.detail-value {
  color: var(--text-primary);
}

.log-details-pre {
  background-color: var(--bg-tertiary);
  padding: 1rem;
  border-radius: var(--radius-md);
  font-size: 0.875rem;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 300px;
  overflow-y: auto;
}
</style>
