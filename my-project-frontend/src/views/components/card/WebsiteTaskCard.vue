<template>
  <div class="task-detail">
    <!-- 新增的按钮组 -->
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
        来点建议啊烙铁(显示上限5条)
      </button>
    </div>

    <!-- 根据 activeTab 显示不同的内容 -->
    <div v-if="activeTab === 'ideas'" class="task-list">
      <div class="task-card" v-for="(task, index) in paginatedTasks" :key="index">
        <h3>{{ task.title }}</h3>
        <p><strong>任务：</strong>{{ task.description }}</p>
        <p><strong>状态：</strong><span :class="getStatusClass(task.status)">{{ getStatusText(task.status) }}</span></p>
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

    <div v-else-if="activeTab === 'feedback'" class="feedback-content">
      <button class="add-feedback-button" @click="showFeedbackForm = true">
        + 添加建议
      </button>

      <div class="task-card" v-for="(task, index) in paginatedUserTasks" :key="index">
        <h3>{{ task.titleByuser }}</h3>
        <p><strong>任务：</strong>{{ task.descriptionByuser }}</p>
        <p><strong>姓名：</strong>{{ task.username }}</p>
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
      <div v-if="showFeedbackForm" class="feedback-form-overlay" @click="showFeedbackForm = false">
        <div class="feedback-form" @click.stop>
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
              <label for="username">姓名 (可选):</label>
              <input type="text" id="username" v-model="feedbackForm.username">
            </div>
            <div class="form-actions">
              <button type="button" @click="showFeedbackForm = false">取消</button>
              <button type="submit">提交</button>
            </div>
          </form>
        </div>
      </div>

      <!-- 成功提示 -->
      <div v-if="showSuccessMessage" class="success-message" @click="showSuccessMessage = false">
        <div class="success-content" @click.stop>
          <div class="success-icon">✓</div>
          <h3>添加成功!</h3>
          <p>您的建议已成功提交</p>
          <button @click="showSuccessMessage = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { post } from '@/net';
import { ElNotification } from 'element-plus';
import { useUserStore } from '@/stores/user_store.js';

const userStore = useUserStore();

console.log(userStore.username)
const activeTab = ref('ideas'); // 默认显示 "Ideas" 页面
const showFeedbackForm = ref(false); // 控制反馈表单显示
const tasks = ref([]);
const showSuccessMessage = ref(false); // 控制成功提示显示

// 分页相关状态
const taskCurrentPage = ref(1);
const userTaskCurrentPage = ref(1);
const itemsPerPage = ref(5); // 每页显示3个项目

// 反馈表单数据
const feedbackForm = ref({
  titleByuser: '',
  descriptionByuser: '',
  username: userStore.username || ''
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
      title: task.title,
      description: task.description,
      status: task.status || 0 // 默认状态为0（未开始）
    }));
    // 重置页码
    taskCurrentPage.value = 1;
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
      return 'not-started';
    case 1:
      return 'in-progress';
    case 2:
      return 'completed';
    default:
      return '';
  }
};

//聆听您的意见实现
const tasksByUser = ref([]);

// 获取任务列表
const fetchTasksByUser = () => {
  post('/blog/getTaskByUser', {}, (data) => {
    tasksByUser.value = data.map(task => ({
      titleByuser: task.titleByuser,
      descriptionByuser: task.descriptionByuser,
      username: task.username
    }));
    // 重置页码
    userTaskCurrentPage.value = 1;
  });
};

// 提交用户反馈
const submitFeedback = () => {
  // 创建一个新对象，确保数据格式正确
  const formData = {
    titleByuser: feedbackForm.value.titleByuser,
    descriptionByuser: feedbackForm.value.descriptionByuser,
    username: feedbackForm.value.username
  };

  post('/blog/setTaskByUser', formData, (data) => {
    // 提交成功后重置表单并刷新列表
    feedbackForm.value = {
      titleByuser: '',
      descriptionByuser: '',
      username: userStore.username || ''
    };
    showFeedbackForm.value = false;
    fetchTasksByUser(); // 重新获取用户任务列表

    // 显示成功提示
    showSuccessMessage.value = true;

    // 3秒后自动关闭提示
    setTimeout(() => {
      showSuccessMessage.value = false;
    }, 3000);
  }, (message, code) => {
    // 处理失败情况
    console.error('提交失败:', message, '错误码:', code);
    ElNotification({
      title: '提交失败',
      message: message,
      type: 'error',
      duration: 3000
    });
  });
};

// 组件挂载时获取任务数据
onMounted(() => {
  fetchTasks();
  fetchTasksByUser();
  // 确保表单中的用户名是最新的
  feedbackForm.value.username = userStore.username || '';
});
</script>

<style scoped>
.success-message {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.success-content {
  background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%);
  padding: 30px;
  border-radius: 20px;
  width: 90%;
  max-width: 400px;
  text-align: center;
  box-shadow: 0 10px 30px rgba(255, 105, 180, 0.5);
  border: 3px solid #fff;
  position: relative;
  animation: popIn 0.5s ease-out;
}

@keyframes popIn {
  0% {
    transform: scale(0.8);
    opacity: 0;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.success-icon {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  font-size: 40px;
  color: white;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.success-content h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 24px;
}

.success-content p {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 16px;
}

.success-content button {
  padding: 10px 25px;
  border: none;
  border-radius: 25px;
  background: linear-gradient(to right, #667eea, #764ba2);
  color: white;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.success-content button:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

.tab-buttons {
  display: flex;
  justify-content: center;
  margin-bottom: 30px;
}

.tab-button {
  background-color: rgba(255, 255, 255, 0.2); /* 半透明背景 */
  color: #fff;
  border: none;
  padding: 10px 20px;
  margin: 0 5px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 16px;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.6); /* 文字发光效果 */
  box-shadow: 0 0 5px rgba(255, 255, 255, 0.5); /* 按钮阴影 */
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
}

.tab-button:hover {
  background-color: rgba(255, 255, 255, 0.3); /* 悬停时背景更亮 */
  transform: scale(1.05);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.7); /* 增强阴影 */
}

.tab-button.active {
  background-color: rgba(255, 255, 255, 0.4); /* 激活按钮的背景颜色 */
  font-weight: bold;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.8); /* 更强的激活阴影 */
}

.add-feedback-button {
  background-color: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: none;
  padding: 10px 20px;
  border-radius: 20px;
  cursor: pointer;
  font-size: 16px;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  text-shadow: 0 0 5px rgba(255, 255, 255, 0.6);
  box-shadow: 0 0 5px rgba(255, 255, 255, 0.5);
  transition: background-color 0.3s, transform 0.2s, box-shadow 0.3s;
  margin-bottom: 20px;
  align-self: flex-start;
}

.add-feedback-button:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: scale(1.05);
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.7);
}

.feedback-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.5);
}

.task-detail {
  border-radius: 20px;
  padding: 40px;
  max-width: 100%;
  margin: 0 auto;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  color: #fff;
  /* 调暗背景图片 */
  background-image: url('@/img/websitetaskbackground/WebsiteTaskBackground.jpg');
  background-size: cover;
  background-position: center;
  background-attachment: fixed;
  /* 添加背景遮罩以调暗背景 */
  background-color: rgba(0, 0, 0, 0.4); /* 黑色遮罩，透明度为60% */
  background-blend-mode: multiply; /* 使背景图片与遮罩混合 */
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  text-shadow: 0 0 3px rgba(255, 255, 255, 0.5);
}

.task-card {
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  color: white;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  backdrop-filter: blur(10px);
}

.task-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(255, 0, 255, 0.3);
}

.task-card h3 {
  margin-top: 0;
  font-size: 20px;
  font-weight: bold;
  color: #fff;
  text-shadow: 0 0 5px rgba(255, 0, 255, 0.6);
}

.task-card p {
  margin: 5px 0;
  font-size: 14px;
  color: #f0f0f0; /* 更清晰的浅色字体，动漫风格 */
}

.completed {
  color: #67c23a;
  font-weight: bold;
  text-shadow: 0 0 3px rgba(103, 194, 58, 0.5);
}

.in-progress {
  color: #e6a23c;
  font-weight: bold;
  text-shadow: 0 0 3px rgba(230, 162, 60, 0.5);
}

.not-started {
  color: #f56c6c;
  font-weight: bold;
  text-shadow: 0 0 3px rgba(245, 108, 108, 0.5);
}

.feedback-form-overlay {
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

.feedback-form {
  background: linear-gradient(135deg, rgba(255, 192, 203, 0.9), rgba(173, 216, 230, 0.9));
  padding: 30px;
  border-radius: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 0 20px rgba(255, 105, 180, 0.7);
  color: #333;
  border: 3px solid #ff69b4;
  position: relative;
  overflow: hidden;
}

.feedback-form::before {
  content: "";
  position: absolute;
  top: -10px;
  left: -10px;
  right: -10px;
  bottom: -10px;
  background: linear-gradient(45deg, #ff69b4, #ba55d3, #00bfff, #00fa9a, #ff69b4);
  background-size: 400% 400%;
  z-index: -1;
  border-radius: 25px;
  animation: gradientBorder 3s ease infinite;
}

@keyframes gradientBorder {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.feedback-form h2 {
  margin-top: 0;
  text-align: center;
  color: #666;
  font-size: 28px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  display: inline-block;
  left: 50%;
  transform: translateX(-50%);
}

.feedback-form h2::after {
  content: "";
  display: block;
  width: 50px;
  height: 3px;
  background: linear-gradient(to right, #ff69b4, #00bfff);
  margin: 8px auto 0;
  border-radius: 3px;
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
  color: #5a5a5a;
  font-size: 16px;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #ffb6c1;
  border-radius: 12px;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  box-sizing: border-box;
  background-color: rgba(255, 255, 255, 0.7);
  transition: all 0.3s ease;
  font-size: 15px;
  color: black;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #ff69b4;
  box-shadow: 0 0 10px rgba(255, 105, 180, 0.5);
  background-color: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
}

.form-group textarea {
  height: 120px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
}

.form-actions button {
  padding: 12px 25px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
  font-size: 16px;
  font-weight: bold;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.form-actions button::before {
  content: "";
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: 0.5s;
}

.form-actions button:hover::before {
  left: 100%;
}

.form-actions button[type="button"] {
  background: linear-gradient(to right, #a9a9a9, #808080);
  color: #fff;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.form-actions button[type="button"]:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

.form-actions button[type="submit"] {
  background: linear-gradient(to right, #ff69b4, #00bfff);
  color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
}

.form-actions button[type="submit"]:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 191, 255, 0.4);
}

/* 分页样式 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin: 5px 0;
  flex-wrap: wrap;
}

.page-button {
  padding: 8px 15px;
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  font-family: 'Comic Sans MS', 'Noto Sans JP', sans-serif;
}

.page-button:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
}

.page-button.active {
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.3);
  font-weight: bold;
}

.page-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .pagination {
    gap: 5px;
  }

  .page-button {
    padding: 6px 12px;
    font-size: 0.9rem;
  }
}
/* 在 WebsiteTaskCard.vue 的样式部分添加暗黑模式支持 */

/* 暗黑模式下的表单样式 */
html.dark .feedback-form {
  background: linear-gradient(135deg, rgba(60, 60, 80, 0.9), rgba(50, 70, 90, 0.9));
  color: #e0e0e0;
  border: 3px solid #5a7a9a;
}

html.dark .feedback-form h2 {
  color: #e0e0e0;
}

html.dark .form-group label {
  color: #c0c0c0;
}

html.dark .form-group input,
html.dark .form-group textarea {
  background-color: rgba(40, 40, 60, 0.7);
  border: 2px solid #5a7a9a;
  color: #e0e0e0;
}

html.dark .form-group input:focus,
html.dark .form-group textarea:focus {
  background-color: rgba(40, 40, 60, 0.9);
  border-color: #64b5f6;
  box-shadow: 0 0 10px rgba(100, 181, 246, 0.5);
}

html.dark .form-actions button[type="button"] {
  background: linear-gradient(to right, #5a5a5a, #404040);
  color: #e0e0e0;
}

html.dark .form-actions button[type="submit"] {
  background: linear-gradient(to right, #6495ed, #5a7a9a);
  color: #e0e0e0;
}

/* 暗黑模式下的任务卡片样式 */
html.dark .task-card {
  background-color: rgba(40, 40, 60, 0.7);
  color: #e0e0e0;
}

html.dark .task-card h3 {
  color: #e0e0e0;
}

html.dark .task-card p {
  color: #c0c0c0;
}

/* 暗黑模式下的按钮样式 */
html.dark .tab-button {
  background-color: rgba(60, 60, 80, 0.4);
  color: #e0e0e0;
}

html.dark .tab-button:hover {
  background-color: rgba(70, 70, 90, 0.5);
}

html.dark .tab-button.active {
  background-color: rgba(80, 80, 110, 0.6);
}

html.dark .add-feedback-button {
  background-color: rgba(60, 60, 80, 0.4);
  color: #e0e0e0;
}

html.dark .add-feedback-button:hover {
  background-color: rgba(70, 70, 90, 0.5);
}

/* 暗黑模式下的分页按钮样式 */
html.dark .page-button {
  background: rgba(60, 60, 80, 0.4);
  border: 1px solid rgba(100, 100, 140, 0.5);
  color: #e0e0e0;
}

html.dark .page-button:hover:not(:disabled) {
  background: rgba(70, 70, 90, 0.5);
}

html.dark .page-button.active {
  background: rgba(80, 80, 110, 0.6);
}

/* 暗黑模式下的成功提示样式 */
html.dark .success-content {
  background: linear-gradient(135deg, #5a3a5a 0%, #4a4a6a 100%);
  color: #e0e0e0;
}

html.dark .success-content h3 {
  color: #e0e0e0;
}

html.dark .success-content p {
  color: #c0c0c0;
}

html.dark .success-icon {
  background: linear-gradient(135deg, #4a5a8a 0%, #5a4a7a 100%);
}
</style>
