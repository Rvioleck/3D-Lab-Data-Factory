/**
 * 性能优化工具函数
 */

/**
 * 防抖函数 - 延迟执行函数，如果在延迟时间内再次调用则重新计时
 * @param {Function} fn 要执行的函数
 * @param {number} delay 延迟时间（毫秒）
 * @returns {Function} 防抖处理后的函数
 */
export function debounce(fn, delay = 300) {
  let timer = null;
  return function(...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
      timer = null;
    }, delay);
  };
}

/**
 * 节流函数 - 限制函数在一定时间内只能执行一次
 * @param {Function} fn 要执行的函数
 * @param {number} limit 限制时间（毫秒）
 * @returns {Function} 节流处理后的函数
 */
export function throttle(fn, limit = 300) {
  let inThrottle = false;
  return function(...args) {
    if (!inThrottle) {
      fn.apply(this, args);
      inThrottle = true;
      setTimeout(() => {
        inThrottle = false;
      }, limit);
    }
  };
}

/**
 * 使用requestAnimationFrame优化滚动事件
 * @param {Function} callback 滚动时要执行的回调函数
 * @returns {Function} 优化后的滚动处理函数
 */
export function optimizedScroll(callback) {
  let ticking = false;

  return function(event) {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        callback(event);
        ticking = false;
      });
      ticking = true;
    }
  };
}

/**
 * 延迟加载图片
 * @param {HTMLElement} imgElement 图片元素
 * @param {string} src 图片源地址
 */
export function lazyLoadImage(imgElement, src) {
  const img = new Image();
  img.src = src;
  img.onload = () => {
    imgElement.src = src;
    imgElement.classList.add('loaded');
  };
}

/**
 * 使用IntersectionObserver实现元素懒加载
 * @param {string} selector 要观察的元素选择器
 * @param {Function} callback 元素进入视口时的回调
 * @param {Object} options IntersectionObserver选项
 */
export function createIntersectionObserver(selector, callback, options = {}) {
  const defaultOptions = {
    root: null,
    rootMargin: '0px',
    threshold: 0.1
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        callback(entry.target);
        observer.unobserve(entry.target);
      }
    });
  }, { ...defaultOptions, ...options });

  document.querySelectorAll(selector).forEach(el => {
    observer.observe(el);
  });

  return observer;
}

/**
 * 优化渲染性能的CSS类
 * @returns {Object} 包含CSS类名的对象
 */
export const performanceClasses = {
  hardwareAccelerated: 'hardware-accelerated',
  willChange: 'will-change',
  lazyLoad: 'lazy-load',
  loaded: 'loaded'
};

/**
 * 检测浏览器是否支持某些现代特性
 * @returns {Object} 支持状态对象
 */
export function detectFeatureSupport() {
  return {
    intersectionObserver: 'IntersectionObserver' in window,
    requestIdleCallback: 'requestIdleCallback' in window,
    customProperties: CSS && CSS.supports && CSS.supports('--a', '0'),
    webp: document.createElement('canvas')
      .toDataURL('image/webp')
      .indexOf('data:image/webp') === 0
  };
}
