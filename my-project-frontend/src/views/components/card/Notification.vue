<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'

// 从后端获取的通知数据
const notifications = ref([])

const showCards = ref(false)
const currentPage = ref(1)
const itemsPerPage = ref(3)

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(notifications.value.length / itemsPerPage.value)
})

// 计算当前页显示的通知
const paginatedNotifications = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage.value
  const endIndex = startIndex + itemsPerPage.value
  return notifications.value.slice(startIndex, endIndex)
})

// 计算当前页码数组
const pageNumbers = computed(() => {
  return Array.from({ length: totalPages.value }, (_, i) => i + 1)
})

// 切换页面
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
    // 重置卡片动画
    showCards.value = false
    setTimeout(() => {
      showCards.value = true
    }, 50)
  }
}

// 从后端获取通知数据
const fetchNotifications = async () => {
  try {
    const response = await axios.get('/notification/show')
    if (response.data.code === 200) {
      // 将后端数据转换为前端需要的格式，并进行倒序处理
      const reversedData = response.data.data.slice().reverse();
      notifications.value = reversedData.map(item => ({
        id: item.id,
        version: item.versionId,
        updateTime: formatDate(item.createTime),
        title: item.title,
        content: item.content,
        publisher: item.adminName // 添加发布人信息
      }))
    }
  } catch (error) {
    console.error('获取通知数据失败:', error)
  }
}


// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  // 获取通知数据
  fetchNotifications().then(() => {
    // 延迟显示卡片，让入场动画更明显
    setTimeout(() => {
      showCards.value = true
    }, 300)
  })
})
</script>

<template>
  <div class="notification-container">
    <div class="header-anime">
      <h2>更新日志</h2>
      <div class="anime-element">✦</div>
    </div>
    <div class="notification-list">
      <div
          class="notification-card"
          v-for="(notification, index) in paginatedNotifications"
          :key="notification.id"
          :class="{ 'card-enter': showCards }"
          :style="{ transitionDelay: showCards ? `${index * 0.1}s` : '0s' }"
      >
        <div class="card-header">
          <span class="version-tag">{{ notification.version }}</span>
          <span class="update-time">{{ notification.updateTime }}</span>
        </div>
        <div class="card-body">
          <h3>{{ notification.title }}</h3>
          <p>{{ notification.content }}</p>
        </div>
        <!-- 添加发布人信息 -->
        <div class="card-publisher" v-if="notification.publisher">
          <span>发布人: {{ notification.publisher }}</span>
        </div>
        <div class="card-footer">
          <span class="anime-decoration">❀</span>
        </div>
        <!-- 添加樱花元素 -->
        <div class="sakura sakura-1">🌸</div>
        <div class="sakura sakura-2">🌸</div>
        <div class="sakura sakura-3">🌸</div>
      </div>
    </div>

    <!-- 分页导航 -->
    <div class="pagination" v-if="totalPages > 1">
      <button
          class="page-button"
          :disabled="currentPage === 1"
          @click="goToPage(currentPage - 1)"
      >
        上一页
      </button>

      <button
          v-for="page in pageNumbers"
          :key="page"
          class="page-button"
          :class="{ active: currentPage === page }"
          @click="goToPage(page)"
      >
        {{ page }}
      </button>

      <button
          class="page-button"
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
      >
        下一页
      </button>
    </div>

    <!-- 添加飘落的樱花背景 -->
    <div class="sakura-falling">
      <div class="sakura-fall sakura-fall-1">🌸</div>
      <div class="sakura-fall sakura-fall-2">🌸</div>
      <div class="sakura-fall sakura-fall-3">🌸</div>
      <div class="sakura-fall sakura-fall-4">🌸</div>
      <div class="sakura-fall sakura-fall-5">🌸</div>
      <div class="sakura-fall sakura-fall-6">🌸</div>
    </div>
  </div>
</template>

<style scoped>
/* 添加发布人信息样式 */
.card-publisher {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px dashed #b3d9ff;
  color: #5c9dff;
  font-size: 14px;
  font-weight: 500;
  text-align: right;
}

.notification-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
  min-height: 100vh;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  border-radius: 20px;
  position: relative;
  overflow: hidden;
}

/* 暗黑模式下的通知容器 */
html.dark .notification-container {
  background: linear-gradient(135deg, #1a2a3a 0%, #253a4a 100%);
}

.header-anime {
  text-align: center;
  position: relative;
  margin-bottom: 30px;
}

.header-anime h2 {
  display: inline-block;
  margin: 0;
  color: #409eff;
  font-size: 2.5rem;
  font-weight: 300;
  padding: 10px 30px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 50px;
  box-shadow: 0 4px 15px rgba(64, 158, 255, 0.2);
  border: 1px solid #b3d9ff;
  position: relative;
  z-index: 2;
}

/* 暗黑模式下的标题 */
html.dark .header-anime h2 {
  background: rgba(40, 60, 80, 0.8);
  color: #64b5f6;
  border: 1px solid #5a7a9a;
  box-shadow: 0 4px 15px rgba(100, 181, 255, 0.3);
}

.anime-element {
  position: absolute;
  top: -10px;
  right: 45%;
  font-size: 1.8rem;
  color: #409eff;
  transform: rotate(20deg);
  animation: float 3s ease-in-out infinite;
}

/* 暗黑模式下的装饰元素 */
html.dark .anime-element {
  color: #64b5f6;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 25px;
  position: relative;
  z-index: 2;
}

.notification-card {
  border: 1px solid #b3d9ff;
  border-radius: 15px;
  padding: 25px;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 8px 24px rgba(179, 217, 255, 0.3);
  transition: all 0.4s ease;
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateY(30px);
}

/* 暗黑模式下的通知卡片 */
html.dark .notification-card {
  background: rgba(40, 60, 80, 0.85);
  border: 1px solid #5a7a9a;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.notification-card.card-enter {
  opacity: 1;
  transform: translateY(0);
}

.notification-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 30px rgba(100, 181, 255, 0.4);
}

/* 暗黑模式下的卡片悬停效果 */
html.dark .notification-card:hover {
  box-shadow: 0 12px 30px rgba(100, 181, 255, 0.2);
}

.notification-card::before {
  content: "";
  position: absolute;
  top: -50px;
  right: -50px;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: linear-gradient(45deg, #409eff, #90caf9);
  opacity: 0.1;
}

/* 暗黑模式下的卡片装饰 */
html.dark .notification-card::before {
  background: linear-gradient(45deg, #1a73e8, #29b6f6);
  opacity: 0.2;
}

.notification-card::after {
  content: "";
  position: absolute;
  bottom: -30px;
  left: -30px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(45deg, #4fc3f7, #29b6f6);
  opacity: 0.1;
}

/* 暗黑模式下的卡片装饰 */
html.dark .notification-card::after {
  background: linear-gradient(45deg, #29b6f6, #0288d1);
  opacity: 0.2;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px dashed #b3d9ff;
}

/* 暗黑模式下的卡片头部 */
html.dark .card-header {
  border-bottom: 1px dashed #5a7a9a;
}

.version-tag {
  background: linear-gradient(45deg, #409eff, #29b6f6);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
}

/* 暗黑模式下的版本标签 */
html.dark .version-tag {
  background: linear-gradient(45deg, #1a73e8, #0288d1);
  box-shadow: 0 2px 8px rgba(26, 115, 232, 0.4);
}

.update-time {
  color: #5c9dff;
  font-size: 14px;
  font-weight: 500;
}

/* 暗黑模式下的更新时间 */
html.dark .update-time {
  color: #90caf9;
}

.card-body h3 {
  margin: 0 0 12px 0;
  color: #1a73e8;
  font-size: 1.3rem;
  font-weight: 500;
}

/* 暗黑模式下的卡片标题 */
html.dark .card-body h3 {
  color: #64b5f6;
}

.card-body p {
  margin: 0;
  color: #4285f4;
  line-height: 1.7;
  font-size: 0.95rem;
}

/* 暗黑模式下的卡片内容 */
html.dark .card-body p {
  color: #90caf9;
}

.card-footer {
  text-align: right;
  margin-top: 15px;
}

.anime-decoration {
  color: #81c784;
  font-size: 1.2rem;
  animation: pulse 2s infinite;
}

/* 暗黑模式下的装饰元素 */
html.dark .anime-decoration {
  color: #4caf50;
}

/* 樱花元素样式 */
.sakura {
  position: absolute;
  color: #ff86b0;
  font-size: 1.2rem;
  opacity: 0.7;
  z-index: -1;
}

/* 暗黑模式下的樱花元素 */
html.dark .sakura {
  color: #f48fb1;
  opacity: 0.5;
}

.sakura-1 {
  top: 20px;
  right: 20px;
  animation: float 4s ease-in-out infinite 0.5s;
}

.sakura-2 {
  bottom: 30px;
  left: 30px;
  animation: float 5s ease-in-out infinite 1s;
}

.sakura-3 {
  top: 50%;
  right: 10px;
  animation: float 3s ease-in-out infinite 1.5s;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin: 30px 0;
  flex-wrap: wrap;
  position: relative;
  z-index: 2;
}

.page-button {
  padding: 8px 15px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px solid #b3d9ff;
  border-radius: 8px;
  color: #409eff;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
}

/* 暗黑模式下的分页按钮 */
html.dark .page-button {
  background: rgba(40, 60, 80, 0.8);
  border: 1px solid #5a7a9a;
  color: #64b5f6;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}

.page-button:hover:not(:disabled) {
  background: linear-gradient(45deg, #409eff, #29b6f6);
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
}

/* 暗黑模式下的分页按钮悬停效果 */
html.dark .page-button:hover:not(:disabled) {
  background: linear-gradient(45deg, #1a73e8, #0288d1);
  box-shadow: 0 4px 10px rgba(26, 115, 232, 0.4);
}

.page-button.active {
  background: linear-gradient(45deg, #409eff, #29b6f6);
  color: white;
  box-shadow: 0 4px 10px rgba(64, 158, 255, 0.4);
}

/* 暗黑模式下的激活分页按钮 */
html.dark .page-button.active {
  background: linear-gradient(45deg, #1a73e8, #0288d1);
  box-shadow: 0 4px 10px rgba(26, 115, 232, 0.4);
}

.page-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

/* 飘落的樱花背景 */
.sakura-falling {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.sakura-fall {
  position: absolute;
  color: #ff86b0;
  font-size: 1.5rem;
  top: -20px;
  animation-name: falling;
  animation-timing-function: linear;
  animation-iteration-count: infinite;
}

/* 暗黑模式下的飘落樱花 */
html.dark .sakura-fall {
  color: #f48fb1;
}

.sakura-fall-1 {
  left: 10%;
  animation-duration: 10s;
  animation-delay: 0s;
  font-size: 1.2rem;
}

.sakura-fall-2 {
  left: 20%;
  animation-duration: 15s;
  animation-delay: 1s;
  font-size: 1.8rem;
}

.sakura-fall-3 {
  left: 35%;
  animation-duration: 13s;
  animation-delay: 2s;
  font-size: 1rem;
}

.sakura-fall-4 {
  left: 55%;
  animation-duration: 16s;
  animation-delay: 0s;
  font-size: 1.4rem;
}

.sakura-fall-5 {
  left: 75%;
  animation-duration: 12s;
  animation-delay: 2.5s;
  font-size: 1.6rem;
}

.sakura-fall-6 {
  left: 90%;
  animation-duration: 14s;
  animation-delay: 1.5s;
  font-size: 1.1rem;
}

/* 发布人信息暗黑模式样式 */
html.dark .card-publisher {
  border-top: 1px dashed #5a7a9a;
  color: #90caf9;
}

@keyframes float {
  0% {
    transform: translateY(0px) rotate(0deg);
  }
  50% {
    transform: translateY(-10px) rotate(10deg);
  }
  100% {
    transform: translateY(0px) rotate(20deg);
  }
}

@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.2);
    opacity: 1;
  }
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
}

@keyframes falling {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(100vh) rotate(360deg);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .notification-container {
    padding: 15px;
  }

  .header-anime h2 {
    font-size: 2rem;
  }

  .notification-card {
    padding: 20px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .pagination {
    gap: 5px;
  }

  .page-button {
    padding: 6px 12px;
    font-size: 0.9rem;
  }

  .sakura-falling {
    display: none;
  }
}
</style>

