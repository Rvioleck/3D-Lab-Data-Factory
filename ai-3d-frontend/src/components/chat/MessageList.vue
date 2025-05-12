<template>
  <div class="message-list">
    <div v-if="messages.length === 0 && !isStreaming" class="empty-messages">
      <p class="text-center text-muted">开始发送消息，与AI助手对话</p>
    </div>

    <template v-else>
      <!-- Date dividers and messages -->
      <template v-for="(message, index) in messages" :key="message.id">
        <!-- Date divider -->
        <div
          v-if="shouldShowDateDivider(message, messages[index-1])"
          class="date-divider"
        >
          <span>{{ formatMessageDate(message.createTime) }}</span>
        </div>

        <!-- Message -->
        <Message
          :message="message"
          @delete="$emit('delete', message.id)"
          @edit="(messageId, content) => $emit('edit', messageId, content)"
        />
      </template>

      <!-- Streaming message -->
      <div v-if="isStreaming" class="streaming-container">
        <div class="message ai-message">
          <div class="message-avatar">
            <div class="avatar-circle ai-avatar">
              <i class="bi bi-robot"></i>
            </div>
          </div>
          <div class="message-content">
            <div class="message-header">
              <span class="message-sender">AI 助手</span>
              <span class="message-time text-muted small">
                {{ formatTime(new Date()) }}
              </span>
            </div>
            <div class="message-bubble ai-bubble">
              <StreamingResponse :content="streamingContent" />
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import Message from './Message.vue'
import StreamingResponse from './StreamingResponse.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  isStreaming: {
    type: Boolean,
    default: false
  },
  streamingContent: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['delete', 'edit'])

// Format date for message groups
const formatMessageDate = (dateString) => {
  if (!dateString) return ''

  const date = new Date(dateString)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  // Check if date is today
  if (date.toDateString() === today.toDateString()) {
    return '今天'
  }

  // Check if date is yesterday
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }

  // Otherwise return formatted date
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// Format time
const formatTime = (dateObj) => {
  return dateObj.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Determine if we should show a date divider
const shouldShowDateDivider = (currentMessage, previousMessage) => {
  if (!previousMessage) return true

  const currentDate = new Date(currentMessage.createTime).toDateString()
  const previousDate = new Date(previousMessage.createTime).toDateString()

  return currentDate !== previousDate
}
</script>

<style scoped>
.message-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.empty-messages {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  opacity: 0.7;
}

.date-divider {
  display: flex;
  align-items: center;
  margin: 1rem 0;
  color: var(--text-tertiary);
  font-size: 0.85rem;
}

.date-divider::before,
.date-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background-color: var(--border-color);
}

.date-divider::before {
  margin-right: 0.5rem;
}

.date-divider::after {
  margin-left: 0.5rem;
}

.streaming-container {
  margin-top: 0.5rem;
}

.message {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

.message-avatar {
  margin-right: 8px;
}

.avatar-circle {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 0.9rem;
  box-shadow: var(--shadow-sm);
}

.ai-avatar {
  background-color: var(--secondary-color);
}

.message-content {
  flex: 1;
  max-width: calc(100% - 40px);
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.message-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  position: relative;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease;
}

.ai-bubble {
  background-color: var(--bg-tertiary);
  border-radius: 0 12px 12px 12px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
