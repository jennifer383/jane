<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="getTransitionName(route)" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<script setup>
import { onMounted } from 'vue'
import { useChatStore } from '@/stores/chatStore'

const chatStore = useChatStore()

// 初始化主题
onMounted(() => {
  chatStore.setTheme(chatStore.currentTheme)
})

// 根据路由决定过渡动画
const getTransitionName = (route) => {
  if (route.path === '/login') {
    return 'slide-up'
  } else if (route.path === '/') {
    return 'slide-down'
  }
  return 'fade'
}
</script>

<style>
/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: all 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* 向上滑动动画（登录页面） */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.25s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px) scale(0.95);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px) scale(1.05);
}

/* 向下滑动动画（主界面） */
.slide-down-enter-active,
.slide-down-leave-active {
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.slide-down-enter-from {
  opacity: 0;
  transform: translateY(-30px) scale(0.95);
}

.slide-down-leave-to {
  opacity: 0;
  transform: translateY(30px) scale(1.05);
}

/* 全局样式 */
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
  overflow: hidden;
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>