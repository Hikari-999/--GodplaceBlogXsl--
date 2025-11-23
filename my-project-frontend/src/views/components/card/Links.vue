<template>
  <div class="links-page">
    <!-- 页面标题 -->
    <div class="page-header" :class="{ 'animate-in': animations.header }">
      <h1 class="animated-title">友情链接</h1>
      <p>与优秀的网站建立连接，共同成长</p>
    </div>

    <!-- 友情链接列表 -->
    <el-row :gutter="30" class="links-container">
      <el-col
          v-for="(link, index) in friendLinks"
          :key="index"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
      >
        <el-card class="link-card" shadow="hover" @click="visitLink(link.url)"
                 :class="{ 'animate-in': animations.cards }"
                 :style="{ animationDelay: (index * 0.1) + 's' }">
          <div class="link-content">
            <div class="link-avatar"
                 :style="{ animationDelay: (index * 0.1 + 0.1) + 's' }">
              <img :src="link.avatar" :alt="link.name" v-if="link.avatar">
              <el-icon v-else class="avatar-placeholder"><Link /></el-icon>
            </div>
            <div class="link-info">
              <h3 :style="{ animationDelay: (index * 0.1 + 0.2) + 's' }">{{ link.name }}</h3>
              <p class="description" :style="{ animationDelay: (index * 0.1 + 0.3) + 's' }">{{ link.description }}</p>
              <p class="url" :style="{ animationDelay: (index * 0.1 + 0.4) + 's' }">{{ truncatedUrl(link.url) }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 申请友链按钮 -->
    <div class="apply-section" :class="{ 'animate-in': animations.applySection }">
      <el-card class="apply-card" shadow="never">
        <el-button type="primary" size="large" @click="openApplyForm" class="pulse-button">
          申请友链
        </el-button>
        <p>如果您希望与本站建立友情链接，请联系我：</p>
        <ul>
          <li>
            <el-icon><Message /></el-icon>
            邮箱：<a :href="'mailto:' + contactEmail">{{ contactEmail }}</a>
          </li>
          <li>
            <el-icon><Link /></el-icon>
            本站链接: <span class="site-url">https://www.godplace.icu</span>
          </li>
        </ul>
      </el-card>
    </div>

    <!-- 申请友链表单对话框 -->

    <el-dialog
        v-model="dialogVisible"
        width="600px"
        :before-close="handleClose"
        class="apply-dialog"
        :class="{ 'dialog-animate-in': animations.dialog }"
    >
      <div class="dialog-header-glow"></div>
      <el-form
          ref="formRef"
          :model="applyForm"
          :rules="rules"
          label-width="80px"
          class="advanced-form"
      >
        <div class="form-header">
          <h3>填写友链信息</h3>
          <p>请提供您网站的详细信息</p>
        </div>

        <el-form-item label="网站名称" prop="name">
          <el-input
              v-model="applyForm.name"
              placeholder="请输入网站名称"
              clearable
              class="custom-input"
          />
        </el-form-item>

        <el-form-item label="网站链接" prop="url">
          <el-input
              v-model="applyForm.url"
              placeholder="请输入网站链接，如：https://www.example.com"
              clearable
              class="custom-input"
          />
        </el-form-item>

        <el-form-item label="网站描述" prop="description">
          <el-input
              v-model="applyForm.description"
              type="textarea"
              :rows="3"
              placeholder="请简单描述您的网站内容"
              maxlength="200"
              show-word-limit
              class="custom-textarea"
          />
        </el-form-item>

        <el-form-item label="网站头像" class="avatar-form-item">
          <div class="avatar-upload-container">
            <el-upload
                class="avatar-uploader"
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleAvatarChange"
                :before-upload="beforeAvatarUpload"
            >
              <div class="avatar-preview">
                <img v-if="applyForm.avatar" :src="applyForm.avatar" class="avatar" />
                <div v-else class="avatar-placeholder-content">
                  <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
                  <span class="avatar-upload-text">点击上传头像</span>
                </div>
              </div>
            </el-upload>
            <div class="upload-tip">支持JPG/PNG格式，大小不超过2MB</div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" class="custom-button cancel-button">取消</el-button>
          <el-button
              type="primary"
              @click="submitApply"
              :loading="submitLoading"
              class="custom-button submit-button"
          >
            提交申请
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Link, Plus, Message, View, Star } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import axios from 'axios'

// 联系邮箱
const contactEmail = ref('1010287405@qq.com')

// 友链数据
const friendLinks = ref([])

// 控制对话框显示
const dialogVisible = ref(false)

// 表单引用
const formRef = ref()

// 提交加载状态
const submitLoading = ref(false)

// 页面加载状态 - 分步骤动画
const animations = ref({
  header: false,
  cards: false,
  applySection: false,
  dialog: false
})

// 申请表单数据
const applyForm = reactive({
  name: '',
  url: '',
  description: '',
  avatar: ''
})

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入网站名称', trigger: 'blur' },
    { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
  ],
  url: [
    { required: true, message: '请输入网站链接', trigger: 'blur' },
    {
      pattern: /^(https?:\/\/)?([\da-z\.-]+)\.([a-z\.]{2,6})([\/\w \.-]*)*\/?$/,
      message: '请输入正确的网站链接格式',
      trigger: 'blur'
    }
  ],
  description: [
    { required: true, message: '请输入网站描述', trigger: 'blur' },
    { min: 5, max: 200, message: '长度在 5 到 200 个字符', trigger: 'blur' }
  ]
}

// 当前选择的文件
const selectedFile = ref(null)

// 获取友链数据
const fetchFriendLinks = async () => {
  try {
    const response = await axios.get('/api/friend-link/list')
    if (response.data.code === 200) {
      friendLinks.value = response.data.data
    } else {
      ElMessage.error('获取友链数据失败')
    }
  } catch (error) {
    console.error('获取友链数据时出错:', error)
    ElMessage.error('获取友链数据时出错')
  }
}

// 访问链接
const visitLink = (url) => {
  ElMessage.success('正在打开链接: ' + url)
  window.open(url, '_blank')
}

// 截断URL显示
const truncatedUrl = (url) => {
  if (!url) return ''
  if (url.length > 30) {
    return url.substring(0, 30) + '...'
  }
  return url
}

// 打开申请表单
const openApplyForm = () => {
  dialogVisible.value = true
  // 重置表单
  if (formRef.value) {
    formRef.value.resetFields()
  }
  applyForm.name = ''
  applyForm.url = ''
  applyForm.description = ''
  applyForm.avatar = ''
  selectedFile.value = null

  // 触发动画
  setTimeout(() => {
    animations.value.dialog = true
  }, 100)
}

// 关闭前确认
const handleClose = (done) => {
  ElMessageBox.confirm('确认关闭表单吗？')
      .then(() => {
        animations.value.dialog = false
        setTimeout(() => {
          done()
        }, 300)
      })
      .catch(() => {
        // 取消关闭
      })
}

// 处理头像选择
const handleAvatarChange = (file) => {
  selectedFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    applyForm.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 头像上传前检查
const beforeAvatarUpload = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  const isAllowedType = allowedTypes.includes(rawFile.type)
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isAllowedType) {
    ElMessage.error('头像图片必须是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
  }
  return isAllowedType && isLt2M
}

// 提交申请
const submitApply = async () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return

    submitLoading.value = true

    try {
      const formData = new FormData()
      formData.append('name', applyForm.name)
      formData.append('url', applyForm.url)
      formData.append('description', applyForm.description)

      if (selectedFile.value) {
        formData.append('avatar', selectedFile.value)
      }

      const response = await axios.post('/api/friend-link/add', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })

      if (response.data.code === 200) {
        ElMessage.success('申请提交成功！')
        dialogVisible.value = false
        // 重新获取友链列表
        fetchFriendLinks()
      } else {
        ElMessage.error(response.data.message || '申请提交失败')
      }
    } catch (error) {
      console.error('提交申请时出错:', error)
      ElMessage.error('提交申请时出错')
    } finally {
      submitLoading.value = false
    }
  })
}

onMounted(() => {
  fetchFriendLinks()
  // 分步骤触发动画
  setTimeout(() => {
    animations.value.header = true
  }, 100)

  setTimeout(() => {
    animations.value.cards = true
  }, 400)

  setTimeout(() => {
    animations.value.applySection = true
  }, 700)
})
</script>
<style scoped>
.links-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px;
  min-height: 100vh;
  color: #333;
  background: #f5f7fa;
  transition: background 0.3s ease, color 0.3s ease;
  position: relative;
  overflow: hidden;
}

/* 添加背景动画效果 */
.links-page::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(circle at 10% 20%, rgba(59, 130, 246, 0.1) 0%, transparent 20%),
  radial-gradient(circle at 90% 80%, rgba(139, 92, 246, 0.1) 0%, transparent 20%);
  z-index: -1;
  animation: bg-move 15s infinite alternate;
}

@keyframes bg-move {
  0% {
    transform: translate(0, 0);
  }
  100% {
    transform: translate(20px, 20px);
  }
}

/* 暗黑模式样式 */
html.dark .links-page {
  background: #1a1a2e;
  color: #f0f0f0;
}

.animated-title {
  position: relative;
  display: inline-block;
  animation: title-glow 3s ease-in-out infinite alternate;
}

@keyframes title-glow {
  0% {
    text-shadow: 0 0 5px rgba(30, 64, 175, 0.5);
  }
  100% {
    text-shadow: 0 0 20px rgba(30, 64, 175, 0.8), 0 0 30px rgba(30, 64, 175, 0.6);
  }
}

.page-header {
  text-align: center;
  margin-bottom: 50px;
  padding: 40px 0;
  position: relative;
  background: #ffffff;
  border-radius: 15px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
  transition: all 0.5s ease;
  opacity: 0;
  transform: translateY(-30px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.page-header.animate-in {
  opacity: 1;
  transform: translateY(0);
  transition: opacity 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275),
  transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.page-header::before {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(45deg, transparent, rgba(59, 130, 246, 0.1), transparent);
  transform: rotate(45deg);
  animation: shine 4s infinite;
}

@keyframes shine {
  0% {
    transform: rotate(45deg) translateX(-100%);
  }
  100% {
    transform: rotate(45deg) translateX(100%);
  }
}

html.dark .page-header {
  background: #16213e;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
  border: 1px solid #1f2937;
}

.page-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 200px;
  height: 4px;
  background: linear-gradient(90deg, transparent, #3b82f6, transparent);
  border-radius: 2px;
}

html.dark .page-header::after {
  background: linear-gradient(90deg, transparent, #60a5fa, transparent);
}

.page-header h1 {
  font-size: 3.5rem;
  margin-bottom: 15px;
  color: #1e40af;
  font-weight: 700;
  letter-spacing: 2px;
  position: relative;
  z-index: 1;
}

html.dark .page-header h1 {
  color: #f9fafb;
}

.page-header p {
  font-size: 1.4rem;
  color: #475569;
  max-width: 800px;
  margin: 0 auto;
  font-weight: 300;
  position: relative;
  z-index: 1;
}

html.dark .page-header p {
  color: #d1d5db;
}

.links-container {
  margin-bottom: 50px;
}

.link-card {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  border-radius: 15px;
  overflow: hidden;
  height: 100%;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  position: relative;
  transform-style: preserve-3d;
  perspective: 1000px;
  opacity: 0;
  transform: translateY(30px) rotateX(10deg);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.link-card.animate-in {
  opacity: 1;
  transform: translateY(0) rotateX(0);
  transition: opacity 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275),
  transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.link-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.1), rgba(139, 92, 246, 0.1));
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: -1;
}

.link-card:hover::before {
  opacity: 1;
}

html.dark .link-card {
  background: #1f2937;
  border: 1px solid #374151;
}

.link-card:hover {
  transform: translateY(-10px) scale(1.03);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border-color: #94a3b8;
}

html.dark .link-card:hover {
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  border-color: #4b5563;
}

.link-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 25px 15px;
}

.link-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f1f5f9;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
  flex-shrink: 0;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
  border: 3px solid #ffffff;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: scale(0.5) rotate(-10deg);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.link-card.animate-in .link-avatar {
  opacity: 1;
  transform: scale(1) rotate(0);
  transition: opacity 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.2s,
  transform 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.2s;
}

.link-avatar::after {
  content: "";
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  transform: rotate(45deg);
}

@keyframes avatarFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

html.dark .link-avatar {
  background: #374151;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  border: 3px solid #1f2937;
}

.link-avatar img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.link-avatar:hover img {
  transform: scale(1.1);
}

.avatar-placeholder {
  font-size: 32px;
  color: #94a3b8;
}

html.dark .avatar-placeholder {
  color: #9ca3af;
}

.link-info h3 {
  margin: 0 0 12px 0;
  font-size: 1.5rem;
  color: #1e293b;
  font-weight: 600;
  transition: color 0.3s ease;
  position: relative;
  opacity: 0;
  transform: translateX(-20px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.link-card.animate-in .link-info h3 {
  opacity: 1;
  transform: translateX(0);
  transition: opacity 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.3s,
  transform 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.3s;
}

.link-info h3::after {
  content: "";
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  width: 0;
  height: 2px;
  background: #3b82f6;
  transition: width 0.3s ease;
}

.link-card:hover .link-info h3::after {
  width: 50px;
}

html.dark .link-info h3 {
  color: #f3f4f6;
}

.description {
  margin: 0 0 15px 0;
  color: #64748b;
  font-size: 1rem;
  line-height: 1.5;
  transition: color 0.3s ease;
  min-height: 40px;
  position: relative;
  overflow: hidden;
  opacity: 0;
  transform: translateX(20px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.link-card.animate-in .description {
  opacity: 1;
  transform: translateX(0);
  transition: opacity 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.4s,
  transform 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.4s;
}

.description::before {
  content: "";
  position: absolute;
  top: -10px;
  left: -15px;
  font-size: 3rem;
  color: rgba(59, 130, 246, 0.2);
  font-family: Georgia, serif;
}


html.dark .description {
  color: #d1d5db;
}

.url {
  margin: 0;
  color: #3b82f6;
  font-size: 0.95rem;
  word-break: break-all;
  font-style: italic;
  transition: color 0.3s ease;
  padding: 5px 10px;
  border-radius: 5px;
  background: rgba(59, 130, 246, 0.05);
  display: inline-block;
  opacity: 0;
  transform: scale(0.8);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.link-card.animate-in .url {
  opacity: 1;
  transform: scale(1);
  transition: opacity 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.5s,
  transform 0.6s cubic-bezier(0.175, 0.885, 0.32, 1.275) 0.5s;
}

.url:hover {
  background: rgba(59, 130, 246, 0.1);
}

html.dark .url {
  color: #93c5fd;
  background: rgba(147, 197, 253, 0.1);
}

html.dark .url:hover {
  background: rgba(147, 197, 253, 0.2);
}

/* 添加统计数据样式 */
.link-stats {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px dashed rgba(59, 130, 246, 0.2);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.9rem;
  color: #64748b;
}

html.dark .stat-item {
  color: #9ca3af;
}

.stat-item .el-icon {
  font-size: 1rem;
}

.apply-section {
  max-width: 800px;
  margin: 0 auto;
  text-align: center;
  opacity: 0;
  transform: translateY(50px);
  transition: opacity 0.6s ease, transform 0.6s ease;
}

.apply-section.animate-in {
  opacity: 1;
  transform: translateY(0);
  transition: opacity 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275),
  transform 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.apply-card {
  border-radius: 15px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.05);
  padding: 40px;
  position: relative;
  transition: transform 0.3s ease;
}

.apply-card:hover {
  transform: translateY(-5px);
}

.apply-card::before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 5px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899);
  animation: gradientShift 3s infinite alternate;
}

@keyframes gradientShift {
  0% {
    background: linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899);
  }
  100% {
    background: linear-gradient(90deg, #ec4899, #3b82f6, #8b5cf6);
  }
}

.apply-card :deep(.el-button--primary) {
  background: #1e40af;
  border: 1px solid #1e3a8a;
  color: #f9fafb;
  padding: 20px 40px;
  margin-bottom: 30px;
  font-size: 1.1rem;
  font-weight: 500;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(30, 64, 175, 0.3);
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  z-index: 1;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(30, 64, 175, 0.4);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(30, 64, 175, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(30, 64, 175, 0);
  }
}

.apply-card :deep(.el-button--primary)::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #3730a3;
  z-index: -1;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.apply-card :deep(.el-button--primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(30, 64, 175, 0.4);
}

.apply-card :deep(.el-button--primary:hover)::before {
  opacity: 1;
}

.apply-card :deep(.el-button--primary:active) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.3);
}

html.dark .apply-card {
  background: #1f2937;
  border: 1px solid #374151;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

html.dark .apply-card :deep(.el-button--primary) {
  background: #312e81;
  border: 1px solid #3730a3;
  color: #f3f4f6;
  box-shadow: 0 4px 12px rgba(49, 46, 129, 0.4);
}

html.dark .apply-card :deep(.el-button--primary)::before {
  background: #3730a3;
}

html.dark .apply-card :deep(.el-button--primary:hover) {
  box-shadow: 0 6px 16px rgba(49, 46, 129, 0.5);
}

html.dark .apply-card :deep(.el-button--primary:active) {
  box-shadow: 0 2px 8px rgba(49, 46, 129, 0.4);
}

.apply-card p {
  color: #475569;
  font-size: 1.1rem;
  margin: 0 auto 25px;
  transition: color 0.3s ease;
}

html.dark .apply-card p {
  color: #d1d5db;
}

.apply-card ul {
  text-align: left;
  max-width: 500px;
  margin: 30px auto 0;
  padding-left: 25px;
  background: #f8fafc;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
  position: relative;
}

.apply-card ul::before {
  content: "i";
  position: absolute;
  top: -10px;
  left: 10px;
  width: 20px;
  height: 20px;
  background: #3b82f6;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
}

html.dark .apply-card ul {
  background: #111827;
  border: 1px solid #374151;
}

.apply-card li {
  margin-bottom: 15px;
  line-height: 1.7;
  color: #475569;
  font-size: 1.05rem;
  display: flex;
  align-items: center;
  transition: color 0.3s ease;
}

html.dark .apply-card li {
  color: #d1d5db;
}

.apply-card li .el-icon {
  margin-right: 10px;
  color: #3b82f6;
}

html.dark .apply-card li .el-icon {
  color: #93c5fd;
}

.apply-card a {
  color: #3b82f6;
  text-decoration: none;
  transition: all 0.3s ease;
  padding: 2px 4px;
  border-radius: 3px;
}

html.dark .apply-card a {
  color: #93c5fd;
}

.apply-card a:hover {
  color: #2563eb;
  text-decoration: underline;
  background: #f1f5f9;
}

html.dark .apply-card a:hover {
  color: #bfdbfe;
  background: #1f2937;
}

.site-url {
  font-family: 'Courier New', monospace;
  background: #f1f2f6;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.site-url:hover {
  background: #e2e8f0;
  transform: scale(1.02);
}

html.dark .site-url {
  background: #374151;
}

html.dark .site-url:hover {
  background: #4b5563;
}

/* 上传组件样式 */
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
  border-radius: 10px;
}

.avatar-uploader .el-upload {
  border: 2px dashed #d9d9d9;
  border-radius: 10px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
  width: 178px;
  height: 178px;
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

html.dark .avatar-uploader .el-upload {
  border-color: #4b5563;
}

html.dark .avatar-uploader .el-upload:hover {
  border-color: #409eff;
}


.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 10px;
  text-align: center;
}

html.dark .upload-tip {
  color: #9ca3af;
}

/* 对话框动画 */
.apply-dialog {
  opacity: 0;
  transform: scale(0.8);
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.apply-dialog.dialog-animate-in {
  opacity: 1;
  transform: scale(1);
  transition: opacity 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275),
  transform 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.apply-dialog :deep(.el-dialog) {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 15px;
  border: 1px solid #4a3aff;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}

.apply-dialog :deep(.el-dialog__header) {
  padding: 20px 30px;
  border-bottom: 1px solid rgba(122, 106, 255, 0.2);
  margin-right: 0;
}

.apply-dialog :deep(.el-dialog__title) {
  color: #e2e1ff;
  font-weight: 600;
  font-size: 1.4rem;
}

.apply-dialog :deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 25px;
}

.apply-dialog :deep(.el-dialog__headerbtn .el-dialog__close) {
  color: #a09cff;
  font-size: 24px;
  transition: all 0.3s ease;
}

.apply-dialog :deep(.el-dialog__headerbtn .el-dialog__close:hover) {
  color: #ffffff;
  transform: rotate(90deg);
}

.apply-dialog :deep(.el-dialog__body) {
  padding: 20px 30px;
}

.form-header {
  text-align: center;
  margin-bottom: 25px;
  padding-bottom: 15px;
  border-bottom: 1px solid rgba(122, 106, 255, 0.2);
}

.form-header h3 {
  color: #e2e1ff;
  font-size: 1.3rem;
  margin-top: -50px;
  margin-bottom: 8px;
  font-weight: 600;
}

.form-header p {
  color: #a09cff;
  font-size: 0.95rem;
  margin: 0;
}

.apply-dialog :deep(.el-form-item) {
  margin-bottom: 22px;
}

.apply-dialog :deep(.el-form-item__label) {
  color: #c5c2ff;
  font-weight: 500;
}

.apply-dialog :deep(.el-input__inner) {
  background: rgba(31, 30, 64, 0.7);
  border: 1px solid #4a3aff;
  color: #e2e1ff;
  border-radius: 8px;
  height: 42px;
  transition: all 0.3s ease;
}

.apply-dialog :deep(.el-input__inner:focus) {
  border-color: #7a6aff;
  box-shadow: 0 0 0 2px rgba(122, 106, 255, 0.2);
}

.apply-dialog :deep(.el-textarea__inner) {
  background: rgba(31, 30, 64, 0.7);
  border: 1px solid #4a3aff;
  color: #e2e1ff;
  border-radius: 8px;
  transition: all 0.3s ease;
  min-height: 100px;
}

.apply-dialog :deep(.el-textarea__inner:focus) {
  border-color: #7a6aff;
  box-shadow: 0 0 0 2px rgba(122, 106, 255, 0.2);
}

.apply-dialog :deep(.el-input__count) {
  color: #a09cff;
}

.avatar-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-preview {
  position: relative;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px dashed #4a3aff;
  transition: all 0.3s ease;
}

.avatar-preview:hover {
  border-color: #7a6aff;
  box-shadow: 0 0 0 4px rgba(122, 106, 255, 0.2);
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background: rgba(31, 30, 64, 0.5);
}



.avatar-upload-text {
  color: #a09cff;
  font-size: 12px;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #7a6aff;
  margin-top: 12px;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  padding: 20px 30px;
  margin-top: -40px;
  border-top: 1px solid rgba(122, 106, 255, 0.2);
}

.custom-button {
  border-radius: 8px;
  padding: 10px 25px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-button {
  background: rgba(31, 30, 64, 0.7) !important;
  border: 1px solid #4a3aff !important;
  color: #c5c2ff !important;
}

.cancel-button:hover {
  background: rgba(74, 58, 255, 0.3) !important;
  border-color: #7a6aff !important;
}

.submit-button {
  background: linear-gradient(135deg, #7a6aff 0%, #5e4dff 100%) !important;
  border: none !important;
  color: white !important;
  box-shadow: 0 4px 15px rgba(122, 106, 255, 0.3);
}

.submit-button:hover {
  box-shadow: 0 6px 20px rgba(122, 106, 255, 0.5);
  transform: translateY(-2px);
}

.submit-button:active {
  transform: translateY(0);
}

.dialog-header-glow {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: linear-gradient(90deg, #3b82f6, #8b5cf6, #ec4899);
  animation: gradientShift 3s infinite alternate;
  border-radius: 2px 2px 0 0;
  z-index: 10;
}

@keyframes gradientShift {
  0% {
    background: linear-gradient(90deg, #7a6aff, #5e4dff, #4a3aff);
  }
  100% {
    background: linear-gradient(90deg, #4a3aff, #7a6aff, #5e4dff);
  }
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .links-page {
    padding: 20px;
  }

  .page-header h1 {
    font-size: 3rem;
  }

  .page-header p {
    font-size: 1.2rem;
  }
}

@media (max-width: 768px) {
  .links-page {
    padding: 15px;
  }

  .page-header {
    padding: 30px 15px;
    margin-bottom: 30px;
  }

  .page-header h1 {
    font-size: 2.5rem;
  }

  .page-header p {
    font-size: 1.1rem;
  }

  .link-content {
    padding: 20px 10px;
  }

  .link-avatar {
    width: 70px;
    height: 70px;
  }

  .link-info h3 {
    font-size: 1.3rem;
  }

  .apply-card {
    padding: 25px 15px;
  }

  .avatar-uploader .el-upload,
  .avatar-uploader .avatar,
  .avatar-uploader-icon {
    width: 60px;
    height: 60px;
  }

}

@media (max-width: 480px) {
  .page-header h1 {
    font-size: 2rem;
  }

  .page-header p {
    font-size: 1rem;
  }

  .link-info h3 {
    font-size: 1.2rem;
  }

  .description {
    font-size: 0.9rem;
  }

  .apply-card :deep(.el-button--primary) {
    padding: 15px 30px;
    font-size: 1rem;
  }

  .link-stats {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
