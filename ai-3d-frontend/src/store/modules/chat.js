import { createSession, listSessions, listMessages, deleteSession, deleteMessage, sendMessage, streamChat } from '../../api/chat'

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

  async sendMessageWithAutoSession({ commit }, message) {
    try {
      // 1. 发送消息并自动创建会话
      const response = await sendMessage(message, { first: true })
      if (response.code === 0) {
        // 2. 获取新创建的会话信息
        const sessionId = response.data.sessionId

        // 3. 刷新会话列表
        await this.dispatch('chat/fetchSessions')

        // 4. 设置当前会话
        commit('SET_CURRENT_SESSION', sessionId)

        // 5. 添加用户消息
        const userMessage = {
          id: Date.now().toString() + '-user',
          sessionId,
          role: 'user',
          content: message,
          createTime: new Date().toISOString()
        }
        commit('ADD_MESSAGE', userMessage)

        // 6. 添加AI回复
        commit('ADD_MESSAGE', response.data)

        return Promise.resolve(response.data)
      } else {
        return Promise.reject(response.message || '发送消息失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '发送消息失败')
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
      console.log('store: 开始获取消息, sessionId:', sessionId)
      const response = await listMessages(sessionId)
      console.log('store: 收到消息响应:', response)

      if (response.code === 0) {
        console.log('store: 消息数据:', response.data)
        commit('SET_MESSAGES', response.data)
        return Promise.resolve(response.data)
      } else {
        console.error('store: 获取消息失败, 错误代码:', response.code, '错误信息:', response.message)
        return Promise.reject(response.message || '获取消息列表失败')
      }
    } catch (error) {
      console.error('store: 获取消息异常:', error)
      // 将原始错误对象传递出去，以便上层组件可以获取更详细的错误信息
      return Promise.reject(error)
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

  async deleteMessage({ commit }, messageId) {
    try {
      const response = await deleteMessage(messageId)
      if (response.code === 0 && response.data) {
        commit('REMOVE_MESSAGE', messageId)
        return Promise.resolve(true)
      } else {
        return Promise.reject(response.message || '删除消息失败')
      }
    } catch (error) {
      return Promise.reject(error.message || '删除消息失败')
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

  setCurrentSession({ commit }, sessionId, options = {}) {
    const { keepMessages = false } = options || {};
    commit('SET_CURRENT_SESSION', sessionId)

    // 如果不需要保留消息，则清除临时消息
    if (!keepMessages && sessionId !== 'temp-session') {
      commit('CLEAR_TEMP_MESSAGES')
    }
  },

  startStreaming({ commit }) {
    commit('SET_IS_STREAMING', true)
    commit('SET_STREAMING_MESSAGE', '')
  },

  appendStreamingContent({ commit }, content) {
    commit('APPEND_STREAMING_CONTENT', content)
  },

  finishStreaming({ commit, state }, options = {}) {
    const { keepContent = false } = options;

    // 将流式消息添加到消息列表
    if (state.streamingMessage.trim()) {
      // 如果当前会话存在且不是首次对话模式，正常添加消息
      if (state.currentSessionId &&
          state.currentSessionId !== 'temp-session' &&
          state.messages.some(m => m.sessionId === state.currentSessionId)) {
        const message = {
          id: Date.now().toString(),
          sessionId: state.currentSessionId,
          role: 'assistant',
          content: state.streamingMessage,
          createTime: new Date().toISOString()
        }
        commit('ADD_MESSAGE', message)
        console.log('流式响应完成，添加消息到列表:', message.id)
      }
      // 如果是首次对话，消息会在ChatView中手动添加
    }

    // 如果不需要保留流式消息内容，则清空
    if (!keepContent) {
      commit('SET_STREAMING_MESSAGE', '')
    }

    // 延迟一小段时间再关闭流式状态，确保消息渲染完成
    // 增加延迟时间，确保有足够时间渲染markdown
    setTimeout(() => {
      commit('SET_IS_STREAMING', false)
      console.log('流式响应状态关闭，应该显示markdown格式化内容')
    }, 200)
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
  },

  async streamChatWithAutoSession({ commit, dispatch }, { message, callbacks }) {
    try {
      // 1. 添加用户消息到前端显示
      const userMessage = {
        id: Date.now().toString(),
        sessionId: null, // 暂时不知道会话ID
        role: 'user',
        content: message,
        createTime: new Date().toISOString()
      }
      commit('ADD_MESSAGE', userMessage)

      // 2. 开始流式响应
      commit('SET_IS_STREAMING', true)
      commit('SET_STREAMING_MESSAGE', '')

      // 3. 调用流式发送消息的API，设置first=true自动创建会话
      await streamChat(message, { first: true }, {
        onMessage: (content) => {
          commit('APPEND_STREAMING_CONTENT', content)
          callbacks.onMessage && callbacks.onMessage(content)
        },
        onDone: async () => {
          // 4. 流式响应结束后，刷新会话列表
          await dispatch('fetchSessions')

          // 5. 如果有会话，选择第一个会话
          if (state.sessions.length > 0) {
            const newSessionId = state.sessions[0].id
            commit('SET_CURRENT_SESSION', newSessionId)

            // 6. 更新用户消息的会话ID
            userMessage.sessionId = newSessionId

            // 7. 将流式消息添加到消息列表
            if (state.streamingMessage.trim()) {
              const aiMessage = {
                id: Date.now().toString(),
                sessionId: newSessionId,
                role: 'assistant',
                content: state.streamingMessage,
                createTime: new Date().toISOString()
              }
              commit('ADD_MESSAGE', aiMessage)
            }
          }

          // 8. 重置流式状态
          commit('SET_IS_STREAMING', false)
          commit('SET_STREAMING_MESSAGE', '')

          callbacks.onDone && callbacks.onDone()
        },
        onError: (error) => {
          commit('SET_IS_STREAMING', false)
          commit('SET_STREAMING_MESSAGE', '')
          callbacks.onError && callbacks.onError(error)
        }
      })
    } catch (error) {
      commit('SET_IS_STREAMING', false)
      commit('SET_STREAMING_MESSAGE', '')
      throw error
    }
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
    // 先检查是否为当前会话
    const isCurrentSession = state.currentSessionId === sessionId

    // 从会话列表中移除
    state.sessions = state.sessions.filter(s => s.id !== sessionId)

    // 如果是当前会话，且不是临时会话，则选择新的会话
    if (isCurrentSession) {
      // 如果是临时会话，不需要清空消息，因为将会加载真实会话的消息
      if (!sessionId.toString().startsWith('temp-')) {
        state.messages = []
      }

      // 如果还有其他会话，选择第一个
      if (state.sessions.length > 0) {
        state.currentSessionId = state.sessions[0].id
      } else {
        state.currentSessionId = null
      }
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
    if (sessionId) {
      // 清除指定会话的消息
      if (state.currentSessionId === sessionId) {
        state.messages = []
      }
    } else {
      // 如果没有指定会话，清除所有消息
      state.messages = []
    }
  },

  // 清除临时会话的消息
  CLEAR_TEMP_MESSAGES(state) {
    state.messages = state.messages.filter(msg => msg.sessionId !== 'temp-session')
  },

  SET_IS_STREAMING(state, isStreaming) {
    state.isStreaming = isStreaming
  },

  SET_STREAMING_MESSAGE(state, message) {
    state.streamingMessage = message
  },

  APPEND_STREAMING_CONTENT(state, content) {
    // 处理流式内容
    if (content) {
      // 将新内容追加到现有流式消息中
      state.streamingMessage += content;

      // 打印当前流式消息状态
      console.log('收到新token:', content);
      console.log('当前流式消息长度:', state.streamingMessage.length);
    }
  },

  REMOVE_MESSAGE(state, messageId) {
    state.messages = state.messages.filter(msg => msg.id !== messageId)
  },

  // 更新临时消息的会话ID
  UPDATE_TEMP_MESSAGES_SESSION_ID(state, newSessionId) {
    state.messages.forEach(msg => {
      if (msg.sessionId === 'temp-session') {
        msg.sessionId = newSessionId;
      }
    });
  }
}

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations
}
