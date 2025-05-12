<template>
  <div class="chat-interface">
    <!-- Sidebar with sessions -->
    <div class="chat-sidebar" :class="{ 'mobile-hidden': !showSidebar }">
      <div class="sidebar-header">
        <h5 class="m-0">对话历史</h5>
        <button class="btn btn-sm btn-primary" @click="startNewChat">
          <i class="bi bi-plus-lg"></i> 新对话
        </button>
      </div>
      
      <div class="sidebar-content">
        <SessionList 
          :sessions="sortedSessions" 
          :currentSessionId="currentSessionId"
          @select="selectSession"
          @delete="deleteSession"
          @rename="renameSession"
        />
      </div>
    </div>
    
    <!-- Main chat area -->
    <div class="chat-main">
      <!-- Mobile sidebar toggle -->
      <div class="mobile-sidebar-toggle d-md-none">
        <button class="btn btn-sm btn-outline-secondary" @click="toggleSidebar">
          <i class="bi" :class="showSidebar ? 'bi-x-lg' : 'bi-list'"></i>
          {{ showSidebar ? '关闭菜单' : '对话历史' }}
        </button>
      </div>
      
      <!-- Welcome screen for new chat -->
      <div v-if="isNewChat && currentMessages.length === 0 && !isStreaming" class="welcome-screen">
        <div class="welcome-content">
          <i class="bi bi-robot text-primary"></i>
          <h2>欢迎使用AI助手</h2>
          <p class="text-muted">这是一个新对话，直接在下方输入框中发送消息，系统将自动创建新会话！</p>
          
          <div class="suggestions">
            <div class="suggestion-title">您可以尝试这些问题：</div>
            <div class="suggestion-items">
              <button class="suggestion-btn" @click="usePromptSuggestion('你能做什么？')">
                你能做什么？
              </button>
              <button class="suggestion-btn" @click="usePromptSuggestion('帮我写一个简单的Java程序')">
                帮我写一个简单的Java程序
              </button>
              <button class="suggestion-btn" @click="usePromptSuggestion('解释一下什么是人工智能')">
                解释一下什么是人工智能
              </button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Session header -->
      <div v-else-if="currentSession" class="chat-header">
        <h5>{{ currentSession.sessionName }}</h5>
        <div class="header-actions">
          <button class="btn btn-sm btn-icon" title="新对话" @click="startNewChat">
            <i class="bi bi-plus-circle"></i>
          </button>
        </div>
      </div>
      
      <!-- Messages area -->
      <div class="chat-messages" ref="messagesContainer">
        <MessageList 
          :messages="currentMessages" 
          :isStreaming="isStreaming"
          :streamingContent="streamingContent"
          @delete="deleteMessage"
          @edit="editMessage"
        />
      </div>
      
      <!-- Input area -->
      <div class="chat-input-container">
        <MessageInput 
          :disabled="isStreaming" 
          :placeholder="inputPlaceholder"
          @send="handleSendMessage" 
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import SessionList from './SessionList.vue'
import MessageList from './MessageList.vue'
import MessageInput from './MessageInput.vue'

const chatStore = useChatStore()

// State
const messagesContainer = ref(null)
const showSidebar = ref(false)

// Computed properties
const sortedSessions = computed(() => chatStore.sortedSessions)
const currentSessionId = computed(() => chatStore.currentSessionId)
const currentSession = computed(() => chatStore.currentSession)
const currentMessages = computed(() => chatStore.currentMessages)
const isStreaming = computed(() => chatStore.isStreaming)
const streamingContent = computed(() => chatStore.streamingContent)
const isNewChat = computed(() => chatStore.isNewChat)

const inputPlaceholder = computed(() => {
  if (isNewChat.value) {
    return '输入消息开始新对话，系统将自动创建会话...'
  }
  return '输入消息...'
})

// Methods
const loadSessions = async () => {
  try {
    await chatStore.fetchSessions()
    if (sortedSessions.value.length > 0 && !currentSessionId.value) {
      selectSession(sortedSessions.value[0].id)
    }
  } catch (error) {
    console.error('Failed to load sessions:', error)
  }
}

const selectSession = async (sessionId) => {
  await chatStore.setCurrentSession(sessionId)
  scrollToBottom()
  showSidebar.value = false
}

const startNewChat = () => {
  chatStore.startNewChat()
  scrollToBottom()
  showSidebar.value = false
}

const handleSendMessage = async (content) => {
  if (!content.trim()) return
  
  try {
    await chatStore.sendMessage(content)
    scrollToBottom(true)
  } catch (error) {
    console.error('Failed to send message:', error)
  }
}

const deleteSession = async (sessionId) => {
  try {
    await chatStore.deleteSession(sessionId)
  } catch (error) {
    console.error('Failed to delete session:', error)
  }
}

const renameSession = async (sessionId, newName) => {
  try {
    await chatStore.renameSession(sessionId, newName)
  } catch (error) {
    console.error('Failed to rename session:', error)
  }
}

const deleteMessage = async (messageId) => {
  try {
    await chatStore.deleteMessage(messageId)
  } catch (error) {
    console.error('Failed to delete message:', error)
  }
}

const editMessage = async (messageId, newContent) => {
  try {
    await chatStore.editMessage(messageId, newContent)
  } catch (error) {
    console.error('Failed to edit message:', error)
  }
}

const toggleSidebar = () => {
  showSidebar.value = !showSidebar.value
}

const scrollToBottom = async (force = false) => {
  await nextTick()
  if (messagesContainer.value) {
    const container = messagesContainer.value
    const isAtBottom = container.scrollHeight - container.scrollTop - container.clientHeight < 100
    
    if (isAtBottom || force) {
      container.scrollTop = container.scrollHeight
    }
  }
}

const usePromptSuggestion = (suggestion) => {
  handleSendMessage(suggestion)
}

// Watchers
watch(() => currentMessages.value.length, () => {
  scrollToBottom()
})

watch(() => streamingContent.value, () => {
  scrollToBottom()
})

// Lifecycle hooks
onMounted(async () => {
  await loadSessions()
  
  // If no sessions, start a new chat
  if (sortedSessions.value.length === 0) {
    startNewChat()
  }
})
</script>

<style scoped>
.chat-interface {
  display: grid;
  grid-template-columns: 280px 1fr;
  height: calc(100vh - 100px);
  overflow: hidden;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  background-color: var(--bg-primary);
}

.chat-sidebar {
  background-color: var(--bg-tertiary);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-content {
  flex: 1;
  overflow-y: auto;
}

.chat-main {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
}

.mobile-sidebar-toggle {
  padding: 0.5rem;
  border-bottom: 1px solid var(--border-color);
}

.chat-header {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.chat-input-container {
  padding: 1rem;
  border-top: 1px solid var(--border-color);
}

.welcome-screen {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.welcome-content {
  max-width: 600px;
  text-align: center;
}

.welcome-content i {
  font-size: 5rem;
  margin-bottom: 1rem;
}

.suggestions {
  margin-top: 2rem;
}

.suggestion-title {
  margin-bottom: 1rem;
  font-weight: 500;
}

.suggestion-items {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.suggestion-btn {
  background-color: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 0.5rem 1rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.suggestion-btn:hover {
  background-color: var(--bg-tertiary);
  transform: translateY(-2px);
  box-shadow: var(--shadow-sm);
}

/* Mobile styles */
@media (max-width: 768px) {
  .chat-interface {
    grid-template-columns: 1fr;
  }
  
  .chat-sidebar {
    position: fixed;
    top: 70px;
    left: 0;
    bottom: 0;
    width: 280px;
    z-index: 1000;
    transform: translateX(0);
    transition: transform 0.3s ease;
  }
  
  .mobile-hidden {
    transform: translateX(-100%);
  }
}
</style>
