<template>
    <div class="theme-selector">
      <div class="theme-btn" @click="toggleThemeOptions">
        <i class="fas fa-palette"></i>
        <span>主题</span>
      </div>
      <div class="theme-options" v-show="showThemeOptions">
        <div 
          class="theme-option" 
          v-for="theme in themes" 
          :key="theme.id"
          @click="setTheme(theme.id)"
        >
          <div :class="`theme-color theme-${theme.id}-color`"></div>
          <span>{{ theme.name }}</span>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from 'vue'
  import { useChatStore } from '@/stores/chatStore'
  
  const chatStore = useChatStore()
  const showThemeOptions = ref(false)
  
  const themes = [
    { id: 'dark', name: '深色主题' },
    { id: 'light', name: '浅色主题' },
    { id: 'blue', name: '灰色主题' },
    { id: 'green', name: '粉色主题' }
  ]
  
  function toggleThemeOptions() {
    showThemeOptions.value = !showThemeOptions.value
  }
  
  function setTheme(theme) {
    chatStore.setTheme(theme)
    showThemeOptions.value = false
  }
  </script>
  
  <style scoped>
  .theme-selector {
    position: relative;
  }
  
  .theme-btn {
    background: var(--input-bg);
    border: 1px solid var(--border-color);
    border-radius: 20px;
    padding: 8px 16px;
    font-size: 14px;
    color: var(--text-primary);
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.15s;
  }
  
  .theme-btn:hover {
    border-color: var(--primary);
  }
  
  .theme-options {
    position: absolute;
    top: 110%;
    left: 0;
    background: var(--input-bg);
    border: 1px solid var(--border-color);
    border-radius: 12px;
    padding: 12px;
    width: 180px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    z-index: 1000;
    box-shadow: var(--shadow);
  }
  
  .theme-option {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 8px 12px;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.1s;
  }
  
  .theme-option:hover {
    background: var(--sidebar-hover);
  }
  
  .theme-color {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    border: 2px solid var(--border-color);
  }
  
  .theme-dark-color { background: linear-gradient(135deg, #6366f1, #1e1f25); }
  .theme-light-color { background: linear-gradient(135deg, #4f46e5, #f9fafb); }
  .theme-blue-color { background: linear-gradient(135deg,  #6b7280, #f3f4f6); }
  .theme-green-color { background: linear-gradient(135deg, #e8d6e4, #f9a8d4); }
  </style>