<script setup>
import { ref, onMounted,onUnmounted } from 'vue'
import TaskList from "@/views/components/administrationpage/TaskList.vue";
import NotificationBackstage from "@/views/components/administrationpage/NotificationBackstage.vue";
import FriendLinkAdmin from "@/views/components/administrationpage/FriendLinkAdmin.vue";
import MessageManagement from "@/views/components/administrationpage/MessageManagement.vue";

// 当前激活的页面
const activePage = ref('taskList')

// 页面选项
const pages = [
  { id: 'taskList', name: '网站开发任务', icon: '📋' },
  { id: 'announcement', name: '发布公告', icon: '📢' },
  { id: 'friendLink', name: '友链管理', icon: '🔗' },
  { id: 'MessageManagement', name: '留言管理', icon: '💬' },
  { id: 'other3', name: '其他功能3', icon: '🚀' }
]

// 切换页面
const switchPage = (pageId) => {
  activePage.value = pageId
}

onMounted(() => {
  // 监听标签切换事件
  window.addEventListener('switch-tab', handleTabSwitch);
});

// 处理标签切换事件
function handleTabSwitch(event) {
  const { tab } = event.detail;
  activePage.value = tab;
}

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('switch-tab', handleTabSwitch);
});
</script>

<template>
  <div class="core-functionality">
    <!-- 顶部页面切换按钮 -->
    <div class="page-tabs">
      <button
          v-for="page in pages"
          :key="page.id"
          :class="['tab-button', { active: activePage === page.id }]"
          @click="switchPage(page.id)"
      >
        <span class="tab-icon">{{ page.icon }}</span>
        <span class="tab-name">{{ page.name }}</span>
      </button>
    </div>

    <!-- 页面内容区域 -->
    <div class="page-content">
      <div v-if="activePage === 'taskList'" class="content-wrapper">
        <h2>网站开发任务</h2>
        <div class="placeholder">
          <TaskList />
        </div>
      </div>

      <div v-else-if="activePage === 'announcement'" class="content-wrapper">
        <h2>发布公告</h2>
        <div class="placeholder">
          <NotificationBackstage />
        </div>
      </div>

      <div v-else-if="activePage === 'friendLink'" class="content-wrapper">
        <h2>友链管理</h2>
        <div class="placeholder">
          <FriendLinkAdmin />
        </div>
      </div>

      <div v-else-if="activePage === 'MessageManagement'" class="content-wrapper">
        <h2>留言管理</h2>
        <div class="placeholder">
          <MessageManagement />
        </div>
      </div>

      <div v-else-if="activePage === 'other3'" class="content-wrapper">
        <h2>其他功能3</h2>
        <div class="placeholder">
          <p>其他功能3内容区域</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.core-functionality {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.page-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  padding: 15px 0;
  border-bottom: 2px solid #eee;
  margin-bottom: 20px;
}

.tab-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.tab-button:hover {
  background-color: #007bff;
}

.tab-button.active {
  background-color: #1abc9c;
  color: white;
}

.tab-icon {
  font-size: 1.2em;
}

.page-content {
  flex: 1;
  overflow-y: auto;
}

.content-wrapper {
  height: 100%;
}

.content-wrapper h2 {
  margin-top: 0;
  color: #2c3e50;
}

.placeholder {
  height: calc(100% - 40px);
}
</style>
