<template>
  <div class="home-view">
    <div class="container">
      <div class="row align-items-center min-vh-75">
        <div class="col-lg-6 py-5">
          <h1 class="display-4 fw-bold mb-4">AI 智能助手</h1>
          <p class="lead mb-4">
            基于先进的人工智能技术，为您提供智能对话服务。无论是日常聊天、学习辅助还是专业咨询，都能给您带来优质的体验。
          </p>
          <div class="features mb-5">
            <div class="feature-item">
              <i class="bi bi-lightning-charge-fill text-primary"></i>
              <span>快速响应</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-shield-check text-primary"></i>
              <span>安全可靠</span>
            </div>
            <div class="feature-item">
              <i class="bi bi-chat-square-text-fill text-primary"></i>
              <span>智能对话</span>
            </div>
          </div>
          <div class="d-flex flex-wrap gap-3">
            <router-link to="/chat" class="btn btn-primary btn-lg rounded-pill">
              <i class="bi bi-chat-dots me-2"></i>开始聊天
            </router-link>
            <router-link to="/register" class="btn btn-outline-primary btn-lg rounded-pill">
              <i class="bi bi-person-plus me-2"></i>注册账号
            </router-link>
          </div>
        </div>
        <div class="col-lg-6 d-none d-lg-block">
          <div class="hero-image text-center">
            <img src="../assets/ai-chat.svg" alt="AI聊天" class="img-fluid" />
          </div>
        </div>
      </div>
    </div>
    
    <div class="bg-light py-5 mt-4">
      <div class="container">
        <div class="row justify-content-center">
          <div class="col-lg-8 text-center">
            <h2 class="mb-4">为什么选择我们的AI助手？</h2>
            <p class="lead mb-5">
              我们的AI助手采用最新的大语言模型技术，能够理解复杂的问题，提供准确的回答，让您的交流更加高效。
            </p>
          </div>
        </div>
        
        <div class="row g-4">
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body p-4 text-center">
                <div class="feature-icon mb-3">
                  <i class="bi bi-cpu"></i>
                </div>
                <h4>智能理解</h4>
                <p class="text-muted">
                  先进的自然语言处理能力，能够理解您的意图，提供精准的回答。
                </p>
              </div>
            </div>
          </div>
          
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body p-4 text-center">
                <div class="feature-icon mb-3">
                  <i class="bi bi-clock-history"></i>
                </div>
                <h4>全天候服务</h4>
                <p class="text-muted">
                  24小时随时待命，无论何时何地，都能为您提供即时的帮助。
                </p>
              </div>
            </div>
          </div>
          
          <div class="col-md-4">
            <div class="card h-100 border-0 shadow-sm">
              <div class="card-body p-4 text-center">
                <div class="feature-icon mb-3">
                  <i class="bi bi-graph-up"></i>
                </div>
                <h4>持续进步</h4>
                <p class="text-muted">
                  我们的AI模型不断学习和优化，服务质量会越来越好。
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8 text-center">
          <h2 class="mb-4">准备好开始了吗？</h2>
          <p class="lead mb-5">
            立即注册账号，体验智能AI助手带来的便捷服务。
          </p>
          <div class="d-flex justify-content-center gap-3">
            <router-link to="/register" class="btn btn-primary btn-lg rounded-pill">
              <i class="bi bi-person-plus me-2"></i>免费注册
            </router-link>
            <router-link to="/login" class="btn btn-outline-primary btn-lg rounded-pill">
              <i class="bi bi-box-arrow-in-right me-2"></i>立即登录
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home-view {
  overflow-x: hidden;
}

.min-vh-75 {
  min-height: 75vh;
}

.features {
  display: flex;
  gap: 1.5rem;
  flex-wrap: wrap;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--bg-tertiary);
  padding: 0.5rem 1rem;
  border-radius: var(--radius-full);
  font-weight: 500;
}

.feature-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  background-color: var(--primary-light);
  color: var(--primary-color);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
  font-size: 2rem;
}

.card {
  border-radius: var(--radius-lg);
  transition: transform 0.3s ease;
}

.card:hover {
  transform: translateY(-5px);
}

.hero-image {
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
  100% {
    transform: translateY(0px);
  }
}

@media (max-width: 992px) {
  .min-vh-75 {
    min-height: auto;
  }
}
</style>
