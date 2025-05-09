/**
 * Bootstrap Modal 修复工具
 * 
 * 解决在Vue应用中使用Bootstrap模态框时可能出现的问题：
 * 1. 模态框关闭后背景遮罩没有正确移除
 * 2. 多个模态框叠加时的滚动和焦点问题
 * 3. 模态框关闭后页面无法滚动
 */

/**
 * 清理模态框状态
 * 移除可能残留的模态框背景和相关样式
 */
export function cleanupModalState() {
  // 移除可能残留的模态框背景
  const modalBackdrops = document.querySelectorAll('.modal-backdrop');
  modalBackdrops.forEach(backdrop => {
    backdrop.remove();
  });

  // 移除body上的modal-open类和内联样式
  document.body.classList.remove('modal-open');
  document.body.style.removeProperty('overflow');
  document.body.style.removeProperty('padding-right');
}

/**
 * 修复模态框滚动问题
 * @param {HTMLElement} modalElement - 模态框DOM元素
 */
export function fixModalScroll(modalElement) {
  if (!modalElement) return;
  
  // 确保模态框内容可以滚动
  const modalDialog = modalElement.querySelector('.modal-dialog');
  if (modalDialog) {
    modalDialog.style.overflowY = 'auto';
    modalDialog.style.maxHeight = 'calc(100vh - 60px)';
  }
}

/**
 * 在Vue组件卸载前清理模态框
 * 在组件的beforeUnmount生命周期钩子中调用
 */
export function cleanupModalsBeforeUnmount() {
  // 关闭所有打开的模态框
  const openModals = document.querySelectorAll('.modal.show');
  openModals.forEach(modal => {
    // 使用Bootstrap的Modal API关闭模态框
    if (window.bootstrap && window.bootstrap.Modal) {
      const modalInstance = window.bootstrap.Modal.getInstance(modal);
      if (modalInstance) {
        modalInstance.hide();
      }
    }
  });
  
  // 清理模态框状态
  cleanupModalState();
}

export default {
  cleanupModalState,
  fixModalScroll,
  cleanupModalsBeforeUnmount
};
