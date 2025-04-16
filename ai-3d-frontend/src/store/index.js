import { createStore } from 'vuex'
import user from './modules/user'
import chat from './modules/chat'

export default createStore({
  modules: {
    user,
    chat
  }
})
