<template>
  <div class="model-selector-container">
    <div class="model-trigger" @click="toggleDropdown">
      <span class="model-label">当前模型：</span>
      <div class="model-tag">{{ selectedModel }}</div>
      <i class="fas fa-chevron-down" :class="{ 'rotate': isOpen }"></i>
    </div>
    
    <transition name="dropdown">
      <div class="model-dropdown" v-if="isOpen">
        <div class="dropdown-header">
          <h3>选择模型</h3>
          <div class="search-box">
            <i class="fas fa-search"></i>
            <input 
              type="text" 
              placeholder="搜索模型..." 
              v-model="searchQuery"
              @input="filterModels"
            />
          </div>
        </div>
        
        <div class="model-list">
          <div 
            v-for="model in filteredModels" 
            :key="model.value"
            class="model-item"
            :class="{ 'selected': model.value === selectedModel }"
            @click="selectModel(model)"
          >
            <div class="model-info">
              <div class="model-name">{{ model.label }}</div>
              <div class="model-desc">{{ model.description }}</div>
            </div>
            <i v-if="model.value === selectedModel" class="fas fa-check"></i>
          </div>
          
          <div v-if="filteredModels.length === 0" class="no-results">
            <i class="fas fa-search"></i>
            <span>未找到匹配的模型</span>
          </div>
        </div>
        
        <div class="dropdown-footer">
          <button type="button" class="cancel-btn" @click="closeDropdown">取消</button>
        </div>
      </div>
    </transition>
    
    <!-- 点击外部关闭下拉菜单 -->
    <div 
      v-if="isOpen" 
      class="click-outside" 
      @click="closeDropdown"
    ></div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'

// 定义组件属性
const props = defineProps({
  modelValue: {
    type: String,
    required: true
  },
  availableModels: {
    type: Array,
    default: () => []
  }
})

// 定义事件
const emit = defineEmits(['update:modelValue', 'modelChange'])

// 内部状态
const isOpen = ref(false)
const searchQuery = ref('')
const selectedModel = ref(props.modelValue)

// 监听外部模型变化
watch(() => props.modelValue, (newValue) => {
  selectedModel.value = newValue
})

// 模型列表
const models = ref([
  {
    value: 'qwen-turbo',
    label: 'DeepSeek-V3.2 (默认)',
    description: '高性能中文AI模型，适合各种对话场景'
  },
  // {
  //   value: 'gpt-4-turbo',
  //   label: 'GPT-4 Turbo',
  //   description: 'OpenAI最新一代模型，强大的多模态能力'
  // },
  // {
  //   value: 'claude-3-opus',
  //   label: 'Claude 3 Opus',
  //   description: 'Anthropic最新模型，擅长处理长文本'
  // },
  // {
    // value: 'gemini-pro',
    // label: 'Gemini Pro',
    // description: 'Google的AI模型，强大的推理能力'
  // },
  {
    value: 'llama3.2:3b',
    label: 'llama3.2 3b',
    description: 'Meta的开源大模型，适合多种应用场景'
  },
  {
    value: 'deepseek-chat',
    label: 'DeepSeek Chat',
    description: '专注于对话的AI模型'
  },
  {
    value: 'kimi-k2-thinking',
    label: 'Kimi K2 Thinking',
    description: '字节跳动的AI模型，擅长创造性思维'
  }
])

// 过滤后的模型列表
const filteredModels = computed(() => {
  if (!searchQuery.value.trim()) {
    return models.value
  }
  
  const query = searchQuery.value.toLowerCase()
  return models.value.filter(model => 
    model.label.toLowerCase().includes(query) || 
    model.description.toLowerCase().includes(query) ||
    model.value.toLowerCase().includes(query)
  )
})

// 切换下拉菜单
const toggleDropdown = () => {
  isOpen.value = !isOpen.value
}

// 关闭下拉菜单
const closeDropdown = () => {
  isOpen.value = false
  searchQuery.value = ''
}

// 选择模型
const selectModel = (model) => {
  selectedModel.value = model.value
  emit('update:modelValue', model.value)
  emit('modelChange', model.value)
  closeDropdown()
}

// 点击外部关闭
const handleClickOutside = (event) => {
  const dropdown = document.querySelector('.model-dropdown')
  const trigger = document.querySelector('.model-trigger')
  
  if (dropdown && trigger && !dropdown.contains(event.target) && !trigger.contains(event.target)) {
    closeDropdown()
  }
}

// 键盘事件处理
const handleKeydown = (event) => {
  if (event.key === 'Escape') {
    closeDropdown()
  }
}

// 生命周期钩子
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.model-selector-container {
  position: relative;
  display: inline-block;
  z-index: 100;
}

.model-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  background: var(--input-bg);
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;
}

.model-trigger:hover {
  background: var(--sidebar-hover);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.model-label {
  color: var(--text-secondary);
}

.model-tag {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.model-trigger:hover .model-tag {
  transform: scale(1.05);
}

.model-trigger i {
  color: var(--text-secondary);
  transition: all 0.2s ease;
}

.model-trigger i.rotate {
  transform: rotate(180deg);
}

/* 下拉菜单 */
.model-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  margin-top: 8px;
  width: 320px;
  background: var(--content-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 1000;
}

.dropdown-header {
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  background: var(--content-bg);
}

.dropdown-header h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box i {
  position: absolute;
  left: 12px;
  color: var(--text-tertiary);
  font-size: 14px;
}

.search-box input {
  width: 100%;
  padding: 10px 16px 10px 36px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--input-bg);
  color: var(--text-primary);
  font-size: 14px;
  outline: none;
  transition: all 0.2s ease;
}

.search-box input:focus {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.search-box input::placeholder {
  color: var(--text-tertiary);
}

.model-list {
  max-height: 300px;
  overflow-y: auto;
  padding: 8px 0;
}

.model-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
  margin: 0 8px;
}

.model-item:hover {
  background: var(--sidebar-hover);
  transform: translateX(4px);
}

.model-item.selected {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(139, 92, 246, 0.1));
  border-left: 3px solid var(--primary);
}

.model-info {
  flex: 1;
  overflow: hidden;
}

.model-name {
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
  font-size: 14px;
}

.model-desc {
  font-size: 12px;
  color: var(--text-tertiary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.model-item i {
  color: var(--primary);
  font-size: 14px;
  opacity: 0;
  transition: all 0.2s ease;
}

.model-item.selected i {
  opacity: 1;
}

.no-results {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: var(--text-tertiary);
  gap: 8px;
}

.no-results i {
  font-size: 24px;
}

.dropdown-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: flex-end;
  background: var(--content-bg);
}

.cancel-btn {
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--input-bg);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  background: var(--sidebar-hover);
  border-color: var(--primary);
}

/* 点击外部遮罩 */
.click-outside {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

/* 过渡动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}

/* 滚动条样式 */
.model-list::-webkit-scrollbar {
  width: 6px;
}

.model-list::-webkit-scrollbar-track {
  background: transparent;
}

.model-list::-webkit-scrollbar-thumb {
  background: var(--border-color);
  border-radius: 3px;
}

.model-list::-webkit-scrollbar-thumb:hover {
  background: var(--text-tertiary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .model-dropdown {
    width: 280px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>