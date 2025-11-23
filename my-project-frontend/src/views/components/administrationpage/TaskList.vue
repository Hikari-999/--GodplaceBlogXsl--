<script setup>
import { ref, onMounted, computed } from 'vue';
import { post } from '@/net';
import { ElNotification, ElMessageBox } from 'element-plus';
import { useUserStore } from '@/stores/user_store.js';

const userStore = useUserStore();

const activeTab = ref('ideas');
const showFeedbackForm = ref(false);
const showAddTaskForm = ref(false);
const showSuccessMessage = ref(false);
const showUpdateTaskForm = ref(false);
const showUpdateUserTaskForm = ref(false);
const tasks = ref([]);
const tasksByUser = ref([]);

// 分页相关状态
const taskCurrentPage = ref(1);
const userTaskCurrentPage = ref(1);
const itemsPerPage = ref(5);

// 反馈表单数据
const feedbackForm = ref({
  titleByuser: '',
  descriptionByuser: '',
  username: userStore.username || ''
});

// 新增任务表单数据
const newTaskForm = ref({
  title: '',
  description: '',
  status: 0,
  role: userStore.role || '' // 添加 role 字段
});

// 更新任务表单数据
const updateTaskForm = ref({
  id: null,
  title: '',
  description: '',
  status: 0,
  role: userStore.role || ''
});

// 用户建议更新表单数据
const updateUserTaskForm = ref({
  id: null,
  titleByuser: '',
  descriptionByuser: '',
  username: '',
  role: userStore.role || ''
});

// 计算分页数据
const totalTaskPages = computed(() => {
  return Math.ceil(tasks.value.length / itemsPerPage.value);
});

const totalUserTaskPages = computed(() => {
  return Math.ceil(tasksByUser.value.length / itemsPerPage.value);
});

const paginatedTasks = computed(() => {
  const startIndex = (taskCurrentPage.value - 1) * itemsPerPage.value;
  const endIndex = startIndex + itemsPerPage.value;
  return tasks.value.slice(startIndex, endIndex);
});

const paginatedUserTasks = computed(() => {
  const startIndex = (userTaskCurrentPage.value - 1) * itemsPerPage.value;
  const endIndex = startIndex + itemsPerPage.value;
  return tasksByUser.value.slice(startIndex, endIndex);
});

const taskPageNumbers = computed(() => {
  return Array.from({ length: totalTaskPages.value }, (_, i) => i + 1);
});

const userTaskPageNumbers = computed(() => {
  return Array.from({ length: totalUserTaskPages.value }, (_, i) => i + 1);
});

// 获取任务列表
const fetchTasks = () => {
  post('/blog/getTask', {}, (data) => {
    tasks.value = data.map(task => ({
      id: task.id,
      title: task.title,
      description: task.description,
      status: task.status || 0
    }));
    taskCurrentPage.value = 1;
  });
};

// 获取用户建议列表
const fetchTasksByUser = () => {
  post('/blog/getTaskByUser', {}, (data) => {
    tasksByUser.value = data.map(task => ({
      id: task.id,
      titleByuser: task.titleByuser,
      descriptionByuser: task.descriptionByuser,
      username: task.username
    }));
    userTaskCurrentPage.value = 1;
  });
};

// 根据状态值获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0:
      return '未开始';
    case 1:
      return '进行中';
    case 2:
      return '已完成';
    default:
      return '未知';
  }
};

// 根据状态值获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0:
      return 'status-not-started';
    case 1:
      return 'status-in-progress';
    case 2:
      return 'status-completed';
    default:
      return '';
  }
};

// 提交用户反馈
const submitFeedback = () => {
  const formData = {
    titleByuser: feedbackForm.value.titleByuser,
    descriptionByuser: feedbackForm.value.descriptionByuser,
    username: feedbackForm.value.username
  };

  post('/blog/setTaskByUser', formData, (data) => {
    feedbackForm.value = {
      titleByuser: '',
      descriptionByuser: '',
      username: userStore.username || ''
    };
    showFeedbackForm.value = false;
    fetchTasksByUser();

    showSuccessMessage.value = true;
    setTimeout(() => {
      showSuccessMessage.value = false;
    }, 3000);
  }, (message, code) => {
    console.error('提交失败:', message, '错误码:', code);
    ElNotification({
      title: '提交失败',
      message: message,
      type: 'error',
      duration: 3000
    });
  });
};

// 新增任务
const addNewTask = () => {
  // 确保发送 role 字段
  const formData = {
    title: newTaskForm.value.title,
    description: newTaskForm.value.description,
    status: newTaskForm.value.status,
    role: userStore.role || 'admin' // 确保发送 role 字段
  };

  post('/blog/setTask', formData, (data) => {
    newTaskForm.value = {
      title: '',
      description: '',
      status: 0,
      role: userStore.role || 'admin'
    };
    showAddTaskForm.value = false;
    fetchTasks();

    showSuccessMessage.value = true;
    setTimeout(() => {
      showSuccessMessage.value = false;
    }, 3000);
  }, (message, code) => {
    console.error('添加失败:', message, '错误码:', code);
    ElNotification({
      title: '添加失败',
      message: message,
      type: 'error',
      duration: 3000
    });
  });
};

// 打开更新任务表单
const openUpdateTaskForm = (task) => {
  updateTaskForm.value = {
    id: task.id,
    title: task.title,
    description: task.description,
    status: task.status,
    role: userStore.role || 'admin'
  };
  showUpdateTaskForm.value = true;
};

// 更新任务
const updateTask = () => {
  const formData = {
    id: updateTaskForm.value.id,
    title: updateTaskForm.value.title,
    description: updateTaskForm.value.description,
    status: updateTaskForm.value.status,
    role: userStore.role || 'admin'
  };

  post('/blog/updateTask', formData, (data) => {
    updateTaskForm.value = {
      id: null,
      title: '',
      description: '',
      status: 0,
      role: userStore.role || 'admin'
    };
    showUpdateTaskForm.value = false;
    fetchTasks();

    showSuccessMessage.value = true;
    setTimeout(() => {
      showSuccessMessage.value = false;
    }, 3000);
  }, (message, code) => {
    console.error('更新失败:', message, '错误码:', code);
    ElNotification({
      title: '更新失败',
      message: message,
      type: 'error',
      duration: 3000
    });
  });
};

// 删除任务
const deleteTask = (taskId) => {
  ElMessageBox.confirm('确定要删除这个任务吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 使用正确的参数格式发送请求
    const params = new URLSearchParams();
    params.append('id', taskId);
    params.append('role', userStore.role || 'admin');

    post('/blog/deleteTask', params, (data) => {
      fetchTasks();
      ElNotification({
        title: '删除成功',
        message: '任务已成功删除',
        type: 'success',
        duration: 3000
      });
    }, (message, code) => {
      console.error('删除失败:', message, '错误码:', code);
      ElNotification({
        title: '删除失败',
        message: message,
        type: 'error',
        duration: 3000
      });
    });
  }).catch(() => {
    // 用户取消删除
  });
};

// 打开更新用户建议表单
const openUpdateUserTaskForm = (task) => {
  updateUserTaskForm.value = {
    id: task.id,
    titleByuser: task.titleByuser,
    descriptionByuser: task.descriptionByuser,
    username: task.username,
    role: userStore.role || ''
  };
  showUpdateUserTaskForm.value = true;
};

// 更新用户建议
const updateUserTask = () => {
  const formData = {
    id: updateUserTaskForm.value.id,
    titleByuser: updateUserTaskForm.value.titleByuser,
    descriptionByuser: updateUserTaskForm.value.descriptionByuser,
    username: updateUserTaskForm.value.username,
    role: userStore.role || ''
  };

  post('/blog/updateTask', formData, (data) => {
    updateUserTaskForm.value = {
      id: null,
      titleByuser: '',
      descriptionByuser: '',
      username: '',
      role: userStore.role || ''
    };
    showUpdateUserTaskForm.value = false;
    fetchTasksByUser();

    showSuccessMessage.value = true;
    setTimeout(() => {
      showSuccessMessage.value = false;
    }, 3000);
  }, (message, code) => {
    console.error('更新失败:', message, '错误码:', code);
    ElNotification({
      title: '更新失败',
      message: message,
      type: 'error',
      duration: 3000
    });
  });
};

// 删除用户建议
const deleteUserTask = (taskId) => {
  ElMessageBox.confirm('确定要删除这个建议吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 使用正确的参数格式发送请求
    const params = new URLSearchParams();
    params.append('id', taskId);
    params.append('role', userStore.role || '');

    post('/blog/deleteTask', params, (data) => {
      fetchTasksByUser();
      ElNotification({
        title: '删除成功',
        message: '建议已成功删除',
        type: 'success',
        duration: 3000
      });
    }, (message, code) => {
      console.error('删除失败:', message, '错误码:', code);
      ElNotification({
        title: '删除失败',
        message: message,
        type: 'error',
        duration: 3000
      });
    });
  }).catch(() => {
    // 用户取消删除
  });
};

// 组件挂载时获取数据
onMounted(() => {
  fetchTasks();
  fetchTasksByUser();
  // 确保表单中的用户名是最新的
  feedbackForm.value.username = userStore.username || '';
  // 确保表单中的role是最新的
  newTaskForm.value.role = userStore.role || 'admin';
  updateTaskForm.value.role = userStore.role || 'admin';
});
</script>

<template>
  <div class="task-management">
    <!-- 按钮组 -->
    <div class="tab-buttons">
      <button
          :class="['tab-button', { active: activeTab === 'ideas' }]"
          @click="activeTab = 'ideas'"
      >
        Ideas
      </button>
      <button
          :class="['tab-button', { active: activeTab === 'feedback' }]"
          @click="activeTab = 'feedback'"
      >
        用户建议
      </button>
      <button
          v-if="activeTab === 'ideas'"
          class="add-task-button"
          @click="showAddTaskForm = true"
      >
        + 新增任务
      </button>
    </div>

    <!-- Ideas 页面 -->
    <div v-if="activeTab === 'ideas'" class="task-list">
      <div class="task-card" v-for="(task, index) in paginatedTasks" :key="index">
        <div class="task-header">
          <h3>{{ task.title }}</h3>
          <div class="task-id">ID: {{ task.id }}</div>
        </div>
        <p class="task-description">{{ task.description }}</p>
        <p class="task-status">
          <span :class="getStatusClass(task.status)">
            {{ getStatusText(task.status) }}
          </span>
        </p>
        <div class="task-actions">
          <button class="update-button" @click="openUpdateTaskForm(task)">更新</button>
          <button class="delete-button" @click="deleteTask(task.id)">删除</button>
        </div>
      </div>

      <!-- Ideas 分页导航 -->
      <div class="pagination" v-if="totalTaskPages > 1">
        <button
            class="page-button"
            :disabled="taskCurrentPage === 1"
            @click="taskCurrentPage--"
        >
          上一页
        </button>

        <button
            v-for="page in taskPageNumbers"
            :key="page"
            class="page-button"
            :class="{ active: taskCurrentPage === page }"
            @click="taskCurrentPage = page"
        >
          {{ page }}
        </button>

        <button
            class="page-button"
            :disabled="taskCurrentPage === totalTaskPages"
            @click="taskCurrentPage++"
        >
          下一页
        </button>
      </div>
    </div>

    <!-- Feedback 页面 -->
    <div v-else-if="activeTab === 'feedback'" class="feedback-content">
      <button class="add-feedback-button" @click="showFeedbackForm = true">
        + 添加建议
      </button>

      <div class="task-card" v-for="(task, index) in paginatedUserTasks" :key="index">
        <div class="task-header">
          <h3>{{ task.titleByuser }}</h3>
          <div class="task-id">ID: {{ task.id }}</div>
        </div>
        <p class="task-description">{{ task.descriptionByuser }}</p>
        <p class="task-user">用户: {{ task.username }}</p>
        <div class="task-actions">
          <button class="update-button" @click="openUpdateUserTaskForm(task)">更新</button>
          <button class="delete-button" @click="deleteUserTask(task.id)">删除</button>
        </div>
      </div>

      <!-- Feedback 分页导航 -->
      <div class="pagination" v-if="totalUserTaskPages > 1">
        <button
            class="page-button"
            :disabled="userTaskCurrentPage === 1"
            @click="userTaskCurrentPage--"
        >
          上一页
        </button>

        <button
            v-for="page in userTaskPageNumbers"
            :key="page"
            class="page-button"
            :class="{ active: userTaskCurrentPage === page }"
            @click="userTaskCurrentPage = page"
        >
          {{ page }}
        </button>

        <button
            class="page-button"
            :disabled="userTaskCurrentPage === totalUserTaskPages"
            @click="userTaskCurrentPage++"
        >
          下一页
        </button>
      </div>

      <!-- 添加建议的弹窗表单 -->
      <div v-if="showFeedbackForm" class="modal-overlay" @click="showFeedbackForm = false">
        <div class="modal-content" @click.stop>
          <h2>提供建议</h2>
          <form @submit.prevent="submitFeedback">
            <div class="form-group">
              <label for="title">标题:</label>
              <input type="text" id="title" v-model="feedbackForm.titleByuser" required>
            </div>
            <div class="form-group">
              <label for="description">描述:</label>
              <textarea id="description" v-model="feedbackForm.descriptionByuser" required></textarea>
            </div>
            <div class="form-group">
              <label for="username">姓名:</label>
              <input type="text" id="username" v-model="feedbackForm.username">
            </div>
            <div class="form-actions">
              <button type="button" @click="showFeedbackForm = false">取消</button>
              <button type="submit">提交</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 新增任务的弹窗表单 -->
    <div v-if="showAddTaskForm" class="modal-overlay" @click="showAddTaskForm = false">
      <div class="modal-content" @click.stop>
        <h2>新增任务</h2>
        <form @submit.prevent="addNewTask">
          <div class="form-group">
            <label for="task-title">标题:</label>
            <input type="text" id="task-title" v-model="newTaskForm.title" required>
          </div>
          <div class="form-group">
            <label for="task-description">描述:</label>
            <textarea id="task-description" v-model="newTaskForm.description" required></textarea>
          </div>
          <div class="form-group">
            <label for="task-status">状态:</label>
            <select id="task-status" v-model="newTaskForm.status">
              <option value="0">未开始</option>
              <option value="1">进行中</option>
              <option value="2">已完成</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" @click="showAddTaskForm = false">取消</button>
            <button type="submit">添加</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 更新任务的弹窗表单 -->
    <div v-if="showUpdateTaskForm" class="modal-overlay" @click="showUpdateTaskForm = false">
      <div class="modal-content" @click.stop>
        <h2>更新任务</h2>
        <form @submit.prevent="updateTask">
          <div class="form-group">
            <label for="update-task-id">任务ID:</label>
            <input type="text" id="update-task-id" v-model="updateTaskForm.id" readonly class="readonly-field">
          </div>
          <div class="form-group">
            <label for="update-task-title">标题:</label>
            <input type="text" id="update-task-title" v-model="updateTaskForm.title" required>
          </div>
          <div class="form-group">
            <label for="update-task-description">描述:</label>
            <textarea id="update-task-description" v-model="updateTaskForm.description" required></textarea>
          </div>
          <div class="form-group">
            <label for="update-task-status">状态:</label>
            <select id="update-task-status" v-model="updateTaskForm.status">
              <option value="0">未开始</option>
              <option value="1">进行中</option>
              <option value="2">已完成</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" @click="showUpdateTaskForm = false">取消</button>
            <button type="submit">更新</button>
          </div>
        </form>
      </div>
    </div>

    <!-- 更新用户建议的弹窗表单 -->
    <div v-if="showUpdateUserTaskForm" class="modal-overlay" @click="showUpdateUserTaskForm = false">
      <div class="modal-content" @click.stop>
        <h2>更新建议</h2>
        <form @submit.prevent="updateUserTask">
          <div class="form-group">
            <label for="update-user-task-id">建议ID:</label>
            <input type="text" id="update-user-task-id" v-model="updateUserTaskForm.id" readonly class="readonly-field">
          </div>
          <div class="form-group">
            <label for="update-user-task-title">标题:</label>
            <input type="text" id="update-user-task-title" v-model="updateUserTaskForm.titleByuser" required>
          </div>
          <div class="form-group">
            <label for="update-user-task-description">描述:</label>
            <textarea id="update-user-task-description" v-model="updateUserTaskForm.descriptionByuser" required></textarea>
          </div>
          <div class="form-actions">
            <button type="button" @click="showUpdateUserTaskForm = false">取消</button>
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
        <p>操作已成功完成</p>
        <button @click="showSuccessMessage = false">确定</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100%;
}

.tab-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  padding: 10px 0;
  border-bottom: 1px solid #e1e5eb;
}

.tab-button, .add-task-button, .add-feedback-button {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #ffffff;
  color: #606266;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.tab-button:hover, .add-task-button:hover, .add-feedback-button:hover {
  background-color: #f5f7fa;
  border-color: #c0c4cc;
}

.tab-button.active {
  background-color: #409eff;
  border-color: #409eff;
  color: #ffffff;
}

.add-task-button, .add-feedback-button {
  margin-left: auto;
}

.task-list, .feedback-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.task-card {
  background-color: #ffffff;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 16px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s;
  text-align: left;
}

.task-card:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.task-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.task-id {
  font-size: 12px;
  color: #909399;
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
}

.task-description {
  margin: 8px 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.task-status, .task-user {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #909399;
}

.status-not-started {
  color: #f56c6c;
  font-weight: 500;
}

.status-in-progress {
  color: #e6a23c;
  font-weight: 500;
}

.status-completed {
  color: #67c23a;
  font-weight: 500;
}

.task-actions {
  display: flex;
  gap: 10px;
  margin-top: 15px;
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

/* 输入框聚焦时的样式 */
.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: #409eff; /* 聚焦时边框颜色 */
}

.form-group textarea {
  height: 100px;
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
  .task-management {
    padding: 15px;
  }

  .tab-buttons {
    flex-wrap: wrap;
  }

  .add-task-button, .add-feedback-button {
    margin-left: 0;
    width: 100%;
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

  .task-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
}
.readonly-field {
  background-color: #f5f7fa;
  color: #909399;
  cursor: not-allowed;
  pointer-events: none;
}
</style>
