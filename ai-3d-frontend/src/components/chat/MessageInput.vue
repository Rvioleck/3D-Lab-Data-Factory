<template>
  <div class="message-input-wrapper">
    <textarea
      ref="inputElement"
      class="message-input"
      :class="{ 'disabled': disabled }"
      v-model="inputContent"
      :disabled="disabled"
      :placeholder="placeholder"
      rows="1"
      @keydown.enter.prevent="handleEnterKey"
      @input="autoResize"
    ></textarea>
    
    <div class="input-actions">
      <button 
        class="send-button"
        :disabled="!canSend || disabled"
        @click="sendMessage"
        title="发送消息"
      >
        <i class="bi" :class="buttonIcon"></i>
      </button>
    </div>
    
    <div v-if="disabled" class="streaming-indicator">
      <div class="typing-dots">
        <span></span>
        <span></span>
        <span></span>
      </div>
      <span class="streaming-text">AI正在回复...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onMounted } from 'vue'

const props = defineProps({
  disabled: {
    type: Boolean,
    default: false
  },
  placeholder: {
    type: String,
    default: '输入消息...'
  }
})

const emit = defineEmits(['send'])

const inputElement = ref(null)
const inputContent = ref('')

const canSend = computed(() => inputContent.value.trim().length > 0)

const buttonIcon = computed(() => {
  if (props.disabled) return 'bi-hourglass-split'
  return canSend.value ? 'bi-send-fill' : 'bi-send'
})

const sendMessage = () => {
  if (!canSend.value || props.disabled) return
  
  const content = inputContent.value.trim()
  emit('send', content)
  inputContent.value = ''
  
  // Reset textarea height
  if (inputElement.value) {
    inputElement.value.style.height = 'auto'
  }
}

const handleEnterKey = (event) => {
  // Send on Enter, but allow Shift+Enter for new line
  if (!event.shiftKey && !props.disabled) {
    sendMessage()
  }
}

const autoResize = () => {
  const el = inputElement.value
  if (!el) return
  
  // Reset height to auto to get the correct scrollHeight
  el.style.height = 'auto'
  
  // Set new height based on scrollHeight (with max height limit)
  const newHeight = Math.min(el.scrollHeight, 150)
  el.style.height = `${newHeight}px`
}

// Focus input when enabled
watch(() => props.disabled, (newVal) => {
  if (!newVal) {
    nextTick(() => {
      inputElement.value?.focus()
    })
  }
})

// Auto-resize on content change
watch(inputContent, () => {
  nextTick(autoResize)
})

onMounted(() => {
  if (!props.disabled) {
    inputElement.value?.focus()
  }
})
</script>

<style scoped>
.message-input-wrapper {
  position: relative;
}

.message-input {
  width: 100%;
  padding: 0.75rem 3rem 0.75rem 1rem;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  resize: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
  min-height: 44px;
  max-height: 150px;
  line-height: 1.5;
}

.message-input:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 2px rgba(var(--primary-rgb), 0.2);
}

.message-input.disabled {
  background-color: var(--bg-tertiary);
  cursor: not-allowed;
}

.input-actions {
  position: absolute;
  right: 0.75rem;
  bottom: 0.75rem;
  display: flex;
  gap: 0.5rem;
}

.send-button {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--primary-color);
  color: white;
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s ease, transform 0.1s ease;
}

.send-button:hover:not(:disabled) {
  background-color: var(--primary-hover);
  transform: scale(1.05);
}

.send-button:disabled {
  background-color: var(--bg-tertiary);
  color: var(--text-tertiary);
  cursor: not-allowed;
}

.streaming-indicator {
  position: absolute;
  right: 0.75rem;
  top: -1.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--primary-color);
  font-size: 0.85rem;
}

.typing-dots {
  display: flex;
  gap: 3px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: var(--primary-color);
  animation: typingAnimation 1.4s infinite ease-in-out;
}

.typing-dots span:nth-child(1) {
  animation-delay: 0s;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typingAnimation {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}
</style>
