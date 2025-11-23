<template>
  <div class="friend-link-admin">
    <!-- 友链列表 -->
    <el-table :data="friendLinks" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="网站名称" />
      <el-table-column prop="url" label="网站链接" />
      <el-table-column prop="description" label="描述" />
      <el-table-column label="头像">
        <template #default="scope">
          <el-avatar
              v-if="scope.row.avatar"
              :src="scope.row.avatar"
              size="small"
              shape="square"
          />
          <span v-else>无头像</span>
        </template>
      </el-table-column>
      <el-table-column label="是否展示" width="100">
        <template #default="scope">
          <el-switch
              v-model="scope.row.isDefault"
              :active-value="1"
              :inactive-value="0"
              @change="toggleFriendLinkStatus(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="openEditForm(scope.row)">编辑</el-button>
          <el-popconfirm
              title="确定要删除这个友链吗？"
              @confirm="deleteFriendLink(scope.row.id)"
          >
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑友链对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="网站名称" prop="name" :rules="[{ required: true, message: '请输入网站名称', trigger: 'blur' }]">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="网站链接" prop="url" :rules="[{ required: true, message: '请输入网站链接', trigger: 'blur' }]">
          <el-input v-model="editForm.url" />
        </el-form-item>
        <el-form-item label="网站描述" prop="description" :rules="[{ required: true, message: '请输入网站描述', trigger: 'blur' }]">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="网站头像">
          <el-upload
              class="avatar-uploader"
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              :before-upload="beforeAvatarUpload"
          >
            <img v-if="editForm.avatar" :src="editForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">点击上传网站头像，支持JPG/PNG格式，大小不超过2MB</div>
        </el-form-item>
        <el-form-item label="默认展示">
          <el-radio-group v-model="editForm.isDefault">
            <el-radio :label="1">展示</el-radio>
            <el-radio :label="0">隐藏</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 友链数据
const friendLinks = ref([])
const loading = ref(false)

// 控制对话框显示
const dialogVisible = ref(false)
const isEditMode = ref(false)

// 编辑表单数据
const editForm = reactive({
  id: null,
  name: '',
  url: '',
  description: '',
  avatar: '',
  isDefault: ''
})

// 当前选择的文件
const selectedFile = ref(null)

// 对话框标题
const dialogTitle = computed(() => isEditMode.value ? '编辑友链' : '添加友链')

// 获取友链数据
const fetchFriendLinks = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/friend-link/list-by-admin')
    if (response.data.code === 200) {
      friendLinks.value = response.data.data
    } else {
      ElMessage.error('获取友链数据失败')
    }
  } catch (error) {
    console.error('获取友链数据时出错:', error)
    ElMessage.error('获取友链数据时出错')
  } finally {
    loading.value = false
  }
}

// 打开编辑表单
const openEditForm = (link) => {
  isEditMode.value = true
  dialogVisible.value = true
  // 填充表单数据
  editForm.id = link.id
  editForm.name = link.name
  editForm.url = link.url
  editForm.description = link.description
  editForm.avatar = link.avatar
  editForm.isDefault = link.isDefault || 0
  selectedFile.value = null
}
// 切换友链展示状态
const toggleFriendLinkStatus = async (link) => {
  try {
    const updateData = {
      name: link.name,
      url: link.url,
      description: link.description,
      avatar: link.avatar,
      isDefault: link.isDefault
    };

    const response = await axios.put(`/api/friend-link/update/${link.id}`, updateData);

    if (response.data.code === 200) {
      ElMessage.success('友链状态更新成功！');
    } else {
      // 恢复开关状态
      link.isDefault = link.isDefault === 1 ? 0 : 1;
      ElMessage.error(response.data.message || '友链状态更新失败');
    }
  } catch (error) {
    // 恢复开关状态
    link.isDefault = link.isDefault === 1 ? 0 : 1;
    console.error('更新友链状态时出错:', error);
    ElMessage.error('友链状态更新失败');
  }
}
// 处理头像选择
const handleAvatarChange = (file) => {
  selectedFile.value = file.raw
  const reader = new FileReader()
  reader.onload = (e) => {
    editForm.avatar = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 头像上传前检查
const beforeAvatarUpload = (rawFile) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  const isAllowedType = allowedTypes.includes(rawFile.type)
  const isLt2M = rawFile.size / 1024 / 1024 < 2

  if (!isAllowedType) {
    ElMessage.error('头像图片必须是 JPG/PNG 格式!')
  }
  if (!isLt2M) {
    ElMessage.error('头像图片大小不能超过 2MB!')
  }
  return isAllowedType && isLt2M
}

// 提交编辑
const submitEdit = async () => {
  if (!editForm.name || !editForm.url || !editForm.description) {
    ElMessage.error('请填写所有必填字段')
    return
  }

  try {
    let response
    if (isEditMode.value) {
      // 更新友链
      const updateData = {
        name: editForm.name,
        url: editForm.url,
        description: editForm.description,
        avatar: editForm.avatar,
        isDefault: editForm.isDefault
      }
      response = await axios.put(`/api/friend-link/update/${editForm.id}`, updateData)
    } else {
      // 添加友链（这里暂时不实现添加功能，只保留编辑功能）
      const formData = new FormData()
      formData.append('name', editForm.name)
      formData.append('url', editForm.url)
      formData.append('description', editForm.description)

      if (selectedFile.value) {
        formData.append('avatar', selectedFile.value)
      }

      response = await axios.post('/api/friend-link/add', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    }

    if (response.data.code === 200) {
      ElMessage.success(isEditMode.value ? '友链更新成功！' : '友链添加成功！')
      dialogVisible.value = false
      // 重新获取友链列表
      fetchFriendLinks()
    } else {
      ElMessage.error(response.data.message || (isEditMode.value ? '友链更新失败' : '友链添加失败'))
    }
  } catch (error) {
    console.error('提交时出错:', error)
    ElMessage.error(isEditMode.value ? '友链更新失败' : '友链添加失败')
  }
}

// 删除友链
const deleteFriendLink = async (id) => {
  try {
    const response = await axios.delete(`/api/friend-link/delete/${id}`)
    if (response.data.code === 200) {
      ElMessage.success('友链删除成功！')
      // 重新获取友链列表
      fetchFriendLinks()
    } else {
      ElMessage.error(response.data.message || '友链删除失败')
    }
  } catch (error) {
    console.error('删除友链时出错:', error)
    ElMessage.error('友链删除失败')
  }
}

onMounted(() => {
  fetchFriendLinks()
})
</script>

<style scoped>
.friend-link-admin {
  padding: 20px;
}

.friend-link-admin h2 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 7px;
}
</style>
