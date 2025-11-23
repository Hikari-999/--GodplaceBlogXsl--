<template>
  <div class="talk-management" v-loading="globalLoading">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="header-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">随笔管理</span>
              <div class="header-actions">
                <el-button @click="refreshData" :icon="Refresh">
                  刷新
                </el-button>
              </div>
            </div>
          </template>
          <div class="stats-container">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ total }}</div>
                  <div class="stat-label">总计</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ likesCount }}</div>
                  <div class="stat-label">总点赞</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ commentsCount }}</div>
                  <div class="stat-label">总评论</div>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-item">
                  <div class="stat-value">{{ viewsCount }}</div>
                  <div class="stat-label">总浏览</div>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 发布/编辑表单 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>{{ isEditing ? '编辑随笔' : '发布随笔' }}</span>
            </div>
          </template>

          <el-form :model="talkForm" :rules="rules" ref="talkFormRef" label-width="80px">
            <el-form-item label="内容" prop="content">
              <el-input
                  v-model="talkForm.content"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入随笔内容"
                  maxlength="500"
                  show-word-limit
              />
            </el-form-item>

            <el-form-item label="图片" prop="imageList">
              <el-upload
                  class="image-uploader"
                  :auto-upload="false"
                  :on-change="handleImageChange"
                  :on-remove="handleImageRemove"
                  :file-list="fileList"
                  list-type="picture-card"
                  multiple
              >
                <el-icon><Plus /></el-icon>
                <div class="el-upload__text">
                  点击上传图片
                </div>
              </el-upload>
              <div class="upload-tip">
                支持上传jpg/png图片，单张图片不超过5MB
              </div>
              <div class="image-preview" v-if="talkForm.imageList && talkForm.imageList.length > 0">
                <div
                    v-for="(image, index) in talkForm.imageList"
                    :key="index"
                    class="image-item"
                >
                  <el-image
                      :src="getImageUrl(image)"
                      fit="cover"
                      class="preview-image"
                      :preview-src-list="talkForm.imageList.map(img => getImageUrl(img))"
                  />
                  <el-button
                      type="danger"
                      size="small"
                      @click="removeImage(index)"
                      circle
                  >
                    ×
                  </el-button>
                </div>
              </div>
            </el-form-item>

            <el-form-item label="标签" prop="tags">
              <el-select
                  v-model="talkForm.tags"
                  multiple
                  filterable
                  allow-create
                  default-first-option
                  placeholder="请选择或输入标签"
                  style="width: 100%"
              >
                <el-option
                    v-for="tag in availableTags"
                    :key="tag"
                    :label="tag"
                    :value="tag"
                />
              </el-select>
            </el-form-item>

            <el-form-item>
              <el-button
                  type="primary"
                  @click="submitTalk"
                  :loading="submitLoading"
              >
                {{ isEditing ? '更新' : '发布' }}
              </el-button>
              <el-button @click="resetForm">重置</el-button>
              <el-button v-if="isEditing" @click="cancelEdit">取消编辑</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 随笔列表 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="list-card">
          <template #header>
            <div class="card-header">
              <span>随笔列表</span>
              <div class="search-container">
                <el-input
                    v-model="searchKeyword"
                    placeholder="搜索随笔内容"
                    style="width: 200px; margin-left: 20px"
                    clearable
                    @clear="fetchTalks"
                    @keyup.enter="fetchTalks"
                >
                  <template #append>
                    <el-button @click="fetchTalks">
                      <el-icon><Search /></el-icon>
                    </el-button>
                  </template>
                </el-input>
              </div>
            </div>
          </template>

          <el-table
              :data="talks"
              v-loading="loading"
              stripe
              style="width: 100%"
              row-key="id"
          >
            <el-table-column prop="id" label="ID" width="60" />
            <el-table-column prop="content" label="内容" min-width="200">
              <template #default="scope">
                <div class="content-preview">{{ scope.row.content }}</div>
              </template>
            </el-table-column>
            <el-table-column label="图片" width="100">
              <template #default="scope">
                <div v-if="scope.row.imageList && scope.row.imageList.length > 0">
                  <el-image
                      :src="getImageUrl(scope.row.imageList[0])"
                      fit="cover"
                      class="table-image"
                      :preview-src-list="scope.row.imageList.map(img => getImageUrl(img))"
                  />
                  <div v-if="scope.row.imageList.length > 1" class="image-count">
                    +{{ scope.row.imageList.length - 1 }}
                  </div>
                </div>
                <span v-else>无</span>
              </template>
            </el-table-column>
            <el-table-column label="标签" width="150">
              <template #default="scope">
                <el-tag
                    v-for="(tag, index) in scope.row.tags"
                    :key="index"
                    size="small"
                    style="margin-right: 5px"
                >
                  {{ tag }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="likes" label="点赞" width="70" />
            <el-table-column prop="comments" label="评论" width="70" />
            <el-table-column prop="views" label="浏览" width="70" />
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button size="small" @click="editTalk(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteTalk(scope.row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
                v-model:current-page="currentPage"
                v-model:page-size="pageSize"
                :page-sizes="[10, 20, 50]"
                :total="total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 评论管理 -->
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card class="comments-card">
          <template #header>
            <div class="card-header">
              <span>评论管理</span>
              <div class="header-actions">
                <el-button @click="loadAllComments" :icon="Refresh">
                  刷新评论
                </el-button>
              </div>
            </div>
          </template>

          <el-table
              :data="allComments"
              v-loading="commentsLoading"
              stripe
              style="width: 100%"
          >
            <el-table-column prop="id" label="评论ID" width="80" />
            <el-table-column prop="talkId" label="随笔ID" width="80" />
            <el-table-column prop="content" label="评论内容" min-width="200">
              <template #default="scope">
                <div class="comment-content-preview">{{ scope.row.content }}</div>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="评论者" width="120" />
            <el-table-column prop="createTime" label="评论时间" width="160">
              <template #default="scope">
                {{ formatTime(scope.row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" fixed="right">
              <template #default="scope">
                <el-button size="small" type="danger" @click="deleteComment(scope.row.id)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pagination-container">
            <el-pagination
                v-model:current-page="commentsPage"
                v-model:page-size="commentsPageSize"
                :page-sizes="[10, 20, 50]"
                :total="commentsTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleCommentsSizeChange"
                @current-change="handleCommentsCurrentChange"
            />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>


<script setup>
import { ref, reactive, onMounted, nextTick, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Refresh } from '@element-plus/icons-vue'
import axios from 'axios'
import { get, del } from '@/net'

// 表单引用
const talkFormRef = ref()

// 状态变量
const talks = ref([])
const loading = ref(false)
const globalLoading = ref(false)
const submitLoading = ref(false)
const isEditing = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')
const availableTags = ref([])
const fileList = ref([])
const imageFiles = ref([]) // 存储实际的文件对象

// 评论相关变量
const allComments = ref([])
const commentsLoading = ref(false)
const commentsPage = ref(1)
const commentsPageSize = ref(10)
const commentsTotal = ref(0)

// 统计数据
const likesCount = computed(() => {
  return talks.value.reduce((sum, talk) => sum + (talk.likes || 0), 0)
})

const commentsCount = computed(() => {
  return talks.value.reduce((sum, talk) => sum + (talk.comments || 0), 0)
})

const viewsCount = computed(() => {
  return talks.value.reduce((sum, talk) => sum + (talk.views || 0), 0)
})

// 表单数据
const talkForm = reactive({
  id: null,
  content: '',
  imageList: [],
  tags: []
})

// 表单验证规则
const rules = {
  content: [
    { required: true, message: '请输入随笔内容', trigger: 'blur' }
  ]
}

// 刷新数据
const refreshData = () => {
  globalLoading.value = true
  fetchTalks()
  loadAllComments()
  setTimeout(() => {
    globalLoading.value = false
    ElMessage.success('数据刷新成功')
  }, 500)
}

// 格式化时间
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}
// 获取图片完整URL
const getImageUrl = (imagePath) => {
  // 如果是完整URL则直接返回
  if (imagePath && (imagePath.startsWith('http://') || imagePath.startsWith('https://'))) {
    return imagePath;
  }

  // 如果是相对路径，则添加基础URL
  if (imagePath && imagePath.startsWith('/')) {
    // 使用axios的baseURL作为基础URL
    const baseURL = axios.defaults.baseURL || '';
    return baseURL + imagePath;
  }

  return imagePath;
}
// 图片上传前检查
const beforeImageUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!');
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
  }
  return isImage && isLt5M;
}

// 处理图片选择
const handleImageChange = (file, fileList) => {
  // 检查文件类型和大小
  if (!beforeImageUpload(file.raw)) {
    // 从文件列表中移除无效文件
    const index = fileList.findIndex(f => f.uid === file.uid);
    if (index > -1) {
      fileList.splice(index, 1);
    }
    return false;
  }

  // 将文件添加到imageFiles数组中
  imageFiles.value.push(file.raw);
  fileList.value = [...fileList];
}

// 处理图片移除
const handleImageRemove = (file, fileList) => {
  // 找到并移除对应的文件
  const index = imageFiles.value.findIndex(f => f.uid === file.uid);
  if (index > -1) {
    imageFiles.value.splice(index, 1);
  }
  fileList.value = [...fileList];
}

// 移除图片
const removeImage = (index) => {
  talkForm.imageList.splice(index, 1);
  imageFiles.value.splice(index, 1);
  fileList.value.splice(index, 1);
}

// 上传图片文件
const uploadImages = async () => {
  if (imageFiles.value.length === 0) {
    return [];
  }

  const formData = new FormData();
  imageFiles.value.forEach(file => {
    formData.append('images', file);
  });

  try {
    const response = await axios.post('/api/talk/upload-images', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });

    if (response.data.code === 200) {
      return response.data.data;
    } else {
      throw new Error(response.data.message);
    }
  } catch (error) {
    ElMessage.error('图片上传失败: ' + (error.message || '未知错误'));
    throw error;
  }
}

// 获取随笔列表
const fetchTalks = () => {
  loading.value = true
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value
  }

  if (searchKeyword.value) {
    // 如果有搜索关键词，可以添加到参数中
    params.keyword = searchKeyword.value
  }

  axios.get('/api/talk/page', { params })
      .then(response => {
        const pageData = response.data.data
        talks.value = pageData.records
        total.value = pageData.total
        extractTagsFromTalks()
      })
      .catch(error => {
        console.error('获取随笔列表失败:', error)
        ElMessage.error('获取随笔列表失败')
      })
      .finally(() => {
        loading.value = false
      })
}

// 获取所有评论
const loadAllComments = () => {
  commentsLoading.value = true
  // 获取所有随笔的评论
  get('/api/talk/list', (data) => {
    let comments = []
    data.forEach(talk => {
      if (talk.commentList && talk.commentList.length > 0) {
        talk.commentList.forEach(comment => {
          comment.talkContent = talk.content.substring(0, 30) + (talk.content.length > 30 ? '...' : '') // 添加随笔内容预览
          comments.push(comment)
        })
      }
    })
    allComments.value = comments
    commentsTotal.value = comments.length
    commentsLoading.value = false
  }, (message) => {
    console.error('获取评论列表失败:', message)
    ElMessage.error('获取评论列表失败: ' + message)
    commentsLoading.value = false
  })
}

// 删除评论
const deleteComment = (id) => {
  ElMessageBox.confirm('确定要删除这条评论吗？此操作不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    del(`/api/talk/comment/${id}`, () => {
      ElMessage.success('删除成功')
      loadAllComments()
    }, (message) => {
      console.error('删除评论失败:', message)
      ElMessage.error('删除评论失败: ' + message)
    })
  }).catch(() => {
    // 用户取消删除
  })
}

// 从随笔中提取所有标签
const extractTagsFromTalks = () => {
  const tagsSet = new Set()
  talks.value.forEach(talk => {
    if (talk.tags && Array.isArray(talk.tags)) {
      talk.tags.forEach(tag => tagsSet.add(tag))
    }
  })
  availableTags.value = Array.from(tagsSet)
}

// 提交随笔（发布或更新）
const submitTalk = async () => {
  const valid = await talkFormRef.value.validate();
  if (valid) {
    submitLoading.value = true

    try {
      // 如果有新图片需要上传，则先上传图片
      if (imageFiles.value.length > 0) {
        const imagePaths = await uploadImages();
        talkForm.imageList = [...talkForm.imageList, ...imagePaths];
      }

      // 准备提交数据
      const requestData = {
        ...talkForm
      }

      const url = isEditing.value ? '/api/talk/update' : '/api/talk/publish'
      const method = isEditing.value ? 'put' : 'post'

      await axios({
        method: method,
        url: url,
        data: requestData
      })

      ElMessage.success(isEditing.value ? '随笔更新成功' : '随笔发布成功')
      resetForm()
      fetchTalks()
      loadAllComments()
    } catch (error) {
      console.error(isEditing.value ? '更新随笔失败:' : '发布随笔失败:', error)
      ElMessage.error(isEditing.value ? '更新随笔失败' : '发布随笔失败')
    } finally {
      submitLoading.value = false
    }
  }
}

// 编辑随笔
const editTalk = (talk) => {
  isEditing.value = true
  talkForm.id = talk.id
  talkForm.content = talk.content
  // 处理图片URL
  talkForm.imageList = (talk.imageList || []).map(img => getImageUrl(img)) || []
  talkForm.tags = talk.tags || []

  // 重置文件列表
  fileList.value = []
  imageFiles.value = []

  // 滚动到表单顶部
  nextTick(() => {
    document.querySelector('.form-card').scrollIntoView({ behavior: 'smooth' })
  })
}

// 取消编辑
const cancelEdit = () => {
  resetForm()
}

// 重置表单
const resetForm = () => {
  talkFormRef.value.resetFields()
  talkForm.id = null
  talkForm.content = ''
  talkForm.imageList = []
  talkForm.tags = []
  fileList.value = []
  imageFiles.value = []
  isEditing.value = false
}

// 删除随笔
const deleteTalk = (id) => {
  ElMessageBox.confirm('确定要删除这条随笔吗？此操作不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    axios.delete(`/api/talk/${id}`)
        .then(() => {
          ElMessage.success('删除成功')
          fetchTalks()
          loadAllComments()
          if (isEditing.value && talkForm.id === id) {
            resetForm()
          }
        })
        .catch(error => {
          console.error('删除随笔失败:', error)
          ElMessage.error('删除随笔失败')
        })
  }).catch(() => {
    // 用户取消删除
  })
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchTalks()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchTalks()
}

// 评论分页相关方法
const handleCommentsSizeChange = (val) => {
  commentsPageSize.value = val
  loadAllComments()
}

const handleCommentsCurrentChange = (val) => {
  commentsPage.value = val
  loadAllComments()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchTalks()
  loadAllComments()
})
</script>


<style scoped>
.talk-management {
  padding: 20px;
  min-height: calc(100vh - 120px);
}

.header-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stats-container {
  margin-top: 10px;
}

.stat-item {
  text-align: center;
  padding: 15px 0;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: var(--el-color-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}

.form-card, .list-card {
  margin-bottom: 20px;
}

.content-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table-image {
  width: 50px;
  height: 50px;
  border-radius: 4px;
}

.image-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: center;
}

.image-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.image-item {
  position: relative;
  width: 100px;
  height: 100px;
}

.preview-image {
  width: 100%;
  height: 100%;
  border-radius: 4px;
}

.image-item .el-button {
  position: absolute;
  top: -10px;
  right: -10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.image-uploader .el-upload--picture-card {
  width: 100px;
  height: 100px;
  line-height: 100px;
}

.image-uploader .el-upload-list--picture-card .el-upload-list__item {
  width: 100px;
  height: 100px;
}

.upload-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 5px;
}

.search-container {
  display: flex;
  align-items: center;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .stat-item {
    padding: 10px 0;
  }

  .stat-value {
    font-size: 20px;
  }

  .el-col-6 {
    width: 50%;
    margin-bottom: 10px;
  }
}

@media (max-width: 768px) {
  .talk-management {
    padding: 10px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-actions {
    margin-top: 10px;
    align-self: flex-end;
  }

  .search-container {
    margin-left: 0 !important;
    margin-top: 10px;
  }
}
</style>
