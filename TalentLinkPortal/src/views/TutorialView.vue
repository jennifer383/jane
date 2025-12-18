<template>
  <div class="tutorial-container">
    <div class="tutorial-header">
      <button class="back-btn" @click="goBack">
        <i class="fas fa-arrow-left"></i>
        返回
      </button>
      <h1 class="tutorial-title">使用教程</h1>
    </div>
    
    <div class="tutorial-content">
      <div class="tutorial-section">
        <h2>欢迎使用 JaneAI</h2>
        <p>这里是使用教程页面，您可以在这里了解如何使用我们的AI助手。</p>
      </div>
      
      <div class="tutorial-section">
        <h3>基本功能</h3>
        <ul>
          <li>智能对话：与AI进行自然语言交流</li>
          <li>文档上传：支持PDF文件上传和分析</li>
          <li>多轮对话：保持上下文连贯性</li>
        </ul>
      </div>
      
      <div class="tutorial-section">
        <h3>快速开始</h3>
        <ol>
          <li>在聊天框中输入您的问题</li>
          <li>点击发送按钮或按Enter键</li>
          <li>等待AI回复</li>
          <li>继续对话或开始新话题</li>
        </ol>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.tutorial-container {
  min-height: 100vh;
  background: var(--content-bg);
  color: var(--text-primary);
}

.tutorial-header {
  background: var(--sidebar-bg);
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  padding: 10px 16px;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--sidebar-hover);
  color: var(--primary);
}

.tutorial-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: var(--text-primary);
}

.tutorial-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 24px;
}

.tutorial-section {
  margin-bottom: 32px;
  background: var(--input-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
}

.tutorial-section h2 {
  color: var(--primary);
  font-size: 20px;
  margin-bottom: 16px;
}

.tutorial-section h3 {
  color: var(--text-primary);
  font-size: 18px;
  margin-bottom: 12px;
}

.tutorial-section p {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
}

.tutorial-section ul,
.tutorial-section ol {
  color: var(--text-secondary);
  line-height: 1.6;
  padding-left: 20px;
}

.tutorial-section li {
  margin-bottom: 8px;
}
</style>