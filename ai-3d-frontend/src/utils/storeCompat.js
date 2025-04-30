/**
 * Store compatibility layer to help migrate from Vuex to Pinia
 * This utility provides a compatibility layer for components that still use Vuex
 * while we transition to Pinia.
 */

import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'
import { computed } from 'vue'

/**
 * Create a Vuex-compatible store interface using Pinia stores
 * @returns {Object} Vuex-compatible store interface
 */
export const useStore = () => {
  // Get Pinia stores
  const userStore = useUserStore()
  const chatStore = useChatStore()

  // Create Vuex-compatible store interface
  return {
    // Vuex-compatible getters
    getters: {
      // User getters
      'user/currentUser': userStore.user,
      'user/isLoggedIn': computed(() => !!userStore.user.value),
      'user/isAdmin': computed(() => userStore.user.value?.userRole === 'admin'),

      // Chat getters
      'chat/sessions': chatStore.sortedSessions,
      'chat/currentSessionId': chatStore.currentSessionId,
      'chat/messages': chatStore.sortedMessages,
      'chat/isStreaming': chatStore.streaming,
      'chat/streamingMessage': chatStore.streamingMessage?.value || ''
    },

    // Vuex-compatible dispatch
    dispatch: async (action, payload) => {
      const [namespace, method] = action.split('/')

      // User actions
      if (namespace === 'user') {
        switch (method) {
          case 'login':
            return await userStore.login(payload)
          case 'register':
            return await userStore.register(payload)
          case 'fetchCurrentUser':
            return await userStore.fetchCurrentUser()
          case 'logout':
            return await userStore.logout()
          case 'updateProfile':
            return await userStore.updateProfile(payload)
        }
      }

      // Chat actions
      if (namespace === 'chat') {
        switch (method) {
          case 'createSession':
            return await chatStore.createSession(payload)
          case 'loadSessions':
            return await chatStore.loadSessions()
          case 'loadMessages':
            return await chatStore.loadMessages(payload)
          case 'deleteSession':
            return await chatStore.deleteSession(payload)
          case 'deleteMessage':
            return await chatStore.deleteMessage(payload)
          case 'sendMessage':
            return await chatStore.sendMessage(payload)
          case 'streamMessage':
            return await chatStore.streamMessage(payload)
          case 'setCurrentSession':
            return chatStore.setCurrentSession(payload)
          case 'fetchSessions':
            return await chatStore.loadSessions()
          case 'fetchMessages':
            return await chatStore.loadMessages(payload)
          case 'startStreaming':
            return chatStore.streaming = true
          case 'finishStreaming':
            return chatStore.streaming = false
          case 'appendStreamingContent':
            // 确保streamingMessage是一个ref
            if (chatStore.streamingMessage && typeof chatStore.streamingMessage === 'object') {
              if (chatStore.streamingMessage.value === undefined) {
                chatStore.streamingMessage.value = ''
              }
              chatStore.streamingMessage.value += payload
            } else {
              console.error('streamingMessage不是一个ref对象:', chatStore.streamingMessage)
            }
            return null
          case 'ADD_MESSAGE':
            if (!chatStore.messages.value['temp-session']) {
              chatStore.messages.value['temp-session'] = []
            }
            chatStore.messages.value['temp-session'].push(payload)
            return null
        }
      }

      // Reconstruction actions (fallback to direct store access)
      if (namespace === 'reconstruction') {
        console.warn('Reconstruction store not yet migrated to Pinia')
        // For now, we'll just log a warning and return null
        return null
      }

      console.warn(`Action ${action} not implemented in compatibility layer`)
      return null
    },

    // Vuex-compatible commit
    commit: (mutation, payload) => {
      const [namespace, method] = mutation.split('/')

      // Chat mutations
      if (namespace === 'chat') {
        switch (method) {
          case 'ADD_MESSAGE':
            // 确保messages是一个对象
            if (typeof chatStore.messages.value !== 'object' || chatStore.messages.value === null) {
              chatStore.messages.value = {}
            }
            // 确保temp-session是一个数组
            if (!chatStore.messages.value['temp-session']) {
              chatStore.messages.value['temp-session'] = []
            }
            chatStore.messages.value['temp-session'].push(payload)
            return
          case 'SET_MESSAGES':
            // 确保messages是一个对象
            if (typeof chatStore.messages.value !== 'object' || chatStore.messages.value === null) {
              chatStore.messages.value = {}
            }
            if (Array.isArray(payload)) {
              chatStore.messages.value['temp-session'] = payload
            }
            return
          case 'UPDATE_TEMP_MESSAGES_SESSION_ID':
            // 确保messages是一个对象
            if (typeof chatStore.messages.value !== 'object' || chatStore.messages.value === null) {
              chatStore.messages.value = {}
              return
            }
            // 更新临时消息的会话ID
            if (chatStore.messages.value['temp-session'] && chatStore.messages.value['temp-session'].length > 0) {
              const messages = chatStore.messages.value['temp-session']
              chatStore.messages.value[payload] = messages
              delete chatStore.messages.value['temp-session']
            }
            return
        }
      }

      console.warn(`Mutation ${mutation} called through compatibility layer. Please migrate to direct Pinia store access.`)
    },

    // Vuex-compatible state
    state: {
      user: {
        currentUser: userStore.user,
        isLoggedIn: userStore.isLoggedIn
      },
      chat: {
        sessions: chatStore.sessions,
        currentSessionId: chatStore.currentSessionId,
        messages: chatStore.messages,
        isStreaming: chatStore.streaming
      }
    }
  }
}

export default {
  useStore
}
