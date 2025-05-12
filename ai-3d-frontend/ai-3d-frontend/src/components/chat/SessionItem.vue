<template>
  <div
    class="session-item"
    :class="{ 'active': isActive }"
    :data-id="session.id"
    @click="$emit('select')"
    @mouseenter="showActions = true"
    @mouseleave="showActions = false"
  >
    <div class="session-info">
      <div class="session-icon">
        <i class="bi" :class="isActive ? 'bi-chat-dots-fill' : 'bi-chat-dots'"></i>
      </div>
      <div class="session-details">
        <div class="session-name">{{ session.sessionName }}</div>
        <div class="session-time">
          {{ formatTime(session.createTime) }}
        </div>
      </div>
    </div>
    <div class="session-actions" v-if="showActions || isRenaming">
      <button
        v-if="!isRenaming"
        class="action-btn"
        title="重命名"
        @click.stop="startRename"
      >
        <i class="bi bi-pencil"></i>
      </button>
      <button
        v-if="!isRenaming"
        class="action-btn text-danger"
        title="删除"
        @click.stop="confirmDelete"
      >
        <i class="bi bi-trash"></i>
      </button>
    </div>

    <!-- Rename form -->
    <div class="rename-form" v-if="isRenaming" @click.stop>
      <input
        type="text"
        class="rename-input"
        v-model="newName"
        placeholder="输入新名称"
        ref="renameInput"
        @keyup.enter="saveRename"
        @keyup.esc="cancelRename"
      >
      <div class="rename-actions">
        <button class="btn-cancel" @click="cancelRename">取消</button>
        <button class="btn-save" @click="saveRename">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'

const props = defineProps({
  session: {
    type: Object,
    required: true
  },
  isActive: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['select', 'delete', 'rename'])

const showActions = ref(false)
const isRenaming = ref(false)
const newName = ref('')
const renameInput = ref(null)

// Format time
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)

  // If today, show only time
  const today = new Date()
  if (date.toDateString() === today.toDateString()) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }

  // If this year, show month and day
  if (date.getFullYear() === today.getFullYear()) {
    return date.toLocaleDateString([], { month: 'short', day: 'numeric' })
  }

  // Otherwise show date with year
  return date.toLocaleDateString([], { year: 'numeric', month: 'short', day: 'numeric' })
}

// Delete session
const confirmDelete = () => {
  if (confirm(`确定要删除会话 "${props.session.sessionName}" 吗？`)) {
    emit('delete')
  }
}

// Rename session
const startRename = () => {
  newName.value = props.session.sessionName
  isRenaming.value = true
  
  // Focus the input after it's rendered
  nextTick(() => {
    renameInput.value?.focus()
  })
}

const saveRename = () => {
  if (newName.value.trim()) {
    emit('rename', newName.value.trim())
    isRenaming.value = false
  }
}

const cancelRename = () => {
  isRenaming.value = false
}

// 不再需要这些事件监听器，因为我们使用了Vue的@mouseenter和@mouseleave指令
</script>

<style scoped>
.session-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem;
  border-radius: var(--radius-md);
  margin-bottom: 0.5rem;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  border: 1px solid transparent;
}

.session-item:hover {
  background-color: var(--bg-tertiary);
  border-color: var(--border-color);
  transform: translateX(3px);
}

.session-item.active {
  background-color: var(--primary-light);
  border-color: var(--primary-color);
}

.session-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex: 1;
  min-width: 0;
}

.session-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--bg-secondary);
  color: var(--primary-color);
}

.active .session-icon {
  background-color: var(--primary-color);
  color: white;
}

.session-details {
  flex: 1;
  min-width: 0;
}

.session-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 0.75rem;
  color: var(--text-tertiary);
  margin-top: 0.25rem;
}

.session-actions {
  display: flex;
  gap: 0.5rem;
}

.action-btn {
  background: none;
  border: none;
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.action-btn:hover {
  background-color: var(--bg-secondary);
}

.rename-form {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-primary);
  border-radius: var(--radius-md);
  padding: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  z-index: 10;
  border: 1px solid var(--primary-color);
  box-shadow: var(--shadow-md);
}

.rename-input {
  padding: 0.5rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border-color);
  background-color: var(--bg-secondary);
  color: var(--text-primary);
}

.rename-input:focus {
  outline: none;
  border-color: var(--primary-color);
}

.rename-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

.btn-cancel, .btn-save {
  padding: 0.25rem 0.5rem;
  border-radius: var(--radius-sm);
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
}

.btn-cancel {
  background-color: var(--bg-tertiary);
  color: var(--text-primary);
}

.btn-save {
  background-color: var(--primary-color);
  color: white;
}
</style>
