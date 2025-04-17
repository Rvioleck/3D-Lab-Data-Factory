/**
 * 图片懒加载指令
 * 使用方式：v-lazy="图片URL"
 */

export default {
  mounted(el, binding) {
    function loadImage() {
      const imageElement = el.tagName === 'IMG' ? el : el.querySelector('img');
      
      if (!imageElement) {
        return;
      }
      
      // 保存原始图片URL
      const imageUrl = binding.value;
      
      // 设置加载中的占位图
      if (!imageElement.src) {
        imageElement.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2VlZSIvPjx0ZXh0IHRleHQtYW5jaG9yPSJtaWRkbGUiIHg9IjEwMCIgeT0iMTAwIiBzdHlsZT0iZmlsbDojYWFhO2ZvbnQtd2VpZ2h0OmJvbGQ7Zm9udC1zaXplOjEzcHg7Zm9udC1mYW1pbHk6QXJpYWwsSGVsdmV0aWNhLHNhbnMtc2VyaWY7ZG9taW5hbnQtYmFzZWxpbmU6bWlkZGxlIj7liqDovb3kuK0uLi48L3RleHQ+PC9zdmc+';
      }
      
      // 创建新图片对象预加载
      const img = new Image();
      img.src = imageUrl;
      
      img.onload = () => {
        // 图片加载完成后替换src
        imageElement.src = imageUrl;
        el.classList.add('loaded');
        el.classList.remove('loading');
      };
      
      img.onerror = () => {
        // 加载失败时显示错误占位图
        imageElement.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2VlZSIvPjx0ZXh0IHRleHQtYW5jaG9yPSJtaWRkbGUiIHg9IjEwMCIgeT0iMTAwIiBzdHlsZT0iZmlsbDojYWFhO2ZvbnQtd2VpZ2h0OmJvbGQ7Zm9udC1zaXplOjEzcHg7Zm9udC1mYW1pbHk6QXJpYWwsSGVsdmV0aWNhLHNhbnMtc2VyaWY7ZG9taW5hbnQtYmFzZWxpbmU6bWlkZGxlIj7liqDovb3lpLHotKUuLi48L3RleHQ+PC9zdmc+';
        el.classList.add('error');
        el.classList.remove('loading');
      };
      
      // 添加加载中的类
      el.classList.add('loading');
    }
    
    function handleIntersect(entries, observer) {
      entries.forEach(entry => {
        // 当元素进入视口时加载图片
        if (entry.isIntersecting) {
          loadImage();
          // 加载后取消观察
          observer.unobserve(el);
        }
      });
    }
    
    // 使用IntersectionObserver实现懒加载
    if ('IntersectionObserver' in window) {
      const observer = new IntersectionObserver(handleIntersect, {
        root: null, // 默认使用视口
        rootMargin: '0px', // 视口边界
        threshold: 0.01 // 元素可见1%时触发
      });
      
      observer.observe(el);
    } else {
      // 降级处理：不支持IntersectionObserver的浏览器直接加载
      loadImage();
    }
  }
};
