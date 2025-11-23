<template>
  <div class="system-settings">
    <!-- 设置选项卡 -->
    <div class="settings-tabs">
      <button
          v-for="tab in settingTabs"
          :key="tab.id"
          :class="['tab-button', { active: activeTab === tab.id }]"
          @click="switchTab(tab.id)"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-name">{{ tab.name }}</span>
      </button>
    </div>

    <!-- 设置内容区域 -->
    <div class="settings-content">
      <!-- 网站基本信息设置 -->
      <div v-if="activeTab === 'basic'" class="content-wrapper">
        <h2>网站基本信息设置</h2>
        <form @submit.prevent="saveBasicSettings">
          <div class="form-group">
            <label>网站名称</label>
            <input v-model="basicSettings.siteName" type="text" class="form-control">
          </div>
          <div class="form-group">
            <label>网站描述</label>
            <textarea v-model="basicSettings.siteDescription" class="form-control"></textarea>
          </div>
          <div class="form-group">
            <label>关键词</label>
            <input v-model="basicSettings.keywords" type="text" class="form-control">
          </div>
          <button type="submit" class="btn btn-primary">保存设置</button>
        </form>
      </div>

      <!-- 用户管理设置 -->
      <div v-else-if="activeTab === 'user'" class="content-wrapper">
        <h2>用户管理设置</h2>
        <form @submit.prevent="saveUserSettings">
          <div class="form-group">
            <label>
              <input v-model="userSettings.allowRegistration" type="checkbox">
              允许用户注册
            </label>
          </div>
          <div class="form-group">
            <label>默认用户角色</label>
            <select v-model="userSettings.defaultRole" class="form-control">
              <option value="user">普通用户</option>
              <option value="vip">VIP用户</option>
            </select>
          </div>
          <button type="submit" class="btn btn-primary">保存设置</button>
        </form>
      </div>

      <!-- 更多设置模块... -->
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'

// 当前激活的选项卡
const activeTab = ref('basic')

// 设置选项卡
const settingTabs = [
  { id: 'basic', name: '基本信息', icon: '🌐' },
  { id: 'user', name: '用户管理', icon: '👥' },
  { id: 'content', name: '内容管理', icon: '📝' },
  { id: 'security', name: '安全设置', icon: '🔒' },
  { id: 'email', name: '邮件服务', icon: '📧' }
]

// 基本信息设置
const basicSettings = reactive({
  siteName: '',
  siteDescription: '',
  keywords: ''
})

// 用户管理设置
const userSettings = reactive({
  allowRegistration: true,
  defaultRole: 'user'
})

// 切换选项卡
const switchTab = (tabId) => {
  activeTab.value = tabId
}

// 保存基本设置
const saveBasicSettings = () => {
  // 调用后端API保存设置
  console.log('保存基本设置', basicSettings)
}

// 保存用户设置
const saveUserSettings = () => {
  // 调用后端API保存设置
  console.log('保存用户设置', userSettings)
}
</script>

<style scoped>
.system-settings {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.settings-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-button {
  padding: 10px 15px;
  border: none;
  border-radius: 5px;
  background-color: #34495e;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  transition: background-color 0.3s;
}

.tab-button:hover {
  background-color: #2c3e50;
}

.tab-button.active {
  background-color: #1abc9c;
}

.settings-content {
  flex: 1;
  overflow-y: auto;
}

.content-wrapper {
  background-color: #2c3e50;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  color: white;
}

.content-wrapper h2 {
  margin-top: 0;
  border-bottom: 1px solid #34495e;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.form-control {
  width: 100%;
  padding: 8px;
  border: 1px solid #34495e;
  border-radius: 4px;
  background-color: #34495e;
  color: white;
}

.form-control:focus {
  outline: none;
  border-color: #1abc9c;
}

.btn-primary {
  background-color: #1abc9c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #16a085;
}
</style>
