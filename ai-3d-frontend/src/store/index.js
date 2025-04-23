import { createStore } from 'vuex'
import user from './modules/user'
import chat from './modules/chat'
import reconstruction from './modules/reconstruction'

export default createStore({
  modules: {
    user,
    chat,
    reconstruction
  }
})
