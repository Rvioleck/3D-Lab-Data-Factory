<template>
  <div class="user-profile-stats">
    <div class="card">
      <div class="card-header">
        <h5 class="mb-0">我的统计</h5>
      </div>
      <div class="card-body">
        <div class="row g-3">
          <div class="col-md-6">
            <div class="stat-card">
              <div class="stat-icon">
                <i class="bi bi-image text-primary"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.imageCount }}</div>
                <div class="stat-label">上传图片</div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="stat-card">
              <div class="stat-icon">
                <i class="bi bi-box text-success"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.modelCount }}</div>
                <div class="stat-label">创建模型</div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="stat-card">
              <div class="stat-icon">
                <i class="bi bi-chat-dots text-info"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ stats.chatCount }}</div>
                <div class="stat-label">聊天会话</div>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="stat-card">
              <div class="stat-icon">
                <i class="bi bi-calendar-check text-warning"></i>
              </div>
              <div class="stat-content">
                <div class="stat-value">{{ memberDays }}</div>
                <div class="stat-label">会员天数</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'UserProfileStats',
  props: {
    userId: {
      type: [Number, String],
      required: true
    },
    createTime: {
      type: [Date, String],
      default: null
    }
  },
  setup(props) {
    // 计算会员天数
    const memberDays = computed(() => {
      if (!props.createTime) return 0
      
      const createDate = new Date(props.createTime)
      const today = new Date()
      const diffTime = Math.abs(today - createDate)
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
      
      return diffDays
    })
    
    // 模拟统计数据
    // 实际应用中应该从API获取这些数据
    const stats = computed(() => {
      return {
        imageCount: Math.floor(Math.random() * 20),
        modelCount: Math.floor(Math.random() * 10),
        chatCount: Math.floor(Math.random() * 5)
      }
    })
    
    return {
      memberDays,
      stats
    }
  }
}
</script>

<style scoped>
.user-profile-stats {
  margin-bottom: 1.5rem;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 1rem;
  border-radius: var(--radius-md);
  background-color: var(--bg-tertiary);
  transition: transform 0.2s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1rem;
  font-size: 1.5rem;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: 600;
  line-height: 1.2;
}

.stat-label {
  font-size: 0.875rem;
  color: var(--text-muted);
}
</style>
