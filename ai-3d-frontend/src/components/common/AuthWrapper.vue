<template>
  <div v-if="hasPermission">
    <slot></slot>
  </div>
  <div v-else-if="showFallback">
    <slot name="fallback">
      <div class="auth-fallback">
        <p>{{ fallbackMessage }}</p>
      </div>
    </slot>
  </div>
</template>

<script>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

export default {
  name: 'AuthWrapper',
  props: {
    requiresAuth: {
      type: Boolean,
      default: false
    },
    requiresAdmin: {
      type: Boolean,
      default: false
    },
    requiresOwner: {
      type: Boolean,
      default: false
    },
    resourceOwnerId: {
      type: [String, Number],
      default: null
    },
    showFallback: {
      type: Boolean,
      default: false
    },
    fallbackMessage: {
      type: String,
      default: '您没有权限查看此内容'
    }
  },
  setup(props) {
    const userStore = useUserStore()
    
    const hasPermission = computed(() => {
      const isLoggedIn = userStore.isLoggedIn
      const isAdmin = userStore.isAdmin
      const userId = userStore.user?.id
      
      if (props.requiresAuth && !isLoggedIn) return false
      if (props.requiresAdmin && !isAdmin) return false
      if (props.requiresOwner && props.resourceOwnerId !== userId && !isAdmin) return false
      
      return true
    })
    
    return {
      hasPermission
    }
  }
}
</script>

<style scoped>
.auth-fallback {
  padding: 1rem;
  background-color: var(--bg-secondary);
  border-radius: var(--radius-md);
  text-align: center;
  color: var(--text-secondary);
  margin: 1rem 0;
}
</style>
