<template>
  <el-card class="log-monitoring-card">
    <template #header>
      <div class="card-header">
        <span>日志检测</span>
      </div>
    </template>

    <!-- 日志列表 -->
    <el-table
        :data="logs"
        border
        style="width: 100%"
        max-height="600px"
        @row-click="handleRowClick"
    >
      <el-table-column prop="timestamp" label="时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.timestamp) }}
        </template>
      </el-table-column>

      <el-table-column prop="content" label="日志内容">
        <template #default="scope">
          <span :class="{ 'alarm-content': scope.row.isAlarm }">
            {{ scope.row.content }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="isAlarm" label="报警状态" width="120">
        <template #default="scope">
          <el-tag
              :type="scope.row.isAlarm ? 'danger' : 'success'"
              effect="dark"
          >
            {{ scope.row.isAlarm ? '报警' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <!-- 加载状态（可选） -->
    <div v-if="loading" class="loading-overlay">
      <el-icon><loading /></el-icon>
      正在加载日志...
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElLoading, ElMessage } from 'element-plus'
import axios from 'axios' // 确保已安装
import { Loading } from '@element-plus/icons-vue'

const logs = ref([])
const loading = ref(false)

// 时间格式化（假设 timestamp 是时间戳）
const formatDate = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString() // 根据需求调整格式
}

// 获取日志数据
const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/getlog')
    logs.value = res.data.map(log => ({
      ...log,
      timestamp: log.timestamp || Date.now() // 处理可能缺失的时间戳
    }))
  } catch (error) {
    ElMessage.error('加载日志失败，请重试')
  } finally {
    loading.value = false
  }
}

// 行点击事件（可选）
const handleRowClick = (row) => {
  ElMessage.info(`查看日志详情：${row.content}`)
}

onMounted(() => {
  fetchLogs()
  // 可选：每5秒自动刷新
  setInterval(fetchLogs, 5000)
})
</script>

<style scoped>
.log-monitoring-card {
  margin: 20px;
  padding: 20px;
}

.el-table {
  margin-top: 20px;
}

.alarm-content {
  color: #f56c6c;
  font-weight: bold;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}

.loading-overlay .el-icon {
  font-size: 48px;
  margin-bottom: 10px;
}
</style>
