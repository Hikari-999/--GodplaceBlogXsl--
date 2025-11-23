<template>
  <div class="blog-container">
    <div class="blog-detail">
      <div class="blog-header">
        <h1>{{ blog?.title || '加载中...' }}</h1>
        <div class="blog-meta">
          <span class="date">
            <i class="icon-calendar"></i>
            发表: {{ formatDate(blog?.startTime) }}
          </span>
          <span class="update-date" v-if="blog?.updateTime && blog?.updateTime !== blog?.startTime">
            <i class="icon-update"></i>
            更新: {{ formatDate(blog?.updateTime) }}
          </span>
          <span class="views">
            <i class="icon-eye"></i>
            {{ blog?.readNum || 0 }} 浏览
          </span>
          <span class="likes" @click="handleLike" :class="{ liked: isLiked }">
            <i class="icon-heart"></i>
            <span class="likes-count">{{ blog?.star || 0 }}</span>
            <span class="likes-count likes-count-animation" v-if="showLikeAnimation">+1</span>
          </span>
        </div>
        <div class="tags">
          <span
              v-for="(tag, index) in blog?.tags"
              :key="index"
              class="tag"
              :style="{ backgroundColor: getTagColor(tag) }"
              v-if="blog?.tags"
          >
            {{ tag }}
          </span>
        </div>
      </div>

      <div class="blog-content" v-html="renderedContent" ref="contentRef"></div>
    </div>

    <!-- 实时目录 -->
    <div class="toc-container" v-if="tocItems.length > 0">
      <!-- 返回按钮 -->
      <div class="back-button" @click="goToBlogList">
        <span class="back-icon">←</span>
        <span>返回博客列表</span>
      </div>
      <div class="toc-header">目录</div>
      <ul class="toc-list">
        <li
            v-for="item in tocItems"
            :key="item.id"
            :class="['toc-item', `toc-level-${item.level}`, { 'active': activeTocId === item.id }]"
            @click="scrollToHeading(item.id)"
        >
          <a href="javascript:void(0)">{{ item.text }}</a>
        </li>
      </ul>
    </div>

    <!-- 返回顶部按钮 -->
    <div class="back-to-top" @click="scrollToTop" v-show="showBackToTop">
      <span>↑</span>
    </div>
  </div>
</template>



<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import axios from 'axios'

// 获取路由参数和路由器
const route = useRoute()
const router = useRouter()
const contentRef = ref(null)
const tocItems = ref([])
const activeTocId = ref('')
const observer = ref(null)
const showBackToTop = ref(false)
const isLiked = ref(false)
const showLikeAnimation = ref(false)

// 博客文章数据（从后端获取）
const blog = ref({
  articleId: 0,
  title: '',
  startTime: '',
  readNum: 0,
  star: 0,
  tags: [],
  description: ''
})

// 文章内容
const articleContent = ref('')

// 渲染Markdown内容
const renderedContent = computed(() => {
  if (articleContent.value) {
    return DOMPurify.sanitize(marked(articleContent.value))
  }
  return ''
})

// 获取标签颜色的方法
const getTagColor = (tag) => {
  // 处理标签未定义的情况
  if (!tag) return '#1976d2';

  const colors = [
    '#1976d2', '#388e3c', '#f57c00', '#7b1fa2', '#c2185b',
    '#303f9f', '#0097a7', '#5d4037', '#689f38', '#e64a19'
  ]

  let hash = 0
  for (let i = 0; i < tag.length; i++) {
    hash = tag.charCodeAt(i) + ((hash << 5) - hash)
  }

  const index = Math.abs(hash) % colors.length
  return colors[index]
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN');
}

// 获取文章详情
const fetchArticleDetail = async (articleId) => {
  try {
    // 获取文章基本信息
    const baseInfoResponse = await axios.get(`/article-base-info/getArticleInfoById?id=${articleId}`);
    if (baseInfoResponse.data.data) {
      blog.value = baseInfoResponse.data.data;
    }

    try {
      const module = await import(`@/assets/articles/article${articleId}.md?raw`);
      articleContent.value = module.default;
    } catch (error) {
      console.warn(`无法加载文章内容文件 article${articleId}.md，使用默认内容`);
      articleContent.value = '# 文章内容\n\n文章内容正在准备中...';
    }
  } catch (error) {
    console.error('获取文章详情失败:', error);
  }
}

// 加载文章内容
const loadArticleContent = async (articleId) => {
  try {
    const module = await import(`@/assets/articles/article${articleId}.md?raw`);
    articleContent.value = module.default;
  } catch (error) {
    console.warn(`无法加载文章内容文件 article${articleId}.md，使用默认内容`);
    articleContent.value = '# 文章内容\n\n文章内容正在准备中...';
  }
}

// 增加文章阅读量
const increaseReadCount = async (articleId) => {
  try {
    // 使用查询参数而不是请求体发送参数
    await axios.post(`/article-base-info/modifyReadOrStar?type=read&id=${articleId}`);

    // 更新本地阅读量计数
    if (blog.value) {
      blog.value.readNum += 1;
    }
  } catch (error) {
    console.error('增加阅读量失败:', error);
  }
}

// 处理点赞
const handleLike = async () => {
  // 确保blog数据存在且未点过赞
  if (!blog.value || isLiked.value) return;

  const articleId = route.params.id;
  const previousStarCount = blog.value.star;

  isLiked.value = true;
  showLikeAnimation.value = true;

  try {
    // 调用后端接口更新点赞数
    await axios.post(`/article-base-info/modifyReadOrStar?type=star&id=${articleId}`);

    // 更新本地点赞数
    if (blog.value) {
      blog.value.star = parseInt(previousStarCount) + 1;
    }
  } catch (error) {
    console.error('点赞失败:', error);
    // 如果失败，恢复状态
    isLiked.value = false;
    if (blog.value) {
      blog.value.star = previousStarCount;
    }
  } finally {
    // 隐藏动画
    setTimeout(() => {
      showLikeAnimation.value = false;
    }, 1000);
  }
}

// 生成目录项
const generateToc = () => {
  nextTick(() => {
    if (!contentRef.value) return

    const headings = contentRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
    const items = []

    headings.forEach((heading, index) => {
      const level = parseInt(heading.tagName.charAt(1))
      const id = `heading-${index}`
      heading.id = id
      items.push({
        id,
        level,
        text: heading.textContent.trim()
      })
    })

    tocItems.value = items
    setupIntersectionObserver()
  })
}

// 设置交叉观察器以高亮当前章节
const setupIntersectionObserver = () => {
  if (observer.value) {
    observer.value.disconnect()
  }

  observer.value = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        activeTocId.value = entry.target.id
      }
    })
  }, {
    rootMargin: '-20% 0% -80% 0%'
  })

  // 观察所有标题元素
  if (contentRef.value) {
    const headings = contentRef.value.querySelectorAll('h1, h2, h3, h4, h5, h6')
    headings.forEach(heading => {
      observer.value.observe(heading)
    })
  }
}

// 滚动到指定标题
const scrollToHeading = (id) => {
  const element = document.getElementById(id)
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

// 滚动到顶部
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// 返回博客列表
const goToBlogList = () => {
  router.push('/blogs')
}

// 处理滚动事件
const handleScroll = () => {
  // 控制返回顶部按钮的显示
  showBackToTop.value = window.scrollY > 300;
}

onMounted(() => {
  const event = new CustomEvent('headerVisibilityChange', {
    detail: true
  });
  window.dispatchEvent(event);

  // 从路由参数获取文章ID
  const articleId = route.params.id;

  // 从路由状态获取博客数据
  const blogData = history.state?.blogData;
  if (blogData) {
    blog.value = blogData;
    // 如果有博客数据，直接加载文章内容
    if (articleId) {
      loadArticleContent(articleId);
      // 增加阅读量
      increaseReadCount(articleId);
    }
  } else if (articleId) {
    // 如果没有路由状态数据，则从后端获取（备选方案）
    fetchArticleDetail(articleId);
    // 增加阅读量
    increaseReadCount(articleId);
  }

  // 添加滚动事件监听器
  window.addEventListener('scroll', handleScroll);
})

onBeforeUnmount(() => {
  if (observer.value) {
    observer.value.disconnect()
  }
  // 移除滚动事件监听器
  window.removeEventListener('scroll', handleScroll);
})

// 监听文章内容变化，重新生成目录
watch(articleContent, () => {
  if (articleContent.value) {
    generateToc()
  }
})
</script>




<style scoped>
/* 添加图标样式 */
.icon-calendar, .icon-update, .icon-eye, .icon-heart {
  font-style: normal;
  margin-right: 5px;
}

.icon-calendar::before {
  content: "📅";
}

.icon-update::before {
  content: "🔄";
}

.icon-eye::before {
  content: "👁";
}

.icon-heart::before {
  content: "♡";
  transition: all 0.3s ease;
  cursor: pointer;
}

.likes.liked .icon-heart::before {
  content: "❤";
  color: #ff6b6b;
}

/* 暗黑模式下的图标样式 */
html.dark .icon-calendar::before {
  content: "📅";
}

html.dark .icon-update::before {
  content: "🔄";
}

html.dark .icon-eye::before {
  content: "👁";
}

html.dark .icon-heart::before {
  content: "♡";
}

.likes.liked html.dark .icon-heart::before {
  content: "❤";
  color: #ff6b6b;
}

/* 更新时间样式 */
.update-date {
  color: #4caf50;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.likes {
  cursor: pointer;
  display: flex;
  align-items: center;
  position: relative;
}

.likes-count {
  transition: all 0.3s ease;
}

.likes-count-animation {
  position: absolute;
  top: -20px;
  right: 0;
  color: #ff6b6b;
  font-weight: bold;
  animation: likeAnimation 1s ease-out forwards;
}

@keyframes likeAnimation {
  0% {
    transform: translateY(0);
    opacity: 1;
  }
  100% {
    transform: translateY(-20px);
    opacity: 0;
  }
}

.blog-container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  gap: 30px;
  position: relative;
}

.blog-detail {
  flex: 1;
  padding: 40px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 30px);
  font-family: 'Arial', sans-serif;
}

/* 返回按钮样式 */
.back-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: #1e88e5;
  color: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 10px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.back-button:hover {
  background: #0d47a1;
  transform: translateY(-2px);
}

.back-icon {
  font-size: 1.2em;
  font-weight: bold;
}

.toc-container {
  width: 180px;
  position: fixed;
  top: 90px;
  right: calc((100% - 1200px) / 2 - 230px);
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  max-height: calc(100vh - 220px);
  overflow-y: auto;
  z-index: 100;
}

.toc-header {
  text-align: center;
  font-size: 1.2rem;
  font-weight: bold;
  color: #1e88e5;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #1e88e5;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  margin-bottom: 8px;
}

.toc-item a {
  text-decoration: none;
  color: #555;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  display: block;
  padding: 5px 10px;
  border-radius: 5px;
}

.toc-item:hover a {
  background-color: #e3f2fd;
  color: #1e88e5;
}

.toc-item.active a {
  background-color: #1e88e5;
  color: white;
}

.toc-level-2 {
  padding-left: 15px;
}

.toc-level-3 {
  padding-left: 30px;
}

.toc-level-4 {
  padding-left: 45px;
}

.toc-level-5 {
  padding-left: 60px;
}

.toc-level-6 {
  padding-left: 75px;
}

.blog-header h1 {
  color: #1e88e5;
  margin-bottom: 20px;
  text-shadow: 1px 1px 2px rgba(0,0,0,0.1);
}

.blog-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  color: #1e88e5;
  font-weight: 500;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 30px;
}

.tag {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  color: #fff;
  opacity: 0.9;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.blog-content :deep(h1) {
  color: #0d47a1;
  border-bottom: 2px solid #1e88e5;
  padding-bottom: 10px;
  margin-top: 30px;
}

.blog-content :deep(h2) {
  color: #1565c0;
  margin-top: 25px;
}

.blog-content :deep(h3) {
  color: #1976d2;
  margin-top: 20px;
}

.blog-content :deep(p) {
  line-height: 1.7;
  color: #333;
  margin-bottom: 15px;
}

.blog-content :deep(code) {
  background-color: #e3f2fd;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
}

.blog-content :deep(pre) {
  background-color: #1e1e1e;
  color: #d4d4d4;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 20px 0;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.blog-content :deep(pre code) {
  background: none;
  padding: 0;
}

.blog-content :deep(.hljs) {
  background: #1e1e1e;
}

.blog-content :deep(ul), .blog-content :deep(ol) {
  margin-left: 20px;
  margin-bottom: 20px;
}

.blog-content :deep(li) {
  margin-bottom: 8px;
  line-height: 1.6;
}

/* 返回顶部按钮 */
.back-to-top {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 50px;
  height: 50px;
  background: #1e88e5;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  z-index: 1000;
}

.back-to-top:hover {
  background: #0d47a1;
  transform: translateY(-3px);
}

.back-to-top span {
  font-size: 24px;
  font-weight: bold;
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 1.2rem;
  color: #1e88e5;
}

/* 暗黑模式样式 */
html.dark .blog-container {
  background-color: #1a1a1a;
}

html.dark .blog-detail {
  background: linear-gradient(135deg, #1a1a1a 0%, #2d2d2d 100%);
  color: #e0e0e0;
}

html.dark .back-button {
  background: #64b5f6;
}

html.dark .back-button:hover {
  background: #90caf9;
}

html.dark .toc-container {
  background: rgba(40, 40, 40, 0.8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

html.dark .toc-header {
  color: #64b5f6;
  border-bottom: 2px solid #64b5f6;
}

html.dark .toc-item a {
  color: #b0bec5;
}

html.dark .toc-item:hover a {
  background-color: #333;
  color: #64b5f6;
}

html.dark .toc-item.active a {
  background-color: #64b5f6;
  color: #1a1a1a;
}

html.dark .blog-header h1 {
  color: #64b5f6;
}

html.dark .blog-meta {
  color: #90caf9;
}

html.dark .blog-content :deep(h1) {
  color: #90caf9;
  border-bottom: 2px solid #64b5f6;
}

html.dark .blog-content :deep(h2) {
  color: #42a5f5;
}

html.dark .blog-content :deep(h3) {
  color: #2196f3;
}

html.dark .blog-content :deep(p) {
  color: #e0e0e0;
}

html.dark .blog-content :deep(code) {
  background-color: #171717;
  color: #f0f0f0;
}

html.dark .back-to-top {
  background: #64b5f6;
}

html.dark .back-to-top:hover {
  background: #90caf9;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .toc-container {
    right: 20px;
  }
}

@media (max-width: 1024px) {
  .blog-container {
    flex-direction: column;
  }

  .toc-container {
    width: 100%;
    position: relative;
    top: 0 !important;
    right: 0;
    order: -1;
    margin-bottom: 20px;
    max-height: 300px;
  }

  .back-to-top {
    bottom: 20px;
    right: 20px;
    width: 40px;
    height: 40px;
  }

  .back-to-top span {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  .blog-container {
    padding: 10px;
  }

  .blog-detail {
    padding: 20px;
  }

  .blog-meta {
    flex-direction: column;
    gap: 5px;
  }

}
</style>

