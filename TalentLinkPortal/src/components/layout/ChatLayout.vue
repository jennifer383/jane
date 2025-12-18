<template>
  <div class="app-container" :class="{ 'sidebar-collapsed': isSidebarCollapsed }">
    <transition name="fade-in" appear>
      <div class="mobile-menu-btn" @click="toggleSidebar">
        <i class="fas fa-bars"></i>
      </div>
    </transition>
    
    <transition name="slide-in-left" appear>
      <Sidebar :is-collapsed="isSidebarCollapsed" @toggle-collapse="toggleSidebar" />
    </transition>
    
    <transition name="slide-in-right" appear>
      <ChatArea />
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Sidebar from '@/components/layout/Sidebar.vue'
import ChatArea from '@/components/layout/ChatArea.vue'

const isSidebarCollapsed = ref(false)
const toggleSidebar = () => {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}
</script>

<style scoped>
.app-container {
  display: flex;
  height: 100vh;
  position: relative;
  width: 100%;
  transition: all 0.2s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  overflow: hidden;
}

.app-container.sidebar-collapsed {
  margin-left: 0 !important;
}

.mobile-menu-btn {
  display: none;
  position: absolute;
  top: 20px;
  left: 20px;
  background: var(--sidebar-bg);
  border: 1px solid var(--border-color);
  width: 40px;
  height: 40px;
  border-radius: 8px;
  z-index: 200;
  align-items: center;
  justify-content: center;
  color: var(--text-primary);
  cursor: pointer;
  transition: all 0.15s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.mobile-menu-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  background: var(--sidebar-hover);
}

.mobile-menu-btn:active {
  transform: translateY(0);
}

/* 组件进入动画 */
.fade-in-enter-active {
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.fade-in-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.slide-in-left-enter-active {
  transition: all 0.35s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.slide-in-left-enter-from {
  opacity: 0;
  transform: translateX(-100%);
}

.slide-in-right-enter-active {
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.slide-in-right-enter-from {
  opacity: 0;
  transform: translateX(50px);
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: flex;
  }
}
</style>