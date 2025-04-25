<template>
  <Teleport to="body">
    <div class="toast-container" :class="position">
      <TransitionGroup name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          class="toast"
          :class="[toast.type, { 'glass-card': isGlass }]"
        >
          <div class="toast-icon" v-if="toast.icon">
            <i :class="toast.icon"></i>
          </div>
          <div class="toast-content">
            <div class="toast-title" v-if="toast.title">{{ toast.title }}</div>
            <div class="toast-message">{{ toast.message }}</div>
          </div>
          <button class="toast-close" @click="removeToast(toast.id)">
            <i class="bi bi-x"></i>
          </button>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<script>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { initToast } from '@/utils/toast'

// Toast types
export const TOAST_TYPES = {
  SUCCESS: 'success',
  ERROR: 'error',
  WARNING: 'warning',
  INFO: 'info'
}

// Toast icons
const TOAST_ICONS = {
  [TOAST_TYPES.SUCCESS]: 'bi bi-check-circle',
  [TOAST_TYPES.ERROR]: 'bi bi-exclamation-circle',
  [TOAST_TYPES.WARNING]: 'bi bi-exclamation-triangle',
  [TOAST_TYPES.INFO]: 'bi bi-info-circle'
}

export default {
  name: 'Toast',
  props: {
    isGlass: {
      type: Boolean,
      default: false
    },
    position: {
      type: String,
      default: 'top-right',
      validator: (value) => [
        'top-right',
        'top-left',
        'bottom-right',
        'bottom-left',
        'top-center',
        'bottom-center'
      ].includes(value)
    }
  },
  setup(props) {
    const toasts = ref([])
    const toastTimeouts = ref({})

    // Add a new toast
    const show = (message, options = {}) => {
      const id = Date.now().toString()
      const duration = options.duration || 5000
      const type = options.type || TOAST_TYPES.INFO
      const icon = options.icon || TOAST_ICONS[type]

      // Create toast object
      const toastObj = {
        id,
        title: options.title || '',
        message: message || '',
        type,
        icon,
        duration
      }

      // Add toast to list
      toasts.value.push(toastObj)

      // Set timeout to remove toast
      if (duration > 0) {
        toastTimeouts.value[id] = setTimeout(() => {
          removeToast(id)
        }, duration)
      }

      return id
    }

    // Remove a toast by ID
    const removeToast = (id) => {
      // Clear timeout
      if (toastTimeouts.value[id]) {
        clearTimeout(toastTimeouts.value[id])
        delete toastTimeouts.value[id]
      }

      // Remove toast from list
      toasts.value = toasts.value.filter(toast => toast.id !== id)
    }

    // Clear all toasts
    const clearToasts = () => {
      // Clear all timeouts
      Object.values(toastTimeouts.value).forEach(timeout => {
        clearTimeout(timeout)
      })
      toastTimeouts.value = {}

      // Clear all toasts
      toasts.value = []
    }

    // Register global toast methods
    onMounted(() => {
      // Initialize toast utility with this component instance
      initToast({
        show,
        clearToasts
      })
    })

    // Clean up on unmount
    onBeforeUnmount(() => {
      clearToasts()
    })

    return {
      toasts,
      removeToast
    }
  }
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  max-width: 350px;
  width: 100%;
  padding: 1rem;
}

/* Position variants */
.toast-container.top-right {
  top: 0;
  right: 0;
}

.toast-container.top-left {
  top: 0;
  left: 0;
}

.toast-container.bottom-right {
  bottom: 0;
  right: 0;
}

.toast-container.bottom-left {
  bottom: 0;
  left: 0;
}

.toast-container.top-center {
  top: 0;
  left: 50%;
  transform: translateX(-50%);
}

.toast-container.bottom-center {
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
}

/* Toast styles */
.toast {
  display: flex;
  align-items: flex-start;
  padding: 1rem;
  border-radius: var(--radius-md);
  background-color: var(--bg-primary);
  box-shadow: var(--shadow-md);
  border-left: 4px solid;
  max-width: 100%;
}

.toast.success {
  border-left-color: var(--success-color);
}

.toast.error {
  border-left-color: var(--error-color);
}

.toast.warning {
  border-left-color: var(--warning-color);
}

.toast.info {
  border-left-color: var(--info-color);
}

/* Toast parts */
.toast-icon {
  margin-right: 0.75rem;
  font-size: 1.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.toast.success .toast-icon {
  color: var(--success-color);
}

.toast.error .toast-icon {
  color: var(--error-color);
}

.toast.warning .toast-icon {
  color: var(--warning-color);
}

.toast.info .toast-icon {
  color: var(--info-color);
}

.toast-content {
  flex: 1;
  min-width: 0;
}

.toast-title {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.toast-message {
  word-break: break-word;
}

.toast-close {
  background: transparent;
  border: none;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 1rem;
  padding: 0.25rem;
  margin-left: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
}

.toast-close:hover {
  color: var(--text-primary);
}

/* Glass style */
.toast.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
}

:global(.dark-theme) .toast.glass-card {
  background: var(--glass-dark-bg);
  border-color: var(--glass-dark-border);
}

/* Toast animations */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>
