<template>
  <div
      class="ai-card"
      :style="{
        left: cardPosition.x + 'px',
        bottom: cardPosition.y + 'px'
      }"
      @mousedown="startDragging"
  >
    <!-- 背景图 -->
    <div class="background-image"></div>

    <!-- 标题 -->
    <div class="card-title">
      <p class="title-text">AI助手</p>
    </div>

    <!-- AI图标 -->
    <div class="ai-icon">
      <span class="icon-text">🤖</span>
    </div>

    <!-- 状态指示器 -->
    <div class="status-indicator">
      <div class="status-light" :class="{ 'connected': isConnected }"></div>
      <span class="status-text">{{ isConnected ? '已连接' : '未连接' }}</span>
    </div>
  </div>

  <!-- 对话窗口 -->
  <div v-if="showChatWindow" class="chat-window">
    <div class="chat-header">
      <h3>AI助手对话</h3>
      <button class="close-button" @click="closeChatWindow">×</button>
    </div>

    <div class="chat-messages" ref="chatMessages">
      <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.type]"
      >
        <div class="message-content">
          <div class="message-text">{{ message.content }}</div>
          <div class="message-time">{{ formatTime(message.timestamp) }}</div>
        </div>
      </div>
      <!-- 流式输出时显示的正在输入指示器 -->
      <div v-if="isReceivingStream" class="message ai">
        <div class="message-content">
          <div class="message-text">
            <span class="typing-indicator">
              <span></span>
              <span></span>
              <span></span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input">
      <input
          v-model="inputMessage"
          type="text"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          :disabled="!isConnected || isReceivingStream"
      />
      <button @click="sendMessage" :disabled="!isConnected || !inputMessage.trim() || isReceivingStream">发送</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { WebSocketConfig } from '@/config/websocket.config.ts'

const cardPosition = ref({ x: window.innerWidth - 190, y: 20}) // 与音乐播放器错开位置
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })
const showChatWindow = ref(false)
const isConnected = ref(false)
const websocket = ref(null)
const inputMessage = ref('')
const messages = ref([])
const chatMessages = ref(null)
const conversationId = ref('') // 会话ID
const isReceivingStream = ref(false) // 是否正在接收流式数据
const currentStreamingMessage = ref('') // 当前正在流式接收的消息

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN', { hour12: false })
}

// 拖拽开始
const startDragging = (e) => {
  isDragging.value = true
  dragOffset.value = {
    x: e.clientX - cardPosition.value.x,
    y: window.innerHeight - e.clientY - cardPosition.value.y,
  }
  window.addEventListener("mousemove", onDragging)
  window.addEventListener("mouseup", stopDragging)
}

// 拖拽中
const onDragging = (e) => {
  if (isDragging.value) {
    const newLeft = e.clientX - dragOffset.value.x
    const newBottom = window.innerHeight - e.clientY - dragOffset.value.y

    // 限制不能拖出可视区域
    const safeLeft = Math.max(0, Math.min(newLeft, window.innerWidth - 100))
    const safeBottom = Math.max(0, Math.min(newBottom, window.innerHeight - 100))

    cardPosition.value = {
      x: safeLeft,
      y: safeBottom,
    }
  }
}

// 拖拽结束
const stopDragging = () => {
  isDragging.value = false
  window.removeEventListener("mousemove", onDragging)
  window.removeEventListener("mouseup", stopDragging)
}

// 打开对话窗口
const openChatWindow = () => {
  showChatWindow.value = true
  nextTick(() => {
    scrollToBottom()
  })
}

// 关闭对话窗口
const closeChatWindow = () => {
  showChatWindow.value = false
}

// 滚动到底部
const scrollToBottom = () => {
  if (chatMessages.value) {
    chatMessages.value.scrollTop = chatMessages.value.scrollHeight
  }
}

// 连接WebSocket
const connectWebSocket = () => {
  // 生成会话ID（这里简单使用时间戳，实际项目中可以使用用户ID等）
  conversationId.value = 'conv_' + Date.now()

  // 使用配置文件中的WebSocket路径
  const wsUrl = WebSocketConfig.getAiWebSocketUrl()

  try {
    websocket.value = new WebSocket(wsUrl)

    websocket.value.onopen = () => {
      isConnected.value = true
      // 添加系统消息
      messages.value.push({
        type: 'system',
        content: '已连接到AI助手',
        timestamp: Date.now()
      })
      scrollToBottom()
    }

    websocket.value.onmessage = (event) => {
      const data = JSON.parse(event.data)

      if (data.type === 'stream') {
        // 处理流式消息
        if (!isReceivingStream.value) {
          isReceivingStream.value = true
          currentStreamingMessage.value = ''

          // 添加一个新的AI消息
          messages.value.push({
            type: 'ai',
            content: '',
            timestamp: Date.now(),
            streaming: true
          })
        }

        // 更新当前流式消息内容
        currentStreamingMessage.value += data.message
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.streaming) {
          lastMessage.content = currentStreamingMessage.value
        }

        scrollToBottom()
      } else if (data.type === 'done') {
        // 流式传输完成
        isReceivingStream.value = false
        currentStreamingMessage.value = ''
        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.streaming) {
          delete lastMessage.streaming
        }
        scrollToBottom()
      } else if (data.type === 'complete') {
        // 处理完整消息
        messages.value.push({
          type: 'ai',
          content: data.message,
          timestamp: Date.now()
        })
        scrollToBottom()
      } else if (data.type === 'error') {
        // 处理错误消息
        isReceivingStream.value = false
        currentStreamingMessage.value = ''
        messages.value.push({
          type: 'system',
          content: data.message,
          timestamp: Date.now()
        })
        scrollToBottom()
      } else {
        // 兼容旧格式
        messages.value.push({
          type: 'ai',
          content: data.message,
          timestamp: Date.now()
        })
        scrollToBottom()
      }
    }

    websocket.value.onclose = () => {
      isConnected.value = false
      isReceivingStream.value = false
      currentStreamingMessage.value = ''
      messages.value.push({
        type: 'system',
        content: '与AI助手的连接已断开',
        timestamp: Date.now()
      })
      scrollToBottom()
    }

    websocket.value.onerror = (error) => {
      console.error('WebSocket错误:', error)
      isReceivingStream.value = false
      currentStreamingMessage.value = ''
      messages.value.push({
        type: 'system',
        content: '连接发生错误: ' + error.message,
        timestamp: Date.now()
      })
      scrollToBottom()
    }
  } catch (error) {
    console.error('连接WebSocket失败:', error)
    isConnected.value = false
    isReceivingStream.value = false
    currentStreamingMessage.value = ''
    messages.value.push({
      type: 'system',
      content: '连接失败: ' + error.message,
      timestamp: Date.now()
    })
  }
}

// 发送消息
const sendMessage = () => {
  if (!inputMessage.value.trim() || !isConnected.value) return

  const message = inputMessage.value.trim()

  // 添加用户消息到界面
  messages.value.push({
    type: 'user',
    content: message,
    timestamp: Date.now()
  })

  // 发送消息到服务器（启用流式传输）
  if (websocket.value && websocket.value.readyState === WebSocket.OPEN) {
    websocket.value.send(JSON.stringify({
      conversationId: conversationId.value,
      message: message,
      stream: true // 启用流式传输
    }))
  }

  // 清空输入框
  inputMessage.value = ''

  scrollToBottom()
}

// 初始化
onMounted(() => {
  // 给卡片添加点击事件监听器
  const cardElement = document.querySelector('.ai-card')
  if (cardElement) {
    cardElement.addEventListener('click', openChatWindow)
  }

  // 连接WebSocket
  connectWebSocket()
})

// 清理
onUnmounted(() => {
  // 移除点击事件监听器
  const cardElement = document.querySelector('.ai-card')
  if (cardElement) {
    cardElement.removeEventListener('click', openChatWindow)
  }

  // 关闭WebSocket连接
  if (websocket.value) {
    websocket.value.close()
  }
})
</script>

<style scoped>
.ai-card {
  position: fixed;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  padding: 20px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.3);
  z-index: 999999;
  cursor: pointer;
  transition: transform 0.2s ease;
  font-family: 'Comic Sans MS', 'Arial Rounded MT Bold', sans-serif;
  border: 3px solid #4CAF50;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  text-align: center;
  bottom: 160px;
  left: 20px;
  animation: float 3s ease-in-out infinite;
  overflow: hidden;
  background: linear-gradient(135deg, #7bc6cc, #1e88e5);
}

.background-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  z-index: -1;
  border-radius: 50%;
}

.card-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: white;
  margin-top: 10px;
  margin-bottom: 5px;
  font-weight: bold;
  text-shadow: 1px 1px 2px #000;
}

.title-text {
  margin: 0;
  white-space: nowrap;
}

.ai-icon {
  margin: 5px 0;
}

.icon-text {
  font-size: 32px;
}

.status-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 5px;
}

.status-light {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #f44336;
  margin-bottom: 3px;
}

.status-light.connected {
  background-color: #4CAF50;
  box-shadow: 0 0 8px #4CAF50;
}

.status-text {
  font-size: 10px;
  color: white;
  text-shadow: 1px 1px 1px #000;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

/* 聊天窗口样式 - QQ卡通风格 */
.chat-window {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 400px;
  height: 500px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  z-index: 1000000;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #d9d9d9;
}

.chat-header {
  background: linear-gradient(to right, #36b2f0, #5cdbd3);
  color: white;
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-top-left-radius: 12px;
  border-top-right-radius: 12px;
}

.chat-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.close-button {
  background: none;
  border: none;
  color: white;
  font-size: 20px;
  cursor: pointer;
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.close-button:hover {
  background-color: rgba(255, 255, 255, 0.2);
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
  background: #f5f5f5;
}

.message {
  max-width: 80%;
  position: relative;
  animation: fadeIn 0.3s ease;
  display: flex;
  flex-direction: column;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}

.message.user {
  align-self: flex-end;
  align-items: flex-end;
}

.message.ai {
  align-self: flex-start;
  align-items: flex-start;
}

.message.system {
  align-self: center;
  background-color: #e0e0e0;
  color: #666;
  font-size: 12px;
  padding: 5px 15px;
  border-radius: 12px;
}

/* QQ风格对话气泡 */
.message.user .message-content {
  background-color: #36b2f0;
  color: white;
  padding: 10px 15px;
  border-radius: 15px;
  border-bottom-right-radius: 2px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  max-width: 100%;
}

.message.user .message-content::after {
  content: '';
  position: absolute;
  right: -8px;
  bottom: 0;
  width: 0;
  height: 0;
  border: 10px solid transparent;
  border-left-color: #36b2f0;
  border-right: 0;
  border-bottom: 0;
  margin-top: -5px;
}

.message.ai .message-content {
  background-color: #ffffff;
  color: #333;
  padding: 10px 15px;
  border-radius: 15px;
  border-bottom-left-radius: 2px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  max-width: 100%;
  border: 1px solid #e0e0e0;
}

.message.ai .message-content::after {
  content: '';
  position: absolute;
  left: -8px;
  bottom: 0;
  width: 0;
  height: 0;
  border: 10px solid transparent;
  border-right-color: #ffffff;
  border-left: 0;
  border-bottom: 0;
  margin-top: -5px;
}

.message-content {
  word-wrap: break-word;
  line-height: 1.4;
}

.message-text {
  font-size: 14px;
}

.message-time {
  font-size: 10px;
  opacity: 0.7;
  margin-top: 5px;
  text-align: right;
}

.chat-input {
  display: flex;
  padding: 12px;
  background-color: #ffffff;
  border-top: 1px solid #e0e0e0;
  color: black;
}

.chat-input input {
  color: black;
  flex: 1;
  padding: 8px 15px;
  border: 1px solid #d9d9d9;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  background: #f5f5f5;
}

.chat-input input:disabled {
  background-color: #f0f0f0;
}

.chat-input button {
  margin-left: 10px;
  padding: 8px 20px;
  background: linear-gradient(to right, #36b2f0, #5cdbd3);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.chat-input button:hover:not(:disabled) {
  opacity: 0.9;
  transform: translateY(-1px);
}

.chat-input button:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

/* 流式输出的打字机动画 */
.typing-indicator {
  display: inline-flex;
  align-items: center;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background-color: #888;
  border-radius: 50%;
  margin: 0 2px;
  animation: typing 1s infinite;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

/* 响应式设计 */
@media (max-width: 600px) {
  .chat-window {
    width: 90%;
    height: 70%;
  }
}
</style>



