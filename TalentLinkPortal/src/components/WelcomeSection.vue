<template>
  <div class="welcome-section">
    <h1 class="welcome-title">欢迎使用 JaneAI</h1>
    <p class="welcome-subtitle">我是您的AI助手，可以帮助您解答问题、生成内容、分析数据等。请随时向我提问，我会尽力提供准确、有用的信息。</p>
    
    <div class="features-grid">
      <div class="feature-card">
        <div class="feature-icon">
          <i class="fas fa-lightbulb"></i>
        </div>
        <h3 class="feature-title">智能问答</h3>
        <p class="feature-desc">解答各种领域的问题，提供专业、准确的答案</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">
          <i class="fas fa-code"></i>
        </div>
        <h3 class="feature-title">代码生成</h3>
        <p class="feature-desc">支持多种编程语言，生成高质量代码和调试建议</p>
      </div>
      
      <div class="feature-card">
        <div class="feature-icon">
          <i class="fas fa-file-alt"></i>
        </div>
        <h3 class="feature-title">内容创作</h3>
        <p class="feature-desc">撰写文章、报告、邮件等各类文本内容</p>
      </div>
    </div>
  </div>
</template>

<script setup>
// 移除了点击事件处理，功能卡片现在仅作为展示用途
</script>

<style scoped>
/* 样式保持不变 */
  .welcome-section {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    height: 100%;
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .welcome-title {
    font-size: 32px;
    font-weight: 700;
    margin-bottom: 20px;
    background: linear-gradient(90deg, var(--primary), #a78bfa);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
  }
  
  .welcome-subtitle {
    font-size: 18px;
    color: var(--text-secondary);
    margin-bottom: 40px;
    max-width: 600px;
    line-height: 1.6;
  }
  
  .features-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 20px;
    margin-top: 30px;
    width: 100%;
  }
  
  .feature-card {
    background: var(--input-bg);
    border: 1px solid var(--border-color);
    border-radius: 16px;
    padding: 24px;
    text-align: center;
    transition: all 0.15s;
  }
  
  .feature-icon {
    font-size: 32px;
    margin-bottom: 16px;
    color: var(--primary);
  }
  
  .feature-title {
    font-size: 18px;
    font-weight: 600;
    margin-bottom: 12px;
  }
  
  .feature-desc {
    font-size: 14px;
    color: var(--text-secondary);
    line-height: 1.5;
  }
  
  @media (max-width: 768px) {
    .features-grid {
      grid-template-columns: 1fr;
    }
  }
  </style>