<template>
  <button
    class="modal-emergency-fix"
    @click="fixModals"
    title="点击修复模态框问题"
  >
    修复灰屏
  </button>
</template>

<script>
import { cleanupModals } from '@/utils/modalFix'

export default {
  name: 'ModalEmergencyFix',
  setup() {
    // 修复模态框问题
    const fixModals = () => {
      cleanupModals()
    }

    return {
      fixModals
    }
  }
}
</script>

<style scoped>
.modal-emergency-fix {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  background: #dc3545;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 5px;
  cursor: pointer;
  font-weight: bold;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.modal-emergency-fix:hover {
  background: #c82333;
  transform: scale(1.05);
}

/* 默认隐藏按钮 */
.modal-emergency-fix {
  display: none;
}

/* 当有模态框背景但没有可见模态框时显示按钮 */
:global(body.modal-open:not(:has(.modal.show))) .modal-emergency-fix {
  display: block;
}
</style>
