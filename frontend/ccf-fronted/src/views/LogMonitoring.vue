<template>
  <el-card class="log-monitoring-card">
    <template #header>
      <div class="card-header">
        <span>日志检测</span>
      </div>
    </template>

    <!-- 分页控件 -->
    <el-pagination
        :current-page="currentPage + 1"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
        style="margin-bottom: 20px;"
    />

    <!-- 日志列表 -->
    <el-table
        :data="logs"
        border
        style="width: 100%"
        max-height="600px"
    >
      <el-table-column prop="timestamp" label="时间" width="180">
        <template #default="scope">
          {{ formatDate(scope.row.timestamp) }}
        </template>
      </el-table-column>

      <el-table-column prop="message" label="日志内容">
        <template #default="scope">
          <span :class="{ 'alarm-content': scope.row.status === 'error' }">
            {{ scope.row.message }}
          </span>
        </template>
      </el-table-column>

      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag
              :type="scope.row.status === 'error' ? 'danger' : 'success'"
              effect="dark"
          >
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column prop="host" label="主机" width="150" />
      <el-table-column prop="logFileName" label="日志文件" width="200" />
    </el-table>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-overlay">
      <el-icon><loading /></el-icon>
      正在加载日志...
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import axios from 'axios';
import { Loading } from '@element-plus/icons-vue';
import apiClient from "@/api/axiosInstance.js";

const logs = ref([]);
const total = ref(0);
const currentPage = ref(0);
const pageSize = ref(10);
const loading = ref(false);

// 时间格式化（处理date-time格式字符串）
const formatDate = (timestampStr) => {
  const date = new Date(timestampStr);
  return date.toLocaleString(); // 根据需求调整格式
};

// 获取日志数据
const fetchLogs = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    };
    const config = {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('token')}` // 假设token存储在localStorage
      }
    };

    const res = await apiClient().get('/logs/list', { params, ...config });
    logs.value = res.data.list;
    total.value = res.data.total;
  } catch (error) {
    ElMessage.error('加载日志失败，请检查网络或权限');
  } finally {
    loading.value = false;
  }
};

// 分页切换处理
const handlePageChange = (newPage) => {
  currentPage.value = newPage - 1; // 转换为0-based页码
  fetchLogs();
};

// 初始化加载
onMounted(() => {
  fetchLogs();
});
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
