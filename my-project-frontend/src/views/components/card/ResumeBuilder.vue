<template>
  <div class="resume-builder">
    <div class="resume-container">
      <!-- 简历预览区域 -->
      <div class="resume-preview">
        <div class="resume-content">
          <!-- 个人基本信息 -->
          <div class="resume-section personal-info">
            <div class="avatar-section">
              <img v-if="resumeData.avatar" :src="resumeData.avatar" alt="个人照片" class="avatar-image">
              <div v-else class="avatar-placeholder">照片</div>
            </div>
            <div class="personal-details">
              <h1 class="name">{{ resumeData.name || '姓名' }}</h1>
              <div class="contact-info">
                <p><i class="icon">性别:</i> {{ resumeData.gender || '性别' }}</p>
                <p><i class="icon">年龄:</i> {{ resumeData.age || '年龄' }}</p>
                <p><i class="icon">电话:</i> {{ resumeData.phone || '电话' }}</p>
                <p><i class="icon">微信:</i> {{ resumeData.wechat || '微信' }}</p>
                <p><i class="icon">邮箱:</i> {{ resumeData.email || '邮箱' }}</p>
                <p><i class="icon">地址:</i> {{ resumeData.address || '地址' }}</p>
              </div>
            </div>
          </div>

          <!-- 教育经历 -->
          <div class="resume-section education" v-if="resumeData.education.length > 0">
            <h2 class="section-title">教育经历</h2>
            <div class="section-content">
              <div v-for="(edu, index) in resumeData.education" :key="index" class="education-item">
                <h3>{{ edu.school || '学校名称' }}</h3>
                <p>{{ edu.major || '专业' }} | {{ edu.degree || '学位' }} | {{ edu.time || '时间' }}</p>
                <p v-if="edu.description">{{ edu.description }}</p>
              </div>
            </div>
          </div>

          <!-- 专业技能 -->
          <div class="resume-section professional" v-if="resumeData.professional.length > 0">
            <h2 class="section-title">专业技能</h2>
            <div class="section-content">
              <ol>
                <li v-for="(prof, index) in resumeData.professional" :key="index">
                  {{ prof.description || '技能描述' }}
                </li>
              </ol>
            </div>
          </div>

          <!-- 项目经历 -->
          <div class="resume-section projects" v-if="resumeData.projects.length > 0">
            <h2 class="section-title">项目经历</h2>
            <div class="section-content">
              <div v-for="(project, index) in resumeData.projects" :key="index" class="project-item">
                <h3>{{ project.name || '项目名称' }}</h3>
                <p>{{ project.time || '时间' }} | {{ project.tech || '技术栈' }}</p>
                <p v-if="project.description">{{ project.description }}</p>
              </div>
            </div>
          </div>

          <!-- 实习/工作经历 -->
          <div class="resume-section work" v-if="resumeData.work.length > 0">
            <h2 class="section-title">实习/工作经历</h2>
            <div class="section-content">
              <div v-for="(work, index) in resumeData.work" :key="index" class="work-item">
                <h3>{{ work.company || '公司名称' }} - {{ work.position || '职位' }}</h3>
                <p>{{ work.time || '时间' }}</p>
                <p v-if="work.description">{{ work.description }}</p>
              </div>
            </div>
          </div>

          <!-- 荣誉奖项 -->
          <div class="resume-section awards" v-if="resumeData.awards.length > 0">
            <h2 class="section-title">荣誉奖项</h2>
            <div class="section-content">
              <ol>
                <li v-for="(award, index) in resumeData.awards" :key="index">
                  {{ award.name || '奖项名称' }} - {{ award.time || '时间' }}
                </li>
              </ol>
            </div>
          </div>
        </div>
      </div>

      <!-- 表单编辑区域 -->
      <div class="form-container">
        <!-- 导出和重置按钮区域 -->
        <div class="action-buttons">
          <button @click="exportToPDF" class="action-btn pdf-btn">
            导出为PDF
          </button>
          <button @click="exportToWord" class="action-btn word-btn">
            导出为Word
          </button>
          <button @click="resetForm" class="action-btn reset-btn">
            重置表单
          </button>
        </div>

        <!-- 个人基本信息表单 -->
        <div class="form-section">
          <h2>个人基本信息</h2>
          <div class="form-row">
            <div class="form-group">
              <label>姓名</label>
              <input v-model="resumeData.name" type="text" placeholder="请输入姓名">
            </div>
            <div class="form-group">
              <label>性别</label>
              <select v-model="resumeData.gender">
                <option value="">请选择</option>
                <option value="男">男</option>
                <option value="女">女</option>
              </select>
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>年龄</label>
              <input v-model="resumeData.age" type="number" placeholder="请输入年龄">
            </div>
            <div class="form-group">
              <label>电话</label>
              <input v-model="resumeData.phone" type="text" placeholder="请输入电话号码">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>微信</label>
              <input v-model="resumeData.wechat" type="text" placeholder="请输入微信号">
            </div>
            <div class="form-group">
              <label>邮箱</label>
              <input v-model="resumeData.email" type="email" placeholder="请输入邮箱地址">
            </div>
          </div>
          <div class="form-group">
            <label>地址</label>
            <input v-model="resumeData.address" type="text" placeholder="请输入地址">
          </div>
          <div class="form-group">
            <label>个人照片</label>
            <div class="avatar-upload">
              <input type="file" accept="image/*" @change="handleAvatarUpload" ref="avatarInput" style="display: none;">
              <button @click="$refs.avatarInput.click()" class="upload-btn">选择照片</button>
              <button v-if="resumeData.avatar" @click="removeAvatar" class="remove-avatar-btn">移除照片</button>
            </div>
            <div v-if="resumeData.avatar" class="avatar-preview">
              <img :src="resumeData.avatar" alt="预览照片" class="preview-image">
            </div>
          </div>
        </div>

        <!-- 教育经历表单 -->
        <div class="form-section">
          <h2>教育经历</h2>
          <div v-for="(edu, index) in resumeData.education" :key="index" class="form-subsection">
            <div class="form-row">
              <div class="form-group">
                <label>学校</label>
                <input v-model="edu.school" type="text" placeholder="请输入学校名称">
              </div>
              <div class="form-group">
                <label>专业</label>
                <input v-model="edu.major" type="text" placeholder="请输入专业">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>学位</label>
                <input v-model="edu.degree" type="text" placeholder="例如：本科/硕士">
              </div>
              <div class="form-group">
                <label>时间</label>
                <input v-model="edu.time" type="text" placeholder="例如：2020.09-2024.06">
              </div>
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="edu.description" placeholder="请输入相关描述"></textarea>
            </div>
            <button @click="removeEducation(index)" class="remove-btn">删除此项</button>
          </div>
          <button @click="addEducation" class="add-btn">添加教育经历</button>
        </div>

        <!-- 专业技能表单 -->
        <div class="form-section">
          <h2>专业技能</h2>
          <div v-for="(prof, index) in resumeData.professional" :key="index" class="form-subsection">
            <div class="form-group">
              <label>技能描述</label>
              <textarea v-model="prof.description" placeholder="请输入技能描述"></textarea>
            </div>
            <button @click="removeProfessional(index)" class="remove-btn">删除此项</button>
          </div>
          <button @click="addProfessional" class="add-btn">添加专业技能</button>
        </div>

        <!-- 项目经历表单 -->
        <div class="form-section">
          <h2>项目经历</h2>
          <div v-for="(project, index) in resumeData.projects" :key="index" class="form-subsection">
            <div class="form-row">
              <div class="form-group">
                <label>项目名称</label>
                <input v-model="project.name" type="text" placeholder="请输入项目名称">
              </div>
              <div class="form-group">
                <label>技术栈</label>
                <input v-model="project.tech" type="text" placeholder="例如：Vue.js, Spring Boot">
              </div>
            </div>
            <div class="form-group">
              <label>时间</label>
              <input v-model="project.time" type="text" placeholder="例如：2023.01-2023.05">
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="project.description" placeholder="请输入项目描述"></textarea>
            </div>
            <button @click="removeProject(index)" class="remove-btn">删除此项</button>
          </div>
          <button @click="addProject" class="add-btn">添加项目经历</button>
        </div>

        <!-- 实习/工作经历表单 -->
        <div class="form-section">
          <h2>实习/工作经历</h2>
          <div v-for="(work, index) in resumeData.work" :key="index" class="form-subsection">
            <div class="form-row">
              <div class="form-group">
                <label>公司名称</label>
                <input v-model="work.company" type="text" placeholder="请输入公司名称">
              </div>
              <div class="form-group">
                <label>职位</label>
                <input v-model="work.position" type="text" placeholder="请输入职位">
              </div>
            </div>
            <div class="form-group">
              <label>时间</label>
              <input v-model="work.time" type="text" placeholder="例如：2023.07-2023.09">
            </div>
            <div class="form-group">
              <label>描述</label>
              <textarea v-model="work.description" placeholder="请输入工作描述"></textarea>
            </div>
            <button @click="removeWork(index)" class="remove-btn">删除此项</button>
          </div>
          <button @click="addWork" class="add-btn">添加实习/工作经历</button>
        </div>

        <!-- 荣誉奖项表单 -->
        <div class="form-section">
          <h2>荣誉奖项</h2>
          <div v-for="(award, index) in resumeData.awards" :key="index" class="form-subsection">
            <div class="form-row">
              <div class="form-group">
                <label>奖项名称</label>
                <input v-model="award.name" type="text" placeholder="请输入奖项名称">
              </div>
              <div class="form-group">
                <label>时间</label>
                <input v-model="award.time" type="text" placeholder="例如：2023.05">
              </div>
            </div>
            <button @click="removeAward(index)" class="remove-btn">删除此项</button>
          </div>
          <button @click="addAward" class="add-btn">添加荣誉奖项</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import html2canvas from 'html2canvas'
import jsPDF from 'jspdf'
import { saveAs } from 'file-saver'

// 简历数据
const resumeData = reactive({
  // 个人基本信息
  name: '',
  gender: '',
  age: '',
  phone: '',
  wechat: '',
  email: '',
  address: '',
  avatar: '',

  // 教育经历
  education: [],

  // 专业技能
  professional: [],

  // 项目经历
  projects: [],

  // 实习/工作经历
  work: [],

  // 荣誉奖项
  awards: []
})

// 初始化默认数据
onMounted(() => {
  // 设置默认个人信息
  resumeData.name = '张三'
  resumeData.gender = '男'
  resumeData.age = '25'
  resumeData.phone = '138-0000-0000'
  resumeData.wechat = 'zhangsan123'
  resumeData.email = 'zhangsan@example.com'
  resumeData.address = '北京市朝阳区xxx街道'

  // 添加默认教育经历
  resumeData.education.push({
    school: '北京大学',
    major: '计算机科学与技术',
    degree: '本科',
    time: '2016.09-2020.06',
    description: '主修课程：数据结构、算法设计、数据库原理、计算机网络等'
  })

  // 添加默认专业技能
  resumeData.professional.push(
      { description: '熟练掌握Java、Python等编程语言' },
      { description: '熟悉Spring、Vue.js等主流框架' },
      { description: '具备良好的数据库设计和优化能力' },
      { description: '熟悉Linux系统操作和常用命令' }
  )

  // 添加默认项目经历
  resumeData.projects.push({
    name: '在线教育平台',
    tech: 'Spring Boot, Vue.js, MySQL',
    time: '2022.03-2022.08',
    description: '负责后端API开发和数据库设计，使用Spring Boot框架实现用户管理、课程管理等功能模块'
  })

  // 添加默认工作经历
  resumeData.work.push({
    company: '某某科技有限公司',
    position: 'Java开发工程师',
    time: '2020.07-至今',
    description: '参与公司核心产品的开发与维护，负责后端服务的设计与实现，优化系统性能，提升用户体验'
  })

  // 添加默认荣誉奖项
  resumeData.awards.push(
      { name: '优秀毕业生', time: '2020.06' },
      { name: '编程竞赛一等奖', time: '2019.12' }
  )
})

// 添加教育经历
const addEducation = () => {
  resumeData.education.push({
    school: '',
    major: '',
    degree: '',
    time: '',
    description: ''
  })
}

// 删除教育经历
const removeEducation = (index) => {
  resumeData.education.splice(index, 1)
}

// 添加专业技能
const addProfessional = () => {
  resumeData.professional.push({
    description: ''
  })
}

// 删除专业技能
const removeProfessional = (index) => {
  resumeData.professional.splice(index, 1)
}

// 添加项目经历
const addProject = () => {
  resumeData.projects.push({
    name: '',
    tech: '',
    time: '',
    description: ''
  })
}

// 删除项目经历
const removeProject = (index) => {
  resumeData.projects.splice(index, 1)
}

// 添加实习/工作经历
const addWork = () => {
  resumeData.work.push({
    company: '',
    position: '',
    time: '',
    description: ''
  })
}

// 删除实习/工作经历
const removeWork = (index) => {
  resumeData.work.splice(index, 1)
}

// 添加荣誉奖项
const addAward = () => {
  resumeData.awards.push({
    name: '',
    time: ''
  })
}

// 删除荣誉奖项
const removeAward = (index) => {
  resumeData.awards.splice(index, 1)
}

// 处理头像上传
const handleAvatarUpload = (event) => {
  const file = event.target.files[0]
  if (file) {
    const reader = new FileReader()
    reader.onload = (e) => {
      resumeData.avatar = e.target.result
    }
    reader.readAsDataURL(file)
  }
}

// 移除头像
const removeAvatar = () => {
  resumeData.avatar = ''
  // 重置文件输入框
  event.target.previousElementSibling.value = ''
}

// 重置表单
const resetForm = () => {
  // 重置个人信息
  resumeData.name = ''
  resumeData.gender = ''
  resumeData.age = ''
  resumeData.phone = ''
  resumeData.wechat = ''
  resumeData.email = ''
  resumeData.address = ''
  resumeData.avatar = ''

  // 清空各列表
  resumeData.education = []
  resumeData.professional = []
  resumeData.projects = []
  resumeData.work = []
  resumeData.awards = []
}

// 导出为PDF
const exportToPDF = () => {
  const resumeElement = document.querySelector('.resume-preview')
  html2canvas(resumeElement).then(canvas => {
    const imgData = canvas.toDataURL('image/png')
    const pdf = new jsPDF('p', 'mm', 'a4')
    const imgWidth = 210 // A4宽度
    const pageHeight = 297 // A4高度
    const imgHeight = canvas.height * imgWidth / canvas.width
    let heightLeft = imgHeight
    let position = 0

    pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      pdf.addPage()
      pdf.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    pdf.save(`${resumeData.name || '简历'}_简历.pdf`)
  })
}

// 导出为Word
const exportToWord = () => {
  const resumeElement = document.querySelector('.resume-preview')
  const htmlContent = resumeElement.innerHTML

  // 创建Word文档内容
  const wordContent = `
    <html xmlns:o='urn:schemas-microsoft-com:office:office'
          xmlns:w='urn:schemas-microsoft-com:office:word'
          xmlns='http://www.w3.org/TR/REC-html40'>
    <head>
      <meta charset='utf-8'>
      <title>简历</title>
    </head>
    <body>
      ${htmlContent}
    </body>
    </html>
  `

  // 创建Blob对象
  const blob = new Blob([
    '\ufeff', // BOM for UTF-8
    wordContent
  ], {
    type: 'application/msword'
  })

  // 下载文件
  saveAs(blob, `${resumeData.name || '简历'}_简历.doc`)
}
</script>

<style scoped>
.resume-builder {
  display: flex;
  padding: 20px;
  min-height: 100vh;
  background-color: #f0f8ff; /* 默认蓝白色背景 */
  color: #333333;
}

.resume-container {
  display: flex;
  gap: 20px;
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
}

.resume-preview {
  flex: 1;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 30px;
  min-height: calc(100vh - 40px);
  color: #333;
}

.resume-content {
  max-width: 800px;
  margin: 0 auto;
}

.resume-section {
  margin-bottom: 30px;
}

.personal-info {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-image {
  width: 98px;
  height: 134px;
  object-fit: cover;
  border: 1px solid #eee;
}

.avatar-placeholder {
  width: 98px;
  height: 134px;
  background-color: #f0f8ff;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5a9bd5;
  border: 1px dashed #add8e6;
}

.personal-details {
  flex: 1;
}

.name {
  margin: 0 0 15px 0;
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.contact-info {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.contact-info p {
  margin: 0;
  font-size: 14px;
  color: #333;
}

.icon {
  font-weight: bold;
  font-style: normal; /* 去除斜体 */
  margin-right: 5px;
}

.section-title {
  font-size: 20px;
  color: #333;
  border-left: 4px solid #409eff;
  padding-left: 10px;
  margin: 0 0 15px 0;
}

.section-content h3 {
  margin: 0 0 5px 0;
  color: #333;
}

.section-content p {
  margin: 5px 0;
  color: #666;
  line-height: 1.5;
}

.form-container {
  width: 400px;
  background: #f8fbff;
  border-radius: 8px;
  padding: 20px;
  height: fit-content;
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  box-sizing: border-box;
  border: 1px solid #e1eeff;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.action-btn {
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.reset-btn {
  grid-column: span 2;
  background-color: #909399;
  color: white;
}

.reset-btn:hover {
  background-color: #a6a9ad;
}

.pdf-btn {
  background-color: #f56c6c;
  color: white;
}

.pdf-btn:hover {
  background-color: #ff4d4f;
}

.word-btn {
  background-color: #409eff;
  color: white;
}

.word-btn:hover {
  background-color: #1890ff;
}

.form-section {
  margin-bottom: 25px;
}

.form-section h2 {
  font-size: 18px;
  margin: 0 0 15px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e1eeff;
  color: #2c5aa0;
}

.form-subsection {
  background: #f0f8ff;
  padding: 15px;
  border-radius: 6px;
  margin-bottom: 15px;
  border: 1px solid #d0e6ff;
}

.form-row {
  display: flex;
  gap: 15px;
}

.form-group {
  flex: 1;
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #2c5aa0;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d0e6ff;
  border-radius: 4px;
  background: #ffffff;
  color: #333333;
  font-size: 14px;
  box-sizing: border-box;
  box-shadow: inset 0 1px 2px rgba(0, 0, 0, 0.05);
}

.form-group textarea {
  min-height: 80px;
  resize: vertical;
}

.add-btn,
.remove-btn,
.upload-btn,
.remove-avatar-btn {
  width: 100%;
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  margin-top: 5px;
  box-sizing: border-box;
}

.add-btn {
  background-color: #409eff;
  color: white;
}

.remove-btn,
.remove-avatar-btn {
  background-color: #f56c6c;
  color: white;
}

.upload-btn {
  background-color: #67c23a;
  color: white;
}

.avatar-preview {
  margin-top: 10px;
}

.preview-image {
  width: 98px;
  height: 134px;
  object-fit: cover;
  border-radius: 2px;
}

/* 暗黑模式适配 */
html.dark .resume-builder {
  background-color: #2a2a3a; /* 紫灰色背景 */
  color: #e0e0e0;
}

html.dark .form-container {
  background: #383848;
  border: 1px solid #5a5a6a;
}

.action-buttons {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.action-btn {
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.reset-btn {
  grid-column: span 2;
  background-color: #909399;
  color: white;
}

.reset-btn:hover {
  background-color: #a6a9ad;
}

.pdf-btn {
  background-color: #f56c6c;
  color: white;
}

.pdf-btn:hover {
  background-color: #ff4d4f;
}

.word-btn {
  background-color: #409eff;
  color: white;
}

.word-btn:hover {
  background-color: #1890ff;
}

html.dark .action-btn {
  background-color: #454555;
  color: #e0e0e0;
  border: 1px solid #5a5a6a;
}

html.dark .reset-btn {
  background-color: #5a5a6a;
  color: #e0e0e0;
}

html.dark .reset-btn:hover {
  background-color: #6a6a7a;
}

html.dark .pdf-btn {
  background-color: #5c3a3a;
  color: #f8d4d4;
}

html.dark .pdf-btn:hover {
  background-color: #7a4d4d;
}

html.dark .word-btn {
  background-color: #3a4a66;
  color: #d4e0f8;
}

html.dark .word-btn:hover {
  background-color: #4d638c;
}

html.dark .form-section h2 {
  border-bottom: 1px solid #5a5a6a;
  color: #e0e0e0;
}

html.dark .form-subsection {
  background: #454555;
  border: 1px solid #5a5a6a;
}

html.dark .form-group label {
  color: #e0e0e0;
}

html.dark .form-group input,
html.dark .form-group select,
.html.dark .form-group textarea {
  background: #404050;
  color: #e0e0e0;
  border: 1px solid #5a5a6a;
}

html.dark .name,
html.dark .section-title,
html.dark .section-content h3,
html.dark .contact-info p {
  color: #333; /* 保持黑色文字 */
}

html.dark .section-content p {
  color: #666; /* 保持灰色文字 */
}

html.dark .personal-info {
  border-bottom: 1px solid #5a5a6a;
}

html.dark .avatar-placeholder {
  background-color: #454555;
  color: #b0b0c0;
  border: 1px dashed #5a5a6a;
}

/* 滚动条样式 */
.form-container::-webkit-scrollbar {
  width: 6px;
}

.form-container::-webkit-scrollbar-track {
  background: #f8fbff;
}

.form-container::-webkit-scrollbar-thumb {
  background: #d0e6ff;
  border-radius: 3px;
}

html.dark .form-container::-webkit-scrollbar-track {
  background: #383848;
}

html.dark .form-container::-webkit-scrollbar-thumb {
  background: #5a5a6a;
  border-radius: 3px;
}
</style>
