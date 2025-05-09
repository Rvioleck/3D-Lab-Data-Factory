<template>
  <div class="session-list">
    <div v-if="sessions.length === 0" class="empty-sessions">
      <div class="text-center py-4">
        <i class="bi bi-chat-square-text text-muted" style="font-size: 3rem;"></i>
        <p class="text-muted mt-3">暂无会话</p>
        <p class="text-muted small">发送消息开始新对话</p>
      </div>
    </div>
    
    <div v-else class="sessions-container">
      <SessionItem
        v-for="session in sessions"
        :key="session.id"
        :session="session"
        :isActive="session.id === currentSessionId"
        @select="$emit('select', session.id)"
        @delete="$emit('delete', session.id)"
        @rename="$emit('rename', session.id, $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import SessionItem from './SessionItem.vue'

defineProps({
  sessions: {
    type: Array,
    default: () => []
  },
  currentSessionId: {
    type: String,
    default: null
  }
})

defineEmits(['select', 'delete', 'rename'])
</script>

<style scoped>
.session-list {
  height: 100%;
  overflow-y: auto;
}

.empty-sessions {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0.7;
}

.sessions-container {
  padding: 0.5rem;
}
</style>
