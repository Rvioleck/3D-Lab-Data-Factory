import { Modal } from 'bootstrap'

/**
 * 清理模态框状态
 */
export function cleanupModalState() {
  const modalBackdrops = document.getElementsByClassName('modal-backdrop')
  while (modalBackdrops.length > 0) {
    modalBackdrops[0].remove()
  }
  document.body.classList.remove('modal-open')
  document.body.style.removeProperty('padding-right')
  document.body.style.removeProperty('overflow')
}

/**
 * 创建安全的模态框实例
 */
export function createSafeModal(element, options = {}) {
  if (!element) return null
  
  const modal = new Modal(element, {
    ...options,
    backdrop: false
  })
  
  const originalHide = modal.hide
  modal.hide = function() {
    originalHide.call(this)
    setTimeout(cleanupModalState, 300)
  }
  
  return modal
}

// 默认导出
export default {
  cleanupModalState,
  createSafeModal
}