<template>
  <div class="user-management">
    <h2>用户管理</h2>

    <!-- 用户搜索和过滤 -->
    <div class="user-controls">
      <div class="search-box">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名或邮箱"
            clearable
            style="width: 300px"
            @keyup.enter="searchUsers"
        >
          <template #append>
            <el-button @click="searchUsers">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <div class="filter-controls">
        <el-select v-model="roleFilter" placeholder="角色筛选" clearable style="width: 150px" @change="filterUsers">
          <el-option label="全部角色" value=""></el-option>
          <el-option label="管理员" value="admin"></el-option>
          <el-option label="普通用户" value="user"></el-option>
          <el-option label="VIP" value="vip"></el-option>
          <el-option label="超级VIP" value="bigvip"></el-option>
        </el-select>

        <el-button type="primary" @click="showAddUser" style="margin-left: 10px">
          <el-icon><Plus /></el-icon> 添加用户
        </el-button>

        <el-button @click="refreshUsers" style="margin-left: 10px">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <!-- 用户列表 -->
    <div class="user-table-container">
      <el-table
          :data="filteredUsers"
          style="width: 100%"
          v-loading="loading"
          stripe
          border
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ getRoleDisplayName(scope.row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="registerTime" label="注册时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.registerTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="scope">
            <el-button
                size="small"
                @click="showEditUser(scope.row)"
            >
              编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="deleteUser(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalUsers"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>

    <!-- 编辑用户对话框 -->
    <el-dialog
        v-model="editUserDialogVisible"
        width="500px"
        :before-close="handleClose"
        class="user-edit-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <el-icon class="dialog-icon"><User /></el-icon>
          <span class="dialog-title">编辑用户</span>
        </div>
      </template>
      <el-form
          :model="userForm"
          label-width="80px"
          :rules="userFormRules"
          ref="userFormRef"
          class="user-form"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="userForm.username"
              :disabled="!!editingUser.id"
              placeholder="请输入用户名"
              size="default"
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="userForm.email"
              placeholder="请输入邮箱"
              size="default"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="userForm.password"
              type="password"
              :placeholder="editingUser.id ? '留空则不修改密码' : '请输入密码'"
              size="default"
              show-password
          />
        </el-form-item>

        <!-- 角色 -->
        <el-form-item label="角色">
          <el-select
              v-model="userForm.role"
              placeholder="请选择角色"
              size="default"              style="width: 100%"
          >
            <el-option label="普通用户" value="user" />
            <el-option label="VIP" value="vip" />
            <el-option label="超级VIP" value="bigvip" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="editUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="updateUser">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 添加用户对话框 -->
    <el-dialog
        v-model="addUserDialogVisible"
        width="500px"
        class="user-add-dialog"
    >
      <template #header>
        <div class="dialog-header">
          <el-icon class="dialog-icon"><Plus /></el-icon>
          <span class="dialog-title">添加用户</span>
        </div>
      </template>
      <el-form
          :model="addUserForm"
          label-width="80px"
          :rules="addUserFormRules"
          ref="addUserFormRef"
          class="user-form"
      >
        <!-- 用户名 -->
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="addUserForm.username"
              placeholder="请输入用户名"
              size="default"
          />
        </el-form-item>

        <!-- 邮箱 -->
        <el-form-item label="邮箱" prop="email">
          <el-input
              v-model="addUserForm.email"
              placeholder="请输入邮箱"
              size="default"
          />
        </el-form-item>

        <!-- 密码 -->
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="addUserForm.password"
              type="password"
              placeholder="请输入密码"
              size="default"
              show-password
          />
        </el-form-item>

        <!-- 角色 -->
        <el-form-item label="角色">
          <el-select
              v-model="addUserForm.role"
              placeholder="请选择角色"
              size="default"              style="width: 100%"
          >
            <el-option label="普通用户" value="user" />
            <el-option label="VIP" value="vip" />
            <el-option label="超级VIP" value="bigvip" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="addUserDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="addUser">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue'
import { get, post } from '@/net'
import { useUserStore } from '@/stores/user_store'
import { ElMessage, ElMessageBox } from 'element-plus'
import {Search, Refresh, Plus, User} from '@element-plus/icons-vue'

// 用户存储
const userStore = useUserStore()

// 用户数据
const users = ref([])
const loading = ref(false)

// 搜索和过滤
const searchKeyword = ref('')
const roleFilter = ref('')

// 分页
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)

// 编辑用户对话框
const editUserDialogVisible = ref(false)
// 添加用户对话框
const addUserDialogVisible = ref(false)
const userFormRef = ref()
const addUserFormRef = ref()
const editingUser = ref({})

const userForm = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  role: 'user'
})
const getRoleDisplayName = (role) => {
  switch (role) {
    case 'admin':
      return '管理员'
    case 'vip':
      return 'VIP'
    case 'bigvip':
      return '超级VIP'
    default:
      return '普通用户'
  }
}

const getRoleTagType = (role) => {
  switch (role) {
    case 'admin':
      return 'danger'
    case 'vip':
      return 'warning'
    case 'bigvip':
      return 'error'
    default:
      return 'success'
  }
}
const addUserForm = reactive({
  username: '',
  email: '',
  password: '',
  role: 'user'
})

const userFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 10, message: '用户名长度为1-10个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

const addUserFormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 10, message: '用户名长度为1-10个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' }
  ]
}

// 计算属性：过滤后的用户列表
const filteredUsers = computed(() => {
  let filtered = users.value

  // 根据搜索关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    filtered = filtered.filter(user =>
        user.username.toLowerCase().includes(keyword) ||
        user.email.toLowerCase().includes(keyword)
    )
  }

  // 根据角色过滤
  if (roleFilter.value) {
    filtered = filtered.filter(user => user.role === roleFilter.value)
  }

  // 更新总用户数
  totalUsers.value = filtered.length

  // 分页处理
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filtered.slice(start, end)
})

// 获取用户列表
const fetchUsers = () => {
  loading.value = true
  get('/admin/user/list', (data) => {
    users.value = data
    totalUsers.value = data.length
    loading.value = false
  }, (message, code) => {
    ElMessage.error(`获取用户列表失败: ${message}`)
    loading.value = false
  })
}

// 搜索用户
const searchUsers = () => {
  currentPage.value = 1
  // 过滤将在 computed 属性中处理
}

// 角色过滤
const filterUsers = () => {
  currentPage.value = 1
  // 过滤将在 computed 属性中处理
}

// 刷新用户列表
const refreshUsers = () => {
  searchKeyword.value = ''
  roleFilter.value = ''
  currentPage.value = 1
  fetchUsers()
}

// 显示编辑用户对话框
const showEditUser = (user) => {
  editingUser.value = user
  userForm.id = user.id
  userForm.username = user.username
  userForm.email = user.email
  userForm.password = ''
  userForm.role = user.role || 'user'
  editUserDialogVisible.value = true
}

// 显示添加用户对话框
const showAddUser = () => {
  addUserForm.username = ''
  addUserForm.email = ''
  addUserForm.password = ''
  addUserForm.role = 'user'
  addUserDialogVisible.value = true
}

// 更新用户信息
const updateUser = () => {
  userFormRef.value.validate((valid) => {
    if (!valid) {
      return
    }

    // 准备提交的数据
    const userData = {
      id: userForm.id,
      username: userForm.username,
      email: userForm.email,
      role: userForm.role
    }

    // 只有在输入了新密码时才提交密码
    if (userForm.password) {
      userData.password = userForm.password
    }

    post('/admin/user/update', userData, () => {
      ElMessage.success('用户信息更新成功')
      editUserDialogVisible.value = false
      fetchUsers() // 重新获取用户列表
    }, (message, code) => {
      ElMessage.error(`用户信息更新失败: ${message}`)
    })
  })
}

// 添加用户
const addUser = () => {
  addUserFormRef.value.validate((valid) => {
    if (!valid) {
      return
    }

    // 准备提交的数据
    const userData = {
      username: addUserForm.username,
      email: addUserForm.email,
      password: addUserForm.password,
      role: addUserForm.role
    }

    post('/admin/user/add', userData, () => {
      ElMessage.success('用户添加成功')
      addUserDialogVisible.value = false
      fetchUsers() // 重新获取用户列表
    }, (message, code) => {
      ElMessage.error(`用户添加失败: ${message}`)
    })
  })
}

// 删除用户
const deleteUser = (user) => {
  ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？此操作不可恢复！`,
      '确认删除',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    post(`/admin/user/delete/${user.id}`, {}, () => {
      ElMessage.success('用户删除成功')
      fetchUsers() // 重新获取用户列表
    }, (message, code) => {
      ElMessage.error(`删除用户失败: ${message}`)
    })
  }).catch(() => {
    // 用户取消删除
  })
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 组件挂载时获取用户列表
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management h2 {
  color: #333;
}
.user-management {
  padding: 20px;
}

.user-controls {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}


@media (max-width: 768px) {
  .user-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    order: 2;
  }

  .filter-controls {
    order: 1;
    justify-content: flex-end;
  }
}

/* 对话框整体样式 */
.user-edit-dialog {
  --el-dialog-border-radius: 12px;
  --el-dialog-background: rgba(30, 30, 30, 0.95);
  --el-dialog-header-background: rgba(30, 30, 30, 0.95);
  --el-dialog-header-font-color: #fff;
  --el-dialog-title-font-size: 20px;
  --el-dialog-title-font-weight: 600;
  --el-dialog-close-color: #aaa;
  --el-dialog-close-hover-color: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.user-add-dialog {
  --el-dialog-border-radius: 12px;
  --el-dialog-background: rgba(30, 30, 30, 0.95);
  --el-dialog-header-background: rgba(30, 30, 30, 0.95);
  --el-dialog-header-font-color: #fff;
  --el-dialog-title-font-size: 20px;
  --el-dialog-title-font-weight: 600;
  --el-dialog-close-color: #aaa;
  --el-dialog-close-hover-color: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.user-edit-dialog .el-dialog__header,
.user-add-dialog .el-dialog__header {
  border-bottom: 1px solid #444;
}

.user-edit-dialog .el-dialog__body,
.user-add-dialog .el-dialog__body {
  padding: 0;
  background-color: #1e1e1e;
}

.user-edit-dialog .el-dialog__footer,
.user-add-dialog .el-dialog__footer {
  border-top: 1px solid #444;
  background-color: #1e1e1e;
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dialog-icon {
  font-size: 24px;
  color: #409eff;
}

.dialog-title {
  font-size: 20px;
  font-weight: 600;
  color: #fff;
}

.user-edit-dialog .el-form-item,
.user-add-dialog .el-form-item {
  margin-bottom: 20px;
}

.user-edit-dialog .el-form-item__label,
.user-add-dialog .el-form-item__label {
  color: #e0e0e0;
  font-weight: 500;
}

.user-edit-dialog .el-input__inner,
.user-add-dialog .el-input__inner {
  background-color: rgba(50, 50, 50, 0.7);
  border: 1px solid #555;
  border-radius: 6px;
  color: #fff;
  height: 40px;
}

.user-edit-dialog .el-input__inner:focus,
.user-add-dialog .el-input__inner:focus {
  border-color: #409eff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.user-edit-dialog .el-select .el-input__inner,
.user-add-dialog .el-select .el-input__inner {
  background-color: rgba(50, 50, 50, 0.7);
  border: 1px solid #555;
  border-radius: 6px;
  color: #fff;
}

.user-edit-dialog .el-select-dropdown,
.user-add-dialog .el-select-dropdown {
  background-color: #2d2d2d;
  border: 1px solid #555;
  border-radius: 6px;
}

.user-edit-dialog .el-select-dropdown__item,
.user-add-dialog .el-select-dropdown__item {
  color: #fff;
}

.user-edit-dialog .el-select-dropdown__item.hover,
.user-edit-dialog .el-select-dropdown__item:hover,
.user-add-dialog .el-select-dropdown__item.hover,
.user-add-dialog .el-select-dropdown__item:hover {
  background-color: #3a3a3a;
}

.user-edit-dialog .el-select-dropdown__item.selected,
.user-add-dialog .el-select-dropdown__item.selected {
  color: #409eff;
  background-color: #3a3a3a;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0;
  padding: 0;
  margin: 0;
}

.dialog-footer .el-button {
  border-radius: 6px;
  padding: 8px 20px;
}

@media (max-width: 768px) {
  .user-controls {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    order: 2;
  }

  .filter-controls {
    order: 1;
    justify-content: flex-end;
  }
}
</style>

