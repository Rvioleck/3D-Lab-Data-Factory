/**
 * 模态框辅助工具
 * 提供更现代的模态框管理方法，替代旧的modalFix.js
 */

/**
 * 清理Bootstrap模态框状态
 * 用于修复模态框关闭后可能出现的问题
 */
export function cleanupModalState() {
  // 移除body上的modal-open类和内联样式
  document.body.classList.remove('modal-open');
  document.body.style.removeProperty('overflow');
  document.body.style.removeProperty('padding-right');
  
  // 移除所有模态框背景
  const backdrops = document.querySelectorAll('.modal-backdrop');
  backdrops.forEach(backdrop => {
    backdrop.parentNode?.removeChild(backdrop);
  });
}

/**
 * 创建模态框实例并添加清理功能
 * @param {HTMLElement} element - 模态框DOM元素
 * @param {Object} options - Bootstrap Modal选项
 * @returns {Object} 增强的模态框实例
 */
export function createSafeModal(element, options = {}) {
  if (!element) return null;
  
  // 导入Bootstrap Modal
  const { Modal } = require('bootstrap');
  
  // 创建模态框实例
  const modal = new Modal(element, options);
  
  // 增强hide方法，确保在隐藏后清理状态
  const originalHide = modal.hide;
  modal.hide = function() {
    originalHide.call(this);
    setTimeout(cleanupModalState, 300); // 等待过渡动画完成
  };
  
  return modal;
}

export default {
  cleanupModalState,
  createSafeModal
};
