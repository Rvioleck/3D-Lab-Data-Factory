<template>
  <button
    :type="type"
    class="custom-button"
    :class="[
      variant,
      size,
      {
        'loading': loading,
        'block': block,
        'glass-button': isGlass,
        'btn-3d': is3d,
        'icon-only': iconOnly,
        'rounded': rounded
      }
    ]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="spinner"></span>
    <i v-if="icon && !loading" :class="icon"></i>
    <span v-if="!iconOnly && $slots.default" class="button-text">
      <slot></slot>
    </span>
  </button>
</template>

<script>
export default {
  name: 'Button',
  props: {
    type: {
      type: String,
      default: 'button'
    },
    variant: {
      type: String,
      default: 'primary',
      validator: (value) => [
        'primary',
        'secondary',
        'success',
        'danger',
        'warning',
        'info',
        'light',
        'dark',
        'link',
        'outline-primary',
        'outline-secondary',
        'outline-success',
        'outline-danger',
        'outline-warning',
        'outline-info',
        'outline-light',
        'outline-dark'
      ].includes(value)
    },
    size: {
      type: String,
      default: 'medium',
      validator: (value) => ['small', 'medium', 'large'].includes(value)
    },
    icon: {
      type: String,
      default: ''
    },
    iconOnly: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    block: {
      type: Boolean,
      default: false
    },
    isGlass: {
      type: Boolean,
      default: false
    },
    is3d: {
      type: Boolean,
      default: false
    },
    rounded: {
      type: Boolean,
      default: false
    }
  },
  emits: ['click'],
  setup(props, { emit }) {
    const handleClick = (event) => {
      if (!props.disabled && !props.loading) {
        emit('click', event)
      }
    }

    return {
      handleClick
    }
  }
}
</script>

<style scoped>
.custom-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
  vertical-align: middle;
  user-select: none;
  border: 1px solid transparent;
  padding: 0.5rem 1rem;
  font-size: 1rem;
  line-height: 1.5;
  border-radius: var(--radius-md);
  transition: all 0.2s ease-in-out;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.custom-button:focus {
  outline: none;
  box-shadow: 0 0 0 0.2rem rgba(var(--primary-rgb), 0.25);
}

.custom-button:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

/* Variants */
.primary {
  background-color: var(--primary-color);
  color: white;
}

.primary:hover:not(:disabled) {
  background-color: var(--primary-hover);
}

.secondary {
  background-color: var(--secondary-color);
  color: white;
}

.secondary:hover:not(:disabled) {
  background-color: var(--secondary-hover);
}

.success {
  background-color: var(--success-color);
  color: white;
}

.success:hover:not(:disabled) {
  background-color: var(--success-hover);
}

.danger {
  background-color: var(--error-color);
  color: white;
}

.danger:hover:not(:disabled) {
  background-color: var(--error-hover);
}

.warning {
  background-color: var(--warning-color);
  color: white;
}

.warning:hover:not(:disabled) {
  background-color: var(--warning-hover);
}

.info {
  background-color: var(--info-color);
  color: white;
}

.info:hover:not(:disabled) {
  background-color: var(--info-hover);
}

.light {
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

.light:hover:not(:disabled) {
  background-color: var(--bg-hover);
}

.dark {
  background-color: var(--text-primary);
  color: var(--bg-primary);
}

.dark:hover:not(:disabled) {
  background-color: var(--text-secondary);
}

.link {
  background-color: transparent;
  color: var(--primary-color);
  text-decoration: none;
  border: none;
}

.link:hover:not(:disabled) {
  text-decoration: underline;
  background-color: transparent;
}

/* Outline variants */
.outline-primary {
  background-color: transparent;
  color: var(--primary-color);
  border-color: var(--primary-color);
}

.outline-primary:hover:not(:disabled) {
  background-color: var(--primary-color);
  color: white;
}

.outline-secondary {
  background-color: transparent;
  color: var(--secondary-color);
  border-color: var(--secondary-color);
}

.outline-secondary:hover:not(:disabled) {
  background-color: var(--secondary-color);
  color: white;
}

/* Size variants */
.small {
  padding: 0.25rem 0.5rem;
  font-size: 0.875rem;
}

.medium {
  padding: 0.5rem 1rem;
  font-size: 1rem;
}

.large {
  padding: 0.75rem 1.5rem;
  font-size: 1.125rem;
}

/* Block button */
.block {
  display: flex;
  width: 100%;
}

/* Icon spacing */
.custom-button i {
  margin-right: 0.5rem;
}

.icon-only i {
  margin-right: 0;
}

/* Loading state */
.loading {
  color: transparent !important;
}

.spinner {
  display: inline-block;
  width: 1rem;
  height: 1rem;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Icon only */
.icon-only {
  padding: 0.5rem;
  border-radius: 50%;
}

.icon-only.small {
  padding: 0.25rem;
}

.icon-only.large {
  padding: 0.75rem;
}

/* Rounded */
.rounded {
  border-radius: var(--radius-full);
}

/* Glass button */
.glass-button {
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur));
  -webkit-backdrop-filter: blur(var(--glass-blur));
  border: 1px solid var(--glass-border);
}

.glass-button:hover:not(:disabled) {
  background: var(--glass-hover);
}

/* 3D button */
.btn-3d {
  transform: translateY(0);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.btn-3d:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 8px rgba(0, 0, 0, 0.15);
}

.btn-3d:active:not(:disabled) {
  transform: translateY(1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>
