<template>
  <div class="resource-statistics">
    <div class="row g-3">
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-primary text-white">
          <div class="stat-icon">
            <i class="bi bi-images"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalImages }}</div>
            <div class="stat-label">图片资源</div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-success text-white">
          <div class="stat-icon">
            <i class="bi bi-box"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalModels }}</div>
            <div class="stat-label">3D模型</div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-info text-white">
          <div class="stat-icon">
            <i class="bi bi-tags"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.totalTags }}</div>
            <div class="stat-label">标签数量</div>
          </div>
        </div>
      </div>
      <div class="col-md-3 col-sm-6">
        <div class="stat-card bg-warning text-white">
          <div class="stat-icon">
            <i class="bi bi-clock-history"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stats.recentUploads }}</div>
            <div class="stat-label">近期上传</div>
          </div>
        </div>
      </div>
    </div>

    <div class="row mt-4" v-if="isAdmin">
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h5 class="mb-0">资源使用趋势</h5>
          </div>
          <div class="card-body">
            <div class="chart-container">
              <!-- 这里可以添加图表组件，如ECharts或Chart.js -->
              <div class="placeholder-chart">
                <div class="chart-bars">
                  <div class="chart-bar" v-for="(value, index) in trendData" :key="index" :style="{ height: `${value * 2}px` }"></div>
                </div>
                <div class="chart-labels">
                  <div class="chart-label" v-for="(month, index) in trendMonths" :key="index">{{ month }}</div>
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
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'

// 导入资源API
// import { getResourceStats } from '@/api/resource'

export default {
  name: 'ResourceStatistics',
  setup() {
    const store = useStore()
    const isAdmin = computed(() => store.getters['user/isAdmin'])

    // 统计数据
    const stats = ref({
      totalImages: 0,
      totalModels: 0,
      totalTags: 0,
      recentUploads: 0
    })

    // 趋势数据（模拟）
    const trendData = ref([25, 40, 30, 50, 45, 60])
    const trendMonths = ref(['1月', '2月', '3月', '4月', '5月', '6月'])

    // 初始化
    onMounted(async () => {
      await loadStats()
    })

    // 加载统计数据
    const loadStats = async () => {
      try {
        // 直接使用模拟数据
        stats.value = {
          totalImages: 0,
          totalModels: 0,
          totalTags: 0,
          recentUploads: 0
        }
      } catch (error) {
        console.error('加载资源统计信息失败:', error)
      }
    }

    return {
      stats,
      isAdmin,
      trendData,
      trendMonths
    }
  }
}
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  padding: 1.25rem;
  border-radius: 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  font-size: 2.5rem;
  margin-right: 1rem;
  opacity: 0.8;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.75rem;
  font-weight: 700;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  opacity: 0.9;
}

.chart-container {
  height: 200px;
  position: relative;
}

.placeholder-chart {
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 85%;
}

.chart-bar {
  width: 40px;
  background-color: #6c757d;
  border-radius: 4px 4px 0 0;
  transition: height 0.5s;
}

.chart-bar:nth-child(odd) {
  background-color: #0d6efd;
}

.chart-labels {
  display: flex;
  justify-content: space-around;
  margin-top: 10px;
}

.chart-label {
  font-size: 0.75rem;
  color: #6c757d;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .stat-icon {
    font-size: 2rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .chart-bar {
    width: 30px;
  }
}
</style>
