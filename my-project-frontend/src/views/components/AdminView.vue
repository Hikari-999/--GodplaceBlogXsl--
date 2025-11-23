<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user_store';
import { onMounted, ref, computed, onUnmounted } from 'vue';
import ArticleManagement from "@/views/components/administrationpage/ArticleManagement.vue";
import CoreFunctionality from "@/views/components/administrationpage/CoreFunctionality.vue";
import Dashboard from "@/views/components/administrationpage/Dashboard.vue";
import UserManagement from "@/views/components/administrationpage/UserManagement.vue";
import AdministrationPage from "@/views/components/administrationpage/AdministrationPage.vue"
import TalkManagement from "@/views/components/administrationpage/TalkManagement.vue"
import LinkManagement from "@/views/components/administrationpage/FriendLinkAdmin.vue"
const router = useRouter();
const userStore = useUserStore();
const isAdmin = ref(false);
const activeMenu = ref('dashboard');

// 菜单选项
const menuItems = [
  { id: 'dashboard', name: '仪表盘', icon: '📊' },
  { id: 'users', name: '用户管理', icon: '👥' },
  { id: 'posts', name: '文章管理', icon: '📝' },
  { id: 'talks', name: '随笔管理', icon: '💬' },
/*  { id: 'links', name: '友链管理', icon: '⭐' },*/
  { id: 'AdministrationPage', name: '系统设置(待实现)', icon: '⚙️' },
  { id: 'coreFunctionality', name: '核心功能', icon: '🔧💎🎯🚀' },
];

// 根据选中菜单显示不同内容
const activeComponent = computed(() => {
  switch (activeMenu.value) {
    case 'dashboard':
      return 'Dashboard';
    case 'users':
      return 'UserManagement';
    case 'posts':
      return 'PostManagement';
    case 'talks':
      return 'TalkManagement';
    case 'comments':
      return 'ArticleManagement';
    case 'AdministrationPage':
      return 'AdministrationPage';
    case 'coreFunctionality':
      return 'CoreFunctionality';
    case 'links':
      return 'LinkManagement';
    default:
      return 'Dashboard';
  }
});

onMounted(() => {
  // 确保只有管理员可以访问此页面
  if (userStore.role !== 'admin') {
    router.push('/index');
  } else {
    isAdmin.value = true;
  }

  // 监听菜单切换事件
  window.addEventListener('switch-menu', handleMenuSwitch);
});

// 处理菜单切换事件
function handleMenuSwitch(event) {
  const { menu, tab } = event.detail;
  activeMenu.value = menu;

  // 如果需要切换到核心功能页面的特定标签
  if (tab) {
    // 延迟执行以确保组件已加载
    setTimeout(() => {
      window.dispatchEvent(new CustomEvent('switch-tab', { detail: { tab } }));
    }, 0);
  }
}

// 切换菜单
function switchMenu(menuId: string) {
  activeMenu.value = menuId;
}

// 组件卸载时移除事件监听器
onUnmounted(() => {
  window.removeEventListener('switch-menu', handleMenuSwitch);
});
</script>

<template>
  <div class="admin-layout" v-if="isAdmin">
    <!-- 顶部导航栏 -->
    <header class="admin-header">
    </header>

    <div class="admin-content">
      <!-- 左侧功能菜单 -->
      <nav class="sidebar">
        <ul class="menu-list">
          <li
              v-for="item in menuItems"
              :key="item.id"
              :class="['menu-item', { active: activeMenu === item.id }]"
              @click="switchMenu(item.id)"
          >
            <span class="menu-icon">{{ item.icon }}</span>
            <span class="menu-name">{{ item.name }}</span>
          </li>
        </ul>
      </nav>

      <!-- 右侧主体内容 -->
      <main class="main-content">
        <div v-if="activeMenu === 'dashboard'" class="content-section">
          <Dashboard />
        </div>

        <div v-else-if="activeMenu === 'users'" class="content-section">
          <UserManagement />
        </div>

        <div v-else-if="activeMenu === 'posts'" class="content-section">
          <ArticleManagement/>
        </div>

        <div v-else-if="activeMenu === 'talks'" class="content-section">
          <TalkManagement />
        </div>

<!--        <div v-else-if="activeMenu === 'links'" class="content-section">
          <LinkManagement />
        </div>-->

        <div v-else-if="activeMenu === 'AdministrationPage'" class="content-section">
          <AdministrationPage />
        </div>

        <div v-else-if="activeMenu === 'coreFunctionality'" class="content-section">
          <CoreFunctionality />
        </div>
      </main>
    </div>
  </div>
  <div v-else class="loading">
    逮到了，没事来管理员面板干甚魔
  </div>
</template>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background-color: #2c3e50;
  color: white;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.logout-btn {
  background-color: #e74c3c;
  color: white;
  border: none;
  padding: 5px 10px;
  border-radius: 4px;
  cursor: pointer;
}

.admin-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 250px;
  background-color: #34495e;
  color: white;
  overflow-y: auto;
}

.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 15px 20px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.menu-item:hover {
  background-color: #2c3e50;
}

.menu-item.active {
  background-color: #1abc9c;
}

.menu-icon {
  margin-right: 10px;
  font-size: 18px;
}

.main-content {
  flex: 1;
  padding: 20px;
  background-color: #ecf0f1;
  overflow-y: auto;
}

.content-section {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  height: 100%;
}

.loading {
  padding: 20px;
  text-align: center;
}
</style>
