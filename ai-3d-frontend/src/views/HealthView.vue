<template>
  <div class="health-view">
    <div class="row">
      <div class="col-lg-8 offset-lg-2">
        <h2 class="mb-4 text-center">系统健康状态</h2>

        <div class="row g-4">
          <!-- 简单健康检查 -->
          <div class="col-md-6">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-header bg-white border-0 pt-4">
                <div class="d-flex align-items-center">
                  <i class="bi bi-heart-pulse text-primary me-2" style="font-size: 1.5rem;"></i>
                  <h5 class="mb-0">基本状态</h5>
                </div>
              </div>
              <div class="card-body p-4">
                <div v-if="isLoadingSimple" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">加载中...</span>
                  </div>
                  <p class="text-muted mt-2">正在检查系统状态...</p>
                </div>

                <div v-else-if="simpleHealth" class="health-status">
                  <div class="status-indicator success">
                    <i class="bi bi-check-circle-fill"></i>
                  </div>
                  <div class="alert alert-success border-0 bg-light">
                    <strong>状态:</strong> {{ simpleHealth }}
                  </div>
                  <div class="text-center mt-4">
                    <button class="btn btn-outline-primary rounded-pill" @click="checkSimpleHealth">
                      <i class="bi bi-arrow-clockwise me-1"></i> 刷新
                    </button>
                  </div>
                </div>

                <div v-else class="health-status">
                  <div class="status-indicator danger">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                  </div>
                  <div class="alert alert-danger border-0 bg-light">
                    <strong>错误:</strong> {{ simpleError || '无法获取健康状态' }}
                  </div>
                  <div class="text-center mt-4">
                    <button class="btn btn-primary rounded-pill" @click="checkSimpleHealth">
                      <i class="bi bi-arrow-repeat me-1"></i> 重试
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 详细健康检查 -->
          <div class="col-md-6">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-header bg-white border-0 pt-4">
                <div class="d-flex align-items-center">
                  <i class="bi bi-clipboard2-data text-primary me-2" style="font-size: 1.5rem;"></i>
                  <h5 class="mb-0">详细信息</h5>
                </div>
              </div>
              <div class="card-body p-4">
                <div v-if="isLoadingDetail" class="text-center py-4">
                  <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">加载中...</span>
                  </div>
                  <p class="text-muted mt-2">正在获取详细信息...</p>
                </div>

                <div v-else-if="detailHealth">
                  <div class="table-responsive">
                    <table class="table">
                      <tbody>
                        <tr v-for="(value, key) in detailHealth" :key="key" class="detail-item">
                          <th class="text-nowrap">{{ formatKey(key) }}</th>
                          <td class="text-break">{{ formatValue(value) }}</td>
                        </tr>
                      </tbody>
                    </table>
                  </div>
                  <div class="text-center mt-4">
                    <button class="btn btn-outline-primary rounded-pill" @click="checkDetailHealth">
                      <i class="bi bi-arrow-clockwise me-1"></i> 刷新
                    </button>
                  </div>
                </div>

                <div v-else class="health-status">
                  <div class="status-indicator danger">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                  </div>
                  <div class="alert alert-danger border-0 bg-light">
                    <strong>错误:</strong> {{ detailError || '无法获取详细健康状态' }}
                  </div>
                  <div class="text-center mt-4">
                    <button class="btn btn-primary rounded-pill" @click="checkDetailHealth">
                      <i class="bi bi-arrow-repeat me-1"></i> 重试
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { checkHealth, checkHealthDetail } from '../api/health'

export default {
  name: 'HealthView',
  setup() {
    const isLoadingSimple = ref(false)
    const isLoadingDetail = ref(false)
    const simpleHealth = ref(null)
    const detailHealth = ref(null)
    const simpleError = ref(null)
    const detailError = ref(null)

    const checkSimpleHealth = async () => {
      isLoadingSimple.value = true
      simpleError.value = null

      try {
        const response = await checkHealth()
        if (response.code === 0) {
          simpleHealth.value = response.data
        } else {
          simpleError.value = response.message || '请求失败'
          simpleHealth.value = null
        }
      } catch (error) {
        console.error('健康检查失败:', error)
        simpleError.value = error.message || '请求失败'
        simpleHealth.value = null
      } finally {
        isLoadingSimple.value = false
      }
    }

    const checkDetailHealth = async () => {
      isLoadingDetail.value = true
      detailError.value = null

      try {
        const response = await checkHealthDetail()
        if (response.code === 0) {
          detailHealth.value = response.data
        } else {
          detailError.value = response.message || '请求失败'
          detailHealth.value = null
        }
      } catch (error) {
        console.error('详细健康检查失败:', error)
        detailError.value = error.message || '请求失败'
        detailHealth.value = null
      } finally {
        isLoadingDetail.value = false
      }
    }

    const formatKey = (key) => {
      // 将驼峰命名转换为空格分隔的标题格式
      return key
        .replace(/([A-Z])/g, ' $1')
        .replace(/^./, str => str.toUpperCase())
    }

    const formatValue = (value) => {
      // 格式化值，如果是布尔值或特殊值
      if (value === true) return '正常'
      if (value === false) return '异常'
      if (value === null) return '无数据'
      if (value === undefined) return '未定义'

      // 如果是对象，转为JSON字符串
      if (typeof value === 'object') {
        return JSON.stringify(value, null, 2)
      }

      return value
    }

    onMounted(() => {
      checkSimpleHealth()
      checkDetailHealth()
    })

    return {
      isLoadingSimple,
      isLoadingDetail,
      simpleHealth,
      detailHealth,
      simpleError,
      detailError,
      checkSimpleHealth,
      checkDetailHealth,
      formatKey,
      formatValue
    }
  }
}
</script>

<style scoped>
.health-view {
  padding: 2rem 0;
}

.card {
  border-radius: var(--radius-lg);
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
}

.health-status {
  text-align: center;
  padding: 1rem 0;
}

.status-indicator {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
  font-size: 2.5rem;
  color: white;
}

.status-indicator.success {
  background-color: var(--success-color);
}

.status-indicator.danger {
  background-color: var(--error-color);
}

.detail-item th {
  color: var(--text-secondary);
  font-weight: 500;
}

.table {
  margin-bottom: 0;
}

.table td, .table th {
  padding: 0.75rem 1rem;
  border-color: var(--border-color);
}

.alert {
  border-radius: var(--radius-md);
}
</style>
