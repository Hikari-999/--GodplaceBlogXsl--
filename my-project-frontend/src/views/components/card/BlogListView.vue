<template>
  <div class="blog-list-view">
    <!-- 添加樱花飘落效果容器 -->
    <div class="sakura-container" v-if="showSakura">
      <div
          v-for="i in sakuraCount"
          :key="i"
          class="sakura"
          :style="getSakuraStyle(i)"
      ></div>
    </div>

    <div class="blog-list-header">
      <h1>博客文章列表</h1>
      <p>技术博客、专业性探讨❌，看站主吹牛逼✅</p>
    </div>

    <div class="blog-list">
      <div
          v-for="(blog, index) in blogList"
          :key="blog.articleId"
          class="blog-item card"
          @click="goToBlogDetail(blog.articleId)"
          :style="{ animationDelay: `${index * 0.1}s` }"
      >
        <div class="blog-header">
          <div class="blog-date">
            <i class="icon-calendar"></i>
            {{ formatDate(blog.updateTime) }}
          </div>
          <div class="blog-views">
            <i class="icon-eye"></i>
            {{ blog.readNum }}
          </div>
          <div class="blog-likes">
            <i class="icon-heart"></i>
            {{ blog.star }}
          </div>
        </div>

        <div class="blog-content">
          <h2>{{ blog.title }}</h2>
          <p>{{ blog.description }}</p>
        </div>

        <!-- 添加底部线条 -->
        <div class="blog-footer-divider"></div>

        <div class="blog-tags">
          <span
              v-for="(tag, tagIndex) in blog.tags"
              :key="tagIndex"
              class="feature-tag"
              :style="{ backgroundColor: getTagColor(tag) }"
          >
            {{ tag }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

// 控制樱花效果显示
const showSakura = ref(true)
const sakuraCount = ref(30)

// 博客列表数据（从后端获取）
const blogList = ref([])

// 获取樱花样式
const getSakuraStyle = (index) => {
  // 随机大小 (5px - 15px)
  const size = Math.random() * 10 + 5
  // 随机左右位置
  const left = Math.random() * 100
  // 随机动画延迟
  const delay = Math.random() * 10
  // 随机动画时长
  const duration = Math.random() * 5 + 10

  return {
    width: `${size}px`,
    height: `${size}px`,
    left: `${left}%`,
    animationDelay: `${delay}s`,
    animationDuration: `${duration}s`
  }
}

// 页面加载时显示导航栏
onMounted(() => {
  // 发送事件显示导航栏
  const event = new CustomEvent('headerVisibilityChange', {
    detail: true
  });
  window.dispatchEvent(event);

  // 获取博客列表数据
  fetchBlogList();
})

// 获取博客列表数据
const fetchBlogList = async () => {
  try {
    const response = await axios.get('/article-base-info/getArticleList');
    blogList.value = response.data.data;
  } catch (error) {
    console.error('获取博客列表失败:', error);
  }
}

// 获取标签颜色的方法
const getTagColor = (tag) => {
  const colors = [
    '#1976d2',   // 深蓝色
    '#388e3c',   // 绿色
    '#f57c00',   // 橙色
    '#7b1fa2',   // 紫色
    '#c2185b',   // 粉色
    '#303f9f',   // 靛蓝色
    '#0097a7',   // 青色
    '#5d4037',   // 棕色
    '#689f38',   // 浅绿色
    '#e64a19'    // 深橙色
  ]

  // 根据标签名称生成哈希值，确保相同标签总是有相同颜色
  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }

  // 使用哈希值选择颜色
  const index = Math.abs(hash) % colors.length
  return colors[index]
}

// 跳转到博客详情页
const goToBlogDetail = (id) => {
  // 找到对应的博客数据
  const blog = blogList.value.find(item => item.articleId === id)
  if (blog) {
    // 将数据通过路由状态传递
    router.push({
      path: `/blog/${id}`,
      state: { blogData: blog }
    })
  } else {
    router.push(`/blog/${id}`)
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
}
</script>

<style scoped>
.blog-list-view {
  padding: 40px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  color: #333;
  font-family: 'Comic Sans MS', 'Arial', sans-serif;
  min-height: calc(100vh - 30px);
  position: relative;
  overflow: hidden;
}

/* 樱花飘落效果 */
.sakura-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.sakura {
  position: absolute;
  top: -10px;
  background-image: radial-gradient(circle, #ffafbd 20%, #ffc3a0 70%);
  border-radius: 50% 50% 50% 0;
  transform: rotate(45deg);
  opacity: 0.8;
  box-shadow: 0 0 5px #ffafbd;
  animation: falling linear infinite;
}

@keyframes falling {
  0% {
    transform: translate3d(0, 0, 0) rotate(0deg);
    opacity: 0.8;
  }
  100% {
    transform: translate3d(20px, 100vh, 0) rotate(360deg);
    opacity: 0;
  }
}

.blog-list-header {
  text-align: center;
  margin-bottom: 30px;
  animation: fadeInDown 0.8s ease-out;
  position: relative;
  z-index: 1;
}

.blog-list-header h1 {
  font-size: 2.5rem;
  color: #1e88e5;
  margin-bottom: 10px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

.blog-list-header p {
  font-size: 1.1rem;
  color: #64b5f6;
  max-width: 600px;
  margin: 0 auto;
}

.blog-list {
  display: flex;
  flex-wrap: wrap;
  gap: 50px;
  justify-content: center;
  padding: 20px 0;
  position: relative;
  z-index: 1;
}

.blog-item.card {
  background: linear-gradient(145deg, #ffffff, #f0f0f0);
  border-radius: 15px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
  width: 360px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  overflow: hidden;
  cursor: pointer;
  opacity: 0;
  transform: translateY(20px);
  animation: fadeInUp 0.6s forwards;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 1;
}

.blog-item.card:hover {
  transform: translateY(-10px);
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.15);
}

.blog-header {
  display: flex;
  justify-content: space-between;
  padding: 15px 20px 10px;
  border-bottom: 1px solid #e0e0e0;
}

.blog-date, .blog-views, .blog-likes {
  font-size: 0.85rem;
  color: #1e88e5;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.icon-calendar, .icon-eye, .icon-heart {
  font-style: normal;
  font-size: 1.1em;
}

.icon-calendar::before {
  content: "📅";
}

.icon-eye::before {
  content: "👁";
}

.icon-heart::before {
  content: "❤";
}

html.dark .icon-calendar::before {
  content: "📅";
}

html.dark .icon-eye::before {
  content: "👁";
}

html.dark .icon-heart::before {
  content: "❤";
}

.blog-content {
  padding: 20px;
  flex-grow: 1;
}

.blog-content h2 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #1e88e5;
  font-size: 1.5rem;
  transition: color 0.3s ease;
}

.blog-item.card:hover .blog-content h2 {
  color: #0d47a1;
}

.blog-content p {
  color: #546e7a;
  margin-bottom: 15px;
  line-height: 1.6;
}

.blog-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 20px 20px;
  width: 100%;
}

.feature-tag {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  color: #fff;
  opacity: 0.9;
  transition: all 0.3s ease;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  white-space: nowrap;
}

.feature-tag:hover {
  transform: scale(1.05);
  opacity: 1;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

/* 动画关键帧 */
@keyframes fadeInUp {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .blog-list-view {
    padding: 15px;
  }

  .blog-list {
    gap: 20px;
  }

  .blog-item.card {
    width: 100%;
    max-width: 400px;
  }

  .blog-header {
    padding: 12px 15px 8px;
  }

  .blog-date, .blog-views, .blog-likes {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .blog-list-header h1 {
    font-size: 2rem;
  }

  .blog-content {
    padding: 15px;
  }

  .blog-content h2 {
    font-size: 1.3rem;
  }

  .feature-tag {
    font-size: 0.7rem;
    padding: 4px 10px;
  }

  .blog-tags {
    padding: 8px 15px 15px;
  }
}

/* 暗黑模式样式 */
html.dark .blog-list-view {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #e0e0e0;
}

html.dark .blog-list-header h1 {
  color: #64b5f6;
  text-shadow: 1px 1px 2px rgba(255,255,255,0.1);
}

html.dark .blog-list-header p {
  color: #90caf9;
}

html.dark .blog-item.card {
  background: linear-gradient(145deg, #2d2d2d, #1f1f1f);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.3);
}

html.dark .blog-item.card:hover {
  box-shadow: 0 12px 25px rgba(0, 0, 0, 0.5);
}

html.dark .blog-content h2 {
  color: #64b5f6;
}

html.dark .blog-item.card:hover .blog-content h2 {
  color: #90caf9;
}

html.dark .blog-content p {
  color: #b0bec5;
}

html.dark .blog-header {
  border-bottom: 1px solid #424242;
}

html.dark .blog-date,
html.dark .blog-views,
html.dark .blog-likes {
  color: #64b5f6;
}

/* 添加以下新样式 */
.blog-footer-divider {
  height: 1px;
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* 暗黑模式下的分割线样式 */
html.dark .blog-footer-divider {
  background: rgba(255, 255, 255, 0.1);
  box-shadow: 0 1px 2px rgba(255, 255, 255, 0.05);
}

/* 暗黑模式下的樱花效果 */
html.dark .sakura {
  background-image: radial-gradient(circle, #5d4037 20%, #795548 70%);
  box-shadow: 0 0 5px #5d4037;
  opacity: 0.6;
}
</style>
