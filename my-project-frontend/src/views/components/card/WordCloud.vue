<template>
  <div class="full-screen-message-cloud">
    <!-- 背景画布用于流星雨效果 -->
    <canvas ref="meteorCanvas" class="meteor-canvas"></canvas>

    <!-- 主要内容区域 - 铺满整个屏幕 -->
    <div class="message-cloud-container">
      <!-- 留言显示区域 - 占据大部分空间 -->
      <div class="message-container" ref="messageContainer"></div>

      <!-- 简化后的输入区域 - 位于屏幕底部中央 -->
      <div class="simple-input-area">
        <div class="input-wrapper">
          <el-input
              v-model="newMessage"
              placeholder="我想留点什么好呢..."
              class="simple-message-input"
              @keyup.enter="addMessage"
          />
          <el-button
              type="primary"
              @click="addMessage"
              :disabled="!newMessage.trim()"
              class="send-button"
          >
            发送
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { get, post } from '@/net'

// 响应式数据
const meteorCanvas = ref(null)
const messageContainer = ref(null)
const newMessage = ref('')
const messageAuthor = ref('')
const messages = ref([])

// 流星雨动画相关
let animationId = null
let meteorInterval = null
const meteors = ref([])
let messageMeteors = [] // 存储留言流星

// 初始化流星雨画布
const initMeteorCanvas = () => {
  const canvas = meteorCanvas.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  const resizeCanvas = () => {
    canvas.width = canvas.clientWidth
    canvas.height = canvas.clientHeight
  }

  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)

  // 创建背景流星
  const createMeteor = () => {
    const canvas = meteorCanvas.value
    if (!canvas) return

    // 每次创建1-3个流星以增强效果
    const meteorCount = Math.floor(Math.random() * 3) + 1
    for (let i = 0; i < meteorCount; i++) {
      meteors.value.push({
        x: Math.random() * canvas.width,
        y: -20,
        length: Math.random() * 150 + 60, // 更长的尾巴
        speed: Math.random() * 15, // 更快的速度
        angle: Math.random() * Math.PI / 20 - Math.PI / 8, // 更宽的角度范围
        brightness: Math.random() * 0.8 + 0.1, // 更亮的流星
        alpha: Math.random() * 0.7 + 0.3, // 更明显的透明度
        size: Math.random() * 4 , // 更大的尺寸
        color: `hsl(${Math.random() * 60 + 180}, 100%, 80%)` // 随机蓝白色调
      })
    }
  }

  // 绘制所有流星（包括背景流星和留言流星）
  const drawMeteors = () => {
    const canvas = meteorCanvas.value
    if (!canvas) return

    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // 绘制深色半透明背景以增强流星效果
    ctx.fillStyle = 'rgba(12, 20, 69, 0.2)'
    ctx.fillRect(0, 0, canvas.width, canvas.height)

    // 更新和绘制背景流星
    for (let i = meteors.value.length - 1; i >= 0; i--) {
      const meteor = meteors.value[i]

      // 更新位置
      meteor.x += Math.sin(meteor.angle) * meteor.speed
      meteor.y += Math.cos(meteor.angle) * meteor.speed

      // 添加闪烁效果
      const flicker = Math.sin(Date.now() / 100 + i) * 0.3 + 0.7

      // 绘制流星主体
      ctx.beginPath()
      ctx.moveTo(meteor.x, meteor.y)
      ctx.lineTo(
          meteor.x - Math.sin(meteor.angle) * meteor.length,
          meteor.y - Math.cos(meteor.angle) * meteor.length
      )

      // 设置样式
      const gradient = ctx.createLinearGradient(
          meteor.x, meteor.y,
          meteor.x - Math.sin(meteor.angle) * meteor.length,
          meteor.y - Math.cos(meteor.angle) * meteor.length
      )
      gradient.addColorStop(0, `rgba(255, 255, 255, ${meteor.alpha * flicker})`)
      gradient.addColorStop(0.3, `rgba(255, 255, 255, ${meteor.alpha * 0.7 * flicker})`)
      gradient.addColorStop(1, `rgba(255, 255, 255, 0)`)

      ctx.strokeStyle = gradient
      ctx.lineWidth = meteor.size
      ctx.stroke()

      // 绘制流星头部光晕
      ctx.beginPath()
      ctx.arc(meteor.x, meteor.y, meteor.size * 2, 0, Math.PI * 2)
      const radialGradient = ctx.createRadialGradient(
          meteor.x, meteor.y, 0,
          meteor.x, meteor.y, meteor.size * 3
      )
      radialGradient.addColorStop(0, `rgba(255, 255, 255, ${meteor.alpha * 0.8})`)
      radialGradient.addColorStop(1, 'rgba(255, 255, 255, 0)')
      ctx.fillStyle = radialGradient
      ctx.fill()

      // 移除超出屏幕的流星
      if (meteor.y > canvas.height + 100 || meteor.x > canvas.width + 100 || meteor.x < -100) {
        meteors.value.splice(i, 1)
      }
    }

    // 更新和绘制留言流星
    for (let i = messageMeteors.length - 1; i >= 0; i--) {
      const meteor = messageMeteors[i]

      // 更新位置（从右向左）
      meteor.x -= meteor.speed
      meteor.y += Math.sin(meteor.x / 100) * 0.2 // 轻微的上下波动

      // 绘制流星主体
      ctx.save()
      ctx.translate(meteor.x, meteor.y)

      // 绘制文字
      ctx.font = meteor.font
      ctx.fillStyle = meteor.color
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'

      // 文字发光效果
      ctx.shadowColor = meteor.color
      ctx.shadowBlur = 10
      ctx.fillText(meteor.content, 0, 0)
      ctx.restore()

      // 移除超出屏幕的留言流星
      if (meteor.x < -200) {
        messageMeteors.splice(i, 1)
      }
    }

    animationId = requestAnimationFrame(drawMeteors)
  }

  // 开始动画
  drawMeteors()

  // 更频繁地创建流星，每100-200毫秒创建一次
  meteorInterval = setInterval(createMeteor, Math.random() * 100 + 100)
}

// 添加新留言
const addMessage = () => {
  if (!newMessage.value.trim()) {
    ElMessage.warning('请输入留言内容')
    return
  }

  // 创建留言对象
  const message = {
    content: newMessage.value.trim(),
    author: messageAuthor.value.trim() || '匿名'
  }

  // 发送到后端
  post('/api/message/add', message, () => {
    // 添加到本地列表
    const fullMessage = {
      content: message.content,
      author: message.author,
      timestamp: new Date(),
      id: Date.now()
    }

    messages.value.unshift(fullMessage)

    // 创建留言流星
    const canvas = meteorCanvas.value
    if (canvas) {
      const colors = [
        'rgb(255, 107, 107)',  // 红色
        'rgb(78, 205, 196)',  // 青色
        'rgb(69, 183, 209)',  // 蓝色
        'rgb(255, 190, 11)',  // 黄色
        'rgb(251, 86, 7)',    // 橙色
        'rgb(131, 56, 236)',  // 紫色
        'rgb(58, 134, 255)',  // 亮蓝色
        'rgb(6, 214, 160)'    // 绿色
      ]

      const fonts = [
        'bold 24px Arial',
        'normal 20px Arial',
        'bold 28px Arial',
        'normal 18px Arial',
        'bold 32px Arial',
        'normal 22px Arial',
        'bold 26px Arial',
        'normal 19px Arial'
      ]

      messageMeteors.push({
        content: fullMessage.content,
        x: canvas.width + 100,
        y: Math.random() * (canvas.height - 200) + 100,
        length: Math.random() * 50 + 30,
        speed: Math.random() * 1.5 + 0.5, // 减缓速度
        color: colors[Math.floor(Math.random() * colors.length)],
        font: fonts[Math.floor(Math.random() * fonts.length)]
      })
    }

    // 保持最多显示30条留言
    if (messages.value.length > 10000) {
      messages.value.pop()
    }

    // 清空输入框
    newMessage.value = ''

    ElMessage.success('留言已发送')
  }, (error) => {
    ElMessage.error('留言发送失败: ' + error.message)
  })
}

// 获取所有留言
const fetchMessages = () => {
  get('/api/message/list', (data) => {
    messages.value = data.map(msg => ({
      content: msg.content,
      author: msg.author || '匿名',
      id: msg.id,
      timestamp: msg.createTime
    }))

    // 初始化一些留言流星
    const canvas = meteorCanvas.value
    if (canvas) {
      messages.value.forEach((message, index) => {
        if (index < 1000) { // 只为前5条留言创建流星
          const colors = [
            'rgb(255, 107, 107)',
            'rgb(78, 205, 196)',
            'rgb(69, 183, 209)',
            'rgb(255, 190, 11)',
            'rgb(251, 86, 7)'
          ]

          const fonts = [
            'bold 24px Arial',
            'normal 20px Arial',
            'bold 28px Arial',
            'normal 18px Arial',
            'bold 32px Arial'
          ]

          messageMeteors.push({
            content: message.content,
            x: canvas.width - (index * 200),
            y: Math.random() * (canvas.height - 200) + 100,
            length: Math.random() * 50 + 30,
            speed: Math.random() * 1.5+ 0.1,
            color: colors[index % colors.length],
            font: fonts[index % fonts.length]
          })
        }
      })
    }
  }, (error) => {
    ElMessage.error('获取留言失败: ' + error.message)
  })
}

// 组件挂载时初始化
onMounted(() => {
  initMeteorCanvas()
  fetchMessages()
})

// 组件卸载时清理
onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  if (meteorInterval) {
    clearInterval(meteorInterval)
  }
})
</script>


<style scoped>
.full-screen-message-cloud {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(to bottom, #0c1445, #1a237e, #4a148c);
  border-radius: 0;
}

.meteor-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.message-cloud-container {
  position: relative;
  z-index: 2;
  width: 100%;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
}

.message-header {
  text-align: center;
  margin-bottom: 20px;
  color: white;
  font-size: 18px;
  opacity: 0.8;
}

.message-header h2 {
  margin: 0;
  font-size: 24px;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.message-container {
  flex: 1;
  position: relative;
  width: 100%;
  min-height: 300px;
  margin-bottom: 20px;
  border: none;
  background: transparent;
  overflow: hidden;
}

.simple-input-area {
  position: fixed;
  bottom: 30px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  width: 80%;
  max-width: 500px;
  border: none;
  box-shadow: none;
  background: transparent;
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
  background: transparent;
  border: none;
  box-shadow: none;
}

.simple-message-input {
  flex: 1;
  border: none;
  box-shadow: none;
}

.simple-message-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding: 0;
  background: transparent;
  box-shadow: none;
}

.simple-message-input :deep(.el-input__inner) {
  border-radius: 20px;
  padding: 10px 20px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
  backdrop-filter: blur(10px);
  box-shadow: none;
}

.simple-message-input :deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.7);
}

.simple-message-input :deep(.el-input__inner:focus) {
  outline: none;
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.send-button {
  border-radius: 20px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 10px rgba(64, 158, 255, 0.3);
  white-space: nowrap;
}

.send-button:hover {
  background: linear-gradient(135deg, #50a8ff, #77c5ff);
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.4);
}

.send-button:active {
  transform: translateY(0);
}

.send-button:disabled {
  background: #a0cfff;
  cursor: not-allowed;
}
</style>
