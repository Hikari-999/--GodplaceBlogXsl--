<template>
  <div class="message-management">
    <!-- 查询区域 -->
    <div class="search-area">
      <el-input
          v-model="searchContent"
          placeholder="请输入留言内容关键词"
          style="width: 200px; margin-right: 10px;"
          clearable
      />
      <el-input
          v-model="searchAuthor"
          placeholder="请输入留言者"
          style="width: 200px; margin-right: 10px;"
          clearable
      />
      <el-button type="primary" @click="fetchMessages">查询</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 留言列表 -->
    <el-table
        :data="filteredMessages"
        style="width: 100%; margin-top: 20px;"
        border
        stripe
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="留言内容" show-overflow-tooltip />
      <el-table-column prop="author" label="留言者" width="150" />
      <el-table-column prop="createTime" label="留言时间" width="200">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button
              type="danger"
              size="small"
              @click="deleteMessage(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="messages.length"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: center;"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { get, post } from '@/net'

// 数据相关
const messages = ref([])
const currentPage = ref(1)
const pageSize = ref(10)

// 搜索相关
const searchContent = ref('')
const searchAuthor = ref('')

// 计算过滤后的留言
const filteredMessages = computed(() => {
  let result = messages.value

  // 根据内容关键词过滤
  if (searchContent.value) {
    result = result.filter(msg =>
        msg.content.toLowerCase().includes(searchContent.value.toLowerCase())
    )
  }

  // 根据留言者过滤
  if (searchAuthor.value) {
    result = result.filter(msg =>
        msg.author.toLowerCase().includes(searchAuthor.value.toLowerCase())
    )
  }

  // 分页处理
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return result.slice(start, end)
})

// 获取所有留言
const fetchMessages = () => {
  get('/api/message/list', (data) => {
    messages.value = data
  }, (error) => {
    ElMessage.error('获取留言失败: ' + error.message)
  })
}

// 删除留言
const deleteMessage = (id) => {
  ElMessageBox.confirm(
      '确定要删除这条留言吗？',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    post(`/api/message/delete/${id}`, {}, () => {
      // 删除成功后重新获取留言列表
      fetchMessages()
      ElMessage.success('删除成功')
    }, (error) => {
      ElMessage.error('删除失败: ' + error.message)
    })
  }).catch(() => {
    // 用户取消删除
  })
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN')
}

// 重置搜索
const resetSearch = () => {
  searchContent.value = ''
  searchAuthor.value = ''
  fetchMessages()
}

// 分页相关方法
const handleSizeChange = (val) => {
  pageSize.value = val
  currentPage.value = 1
}

const handleCurrentChange = (val) => {
  currentPage.value = val
}

onMounted(() => {
  fetchMessages()
})
</script>

<style scoped>.message-management {
  padding: 20px;
}

.message-management h3 {
  color: #000000;
}

.search-area {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.search-area :deep(.el-input__inner) {
  color: #000000;
}

.search-area :deep(.el-input__inner::placeholder) {
  color: #999999;
}

.el-table {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

:deep(.message-table .el-table__cell) {
  color: #000000;
}

:deep(.message-table .el-table__header th) {
  color: #000000;
  font-weight: bold;
}
</style>
