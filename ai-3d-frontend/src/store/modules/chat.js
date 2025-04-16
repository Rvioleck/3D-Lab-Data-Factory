import { createSession, listSessions, listMessages, deleteSession, sendMessage } from '../../api/chat'

const state = {
  sessions: [],
  currentSessionId: null,
  messages: [],
  isStreaming: false,
  streamingMessage: ''
}

const getters = {
  sessions: state => state.sessions,
  currentSessionId: state => state.currentSessionId,
  messages: state => state.messages,
  isStreaming: state => state.isStreaming,
  streamingMessage: state => state.streamingMessage
}

const actions = {
  async createSession({ commit }, sessionName) {
    try {
      const response = await createSession(sessionName)
      if (response.code === 0) {
        commit('ADD_SESSION', response.data)
        commit('SET_CURRENT_SESSION', response.data.id)
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '创建会话失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '创建会话失败')
    }
  },

  async fetchSessions({ commit }) {
    try {
      const response = await listSessions()
      if (response.code === 0) {
        commit('SET_SESSIONS', response.data)
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '获取会话列表失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '获取会话列表失败')
    }
  },

  async fetchMessages({ commit }, sessionId) {
    try {
      const response = await listMessages(sessionId)
      if (response.code === 0) {
        commit('SET_MESSAGES', response.data)
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '获取消息列表失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '获取消息列表失败')
    }
  },

  async deleteSession({ commit }, sessionId) {
    try {
      const response = await deleteSession(sessionId)
      if (response.code === 0) {
        commit('REMOVE_SESSION', sessionId)
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '删除会话失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '删除会话失败')
    }
  },

  // 重命名会话
  async renameSession({ commit, state }, { id, name }) {
    // 在实际项目中需要调用API更新后端
    // 这里只更新前端状态
    try {
      commit('UPDATE_SESSION_NAME', { id, name })
      return Promise.resolve(true)
    } catch (error) {
      return Promise.reject('重命名会话失败')
    }
  },

  // 清空当前会话消息
  clearMessages({ commit }, sessionId) {
    commit('CLEAR_MESSAGES', sessionId)
  },

  setCurrentSession({ commit }, sessionId) {
    commit('SET_CURRENT_SESSION', sessionId)
  },

  startStreaming({ commit }) {
    commit('SET_IS_STREAMING', true)
    commit('SET_STREAMING_MESSAGE', '')
  },

  appendStreamingContent({ commit }, content) {
    commit('APPEND_STREAMING_CONTENT', content)
  },

  finishStreaming({ commit, state }) {
    // 将流式消息添加到消息列表
    if (state.streamingMessage.trim()) {
      const message = {
        id: Date.now().toString(),
        sessionId: state.currentSessionId,
        role: 'assistant',
        content: state.streamingMessage,
        createTime: new Date().toISOString()
      }
      commit('ADD_MESSAGE', message)
    }
    commit('SET_IS_STREAMING', false)
    commit('SET_STREAMING_MESSAGE', '')
  },

  addUserMessage({ commit }, { sessionId, content }) {
    const message = {
      id: Date.now().toString(),
      sessionId,
      role: 'user',
      content,
      createTime: new Date().toISOString()
    }
    commit('ADD_MESSAGE', message)
  }
}

const mutations = {
  SET_SESSIONS(state, sessions) {
    state.sessions = sessions
  },

  ADD_SESSION(state, session) {
    state.sessions.push(session)
  },

  REMOVE_SESSION(state, sessionId) {
    state.sessions = state.sessions.filter(s => s.id !== sessionId)
    if (state.currentSessionId === sessionId) {
      state.currentSessionId = state.sessions.length > 0 ? state.sessions[0].id : null
      state.messages = []
    }
  },

  UPDATE_SESSION_NAME(state, { id, name }) {
    const session = state.sessions.find(s => s.id === id)
    if (session) {
      session.sessionName = name
    }
  },

  SET_CURRENT_SESSION(state, sessionId) {
    state.currentSessionId = sessionId
  },

  SET_MESSAGES(state, messages) {
    state.messages = messages
  },

  ADD_MESSAGE(state, message) {
    state.messages.push(message)
  },

  CLEAR_MESSAGES(state, sessionId) {
    if (state.currentSessionId === sessionId) {
      state.messages = []
    }
  },

  SET_IS_STREAMING(state, isStreaming) {
    state.isStreaming = isStreaming
  },

  SET_STREAMING_MESSAGE(state, message) {
    state.streamingMessage = message
  },

  APPEND_STREAMING_CONTENT(state, content) {
    state.streamingMessage += content
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
