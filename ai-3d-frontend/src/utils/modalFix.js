/**
 * 模态框修复工具
 *
 * 最简化版本，完全禁用模态框背景遮罩层
 */

/**
 * 清除模态框背景和相关状态
 */
export function cleanupModals() {
  // 移除所有模态框背景
  const backdrops = document.querySelectorAll('.modal-backdrop');
  backdrops.forEach(backdrop => {
    backdrop.parentNode?.removeChild(backdrop);
  });

  // 移除body上的modal-open类和内联样式
  document.body.classList.remove('modal-open');
  document.body.style.removeProperty('overflow');
  document.body.style.removeProperty('padding-right');
}

/**
 * 初始化模态框修复
 */
export function setupModalFix() {
  // 初始清理
  cleanupModals();

  // 路由变化时清理
  window.addEventListener('popstate', cleanupModals);

  // 在全局对象上添加修复函数
  window.fixModals = cleanupModals;
}

export default {
  cleanupModals,
  setupModalFix
};
