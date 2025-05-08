<template>
  <div class="no-permission-container">
    <div class="text-center">
      <i class="bi bi-shield-lock text-danger" style="font-size: 5rem;"></i>
      <h2 class="mt-4">{{ title }}</h2>
      <p class="text-muted mt-3">{{ message }}</p>
      <router-link :to="backLink" class="btn btn-primary mt-3">
        <i class="bi bi-house me-2"></i>{{ backText }}
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NoPermission',
  props: {
    title: {
      type: String,
      default: '权限不足'
    },
    message: {
      type: String,
      default: '很抱歉，您没有权限访问此功能。'
    },
    backLink: {
      type: String,
      default: '/home'
    },
    backText: {
      type: String,
      default: '返回首页'
    }
  }
}
</script>

<style scoped>
.no-permission-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 2rem;
}
</style>
