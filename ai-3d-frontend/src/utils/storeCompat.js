/**
 * Store compatibility layer to help migrate from Vuex to Pinia
 * This utility provides a compatibility layer for components that still use Vuex
 * while we transition to Pinia.
 */

import { useUserStore } from '@/stores/user'
import { useChatStore } from '@/stores/chat'

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
      'user/isLoggedIn': userStore.isLoggedIn,
      'user/isAdmin': userStore.isAdmin,

      // Chat getters
      'chat/sessions': chatStore.sortedSessions,
      'chat/currentSessionId': chatStore.currentSessionId,
      'chat/messages': chatStore.sortedMessages,
      'chat/isStreaming': chatStore.streaming
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
            return await chatStore.setCurrentSession(payload)
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
      console.warn(`Mutation ${mutation} called through compatibility layer. Please migrate to direct Pinia store access.`)
      // We don't implement mutations as they should be replaced with direct store access
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
