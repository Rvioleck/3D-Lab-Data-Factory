<template>
  <div class="chat-view">
    <!-- 非管理员用户显示无权限提示 -->
    <NoPermission
      v-if="!isAdmin"
      title="权限不足"
      message="很抱歉，聊天功能当前仅开放给管理员使用。"
      backLink="/home"
      backText="返回首页"
    />

    <!-- 管理员用户显示聊天界面 -->
    <ChatInterface v-else />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import ChatInterface from '../components/chat/ChatInterface.vue'
import NoPermission from '../components/shared/NoPermission.vue'

const userStore = useUserStore()

// Check if user is admin
const isAdmin = computed(() => userStore.isAdmin)
</script>

<style scoped>
.chat-view {
  height: calc(100vh - 100px);
}
</style>
