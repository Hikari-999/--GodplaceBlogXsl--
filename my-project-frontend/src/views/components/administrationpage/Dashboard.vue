<template>
  <div class="dashboard">
    <h2>仪表盘</h2>

    <!-- 统计数据卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon visits-icon">👁️</div>
        <div class="stat-info">
          <h3>总访问量</h3>
          <p>{{ dashboardData.totalVisits }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon online-icon">👤</div>
        <div class="stat-info">
          <h3>当前在线</h3>
          <p>{{ dashboardData.currentOnline }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon articles-icon">📝</div>
        <div class="stat-info">
          <h3>文章总数</h3>
          <p>{{ dashboardData.totalArticles }}</p>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon runtime-icon">⏱️</div>
        <div class="stat-info">
          <h3>更新同步时间</h3>
          <p>{{ dashboardData.updateRuntime }}</p>
        </div>
      </div>
    </div>

    <!-- 详细信息区域 -->
    <div class="details-grid">
      <!-- 最近任务 -->
      <div class="detail-card">
        <div class="card-header">
          <h3>最近任务</h3>
          <button class="view-all-btn" @click="goToCoreFunctionality('taskList')">查看全部</button>
        </div>
        <div class="card-content">
          <div v-if="dashboardData.recentTasks.length === 0" class="empty-state">
            暂无任务
          </div>
          <div v-else class="task-list">
            <div
                v-for="task in dashboardData.recentTasks"
                :key="task.id"
                class="task-item"
            >
              <div class="task-info">
                <div class="task-title">{{ task.title }}</div>
                <div class="task-desc">{{ task.description }}</div>
              </div>
              <div class="task-status" :class="'status-' + task.status">
                {{ getStatusText(task.status) }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 最新公告 -->
      <div class="detail-card">
        <div class="card-header">
          <h3>最新公告</h3>
          <button class="view-all-btn" @click="goToCoreFunctionality('announcement')">查看全部</button>
        </div>
        <div class="card-content">
          <div v-if="dashboardData.recentNotifications.length === 0" class="empty-state">
            暂无公告
          </div>
          <div v-else class="notification-list">
            <div
                v-for="notification in dashboardData.recentNotifications"
                :key="notification.id"
                class="notification-item"
            >
              <div class="notification-info">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-content">{{ notification.content }}</div>
              </div>
              <div class="notification-date">
                {{ formatDate(notification.createTime) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="actions-grid">
        <button class="action-button" @click="goToCoreFunctionality('taskList')">
          <span class="action-icon">📋</span>
          <span>管理任务</span>
        </button>
        <button class="action-button" @click="goToCoreFunctionality('announcement')">
          <span class="action-icon">📢</span>
          <span>发布公告</span>
        </button>
        <button class="action-button" @click="goToArticleManagement">
          <span class="action-icon">📝</span>
          <span>管理文章</span>
        </button>
        <button class="action-button" disabled>
          <span class="action-icon">👥</span>
          <span>管理用户</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { get, post } from '@/net';
import { useRouter } from 'vue-router';

const router = useRouter();

// 仪表盘数据
const dashboardData = ref({
  totalVisits: 0,
  currentOnline: 0,
  totalArticles: 0,
  startRuntime: '',
  updateRuntime: '',
  recentTasks: [],
  recentNotifications: []
});

onMounted(() => {
  loadDashboardData();
});

// 加载仪表盘数据
function loadDashboardData() {
  // 获取基础信息
  get('/base-info/get-info', (data) => {
    dashboardData.value.totalVisits = data.totalVisits;
    dashboardData.value.currentOnline = data.currentOnline;
    dashboardData.value.totalArticles = data.totalArticles;
    dashboardData.value.startRuntime = formatDate(data.startRuntime);
    dashboardData.value.updateRuntime = formatDate(data.updateRuntime);
  });

  // 获取最近任务
  post('/blog/getTask', {}, (data) => {
    dashboardData.value.recentTasks = data.slice(0, 5); // 只取前5个
  });

  // 获取最近公告
  get('/notification/show', (data) => {
    // 取最新的5条公告
    const sortedData = data.sort((a, b) => new Date(b.createTime) - new Date(a.createTime));
    dashboardData.value.recentNotifications = sortedData.slice(0, 5);
  });
}

// 格式化日期
function formatDate(dateString) {
  if (!dateString) return '';
  const date = new Date(dateString);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
}

// 获取任务状态文本
function getStatusText(status) {
  switch (status) {
    case 0: return '待办';
    case 1: return '进行中';
    case 2: return '已完成';
    default: return '未知';
  }
}

// 跳转到核心功能页面
function goToCoreFunctionality(tab) {
  // 这里需要通过事件通知父组件切换菜单和标签
  window.dispatchEvent(new CustomEvent('switch-menu', { detail: { menu: 'coreFunctionality', tab } }));
}

// 跳转到文章管理
function goToArticleManagement() {
  window.dispatchEvent(new CustomEvent('switch-menu', { detail: { menu: 'posts' } }));
}
</script>

<style scoped>

.dashboard h2 {
  color: #333;
  text-align: center;
  font-weight: bold;
  font-size: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.stat-icon {
  font-size: 2rem;
  margin-right: 15px;
}

.stat-info h3 {
  margin: 0 0 5px 0;
  font-size: 1rem;
  font-weight: normal;
}

.stat-info p {
  margin: 0;
  font-size: 1.8rem;
  font-weight: bold;
}

.details-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.detail-card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  overflow: hidden;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.card-header h3 {
  margin: 0;
  color: #333;
}

.view-all-btn {
  background: #1abc9c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}

.view-all-btn:hover {
  background: #16a085;
}

.card-content {
  padding: 20px;
  min-height: 250px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #7f8c8d;
  font-style: italic;
}

.task-list, .notification-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.task-item, .notification-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.task-info, .notification-info {
  flex: 1;
}

.task-title, .notification-title {
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.task-desc, .notification-content {
  font-size: 0.9rem;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.task-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: bold;
  white-space: nowrap;
}

.status-0 {
  background-color: #f39c12;
  color: white;
}

.status-1 {
  background-color: #3498db;
  color: white;
}

.status-2 {
  background-color: #2ecc71;
  color: white;
}

.notification-date {
  font-size: 0.8rem;
  color: #95a5a6;
  margin-left: 15px;
  text-align: right;
}

.quick-actions h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.action-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: white;
  border: 1px solid #eee;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-button:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.1);
  border-color: #1abc9c;
}

.action-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-icon {
  font-size: 2rem;
  margin-bottom: 10px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr 1fr;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .task-item, .notification-item {
    flex-direction: column;
    gap: 10px;
  }

  .notification-date {
    text-align: left;
    margin-left: 0;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>
