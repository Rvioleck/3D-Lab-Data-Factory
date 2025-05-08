<template>
  <div class="skeleton-loader" :class="[type, { 'with-animation': animate }]" :style="customStyle">
    <div v-if="type === 'text'" class="skeleton-text" :style="{ width: width }"></div>
    <div v-else-if="type === 'circle'" class="skeleton-circle" :style="{ width: size, height: size }"></div>
    <div v-else-if="type === 'rect'" class="skeleton-rect" :style="{ width: width, height: height }"></div>
    <div v-else-if="type === 'card'" class="skeleton-card">
      <div class="skeleton-card-header">
        <div class="skeleton-circle" style="width: 40px; height: 40px;"></div>
        <div class="skeleton-text-group">
          <div class="skeleton-text" style="width: 60%;"></div>
          <div class="skeleton-text" style="width: 40%;"></div>
        </div>
      </div>
      <div class="skeleton-card-body">
        <div class="skeleton-text" style="width: 100%;"></div>
        <div class="skeleton-text" style="width: 90%;"></div>
        <div class="skeleton-text" style="width: 80%;"></div>
      </div>
    </div>
    <div v-else-if="type === 'message'" class="skeleton-message">
      <div class="skeleton-message-avatar">
        <div class="skeleton-circle" style="width: 40px; height: 40px;"></div>
      </div>
      <div class="skeleton-message-content">
        <div class="skeleton-message-header">
          <div class="skeleton-text" style="width: 30%;"></div>
          <div class="skeleton-text" style="width: 20%;"></div>
        </div>
        <div class="skeleton-message-body">
          <div class="skeleton-text" style="width: 100%;"></div>
          <div class="skeleton-text" style="width: 90%;"></div>
          <div class="skeleton-text" style="width: 70%;"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SkeletonLoader',
  props: {
    type: {
      type: String,
      default: 'text',
      validator: (value) => ['text', 'circle', 'rect', 'card', 'message'].includes(value)
    },
    width: {
      type: String,
      default: '100%'
    },
    height: {
      type: String,
      default: '16px'
    },
    size: {
      type: String,
      default: '40px'
    },
    animate: {
      type: Boolean,
      default: true
    },
    customStyle: {
      type: Object,
      default: () => ({})
    }
  }
}
</script>

<style scoped>
.skeleton-loader {
  display: inline-block;
  position: relative;
  overflow: hidden;
  background-color: var(--neutral-200);
  border-radius: var(--radius-md);
}

.skeleton-loader.with-animation::after {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    90deg,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.6) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  animation: skeleton-loading 1.5s infinite;
  will-change: transform;
}

@keyframes skeleton-loading {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}

.skeleton-text {
  height: 16px;
  margin-bottom: 8px;
  border-radius: var(--radius-md);
}

.skeleton-circle {
  border-radius: 50%;
}

.skeleton-rect {
  border-radius: var(--radius-md);
}

.skeleton-card {
  width: 100%;
  border-radius: var(--radius-lg);
  padding: 16px;
}

.skeleton-card-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.skeleton-text-group {
  flex: 1;
  margin-left: 12px;
}

.skeleton-text-group .skeleton-text:last-child {
  margin-bottom: 0;
}

.skeleton-card-body .skeleton-text {
  height: 12px;
}

.skeleton-message {
  display: flex;
  width: 100%;
  margin-bottom: 16px;
}

.skeleton-message-avatar {
  margin-right: 12px;
}

.skeleton-message-content {
  flex: 1;
}

.skeleton-message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.skeleton-message-header .skeleton-text {
  height: 12px;
  margin-bottom: 0;
}

.skeleton-message-body .skeleton-text {
  height: 14px;
}
</style>
