<template>
  <div class="talks-container">
    <div class="talks-header">
      <div class="header-content">
        <h2>随笔</h2>
        <div class="header-decoration">
          <div class="decoration-item"></div>
          <div class="decoration-item"></div>
          <div class="decoration-item"></div>
        </div>
      </div>
      <div class="animated-background">
        <div class="floating-element"></div>
        <div class="floating-element"></div>
        <div class="floating-element"></div>
        <div class="floating-element"></div>
        <div class="floating-element"></div>
      </div>
    </div>

    <div class="talks-content">
      <div v-if="talks.length === 0" class="no-talks">
        <el-empty description="暂无随笔内容" />
      </div>

      <div v-else class="talks-list">
        <div
            v-for="(talk, index) in talks"
            :key="index"
            class="talk-item"
        >
          <div class="talk-header">
            <el-avatar :src="talk.avatar || defaultAvatar" />
            <div class="talk-user-info">
              <span class="talk-username">{{ talk.username }}</span>
              <span class="talk-time">{{ formatTime(talk.createTime) }}</span>
            </div>
            <div class="talk-tags">
              <el-tag
                  v-for="(tag, tagIndex) in talk.tags"
                  :key="tagIndex"
                  size="small"
                  type="info"
                  class="talk-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </div>

          <div class="talk-content">
            <p>{{ talk.content }}</p>
          </div>

          <div v-if="talk.imageList && talk.imageList.length > 0" class="talk-images">
            <el-image
                v-for="(image, imgIndex) in talk.imageList"
                :key="imgIndex"
                :src="getImageUrl(image)"
                fit="cover"
                class="talk-image"
                :preview-src-list="talk.imageList.map(img => getImageUrl(img))"
                :initial-index="imgIndex"
                :preview-teleported="true"
            />
          </div>

          <div class="talk-footer">
            <div class="talk-meta">
              <span class="meta-item" @click="toggleComments(talk)">
                <el-icon><ChatDotRound /></el-icon>
                {{ talk.comments || 0 }} 条评论
              </span>
            </div>
            <div class="talk-actions">
              <el-button type="primary" link @click="likeTalk(talk)">
                <el-icon :class="{ 'liked': talk.liked }">
                  <svg class="star-icon" viewBox="0 0 1024 1024" width="1em" height="1em">
                    <path v-if="talk.liked" d="M908.1 353.1l-253.9-36.9L540.7 86.1c-3.1-6.3-8.2-11.4-14.5-14.5-15.8-7.8-35-1.3-42.9 14.5L369.8 316.2l-253.9 36.9c-7 1-13.4 4.3-18.3 9.3-4.9 5-7.2 11.5-7.2 18.3 0 7.2 2.3 13.7 7.2 18.3l183.7 179.1-43.4 252.9c-1.2 6.9 1.5 13.9 7.2 18.3 5.7 4.4 12.9 5.1 19.2 1.9l226.4-119.1 226.4 119.1c6.3 3.2 13.5 2.5 19.2-1.9 5.7-4.4 8.4-11.4 7.2-18.3l-43.4-252.9 183.7-179.1c4.9-4.6 7.2-11.1 7.2-18.3 0-6.8-2.3-13.3-7.2-18.3z" />
                    <path v-else d="M908.1 353.1l-253.9-36.9L540.7 86.1c-3.1-6.3-8.2-11.4-14.5-14.5-15.8-7.8-35-1.3-42.9 14.5L369.8 316.2l-253.9 36.9c-7 1-13.4 4.3-18.3 9.3-4.9 5-7.2 11.5-7.2 18.3 0 7.2 2.3 13.7 7.2 18.3l183.7 179.1-43.4 252.9c-1.2 6.9 1.5 13.9 7.2 18.3 5.7 4.4 12.9 5.1 19.2 1.9l226.4-119.1 226.4 119.1c6.3 3.2 13.5 2.5 19.2-1.9 5.7-4.4 8.4-11.4 7.2-18.3l-43.4-252.9 183.7-179.1c4.9-4.6 7.2-11.1 7.2-18.3 0-6.8-2.3-13.3-7.2-18.3zM664.2 604.8l38.4 224.8-201.9-106.3-202.1 106.3 38.4-224.8-163.8-160.2 226.3-32.9 101.3-205.9 101.6 205.9 226.3 32.9-163.8 160.2z" />
                  </svg>
                </el-icon>
                Like
              </el-button>
              <el-button type="primary" link @click="shareTalk(talk)">
                <el-icon><Share /></el-icon>
                分享
              </el-button>
            </div>
          </div>

          <!-- 评论区域 -->
          <div v-if="talk.showComments" class="talk-comments">
            <!-- 评论列表 -->
            <div class="comments-list">
              <div
                  v-for="(comment, commentIndex) in talk.commentList"
                  :key="commentIndex"
                  class="comment-item"
              >
                <div class="comment-header">
                  <el-avatar size="small" :src="getRandomAvatar(comment.username)" />
                  <div class="comment-user-info">
                    <span class="comment-username">{{ comment.username }}</span>
                  </div>
                  <div class="comment-actions">
                    <el-button
                        v-if="userStore.isLoggedIn && (userStore.username === comment.username || userStore.role === 'admin')"
                        type="danger"
                        size="small"
                        link
                        @click="deleteComment(talk, comment.id)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div class="comment-content">
                  {{ comment.content }}
                </div>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>

              <div v-if="talk.commentList && talk.commentList.length === 0" class="no-comments">
                暂无评论，快来抢沙发吧！
              </div>
            </div>

            <!-- 评论输入框 -->
            <div v-if="userStore.isLoggedIn" class="comment-input-area">
              <el-input
                  v-model="talk.newComment"
                  type="textarea"
                  :rows="3"
                  placeholder="请输入您的评论..."
                  maxlength="500"
                  show-word-limit
              />
              <div class="comment-actions">
                <el-button
                    type="primary"
                    @click="addComment(talk)"
                    :disabled="!talk.newComment || talk.newComment.trim() === ''"
                >
                  发表评论
                </el-button>
              </div>
            </div>

            <!-- 未登录提示 -->
            <div v-else class="comment-login-prompt">
              <el-alert
                  title="请登录后发表评论"
                  type="info"
                  show-icon
                  :closable="false"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import { ChatDotRound, Star, Share } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/avatar.jpg'
import { get, post, del } from '@/net'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user_store'
import axios from "axios"
// 随笔数据
const talks = ref([])
const userStore = useUserStore()

// 格式化时间
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// 分享随笔
const shareTalk = (talk) => {
  const url = window.location.origin + '/talks/' + talk.id;
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板');
  }).catch(() => {
    // 如果剪贴板API失败，使用传统方式
    const textArea = document.createElement('textarea');
    textArea.value = url;
    document.body.appendChild(textArea);
    textArea.select();
    document.execCommand('copy');
    document.body.removeChild(textArea);
    ElMessage.success('链接已复制到剪贴板');
  });
}

// ... existing code ...
// 点赞随笔
const likeTalk = (talk) => {
  // 切换点赞状态
  talk.liked = !talk.liked;

  // 如果是点赞，增加点赞数；否则减少点赞数
  if (talk.liked) {
    talk.likes = (talk.likes || 0) + 1;
  } else {
    talk.likes = Math.max(0, (talk.likes || 0) - 1);
  }
}

// 获取图片完整URL
const getImageUrl = (imagePath) => {
  // 如果是完整URL则直接返回
  if (imagePath && (imagePath.startsWith('http://') || imagePath.startsWith('https://'))) {
    return imagePath;
  }

  // 如果是相对路径，则添加基础URL
  if (imagePath && imagePath.startsWith('/')) {
    // 使用axios的baseURL作为基础URL
    const baseURL = axios.defaults.baseURL || '';
    return baseURL + imagePath;
  }

  return imagePath;
}

// 生成随机头像URL
const getRandomAvatar = (seed) => {
  // 使用DiceBear API生成基于种子的随机头像
  // 如果没有提供seed，则生成一个随机字符串
  if (!seed) {
    seed = Math.random().toString(36).substring(2);
  }
  return `https://api.dicebear.com/6.x/miniavs/svg?seed=${seed}`;
}

// 展开/收起评论
const toggleComments = (talk) => {
  // 如果还没有加载过评论，则先加载评论
  if (talk.showComments === undefined) {
    talk.showComments = true;
    loadComments(talk);
  } else {
    talk.showComments = !talk.showComments;
  }
}

// 加载评论
const loadComments = (talk) => {
  get(`/api/talk/${talk.id}/comments`, (data) => {
    talk.commentList = data || [];
    talk.newComment = ''; // 初始化评论输入框
  }, (message) => {
    console.error('获取评论失败:', message);
    ElMessage.error('获取评论失败: ' + message);
  });
}

// 添加评论
const addComment = (talk) => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再发表评论');
    return;
  }

  if (!talk.newComment || talk.newComment.trim() === '') {
    ElMessage.warning('请输入评论内容');
    return;
  }

  const commentData = {
    content: talk.newComment,
    username: userStore.username // 使用当前登录用户的用户名
  };

  post(`/api/talk/${talk.id}/comment`, commentData, () => {
    // 添加成功后重新加载评论
    loadComments(talk);
    talk.newComment = ''; // 清空评论输入框
    ElMessage.success('评论发表成功');
  }, (message) => {
    ElMessage.error('发表评论失败: ' + message);
  });
}


// 删除评论
const deleteComment = (talk, commentId) => {
  // 检查用户是否登录
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录后再进行操作');
    return;
  }

  del(`/api/talk/comment/${commentId}`, () => {
    // 删除成功后重新加载评论
    loadComments(talk);
    ElMessage.success('评论删除成功');
  }, (message) => {
    ElMessage.error('删除评论失败: ' + message);
  });
}



// 获取随笔数据
const fetchTalks = () => {
  get('/api/talk/list', (data) => {
    // 对数据进行预处理，确保图片数据完整
    talks.value = data.map(talk => {
      // 处理图片列表，确保是数组格式
      if (talk.images && typeof talk.images === 'string') {
        talk.imageList = talk.images.split(',').filter(img => img.trim() !== '');
      } else if (Array.isArray(talk.imageList)) {
        talk.imageList = talk.imageList.filter(img => img && img.trim() !== '');
      } else {
        talk.imageList = [];
      }
      return talk;
    });
  }, (message) => {
    console.error('获取随笔列表失败:', message);
    ElMessage.error('获取随笔列表失败: ' + message);
  });
}

onMounted(() => {
  fetchTalks()
})
</script>

<style scoped>
.talk-actions .el-button:hover {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  transform: translateY(-2px);
}

.liked {
  animation: likeRotate 0.6s ease;
}

@keyframes likeRotate {
  0% {
    transform: rotate(0deg) scale(1);
  }
  50% {
    transform: rotate(180deg) scale(1.3);
  }
  100% {
    transform: rotate(360deg) scale(1);
  }
}

/* 添加星形图标样式 */
.talk-actions :deep(.el-icon) {
  color: var(--el-text-color-secondary);
  transition: color 0.3s ease;
}

.talk-actions :deep(.liked) {
  color: #ff4d4f !important;
}

/* 为未点赞状态设置描边样式 */
.talk-actions :deep(.el-icon:not(.liked)) svg {
  fill: transparent;
  stroke: currentColor;
  stroke-width: 10;
}

/* 为点赞状态设置填充样式 */
.talk-actions :deep(.liked) svg {
  fill: currentColor;
  stroke: currentColor;
  stroke-width: 10;
}




.talks-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}


.talks-header {
  text-align: center;
  margin-bottom: 30px;
  padding: 50px 20px;
  border-radius: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
  position: relative;
  overflow: hidden;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.talks-header::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.15) 0%, rgba(255,255,255,0) 70%);
  transform: rotate(30deg);
}

.talks-header .header-content {
  position: relative;
  z-index: 2;
}

.talks-header h2 {
  font-size: 3.5rem;
  margin-bottom: 15px;
  font-weight: 800;
  text-shadow: 0 4px 15px rgba(0, 0, 0, 0.3);
  letter-spacing: 3px;
  font-family: 'Microsoft YaHei', serif;
}

.header-decoration {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 25px;
}

.decoration-item {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

.decoration-item:nth-child(2) {
  background: rgba(255, 255, 255, 1);
  transform: scale(1.4);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.8);
}

.no-talks {
  text-align: center;
  padding: 50px 0;
  background: var(--el-bg-color-page);
  border-radius: 16px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
}

.talk-item {
  background: var(--el-bg-color-overlay);
  border-radius: 20px;
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 30px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.1);
  border: 1px solid var(--el-border-color-lighter);
  backdrop-filter: blur(15px);
  position: relative;
  overflow: hidden;
}

.talk-item::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #667eea, #764ba2);
}

.talk-item:hover {
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  transform: translateY(-8px);
}

.talk-header {
  display: flex;
  align-items: center;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.talk-user-info {
  margin-left: 20px;
  display: flex;
  flex-direction: column;
}

.talk-username {
  font-weight: 700;
  font-size: 18px;
  color: var(--el-text-color-primary);
}

.talk-time {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}

.talk-tags {
  margin-left: auto;
  display: flex;
  gap: 10px;
}

.talk-tag {
  border-radius: 25px;
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-7);
  font-weight: 500;
  padding: 5px 12px;
}

.talk-content p {
  font-size: 17px;
  line-height: 2;
  color: var(--el-text-color-primary);
  margin: 0 0 25px 0;
  white-space: pre-wrap;
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  letter-spacing: 0.8px;
}

.talk-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 15px;
  margin-bottom: 25px;
}

.talk-image {
  border-radius: 12px;
  cursor: pointer;
  aspect-ratio: 1;
  object-fit: cover;
  transition: all 0.4s ease;
  box-shadow: 0 6px 15px rgba(0, 0, 0, 0.12);
}

.talk-image:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
}

.talk-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 25px;
  border-top: 1px solid var(--el-border-color-lighter);
  flex-wrap: wrap;
  gap: 15px;
}

.talk-meta {
  display: flex;
  gap: 25px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
  padding: 10px 15px;
  border-radius: 10px;
  transition: all 0.3s ease;
  cursor: pointer;
}

.meta-item:hover {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  transform: translateY(-2px);
}

.talk-actions {
  display: flex;
  gap: 20px;
}

.talk-actions .el-button {
  font-weight: 600;
  color: var(--el-text-color-secondary);
  padding: 10px 15px;
  border-radius: 10px;
  transition: all 0.3s ease;
}

.talk-actions .el-button:hover {
  color: var(--el-color-primary);
  background-color: var(--el-color-primary-light-9);
  transform: translateY(-2px);
}

.comment-item {
  padding: 15px 0;
  border-bottom: 1px dashed var(--el-border-color-light);
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.comment-user-info {
  margin-left: 10px;
  flex: 1;
}

.comment-username {
  font-weight: 600;
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.comment-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
  margin-left: 33px;
}

.comment-actions {
  margin-top: 8px;
  margin-left: auto;
}

.comment-content {
  padding-left: 34px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--el-text-color-primary);
  white-space: pre-wrap;
}

.comment-input-area {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.comment-login-prompt {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.no-comments {
  padding: 20px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

/* 暗黑模式适配 - 高级黑色调色板 */
html.dark .talks-header {
  background: linear-gradient(135deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.5);
}

html.dark .talks-header::before {
  background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0) 70%);
}

html.dark .talk-item {
  background: linear-gradient(145deg, #1a1a1a, #121212);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 6px 25px rgba(0, 0, 0, 0.4);
}

html.dark .talk-item:hover {
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

html.dark .talk-content p {
  color: #e0e0e0;
}

html.dark .talk-tag {
  background: rgba(147, 112, 219, 0.2);
  border-color: rgba(147, 112, 219, 0.4);
  color: #b39ddb;
}

html.dark .meta-item {
  color: #aaa;
}

html.dark .decoration-item {
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.3);
}

html.dark .decoration-item:nth-child(2) {
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.5);
}

html.dark .comment-item {
  border-bottom: 1px dashed rgba(255, 255, 255, 0.1);
}

html.dark .comment-content {
  color: #e0e0e0;
}

html.dark .comment-username {
  color: #fff;
}

@media (max-width: 768px) {
  .talks-container {
    padding: 15px;
  }

  .talks-header h2 {
    font-size: 2.5rem;
  }

  .talks-header {
    padding: 30px 15px;
  }

  .talk-item {
    padding: 20px;
  }

  .talk-images {
    grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  }

  .talk-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .talk-user-info {
    margin-left: 0;
  }

  .talk-tags {
    margin-left: 0;
  }

  .talk-footer {
    flex-direction: column;
    align-items: flex-start;
  }

  .talk-meta {
    flex-wrap: wrap;
  }

  .comment-content {
    padding-left: 0;
  }
}

/* 背景动画元素 */
.animated-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  overflow: hidden;
}

.floating-element {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  animation: float 6s ease-in-out infinite;
}

.floating-element:nth-child(1) {
  width: 80px;
  height: 80px;
  top: 20%;
  left: 10%;
  animation-delay: 0s;
}

.floating-element:nth-child(2) {
  width: 50px;
  height: 50px;
  top: 60%;
  left: 20%;
  animation-delay: 1s;
}

.floating-element:nth-child(3) {
  width: 100px;
  height: 100px;
  top: 40%;
  right: 10%;
  animation-delay: 2s;
}

.floating-element:nth-child(4) {
  width: 60px;
  height: 60px;
  bottom: 20%;
  right: 25%;
  animation-delay: 3s;
}

.floating-element:nth-child(5) {
  width: 40px;
  height: 40px;
  bottom: 60%;
  left: 30%;
  animation-delay: 4s;
}

@keyframes float {
  0% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(10px, 15px) rotate(180deg);
  }
  100% {
    transform: translate(0, 0) rotate(360deg);
  }
}
</style>
