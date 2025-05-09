import ChatInterface from './ChatInterface.vue'
import Message from './Message.vue'
import MessageInput from './MessageInput.vue'
import MessageList from './MessageList.vue'
import SessionItem from './SessionItem.vue'
import SessionList from './SessionList.vue'
import StreamingResponse from './StreamingResponse.vue'

// Export with both original and new names for backward compatibility
export {
  ChatInterface,
  Message,
  Message as ChatMessage,
  MessageInput,
  MessageList,
  SessionItem,
  SessionItem as ChatSession,
  SessionList,
  StreamingResponse
}
