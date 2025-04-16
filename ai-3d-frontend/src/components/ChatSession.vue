<template>
  <div
    class="chat-session p-3 mb-2 d-flex justify-content-between align-items-center"
    :class="{ 'active': isActive }"
    @click="selectSession"
  >
    <div class="session-info d-flex align-items-center">
      <div class="session-icon me-2">
        <i class="bi" :class="isActive ? 'bi-chat-dots-fill' : 'bi-chat-dots'"></i>
      </div>
      <div>
        <div class="session-name">{{ session.sessionName }}</div>
        <div class="session-time text-muted small">
          {{ formatTime(session.createTime) }}
        </div>
      </div>
    </div>
    <div class="session-actions">
      <button
        class="btn btn-sm btn-icon"
        title="重命名"
        @click.stop="startRename"
      >
        <i class="bi bi-pencil"></i>
      </button>
      <button
        class="btn btn-sm btn-icon text-danger"
        title="删除"
        @click.stop="confirmDelete"
      >
        <i class="bi bi-trash"></i>
      </button>
    </div>

    <!-- 重命名弹窗 -->
    <div class="rename-modal" v-if="isRenaming" @click.stop>
      <div class="rename-content p-2">
        <input
          type="text"
          class="form-control form-control-sm mb-2"
          v-model="newName"
          placeholder="输入新名称"
          ref="renameInput"
          @keyup.enter="saveRename"
          @keyup.esc="cancelRename"
        >
        <div class="d-flex justify-content-end">
          <button class="btn btn-sm btn-secondary me-2" @click="cancelRename">取消</button>
          <button class="btn btn-sm btn-primary" @click="saveRename">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed, ref, nextTick } from 'vue'

export default {
  name: 'ChatSession',
  props: {
    session: {
      type: Object,
      required: true
    },
    currentSessionId: {
      type: String,
      required: true
    }
  },
  emits: ['select', 'delete', 'rename'],
  setup(props, { emit }) {
    const isActive = computed(() => props.session.id === props.currentSessionId)
    const isRenaming = ref(false)
    const newName = ref('')
    const renameInput = ref(null)

    // 选择会话
    const selectSession = () => {
      if (!isRenaming.value) {
        emit('select', props.session.id)
      }
    }

    // 删除会话
    const confirmDelete = () => {
      if (confirm(`确定要删除会话 "${props.session.sessionName}" 吗？`)) {
        emit('delete', props.session.id)
      }
    }

    // 开始重命名
    const startRename = () => {
      newName.value = props.session.sessionName
      isRenaming.value = true

      // 等待DOM更新后聚焦到输入框
      nextTick(() => {
        if (renameInput.value) {
          renameInput.value.focus()
          renameInput.value.select()
        }
      })
    }

    // 保存重命名
    const saveRename = () => {
      if (newName.value.trim() && newName.value !== props.session.sessionName) {
        emit('rename', {
          id: props.session.id,
          name: newName.value.trim()
        })
      }
      isRenaming.value = false
    }

    // 取消重命名
    const cancelRename = () => {
      isRenaming.value = false
    }

    // 格式化时间
    const formatTime = (timeString) => {
      if (!timeString) return ''
      const date = new Date(timeString)

      // 如果是今天的日期，只显示时间
      const today = new Date()
      if (date.toDateString() === today.toDateString()) {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      }

      // 否则显示日期和时间
      return date.toLocaleDateString([], { month: 'short', day: 'numeric' }) + ' ' +
             date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    }

    return {
      isActive,
      isRenaming,
      newName,
      renameInput,
      selectSession,
      confirmDelete,
      startRename,
      saveRename,
      cancelRename,
      formatTime
    }
  }
}
</script>

<style scoped>
.chat-session {
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  border: 1px solid transparent;
  animation: fadeIn 0.3s ease;
  transform-origin: center;
}

.chat-session:hover {
  background-color: var(--bg-tertiary);
  border-color: var(--border-color);
  transform: translateX(3px);
}

.chat-session.active {
  background-color: var(--primary-light);
  border-color: var(--primary-color);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(var(--primary-rgb), 0.4); }
  70% { box-shadow: 0 0 0 5px rgba(var(--primary-rgb), 0); }
  100% { box-shadow: 0 0 0 0 rgba(var(--primary-rgb), 0); }
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.session-info {
  flex: 1;
  min-width: 0; /* 允许子元素收缩 */
}

.session-icon {
  color: var(--primary-color);
  font-size: 1.2rem;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-tertiary);
  border-radius: var(--radius-full);
  transition: all 0.3s ease;
}

.chat-session:hover .session-icon {
  transform: scale(1.1);
}

.chat-session.active .session-icon {
  background-color: var(--primary-color);
  color: white;
}

.session-name {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 180px;
}

.session-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: all 0.3s ease;
}

.chat-session:hover .session-actions,
.chat-session.active .session-actions {
  opacity: 1;
}

.btn-icon {
  width: 28px;
  height: 28px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: var(--radius-full);
  background-color: var(--bg-tertiary);
  border: none;
  transition: all 0.2s ease;
  transform: scale(0.9);
}

.btn-icon:hover {
  background-color: var(--bg-secondary);
  transform: scale(1.1);
}

.btn-icon.text-danger:hover {
  background-color: var(--danger-light, #ffebee);
  color: var(--danger, #dc3545);
}

/* 重命名弹窗 */
.rename-modal {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: var(--bg-primary);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  z-index: 10;
  animation: fadeIn 0.2s ease;
}

.rename-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
</style>
