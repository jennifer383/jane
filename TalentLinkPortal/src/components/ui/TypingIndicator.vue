<template>
  <div class="typing-indicator-wrapper">
    <div class="typing-indicator">
      <div class="typing-content">
        <div class="typing-text">AI正在思考中</div>
        <div class="typing-dots">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// 无需额外逻辑
</script>

<style scoped>
.typing-indicator-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.typing-indicator {
  width: 80%;
  max-width: 900px;
  display: flex;
  align-items: flex-start;
  padding: 16px 0;
}



.typing-content {
  background: var(--input-bg);
  border-radius: 12px;
  padding: 16px 20px;
  box-shadow: var(--shadow);
  border: 1px solid var(--border-color);
  position: relative;
  overflow: hidden;
}

.typing-text {
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 8px;
  font-style: italic;
}

.typing-dots {
  display: flex;
  gap: 4px;
  align-items: center;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary);
  animation: typing 0.7s infinite ease-in-out;
}

.dot:nth-child(1) {
  animation-delay: 0s;
}

.dot:nth-child(2) {
  animation-delay: 0.1s;
}

.dot:nth-child(3) {
  animation-delay: 0.2s;
}

/* 添加闪烁的背景效果 */
.typing-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(99, 102, 241, 0.1),
    transparent
  );
  animation: shimmer 1s infinite;
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.4;
  }
  30% {
    transform: translateY(-10px);
    opacity: 1;
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .typing-indicator {
    width: 95%;
  }
  
  .typing-content {
    padding: 12px 16px;
  }
}
</style>