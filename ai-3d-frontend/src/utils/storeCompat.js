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
      'user/isLoggedIn': computed(() => !!userStore.user),
      'user/isAdmin': computed(() => userStore.isAdmin),

      // Chat getters
      'chat/sessions': chatStore.sortedSessions,
      'chat/currentSessionId': computed(() => chatStore.currentSessionId),
      'chat/messages': chatStore.currentMessages,
      'chat/isStreaming': computed(() => chatStore.isStreaming),
      'chat/streamingMessage': computed(() => chatStore.streamingContent)
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
            // Not needed in new implementation
            return null
          case 'loadSessions':
          case 'fetchSessions':
            return await chatStore.fetchSessions()
          case 'loadMessages':
          case 'fetchMessages':
            return await chatStore.fetchMessages(payload)
          case 'deleteSession':
            return await chatStore.deleteSession(payload)
          case 'deleteMessage':
            return await chatStore.deleteMessage(payload)
          case 'sendMessage':
            if (typeof payload === 'string') {
              return await chatStore.sendMessage(payload)
            } else {
              return await chatStore.sendMessage(payload.content, payload)
            }
          case 'streamMessage':
            if (typeof payload === 'string') {
              return await chatStore.sendMessage(payload)
            } else {
              return await chatStore.sendMessage(payload.content, payload)
            }
          case 'setCurrentSession':
            return chatStore.setCurrentSession(payload)
          case 'startStreaming':
            // Not needed in new implementation
            return true
          case 'finishStreaming':
            // Not needed in new implementation
            return null
          case 'appendStreamingContent':
            // Not needed in new implementation
            return null
          case 'ADD_MESSAGE':
            // Not needed in new implementation
            return null
          case 'startNewChat':
            return chatStore.startNewChat()
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
            // Not needed in new implementation
            return
          case 'SET_MESSAGES':
            // Not needed in new implementation
            return
          case 'UPDATE_TEMP_MESSAGES_SESSION_ID':
            // Not needed in new implementation
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
        isStreaming: chatStore.isStreaming,
        isNewChat: chatStore.isNewChat
      }
    }
  }
}

export default {
  useStore
}
