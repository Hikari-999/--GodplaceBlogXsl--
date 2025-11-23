<script setup>
import { ref, onMounted, computed } from 'vue'
import { get, post } from '@/net'
import { useUserStore } from '@/stores/user_store.js'
import { ElNotification, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

// 通知数据
const notifications = ref([])
// 控制发布表单显示
const showPublishForm = ref(false)
// 控制更新表单显示
const showUpdateForm = ref(false)
// 控制成功提示显示
const showSuccessMessage = ref(false)

// 分页相关
const currentPage = ref(1)
const itemsPerPage = ref(5)

// 发布表单数据
const publishForm = ref({
  versionId: '',
  title: '',
  content: '',
  adminName: userStore.username || '',
  role: userStore.role || 'admin'
})

// 更新表单数据
const updateForm = ref({
  id: null,
  versionId: '',
  title: '',
  content: '',
  adminName: userStore.username || '',
  role: userStore.role || 'admin'
})

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

// 从后端获取通知数据
const fetchNotifications = () => {
  get('/notification/show', (data) => {
    // 将数据倒序
    const reversedData = data.reverse();

    notifications.value = reversedData.map(item => ({
      id: item.id,
      version: item.versionId,
      updateTime: formatDate(item.createTime),
      title: item.title,
      content: item.content,
      publisher: item.adminName
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

// 发布新通知
const publishNotification = () => {
  // 确保所有必需字段都包含在内
  const formData = {
    versionId: publishForm.value.versionId,
    title: publishForm.value.title,
    content: publishForm.value.content,
    adminName: publishForm.value.adminName,
    role: publishForm.value.role
  }

  post('/notification/modify', formData, () => {
    // 重置表单
    publishForm.value = {
      versionId: '',
      title: '',
      content: '',
      adminName: userStore.username || '',
      role: userStore.role || 'admin'
    }

    showPublishForm.value = false
    fetchNotifications() // 重新获取通知列表

    // 显示成功提示
    showSuccessMessage.value = true
    setTimeout(() => {
      showSuccessMessage.value = false
    }, 3000)
  }, (message) => {
    ElNotification({
      title: '发布失败',
      message: message,
      type: 'error',
      duration: 3000
    })
  })
}

// 打开更新表单
const openUpdateForm = (notification) => {
  updateForm.value = {
    id: notification.id,
    versionId: notification.version,
    title: notification.title,
    content: notification.content,
    adminName: notification.publisher || userStore.username || '',
    role: userStore.role || 'admin'
  }
  showUpdateForm.value = true
}

// 更新通知
const updateNotification = () => {
  const formData = {
    versionId: updateForm.value.versionId,
    title: updateForm.value.title,
    content: updateForm.value.content,
    adminName: updateForm.value.adminName,
    role: updateForm.value.role
  }

  post(`/notification/update?id=${updateForm.value.id}`, formData, () => {
    // 重置表单
    updateForm.value = {
      id: null,
      versionId: '',
      title: '',
      content: '',
      adminName: userStore.username || '',
      role: userStore.role || 'admin'
    }

    showUpdateForm.value = false
    fetchNotifications() // 重新获取通知列表

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

// 删除通知
const deleteNotification = (id) => {
  ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const params = new URLSearchParams();
    params.append('id', id);
    params.append('role', userStore.role || 'admin');

    post('/notification/delete', params, (data) => {
      fetchNotifications() // 重新获取通知列表
      ElNotification({
        title: '删除成功',
        message: '通知已成功删除',
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
  fetchNotifications()
  // 确保表单中的信息是最新的
  publishForm.value.adminName = userStore.username || ''
  publishForm.value.role = userStore.role || 'admin'
  updateForm.value.adminName = userStore.username || ''
  updateForm.value.role = userStore.role || 'admin'
})
</script>


<template>
  <div class="notification-management">
    <div class="header">
      <h2>通知管理</h2>
      <button class="publish-button" @click="showPublishForm = true">
        + 发布通知
      </button>
    </div>

    <div class="notification-list">
      <div class="notification-card" v-for="notification in paginatedNotifications" :key="notification.id">
        <div class="card-header">
          <span class="version-tag">{{ notification.version }}</span>
          <span class="update-time">{{ notification.updateTime }}</span>
        </div>
        <div class="card-body">
          <h3>{{ notification.title }}</h3>
          <p>{{ notification.content }}</p>
        </div>
        <div class="card-footer" v-if="notification.publisher">
          <span>发布人: {{ notification.publisher }}</span>
        </div>
        <div class="card-actions">
          <button class="update-button" @click="openUpdateForm(notification)">更新</button>
          <button class="delete-button" @click="deleteNotification(notification.id)">删除</button>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="notifications.length === 0" class="empty-state">
        <p>暂无通知</p>
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

    <!-- 发布通知表单 -->
    <div v-if="showPublishForm" class="modal-overlay" @click="showPublishForm = false">
      <div class="modal-content" @click.stop>
        <h2>发布通知</h2>
        <form @submit.prevent="publishNotification">
          <div class="form-group">
            <label for="versionId">版本号:</label>
            <input type="text" id="versionId" v-model="publishForm.versionId" required>
          </div>
          <div class="form-group">
            <label for="title">标题:</label>
            <input type="text" id="title" v-model="publishForm.title" required>
          </div>
          <div class="form-group">
            <label for="content">内容:</label>
            <textarea id="content" v-model="publishForm.content" required></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="showPublishForm = false">取消</button>
            <button type="submit">发布</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 更新通知表单 -->
    <div v-if="showUpdateForm" class="modal-overlay" @click="showUpdateForm = false">
      <div class="modal-content" @click.stop>
        <h2>更新通知</h2>
        <form @submit.prevent="updateNotification">
          <div class="form-group">
            <label for="update-versionId">版本号:</label>
            <input type="text" id="update-versionId" v-model="updateForm.versionId" required>
          </div>
          <div class="form-group">
            <label for="update-title">标题:</label>
            <input type="text" id="update-title" v-model="updateForm.title" required>
          </div>
          <div class="form-group">
            <label for="update-content">内容:</label>
            <textarea id="update-content" v-model="updateForm.content" required></textarea>
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
        <p>通知已成功处理</p>
        <button @click="showSuccessMessage = false">确定</button>
      </div>
    </div>
  </div>
</template>


<style scoped>
.notification-management {
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

.publish-button {
  padding: 8px 16px;
  background-color: #409eff;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

.publish-button:hover {
  background-color: #66b1ff;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notification-card {
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
}

.notification-card:hover {
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

.version-tag {
  background-color: #409eff;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.update-time {
  color: #909399;
  font-size: 13px;
}

.card-body h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.card-body p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.card-footer {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  text-align: right;
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
  padding: 10px 15px; /* 增加内边距 */
  border: 1px solid #dcdfe6; /* 边框颜色 */
  border-radius: 4px; /* 圆角 */
  font-size: 14px; /* 字体大小 */
  background-color: #ffffff; /* 背景色调整为白色 */
  color: #303133; /* 文字颜色 */
  box-sizing: border-box; /* 确保 padding 不影响宽度 */
  transition: border-color 0.3s; /* 过渡效果 */
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409eff;
}

.form-group textarea {
  height: 120px;
  resize: vertical;
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
  .notification-management {
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

  .card-actions {
    flex-direction: column;
  }

  .card-actions button {
    width: 100%;
  }
}
</style>

