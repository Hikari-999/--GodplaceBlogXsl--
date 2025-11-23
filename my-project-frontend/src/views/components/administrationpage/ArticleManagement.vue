<template>
  <div class="article-management">
    <div class="header">
      <h2>文章管理</h2>
      <button class="add-button" @click="showAddForm = true">
        + 新增文章
      </button>
    </div>

    <div class="article-list">
      <div class="article-card" v-for="article in paginatedArticles" :key="article.articleId">
        <div class="card-header">
          <h3>{{ article.title }}</h3>
          <div class="article-id">ID: {{ article.articleId }}</div>
        </div>
        <div class="card-body">
          <p class="description">{{ article.description }}</p>
          <div class="tags">
            <span v-for="(tag, index) in article.tags" :key="index" class="tag">{{ tag }}</span>
          </div>
          <div class="stats">
            <span>阅读: {{ article.readNum }}</span>
            <span>收藏: {{ article.star }}</span>
          </div>
        </div>
        <div class="card-footer">
          <span>创建时间: {{ formatDate(article.startTime) }}</span>
          <span>更新时间: {{ formatDate(article.updateTime) }}</span>
        </div>
        <div class="card-actions">
          <button class="update-button" @click="openUpdateForm(article)">更新</button>
          <button class="delete-button" @click="deleteArticle(article.articleId)">删除</button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="articles.length === 0" class="empty-state">
        <p>暂无文章</p>
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

    <!-- 新增文章表单 -->
    <div v-if="showAddForm" class="modal-overlay" @click="showAddForm = false">
      <div class="modal-content" @click.stop>
        <form @submit.prevent="addArticle">
          <div class="form-group">
            <label for="add-title">标题:</label>
            <input type="text" id="add-title" v-model="addForm.title" required>
          </div>
          <div class="form-group">
            <label for="add-description">描述:</label>
            <textarea id="add-description" v-model="addForm.description" required></textarea>
          </div>
          <div class="form-group">
            <label for="add-tags">标签 (用逗号分隔):</label>
            <input type="text" id="add-tags" v-model="tagInput" placeholder="例如: 技术,Java,Vue">
          </div>
          <!-- 添加文件上传字段 -->
          <div class="form-group">
            <label for="add-markdown-file">Markdown文件:</label>
            <input
                type="file"
                id="add-markdown-file"
                @change="handleFileUpload"
                accept=".md"
                required
            />
            <div class="file-info" v-if="addForm.fileName">
              已选择文件: {{ addForm.fileName }}
            </div>
          </div>
          <div class="form-actions">
            <button type="button" @click="showAddForm = false">取消</button>
            <button type="submit">新增</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 更新文章表单 -->
    <div v-if="showUpdateForm" class="modal-overlay" @click="showUpdateForm = false">
      <div class="modal-content" @click.stop>
        <form @submit.prevent="updateArticle">
          <div class="form-group">
            <label for="update-article-id">文章ID:</label>
            <input type="text" id="update-article-id" v-model="updateForm.articleId" readonly class="readonly-field">
          </div>
          <div class="form-group">
            <label for="update-title">标题:</label>
            <input type="text" id="update-title" v-model="updateForm.title" required>
          </div>
          <div class="form-group">
            <label for="update-description">描述:</label>
            <textarea id="update-description" v-model="updateForm.description" required></textarea>
          </div>
          <div class="form-group">
            <label for="update-tags">标签 (用逗号分隔):</label>
            <input type="text" id="update-tags" v-model="updateTagInput" placeholder="例如: 技术,Java,Vue">
          </div>
          <div class="form-actions">
            <button type="button" @click="showUpdateForm = false">取消</button>
            <button type="submit">更新</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 成功提示 -->
    <div v-if="showSuccessMessage" class="modal-overlay" @click="showSuccessMessage = false">
      <div class="success-modal" @click.stop>
        <div class="success-icon">✓</div>
        <h3>操作成功!</h3>
        <p>文章已成功处理</p>
        <button @click="showSuccessMessage = false">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { get, post } from '@/net/index.js'
import { useUserStore } from '@/stores/user_store.js'
import { ElNotification, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

// 文章数据
const articles = ref([])
// 控制新增表单显示
const showAddForm = ref(false)
// 控制更新表单显示
const showUpdateForm = ref(false)
// 控制成功提示显示
const showSuccessMessage = ref(false)

// 分页相关
const currentPage = ref(1)
const itemsPerPage = ref(5)

// 新增表单数据
const addForm = ref({
  title: '',
  description: '',
  role: userStore.role || 'admin',
  markdownFile: null,
  fileName: ''
})

// 标签输入（用于新增）
const tagInput = ref('')

// 更新表单数据
const updateForm = ref({
  articleId: null,
  title: '',
  description: '',
  role: userStore.role || 'admin'
})

// 标签输入（用于更新）
const updateTagInput = ref('')

// 计算总页数
const totalPages = computed(() => {
  return Math.ceil(articles.value.length / itemsPerPage.value)
})

// 计算当前页显示的文章
const paginatedArticles = computed(() => {
  const startIndex = (currentPage.value - 1) * itemsPerPage.value
  const endIndex = startIndex + itemsPerPage.value
  return articles.value.slice(startIndex, endIndex)
})

// 计算当前页码数组
const pageNumbers = computed(() => {
  return Array.from({ length: totalPages.value }, (_, i) => i + 1)
})

// 监听标签输入变化，更新表单中的标签数组
watch(tagInput, (newVal) => {
  // 这里我们不需要直接更新表单中的tags字段，因为提交时会处理
})

watch(updateTagInput, (newVal) => {
  // 这里我们不需要直接更新表单中的tags字段，因为提交时会处理
})

// 从后端获取文章数据
const fetchArticles = () => {
  get('/article-base-info/getArticleList', (data) => {
    articles.value = data.map(item => ({
      articleId: item.articleId,
      title: item.title,
      description: item.description,
      tags: item.tags || [],
      readNum: item.readNum || 0,
      star: item.star || 0,
      startTime: item.startTime,
      updateTime: item.updateTime
    }))
    currentPage.value = 1
  })
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 处理文件上传
const handleFileUpload = (event) => {
  const file = event.target.files[0]
  if (file && file.name.endsWith('.md')) {
    addForm.value.markdownFile = file
    addForm.value.fileName = file.name
  } else if (file) {
    ElNotification({
      title: '文件类型错误',
      message: '请选择.md格式的文件',
      type: 'error',
      duration: 3000
    })
    event.target.value = '' // 清空文件选择
    addForm.value.markdownFile = null
    addForm.value.fileName = ''
  }
}

// 新增文章
const addArticle = () => {
  // 处理标签
  const tags = tagInput.value.split(',').map(tag => tag.trim()).filter(tag => tag !== '')

  // 检查是否选择了文件
  if (!addForm.value.markdownFile) {
    ElNotification({
      title: '文件缺失',
      message: '请选择要上传的Markdown文件',
      type: 'error',
      duration: 3000
    })
    return
  }

  // 创建FormData对象用于文件上传
  const formData = new FormData()
  formData.append('title', addForm.value.title)
  formData.append('description', addForm.value.description)
  formData.append('tags', JSON.stringify(tags))
  formData.append('role', addForm.value.role)
  formData.append('file', addForm.value.markdownFile)
  formData.append('fileName', addForm.value.fileName)

  post('/article-base-info/addWithFile', formData, () => {
    // 重置表单
    addForm.value = {
      title: '',
      description: '',
      role: userStore.role || 'admin',
      markdownFile: null,
      fileName: ''
    }
    tagInput.value = ''

    // 清空文件输入框
    const fileInput = document.getElementById('add-markdown-file')
    if (fileInput) fileInput.value = ''

    showAddForm.value = false
    fetchArticles() // 重新获取文章列表

    // 显示成功提示
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  }, (message) => {
    ElNotification({
      title: '新增失败',
      message: message,
      type: 'error',
      duration: 3000
    })
  })
}

// 打开更新表单
const openUpdateForm = (article) => {
  updateForm.value = {
    articleId: article.articleId,
    title: article.title,
    description: article.description,
    role: userStore.role || 'admin'
  }
  // 设置标签输入框的值
  updateTagInput.value = article.tags ? article.tags.join(', ') : ''
  showUpdateForm.value = true
}

// 更新文章
const updateArticle = () => {
  // 处理标签
  const tags = updateTagInput.value.split(',').map(tag => tag.trim()).filter(tag => tag !== '')

  // 构造请求体数据，确保与后端 ArticleBaseInfoRequestVO 类匹配
  const requestBody = {
    articleId: parseInt(updateForm.value.articleId),
    title: updateForm.value.title,
    description: updateForm.value.description,
    tags: tags,
    role: updateForm.value.role
  }

  // 发送请求
  post('/article-base-info/update', requestBody, () => {
    // 重置表单
    updateForm.value = {
      articleId: null,
      title: '',
      description: '',
      role: userStore.role || 'admin'
    }
    updateTagInput.value = ''

    showUpdateForm.value = false
    fetchArticles() // 重新获取文章列表

    // 显示成功提示
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  }, (message) => {
    ElNotification({
      title: '更新失败',
      message: message,
      type: 'error',
      duration: 3000
    })
  })
}

// 删除文章
const deleteArticle = (articleId) => {
  ElMessageBox.confirm('确定要删除这篇文章吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const params = new URLSearchParams();
    params.append('id', articleId);
    params.append('role', userStore.role || 'admin');

    post('/article-base-info/delete', params, (data) => {
      fetchArticles() // 重新获取文章列表
      ElNotification({
        title: '删除成功',
        message: '文章已成功删除',
        type: 'success',
        duration: 3000
      })
    }, (message) => {
      ElNotification({
        title: '删除失败',
        message: message,
        type: 'error',
        duration: 3000
      })
    })
  }).catch(() => {
    // 用户取消删除
  })
}

// 切换页面
const goToPage = (page) => {
  if (page >= 1 && page <= totalPages.value) {
    currentPage.value = page
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchArticles()
  // 确保表单中的信息是最新的
  addForm.value.role = userStore.role || 'admin'
  updateForm.value.role = userStore.role || 'admin'
})
</script>

<style scoped>
.article-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e1e5eb;
}

.header h2 {
  margin: 0;
  color: #303133;
  font-size: 20px;
  font-weight: 600;
}

.add-button {
  padding: 8px 16px;
  background-color: #409eff;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.add-button:hover {
  background-color: #66b1ff;
}

.article-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.article-card {
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.article-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.article-id {
  font-size: 12px;
  color: #909399;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.card-body .description {
  margin: 0 0 15px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.tags {
  margin-bottom: 15px;
}

.tag {
  display: inline-block;
  background-color: #ecf5ff;
  color: #409eff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  margin-right: 8px;
  margin-bottom: 4px;
}

.stats {
  display: flex;
  gap: 15px;
  font-size: 13px;
  color: #909399;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  color: #909399;
  font-size: 13px;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.update-button, .delete-button {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.update-button {
  border: 1px solid #409eff;
  background-color: #ffffff;
  color: #409eff;
}

.update-button:hover {
  background-color: #ecf5ff;
}

.delete-button {
  border: 1px solid #f56c6c;
  background-color: #ffffff;
  color: #f56c6c;
}

.delete-button:hover {
  background-color: #fef0f0;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin: 20px 0;
}

.page-button {
  padding: 6px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
  color: #606266;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s;
}

.page-button:hover:not(:disabled) {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

.page-button.active {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

.page-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content, .success-modal {
  background-color: #ffffff;
  border-radius: 4px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 20px;
  width: 90%;
  max-width: 500px;
}

.success-modal {
  text-align: center;
  padding: 30px;
}

.success-icon {
  width: 50px;
  height: 50px;
  background-color: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  font-size: 24px;
  color: white;
}

.success-modal h3 {
  margin: 0 0 10px 0;
  color: #303133;
}

.success-modal p {
  margin: 0 0 20px 0;
  color: #606266;
}

.success-modal button {
  padding: 8px 20px;
  background-color: #409eff;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 14px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

/* 通用表单样式 */
.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px 15px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 14px;
  background-color: #ffffff;
  color: #303133;
  box-sizing: border-box;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409eff;
}

.form-group textarea {
  height: 100px;
  resize: vertical;
}

.readonly-field {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
}

.file-info {
  margin-top: 5px;
  font-size: 12px;
  color: #67c23a;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.form-actions button {
  padding: 8px 20px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.form-actions button[type="button"] {
  border: 1px solid #dcdfe6;
  background-color: #ffffff;
  color: #606266;
}

.form-actions button[type="button"]:hover {
  background-color: #f5f7fa;
}

.form-actions button[type="submit"] {
  border: 1px solid #409eff;
  background-color: #409eff;
  color: #ffffff;
}

.form-actions button[type="submit"]:hover {
  background-color: #66b1ff;
}

@media (max-width: 768px) {
  .article-management {
    padding: 15px;
  }

  .header {
    flex-direction: column;
    gap: 15px;
    align-items: flex-start;
  }

  .modal-content, .success-modal {
    width: 95%;
    padding: 15px;
  }

  .form-actions {
    flex-direction: column;
  }

  .form-actions button {
    width: 100%;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }

  .card-footer {
    flex-direction: column;
    gap: 5px;
  }

  .card-actions {
    flex-direction: column;
  }

  .card-actions button {
    width: 100%;
  }

  .stats {
    flex-direction: column;
    gap: 5px;
  }
}
</style>
