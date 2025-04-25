<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="modal-wrapper"
      :class="{ 'show': modelValue }"
      @click="closeOnBackdrop && close()"
    >
      <div
        class="modal-container"
        :class="[size, { 'glass-card': isGlass }]"
        @click.stop
      >
        <!-- Header -->
        <div class="modal-header" v-if="$slots.header || title">
          <slot name="header">
            <h5 class="modal-title">{{ title }}</h5>
          </slot>
          <button
            v-if="showClose"
            type="button"
            class="btn-close"
            @click="close"
            aria-label="Close"
          ></button>
        </div>

        <!-- Body -->
        <div class="modal-body">
          <slot></slot>
        </div>

        <!-- Footer -->
        <div class="modal-footer" v-if="$slots.footer">
          <slot name="footer"></slot>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'

export default {
  name: 'Modal',
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ''
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large', 'full'].includes(value)
    },
    showClose: {
      type: Boolean,
      default: true
    },
    closeOnBackdrop: {
      type: Boolean,
      default: true
    },
    isGlass: {
      type: Boolean,
      default: false
    }
  },
  emits: ['update:modelValue', 'close'],
  setup(props, { emit }) {
    // Close modal
    const close = () => {
      emit('update:modelValue', false)
      emit('close')
    }

    // Handle escape key
    const handleEscape = (event) => {
      if (event.key === 'Escape' && props.modelValue) {
        close()
      }
    }

    // Add/remove event listeners
    onMounted(() => {
      document.addEventListener('keydown', handleEscape)
    })

    onBeforeUnmount(() => {
      document.removeEventListener('keydown', handleEscape)
    })

    // Prevent body scroll when modal is open
    watch(() => props.modelValue, (value) => {
      if (value) {
        document.body.classList.add('modal-open')
      } else {
        document.body.classList.remove('modal-open')
      }
    })

    return {
      close
    }
  }
}
</script>

<style scoped>
.modal-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1050;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.3s ease, visibility 0.3s ease;
}

.modal-wrapper.show {
  opacity: 1;
  visibility: visible;
}

.modal-container {
  background-color: var(--bg-primary);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-lg);
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  transform: translateY(20px);
  transition: transform 0.3s ease;
  overflow: hidden;
}

.modal-wrapper.show .modal-container {
  transform: translateY(0);
}

/* Size variants */
.modal-container.small {
  width: 90%;
  max-width: 400px;
}

.modal-container.medium {
  width: 90%;
  max-width: 600px;
}

.modal-container.large {
  width: 90%;
  max-width: 800px;
}

.modal-container.full {
  width: 95%;
  max-width: 1200px;
}

/* Modal parts */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.modal-title {
  margin: 0;
  font-size: 1.25rem;
  font-weight: 600;
}

.modal-body {
  padding: 1rem;
  overflow-y: auto;
  flex: 1;
}

.modal-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.5rem;
  padding: 1rem;
  border-top: 1px solid var(--border-color);
}

/* Close button */
.btn-close {
  background: transparent;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  transition: background-color 0.2s ease;
}

.btn-close:hover {
  background-color: var(--bg-hover);
}

/* Glass style */
.modal-container.glass-card {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
}

:global(.dark-theme) .modal-container.glass-card {
  background: var(--glass-dark-bg);
  border-color: var(--glass-dark-border);
}

/* Responsive adjustments */
@media (max-width: 576px) {
  .modal-container.small,
  .modal-container.medium,
  .modal-container.large {
    width: 95%;
  }
}
</style>
